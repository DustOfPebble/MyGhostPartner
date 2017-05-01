package core.launcher.application;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

import services.GPS.EventsGPS;
import services.Sensor.SensorState;
import core.Structures.Statistics;
import core.Structures.Node;

public class Docking extends Activity implements EventsGPS {

    private Handler EventTrigger = new Handler();
    private Runnable task = new Runnable() { public void run() { loadStatus();} };
    private int EventsDelay = 2000;

    private RelativeLayout DockingManager = null;

    private ControlSwitch SleepLocker = null;
    private ControlSwitch BatterySaver = null;
    private ControlSwitch LightEnhancer = null;
    private ControlSwitch GPSProvider = null;
    private ControlSwitch HeartBeatSensor = null;

    private Monitor SpeedMonitor = null;
    private short SpeedWidgetMode = -1;
    private Monitor HeartbeatMonitor = null;
    private short HeartbeatWidgetMode = -1;

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

        DockingManager = (RelativeLayout) findViewById(R.id.manage_docking);

        BackendService = (DataManager) getApplication();
        BackendService.setUpdateCallback(this);

        MapView = (MapManager)  findViewById(R.id.map_manager);
        MapView.setBackend(BackendService);

        SleepLocker = (ControlSwitch) findViewById(R.id.switch_sleep_locker);
        SleepLocker.registerModes(Constants.SleepLocked, Constants.SleepUnLocked);
        SleepLocker.registerManager(this);
        SleepLocker.setMode(BackendService.getModeSleep());
        if (BackendService.getModeSleep() == Constants.SleepLocked)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        LightEnhancer = (ControlSwitch) findViewById(R.id.switch_light_enhancer);
        LightEnhancer.registerModes(Constants.LightEnhanced, Constants.LightNormal);
        LightEnhancer.registerManager(this);
        LightEnhancer.setMode(BackendService.getModeLight());
        LightEnhancer.setVisibility(View.INVISIBLE);

        BatterySaver = (ControlSwitch) findViewById(R.id.switch_battery_saver);
        BatterySaver.registerModes(Constants.BatteryDrainMode, Constants.BatterySaveMode);
        BatterySaver.registerManager(this);
        BatterySaver.setMode(BackendService.getModeBattery());
        BatterySaver.setVisibility(View.INVISIBLE);

        GPSProvider = (ControlSwitch) findViewById(R.id.gps_provider);
        GPSProvider.registerModes(Constants.LiveGPS, Constants.ReplayedGPS);
        GPSProvider.registerManager(this);
        GPSProvider.setMode(BackendService.getModeGPS());

        if (SensorState.hasLowEnergyCapabilities) {
            HeartBeatSensor = (ControlSwitch) findViewById(R.id.heartbeat_provider);
            HeartBeatSensor.registerModes(Constants.ConnectedHeartBeat, Constants.DisconnectedHeartBeat);
            HeartBeatSensor.registerManager(this);
            HeartBeatSensor.setMode(BackendService.getModeHeartBeat());
            HeartBeatSensor.setVisibility(View.VISIBLE);
        }

        LayoutInflater fromXML = LayoutInflater.from(this);

        // Hardcoded settings for Speed in left Monitor
        SpeedMonitor = (Monitor) fromXML.inflate(R.layout.widget_monitor, null);
        SpeedMonitor.registerManager(this);
        SpeedMonitor.setID(Constants.SpeedStatsID);
        SpeedWidgetMode = Constants.LeftBottomWidget;
        SpeedMonitor.setIcon( BitmapFactory.decodeResource(getResources(), R.drawable.speed_thumb));
        SpeedMonitor.setNbTicksDisplayed(18);
        SpeedMonitor.setNbTicksLabel(5);
        SpeedMonitor.setTicksStep(1f);
        SpeedMonitor.setPhysicRange(0f,80f);
        SpeedMonitor.setUnit("km/h");
        SpeedMonitor.setVisibility(View.INVISIBLE);

        // Hardcoded settings for Heartbeat in right Monitor
        HeartbeatMonitor = (Monitor) fromXML.inflate(R.layout.widget_monitor, null);
        HeartbeatMonitor.registerManager(this);
        HeartbeatMonitor.setID(Constants.HeatbeatStatsID);
        HeartbeatWidgetMode = Constants.RightBottomWidget;
        HeartbeatMonitor.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.heart_thumb));
        HeartbeatMonitor.setNbTicksDisplayed(22);
        HeartbeatMonitor.setNbTicksLabel(10);
        HeartbeatMonitor.setTicksStep(1f);
        HeartbeatMonitor.setPhysicRange(20f,220f);
        HeartbeatMonitor.setUnit("bpm");
        HeartbeatMonitor.setVisibility(View.INVISIBLE);

        Speeds = new ArrayList<Float>();
        HeartBeats = new ArrayList<Float>();

        applyWidgetsLayout();
    }

    private void applyWidgetsLayout(){
        Point ScreenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(ScreenSize);
        int Bounds = Math.min(ScreenSize.x,ScreenSize.y);
        int SecondaryWidgetWidth = (int)(Bounds * 0.45);
        int PrimaryWidgetWidth = (int)(Bounds * 0.65);
        RelativeLayout.LayoutParams MonitorConfig;

        // Managing SpeedMonitor Widget
        if (SpeedWidgetMode == Constants.LeftBottomWidget) {
            MonitorConfig = new RelativeLayout.LayoutParams(SecondaryWidgetWidth, (int) (SecondaryWidgetWidth * Constants.WidthToHeightFactor));
            MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        else {
            MonitorConfig = new RelativeLayout.LayoutParams(PrimaryWidgetWidth, (int) (PrimaryWidgetWidth * Constants.WidthToHeightFactor));
            MonitorConfig.addRule(RelativeLayout.CENTER_HORIZONTAL);
            MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_TOP);
         }

        SpeedMonitor.setLayoutParams(MonitorConfig);
        DockingManager.removeView(SpeedMonitor);
        DockingManager.addView(SpeedMonitor,MonitorConfig);

        // Managing HeartbeatMonitor Widget
        if (HeartbeatWidgetMode == Constants.RightBottomWidget) {
            MonitorConfig = new RelativeLayout.LayoutParams(SecondaryWidgetWidth, (int) (SecondaryWidgetWidth * Constants.WidthToHeightFactor));
            MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        else {
            MonitorConfig = new RelativeLayout.LayoutParams(PrimaryWidgetWidth, (int) (PrimaryWidgetWidth * Constants.WidthToHeightFactor));
            MonitorConfig.addRule(RelativeLayout.CENTER_HORIZONTAL);
            MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }

        HeartbeatMonitor.setLayoutParams(MonitorConfig);
        DockingManager.removeView(HeartbeatMonitor);
        DockingManager.addView(HeartbeatMonitor,MonitorConfig);

        DockingManager.invalidate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BackendService.setActivityMode(Constants.SwitchBackground);
        if (BackendService.getModeGPS() == Constants.LiveGPS) {
            Toast.makeText(this,getResources().getString(R.string.GPS_record_background), Toast.LENGTH_SHORT).show();
        }
        if (BackendService.getModeGPS() == Constants.ReplayedGPS) {
            Toast.makeText(this,getResources().getString(R.string.GPS_replay_background), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, getResources().getString(R.string.restore_display), Toast.LENGTH_SHORT).show();
        BackPressedCount = 0;
        BackendService = (DataManager) DataManager.getBackend();
        BackendService.setActivityMode(Constants.SwitchForeground);

        // Refresh all button internal state
        loadStatus();
    }

    public void onButtonStatusChanged(short Status) {
        if (Status == Constants.SleepLocked) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            BackendService.storeModeSleep(Status);
            SleepLocker.setMode(Status);
            }
        if (Status == Constants.SleepUnLocked) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            BackendService.storeModeSleep(Status);
            SleepLocker.setMode(Status);
        }

        if (Status == Constants.LiveGPS) {
            BackendService.storeModeGPS(Status);
            GPSProvider.setMode(Status);
        }
        if (Status == Constants.ReplayedGPS) {
            BackendService.storeModeGPS(Status);
            GPSProvider.setMode(Status);
        }

        if (Status == Constants.LightEnhanced) {
            BackendService.storeModeLight(Status);
            LightEnhancer.setMode(Status);
        }
        if (Status == Constants.LightNormal) {
            BackendService.storeModeLight(Status);
            LightEnhancer.setMode(Status);
        }

        if (Status == Constants.BatteryDrainMode) {
            BackendService.storeModeBattery(Status);
            BatterySaver.setMode(Status);
        }
        if (Status == Constants.BatterySaveMode) {
            BackendService.storeModeBattery(Status);
            BatterySaver.setMode(Status);
        }
        if (Status == Constants.ConnectedHeartBeat) {
            BackendService.storeModeHeartBeat(Constants.SearchHeartBeat);
            EventTrigger.postDelayed(task, SensorState.SCAN_TIMEOUT + EventsDelay);
        }
        if (Status == Constants.DisconnectedHeartBeat) {
            BackendService.storeModeHeartBeat(Status);
        }
    }

    public void moveWidget(short ID){
        if (ID == Constants.SpeedStatsID){
            if (SpeedWidgetMode == Constants.LeftBottomWidget) {
                SpeedWidgetMode = Constants.CenterTopWidget;
                HeartbeatWidgetMode = Constants.RightBottomWidget;
            }
            else SpeedWidgetMode = Constants.LeftBottomWidget;
        }

        if (ID == Constants.HeatbeatStatsID) {
            if (HeartbeatWidgetMode == Constants.RightBottomWidget) {
                HeartbeatWidgetMode = Constants.CenterTopWidget;
                SpeedWidgetMode = Constants.LeftBottomWidget;
            }
            else HeartbeatWidgetMode = Constants.RightBottomWidget;
        }

        applyWidgetsLayout();
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
        Statistics LastGPS = BackendService.getLastSnapshot();
        if (null == LastGPS) { return; }
        // Refreshing Statistics
        processLocationChanged(LastGPS);
        // Refreshing Map Display
        MapView.processLocationChanged(LastGPS);
    }

    @Override
    public void processLocationChanged(Statistics Snapshot){
        if (BackendService == null) return;

        // Refresh HeartBeat button state ...
        HeartBeatSensor.setMode(BackendService.getModeHeartBeat());

        // Setting collection area
        Node SizeSelection = BackendService.getExtractStatisticsSize();
        Node ViewCenter = Snapshot.copy();
        searchZone.set(ViewCenter.dx - SizeSelection.dx / 2, ViewCenter.dy - SizeSelection.dy / 2,
                       ViewCenter.dx + SizeSelection.dx / 2, ViewCenter.dy + SizeSelection.dy / 2  );

        // Collecting data from backend
        ArrayList<Statistics> CollectedStatistics = BackendService.filter(BackendService.extract(searchZone),Snapshot);

        // Updating Speeds Statistics
        if (SpeedMonitor.getVisibility() == View.INVISIBLE) SpeedMonitor.setVisibility(View.VISIBLE);
        Speeds.clear();
        Speeds.add(Float.valueOf(Snapshot.getSpeed()*3.6f));
        for (Statistics item: CollectedStatistics) {
            Speeds.add(Float.valueOf(item.getSpeed()*3.6f));
        }
        if (Speeds.isEmpty()) Speeds.add(Float.valueOf(Snapshot.getSpeed()*3.6f));
        SpeedMonitor.updateStatistics(Speeds);

        // Updating HeartBeats Statistics
        if (Snapshot.getHeartbeat() == -1) { HeartbeatMonitor.setVisibility(View.INVISIBLE); return; }
        if (HeartbeatMonitor.getVisibility() == View.INVISIBLE) HeartbeatMonitor.setVisibility(View.VISIBLE);
        HeartBeats.clear();
        HeartBeats.add(Float.valueOf(Snapshot.getHeartbeat()));
        for (Statistics item: CollectedStatistics) {
            if (item.getHeartbeat() == -1) continue;
            HeartBeats.add(Float.valueOf(item.getHeartbeat()));
        }
        if (HeartBeats.isEmpty()) HeartBeats.add(Float.valueOf(Snapshot.getHeartbeat()));
        HeartbeatMonitor.updateStatistics(HeartBeats);
    }
}
