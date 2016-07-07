package com.dustcloud.dailyrace;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileReader implements Runnable {
    DataManager Notify = null;
    FileManager FilesHandler= null;

    public FileReader(FileManager handler, DataManager Client ) {
       Notify = Client;
       FilesHandler =  handler;
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

        Converter Transform = new Converter();
        int NbGeoData = 0;
        String StringJSON = Storage.readLine();
        SurveyLoader Survey;
          while (StringJSON != null) {
            Survey = Transform.fromJSON(StringJSON);
            if (Survey == null) continue;
            Survey.setElapsedDays(NbDays);
            Notify.onLoaded(Survey);
            NbGeoData++;
            //Log.d("FileReader", "Loaded SurveyLoader -> [Long:" + Survey.getLongitude() + "°E,Lat:" + Survey.getLatitude() + "°N]");
            StringJSON = Storage.readLine();
        }
        Log.d("FileReader", NbGeoData +" Blocks Loaded ...");
    }
}
