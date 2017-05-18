package core.Settings;


import core.Structures.Extension;

public class Parameters {
    public static final float WidthToHeightFactor = 3f/4f;

    public static final int NbMaxLoadedPoints = 100000; //

    public static final long TimeUpdateGPS = 1000; // value in ms
    public static int LowAccuracyGPS = 15; // in Meters

    public static int LogClearance = 5; // in Meters
    public static int LiveClearance = 2; // in Meters
    public static int TrackClearance = 20; // in Meters
    public static int TimeSearchLimit = 60; // in Seconds


    public static Extension StatisticsSelectionSize = new Extension(20f,20f); // in meters
    public static Extension DisplayedSelectionSize = new Extension(200f,200f); // in meters

}
