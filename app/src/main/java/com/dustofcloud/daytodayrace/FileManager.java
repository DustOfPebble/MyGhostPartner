package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

public class FileManager {
    ArrayList<File> Collection = null;
    private File InUseDB = null;
    int LastFile = 0;

    public FileManager(Context context) {
        // Check access to Directory storage
        File Directory = context.getFilesDir();

        // Calculate today database file
        Calendar Today = Calendar.getInstance();
        int Day = Today.get(Calendar.DAY_OF_MONTH);
        int Month = Today.get(Calendar.MONTH)+1; // Month is from 0 to 11
        int Year = Today.get(Calendar.YEAR);
        int Hour = Today.get(Calendar.HOUR_OF_DAY);
        int Minute = Today.get(Calendar.MINUTE);

        String NowFilename = String.valueOf(Year) +
                                String.format("%2s", String.valueOf(Month)).replace(' ', '0') +
                                String.format("%2s", String.valueOf(Day)).replace(' ', '0') +
                                "-"+
                                String.format("%2s", String.valueOf(Hour)).replace(' ', '0') +
                                String.format("%2s", String.valueOf(Minute)).replace(' ', '0') +
                                SharedConstants.FilesSignature;

        // Collect all database from storage directory
        File Files[] =  Directory.listFiles();
        Collection = new ArrayList();
        for (File Item : Files ) {
            if (!Item.getPath().endsWith(SharedConstants.FilesSignature)) continue;
            if (!Item.canRead()) continue;
            Log.d("FileManager", "Found DailyDB file => " + Item.getPath() );
            Collection.add(Item);
        }

        // Create InUse database
        InUseDB = new File(Directory.getPath(),NowFilename);
    }

    public FileOutputStream getWriteStream() {
        FileOutputStream WriteStream = null;
        try { WriteStream = new FileOutputStream(InUseDB, true); }
        catch (Exception StreamError) { Log.d("FileManager","Can't open stream "+ InUseDB.getPath()+" for writing..."); }
        return WriteStream;
    }

    public FileInputStream getNextStream() {
        FileInputStream ReadStream = null;
        if (LastFile == Collection.size()) return ReadStream;
        Log.d("FileManager","Selecting data from " + Collection.get(LastFile).getPath());
        try { ReadStream = new FileInputStream(Collection.get(LastFile)); }
        catch (Exception StreamError) {
            Log.d("FileManager","Can't open stream "+ InUseDB.getPath()+" for reading...");
            ReadStream = null;
        }
        LastFile++;
        return ReadStream;
    }

    public FileInputStream getStream(String MatchSearch) {
        FileInputStream ReadStream = null;
        for (File FileDB: Collection ) {
            if (FileDB.getPath().indexOf(MatchSearch) == 0) continue;
            try { ReadStream = new FileInputStream(FileDB); }
            catch (Exception StreamError) {
                Log.d("FileManager","Can't open stream "+ FileDB.getPath()+" for reading...");
                ReadStream = null;
            }
        }
        if (ReadStream == null) Log.d("FileManager","Couldn't find matching file with" + MatchSearch);
        return ReadStream;
    }
}
