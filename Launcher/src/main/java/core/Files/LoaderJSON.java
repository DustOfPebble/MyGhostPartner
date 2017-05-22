package core.Files;

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
    private SavedObject Source = null;
    private int Count = 0;
    private int State;


    static private BufferedReader ReaderOf(File Selected) {
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(Selected), "UTF-8"));
        } catch (Exception FileError) {
            Log.d(LogTag, "Failed to create reading stream from " + Selected.getName());
            return null;
        }
    }

    private static String Head(BufferedReader Reader) {
        String Line = null;
        try { Line = Reader.readLine(); }
        catch (Exception TimeStampsError) { Log.d(LogTag, "Empty file ==> No headers found"); }
        return Line;
    }

    public LoaderJSON(SavedObject Source, LoaderEvents Listener) {
        this.Listener = Listener;
        this.Source = Source;
        State = Loader.finished;
        BufferedReader Reader = ReaderOf(Source.Access);
        if (Reader == null) return ;

        String HeaderJSON = LoaderJSON.Head(Reader);
        if (HeaderJSON == null)  return ;

        Source.Infos = LibJSON.DescriptorFromJSON(HeaderJSON);
        if (Source.Infos == null) return  ;

        Calendar Creation = Calendar.getInstance();
        Creation.set(Calendar.DAY_OF_MONTH,Source.Infos.Day);
        Creation.set(Calendar.MONTH,Source.Infos.Month-1); // Month is from 0 to 11
        Creation.set(Calendar.YEAR, Source.Infos.Year);

        Source.Infos.NbDays = (int)((Calendar.getInstance().getTimeInMillis() - Creation.getTimeInMillis()) / InDays);
        State = Loader.waiting;
    }

    public int Status() { return State;}

    public void start() {
        Count = 0;
        if (State == Loader.finished) {
            Listener.finished(false);
            return;
        }
        State = Loader.running;
        this.run();
    }

    @Override
    public void run() {
        // Run in Background priority mode
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        BufferedReader Reader = ReaderOf(Source.Access);
        if (Reader == null) return;

        Head(Reader);

        Source.Infos.NbNodes = 0;
        String StringJSON;
        try {
            while ((StringJSON = Reader.readLine()) != null) {
                Source.Infos.NbNodes++;
                Sample Values = LibJSON.fromJSON(StringJSON);
                if (Values == null) continue;
                Listener.loaded(Values);
            }
        } catch (Exception FileError) {
            Log.d(LogTag, "Error while loading :" + Source.Access.getName());
            Listener.finished(false);
        }
        Listener.finished(true);
        State = Loader.finished;
    }
}