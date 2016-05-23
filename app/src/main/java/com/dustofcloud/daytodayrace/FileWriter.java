package com.dustofcloud.daytodayrace;

import android.content.Context;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class FileWriter {
    public FileWriter() {
    }

    public void WriteWaypoint(WayPoint WaypointtoWrite) {
        FileOutputStream fos = context.openFileOutput(REGION_FILENAME, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(region);
        oos.close();
    }
}

