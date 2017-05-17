package core.launcher.partner;

import android.app.Application;
import core.Settings.Parameters;
import core.Settings.SwitchModes;
import core.Structures.Extension;

public class ViewStates extends Application  {

    private short ModeGPS = SwitchModes.LiveGPS;
    private short ModeLight = SwitchModes.LightEnhanced;
    private short ModeBattery = SwitchModes.BatteryDrainMode;
    private short ModeScreen = SwitchModes.SleepLocked;
    private short ModeSensor = SwitchModes.DisconnectedHeartBeat;

    @Override
    public void onCreate() { super.onCreate();  }

    public void shutdown() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }


    // Managing state for CoreGPS
    public short getModeGPS() {return ModeGPS;}
    public void storeModeGPS(short mode) {ModeGPS = mode;}

     // Managing sleep state for HMI
    public short getModeSleep() {return ModeScreen;}
    public void storeModeSleep(short mode) {ModeScreen = mode;}

    // Managing back light intensity for screen --> Not implemented
    public short getModeLight() {return ModeLight;}
    public void storeModeLight(short mode) {ModeLight = mode;}

    // Managing back light intensity for screen --> Not implemented
    public short getModeSensor() {return ModeSensor;}
    public void storeModeSensor(short mode) { ModeSensor = mode;}

    // Managing Animation behaviour to reduce energy consumption --> Not implemented
    public short getModeBattery() {return ModeBattery;}
    public void storeModeBattery(short mode) {ModeBattery = mode;}

    // Return area size selection for statistics
    public Extension getExtractStatisticsSize(){ return Parameters.StatisticsSelectionSize; }

    // Return area size selection for statistics
    public Extension getExtractDisplayedSize(){ return Parameters.DisplayedSelectionSize; }

}