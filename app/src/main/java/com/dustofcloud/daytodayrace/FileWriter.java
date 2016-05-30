package com.dustofcloud.daytodayrace;

import android.util.JsonWriter;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileWriter {
    FileManager FilesHandler = null;
    private ArrayList<GeoData> geoDataBuffer = null;
    private int geoDataCount = 60; // Number of stored data between storage (every minute)
    static FileOutputStream Stream = null;
    static BufferedWriter Storage =null;

    public FileWriter(FileManager FilesHandler) throws IOException{
        this.FilesHandler = FilesHandler;
        geoDataBuffer = new ArrayList();
    }

    public void writeGeoData(GeoData geoInfo) throws IOException {
        geoDataBuffer.add(geoInfo);
        if (geoDataBuffer.size() < geoDataCount) return;
        flushBuffer();
    }

    public void flushBuffer() throws IOException {
        Stream = FilesHandler.getWriteStream();
        Log.d("FileWriter","Writing "+geoDataBuffer.size()+" GeoData elements of buffer." );
        Storage = new  BufferedWriter(new OutputStreamWriter(Stream, "UTF-8"));
        for (GeoData geoInfo : geoDataBuffer) {
            Storage.write(geoInfo.toJSON());
            Storage.newLine();
        }
        Storage.flush();
        Storage.close();
        geoDataBuffer.clear();
    }

}

