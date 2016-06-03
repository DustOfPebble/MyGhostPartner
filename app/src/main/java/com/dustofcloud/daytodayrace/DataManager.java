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


public class DataManager extends Application implements  EventsFileReader, LocationListener {
    private  RectF GeoArea = new RectF(-20000f,-20000f,20000f,20000f); // Rectangle of 9,6 km in both direction (Power of 2 x 100)
    private PointF InUseArea = new PointF(10f,10f); // In Use area : values in meters
    private PointF InViewArea = new PointF(200f,200f); // In View area : values in meters (subject to change vs  speed)

    static private final float earthRadius = 6400000f; // Earth Radius is 6400 kms
    static private float earthRadiusCorrected = earthRadius; // Value at Equator to Zero at Pole

    static private double originLongitude = 0f;
    static private double originLatitude = 0f;
    private boolean originSet=false;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // Value in meters
    private static final long MIN_TIME_BW_UPDATES = 1000; // value in ms


    SimulateGPS TrigEvents= null;
    QuadTree GeoStorage = null;
    FileWriter WriteToFile=null;
    FileReader ReadFromFile=null;
    FileManager FilesHandler=null;

    // Specific to manage Callback to clients
    static Context BackendContext = null;
    static ArrayList<EventsGPS> Clients = new ArrayList<EventsGPS>();
    public static LocationManager SourceGPS;


    // Storing callbacks instance from client View
    public void setUpdateCallback(EventsGPS updateClient){
        Clients.add(updateClient);
    }

    // Return Application in order to setup callback from client
    static public Context getBackend(){
        return BackendContext;
    }

    // Return area size selection for statistics
    public PointF getSelectionArea(){
        return InUseArea;
    }

    // Return area size selection for statistics
    public PointF getViewArea(){
        return InViewArea;
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

        TrigEvents = new SimulateGPS(this);
//        TrigEvents.start();
    }


    static public float dX(double longitude) {
        return earthRadiusCorrected * (float) Math.toRadians(longitude-originLongitude);
    }

    static public float dY(double latitude) {
        return  earthRadius * (float) Math.toRadians(latitude-originLatitude);
    }

    public ArrayList<GeoData> getInView(RectF ViewArea){
        return GeoStorage.search(ViewArea);
    }

    public ArrayList<GeoData> getInUse(RectF UseArea){
        return GeoStorage.search(UseArea);
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
            originSet = true;
            originLatitude = update.getLatitude();
            originLongitude = update.getLongitude();
            earthRadiusCorrected = earthRadius *(float)Math.cos( Math.toRadians(originLatitude));
            GeoStorage = new QuadTree(GeoArea); // Create QuadTree storage area

            if (!ReadFromFile.isAlive()) ReadFromFile.startReading();
            //ReadFromFile.run();
        }

        GeoStorage.store(update);

        try { WriteToFile.writeGeoData(update); }
        catch ( Exception writerError ) {
            Log.d("DataManager","Failed to write new GeoData ...");
            writerError.printStackTrace();
        }

        // Loop over registered clients callback ...
        if (Clients.size() !=0) for (EventsGPS Client :Clients) { Client.processLocationChanged(update);}
    }

    @Override
    public void onLoadedPoint(GeoData Loaded) {
        if (Loaded == null) return;
        GeoStorage.store(Loaded);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderDisabled(String provider) { }

    @Override
    public void onProviderEnabled(String provider) { }
}
