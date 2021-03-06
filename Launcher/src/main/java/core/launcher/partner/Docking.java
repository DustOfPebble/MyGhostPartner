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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import core.GPS.CoreGPS;
import core.Settings.Parameters;
import core.launcher.Buttons.SwitchEnums;
import core.Structures.Coords2D;
import core.Structures.Frame;

import core.Structures.Node;
import core.helpers.PermissionLoader;
import core.launcher.Buttons.Switch;
import core.launcher.Buttons.SwitchMonitor;
import core.launcher.Map.Map2D;
import core.launcher.Widgets.ComputedView;
import core.launcher.Widgets.Fields;
import core.launcher.Widgets.HeartExtract;
import core.launcher.Widgets.HistoryView;
import core.launcher.Widgets.Processing;
import core.launcher.Widgets.SpeedExtract;
import core.launcher.Widgets.StatisticView;
import core.launcher.Widgets.DropExtract;
import core.launcher.Widgets.WidgetEnums;
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
/*
    private StatisticView SpeedMonitor = null;
    private StatisticView CardioMonitor = null;
    private HistoryView ElevationHistory = null;
*/
    private Map2D MapView = null;
    private DockingSaved SavedStates;
    private Junction BackendService = null;

    private PermissionLoader Permissions = new PermissionLoader();
    private boolean PermissionsChecked = false;

    private class WidgetConfig {
        Fields Extractor;
        int WidgetID;
        WidgetConfig(int ID, Fields Selected) {
            WidgetID = ID;
            Extractor =  Selected;
        }
    }
    private ArrayList<SwitchMonitor> WidgetSwitches = new ArrayList<>();
    private HashMap<Integer, WidgetConfig> Mapping = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.docker);

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

/*        // Creating widgets instance
        LayoutInflater fromXML = LayoutInflater.from(this);

        SpeedMonitor = (StatisticView) fromXML.inflate(R.layout.statistic_speed, null);
        SpeedMonitor.register(DockingManager);
        SpeedMonitor.registerProcessor(new SpeedExtract());
        //DockingManager.add(SpeedMonitor);

        CardioMonitor = (StatisticView) fromXML.inflate(R.layout.statistic_heart, null);
        CardioMonitor.register(DockingManager);
        CardioMonitor.registerProcessor(new HeartExtract());
        //DockingManager.add(CardioMonitor);

        ElevationHistory = (HistoryView) fromXML.inflate(R.layout.history_drop, null);
        ElevationHistory.register(DockingManager);
        ElevationHistory.registerProcessor(new DropExtract());
        //DockingManager.add(ElevationHistory);
*/
        // Widget Button Show/Hide
        Mapping.put(R.id.drop_history, new WidgetConfig(R.layout.history_drop, new DropExtract()));
        Mapping.put(R.id.speed_statistic, new WidgetConfig(R.layout.statistic_speed, new SpeedExtract()));
        Mapping.put(R.id.heart_statistic, new WidgetConfig(R.layout.statistic_heart, new HeartExtract()));

        Iterator Selector = Mapping.entrySet().iterator();
        while (Selector.hasNext()) {
            Map.Entry Links = (Map.Entry) Selector.next();
            SwitchMonitor Button = (SwitchMonitor) findViewById((int)Links.getKey());
            Button.SwitchID = (int)Links.getKey();
            Button.registerListener(this);
            WidgetSwitches.add(Button);
        }

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

    public void showWidget(SwitchMonitor sender) {
        WidgetConfig Config = Mapping.get(sender.SwitchID);
        if (sender.LinkedView == null) {
            LayoutInflater fromXML = LayoutInflater.from(this);
            sender.LinkedView = (ComputedView) fromXML.inflate(Config.WidgetID, null);
            sender.LinkedView.register(DockingManager);
            sender.LinkedView.registerProcessor(Config.Extractor);
            DockingManager.add(sender.LinkedView);
            sender.LinkedView.setVisibility(View.VISIBLE);
        } else {
            DockingManager.remove(sender.LinkedView);
            sender.LinkedView =  null;
        }
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
        else Status = (Status == SwitchEnums.Enabled)? SwitchEnums.Disabled: SwitchEnums.Enabled;

        if (BackendService == null) {
            SavedStates.storeModeLogger(SwitchEnums.Disabled);
            NodesLogger.setMode(SwitchEnums.Disabled);
            return;
        }
        if (Status == SwitchEnums.Disabled) {
//            ElevationHistory.setVisibility(View.INVISIBLE);
            SavedStates.storeModeLogger(SwitchEnums.Disabled);
            NodesLogger.setMode(SwitchEnums.Disabled);
            BackendService.setLogger(Modes.Finish);
        }
        if (Status == SwitchEnums.Enabled) {
//            ElevationHistory.setVisibility(View.VISIBLE);
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
//                SpeedMonitor.setVisibility(View.VISIBLE);
                SavedStates.storeModeGPS(SwitchEnums.Waiting);
                BackendService.setGPS(true);
                return;
            }
            if (SavedStates.getModeGPS() == SwitchEnums.Waiting) {
//                SpeedMonitor.setVisibility(View.INVISIBLE);
                SavedStates.storeModeGPS(SwitchEnums.Disabled);
                BackendService.setGPS(false);
                return;
            }
        }
        if (Status == SwitchEnums.Enabled) {
 //           SpeedMonitor.setVisibility(View.INVISIBLE);
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
//                CardioMonitor.setVisibility(View.VISIBLE);
                BackendService.setSensor(true);
                return;
            }
            if (SavedStates.getModeSensor() == SwitchEnums.Waiting) {
//                CardioMonitor.setVisibility(View.INVISIBLE);
                CardioSensor.setMode(SwitchEnums.Disabled);
                SavedStates.storeModeSensor(SwitchEnums.Disabled);
                BackendService.setSensor(false);
                return;
            }
        }
        if (Status == SwitchEnums.Enabled) {
//            CardioMonitor.setVisibility(View.INVISIBLE);
            CardioSensor.setMode(SwitchEnums.Disabled);
            SavedStates.storeModeSensor(SwitchEnums.Disabled);
            BackendService.setSensor(false);
        }
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

        // UpdatedBPM setGPS button state
        if (SavedStates.getModeGPS() == SwitchEnums.Waiting) {
            ServiceGPS.setMode(SwitchEnums.Enabled);
            SavedStates.storeModeGPS(SwitchEnums.Enabled);
        }

        Coords2D ViewCenter = InfoGPS.Moved(); // This generate a copy !
        Frame searchZone = new Frame(ViewCenter, Parameters.StatisticsSize);
        Node Live = new Node(ViewCenter, InfoGPS.Statistic());
        for(ComputedView Container: DockingManager.Containers) {
            if (Container.WidgetMode == WidgetEnums.StatsView) {
               Container.pushNodes(Processing.filter(BackendService.getNodesByZone(searchZone), Live), Live);
            }

            if (Container.WidgetMode == WidgetEnums.LogsView) {
                Container.pushNodes(BackendService.getNodesByDelay(10), Live);
            }
        }

        // Updating Background View
        MapView.setGPS(InfoGPS);
    }
}
