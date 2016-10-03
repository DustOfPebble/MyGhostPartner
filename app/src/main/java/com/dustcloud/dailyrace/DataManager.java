package com.dustcloud.dailyrace;

import android.app.Application;
import android.content.Context;
import android.graphics.RectF;
import android.location.LocationManager;
import android.os.Handler;

import java.util.ArrayList;
//ToDo: Reload DB periodically with a smaller SearchableZone to reduce memory footprint
//ToDo: Limits numbers of loaded points to NbFiles/NbPoints

public class DataManager extends Application implements Runnable {
    private RectF SearchableZone = new RectF(-20000f,-20000f,20000f,20000f); // Values in meters (Power of 2 x 100)
    private Vector StatisticsSelectionSize = new Vector(20f,20f); // Values in meters
    private Vector DisplayedSelectionSize = new Vector(200f,200f); // Values in meters

    private SurveyLoader SurveyLiveGPS; // processing for Live real GPS
    private SurveyLoader SurveySimulatedGPS; // processing for Simulated GPS
    private SurveyLoader SurveyFilesGPS; // processing for Files loading GPS

    private int LastHeartBeat;

    private Handler UpdateTrigger = new Handler();

    private int ActivityMode; // Is Activity is currently in Foreground or Background
    static private String BackendMessage;


    private SimulateGPS EventsSimulatedGPS = null;
    private QuadTree SearchableStorage = null;

    // File Management
    private FileManager FilesHandler=null;
    private FileWriter WriteToFile=null;
    private FileReader ReadFromFile=null;
    private Thread LoadingFiles=null;

    // HeartBeat Provider
    private static HeartBeatProvider HearBeatService = null;

    // Holding Persistent state/default for HMI Switch
    private short ModeGPS = SharedConstants.LiveGPS;
    private short ModeLight = SharedConstants.LightEnhanced;
    private short ModeBattery = SharedConstants.BatteryDrainMode;
    private short ModeScreen = SharedConstants.SleepLocked;
    private short ModeHeartBeat = SharedConstants.DisconnectedHeartBeat;

    // Returning instance of this to Activity ...
    static Context Backend = null;

    static ArrayList<EventsProcessGPS> Clients = new ArrayList<EventsProcessGPS>();
    private static LocationManager SourceGPS;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Backend = this;
        SurveyLiveGPS = new SurveyLoader();
        SurveySimulatedGPS = new SurveyLoader();
        SurveyFilesGPS = new SurveyLoader();

        SurveyLiveGPS.setTrack((short)0);
        SurveySimulatedGPS.setTrack((short)0);

        LastHeartBeat = -1;

        ActivityMode = SharedConstants.SwitchForeground;
        BackendMessage="";

        // Starting HearBeatService if HeartBeat Sensor is available
        if (BluetoothConstants.hasLowEnergyCapabilities) {
            HearBeatService = new HeartBeatProvider(this);
            HearBeatService.searchSensor();
        }

        // Startup mode is always GPS Live mode
        SourceGPS = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Arm trigger for a GPS update ...
        UpdateTrigger.postDelayed(this, SharedConstants.TimeUpdateGPS);

        // Starting File Management
        FilesHandler = new FileManager(this);
        ReadFromFile = new FileReader(FilesHandler, this, SurveyFilesGPS);
        WriteToFile = new FileWriter(FilesHandler);

        // Initialize GPS simulator ...
        EventsSimulatedGPS = new SimulateGPS(this, FilesHandler, SurveySimulatedGPS);
    }

    public void shutdown() {
        if (LoadingFiles!= null) LoadingFiles.interrupt();
        EventsSimulatedGPS.stop();
        WriteToFile.shutdown();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void restartLoadingFiles() {
        SearchableStorage = new QuadTree(SearchableZone); // Create QuadTree storage area
        if (LoadingFiles!= null) LoadingFiles.interrupt();
        FilesHandler.resetStreams(); // Forcing to reload all files
        LoadingFiles = new Thread(ReadFromFile);
        LoadingFiles.start();
    }

    // Called for cyclic Update
    @Override
    public void run() {
        SurveyLiveGPS.updateFromGPS(SourceGPS.getLastKnownLocation(LOCATION_SERVICE));
        SurveyLiveGPS.setHeartbeat((short)LastHeartBeat);

        if (SurveyLiveGPS.getBase() == null) {
            Coordinates Origin = SurveyLiveGPS.getCoordinates();
            SurveyLiveGPS.setBase(Origin);
            SurveyFilesGPS.setBase(Origin);
            restartLoadingFiles();
        }

        SurveySnapshot Sample = SurveyLiveGPS.getSnapshot();
        SearchableStorage.store(Sample);
        WriteToFile.appendJSON(SurveyLiveGPS.toJSON());

        // Loop over registered clients callback ...
        if (ActivityMode == SharedConstants.SwitchForeground) {
            for (EventsProcessGPS Client :Clients) Client.processLocationChanged(Sample);
        }

        // Setup next Update ...
        UpdateTrigger.postDelayed(this, SharedConstants.TimeUpdateGPS);
    }

    // Called Only when simulated GPS is Provided
    public void onSimulatedChanged(Coordinates SimulatedCoordinate) {
        if (SurveySimulatedGPS.getBase() == null) {
            SurveySimulatedGPS.setBase(SimulatedCoordinate);
            SurveyFilesGPS.setBase(SimulatedCoordinate);
            restartLoadingFiles();
        }

        SurveySnapshot Sample = SurveySimulatedGPS.getSnapshot();
        // Loop over registered clients callback ...
        if (ActivityMode == SharedConstants.SwitchForeground) {
            for (EventsProcessGPS Client :Clients) Client.processLocationChanged(Sample);
        }
    }

    // Called Only when a new Sample has been loaded from file
    public void onSnapshotLoaded(SurveySnapshot Sample) {
        if (Sample == null) return;
        SearchableStorage.store(Sample);
    }

    // Called from activity to retrieved last position on WakeUp
    public SurveySnapshot getLastSnapshot(){
        if (ModeGPS == SharedConstants.ReplayedGPS)  return SurveySimulatedGPS.getSnapshot();
        else return SurveyLiveGPS.getSnapshot();
    }

    // Managing state for GPS
    public short getModeGPS() {return ModeGPS;}
    public void storeModeGPS(short mode) {
        ModeGPS = mode;
        if (ModeGPS == SharedConstants.ReplayedGPS)  {
            if (!EventsSimulatedGPS.load(1000).isEmpty())   {
                SourceGPS.removeUpdates(this);
                SurveySimulatedGPS.clearOriginCoordinates();
                EventsSimulatedGPS.simulate();
            }

        }
        if (ModeGPS == SharedConstants.LiveGPS)  {
            EventsSimulatedGPS.stop();
            SurveyLiveGPS.clearOriginCoordinates();
        }
    }

    // Managing Toast from Backend ...
    public  void setBackendMessage(String ToastMessage)  { BackendMessage = ToastMessage; }
    public String getBackendMessage()  {
        String SentMessage = BackendMessage;
        BackendMessage = "";
        return SentMessage;
    }

    // Storing callbacks instance from client View
    public void setUpdateCallback(EventsProcessGPS updateClient){
        Clients.add(updateClient);
    }

    // Called from HeartbeatService when heartbeat is updated
    public void processHeartBeatChanged(int Frequency)  {
        UpdateTrigger.removeCallbacks(HeartBeatTimeout);
        UpdateTrigger.postDelayed(HeartBeatTimeout,UpdateDelay);

        if (ModeGPS == SharedConstants.ReplayedGPS) return;
        LastHeartBeat = Frequency;
    }
    // Managing state for Heartbeat sensor
    public short getModeHeartBeat() {return ModeHeartBeat;}
    public void storeModeHeartBeat(short mode) {
        ModeHeartBeat = mode;
        if (!BluetoothConstants.hasLowEnergyCapabilities) return;
        if (ModeHeartBeat == SharedConstants.SearchHeartBeat) HearBeatService.searchSensor();
    }

    // Managing sleep state for HMI
    public short getModeSleep() {return ModeScreen;}
    public void storeModeSleep(short mode) {ModeScreen = mode;}

    // Managing back light intensity for screen --> Not implemented
    public short getModeLight() {return ModeLight;}
    public void storeModeLight(short mode) {ModeLight = mode;}

    // Managing Animation behaviour to reduce energy consumption --> Not implemented
    public short getModeBattery() {return ModeBattery;}
    public void storeModeBattery(short mode) {ModeBattery = mode;}

    // Called on Sleep/Wakeup of activity
    public void setActivityMode(int mode) { ActivityMode = mode; }

    // Return Application in order to setup callback from client
    static public Context getBackend(){ return Backend; }

    // Return area size selection for statistics
    public Vector getExtractStatisticsSize(){ return StatisticsSelectionSize; }

    // Return area size selection for statistics
    public Vector getExtractDisplayedSize(){ return DisplayedSelectionSize; }

    // Return all Point from a geographic area (in cartesian/meters)
    public ArrayList<SurveySnapshot> extract(RectF searchZone){ return SearchableStorage.search(searchZone); }

    // Convert angle from [0,360°] to [-180°,180°]
    private float signed(float Angle) { return  ((Angle > 180)? (180 - Angle) : Angle); }

    // Filter and return Point that match a Bearing Range
    public ArrayList<SurveySnapshot> filter(ArrayList<SurveySnapshot> Collected, SurveySnapshot Snapshot){
        ArrayList<SurveySnapshot> Filtered = new ArrayList<SurveySnapshot>();

        float Heading = signed(Snapshot.getBearing());
        float ExtractedHeading;
        for (SurveySnapshot Extracted : Collected) {
            ExtractedHeading = signed(Extracted.getBearing());
            if (Math.abs(ExtractedHeading - Heading) > SharedConstants.BearingMatchingGap) continue;
            Filtered.add(Extracted);
        }
        return Filtered;
    }

    private void LostHeartBeatSensor() {
        ModeHeartBeat = SharedConstants.DisconnectedHeartBeat;
    }


}
