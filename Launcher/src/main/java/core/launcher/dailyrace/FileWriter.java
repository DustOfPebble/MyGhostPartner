package core.launcher.dailyrace;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
//ToDo: check about number of record on exit if we need to write file (Avoid empty files).
public class FileWriter {
    FileManager FilesHandler = null;
    private ArrayList<String> BufferJSON = null;
    private int WriteLoopWait = 5 * 60000; // Write every 5 minutes
    static FileOutputStream Stream = null;
    static BufferedWriter Storage =null;
    private boolean isHeaderWritten;

    private Handler trigger = new Handler();
    private Runnable task = new Runnable() { public void run() { triggeredWrite();} };

    public FileWriter(FileManager FilesHandler) {
        this.FilesHandler = FilesHandler;
        BufferJSON = new ArrayList();
        isHeaderWritten = false;
        trigger.postDelayed(task, WriteLoopWait);
        Log.d("FileWriter", "Initializing next write in "+WriteLoopWait/1000+"s");
    }

    public void appendJSON(String StringJSON) { BufferJSON.add(StringJSON); }

    public void shutdown() {
        trigger.removeCallbacks(task);
        flushBuffer();
    }

    public void triggeredWrite() {
        flushBuffer();
        trigger.postDelayed(task, WriteLoopWait);
        Log.d("FileWriter", "Triggering next write in "+WriteLoopWait/1000+"s");
    }

    public void flushBuffer() {
        if (BufferJSON.size() == 0) return;
        Stream = FilesHandler.getWriteStream();
        Log.d("FileWriter", "Writing " + BufferJSON.size() + "JSON elements of buffer.");
        try {
            Storage = new BufferedWriter(new OutputStreamWriter(Stream, "UTF-8"));
        } catch (Exception BufferError) {return;}

        try {
            if (!isHeaderWritten) {
                TimeStamps Now = new TimeStamps();
                Storage.write(Now.getNowToJSON());
                Storage.newLine();
                isHeaderWritten = true;
            }
        } catch (Exception HeaderError) {return;}

        try {
            for (String StringJSON : BufferJSON) {
                Storage.write(StringJSON);
                Storage.newLine();
            }
            Storage.flush();
            Storage.close();
            BufferJSON.clear();
        } catch (Exception FlushBufferError) {return;}
    }
}

