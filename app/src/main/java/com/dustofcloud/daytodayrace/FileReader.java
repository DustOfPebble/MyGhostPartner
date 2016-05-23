package com.dustofcloud.daytodayrace;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class FileReader {
    EventsFileReader Notify = null;

    public FileReader(int DaysBackLimit, EventsFileReader LoaderClient ) {
        Notify = LoaderClient;
        FileInputStream fis = context.openFileInput(REGION_FILENAME);
        ObjectInputStream is = new ObjectInputStream(fis);
        Object readObject = is.readObject();
        is.close();

        if(readObject != null && readObject instanceof Region) {
            return (Region) readObject;
    }


}
