package com.dustofcloud.daytodayrace;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.location.Location;


public class DataManager extends Application implements CallbackGPS{
    QuadTree WayPoints = null;
    static RectF StorageArea = new RectF(4800f,4800f,-4800f,-4800f); // StorageArea is a rectangle of 9,6 km in both direction (Power of 2 x 100)
    static final float earthRadius = 6400000f; // Earth Radius is 6400 kms
    static float earthRadiusCorrected = earthRadius; // Value at Equator to Zero at Pole
    static double originLongitude = 0f;
    static double originLatitude = 0f;
    boolean originSet=false;

    GPS positionEmitter;

    // Specific to manage Callback to clients
    static Context BackendContext = null;
    static CallbackUpdateView NotifyClients = null;

    public void setUpdateViewCallback (CallbackUpdateView UpdateViewClient){
        NotifyClients = UpdateViewClient;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        BackendContext = this;
        // Create GPS
        positionEmitter = new GPS(this, this);

        // Starting GPS as Service
        Intent StartGPS = new Intent(this, GPS.class);
        positionEmitter.startService(StartGPS);

        // Load previous files when origin is set.... (low priority thread)
    }

    static public float dX(double longitude) {
        return earthRadiusCorrected * (float) Math.toRadians(longitude-originLongitude);
    }

    static public float dY(double latitude) {
        return  earthRadius * (float) Math.toRadians(latitude-originLatitude);
    }

    // Return Application in order to setup callback from client
    static public Context getBackend(){
        return BackendContext;
    }

    @Override
    public void updatedPosition(Location updatedPosition) {
        if (updatedPosition == null) return;
        if ( !originSet ) {
            originLatitude = updatedPosition.getLatitude();
            originLongitude = updatedPosition.getLongitude();
            earthRadiusCorrected = earthRadius *(float)Math.cos( Math.toRadians(originLatitude));

            WayPoints = new QuadTree(StorageArea); // Create QuadTree storage area for all waypoints
        }
        WayPoints.storeWayPoint(new WayPoint(updatedPosition));
    }
}
