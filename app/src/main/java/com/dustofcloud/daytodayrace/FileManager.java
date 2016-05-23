package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class FileManager {
    private String TodayFile="";
    private ArrayList<String> Files = null;
    int LastFile = -1;

    public FileManager(Context context) {
        Calendar Today = Calendar.getInstance();
        int Day = Today.get(Calendar.DAY_OF_MONTH);
        int Month = Today.get(Calendar.MONTH);
        int Year = Today.get(Calendar.YEAR);

        TodayFile=  String.valueOf(Year) + "-" +
                    String.valueOf(Month) + "-" +
                    String.valueOf(Day) +
                    ".DailyDB";

        File Directory = context.getDir("Databases", Context.MODE_PRIVATE);
        Directory.listFiles("*.DailyDB",) ;
    }

    public String getWriteStream() {
        return TodayFile;
    }

    public String getNextStream() {
        return TodayFile;
    }

}
