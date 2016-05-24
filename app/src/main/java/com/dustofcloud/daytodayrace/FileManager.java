package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class FileManager {
    private final String Signature = ".DailyDB";
    ArrayList<File> Collection = null;
    private File TodayDB= null;
    int LastFile = -1;

    public FileManager(Context context) {
        // Check access to Directory storage
        File Directory = context.getDir("Databases", Context.MODE_PRIVATE);
        if (!Directory.exists()) Directory.mkdir();

        // Calculate today database file
        Calendar Today = Calendar.getInstance();
        int Day = Today.get(Calendar.DAY_OF_MONTH);
        int Month = Today.get(Calendar.MONTH);
        int Year = Today.get(Calendar.YEAR);

        String TodayFilename = String.valueOf(Year) + "-" +
                               String.valueOf(Month) + "-" +
                               String.valueOf(Day) +
                               Signature;

        // Collect all database from storage directory
        File Files[] =  Directory.listFiles();
        Collection = new ArrayList<>();
        for (File Item : Files ) {
            Log.d("FileManage", "DailyDB file => " + Item.getPath() );
            if (!Item.getPath().endsWith(Signature)) continue;
            if (!Item.canRead()) continue;
            if (Item.getPath().endsWith(TodayFilename)) TodayDB = Item;
            Collection.add(Item);
        }

        // Create Daily database if not exist
        if (TodayDB == null) {
            TodayDB = new File(Directory.getPath(),TodayFilename);
            try { TodayDB.createNewFile();}
            catch (Exception FileErrors) { Log.d("FileManager","Can't create " + TodayDB.getPath()); }
        }
    }

    public FileOutputStream getWriteStream() {
        FileOutputStream WriteStream = null;
        try { WriteStream = new FileOutputStream(TodayDB, true); }
        catch (Exception StreamError) { Log.d("FileManager","Can't open stream "+TodayDB.getPath()+" for writing..."); }
        return WriteStream;
    }

    public FileInputStream getNextStream() {
        FileInputStream ReadStream = null;
        if (LastFile == Collection.size()) return ReadStream;
        LastFile++;
        try { ReadStream = new FileInputStream(Collection.get(LastFile)); }
        catch (Exception StreamError) { Log.d("FileManager","Can't open stream "+TodayDB.getPath()+" for reading..."); }
        return ReadStream;
    }

}
