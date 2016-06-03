package com.dustofcloud.daytodayrace;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileReader implements Runnable {
    EventsFileReader NotifyClient = null;
    FileManager FilesHandler= null;

    public FileReader(FileManager FilesHandler, EventsFileReader Suscriber ) {
        this.NotifyClient = Suscriber;
        this.FilesHandler =  FilesHandler;
    }

    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        while (true) {
            FileInputStream Stream = FilesHandler.getNextStream();
            if (Stream == null)  break; // All streams have been processed

            try { ProcessStream(Stream); }
            catch ( Exception ObjectInput ) { Log.d("FileReader","Failed to process input stream ..."); }
        }
    }

    private void ProcessStream(FileInputStream Stream) throws IOException {
        BufferedReader Storage = new BufferedReader(new InputStreamReader(Stream, "UTF-8"));
        String TimeString = Storage.readLine();
        TimeStamps ElapsedDays = new TimeStamps();
        int NbDays = ElapsedDays.getDaysAgoFromJSON(TimeString);
        if (NbDays==-1) return; // We do no process the file ...

        int NbGeoData = 0;
        String GeoString = Storage.readLine();
        GeoData geoInfo;
          while (GeoString != null) {
            geoInfo = new GeoData();
            if (geoInfo.fromJSON(GeoString)) {
                geoInfo.setElapsedDays(NbDays);
                NotifyClient.onLoadedPoint(geoInfo);
                NbGeoData++;
            }
            //Log.d("FileReader", "Loaded GeoData -> [Long:" + geoInfo.getLongitude() + "°E,Lat:" + geoInfo.getLatitude() + "°N]");
            GeoString = Storage.readLine();
        }
        Log.d("FileReader", NbGeoData +" Blocks Loaded ...");
    }
}
