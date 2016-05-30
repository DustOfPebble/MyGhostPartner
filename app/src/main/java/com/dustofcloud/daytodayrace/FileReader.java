package com.dustofcloud.daytodayrace;

import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

public class FileReader extends Thread implements Runnable {
    EventsFileReader NotifyClient = null;
    FileManager FilesHandler= null;

    public FileReader(FileManager FilesHandler, EventsFileReader Suscriber ) {
        this.NotifyClient = Suscriber;
        this.FilesHandler =  FilesHandler;
    }

    public void startReading() {
        this.start();
    }

    private void ProcessStream(FileInputStream Stream) throws IOException {
        BufferedReader Storage = new BufferedReader(new InputStreamReader(Stream, "UTF-8"));
        int NbGeoData = 0;
        String GeoString = Storage.readLine();
        GeoData geoInfo;

          while (GeoString != null) {
            geoInfo = new GeoData();
            if (geoInfo.fromJSON(GeoString)) {
                NotifyClient.onLoadedPoint(geoInfo);
                NbGeoData++;
                GeoString = Storage.readLine();
            }
            //Log.d("FileReader", "Loaded GeoData -> [Long:" + geoInfo.getLongitude() + "°E,Lat:" + geoInfo.getLatitude() + "°N]");
        }
        Log.d("FileReader", NbGeoData +" Blocks Loaded ...");
    }

    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        while (true) {
                FileInputStream Stream = FilesHandler.getNextStream();
                if (Stream == null)  break; // All streams have been processed

                try { ProcessStream(Stream); }
                catch ( Exception ObjectInput ) {
                    Log.d("FileReader","Failed to process input stream ...");
                    ObjectInput.printStackTrace();
                }
        }
    }
}
