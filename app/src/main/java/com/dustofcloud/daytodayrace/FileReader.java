package com.dustofcloud.daytodayrace;

import android.util.Log;

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

        boolean ContinueLoopStream = true;
        while (ContinueLoopStream) {
                FileInputStream Stream = FilesHandler.getNextStream();
                if (Stream == null) { ContinueLoopStream = false; break;} // All streams have been processed

                ObjectInputStream StreamObjects = null;
                try { StreamObjects = new ObjectInputStream(Stream); }
                catch ( Exception ObjectInput ) { Log.d("FileReader","Can't create Object stream ..."); }

                if (StreamObjects == null) break; // Go to next stream

                boolean ContinueLoopObjects = true;
                while(ContinueLoopObjects) {
                    Object StoredItem = null;
                    try { StoredItem = StreamObjects.readObject();}
                    catch (Exception ObjectRead) {Log.d("FileReader", "Can't read Object ...");}

                    if (StoredItem == null) { ContinueLoopObjects = false; break; } // Go to Next Stream
                    if (!(StoredItem instanceof GeoData)) {ContinueLoopObjects = false; break; } // Go to Next Stream

                    NotifyClient.onLoadedPoint((GeoData) StoredItem);
                }

                try {
                    StreamObjects.close();
                    Stream.close();
                }
                catch (Exception CloseStream) {Log.d("FileReader", "Failed on Stream close ...");}

        }
    }
}
