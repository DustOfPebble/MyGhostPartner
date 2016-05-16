package com.dustofcloud.daytodayrace;

import android.app.Application;

/**
 * Created by Xavier JAFFREZIC on 16/05/2016.
 */
public class DatasManager extends Application implements CallbackGPS{
    final int StorageArea = 9600; // Storage Area is 9,6 km in both direction (Power of 2 x 100)
    QuadTree WayPoints = null;
    final double earthRadius = 6400000; // Earth Radius is 6400 kms
    double originLongitude = 0f;
    double originLatitude = 0f;

    @Override
    public void onCreate()
    {
        super.onCreate();

        // Create Datas WayPoints QuadTree storage
        WayPoints = new QuadTree(StorageArea);

        // Start GPS engine
        GPS positions = new GPS(this, this);

        // Load previous files when origin is set.... (low priority thread)
    }

    public float dX(double longitude) {
        return 0f;
    }

    public float dY(double latitude) {
        return 0f;
    }

    @Override
    public void updatedPosition(double longitude, double latitude, float elevation) {
        WayPoints.storeWayPoint(new WayPoint(longitude,latitude,elevation));
    }
}
