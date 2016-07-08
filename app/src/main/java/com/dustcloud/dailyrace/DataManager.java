package com.dustcloud.dailyrace;

import android.app.Application;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
//ToDo: Reload DB periodically with a smaller SearchableZone to reduce memory footprint
//ToDo: Limits numbers of loaded points to NbFiles/NbPoints

public class DataManager extends Application implements LocationListener {
    private RectF SearchableZone = new RectF(-20000f,-20000f,20000f,20000f); // Values in meters (Power of 2 x 100)
    private PointF StatisticsSelectionSize = new PointF(20f,20f); // Values in meters
    private PointF DisplayedSelectionSize = new PointF(200f,200f); // Values in meters

    private SurveyLoader SurveyGPS; // Handle GPS position (Physical or Simulated)

    private int LastHeartBeat;
    private int ActivityMode;
    static private String BackendMessage;

    private static final long DistanceUpdateGPS = 5; // Value in meters
    private static final long TimeUpdateGPS = 1000; // value in ms

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
        SurveyGPS = new SurveyLoader();
        LastHeartBeat = -1;
        ActivityMode = SharedConstants.SwitchForeground;
        BackendMessage="";

        // Starting HearBeatService if HeartBeat Sensor is available
        if (BluetoothConstants.isLowEnergy) {
            HearBeatService = new HeartBeatProvider(this);
            HearBeatService.searchSensor();
        }

        // Startup mode is always GPS Live mode
        SourceGPS = (LocationManager) getSystemService(LOCATION_SERVICE);
        SourceGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, TimeUpdateGPS, DistanceUpdateGPS, this);

        // Starting File Management
        FilesHandler = new FileManager(this);
        ReadFromFile = new FileReader(FilesHandler, this);

        try{ WriteToFile = new FileWriter(FilesHandler);}
        catch (Exception ErrorDB) {Log.d("DataManager", "Couldn't create a new DB...");}

        // Initialize GPS simulator ...
        EventsSimulatedGPS = new SimulateGPS(this, FilesHandler);
    }

    private void init() {
        SearchableStorage = new QuadTree(SearchableZone); // Create QuadTree storage area
        if (LoadingFiles!= null) LoadingFiles.interrupt();
        FilesHandler.resetStreams(); // Forcing to reload all files
        LoadingFiles = new Thread(ReadFromFile);
        LoadingFiles.start();
    }

    public void shutdown() {
        if (LoadingFiles!= null) LoadingFiles.interrupt();
        EventsSimulatedGPS.stop();
        WriteToFile.shutdown();
        SourceGPS.removeUpdates(this);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    // Called Only when real GPS is Updated
    @Override
    public void onLocationChanged(Location LastKnownGPS) {
        if (LastKnownGPS == null) return;
        SurveyGPS.updateFromGPS(LastKnownGPS);
        SurveyGPS.setHeartbeat((short)LastHeartBeat);

        Snapshot Sample = SurveyGPS.getSnapshot();
        SearchableStorage.store(Sample);
        WriteToFile.appendJSON(SurveyGPS.toJSON());

        // Loop over registered clients callback ...
        if (ActivityMode == SharedConstants.SwitchForeground) {
            for (EventsProcessGPS Client :Clients) { Client.processLocationChanged(Sample);}
        }
    }

    // Called Only when simulated GPS is Provided
    public void onSimulatedChanged(Snapshot Sample) {
        // Loop over registered clients callback ...
        if (ActivityMode == SharedConstants.SwitchForeground) {
            for (EventsProcessGPS Client :Clients) { Client.processLocationChanged(Sample);}
        }
    }

    // Called Only when a new Sample has been loaded from file
    public void onSnapshotLoaded(Snapshot Sample) {
        if (Sample == null) return;
        SearchableStorage.store(Sample);
    }

    // Called from activity to retrieved last position on WakeUp
    public Snapshot getLastSnapshot(){ return SurveyGPS.getSnapshot(); }

    // Managing state for GPS
    public short getModeGPS() {return ModeGPS;}
    public void storeModeGPS(short mode) {
        ModeGPS = mode;
        if (ModeGPS == SharedConstants.ReplayedGPS)  {
            if (!EventsSimulatedGPS.load(1000).isEmpty())   {
                SourceGPS.removeUpdates(this);
                SurveyGPS = null;
                EventsSimulatedGPS.simulate();
            }

        }
        if (ModeGPS == SharedConstants.LiveGPS)  {
            EventsSimulatedGPS.stop();
            SourceGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, TimeUpdateGPS, DistanceUpdateGPS,this);
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
        if (ModeGPS == SharedConstants.ReplayedGPS) return;
        LastHeartBeat = Frequency;
    }
    // Managing state for Heartbeat sensor
    public short getModeHeartBeat() {return ModeHeartBeat;}
    public void storeModeHeartBeat(short mode) {
        ModeHeartBeat = mode;
        if (!BluetoothConstants.isLowEnergy) return;
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
    public PointF getExtractStatisticsSize(){ return StatisticsSelectionSize; }

    // Return area size selection for statistics
    public PointF getExtractDisplayedSize(){ return DisplayedSelectionSize; }

    // Return all Point from a geographic area (in cartesian/meters)
    public ArrayList<Snapshot> extract(RectF searchZone){ return SearchableStorage.search(searchZone); }

    // Convert angle from [0,360°] to [-180°,180°]
    private float signed(float Angle) { return  ((Angle > 180)? (180 - Angle) : Angle); }

    // Filter and return Point that match a Bearing Range
    public ArrayList<Snapshot> filter(ArrayList<Snapshot> Collected){
        ArrayList<Snapshot> Filtered = new ArrayList<Snapshot>();

        float Heading = signed(SurveyGPS.getSnapshot().getBearing());
        float ExtractedHeading;
        for (Snapshot Extracted : Collected) {
            ExtractedHeading = signed(Extracted.getBearing());
            if (Math.abs(ExtractedHeading - Heading) > SharedConstants.BearingMatchingGap) continue;
            Filtered.add(Extracted);
        }
        return Filtered;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderDisabled(String provider) { }

    @Override
    public void onProviderEnabled(String provider) { }
}
