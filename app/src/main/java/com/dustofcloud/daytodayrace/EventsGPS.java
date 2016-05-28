package com.dustofcloud.daytodayrace;

import android.os.Handler;

public class EventsGPS {

    DataManager Notify;
    Handler trigger = new Handler();
    Runnable task = new Runnable() { public void run() { sendGPS();} };

    public EventsGPS(DataManager Parent) {
        Notify = Parent;
    }

    public void start() {
        trigger.postDelayed(task, 200);
    }

    public void clear() { trigger.removeCallbacks(task); }

    public void sendGPS() {
        GeoData GPS = new GeoData();
        GPS.fakeGPS();
        Notify.processLocationChanged(GPS);
        trigger.postDelayed(task, 1000);
    }

}
