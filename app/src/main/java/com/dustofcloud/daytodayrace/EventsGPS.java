package com.dustofcloud.daytodayrace;

import android.os.Handler;

public class EventsGPS {

    private DataManager Notify;
    private Handler trigger = new Handler();
    private Runnable task = new Runnable() { public void run() { sendGPS();} };
    private int EventsDelay = 500;

    public EventsGPS(DataManager Parent) {
        Notify = Parent;
    }

    public void start() {
        trigger.postDelayed(task, 100);
    }

    public void clear() { trigger.removeCallbacks(task); }

    public void sendGPS() {
        GeoData GPS = new GeoData();
        GPS.fakeGPS();
        Notify.processLocationChanged(GPS);
        trigger.postDelayed(task, EventsDelay);
    }

}
