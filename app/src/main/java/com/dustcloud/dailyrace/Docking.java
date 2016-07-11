package com.dustcloud.dailyrace;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
// ToDo: inflate XML Widget from explicit call instead of embedding into Activity XML
public class Docking extends Activity implements EventsProcessGPS {

    private Handler EventTrigger = new Handler();
    private Runnable task = new Runnable() { public void run() { loadStatus();} };
    private int EventsDelay = 2000;

    private ControlSwitch SleepLocker = null;
    private ControlSwitch BatterySaver = null;
    private ControlSwitch LightEnhancer = null;
    private ControlSwitch GPSProvider = null;
    private ControlSwitch HeartBeatSensor = null;

    private Monitor LeftMonitor = null;
    private Monitor RightMonitor = null;

    private MapManager MapView = null;

    private RectF searchZone = new RectF();

    private ArrayList<Float> Speeds;
    private ArrayList<Float> HeartBeats;

    private int BackPressedCount = 0;
    private DataManager BackendService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docking);

        BackendService = (DataManager) getApplication();
        BackendService.setUpdateCallback(this);

        MapView = (MapManager)  findViewById(R.id.map_manager);
        MapView.setBackend(BackendService);

        SleepLocker = (ControlSwitch) findViewById(R.id.switch_sleep_locker);
        SleepLocker.registerModes(SharedConstants.SleepLocked, SharedConstants.SleepUnLocked);
        SleepLocker.registerManager(this);
        SleepLocker.setMode(BackendService.getModeSleep());
        if (BackendService.getModeSleep() == SharedConstants.SleepLocked)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        LightEnhancer = (ControlSwitch) findViewById(R.id.switch_light_enhancer);
        LightEnhancer.registerModes(SharedConstants.LightEnhanced, SharedConstants.LightNormal);
        LightEnhancer.registerManager(this);
        LightEnhancer.setMode(BackendService.getModeLight());
        LightEnhancer.setVisibility(View.INVISIBLE);

        BatterySaver = (ControlSwitch) findViewById(R.id.switch_battery_saver);
        BatterySaver.registerModes(SharedConstants.BatteryDrainMode, SharedConstants.BatterySaveMode);
        BatterySaver.registerManager(this);
        BatterySaver.setMode(BackendService.getModeBattery());
        BatterySaver.setVisibility(View.INVISIBLE);

        GPSProvider = (ControlSwitch) findViewById(R.id.gps_provider);
        GPSProvider.registerModes(SharedConstants.LiveGPS, SharedConstants.ReplayedGPS);
        GPSProvider.registerManager(this);
        GPSProvider.setMode(BackendService.getModeGPS());

        if (BluetoothConstants.hasLowEnergyCapabilities) {
            HeartBeatSensor = (ControlSwitch) findViewById(R.id.heartbeat_provider);
            HeartBeatSensor.registerModes(SharedConstants.ConnectedHeartBeat, SharedConstants.DisconnectedHeartBeat);
            HeartBeatSensor.registerManager(this);
            HeartBeatSensor.setMode(BackendService.getModeHeartBeat());
            HeartBeatSensor.setVisibility(View.VISIBLE);
        }

        // Hardcoded settings for Speed in left Monitor
        LeftMonitor = (Monitor) findViewById(R.id.left_monitor);
        LeftMonitor.setIcon( BitmapFactory.decodeResource(getResources(), R.drawable.speed_thumb));
        LeftMonitor.setNbTicksDisplayed(18);
        LeftMonitor.setNbTicksLabel(5);
        LeftMonitor.setTicksStep(1f);
        LeftMonitor.setPhysicRange(0f,80f);
        LeftMonitor.setUnit("km/h");
        LeftMonitor.setVisibility(View.INVISIBLE);

        // Hardcoded settings for Heartbeat in right Monitor
        RightMonitor = (Monitor) findViewById(R.id.right_monitor);
        RightMonitor.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.heart_thumb));
        RightMonitor.setNbTicksDisplayed(22);
        RightMonitor.setNbTicksLabel(10);
        RightMonitor.setTicksStep(1f);
        RightMonitor.setPhysicRange(20f,220f);
        RightMonitor.setUnit("bpm");
        RightMonitor.setVisibility(View.INVISIBLE);

        Speeds = new ArrayList<Float>();
        HeartBeats = new ArrayList<Float>();

    }

    @Override
    protected void onPause() {
        super.onPause();
        BackendService.setActivityMode(SharedConstants.SwitchBackground);
        if (BackendService.getModeGPS() == SharedConstants.LiveGPS) {
            Toast.makeText(this,getResources().getString(R.string.GPS_record_background), Toast.LENGTH_SHORT).show();
        }
        if (BackendService.getModeGPS() == SharedConstants.ReplayedGPS) {
            Toast.makeText(this,getResources().getString(R.string.GPS_replay_background), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, getResources().getString(R.string.restore_display), Toast.LENGTH_SHORT).show();
        BackPressedCount = 0;
        BackendService = (DataManager) DataManager.getBackend();
        BackendService.setActivityMode(SharedConstants.SwitchForeground);

        // Refresh all button internal state
        loadStatus();
    }

    public void onStatusChanged(short Status) {
        if (Status == SharedConstants.SleepLocked) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            BackendService.storeModeSleep(Status);
            SleepLocker.setMode(Status);
            }
        if (Status == SharedConstants.SleepUnLocked) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            BackendService.storeModeSleep(Status);
            SleepLocker.setMode(Status);
        }

        if (Status == SharedConstants.LiveGPS) {
            BackendService.storeModeGPS(Status);
            GPSProvider.setMode(Status);
        }
        if (Status == SharedConstants.ReplayedGPS) {
            BackendService.storeModeGPS(Status);
            GPSProvider.setMode(Status);
        }

        if (Status == SharedConstants.LightEnhanced) {
            BackendService.storeModeLight(Status);
            LightEnhancer.setMode(Status);
        }
        if (Status == SharedConstants.LightNormal) {
            BackendService.storeModeLight(Status);
            LightEnhancer.setMode(Status);
        }

        if (Status == SharedConstants.BatteryDrainMode) {
            BackendService.storeModeBattery(Status);
            BatterySaver.setMode(Status);
        }
        if (Status == SharedConstants.BatterySaveMode) {
            BackendService.storeModeBattery(Status);
            BatterySaver.setMode(Status);
        }
        if (Status == SharedConstants.ConnectedHeartBeat) {
            BackendService.storeModeHeartBeat(SharedConstants.SearchHeartBeat);
            EventTrigger.postDelayed(task, BluetoothConstants.SCAN_TIMEOUT + EventsDelay);
        }
        if (Status == SharedConstants.DisconnectedHeartBeat) {
            BackendService.storeModeHeartBeat(Status);
        }
    }

    @Override
    public void onBackPressed() {
        BackPressedCount++;
        if (BackPressedCount > 1) {
            BackendService.shutdown();
            this.finish();
            System.exit(0);
            super.onBackPressed();
        }
        else { Toast.makeText(this, getResources().getString(R.string.message_back_key_pressed), Toast.LENGTH_SHORT).show(); }
    }

    private void loadStatus() {
        if (BackendService == null) return;
        // Checking for a pending message
        String ToastMessage = BackendService.getBackendMessage();
        if (!ToastMessage.isEmpty()) Toast.makeText(this, ToastMessage, Toast.LENGTH_SHORT).show();

        // updating Buttons status
        HeartBeatSensor.setMode(BackendService.getModeHeartBeat());
        GPSProvider.setMode(BackendService.getModeGPS());
        LightEnhancer.setMode(BackendService.getModeLight());
        BatterySaver.setMode(BackendService.getModeBattery());
        SleepLocker.setMode(BackendService.getModeSleep());

        // Force a refreshed display
        SurveySnapshot LastGPS = BackendService.getLastSnapshot();
        if (null == LastGPS) { return; }
        // Refreshing Statistics
        processLocationChanged(LastGPS);
        // Refreshing Map Display
        MapView.processLocationChanged(LastGPS);
    }

    @Override
    public void processLocationChanged(SurveySnapshot Snapshot){
        if (BackendService == null) return;

        // Setting collection area
        Vector SizeSelection = BackendService.getExtractStatisticsSize();
        Vector ViewCenter = Snapshot.copy();
        searchZone.set(ViewCenter.x - SizeSelection.x / 2, ViewCenter.y - SizeSelection.y / 2,
                       ViewCenter.x + SizeSelection.x / 2, ViewCenter.y + SizeSelection.y / 2  );

        // Collecting data from backend
        ArrayList<SurveySnapshot> CollectedStatistics = BackendService.filter(BackendService.extract(searchZone),Snapshot);

        // Updating Speeds Statistics
        if (LeftMonitor.getVisibility() == View.INVISIBLE) LeftMonitor.setVisibility(View.VISIBLE);
        Speeds.clear();
        Speeds.add(Float.valueOf(Snapshot.getSpeed()*3.6f));
        for (SurveySnapshot item: CollectedStatistics) {
            Speeds.add(Float.valueOf(item.getSpeed()*3.6f));
        }
        if (Speeds.isEmpty()) Speeds.add(Float.valueOf(Snapshot.getSpeed()*3.6f));
        LeftMonitor.updateStatistics(Speeds);

        // Updating HeartBeats Statistics
        if (Snapshot.getHeartbeat() == -1) { RightMonitor.setVisibility(View.INVISIBLE); return; }
        if (RightMonitor.getVisibility() == View.INVISIBLE) RightMonitor.setVisibility(View.VISIBLE);
        HeartBeats.clear();
        HeartBeats.add(Float.valueOf(Snapshot.getHeartbeat()));
        for (SurveySnapshot item: CollectedStatistics) {
            if (item.getHeartbeat() == -1) continue;
            HeartBeats.add(Float.valueOf(item.getHeartbeat()));
        }
        if (HeartBeats.isEmpty()) HeartBeats.add(Float.valueOf(Snapshot.getHeartbeat()));
        RightMonitor.updateStatistics(HeartBeats);
    }
}
