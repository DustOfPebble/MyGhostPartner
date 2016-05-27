package com.dustofcloud.daytodayrace;

import android.app.Application;
import android.content.Context;
import android.graphics.RectF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;


public class DataManager extends Application implements  EventsFileReader, LocationListener {
    static RectF StorageArea = new RectF(4800f,4800f,-4800f,-4800f); // StorageArea is a rectangle of 9,6 km in both direction (Power of 2 x 100)
    static final float earthRadius = 6400000f; // Earth Radius is 6400 kms
    static float earthRadiusCorrected = earthRadius; // Value at Equator to Zero at Pole
    static double originLongitude = 0f;
    static double originLatitude = 0f;
    boolean originSet=false;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 seconde


    EventsGPS TrigEvents= null;
    QuadTree WayPoints = null;
    FileWriter WriteToFile=null;
    FileReader ReadFromFile=null;
    FileManager FilesHandler=null;

    // Specific to manage Callback to clients
    static Context BackendContext = null;
    static EventsDataManager NotifyClient = null;
    public static LocationManager SourceGPS;

    // Storing callbacks instance from client View
    public void setUpdateViewCallback (EventsDataManager UpdateViewClient){
        NotifyClient = UpdateViewClient;
    }

    // Return Application in order to setup callback from client
    static public Context getBackend(){
        return BackendContext;
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
        WriteToFile = new FileWriter(FilesHandler);

        TrigEvents = new EventsGPS(this);
        TrigEvents.start();
     }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d("DataManager", "DataManager is terminating...");
        TrigEvents.clear();
        try { WriteToFile.flushBuffer(); }
        catch ( Exception writerError ) {
            Log.d("DataManager","Failed to flush stored GeoData ...");
            writerError.printStackTrace();
        }

    }

    static public float dX(double longitude) {
        return earthRadiusCorrected * (float) Math.toRadians(longitude-originLongitude);
    }

    static public float dY(double latitude) {
        return  earthRadius * (float) Math.toRadians(latitude-originLatitude);
    }

    public ArrayList<GeoData> getInView(RectF ViewArea){
        return WayPoints.searchWayPoints(ViewArea);
    }

    public ArrayList<GeoData> getInUse(RectF UseArea){
        return WayPoints.searchWayPoints(UseArea);
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
        Log.d("DataManager", "GPS notification ==> [" + update.getLongitude() + "°N," + update.getLatitude() + "°E]");
        if ( !originSet ) {
            originLatitude = update.getLatitude();
            originLongitude = update.getLongitude();
            earthRadiusCorrected = earthRadius *(float)Math.cos( Math.toRadians(originLatitude));
            WayPoints = new QuadTree(StorageArea); // Create QuadTree storage area for all waypoints

            //if (!ReadFromFile.isAlive()) ReadFromFile.startReading();
            ReadFromFile.run();
        }

        WayPoints.storeWayPoint(update);

        try { WriteToFile.writeGeoData(update); }
        catch ( Exception writerError ) {
            Log.d("DataManager","Failed to write new GeoData ...");
            writerError.printStackTrace();
        }

        NotifyClient.updateOffset(update.getCartesian());
    }

    @Override
    public void onLoadedPoint(GeoData Loaded) {
        if (Loaded == null) return;
        WayPoints.storeWayPoint(Loaded);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderDisabled(String provider) { }

    @Override
    public void onProviderEnabled(String provider) { }
}
