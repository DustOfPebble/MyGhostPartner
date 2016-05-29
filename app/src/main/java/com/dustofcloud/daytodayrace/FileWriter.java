package com.dustofcloud.daytodayrace;

import android.util.JsonWriter;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileWriter {
    FileManager FilesHandler = null;
    private ArrayList<GeoData> geoDataBuffer = null;
    private int geoDataCount = 10; // Number of stored data between storage (every minute)
    static private JsonWriter Writer = null;
    static FileOutputStream Stream = null;

    public FileWriter(FileManager FilesHandler) throws IOException{
        this.FilesHandler = FilesHandler;
        Stream = FilesHandler.getWriteStream();
        Writer = new JsonWriter(new OutputStreamWriter(Stream, "UTF-8"));
        Writer.beginArray();
        geoDataBuffer = new ArrayList();
    }

    public void writeGeoData(GeoData geoInfo) throws IOException {
        geoDataBuffer.add(geoInfo);
        if (geoDataBuffer.size() < geoDataCount) return;
        flushBuffer();
    }

    public void flushBuffer() throws IOException {
        if (Stream == null) return;
        Log.d("FileWriter","Writing "+geoDataBuffer.size()+" GeoData elements of buffer." );
        for (GeoData geoInfo : geoDataBuffer) geoInfo.toJSON(Writer);
        geoDataBuffer.clear();
    }
    public void finish() throws IOException {
        Writer.endArray();
        Writer.close();
    }




}

