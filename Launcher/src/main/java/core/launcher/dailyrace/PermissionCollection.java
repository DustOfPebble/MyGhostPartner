package core.launcher.dailyrace;

import android.Manifest;
import java.util.ArrayList;

public class PermissionCollection {
    private ArrayList<String> Permissions;
    public final String Granted = "Granted";
    int Selected;

    public PermissionCollection() {
        Permissions = new ArrayList<>();
        Permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        Permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Permissions.add(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        Selected = 0;
    }
    public void Next() {
        if (Selected == Permissions.size()) return;
        Selected++;
    }

    public String Selected() {
        if (Selected == Permissions.size()) return null;
        return  Permissions.get(Selected);
    }

    public void setGranted() {
        if (Selected == Permissions.size()) return;
        Permissions.set(Selected,Granted);
    }

    public String[] NotGranted() {
        ArrayList<String> NotGranted = new ArrayList<>();
        for (String Permission: Permissions) {
            if (Permission != Granted) NotGranted.add(Permission);
        }
        return  NotGranted.toArray(new String[0]);
    }
}
