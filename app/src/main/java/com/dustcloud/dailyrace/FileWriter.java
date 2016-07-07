package com.dustcloud.dailyrace;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
//ToDo: check about number of record on exit if we need to write file (Avoid empty files).
public class FileWriter {
    FileManager FilesHandler = null;
    private ArrayList<SurveyLoader> SurveyBuffer = null;
    private int WriteLoopWait = 5 * 60000; // Write every 5 minutes
    static FileOutputStream Stream = null;
    static BufferedWriter Storage =null;
    private boolean isHeaderWritten;

    private Handler trigger = new Handler();
    private Runnable task = new Runnable() { public void run() { triggeredWrite();} };


    public FileWriter(FileManager FilesHandler) throws IOException{
        this.FilesHandler = FilesHandler;
        SurveyBuffer = new ArrayList();
        isHeaderWritten = false;
        trigger.postDelayed(task, WriteLoopWait);
        Log.d("FileWriter", "Initializing next write in "+WriteLoopWait/1000+"s");
    }

    public void writeSurvey(SurveyLoader geoInfo) { SurveyBuffer.add(geoInfo); }

    public void shutdown() {
        triggeredWrite();
        trigger.removeCallbacks(task);
    }

    public void triggeredWrite() {
        if (SurveyBuffer.size() == 0) return;
        try { flushBuffer(); }
        catch (Exception BufferWriteFailed) {Log.d("FileWriter", "Failed to write GPS datas");}
        trigger.postDelayed(task, WriteLoopWait);
        Log.d("FileWriter", "Triggering next write in "+WriteLoopWait/1000+"s");
    }

    public void flushBuffer() throws IOException {
        Stream = FilesHandler.getWriteStream();
        Log.d("FileWriter","Writing "+ SurveyBuffer.size()+" SurveyLoader elements of buffer." );
        Storage = new  BufferedWriter(new OutputStreamWriter(Stream, "UTF-8"));

        if (!isHeaderWritten) {
            TimeStamps Now = new TimeStamps();
            Storage.write(Now.getNowToJSON());
            Storage.newLine();
            isHeaderWritten =true;
        }

        Converter Transform = new Converter();
        for (SurveyLoader geoInfo : SurveyBuffer) {
            Storage.write(Transform.toJSON(geoInfo));
            Storage.newLine();
        }
        Storage.flush();
        Storage.close();
        SurveyBuffer.clear();
    }
}

