package com.dustofcloud.daytodayrace;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SimulateGPS {
    private ArrayList<GeoData> Collection;
    private int Index=0;
    private DataManager Notify;
    private FileManager FileHandler;
    private Handler trigger = new Handler();
    private Runnable task = new Runnable() { public void run() { sendGPS();} };
    private int EventsDelay = 1000;

    public SimulateGPS(DataManager Parent) {
        Notify = Parent;
    }

    public Boolean load(String ReplayedFile, int Delay)  {
        FileHandler= new FileManager(Notify);
        EventsDelay = Delay;
        Collection = new ArrayList<GeoData>();

        FileInputStream Stream = FileHandler.getStream(ReplayedFile);
        if (Stream == null)
        {
            Log.d("SimulateGPS", "Stream is not defined...");
            return false; // File does not exists !
        }
        Log.d("SimulateGPS", "Using "+ReplayedFile+" as simulation");
        BufferedReader Storage;
        try { Storage = new BufferedReader(new InputStreamReader(Stream, "UTF-8"));}
        catch (Exception FileError) {
            Log.d("SimulateGPS", "Failed to Open data stream...");
            return false;
        }

        String TimeString;
        int NbDays = 0;
        try {
            TimeString = Storage.readLine();
            TimeStamps ElapsedDays = new TimeStamps();
            NbDays = ElapsedDays.getDaysAgoFromJSON(TimeString);
        } catch (Exception TimeStampsError) { Log.d("SimulateGPS", "TimeStamps is missing...");}

        int NbGeoData = 0;
        String GeoString;
        GeoData geoInfo;
        try {
            while (true) {
                GeoString = Storage.readLine();
//                Log.d("SimulateGPS", "JSON Block="+GeoString );
                geoInfo = new GeoData();
                if (!geoInfo.fromJSON(GeoString)) break;
                geoInfo.setElapsedDays(NbDays);
                geoInfo.setSimulated();
                NbGeoData++;
                Collection.add(geoInfo);
            }
        }
        catch(Exception FileError) {}
        Log.d("SimulateGPS", NbGeoData +" Records loaded ...");
        return true;
    }

    public void sendGPS() {
        if (Index == Collection.size()) Index=0;
        Notify.processLocationChanged(Collection.get(Index));
        trigger.postDelayed(task, EventsDelay);
        Index++;
    }

}
