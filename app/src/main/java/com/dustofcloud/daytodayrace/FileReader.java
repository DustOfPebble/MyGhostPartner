package com.dustofcloud.daytodayrace;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class FileReader implements Runnable {
    EventsFileReader Notify = null;

    public FileReader(int DaysBackLimit, EventsFileReader LoaderClient ) {
        Notify = LoaderClient;
        FileInputStream fis = context.openFileInput(REGION_FILENAME);
        ObjectInputStream is = new ObjectInputStream(fis);
    }

    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        Object readObject = is.readObject();
        is.close();

        if (readObject != null && readObject instanceof WayPoint) {
            Notify.WaypointLoaded((WayPoint) readObject);

        }
    }
}
