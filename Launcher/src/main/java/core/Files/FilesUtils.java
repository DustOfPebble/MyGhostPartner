package core.Files;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class FilesUtils {
    private static final String LogTag = FilesUtils.class.getSimpleName();

    private File WorkingDirectory;
    private Context Owner;

    public FilesUtils(Context context) {
        Owner = context;
    }

    public void CheckDirectory(String SelectedDirectory) {
        boolean ExternalAccess = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());

        if (ExternalAccess) {
            WorkingDirectory = Environment.getExternalStoragePublicDirectory(SelectedDirectory);
            WorkingDirectory.mkdir();
        }
        else WorkingDirectory = Owner.getFilesDir();
        Log.d("FileManager", "Selecting workspace : "+ WorkingDirectory.getAbsolutePath() );
    }

    public ArrayList<File> CollectFiles(String Extension) {
        File Files[] =  WorkingDirectory.listFiles();
        ArrayList<File> Collection = new ArrayList();
        for (File Item : Files ) {
            if (!Item.getPath().endsWith(Extension)) continue;
            if (!Item.canRead()) continue;
            Log.d("FileManager", "Found file :" + Item.getPath() );
            Collection.add(Item);
        }
        return Collection;
    }

    public File CreateFile(String Filename) {
        return new File(WorkingDirectory.getPath(),Filename);
    }

    public FileOutputStream WriteStream(File Sink) {
        FileOutputStream WriteStream;
        try { WriteStream = new FileOutputStream(Sink, true); }
        catch (Exception StreamError) {
            Log.d(LogTag,"Can't open stream "+ Sink.getPath()+" for writing...");
            WriteStream = null;
        }
        return WriteStream;
    }

    static public String Now(String Extension) {
        Calendar Today = Calendar.getInstance();
        int Day = Today.get(Calendar.DAY_OF_MONTH);
        int Month = Today.get(Calendar.MONTH)+1; // Month is from 0 to 11
        int Year = Today.get(Calendar.YEAR);
        int Hour = Today.get(Calendar.HOUR_OF_DAY);
        int Minute = Today.get(Calendar.MINUTE);

        return (String.valueOf(Year) +
                String.format("%2s", String.valueOf(Month)).replace(' ', '0') +
                String.format("%2s", String.valueOf(Day)).replace(' ', '0') +
                "-"+
                String.format("%2s", String.valueOf(Hour)).replace(' ', '0') +
                String.format("%2s", String.valueOf(Minute)).replace(' ', '0') +
                Extension);
    }

}
