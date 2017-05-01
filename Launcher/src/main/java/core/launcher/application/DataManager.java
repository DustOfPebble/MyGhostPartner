package core.launcher.application;

import android.app.Application;
import android.content.Context;
import android.graphics.RectF;
import android.location.LocationManager;
import android.os.Handler;

import java.util.ArrayList;

import core.Structures.Coordinates;
import services.GPS.EventsGPS;
import services.GPS.SensorProvider;
import services.Sensor.SensorEvents;
import services.Sensor.SensorState;
import services.Base.Base;
import core.Structures.Statistics;
import core.Structures.Node;

public class DataManager extends Application implements Runnable, SensorEvents {
    private RectF SearchableZone = new RectF(-20000f,-20000f,20000f,20000f); // Values in meters (Power of 2 dx 100)
    private Node StatisticsSelectionSize = new Node(20f,20f); // Values in meters
    private Node DisplayedSelectionSize = new Node(200f,200f); // Values in meters

    private SurveyLoader SurveyLiveGPS; // processing for Live real CoreGPS
    private SurveyLoader SurveySimulatedGPS; // processing for Simulated CoreGPS
    private SurveyLoader SurveyFilesGPS; // processing for Files loading CoreGPS

    private Handler UpdateTrigger = new Handler();

    private int ActivityMode; // Is Activity is currently in Foreground or Background
    static private String BackendMessage;


    private LoaderGPS CollectedGPS = null;
    private Base SearchableStorage = null;

    // File Management
    private FileManager FilesHandler=null;
    private FileWriter WriteToFile=null;
    private FileReader ReadFromFile=null;
    private Thread LoadingFiles=null;

    // HeartBeat Provider
    private static SensorProvider HeartBeatService = null;

    // Holding Persistent state/default for HMI Switch
    private short ModeGPS = Constants.LiveGPS;
    private short ModeLight = Constants.LightEnhanced;
    private short ModeBattery = Constants.BatteryDrainMode;
    private short ModeScreen = Constants.SleepLocked;
    private short ModeHeartBeat = Constants.DisconnectedHeartBeat;

    // Returning instance of this to Activity ...
    static Context Backend = null;

    static ArrayList<EventsGPS> Clients = new ArrayList<EventsGPS>();
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

        ActivityMode = Constants.SwitchForeground;
        BackendMessage="";

        // Starting HeartBeatService if HeartBeat Sensor is available
        if (SensorState.hasLowEnergyCapabilities) {
            HeartBeatService = new HeartBeatProvider(this);
            HeartBeatService.searchSensor();
        }

        // Startup mode is always CoreGPS Live mode
        SourceGPS = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Arm trigger for a CoreGPS update ...
        UpdateTrigger.postDelayed(this, Constants.TimeUpdateGPS);

        // Starting File Management
        FilesHandler = new FileManager(this);
        ReadFromFile = new FileReader(FilesHandler, this, SurveyFilesGPS);
        WriteToFile = new FileWriter(FilesHandler);

        // Initialize CoreGPS simulator ...Events
        CollectedGPS = new LoaderGPS(FilesHandler);
    }

    public void shutdown() {
        if (LoadingFiles!= null) LoadingFiles.interrupt();
        UpdateTrigger.removeCallbacks(this);
        WriteToFile.shutdown();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void restartLoadingFiles() {
        SearchableStorage = new Base(SearchableZone); // Create QuadTree storage area
        if (LoadingFiles!= null) LoadingFiles.interrupt();
        FilesHandler.resetStreams(); // Forcing to reload all files
        LoadingFiles = new Thread(ReadFromFile);
        LoadingFiles.start();
    }

    // Called for cyclic Update
    @Override
    public void run() {

        if (ModeGPS == Constants.ReplayedGPS)  {
            if (!EventsSimulatedGPS.load(1000).isEmpty())   {
                SourceGPS.removeUpdates(this);
                SurveySimulatedGPS.clearOriginCoordinates();
                EventsSimulatedGPS.simulate();
            }

        }
        if (ModeGPS == Constants.LiveGPS)  {
            EventsSimulatedGPS.stop();
            SurveyLiveGPS.clearOriginCoordinates();
        }

        SurveyLiveGPS.updateFromGPS(SourceGPS.getLastKnownLocation(LOCATION_SERVICE));
        SurveyLiveGPS.setHeartbeat((short)LastHeartBeat);

        if (SurveyLiveGPS.getBase() == null) {
            Coordinates Origin = SurveyLiveGPS.getCoordinates();
            SurveyLiveGPS.setBase(Origin);
            SurveyFilesGPS.setBase(Origin);
            restartLoadingFiles();
        }

        Statistics Sample = SurveyLiveGPS.getSnapshot();
        SearchableStorage.store(Sample);
        WriteToFile.appendJSON(SurveyLiveGPS.toJSON());

        // Loop over registered clients callback ...
        if (ActivityMode == Constants.SwitchForeground) {
            for (EventsGPS Client :Clients) Client.processLocationChanged(Sample);
        }

        // Setup next Update ...
        UpdateTrigger.postDelayed(this, Constants.TimeUpdateGPS);
    }

    // Called Only when simulated CoreGPS is Provided
    public void onSimulatedChanged(Coordinates SimulatedCoordinate) {
        if (SurveySimulatedGPS.getBase() == null) {
            SurveySimulatedGPS.setBase(SimulatedCoordinate);
            SurveyFilesGPS.setBase(SimulatedCoordinate);
            restartLoadingFiles();
        }

        Statistics Sample = SurveySimulatedGPS.getSnapshot();
        // Loop over registered clients callback ...
        if (ActivityMode == Constants.SwitchForeground) {
            for (EventsGPS Client :Clients) Client.processLocationChanged(Sample);
        }
    }

    // Called Only when a new Sample has been loaded from file
    public void onSnapshotLoaded(Statistics Sample) {
        if (Sample == null) return;
        SearchableStorage.store(Sample);
    }

    // Called from activity to retrieved last position on WakeUp
    public Statistics getLastSnapshot(){
        if (ModeGPS == Constants.ReplayedGPS)  return SurveySimulatedGPS.getSnapshot();
        else return SurveyLiveGPS.getSnapshot();
    }

    // Managing state for CoreGPS
    public short getModeGPS() {return ModeGPS;}
    public void storeModeGPS(short mode) {
        ModeGPS = mode;
        if (ModeGPS == Constants.ReplayedGPS)  {
            if (!EventsSimulatedGPS.load(1000).isEmpty())   {
                SourceGPS.removeUpdates(this);
                SurveySimulatedGPS.clearOriginCoordinates();
                EventsSimulatedGPS.simulate();
            }

        }
        if (ModeGPS == Constants.LiveGPS)  {
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
    public void setUpdateCallback(EventsGPS updateClient){
        Clients.add(updateClient);
    }

    // Called from HeartbeatService when heartbeat is updated
    public void processHeartBeatChanged(int Frequency)  {
        UpdateTrigger.removeCallbacks(HeartBeatTimeout);
        UpdateTrigger.postDelayed(HeartBeatTimeout,UpdateDelay);

        if (ModeGPS == Constants.ReplayedGPS) return;
        LastHeartBeat = Frequency;
    }
    // Managing state for Heartbeat sensor
    public short getModeHeartBeat() {return ModeHeartBeat;}
    public void storeModeHeartBeat(short mode) {
        ModeHeartBeat = mode;
        if (!SensorState.hasLowEnergyCapabilities) return;
        if (ModeHeartBeat == Constants.SearchHeartBeat) HeartBeatService.searchSensor();
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
    public Node getExtractStatisticsSize(){ return StatisticsSelectionSize; }

    // Return area size selection for statistics
    public Node getExtractDisplayedSize(){ return DisplayedSelectionSize; }

    // Return all Point from a geographic area (in cartesian/meters)
    public ArrayList<Statistics> extract(RectF searchZone){ return SearchableStorage.collect(searchZone); }

    // Convert angle from [0,360°] to [-180°,180°]
    private float signed(float Angle) { return  ((Angle > 180)? (180 - Angle) : Angle); }

    // Filter and return Point that match a Bearing Range
    public ArrayList<Statistics> filter(ArrayList<Statistics> Collected, Statistics Snapshot){
        ArrayList<Statistics> Filtered = new ArrayList<Statistics>();

        float Heading = signed(Snapshot.getBearing());
        float ExtractedHeading;
        for (Statistics Extracted : Collected) {
            ExtractedHeading = signed(Extracted.getBearing());
            if (Math.abs(ExtractedHeading - Heading) > Constants.BearingMatchingGap) continue;
            Filtered.add(Extracted);
        }
        return Filtered;
    }

    private void LostHeartBeatSensor() {
        ModeHeartBeat = Constants.DisconnectedHeartBeat;
    }


}
