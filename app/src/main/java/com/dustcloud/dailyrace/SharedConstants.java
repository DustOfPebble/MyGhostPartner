package com.dustcloud.dailyrace;


public class SharedConstants {

    static public final String FilesWorkingSpace = "DailyRace.Files";
    static public final String FilesSignature = ".DailyDB";
    static public final int NbMaxLoadedPoints = 100000; //

    static public final short SleepLocked =10;
    static public final short SleepUnLocked =11;

    static public final short LightEnhanced=10;
    static public final short LightNormal=11;

    static public final short BatterySaveMode=20;
    static public final short BatteryDrainMode=21;

    static public final short ReplayedGPS=30;
    static public final short LiveGPS=31;

    static public final short ConnectedHeartBeat=40;
    static public final short DisconnectedHeartBeat =41;
    static public final short SearchHeartBeat =42;

    static public final short SwitchForeground=100;
    static public final short SwitchBackground=101;

    static public final float BearingMatchingGap = 60f; // 60°
    static public final float AccuracyNotLoaded= 25f; // 25 Meters ...

    static public final float WidthToHeightFactor = 3f/4f;
    static public final short HeatbeatStatsID = 200;
    static public final short SpeedStatsID = 201;

    static public final short CenterTopWidget = 220;
    static public final short LeftBottomWidget = 221;
    static public final short RightBottomWidget = 222;
}
