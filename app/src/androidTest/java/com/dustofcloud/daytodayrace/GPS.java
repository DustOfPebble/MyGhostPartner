package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.app.Service;
import android.content.Intent;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Xavier JAFFREZIC on 15/05/2016.
 */
public class GPS extends Service implements LocationListener {

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager sourcesGPS;

    public GPS(Context context) {
        sourcesGPS = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        // Use this object as handler on location update
        sourcesGPS.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                this);
    }

    @Override
    public void onLocationChanged(Location UpdatedPosition) {
        if (UpdatedPosition == null) return;
        Log.d("[Debug]", "(" + UpdatedPosition.getLongitude() + "°N," + UpdatedPosition.getLatitude() + "°E)");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}