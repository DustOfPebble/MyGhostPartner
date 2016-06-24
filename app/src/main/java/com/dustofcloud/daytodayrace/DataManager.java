package com.dustofcloud.daytodayrace;

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

public class DataManager extends Application implements LocationListener {
    private RectF SearchableZone = new RectF(-20000f,-20000f,20000f,20000f); // Values in meters (Power of 2 x 100)
    private PointF StatisticsSelectionSize = new PointF(10f,10f); // Values in meters
    private PointF DisplayedSelectionSize = new PointF(200f,200f); // Values in meters (subject to change vs  speed)

    private Handler TimeoutGPS = new Handler();
    private Runnable task = new Runnable() { public void run() { queryGPG();} };
    private int TimoutDelay = 2000;

    private GeoData LastUpdate;
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
                EventsSimulatedGPS.sendGPS();
            }
        }
        if (ModeGPS == SharedConstants.LiveGPS)  {
            EventsSimulatedGPS.stop();
            SourceGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,this );
            TimeoutGPS.postDelayed(task,TimoutDelay);
        }
    }

    // Managing state for Heartbeat sensor
    public short getModeHeartBeat() {return ModeHeartBeat;}
    public void storeModeHeartBeat(short mode) {
        ModeHeartBeat = mode;
        if (ModeHeartBeat == SharedConstants.ConnectedHeartBeat) HearBeatService.searchSensor();
    }

    // Managing sleep state for HMI
    public short getModeSleep() {return ModeScreen;}
    public void storeModeSleep(short mode) {ModeScreen = mode;}

    // Managing backlight intensity for screen --> Not implemented
    public short getModeLight() {return ModeLight;}
    public void storeModeLight(short mode) {ModeLight = mode;}

    // Managing Animation behaviour to reduce energy consumption --> Not implemented
    public short getModeBattery() {return ModeBattery;}
    public void storeModeBattery(short mode) {ModeBattery = mode;}

    // Called on Sleep/Wakeup of activity
    public void setActivityMode(int mode) {
        ActivityMode = mode;
        if (ActivityMode == SharedConstants.SwitchBackground) TimeoutGPS.removeCallbacks(task);
        if (ActivityMode == SharedConstants.SwitchForeground) TimeoutGPS.postDelayed(task,TimoutDelay);
    }

    // Called from activity to retrieved last position on WakeUp
    public GeoData getLastUpdate(){ return LastUpdate; }

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
    public ArrayList<GeoData> extract(RectF searchZone){ return SearchableStorage.search(searchZone); }

    // Convert angle from [0,360°] to [-180°,180°]
    private float signed(float Angle) { return  ((Angle > 180)? (180 - Angle) : Angle); }

    // Filter and return Point that match a Speed Range and Bearing Range --> Not Used
    public ArrayList<GeoData> filter(ArrayList<GeoData> Collected){
        ArrayList<GeoData> Filtered = new ArrayList<GeoData>();
        float SpeedRange = SharedConstants.SpeedMatchingFactor * LastUpdate.getSpeed() * 3.6f;
        if (SpeedRange < 2) SpeedRange =2;
        if (SpeedRange >10) SpeedRange =10;

        float Heading = signed(LastUpdate.getBearing());
        float ExtractedHeading;
        for (GeoData Extracted : Collected) {
            if (Math.abs(Extracted.getSpeed() - LastUpdate.getSpeed()) > SpeedRange) continue;
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

        // Starting HeartBeat if HeartBeat Sensor is connected
        HearBeatService = new HeartBeatProvider(this);
        HearBeatService.searchSensor();

        SourceGPS = (LocationManager) getSystemService(LOCATION_SERVICE);
        SourceGPS.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                this);

        // Starting File Management
        FilesHandler = new FileManager(this);
        ReadFromFile = new FileReader(FilesHandler, this);

        try{ WriteToFile = new FileWriter(FilesHandler);}
        catch (Exception ErrorDB) {}
        if (WriteToFile == null) Log.d("DataManager", "Couldn't create a new DB...");

        EventsSimulatedGPS = new SimulateGPS(this);
    }

    public void processHeartBeatChanged(int Frequency)  {
        if (ModeGPS == SharedConstants.ReplayedGPS) return;
        LastHeartBeat = Frequency;
    }

    private void init(GeoData update) {
        originLatitude = update.getLatitude();
        originLongitude = update.getLongitude();
        earthRadiusCorrected = earthRadius *(float)Math.cos( Math.toRadians(originLatitude));
        SearchableStorage = new QuadTree(SearchableZone); // Create QuadTree storage area
        LoadingFiles = new Thread(ReadFromFile);
        LoadingFiles.start();
    }

    public void shutdown() {
        if (LoadingFiles!= null) LoadingFiles.interrupt();
        WriteToFile.shutdown();
        TimeoutGPS.removeCallbacks(task);
        SourceGPS.removeUpdates(this);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void queryGPG() {
        Location LastKnownGPS = SourceGPS.getLastKnownLocation(LOCATION_SERVICE);
        if (LastKnownGPS == null) return;
        GeoData LastGPS = new GeoData();
        LastGPS.setGPS(LastKnownGPS);
        LastGPS.setSimulated();
        processLocationChanged(LastGPS);
    }

    @Override
    public void onLocationChanged(Location update) {
        if (update == null) return;
        GeoData geoInfo = new GeoData();
        geoInfo.setGPS(update);
        processLocationChanged(geoInfo);
    }

    public void processLocationChanged(GeoData update) {
        if (update == null) return;

        Log.d("DataManager", "GPS [" + update.getLongitude() + "°E," + update.getLatitude() + "°N]");

        if ( !originSet ) { init(update); originSet = true; }

        // Converting Longitude & Latitude to 2D cartesian distance from an origin
        PointF Displacement =new PointF(dX(update.getLongitude()),dY(update.getLatitude()));
        update.setCoordinate(Displacement);

        // Updating with Last HeartBeat
        update.setHeartbeat(LastHeartBeat);
        if (update.isLive()) {
            SearchableStorage.store(update);
            WriteToFile.writeGeoData(update);
        }
        // Loop over registered clients callback ...
        if (ActivityMode == SharedConstants.SwitchForeground) {
            TimeoutGPS.removeCallbacks(task);
            for (EventsProcessGPS Client :Clients) { Client.processLocationChanged(update);}
        }
        LastUpdate = update;
    }

    public void onLoaded(GeoData Loaded) {
        if (Loaded == null) return;
        Loaded.setCoordinate(new PointF(dX(Loaded.getLongitude()),dY(Loaded.getLatitude())));
        SearchableStorage.store(Loaded);
    }

    // Managing Toast from Backend ...
    public  void setBackendMessage(String ToastMessage)  { BackendMessage = ToastMessage; }
    public String getBackendMessage()  {
        String SentMessage = new String(BackendMessage);
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
