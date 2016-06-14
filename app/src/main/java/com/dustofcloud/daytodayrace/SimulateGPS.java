package com.dustofcloud.daytodayrace;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SimulateGPS {
    private ArrayList<GeoData> RecordsCollection;
    ArrayList<File> FilesCollection = null;

    private int Index=0;
    private int FileIndex=0;
    private DataManager Notify;
    private Handler trigger = new Handler();
    private Runnable task = new Runnable() { public void run() { sendGPS();} };
    private int EventsDelay = 1000;

    public SimulateGPS(DataManager Parent)
    {
        Notify = Parent;
        RecordsCollection = new ArrayList<GeoData>();

        // Check access to Directory storage
        File Directory = Parent.getFilesDir();
        File Files[] =  Directory.listFiles();
        FilesCollection = new ArrayList<File>();
        for (File Item : Files ) {
            if (!Item.getPath().endsWith(SharedConstants.FilesSignature)) continue;
            if (!Item.canRead()) continue;
            FilesCollection.add(Item);
        }
    }

    public String load(int Delay)  {
        if (FilesCollection.size() == 0) return "";
        EventsDelay = Delay;
        RecordsCollection.clear();

        FileInputStream ReadStream = null;
        try { ReadStream = new FileInputStream(FilesCollection.get(FileIndex)); }
            catch (Exception StreamError) {
                FileIndex++;
                if (FileIndex == FilesCollection.size()) FileIndex = 0;
                return "";
            }

        Log.d("SimulateGPS", "Using "+FilesCollection.get(FileIndex).getName()+" as simulation");
        BufferedReader Storage;
        try { Storage = new BufferedReader(new InputStreamReader(ReadStream, "UTF-8"));}
        catch (Exception FileError) {
            Log.d("SimulateGPS", "Failed to Open data stream...");
            return "";
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
                geoInfo = new GeoData();
                if (!geoInfo.fromJSON(GeoString)) break;
                geoInfo.setElapsedDays(NbDays);
                geoInfo.setSimulated();
                NbGeoData++;
                RecordsCollection.add(geoInfo);
            }
        }
        catch(Exception FileError) {}
        Log.d("SimulateGPS", NbGeoData +" Records loaded ...");
        Index = 0;

        String SelectedFile =FilesCollection.get(FileIndex).getName();

        FileIndex++;
        if (FileIndex == FilesCollection.size()) FileIndex = 0;
        return SelectedFile;
    }

    public void sendGPS() {
//        Log.d("SimulateGPS", "Simulating new GPS position ...");
        if (Index == RecordsCollection.size()) Index=0;
        Notify.processLocationChanged(RecordsCollection.get(Index));
        trigger.postDelayed(task, EventsDelay);
        Index++;
    }

    public void stop()
    {
        Log.d("SimulateGPS", "Stopping GPS simulation ...");
        trigger.removeCallbacks(task);
    }

}
