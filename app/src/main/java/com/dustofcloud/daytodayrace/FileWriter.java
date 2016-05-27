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
    private int geoDataCount = 60; // Number of stored data between storage (every minute)

    public FileWriter(FileManager FilesHandler) {
        this.FilesHandler = FilesHandler;
        geoDataBuffer = new ArrayList();
    }

    public void writeGeoData(GeoData geoInfo) throws IOException {
        geoDataBuffer.add(geoInfo);
        if (geoDataBuffer.size() < geoDataCount) return;
        flushBuffer();
    }

    public void flushBuffer() throws IOException {
        Log.d("FileWriter","Writing "+geoDataBuffer.size()+" GeoData elements of buffer." );
        FileOutputStream Stream = FilesHandler.getWriteStream();
        if (Stream == null) return;
        JsonWriter Writer = new JsonWriter(new OutputStreamWriter(Stream, "UTF-8"));
        Writer.setIndent("    ");
        for (GeoData geoInfo : geoDataBuffer) geoInfo.toJSON(Writer);
        Writer.close();
        geoDataBuffer.clear();
    }




}

