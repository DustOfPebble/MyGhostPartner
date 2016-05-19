package com.dustofcloud.daytodayrace;

import android.app.Application;
import android.graphics.PointF;
import android.graphics.RectF;
import android.location.Location;

/**
 * Created by Xavier JAFFREZIC on 16/05/2016.
 */
public class DataManager extends Application implements CallbackGPS{
    QuadTree WayPoints = null;
    static final float earthRadius = 6400000f; // Earth Radius is 6400 kms
    static float earthRadiusCorrected = earthRadius; // Value at Equator to Zero at Pole
    static double originLongitude = 0f;
    static double originLatitude = 0f;
    boolean originSet=false;

    GPS positionEmitter;

    @Override
    public void onCreate()
    {
        super.onCreate();
        // Start GPS engine
        positionEmitter = new GPS(this, this);

        // Load previous files when origin is set.... (low priority thread)
    }

    static public float dX(double longitude) {
        return earthRadiusCorrected * (float) Math.toRadians(longitude-originLongitude);
    }

    static public float dY(double latitude) {
        return  earthRadius * (float) Math.toRadians(latitude-originLatitude);
    }

    @Override
    public void updatedPosition(Location updatedPosition) {
        if (updatedPosition == null) return;
        if ( !originSet ) {
            originLatitude = updatedPosition.getLatitude();
            originLongitude = updatedPosition.getLongitude();
            earthRadiusCorrected = earthRadius *(float)Math.cos( Math.toRadians(originLatitude));

            // Create QuadTree storage Area of 9,6 km in both direction (Power of 2 x 100)for all waypoints
            WayPoints = new QuadTree(new RectF(4800f,4800f,-4800f,-4800f));
        }
        WayPoints.storeWayPoint(new WayPoint(updatedPosition));
    }
}
