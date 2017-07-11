package services.Recorder;

import android.util.Log;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

import core.Files.Descriptor;
import core.Files.FilesUtils;
import core.Files.LibJSON;
import core.Files.PreSets;
import core.Structures.Sample;
import core.launcher.partner.R;

public class LogsWriter {
    private static String LogTag = AccessLogs.class.getSimpleName();

    private ArrayList<Sample> LogEvents = null;
    private String Header = null;
    private String TimeStampedName = null;
    Descriptor Now = new Descriptor();

    LogsWriter(Calendar Date, String CustomName) {
        LogEvents = new ArrayList();
        Now.Day = Date.get(Calendar.DAY_OF_MONTH);
        Now.Month = Date.get(Calendar.MONTH)+1; // Month is from 0 to 11
        Now.Year = Date.get(Calendar.YEAR);
        Now.Name = CustomName;
        Header = LibJSON.DescriptorToJSON(Now);
        TimeStampedName = FilesUtils.NameOf(Date, PreSets.Signature);
        Now.NbDays = 0;
        Log.d(LogTag, "Creating Log {"+TimeStampedName+"}");
    }

    static private BufferedWriter WriterOf(File Selected) {
        try { return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Selected), "UTF-8")); }
        catch (Exception FileError) {
            Log.d(LogTag, "Failed to create stream from " + Selected.getName());
            return null;
        }
    }

    void append(Sample Live) { LogEvents.add(Live); }

    void flush(FilesUtils WorkSpace) {
        if (LogEvents.size() == 0) return;
        BufferedWriter LogWriter = WriterOf(WorkSpace.CreateFile(TimeStampedName));
        if (LogWriter == null) return;

        Log.d(LogTag, "Writing " + LogEvents.size() + " JSON elements from buffer.");
        try {
            LogWriter.write(Header);
            LogWriter.newLine();
            for (Sample Event : LogEvents) {
                LogWriter.write(LibJSON.toJSON(Event));
                LogWriter.newLine();
                Now.NbNodes++;
            }
            LogWriter.flush();
            LogWriter.close();
            LogEvents.clear();
        } catch (Exception WriteError) { Log.d(LogTag, "Written only "+Now.NbNodes+" nodes to file"); }
    }
}
