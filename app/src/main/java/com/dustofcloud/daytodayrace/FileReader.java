package com.dustofcloud.daytodayrace;

import android.util.JsonReader;
import android.util.Log;

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
        JsonReader Reader = new JsonReader(new InputStreamReader(Stream, "UTF-8"));
        GeoData geoInfo = new GeoData();
        geoInfo.fromJSON(Reader);
        while(geoInfo.isLoaded) {
            NotifyClient.onLoadedPoint(geoInfo);
            geoInfo = new GeoData();
            geoInfo.fromJSON(Reader);
        }
    }

    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        boolean ContinueLoopStream = true;
        while (ContinueLoopStream) {
                FileInputStream Stream = FilesHandler.getNextStream();
                if (Stream == null) { ContinueLoopStream = false; break;} // All streams have been processed

                try { ProcessStream(Stream); }
                catch ( Exception ObjectInput ) { Log.d("FileReader","Failed to process input stream ..."); }

        }
    }
}
