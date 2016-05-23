package com.dustofcloud.daytodayrace;

public class FileReader {
    EventsFileReader Notify = null;

    public FileReader(int DaysBackLimit, EventsFileReader LoaderClient ) {
        Notify = LoaderClient;

    }


}
