package core.launcher.partner;

import android.app.Application;

import java.util.HashMap;

import core.launcher.Buttons.SwitchEnums;

public class DockingSaved extends Application  {

    private short ModeGPS = SwitchEnums.Disabled;
    private short ModeSensor = SwitchEnums.Disabled;
    private short ModeLogger = SwitchEnums.Disabled;
    private short ModeScreen = SwitchEnums.Enabled;

    private HashMap<Integer, Short> SavedStates = new HashMap<>();
    public  void setState(int ID, short Setup) {
        SavedStates.put(ID,Setup);
    }
    public short getState(int ID) {
        return SavedStates.get(ID);
    }

    @Override
    public void onCreate() { super.onCreate();  }

    public void shutdown() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    // Managing state for CoreGPS
    public short getModeGPS() {return ModeGPS;}
    public void storeModeGPS(short mode) {ModeGPS = mode;}

    // Managing Heart Sensor  for screen
    public short getModeSensor() {return ModeSensor;}
    public void storeModeSensor(short mode) { ModeSensor = mode;}

    // Managing sleep state for HMI
    public short getModeSleep() {return ModeScreen;}
    public void storeModeSleep(short mode) {ModeScreen = mode;}

    // Managing trace recorder
    public short getModeLogger() {return ModeLogger;}
    public void storeModeLogger(short mode) { ModeLogger = mode;}

}