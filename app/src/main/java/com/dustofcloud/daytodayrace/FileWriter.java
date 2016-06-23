package com.dustofcloud.daytodayrace;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileWriter {
    FileManager FilesHandler = null;
    private ArrayList<GeoData> geoDataBuffer = null;
    private int WriteLoopWait = 60000; // Write every minute
    static FileOutputStream Stream = null;
    static BufferedWriter Storage =null;
    private boolean isHeaderWritten;

    private Handler trigger = new Handler();
    private Runnable task = new Runnable() { public void run() { triggeredWrite();} };


    public FileWriter(FileManager FilesHandler) throws IOException{
        this.FilesHandler = FilesHandler;
        geoDataBuffer = new ArrayList();
        isHeaderWritten = false;
        trigger.postDelayed(task, WriteLoopWait);
        Log.d("FileWriter", "Initializing next write in "+WriteLoopWait/1000+"s");
    }

    public void writeGeoData(GeoData geoInfo) { geoDataBuffer.add(geoInfo); }

    public void shutdown() {
        triggeredWrite();
        trigger.removeCallbacks(task);
    }

    public void triggeredWrite() {
        if (geoDataBuffer.size() == 0) return;
        try { flushBuffer(); }
        catch (Exception BufferWriteFailed) {Log.d("FileWriter", "Failed to write GPS datas");}
        trigger.postDelayed(task, WriteLoopWait);
        Log.d("FileWriter", "Triggering next write in "+WriteLoopWait/1000+"s");
    }

    public void flushBuffer() throws IOException {
        Stream = FilesHandler.getWriteStream();
        Log.d("FileWriter","Writing "+geoDataBuffer.size()+" GeoData elements of buffer." );
        Storage = new  BufferedWriter(new OutputStreamWriter(Stream, "UTF-8"));

        if (!isHeaderWritten) {
            TimeStamps Now = new TimeStamps();
            Storage.write(Now.getNowToJSON());
            Storage.newLine();
            isHeaderWritten =true;
        }

        for (GeoData geoInfo : geoDataBuffer) {
            Storage.write(geoInfo.toJSON());
            Storage.newLine();
        }
        Storage.flush();
        Storage.close();
        geoDataBuffer.clear();
    }
}

