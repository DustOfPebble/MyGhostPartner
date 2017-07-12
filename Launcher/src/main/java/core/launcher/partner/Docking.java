package core.launcher.partner;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import core.GPS.CoreGPS;
import core.Settings.Parameters;
import core.launcher.Buttons.SwitchEnums;
import core.Structures.Coords2D;
import core.Structures.Extension;
import core.Structures.Frame;
import core.Structures.Statistic;

import core.Structures.Node;
import core.helpers.PermissionLoader;
import core.launcher.Buttons.Switch;
import core.launcher.Map.Map2D;
import core.launcher.Widgets.GridedView;
import core.launcher.Widgets.SetSpeed;
import core.launcher.Widgets.SetCardio;
import core.launcher.Widgets.StatsScaled;
import services.Hub;
import services.Junction;
import services.Recorder.Modes;
import services.Signals;

public class Docking extends Activity implements ServiceConnection, Signals {

    private String LogTag = Docking.class.getSimpleName();
    private long LastBackTimeStamps = -1;

    private Organizer DockingManager = null;

    private Switch SleepLocker = null;
    private Switch NodesLogger = null;
    private Switch ServiceGPS = null;
    private Switch CardioSensor = null;

    private StatsScaled SpeedMonitor = null;
    private StatsScaled CardioMonitor = null;
    private GridedView ElevationHistory = null;


    private Map2D MapView = null;
    private DockingSaved SavedStates;
    private Junction BackendService = null;

    private ArrayList<Float> Speeds;
    private ArrayList<Float> HeartBeats;

    private PermissionLoader Permissions = new PermissionLoader();
    private boolean PermissionsChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docking);

        DockingManager = (Organizer) findViewById(R.id.manage_docking);
        DockingManager.register(this);

        MapView = (Map2D)  findViewById(R.id.map_manager);

        SavedStates = (DockingSaved) getApplication();

        SleepLocker = (Switch) findViewById(R.id.switch_sleep_locker);
        SleepLocker.registerListener(this);
        ManageSleepLocker(SwitchEnums.Forced);

        NodesLogger = (Switch) findViewById(R.id.switch_trace_recorder);
        NodesLogger.registerListener(this);
        ManageNodesLogger(SwitchEnums.Forced);

        ServiceGPS = (Switch) findViewById(R.id.gps_provider);
        ServiceGPS.registerListener(this);
        ServiceGPS.setMode(SavedStates.getModeGPS());

        CardioSensor = (Switch) findViewById(R.id.sensor_provider);
        CardioSensor.registerListener(this);
        CardioSensor.setMode(SavedStates.getModeSensor());

        // Creating widgets instance
        LayoutInflater fromXML = LayoutInflater.from(this);

        SpeedMonitor = (StatsScaled) fromXML.inflate(R.layout.statistic_scaled, null);
        SpeedMonitor.setParams(new SetSpeed(this));
        SpeedMonitor.register(DockingManager);
        DockingManager.add(SpeedMonitor);

        CardioMonitor = (StatsScaled) fromXML.inflate(R.layout.statistic_scaled, null);
        CardioMonitor.setParams(new SetCardio(this));
        CardioMonitor.register(DockingManager);
        DockingManager.add(CardioMonitor);

        ElevationHistory = (GridedView) fromXML.inflate(R.layout.statistic_grided, null);
        ElevationHistory.register(DockingManager);
        DockingManager.add(ElevationHistory);

        Speeds = new ArrayList<>();
        HeartBeats = new ArrayList<>();

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
        SavedStates = (DockingSaved) getApplication();
        LastBackTimeStamps = Calendar.getInstance().getTimeInMillis();

        // Refresh all button internal state
        loadStatus();
        StartServices();
    }

    public void onClicked(Switch Sender) {
        if (Sender == SleepLocker) ManageSleepLocker(Sender.Status);
        if (Sender == NodesLogger) ManageNodesLogger(Sender.Status);
        if (Sender == ServiceGPS) ManageGPS(Sender.Status);
        if (Sender == CardioSensor) ManageCardioSensor(Sender.Status);
    }
    private void ManageSleepLocker(short Status) {
        if (Status == SwitchEnums.Forced) Status = SavedStates.getModeSleep();
        else Status = (Status == SwitchEnums.Enabled)? SwitchEnums.Disabled: SwitchEnums.Enabled;

        if (Status == SwitchEnums.Disabled) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            SavedStates.storeModeSleep(SwitchEnums.Disabled);
            SleepLocker.setMode(SwitchEnums.Disabled);
            return;
        }
        if (Status == SwitchEnums.Enabled) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            SavedStates.storeModeSleep(SwitchEnums.Enabled);
            SleepLocker.setMode(SwitchEnums.Enabled);
        }
    }
    private void ManageNodesLogger(short Status) {
        if (Status == SwitchEnums.Forced) Status = SavedStates.getModeLogger();
        else Status = (Status== SwitchEnums.Enabled)? SwitchEnums.Disabled: SwitchEnums.Enabled;

        if (BackendService == null) {
            SavedStates.storeModeLogger(SwitchEnums.Disabled);
            NodesLogger.setMode(SwitchEnums.Disabled);
            return;
        }
        if (Status == SwitchEnums.Disabled) {
            ElevationHistory.setVisibility(View.INVISIBLE);
            SavedStates.storeModeLogger(SwitchEnums.Disabled);
            NodesLogger.setMode(SwitchEnums.Disabled);
            BackendService.setLogger(Modes.Finish);
        }
        if (Status == SwitchEnums.Enabled) {
            ElevationHistory.setVisibility(View.VISIBLE);
            SavedStates.storeModeLogger(SwitchEnums.Enabled);
            NodesLogger.setMode(SwitchEnums.Enabled);
            BackendService.setLogger(Modes.Create);
        }
    }

    private void ManageGPS(short Status) {
        if (BackendService == null) {
            SavedStates.storeModeGPS(SwitchEnums.Disabled);
            ServiceGPS.setMode(SwitchEnums.Disabled);
            return;
        }
        if (Status == SwitchEnums.Disabled) {
            if (SavedStates.getModeGPS() == SwitchEnums.Disabled) {
                SpeedMonitor.setVisibility(View.VISIBLE);
                SavedStates.storeModeGPS(SwitchEnums.Waiting);
                BackendService.setGPS(true);
                return;
            }
            if (SavedStates.getModeGPS() == SwitchEnums.Waiting) {
                SpeedMonitor.setVisibility(View.INVISIBLE);
                SavedStates.storeModeGPS(SwitchEnums.Disabled);
                BackendService.setGPS(false);
                return;
            }
        }
        if (Status == SwitchEnums.Enabled) {
            SpeedMonitor.setVisibility(View.INVISIBLE);
            ServiceGPS.setMode(SwitchEnums.Disabled);
            SavedStates.storeModeGPS(SwitchEnums.Disabled);
            BackendService.setGPS(false);
        }
    }

    private void ManageCardioSensor(short Status) {
        if (BackendService == null) {
            SavedStates.storeModeSensor(SwitchEnums.Disabled);
            CardioSensor.setMode(SwitchEnums.Disabled);
            return;
        }
        if (Status == SwitchEnums.Disabled) {
            if (SavedStates.getModeSensor() == SwitchEnums.Disabled) {
                SavedStates.storeModeSensor(SwitchEnums.Waiting);
                CardioMonitor.setVisibility(View.VISIBLE);
                BackendService.setSensor(true);
                return;
            }
            if (SavedStates.getModeSensor() == SwitchEnums.Waiting) {
                CardioMonitor.setVisibility(View.INVISIBLE);
                CardioSensor.setMode(SwitchEnums.Disabled);
                SavedStates.storeModeSensor(SwitchEnums.Disabled);
                BackendService.setSensor(false);
                return;
            }
        }
        if (Status == SwitchEnums.Enabled) {
            CardioMonitor.setVisibility(View.INVISIBLE);
            CardioSensor.setMode(SwitchEnums.Disabled);
            SavedStates.storeModeSensor(SwitchEnums.Disabled);
            BackendService.setSensor(false);
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
            if (item.Stats.Heartbeat <= -1) continue;
            HeartBeats.add((float)item.Stats.Heartbeat);
        }
        CardioMonitor.setValues(Snapshot.Heartbeat,HeartBeats);
    }

    @Override
    public void onBackPressed() {
        long BackTimeStamps = Calendar.getInstance().getTimeInMillis();
        if ((BackTimeStamps - LastBackTimeStamps) < 1000) {
            SavedStates.shutdown();
            this.finish();
            System.exit(0);
            super.onBackPressed();
        }
        else {
            Toast.makeText(this,
                    getResources().getString(R.string.message_back_key_pressed),
                    Toast.LENGTH_SHORT).show();
            LastBackTimeStamps = BackTimeStamps;
        }
    }

    private void loadStatus() {
        if (SavedStates == null) return;

        // updating Buttons status
        CardioSensor.setMode(SavedStates.getModeSensor());
        ServiceGPS.setMode(SavedStates.getModeGPS());
        NodesLogger.setMode(SavedStates.getModeLogger());
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
    public void UpdateTracking(boolean Success) {  }

    @Override
    public void TrackEvent(int Distance) {  }

    @Override
    public void UpdatedSensor(int Value) {
        // UpdatedBPM CardioSensor button state ...
        if (Value >= 0) {
            if (CardioSensor.Status != SwitchEnums.Enabled) {
                CardioSensor.setMode(SwitchEnums.Enabled);
                SavedStates.storeModeSensor(SwitchEnums.Enabled);
            }
        }
        else  {
            CardioSensor.setMode(SwitchEnums.Disabled);
            SavedStates.storeModeSensor(SwitchEnums.Disabled);
        }
    }

    @Override
    public void UpdatedGPS(CoreGPS InfoGPS) {
        if (SavedStates == null) return;

        // Get Fields readable structure
        Statistic Snapshot = InfoGPS.Statistic();

        // UpdatedBPM setGPS button state
        if (SavedStates.getModeGPS() == SwitchEnums.Waiting) {
            ServiceGPS.setMode(SwitchEnums.Enabled);
            SavedStates.storeModeGPS(SwitchEnums.Enabled);
        }

        // Setting collection area
        Extension SizeSelection = Parameters.StatisticsSize;
        Coords2D ViewCenter = InfoGPS.Moved();
        Frame searchZone = new Frame(ViewCenter, SizeSelection);

        // Collecting data from backend
        ArrayList<Node> CollectedStatistics = Processing.filter(BackendService.getNodes(searchZone), new Node(ViewCenter,Snapshot));

        ManageSpeedStats(CollectedStatistics, Snapshot);
        ManageCardioStats(CollectedStatistics, Snapshot);
        ElevationHistory.update(Snapshot);

        // Updating Background View
        MapView.setGPS(InfoGPS);
    }
}
