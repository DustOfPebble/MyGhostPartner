package core.Files;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
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
        Log.d(LogTag, "Selecting workspace {"+ WorkingDirectory.getAbsolutePath()+"}" );
    }

    public ArrayList<SavedObject> CollectFiles(String Extension) {
        File Files[] =  WorkingDirectory.listFiles();
        ArrayList<SavedObject> Collection = new ArrayList();
        for (File Item : Files ) {
            if (!Item.getPath().endsWith(Extension)) continue;
            if (!Item.canRead()) continue;
            Log.d(LogTag, "Found file [" + Item.getPath()+"]" );
            Collection.add(new SavedObject(Item));
        }
        return Collection;
    }

    public File CreateFile(String Filename) {
        return new File(WorkingDirectory.getPath(),Filename);
    }

    static public String NameOf(Calendar Date,String Extension) {
        int Day = Date.get(Calendar.DAY_OF_MONTH);
        int Month = Date.get(Calendar.MONTH)+1; // Month is from 0 to 11
        int Year = Date.get(Calendar.YEAR);
        int Hour = Date.get(Calendar.HOUR_OF_DAY);
        int Minute = Date.get(Calendar.MINUTE);

        return (String.valueOf(Year) +
                String.format("%2s", String.valueOf(Month)).replace(' ', '0') +
                String.format("%2s", String.valueOf(Day)).replace(' ', '0') +
                "-"+
                String.format("%2s", String.valueOf(Hour)).replace(' ', '0') +
                String.format("%2s", String.valueOf(Minute)).replace(' ', '0') +
                Extension);
    }

}
