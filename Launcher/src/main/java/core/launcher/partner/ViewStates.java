package core.launcher.partner;

import android.app.Application;
import core.Settings.Switches;

public class ViewStates extends Application  {

    private short ModeGPS = Switches.NoGPS;
    private short ModeSensor = Switches.NoSensor;
    private short ModeLogger = Switches.LoggerOFF;
    private short ModeScreen = Switches.SleepLocked;

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

    // Managing back light intensity for screen --> Not implemented
    public short getModeLogger() {return ModeLogger;}
    public void storeModeLight(short mode) { ModeLogger = mode;}

}