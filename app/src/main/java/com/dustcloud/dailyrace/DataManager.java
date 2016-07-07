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

    private Handler TimeoutGPS = new Handler();
    private Runnable task = new Runnable() { public void run() { queryGPS();} };
    private int TimeoutDelayGPS = 2000;

    private SurveyLoader SurveyGPS;
    private int LastHeartBeat = -1;

    private int ActivityMode = SharedConstants.SwitchForeground;

    static private String BackendMessage="";
    static private final float earthRadius = 6400000f; // Earth Radius is 6400 kms
    static private float earthRadiusCorrected = earthRadius; // Value at Equator to Zero at Pole

    static private double originLongitude = 0f;
    static private double originLatitude = 0f;
    private boolean originSet=false;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // Value in meters
    private static final long MIN_TIME_BW_UPDATES = 1000; // value in ms

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

    // Storing callbacks instance from client View
    public void setUpdateCallback(EventsProcessGPS updateClient){
        Clients.add(updateClient);
    }

    // Managing state for GPS
    public short getModeGPS() {return ModeGPS;}
    public void storeModeGPS(short mode) {
        ModeGPS = mode;
        if (ModeGPS == SharedConstants.ReplayedGPS)  {
            if (!EventsSimulatedGPS.load(1000).isEmpty())   {
                SourceGPS.removeUpdates(this);
                TimeoutGPS.removeCallbacks(task);
                SurveyGPS = null;
                originSet=false; // Re-enter the Init function...
                EventsSimulatedGPS.simulate();
            }

        }
        if (ModeGPS == SharedConstants.LiveGPS)  {
            EventsSimulatedGPS.stop();
            SourceGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,this );
            TimeoutGPS.postDelayed(task, TimeoutDelayGPS);
        }
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
    public void setActivityMode(int mode) {
        ActivityMode = mode;
        if (ActivityMode == SharedConstants.SwitchBackground) TimeoutGPS.removeCallbacks(task);
        if (ActivityMode == SharedConstants.SwitchForeground) TimeoutGPS.postDelayed(task, TimeoutDelayGPS);
    }

    // Called from activity to retrieved last position on WakeUp
    public LiveSurvey getLastUpdate(){ return SurveyGPS.getSnapshot(); }

    // Return Application in order to setup callback from client
    static public Context getBackend(){
        return Backend;
    }

    // Return area size selection for statistics
    public PointF getExtractStatisticsSize(){
        return StatisticsSelectionSize;
    }

    // Return area size selection for statistics
    public PointF getExtractDisplayedSize(){
        return DisplayedSelectionSize;
    }

    // Return all Point from a geographic area (in cartesian/meters)
    public ArrayList<LiveSurvey> extract(RectF searchZone){ return SearchableStorage.search(searchZone); }

    // Convert angle from [0,360°] to [-180°,180°]
    private float signed(float Angle) { return  ((Angle > 180)? (180 - Angle) : Angle); }

    // Filter and return Point that match a Bearing Range
    public ArrayList<LiveSurvey> filter(ArrayList<LiveSurvey> Collected){
        ArrayList<LiveSurvey> Filtered = new ArrayList<LiveSurvey>();

        float Heading = signed(SurveyGPS.getBearing());
        float ExtractedHeading;
        for (LiveSurvey Extracted : Collected) {
            ExtractedHeading = signed(Extracted.getBearing());
            if (Math.abs(ExtractedHeading - Heading) > SharedConstants.BearingMatchingGap) continue;
            Filtered.add(Extracted);
        }
        return Filtered;
    }

    // Utility function to convert Latitude/Longitude to cartesian/metric values
    private static float dX(double longitude) {
        return earthRadiusCorrected * (float) Math.toRadians(longitude-originLongitude);
    }

    private static float dY(double latitude) {
        return  earthRadius * (float) Math.toRadians(latitude-originLatitude);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Backend = this;
        SurveyGPS = new SurveyLoader();

        // Starting HeartBeat if HeartBeat Sensor is available
        if (BluetoothConstants.isLowEnergy) {
            HearBeatService = new HeartBeatProvider(this);
            HearBeatService.searchSensor();
        }

        // Startup mode is always GPS Live mode
        SourceGPS = (LocationManager) getSystemService(LOCATION_SERVICE);
        SourceGPS.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                this);

        // Starting File Management
        FilesHandler = new FileManager(this);
        ReadFromFile = new FileReader(FilesHandler, this);

        try{ WriteToFile = new FileWriter(FilesHandler);}
        catch (Exception ErrorDB) {Log.d("DataManager", "Couldn't create a new DB...");}

        // Initialize GPS simulator ...
        EventsSimulatedGPS = new SimulateGPS(this, FilesHandler);
    }

    public void processHeartBeatChanged(int Frequency)  {
        if (ModeGPS == SharedConstants.ReplayedGPS) return;
        LastHeartBeat = Frequency;
    }

    private void init(SurveyLoader update) {
        originLatitude = update.getLatitude();
        originLongitude = update.getLongitude();
        earthRadiusCorrected = earthRadius *(float)Math.cos( Math.toRadians(originLatitude));
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
        TimeoutGPS.removeCallbacks(task);
        SourceGPS.removeUpdates(this);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void queryGPS() {
        Location LastKnownGPS = SourceGPS.getLastKnownLocation(LOCATION_SERVICE);
        if (LastKnownGPS == null) return;
        SurveyGPS.setLive(false);
        SurveyGPS.setGPS(LastKnownGPS);
        processLocationChanged();
    }

    @Override
    public void onLocationChanged(Location LastKnownGPS) {
        if (LastKnownGPS == null) return;
        SurveyGPS.setLive(true);
        SurveyGPS.setGPS(LastKnownGPS);
        processLocationChanged();
    }

    public void processLocationChanged() {

        Log.d("DataManager", "GPS [" + SurveyGPS.getLongitude() + "°E," + SurveyGPS.getLatitude() + "°N]");

        if ( !originSet ) { init(SurveyGPS); originSet = true; }

        // Converting Longitude & Latitude to 2D cartesian distance from an origin
        PointF Displacement = new PointF(dX(SurveyGPS.getLongitude()),dY(SurveyGPS.getLatitude()));
        LiveSurvey Sample = SurveyGPS.getSnapshot();
        Sample.setCoordinate(Displacement);

        // Updating with Last HeartBeat
        if (SurveyGPS.isLive()) SurveyGPS.setHeartbeat((short)LastHeartBeat);

        if (SurveyGPS.isLive()) {
            SearchableStorage.store(Sample);
            WriteToFile.writeSurvey(SurveyGPS);
        }
        // Loop over registered clients callback ...
        if (ActivityMode == SharedConstants.SwitchForeground) {
            TimeoutGPS.removeCallbacks(task);
            for (EventsProcessGPS Client :Clients) { Client.processLocationChanged(Sample);}
        }
    }

    public void onLoaded(SurveyLoader Loaded) {
        if (Loaded == null) return;
        LiveSurvey Sample = Loaded.getSnapshot();
        Sample.setCoordinate(new PointF(dX(Loaded.getLongitude()),dY(Loaded.getLatitude())));
        SearchableStorage.store(Sample);
    }

    // Managing Toast from Backend ...
    public  void setBackendMessage(String ToastMessage)  { BackendMessage = ToastMessage; }
    public String getBackendMessage()  {
        String SentMessage = BackendMessage;
        BackendMessage = "";
        return SentMessage;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderDisabled(String provider) { }

    @Override
    public void onProviderEnabled(String provider) { }
}
