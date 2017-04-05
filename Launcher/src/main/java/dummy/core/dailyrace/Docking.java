package dummy.core.dailyrace;

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
public class Docking extends Activity implements EventsProcessGPS {

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

        if (SensorState.hasLowEnergyCapabilities) {
            HeartBeatSensor = (ControlSwitch) findViewById(R.id.heartbeat_provider);
            HeartBeatSensor.registerModes(SharedConstants.ConnectedHeartBeat, SharedConstants.DisconnectedHeartBeat);
            HeartBeatSensor.registerManager(this);
            HeartBeatSensor.setMode(BackendService.getModeHeartBeat());
            HeartBeatSensor.setVisibility(View.VISIBLE);
        }

        LayoutInflater fromXML = LayoutInflater.from(this);

        // Hardcoded settings for Speed in left Monitor
        SpeedMonitor = (Monitor) fromXML.inflate(R.layout.widget_monitor, null);
        SpeedMonitor.registerManager(this);
        SpeedMonitor.setID(SharedConstants.SpeedStatsID);
        SpeedWidgetMode = SharedConstants.LeftBottomWidget;
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
        HeartbeatMonitor.setID(SharedConstants.HeatbeatStatsID);
        HeartbeatWidgetMode = SharedConstants.RightBottomWidget;
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
        if (SpeedWidgetMode == SharedConstants.LeftBottomWidget) {
            MonitorConfig = new RelativeLayout.LayoutParams(SecondaryWidgetWidth, (int) (SecondaryWidgetWidth * SharedConstants.WidthToHeightFactor));
            MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        else {
            MonitorConfig = new RelativeLayout.LayoutParams(PrimaryWidgetWidth, (int) (PrimaryWidgetWidth * SharedConstants.WidthToHeightFactor));
            MonitorConfig.addRule(RelativeLayout.CENTER_HORIZONTAL);
            MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_TOP);
         }

        SpeedMonitor.setLayoutParams(MonitorConfig);
        DockingManager.removeView(SpeedMonitor);
        DockingManager.addView(SpeedMonitor,MonitorConfig);

        // Managing HeartbeatMonitor Widget
        if (HeartbeatWidgetMode == SharedConstants.RightBottomWidget) {
            MonitorConfig = new RelativeLayout.LayoutParams(SecondaryWidgetWidth, (int) (SecondaryWidgetWidth * SharedConstants.WidthToHeightFactor));
            MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        else {
            MonitorConfig = new RelativeLayout.LayoutParams(PrimaryWidgetWidth, (int) (PrimaryWidgetWidth * SharedConstants.WidthToHeightFactor));
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

    public void onButtonStatusChanged(short Status) {
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
            EventTrigger.postDelayed(task, SensorState.SCAN_TIMEOUT + EventsDelay);
        }
        if (Status == SharedConstants.DisconnectedHeartBeat) {
            BackendService.storeModeHeartBeat(Status);
        }
    }

    public void moveWidget(short ID){
        if (ID == SharedConstants.SpeedStatsID){
            if (SpeedWidgetMode == SharedConstants.LeftBottomWidget) {
                SpeedWidgetMode = SharedConstants.CenterTopWidget;
                HeartbeatWidgetMode = SharedConstants.RightBottomWidget;
            }
            else SpeedWidgetMode = SharedConstants.LeftBottomWidget;
        }

        if (ID == SharedConstants.HeatbeatStatsID) {
            if (HeartbeatWidgetMode == SharedConstants.RightBottomWidget) {
                HeartbeatWidgetMode = SharedConstants.CenterTopWidget;
                SpeedWidgetMode = SharedConstants.LeftBottomWidget;
            }
            else HeartbeatWidgetMode = SharedConstants.RightBottomWidget;
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

        // Refresh HeartBeat button state ...
        HeartBeatSensor.setMode(BackendService.getModeHeartBeat());

        // Setting collection area
        Vector SizeSelection = BackendService.getExtractStatisticsSize();
        Vector ViewCenter = Snapshot.copy();
        searchZone.set(ViewCenter.x - SizeSelection.x / 2, ViewCenter.y - SizeSelection.y / 2,
                       ViewCenter.x + SizeSelection.x / 2, ViewCenter.y + SizeSelection.y / 2  );

        // Collecting data from backend
        ArrayList<SurveySnapshot> CollectedStatistics = BackendService.filter(BackendService.extract(searchZone),Snapshot);

        // Updating Speeds Statistics
        if (SpeedMonitor.getVisibility() == View.INVISIBLE) SpeedMonitor.setVisibility(View.VISIBLE);
        Speeds.clear();
        Speeds.add(Float.valueOf(Snapshot.getSpeed()*3.6f));
        for (SurveySnapshot item: CollectedStatistics) {
            Speeds.add(Float.valueOf(item.getSpeed()*3.6f));
        }
        if (Speeds.isEmpty()) Speeds.add(Float.valueOf(Snapshot.getSpeed()*3.6f));
        SpeedMonitor.updateStatistics(Speeds);

        // Updating HeartBeats Statistics
        if (Snapshot.getHeartbeat() == -1) { HeartbeatMonitor.setVisibility(View.INVISIBLE); return; }
        if (HeartbeatMonitor.getVisibility() == View.INVISIBLE) HeartbeatMonitor.setVisibility(View.VISIBLE);
        HeartBeats.clear();
        HeartBeats.add(Float.valueOf(Snapshot.getHeartbeat()));
        for (SurveySnapshot item: CollectedStatistics) {
            if (item.getHeartbeat() == -1) continue;
            HeartBeats.add(Float.valueOf(item.getHeartbeat()));
        }
        if (HeartBeats.isEmpty()) HeartBeats.add(Float.valueOf(Snapshot.getHeartbeat()));
        HeartbeatMonitor.updateStatistics(HeartBeats);
    }
}
