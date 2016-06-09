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
    private  RectF GeoArea = new RectF(-20000f,-20000f,20000f,20000f); // Values in meters (Power of 2 x 100)
    private PointF InUseArea = new PointF(10f,10f); // Values in meters
    private PointF InViewArea = new PointF(200f,200f); // Values in meters (subject to change vs  speed)

    private GeoData LastUpdate;

    private int RunningMode = SharedConstants.SwitchForeground;

    static private final float earthRadius = 6400000f; // Earth Radius is 6400 kms
    static private float earthRadiusCorrected = earthRadius; // Value at Equator to Zero at Pole

    static private double originLongitude = 0f;
    static private double originLatitude = 0f;
    private boolean originSet=false;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // Value in meters
    private static final long MIN_TIME_BW_UPDATES = 1000; // value in ms

    private SimulateGPS EventsSimulatedGPS = null;

    private QuadTree HighAccuracyStorage = null;
    private QuadTree LowAccuracyStorage = null;
    private FileWriter WriteToFile=null;

    private FileReader ReadFromFile=null;
    private Thread LoadingFiles=null;
    private FileManager FilesHandler=null;

    // Specific to manage Callback to clients
    static Context BackendContext = null;
    static ArrayList<EventsProcessGPS> Clients = new ArrayList<EventsProcessGPS>();
    public static LocationManager SourceGPS;

    // Storing callbacks instance from client View
    public void setUpdateCallback(EventsProcessGPS updateClient){
        Clients.add(updateClient);
    }

    // Return Application in order to setup callback from client
    static public Context getBackend(){
        return BackendContext;
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
        return HighAccuracyStorage.search(searchZone);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d("DataManager", "DataManager has been created...");
        BackendContext = this;

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
        HighAccuracyStorage = new QuadTree(GeoArea); // Create QuadTree storage area
        LowAccuracyStorage = new QuadTree(GeoArea); // Create QuadTree storage area
        LoadingFiles = new Thread(ReadFromFile);
        LoadingFiles.start();
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

        if (LastUpdate !=null) {
            if (LastUpdate.isLive()) {
                if (LastUpdate.getAccuracy() <= SharedConstants.LowPrecisionLimit) HighAccuracyStorage.store(LastUpdate);
                else  LowAccuracyStorage.store(LastUpdate);

                WriteToFile.writeGeoData(update);
            }
        }
        LastUpdate = update;

        // Loop over registered clients callback ...
        if (RunningMode == SharedConstants.SwitchForeground)
            for (EventsProcessGPS Client :Clients) { Client.processLocationChanged(update);}
    }

    public void setMode(int ModeID)
    {
        if (ModeID == SharedConstants.ReplayedGPS)  {
            if (!EventsSimulatedGPS.load(1000).isEmpty())   {
                SourceGPS.removeUpdates(this);
                EventsSimulatedGPS.sendGPS();
            }
        }
        if (ModeID == SharedConstants.LiveGPS)  {
            EventsSimulatedGPS.stop();
            SourceGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,this );
        }

        if (ModeID == SharedConstants.SwitchForeground)   RunningMode = ModeID;
        if (ModeID == SharedConstants.SwitchBackground)   RunningMode = ModeID;
    }

    public void onLoaded(GeoData Loaded) {
        if (Loaded == null) return;
        Loaded.setCoordinate(new PointF(dX(Loaded.getLongitude()),dY(Loaded.getLatitude())));
        if (Loaded.getAccuracy() <= SharedConstants.LowPrecisionLimit) HighAccuracyStorage.store(Loaded);
        else  LowAccuracyStorage.store(Loaded);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderDisabled(String provider) { }

    @Override
    public void onProviderEnabled(String provider) { }
}
