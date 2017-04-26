package core.launcher.helpers;

import java.util.ArrayList;

public class PermissionLoader {
    private ArrayList<String> Permissions;
    public final String Granted = "Granted";
    int Selected;

    public PermissionLoader() {
        Permissions = new ArrayList<>();
        Selected = 0;
    }

    public void Append(String Permission) {
        Permissions.add(Permission);
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
