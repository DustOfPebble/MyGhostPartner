package core.Files;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class LoaderGPX extends Loader implements Runnable{
    static private String LogTag = LoaderGPX.class.getSimpleName();

    private LoaderEvents Listener = null;
    private SavedObject Source = null;
    private int State;

    public LoaderGPX(SavedObject Source, LoaderEvents Listener) {
        this.Listener = Listener;
        this.Source = Source;
        State = Loader.waiting;
    }

    public int Status() { return State;}

    static private BufferedReader ReaderOf(File Selected) {
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(Selected), "UTF-8"));
        } catch (Exception FileError) {
            Log.d(LogTag, "Failed to create stream from " + Selected.getName());
            return null;
        }
    }

    @Override
    public void run() {
        Listener.finished(true);
        State = Loader.finished;
    }
}

