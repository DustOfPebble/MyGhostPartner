package com.dustofcloud.daytodayrace;

import android.util.JsonWriter;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileWriter {
    FileManager FilesHandler = null;
    public FileWriter(FileManager FilesHandler) { this.FilesHandler = FilesHandler; }

    public void writeGeoData(GeoData geoInfo) throws IOException {
        FileOutputStream Stream = FilesHandler.getWriteStream();
        if (Stream == null) return;
        JsonWriter Writer = new JsonWriter(new OutputStreamWriter(Stream, "UTF-8"));
        Writer.setIndent("    ");
        geoInfo.toJSON(Writer);
        Writer.close();
    }
}

