package com.dustofcloud.daytodayrace;

import android.util.JsonWriter;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class FileWriter {
    ObjectOutputStream StreamObjects = null;

    public FileWriter(FileManager FilesHandler) {
        FileOutputStream Stream = FilesHandler.getWriteStream();

        try { StreamObjects = new ObjectOutputStream(Stream); }
        catch ( Exception StreamErrors ) { Log.d("FileReader","Can't create Object stream for writing ..."); }
    }

    public void writeWaypoint(WayPoint WaypointToWrite) {
        if (StreamObjects == null) return;
        try { StreamObjects.writeObject(WaypointToWrite); }
        catch ( Exception WriteErrors ) {
            WriteErrors.printStackTrace();
            Log.d("FileWriter","Can't write Object  ...");
        }
    }
}

