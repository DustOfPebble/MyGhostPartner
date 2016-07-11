package com.dustcloud.dailyrace;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileReader implements Runnable {
    DataManager Notify = null;
    FileManager FilesHandler= null;
    SurveyLoader Survey;

    public FileReader(FileManager handler, DataManager Client, SurveyLoader FilesSurvey) {
       Notify = Client;
       FilesHandler =  handler;
       Survey = FilesSurvey;
    }

    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        while (true) {
            FileInputStream Stream = FilesHandler.getNextStream();
            if (Stream == null)  break; // All streams have been processed
            ProcessStream(Stream);
        }
    }

    private void ProcessStream(FileInputStream Stream) {
        BufferedReader Storage;
        try{ Storage = new BufferedReader(new InputStreamReader(Stream, "UTF-8"));}
        catch (Exception StreamError) { return; }

        String TimeString;
        try{ TimeString = Storage.readLine(); }
        catch (Exception ReadError) {return;}
        TimeStamps ElapsedDays = new TimeStamps();
        int NbDays = ElapsedDays.getDaysAgoFromJSON(TimeString);
        if (NbDays==-1) return;

        Survey.setDays(NbDays);

        int NbSamples = 0;
        String StringJSON ="Loading";
        try {
            while (!StringJSON.isEmpty()) {
                StringJSON = Storage.readLine();
                Survey.fromJSON(StringJSON);
                Notify.onSnapshotLoaded(Survey.getSnapshot());
                NbSamples++;
            }
        }
        catch (Exception EOF) { Log.d("FileReader", NbSamples +" Blocks Loaded ...");}
    }
}
