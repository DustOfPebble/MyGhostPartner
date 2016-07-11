package com.dustcloud.dailyrace;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SimulateGPS implements Runnable {
    private ArrayList<String> CollectionJSON;
    ArrayList<File> FilesCollection = null;

    private int Index;
    private int FileIndex;
    private DataManager Notify;
    private Handler EventTrigger = new Handler();
    private Runnable task = new Runnable() { public void run() { simulate();} };
    private int EventsDelay = 1000;
    private Thread Loading =null;
    private FileInputStream ReadStream = null;
    private FileManager SourcesManager;
    private SurveyLoader Loader;

    public SimulateGPS(DataManager Manager, FileManager SourceGPS, SurveyLoader SimulatedGPS)
    {
        SourcesManager = SourceGPS;
        Notify = Manager;
        Loader = SimulatedGPS;

        CollectionJSON = new ArrayList<String>();

        Index = -1;
        FileIndex = -1;

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

        // Check if we are already loading a file ...
        if (Loading != null) Loading.interrupt();

        EventsDelay = Delay;
        CollectionJSON.clear();

        FileIndex++;
        if (FileIndex == FilesCollection.size()) FileIndex = 0;

        // Try to Open selected stream ...
        try { ReadStream = new FileInputStream(FilesCollection.get(FileIndex)); }
        catch (Exception StreamError) { return ""; }

        // Pre-loading JSON structure to memory  ...
        Loading = new Thread(this);
        Loading.start();

        // Returning selected filename
        String SelectedFile = FilesCollection.get(FileIndex).getName();
        return SelectedFile;
    }

    public void simulate() {
        EventTrigger.postDelayed(task, EventsDelay);
        if (CollectionJSON.size() == 0) return;
        if (Index >= CollectionJSON.size()) Index = 0;
        Loader.fromJSON(CollectionJSON.get(Index));
        Coordinates GPS = Loader.getCoordinates();
        Log.d("SimulateGPS", "Simulating ["+GPS.longitude+"°E,"+GPS.latitude+"°N]");
        Notify.onSimulatedChanged(GPS);
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
        int NbDays = 0;
        String TimeString;
        try {
            TimeString = Storage.readLine();
            TimeStamps ElapsedDays = new TimeStamps();
            NbDays = ElapsedDays.getDaysAgoFromJSON(TimeString);
        } catch (Exception TimeStampsError) { Log.d("SimulateGPS", "TimeStamps is missing...");}
        Loader.setDays(NbDays);

        int NbJSON = 0;
        String StringJSON = "Loading";
        try {
            while (!StringJSON.isEmpty()) {
                StringJSON = Storage.readLine();
                CollectionJSON.add(StringJSON);
                NbJSON++;
            }
        }
        catch(Exception FileError) {}
        Log.d("SimulateGPS", NbJSON +" Records loaded ...");
    }
}
