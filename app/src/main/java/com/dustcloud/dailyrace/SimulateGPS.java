package com.dustcloud.dailyrace;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SimulateGPS implements Runnable {
    private ArrayList<SurveyLoader> RecordsCollection;
    ArrayList<File> FilesCollection = null;

    private int Index=0;
    private int FileIndex=0;
    private DataManager Notify;
    private Handler EventTrigger = new Handler();
    private Runnable task = new Runnable() { public void run() { simulate();} };
    private int EventsDelay = 1000;
    private Thread Loading =null;
    private FileInputStream ReadStream = null;
    private FileManager SourcesManager;

    public SimulateGPS(DataManager Manager, FileManager SourceGPS)
    {
        SourcesManager = SourceGPS;
        Notify = Manager;
        RecordsCollection = new ArrayList<SurveyLoader>();

        // Check access to Directory storage
        File Directory = SourcesManager.getDirectory();
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

        try { ReadStream = new FileInputStream(FilesCollection.get(FileIndex)); }
            catch (Exception StreamError) {
                FileIndex++;
                if (FileIndex == FilesCollection.size()) FileIndex = 0;
                return "";
            }

        // Check if we have a previously loading thread still running
        if (Loading != null) Loading.interrupt();
        Loading = new Thread(this);
        Loading.start();

        String SelectedFile =FilesCollection.get(FileIndex).getName();

        FileIndex++;
        if (FileIndex == FilesCollection.size()) FileIndex = 0;
        return SelectedFile;
    }

    public void simulate() {
//        Log.d("SimulateGPS", "Simulating new GPS position ...");
        EventTrigger.postDelayed(task, EventsDelay);
        if (RecordsCollection.size() == 0) return;
        if (Index >= RecordsCollection.size()) Index=0;
        Notify.processLocationChanged(RecordsCollection.get(Index));
        Index++;
    }

    public void stop()
    {
        Log.d("SimulateGPS", "Stopping GPS simulation ...");
        EventTrigger.removeCallbacks(task);
    }

    // Loading file asynchronous process ...
    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        Log.d("SimulateGPS", "Using "+FilesCollection.get(FileIndex).getName()+" as simulation");
        BufferedReader Storage;
        try { Storage = new BufferedReader(new InputStreamReader(ReadStream, "UTF-8"));}
        catch (Exception FileError) {
            Log.d("SimulateGPS", "Failed to Open data stream...");
            return ;
        }

        Index = 0;

        String TimeString;
        int NbDays = 0;
        try {
            TimeString = Storage.readLine();
            TimeStamps ElapsedDays = new TimeStamps();
            NbDays = ElapsedDays.getDaysAgoFromJSON(TimeString);
        } catch (Exception TimeStampsError) { Log.d("SimulateGPS", "TimeStamps is missing...");}

        int NbGeoData = 0;
        String GeoString;
        SurveyLoader geoInfo;
        try {
            while (true) {
                GeoString = Storage.readLine();
                geoInfo = new SurveyLoader();
                if (!geoInfo.fromJSON(GeoString)) break;
                geoInfo.setElapsedDays(NbDays);
                geoInfo.setSimulated();
                NbGeoData++;
                RecordsCollection.add(geoInfo);
            }
        }
        catch(Exception FileError) {}
        Log.d("SimulateGPS", NbGeoData +" Records loaded ...");
    }
}
