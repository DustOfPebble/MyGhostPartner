package com.dustofcloud.daytodayrace;

public class FileReader {
    CallbackEventsFileReader Notify = null;

    public FileReader(int DaysBackLimit, CallbackEventsFileReader LoaderClient ) {
        Notify = LoaderClient;

    }


}
