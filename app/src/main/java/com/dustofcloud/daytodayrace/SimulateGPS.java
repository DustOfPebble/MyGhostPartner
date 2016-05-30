package com.dustofcloud.daytodayrace;

import android.os.Handler;

public class SimulateGPS {

    private DataManager Notify;
    private Handler trigger = new Handler();
    private Runnable task = new Runnable() { public void run() { sendGPS();} };
    private int EventsDelay = 1000;

    public SimulateGPS(DataManager Parent) {
        Notify = Parent;
    }

    public void start() {
        sendGPS();
    }

    private void sendGPS() {
        GeoData GPS = new GeoData();
        GPS.fakeGPS();
        Notify.processLocationChanged(GPS);
        trigger.postDelayed(task, EventsDelay);
    }

}
