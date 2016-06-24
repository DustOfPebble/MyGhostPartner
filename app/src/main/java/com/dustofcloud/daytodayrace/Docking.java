package com.dustofcloud.daytodayrace;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;

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

    private ArrayList<Statistic> Speeds;
    private ArrayList<Statistic> HeartBeats;

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

        HeartBeatSensor = (ControlSwitch) findViewById(R.id.heartbeat_provider);
        HeartBeatSensor.registerModes(SharedConstants.ConnectedHeartBeat, SharedConstants.DisconnectedHeartBeat);
        HeartBeatSensor.registerManager(this);
        HeartBeatSensor.setMode(BackendService.getModeHeartBeat());


        // Hardcoded settings for Speed in left Monitor
        LeftMonitor = (Monitor) findViewById(R.id.left_monitor);
        LeftMonitor.setIcon( BitmapFactory.decodeResource(getResources(), R.drawable.speed_thumb));
        LeftMonitor.setRuleSettings(10,5,1f,0f,80f); // One Label  every 1 km/h
        LeftMonitor.setUnit("km/h");
        LeftMonitor.setVisibility(View.INVISIBLE);

        // Hardcoded settings for Heartbeat in right Monitor
        RightMonitor = (Monitor) findViewById(R.id.right_monitor);
        RightMonitor.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.heart_thumb));
        RightMonitor.setRuleSettings(12,5,1f,20f,220f); // One Label every 5 bpm
        RightMonitor.setUnit("bpm");
        RightMonitor.setVisibility(View.INVISIBLE);

        Speeds = new ArrayList<Statistic>();
        HeartBeats = new ArrayList<Statistic>();

        EventTrigger=new Handler();
        // Registering Timeout triggers
        EventTrigger.postDelayed(task,EventsDelay);
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

        // Remove all registered Timeout triggers
        EventTrigger.removeCallbacks(task);
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
            BackendService.storeModeHeartBeat(Status);
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
        GeoData LastGPS = BackendService.getLastUpdate();
        if (null == LastGPS) {
            // Registering Timeout triggers
            EventTrigger.postDelayed(task,EventsDelay);
            return;
        }
        // Refreshing Statistics
        processLocationChanged(LastGPS);
        // Refreshing Map Display
        MapView.processLocationChanged(LastGPS);
    }

    @Override
    public void processLocationChanged(GeoData geoInfo){
        if (BackendService == null) return;

        // Remove all registered Timeout triggers
        EventTrigger.removeCallbacks(task);

        // Setting collection area
        PointF SizeSelection = BackendService.getExtractStatisticsSize();
        PointF ViewCenter = geoInfo.getCoordinate();
        searchZone.set(ViewCenter.x - SizeSelection.x / 2, ViewCenter.y - SizeSelection.y / 2,
                       ViewCenter.x + SizeSelection.x / 2, ViewCenter.y + SizeSelection.y / 2  );

        // Collecting data from backend
        ArrayList<GeoData> CollectedStatistics = BackendService.filter(BackendService.extract(searchZone));

        // Updating Speeds Statistics
        LeftMonitor.setVisibility(View.VISIBLE);
        Speeds.clear();
        Speeds.add(new Statistic(geoInfo.getSpeed()*3.6f,geoInfo.getElapsedDays()));
        for (GeoData item: CollectedStatistics) {
            Speeds.add(new Statistic(item.getSpeed()*3.6f,item.getElapsedDays()));
        }
        LeftMonitor.updateStatistics(Speeds);

        // Updating HeartBeats Statistics
//        if (geoInfo.getHeartbeat() == -1) { RightMonitor.setVisibility(View.INVISIBLE); return; }
        RightMonitor.setVisibility(View.VISIBLE);
        HeartBeats.clear();
        HeartBeats.add(new Statistic(geoInfo.getHeartbeat(),geoInfo.getElapsedDays()));
        for (GeoData item: CollectedStatistics) {
            if (item.getHeartbeat() == -1) return;
            Speeds.add(new Statistic(item.getHeartbeat(),item.getElapsedDays()));
        }
        RightMonitor.updateStatistics(HeartBeats);

        // Registering Timeout triggers
        EventTrigger.postDelayed(task,EventsDelay);
    }
}
