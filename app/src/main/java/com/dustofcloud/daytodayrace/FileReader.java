package com.dustofcloud.daytodayrace;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class FileReader implements Runnable {
    EventsFileReader NotifyClient = null;
    FileManager FilesHandler= null;

    public FileReader(FileManager FilesHandler, EventsFileReader Suscriber ) {
        this.NotifyClient = Suscriber;
        this.FilesHandler =  FilesHandler;
    }

    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        String Filename = FilesHandler.getTodayFile();
        FileInputStream fis = context.openFileInput(Filename);
        ObjectInputStream is = new ObjectInputStream(fis);


        Object readObject = is.readObject();
        is.close();

        if (readObject != null && readObject instanceof WayPoint) {
            NotifyClient.WaypointLoaded((WayPoint) readObject);

        }
    }
}
