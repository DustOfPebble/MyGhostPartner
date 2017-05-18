package services.Recorder;

import android.util.Log;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

import core.Files.LibJSON;
import core.Structures.Sample;

public class LogsWriter {
    private static String LogTag = AccessLogs.class.getSimpleName();

    private ArrayList<String> LogEvents = null;
    private String Header = null;

    public LogsWriter() {
        LogEvents = new ArrayList();
        Header = LibJSON.DateToJSON(Calendar.getInstance());
    }

    static private BufferedWriter WriterOf(File Selected) {
        try { return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Selected), "UTF-8")); }
        catch (Exception FileError) {
            Log.d(LogTag, "Failed to create stream from " + Selected.getName());
            return null;
        }
    }

    public void append(Sample Live) { LogEvents.add(LibJSON.toStringJSON(Live)); }

    public void write(File Storage) {
        if (LogEvents.size() == 0) return;
        BufferedWriter LogWriter = WriterOf(Storage);
        if (LogWriter == null) return;

        Log.d(LogTag, "Writing " + LogEvents.size() + " JSON elements from buffer.");
        try {
            LogWriter.write(Header);
            LogWriter.newLine();
            for (String Event : LogEvents) {
                LogWriter.write(Event);
                LogWriter.newLine();
            }
            LogWriter.flush();
            LogWriter.close();
            LogEvents.clear();
        } catch (Exception WriteError) { Log.d(LogTag, "Failed while writing file"); }
    }
}

