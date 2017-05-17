package core.Files;

import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import core.Structures.Sample;


public class LoaderJSON  extends Loader implements Runnable {

    static private String LogTag = LoaderJSON.class.getSimpleName();
    static final long InDays = 24 * 60 * 60 * 1000;

    private LoaderEvents Listener = null;
    private File Source = null;
    private int Count = 0;
    private int State;


    static private BufferedReader ReaderOf(File Selected) {
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(Selected), "UTF-8"));
        } catch (Exception FileError) {
            Log.d(LogTag, "Failed to create stream from " + Selected.getName());
            return null;
        }
    }

    public LoaderJSON(File Source, LoaderEvents Listener) {
        this.Listener = Listener;
        this.Source = Source;
        State = Loader.waiting;
    }

    public int Status() { return State;}

    public void start() {
        Count = 0;
        State = Loader.running;
        this.run();
    }

    public int Count() { return Count;}

    public Bundle header() {
        BufferedReader Reader = ReaderOf(Source);
        if (Reader == null) return null;

        String HeaderJSON = null;
        try { HeaderJSON = Reader.readLine(); }
        catch (Exception TimeStampsError) { Log.d(LogTag, "Empty file ==> No headers found"); }

        Calendar FileCreation = LibJSON.DateFromJSON(HeaderJSON);
        long NbDays = -1;
        if (FileCreation != null)
            NbDays = (Calendar.getInstance().getTimeInMillis() - FileCreation.getTimeInMillis()) / InDays;

        Bundle Headers = new Bundle();
        Headers.putInt(FileDefs.Days, (int) NbDays);
        return Headers;
    }

    @Override
    public void run() {
        // Run in Background priority mode
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        BufferedReader Reader = ReaderOf(Source);
        if (Reader == null) return;

        Count = 0;
        String StringJSON;
        try {
            while ((StringJSON = Reader.readLine()) != null) {
                Count++;
                Sample Values = LibJSON.fromStringJSON(StringJSON);
                if (Values == null) continue;
                Listener.loaded(Values);
            }
        } catch (Exception FileError) {
            Log.d(LogTag, "Error while loading :" + Source.getName());
            Listener.finished(false);
        }
        Listener.finished(true);
        State = Loader.finished;
    }
}