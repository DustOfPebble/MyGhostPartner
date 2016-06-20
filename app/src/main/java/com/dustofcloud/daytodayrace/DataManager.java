package com.dustofcloud.daytodayrace;

import android.app.Application;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class DataManager extends Application implements LocationListener {
    private  RectF SearchableZone = new RectF(-20000f,-20000f,20000f,20000f); // Values in meters (Power of 2 x 100)
    private PointF InUseArea = new PointF(10f,10f); // Values in meters
    private PointF InViewArea = new PointF(200f,200f); // Values in meters (subject to change vs  speed)

    private GeoData LastUpdate;
    private int LastHeartBeat = -1;

    private int ActivityMode = SharedConstants.SwitchForeground;

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
    private short ModeHeartBeat = SharedConstants.DisconnetedHeartBeat;

    // Returning instance of this to Activity ...
    static Context Backend = null;

    static ArrayList<EventsProcessGPS> Clients = new ArrayList<EventsProcessGPS>();
    private static LocationManager SourceGPS;

    // Storing callbacks instance from client View
    public void setUpdateCallback(EventsProcessGPS updateClient){
        Clients.add(updateClient);
    }

    // Getter/Setter for ControlSwitch mode
    public void storeModeGPS(short mode) {
        ModeGPS = mode;
        if (ModeGPS == SharedConstants.ReplayedGPS)  {
            if (!EventsSimulatedGPS.load(1000).isEmpty())   {
                SourceGPS.removeUpdates(this);
                EventsSimulatedGPS.sendGPS();
            }
        }
        if (ModeGPS == SharedConstants.LiveGPS)  {
            EventsSimulatedGPS.stop();
            SourceGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,this );
        }
    }
    public short getModeGPS() {return ModeGPS;}

    public void storeModeHeartBeat(short mode) {
        ModeHeartBeat = mode;
        if (ModeHeartBeat == SharedConstants.ConnectedHeartBeat) HearBeatService.searchSensor();
    }


    public short getModeHeartBeat() {return ModeHeartBeat;}

    public void storeModeSleep(short mode) {ModeScreen = mode;}
    public short getModeSleep() {return ModeScreen;}
    public void storeModeLight(short mode) {ModeLight = mode;}
    public short getModeLight() {return ModeLight;}
    public void storeModeBattery(short mode) {ModeBattery = mode;}
    public short getModeBattery() {return ModeBattery;}
    public void setActivityMode(int mode) { ActivityMode = mode; }
    public GeoData getLastUpdate(){ return LastUpdate; }

    // Return Application in order to setup callback from client
    static public Context getBackend(){
        return Backend;
    }

    // Return area size selection for statistics
    public PointF getComputedSize(){
        return InUseArea;
    }

    // Return area size selection for statistics
    public PointF getDisplayedSize(){
        return InViewArea;
    }

    public ArrayList<GeoData> extract(RectF searchZone){
        return SearchableStorage.search(searchZone);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d("DataManager", "DataManager has been created...");
        Backend = this;

        // Starting HeartBeat if HeartBeat Sensor is connected
        HearBeatService = new HeartBeatProvider(this);

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

    public float dX(double longitude) {
        return earthRadiusCorrected * (float) Math.toRadians(longitude-originLongitude);
    }

    public float dY(double latitude) {
        return  earthRadius * (float) Math.toRadians(latitude-originLatitude);
    }

     @Override
    public void onLocationChanged(Location update) {
        if (update == null) return;
        GeoData geoInfo = new GeoData();
        geoInfo.setGPS(update);
        processLocationChanged(geoInfo);
    }

    private void init(GeoData update) {
        originLatitude = update.getLatitude();
        originLongitude = update.getLongitude();
        earthRadiusCorrected = earthRadius *(float)Math.cos( Math.toRadians(originLatitude));
        SearchableStorage = new QuadTree(SearchableZone); // Create QuadTree storage area
        LoadingFiles = new Thread(ReadFromFile);
        LoadingFiles.start();
    }

    public void processHeartBeatChanged(int Frequency)  {
        LastUpdate.setHeartbeat(Frequency);
        LastHeartBeat = Frequency;
    }

    public void processLocationChanged(GeoData update) {
        if (update == null) return;
        Log.d("DataManager", "GPS notification ==> [" + update.getLongitude() + "°E," + update.getLatitude() + "°N]");

        if ( !originSet ) {
            init(update);
            originSet = true;
        }

        // Converting Longitude & Latitude to 2D cartesian distance from an origin
        update.setCoordinate(new PointF(dX(update.getLongitude()),dY(update.getLatitude())));
        Log.d("DataManager", "Coordinate["+update.getCoordinate().x+","+update.getCoordinate().y+"]");

        // Updating with Last HeartBeat
        update.setHeartbeat(LastHeartBeat);

        if (LastUpdate !=null) {
            if (LastUpdate.isLive()) {
                SearchableStorage.store(LastUpdate);
                WriteToFile.writeGeoData(update);
            }
        }
        LastUpdate = update;

        // Loop over registered clients callback ...
        if (ActivityMode == SharedConstants.SwitchForeground)
            for (EventsProcessGPS Client :Clients) { Client.processLocationChanged(update);}
    }

    public void onLoaded(GeoData Loaded) {
        if (Loaded == null) return;
        Loaded.setCoordinate(new PointF(dX(Loaded.getLongitude()),dY(Loaded.getLatitude())));
        SearchableStorage.store(Loaded);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderDisabled(String provider) { }

    @Override
    public void onProviderEnabled(String provider) { }
}
