package com.dustofcloud.daytodayrace;

import android.util.JsonWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileWriter {
    FileOutputStream Stream = null;

    public FileWriter(FileManager FilesHandler) {
        Stream = FilesHandler.getWriteStream();
    }

    public void writeGeoData(GeoData geoInfo) throws IOException {
        if (Stream == null) return;
        JsonWriter Writer = new JsonWriter(new OutputStreamWriter(Stream, "UTF-8"));
        Writer.setIndent("    ");
        geoInfo.toJSON(Writer);
        Writer.close();
    }
}

