package com.dustofcloud.daytodayrace;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

public class GPS extends IntentService implements LocationListener {

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 seconde

    private EventsGPS NotifyClient;
    // Declaring a Location Manager
    protected LocationManager sourcesGPS;

    @Override
    protected void onHandleIntent(Intent intent) {
        // Use this object as handler on location update
        sourcesGPS.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                this);
    }

    public GPS(Context context, EventsGPS Client) {
        super("GPS-Provider");
        sourcesGPS = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        this.NotifyClient = Client;
    }

    @Override
    public void onLocationChanged(Location update) {
        if (update == null) return;
        Log.d("[Debug]", "(" + update.getLongitude() + "°N," + update.getLatitude() + "°E)");
        this.NotifyClient.updatedPosition(update);
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
}