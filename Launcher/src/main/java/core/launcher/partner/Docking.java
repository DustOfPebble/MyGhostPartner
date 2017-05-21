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
import core.Settings.Switches;
import core.Structures.Coords2D;
import core.Structures.Extension;
import core.Structures.Frame;
import core.Structures.Statistic;

import core.Structures.Node;
import core.helpers.Processing;
import core.helpers.PermissionLoader;
import services.Hub;
import services.Junction;
import services.Recorder.Modes;
import services.Signals;

public class Docking extends Activity implements ServiceConnection, Signals {

    private String LogTag = Docking.class.getSimpleName();

    private RelativeLayout DockingManager = null;

    private ControlSwitch SleepLocker = null;
    private ControlSwitch TraceRecorder = null;
    private ControlSwitch ServiceGPS = null;
    private ControlSwitch CardioSensor = null;

    private Monitor SpeedMonitor = null;
    private short SpeedWidgetMode = -1;
    private Monitor HeartbeatMonitor = null;
    private short CardioWidgetMode = -1;

    private Map2D MapView = null;

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
        MapView = (Map2D)  findViewById(R.id.map_manager);

        SavedStates = (ViewStates) getApplication();

        SleepLocker = (ControlSwitch) findViewById(R.id.switch_sleep_locker);
        SleepLocker.registerModes(Switches.SleepLocked, Switches.SleepUnLocked);
        SleepLocker.registerManager(this);
        SleepLocker.setMode(SavedStates.getModeSleep());

        TraceRecorder = (ControlSwitch) findViewById(R.id.switch_trace_recorder);
        TraceRecorder.registerModes(Switches.TraceEnabled, Switches.TraceDisabled);
        TraceRecorder.registerManager(this);
        TraceRecorder.setMode(SavedStates.getModeLogger());

        ServiceGPS = (ControlSwitch) findViewById(R.id.gps_provider);
        ServiceGPS.registerModes(Switches.LiveGPS, Switches.NoGPS);
        ServiceGPS.registerManager(this);
        ServiceGPS.setMode(SavedStates.getModeGPS());

        CardioSensor = (ControlSwitch) findViewById(R.id.sensor_provider);
        CardioSensor.registerModes(Switches.SensorConnected, Switches.NoSensor);
        CardioSensor.registerManager(this);
        CardioSensor.setMode(SavedStates.getModeSensor());

        LayoutInflater fromXML = LayoutInflater.from(this);

        // Hardcoded settings for Speed in left Monitor
        SpeedMonitor = (Monitor) fromXML.inflate(R.layout.widget_monitor, null);
        SpeedMonitor.registerManager(this);
        SpeedMonitor.setID(Switches.SpeedStatsID);
        SpeedWidgetMode = Switches.LeftBottomWidget;
        SpeedMonitor.setIcon( BitmapFactory.decodeResource(getResources(), R.drawable.speed_thumb));
        SpeedMonitor.setNbTicksDisplayed(18);
        SpeedMonitor.setNbTicksLabel(5);
        SpeedMonitor.setTicksStep(1f);
        SpeedMonitor.setPhysicRange(0f,80f);
        SpeedMonitor.setUnit("km/h");

        // Hardcoded settings for Heartbeat in right Monitor
        HeartbeatMonitor = (Monitor) fromXML.inflate(R.layout.widget_monitor, null);
        HeartbeatMonitor.registerManager(this);
        HeartbeatMonitor.setID(Switches.SensorStatsID);
        CardioWidgetMode = Switches.RightBottomWidget;
        HeartbeatMonitor.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.heart_thumb));
        HeartbeatMonitor.setNbTicksDisplayed(22);
        HeartbeatMonitor.setNbTicksLabel(10);
        HeartbeatMonitor.setTicksStep(1f);
        HeartbeatMonitor.setPhysicRange(20f,220f);
        HeartbeatMonitor.setUnit("bpm");

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
        if (SpeedWidgetMode == Switches.LeftBottomWidget) {
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
        if (CardioWidgetMode == Switches.RightBottomWidget) {
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

    public void moveWidget(short ID){
        if (ID == Switches.SpeedStatsID){
            if (SpeedWidgetMode == Switches.LeftBottomWidget) {
                SpeedWidgetMode = Switches.CenterTopWidget;
                CardioWidgetMode = Switches.RightBottomWidget;
            }
            else SpeedWidgetMode = Switches.LeftBottomWidget;
        }

        if (ID == Switches.SensorStatsID) {
            if (CardioWidgetMode == Switches.RightBottomWidget) {
                CardioWidgetMode = Switches.CenterTopWidget;
                SpeedWidgetMode = Switches.LeftBottomWidget;
            }
            else CardioWidgetMode = Switches.RightBottomWidget;
        }

        applyWidgetsLayout();
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

    public void onClicked(short Status) {
        if ((Status == Switches.SleepLocked) || (Status == Switches.SleepUnLocked)) ManageLockScreen(Status);
        if ((Status == Switches.TraceEnabled) || (Status == Switches.TraceDisabled)) ManageTraceRecorder(Status);
        if ((Status == Switches.LiveGPS) || (Status == Switches.NoGPS))  ManageGPS(Status);
        if ((Status == Switches.SensorConnected) || (Status == Switches.NoSensor)) ManageCardioSensor(Status);
    }
    private void ManageLockScreen(short Status) {
        if (Status == Switches.SleepLocked) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            SavedStates.storeModeSleep(Switches.SleepUnLocked);
            SleepLocker.setMode(Switches.SleepUnLocked);
        }
        if (Status == Switches.SleepUnLocked) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            SavedStates.storeModeSleep(Switches.SleepLocked);
            SleepLocker.setMode(Switches.SleepLocked);
        }
    }
    private void ManageTraceRecorder(short Status) {
        if (BackendService == null) {
            SavedStates.storeModeLight(Switches.TraceDisabled);
            TraceRecorder.setMode(Switches.TraceDisabled);
            return;
        }
        if (Status == Switches.TraceEnabled) {
            SavedStates.storeModeLight(Switches.TraceDisabled);
            TraceRecorder.setMode(Switches.TraceDisabled);
            BackendService.setLog(Modes.Finish);
        }
        if (Status == Switches.TraceDisabled) {
            SavedStates.storeModeLight(Switches.TraceEnabled);
            TraceRecorder.setMode(Switches.TraceEnabled);
            BackendService.setLog(Modes.Create);
        }
    }

    private void ManageGPS(short Status) {
        if (BackendService == null) {
            SavedStates.storeModeGPS(Switches.NoGPS);
            ServiceGPS.setMode(Switches.NoGPS);
            return;
        }
        if (Status == Switches.NoGPS) {
            if (SavedStates.getModeGPS() == Switches.NoGPS) {
                SpeedMonitor.Initialize();
                SpeedMonitor.setVisibility(View.VISIBLE);
                SavedStates.storeModeGPS(Switches.WaitingGPS);
                BackendService.GPS(true);
                return;
            }
            if (SavedStates.getModeGPS() == Switches.WaitingGPS) {
                SpeedMonitor.setVisibility(View.INVISIBLE);
                SavedStates.storeModeGPS(Switches.NoGPS);
                BackendService.GPS(false);
                return;
            }
        }
        if (Status == Switches.LiveGPS) {
            SpeedMonitor.setVisibility(View.INVISIBLE);
            ServiceGPS.setMode(Switches.NoGPS);
            SavedStates.storeModeGPS(Switches.NoGPS);
            BackendService.GPS(false);
        }
    }

    private void ManageCardioSensor(short Status) {
        if (BackendService == null) {
            SavedStates.storeModeSensor(Switches.NoSensor);
            CardioSensor.setMode(Switches.NoSensor);
            return;
        }
        if (Status == Switches.NoSensor) {
            if (SavedStates.getModeSensor() == Switches.NoSensor) {
                SavedStates.storeModeSensor(Switches.WaitingSensor);
                HeartbeatMonitor.Initialize();
                HeartbeatMonitor.setVisibility(View.VISIBLE);
                BackendService.startSensor();
                return;
            }
            if (SavedStates.getModeSensor() == Switches.WaitingSensor) {
                HeartbeatMonitor.setVisibility(View.INVISIBLE);
                CardioSensor.setMode(Switches.NoSensor);
                SavedStates.storeModeSensor(Switches.NoSensor);
                BackendService.stopSensor();
                return;
            }
        }
        if (Status == Switches.WaitingSensor) {
            HeartbeatMonitor.setVisibility(View.INVISIBLE);
            CardioSensor.setMode(Switches.NoSensor);
            SavedStates.storeModeSensor(Switches.NoSensor);
            BackendService.stopSensor();
        }
    }


    private void ManageSpeedStats(ArrayList<Node> CollectedStatistics,Statistic Snapshot) {
        Speeds.clear();
        for (Node item: CollectedStatistics) {
            Speeds.add(item.Stats.Speed*3.6f);
        }
        SpeedMonitor.setValues(Snapshot.Speed*3.6f, Speeds);
    }

    private void ManageCardioStats(ArrayList<Node> CollectedStatistics,Statistic Snapshot) {
        HeartBeats.clear();
        for (Node item: CollectedStatistics) {
            if (item.Stats.Heartbeat == -1) continue;
            HeartBeats.add((float)item.Stats.Heartbeat);
        }
        HeartbeatMonitor.setValues(Snapshot.Heartbeat,HeartBeats);
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
        CardioSensor.setMode(SavedStates.getModeSensor());
        ServiceGPS.setMode(SavedStates.getModeGPS());
        TraceRecorder.setMode(SavedStates.getModeLogger());
        SleepLocker.setMode(SavedStates.getModeSleep());
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
    private void StartServices(){
        if (!PermissionsChecked) return;

        Intent ServiceStarter;
        // Start Service
        ServiceStarter = new Intent(this, Hub.class);
        Log.d(LogTag, "Requesting Service ["+ Hub.class.getSimpleName() +"] to start...");
        startService(ServiceStarter);
        bindService(ServiceStarter, this, 0);
    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(LogTag, "Connected to " + name.getClassName() + " Service");

        // Connection from push Service
        if (Hub.class.getName().equals(name.getClassName())) {
            BackendService = (Junction) service;
            BackendService.RegisterListener(this);
            MapView.setBackend(BackendService);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(LogTag, "Disconnected from " + name.getClassName()  + " Service");

        // Disconnection from push Service
        if (Hub.class.getName().equals(name.getClassName())) {
            BackendService = null;
            MapView.setBackend(null);
        }
    }

    /************************************************************************
     * Implementing Signals interface (callbacks from Service)
     * **********************************************************************/
    @Override
    public void UpdateTracking(boolean Success) {

    }

    @Override
    public void TrackEvent(int Distance) {

    }

    @Override
    public void UpdatedSensor(int Value) {
        // UpdatedBPM CardioSensor button state ...
        if (Value >= 0) {
            if (SavedStates.getModeSensor() == Switches.WaitingSensor) {
                ServiceGPS.setMode(Switches.SensorConnected);
                SavedStates.storeModeGPS(Switches.SensorConnected);
            }
        }
        else  {
            CardioSensor.setMode(Switches.NoSensor);
            SavedStates.storeModeSensor(Switches.NoSensor);
        }
    }

    @Override
    public void UpdatedGPS(CoreGPS InfoGPS) {
        if (SavedStates == null) return;

        // Get Fields readable structure
        Statistic Snapshot = InfoGPS.Statistic(0);

        // UpdatedBPM GPS button state
        if (SavedStates.getModeGPS() == Switches.WaitingGPS) {
            ServiceGPS.setMode(Switches.LiveGPS);
            SavedStates.storeModeGPS(Switches.LiveGPS);
        }

        // Setting collection area
        Extension SizeSelection = Parameters.StatisticsSize;
        Coords2D ViewCenter = InfoGPS.Moved();
        searchZone = new Frame(ViewCenter, SizeSelection);

        // Collecting data from backend
        ArrayList<Node> CollectedStatistics = Processing.filter(BackendService.getNodes(searchZone), Snapshot);

        ManageSpeedStats(CollectedStatistics, Snapshot);
        ManageCardioStats(CollectedStatistics, Snapshot);

        // Updating Background View
        MapView.setGPS(InfoGPS);
    }

    @Override
    public void OutOfRange() { BackendService.reload(); }
}
