package core.launcher.partner;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

import core.GPS.CoreGPS;
import core.Settings.Parameters;
import core.Settings.SwitchModes;
import core.Structures.Coords2D;
import core.Structures.Extension;
import core.Structures.Frame;
import core.Structures.Statistic;

import core.Structures.Node;
import core.helpers.Processing;
import core.helpers.PermissionLoader;
import services.Hub;
import services.Junction;
import services.Signals;

public class Docking extends Activity implements ServiceConnection, Signals {

    private String LogTag = Docking.class.getSimpleName();

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

    private Frame searchZone;

    private ArrayList<Float> Speeds;
    private ArrayList<Float> HeartBeats;

    private int BackPressedCount = 0;
    private ViewStates SavedStates;

    private Junction BackendService = null;

    private PermissionLoader Permissions = new PermissionLoader();
    private boolean PermissionsChecked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docking);

        DockingManager = (RelativeLayout) findViewById(R.id.manage_docking);
        MapView = (MapManager)  findViewById(R.id.map_manager);

        SavedStates = (ViewStates) getApplication();

        SleepLocker = (ControlSwitch) findViewById(R.id.switch_sleep_locker);
        SleepLocker.registerModes(SwitchModes.SleepLocked, SwitchModes.SleepUnLocked);
        SleepLocker.registerManager(this);
        SleepLocker.setMode(SavedStates.getModeSleep());
        if (SavedStates.getModeSleep() == SwitchModes.SleepLocked)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        LightEnhancer = (ControlSwitch) findViewById(R.id.switch_light_enhancer);
        LightEnhancer.registerModes(SwitchModes.LightEnhanced, SwitchModes.LightNormal);
        LightEnhancer.registerManager(this);
        LightEnhancer.setMode(SavedStates.getModeLight());
        LightEnhancer.setVisibility(View.INVISIBLE);

        BatterySaver = (ControlSwitch) findViewById(R.id.switch_battery_saver);
        BatterySaver.registerModes(SwitchModes.BatteryDrainMode, SwitchModes.BatterySaveMode);
        BatterySaver.registerManager(this);
        BatterySaver.setMode(SavedStates.getModeBattery());
        BatterySaver.setVisibility(View.INVISIBLE);

        GPSProvider = (ControlSwitch) findViewById(R.id.gps_provider);
        GPSProvider.registerModes(SwitchModes.LiveGPS, SwitchModes.ReplayedGPS);
        GPSProvider.registerManager(this);
        GPSProvider.setMode(SavedStates.getModeGPS());

        HeartBeatSensor = (ControlSwitch) findViewById(R.id.heartbeat_provider);
        HeartBeatSensor.registerModes(SwitchModes.ConnectedHeartBeat, SwitchModes.DisconnectedHeartBeat);
        HeartBeatSensor.registerManager(this);
        HeartBeatSensor.setMode(SavedStates.getModeSensor());
        HeartBeatSensor.setVisibility(View.VISIBLE);

        LayoutInflater fromXML = LayoutInflater.from(this);

        // Hardcoded settings for Speed in left Monitor
        SpeedMonitor = (Monitor) fromXML.inflate(R.layout.widget_monitor, null);
        SpeedMonitor.registerManager(this);
        SpeedMonitor.setID(SwitchModes.SpeedStatsID);
        SpeedWidgetMode = SwitchModes.LeftBottomWidget;
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
        HeartbeatMonitor.setID(SwitchModes.HeatbeatStatsID);
        HeartbeatWidgetMode = SwitchModes.RightBottomWidget;
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

        // Checking permissions
        Permissions.Append(Manifest.permission.BLUETOOTH);
        Permissions.Append(Manifest.permission.BLUETOOTH_ADMIN);
        Permissions.Append(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Permissions.Append(Manifest.permission.ACCESS_FINE_LOCATION);
        Permissions.Append(Manifest.permission.ACCESS_COARSE_LOCATION);
        Permissions.Append(Manifest.permission.VIBRATE);
        
        String Requested = Permissions.Selected();
        while (Requested != null) {
            if (CheckPermission(Permissions.Selected())) Permissions.setGranted();
            Permissions.Next();
            Requested = Permissions.Selected();
        }
        String[] NotGrantedPermissions = Permissions.NotGranted();
        if (NotGrantedPermissions.length > 0) requestPermissions(NotGrantedPermissions,0);
        else PermissionsChecked = true;
    }

    private void applyWidgetsLayout(){
        Point ScreenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(ScreenSize);
        int Bounds = Math.min(ScreenSize.x,ScreenSize.y);
        int SecondaryWidgetWidth = (int)(Bounds * 0.45);
        int PrimaryWidgetWidth = (int)(Bounds * 0.65);
        RelativeLayout.LayoutParams MonitorConfig;

        // Managing SpeedMonitor Widget
        if (SpeedWidgetMode == SwitchModes.LeftBottomWidget) {
            MonitorConfig = new RelativeLayout.LayoutParams(SecondaryWidgetWidth, (int) (SecondaryWidgetWidth * Parameters.WidthToHeightFactor));
            MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        else {
            MonitorConfig = new RelativeLayout.LayoutParams(PrimaryWidgetWidth, (int) (PrimaryWidgetWidth * Parameters.WidthToHeightFactor));
            MonitorConfig.addRule(RelativeLayout.CENTER_HORIZONTAL);
            MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_TOP);
         }

        SpeedMonitor.setLayoutParams(MonitorConfig);
        DockingManager.removeView(SpeedMonitor);
        DockingManager.addView(SpeedMonitor,MonitorConfig);

        // Managing HeartbeatMonitor Widget
        if (HeartbeatWidgetMode == SwitchModes.RightBottomWidget) {
            MonitorConfig = new RelativeLayout.LayoutParams(SecondaryWidgetWidth, (int) (SecondaryWidgetWidth * Parameters.WidthToHeightFactor));
            MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        else {
            MonitorConfig = new RelativeLayout.LayoutParams(PrimaryWidgetWidth, (int) (PrimaryWidgetWidth * Parameters.WidthToHeightFactor));
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
        if (BackendService == null) return;
        unbindService(this);
        Log.d(LogTag, "Closing connection with Services ...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        BackPressedCount = 0;
        SavedStates = (ViewStates) getApplication();

        // Refresh all button internal state
        loadStatus();
        StartServices();
    }

    public void onButtonStatusChanged(short Status) {
        if (Status == SwitchModes.SleepLocked) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            SavedStates.storeModeSleep(Status);
            SleepLocker.setMode(Status);
        }

        if (Status == SwitchModes.SleepUnLocked) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            SavedStates.storeModeSleep(Status);
            SleepLocker.setMode(Status);
        }

        if (Status == SwitchModes.LiveGPS) {
            SavedStates.storeModeGPS(Status);
            GPSProvider.setMode(Status);
        }
        if (Status == SwitchModes.ReplayedGPS) {
            SavedStates.storeModeGPS(Status);
            GPSProvider.setMode(Status);
        }

        if (Status == SwitchModes.LightEnhanced) {
            SavedStates.storeModeLight(Status);
            LightEnhancer.setMode(Status);
        }
        if (Status == SwitchModes.LightNormal) {
            SavedStates.storeModeLight(Status);
            LightEnhancer.setMode(Status);
        }

        if (Status == SwitchModes.BatteryDrainMode) {
            SavedStates.storeModeBattery(Status);
            BatterySaver.setMode(Status);
        }
        if (Status == SwitchModes.BatterySaveMode) {
            SavedStates.storeModeBattery(Status);
            BatterySaver.setMode(Status);
        }
        if (Status == SwitchModes.ConnectedHeartBeat) {
            SavedStates.storeModeSensor(SwitchModes.SearchHeartBeat);
            if (BackendService == null) return;
            BackendService.startSensor();
        }
        if (Status == SwitchModes.DisconnectedHeartBeat) {
            SavedStates.storeModeSensor(Status);
            if (BackendService == null) return;
            BackendService.stopSensor();
        }
    }

    public void moveWidget(short ID){
        if (ID == SwitchModes.SpeedStatsID){
            if (SpeedWidgetMode == SwitchModes.LeftBottomWidget) {
                SpeedWidgetMode = SwitchModes.CenterTopWidget;
                HeartbeatWidgetMode = SwitchModes.RightBottomWidget;
            }
            else SpeedWidgetMode = SwitchModes.LeftBottomWidget;
        }

        if (ID == SwitchModes.HeatbeatStatsID) {
            if (HeartbeatWidgetMode == SwitchModes.RightBottomWidget) {
                HeartbeatWidgetMode = SwitchModes.CenterTopWidget;
                SpeedWidgetMode = SwitchModes.LeftBottomWidget;
            }
            else HeartbeatWidgetMode = SwitchModes.RightBottomWidget;
        }

        applyWidgetsLayout();
    }

    @Override
    public void onBackPressed() {
        BackPressedCount++;
        if (BackPressedCount > 1) {
            SavedStates.shutdown();
            this.finish();
            System.exit(0);
            super.onBackPressed();
        }
        else { Toast.makeText(this, getResources().getString(R.string.message_back_key_pressed), Toast.LENGTH_SHORT).show(); }
    }

    private void loadStatus() {
        if (SavedStates == null) return;

        // updating Buttons status
        HeartBeatSensor.setMode(SavedStates.getModeSensor());
        GPSProvider.setMode(SavedStates.getModeGPS());
        LightEnhancer.setMode(SavedStates.getModeLight());
        BatterySaver.setMode(SavedStates.getModeBattery());
        SleepLocker.setMode(SavedStates.getModeSleep());
    }

    private void StartServices(){
        if (!PermissionsChecked) return;

        Intent ServiceStarter;
        // Start Service
        ServiceStarter = new Intent(this, Hub.class);
        Log.d(LogTag, "Requesting Service ["+ Hub.class.getSimpleName() +"] to start...");
        startService(ServiceStarter);
        bindService(ServiceStarter, this, 0);
    }

    /************************************************************************
     * Managing requested permissions at runtime
     * **********************************************************************/
    private boolean CheckPermission(String RequestedPermission) {
        return this.checkSelfPermission(RequestedPermission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(LogTag, "Collecting Permissions results...");

        boolean PermissionsGranted = true;
        for(int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                PermissionsGranted = false;
                Log.d(LogTag, "Permission:"+permissions[i]+" is not granted !");
            }
        }

        if (!PermissionsGranted) finish();
        PermissionsChecked = true;
        StartServices();
    }


    /************************************************************************
     * Managing connection to Service
     * **********************************************************************/
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(LogTag, "Connected to " + name.getClassName() + " Service");

        // Connection from push Service
        if (Junction.class.getName().equals(name.getClassName())) {
            BackendService = (Junction) service;
            BackendService.RegisterListener(this);
            MapView.setBackend(BackendService);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(LogTag, "Disconnected from " + name.getClassName()  + " Service");

        // Disconnection from push Service
        if (Junction.class.getName().equals(name.getClassName())) {
            BackendService = null;
            MapView.setBackend(null);
        }
    }

    /************************************************************************
     * Implementing Signals interface (callbacks from Service)
     * **********************************************************************/
    @Override
    public void TrackLoaded(boolean Success) {

    }

    @Override
    public void TrackEvent(int Distance) {

    }

    @Override
    public void UpdateBPM(int Value) {
        if (Value>0) HeartBeatSensor.setMode(SwitchModes.ConnectedHeartBeat);
        else HeartBeatSensor.setMode(SwitchModes.DisconnectedHeartBeat);
    }

    @Override
    public void UpdateGPS(CoreGPS InfoGPS) {
        if (SavedStates == null) return;

        // Refresh HeartBeat button state ...
        HeartBeatSensor.setMode(SavedStates.getModeSensor());

        // Setting collection area
        Extension SizeSelection = SavedStates.getExtractStatisticsSize();
        Coords2D ViewCenter = InfoGPS.Moved();
        searchZone = new Frame(ViewCenter, SizeSelection);

        Statistic Snapshot = InfoGPS.Statistic(0);

        // Collecting data from backend
        ArrayList<Node> CollectedStatistics = Processing.filter(BackendService.getNodes(searchZone), Snapshot);

        // Updating Speeds Statistics
        if (SpeedMonitor.getVisibility() == View.INVISIBLE) SpeedMonitor.setVisibility(View.VISIBLE);
        Speeds.clear();
        Speeds.add(Float.valueOf(Snapshot.Speed*3.6f));
        for (Node item: CollectedStatistics) {
            Speeds.add(Float.valueOf(item.Stats.Speed*3.6f));
        }
        if (Speeds.isEmpty()) Speeds.add(Float.valueOf(Snapshot.Speed*3.6f));
        SpeedMonitor.updateStatistics(Speeds);

        // Updating HeartBeats Statistics
        if (Snapshot.Heartbeat == -1) { HeartbeatMonitor.setVisibility(View.INVISIBLE); return; }
        if (HeartbeatMonitor.getVisibility() == View.INVISIBLE) HeartbeatMonitor.setVisibility(View.VISIBLE);
        HeartBeats.clear();
        HeartBeats.add(Float.valueOf(Snapshot.Heartbeat));
        for (Node item: CollectedStatistics) {
            if (item.Stats.Heartbeat == -1) continue;
            HeartBeats.add(Float.valueOf(item.Stats.Heartbeat));
        }
        if (HeartBeats.isEmpty()) HeartBeats.add(Float.valueOf(Snapshot.Heartbeat));
        HeartbeatMonitor.updateStatistics(HeartBeats);

        // Updating Background View
        MapView.setGPS(InfoGPS);
    }

    @Override
    public void OutOfRange() { BackendService.reload(); }
}
