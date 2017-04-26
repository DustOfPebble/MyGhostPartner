package core.launcher.application;

/********************************************************************
 * Constants used to create dictionnary for sending to Smartwatch
 ********************************************************************/

public class SmartwatchConstants {
    static final String WatchUUID ="e603e869-8eff-40c3-9957-6c6f6087fd57";


    // Information about Phone events
    static public final int CallsCount = 1;
    static public final int MessagesCount = 2;

    // Information about Weather events
    static public final int WeatherSkyNow = 4;
    static public final int WeatherTemperatureNow = 5;
    static public final int WeatherTemperatureMax = 6;
    static public final int WeatherTemperatureMin = 7;
    static public final int WeatherLocationName = 8;

    /********************************
     *  Key definition for messages *
     * ******************************
     * // Information about Phone events
     * #define  CallsCount	1 // (Nb) Unsigned Byte [0,255]
     * #define  MessagesCount 2 // (Nb) Unsigned Byte [0,255]
     *
     * // Information about Weather events
     * #define  WeatherSkyNow	4 // (Nb) Unsigned Byte [0,255]
     * #define  WeatherTemperatureNow 5 // (Nb) Signed Byte [0,255]
     * #define  WeatherTemperatureMax 6 // (Nb) Signed Byte [0,255]
     * #define  WeatherTemperatureMin 7 // (Nb) Signed Byte [0,255]
     * #define  WeatherLocationName 8 // (Name) String  [40 chars]
     *
     * // Information about Heart beat sensor
     * #define  SensorEnabled 10 // Boolean [true,false]
     * #define  SensorBeat 11 // (Beat/sec) Unsigned Byte [0,255]
     * #define  SensorTrendBeat 12 // (Beat/sec) Signed Byte [-127,+127]
     *
     * // Information about Speed
     * #define  CycleEnabled 20 // Boolean [true,false]
     * #define  CycleInstantSpeed 21 // (m/s) Unsigned Byte [0,255]
     * #define  CycleMeanSpeed 22 // (m/s) Unsigned Byte [0,255]
     * #define  CycleTrendSpeed 23 // (m/s) Unsigned Byte [0,255]
     *
     * // Information about Trip
     * #define  TripMode 30 // Boolean [true,false]
     * #define  TripLength 31 // (m) Unsigned int [0,4294967296]
     * #define  TripLengthElapsed 32 // (m) Unsigned int [0,4294967296]
     * #define  TripLengthRemain 33 // (m) Unsigned int [0,4294967296]
     * #define  TripElevationClimbed 34 // (m) Signed short [-16384,+16384]
     * *********************************/

}
