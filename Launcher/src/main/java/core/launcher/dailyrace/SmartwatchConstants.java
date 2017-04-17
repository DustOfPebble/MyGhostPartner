package core.launcher.dailyrace;

/********************************************************************
 * Constants used to create dictionnary for sending to Smartwatch
 ********************************************************************/

public class SmartwatchConstants {
    static final String WatchUUID ="e603e869-8eff-40c3-9957-6c6f6087fd57";

    static final int SensorBeat = 11;

    /********************************
     *  Key definition for messages *
     * ******************************
     * //Information about Phone events
     * #define  CallsCount	5 // (Nb) Unsigned Byte [0,255]
     * #define  MessagesCount 6 // (Nb) Unsigned Byte [0,255]
     *
     * // Information about Heart beat sensor
     * #define  SensorEnabled 10 // Boolean [true,false]
     * #define  SensorBeat 11 // (Beat/sec) Unsigned Byte [0,255]
     * #define  SensorTrendBeat 12 // (Beat/sec) Signed Byte [-127,+127]
     *
     * // Information about Speed
     * #define  CycleEnabled 20 // Boolean [true,false]
     * #define  CycleInstantSpeed 21 // (km/h) Unsigned Byte [0,255]
     * #define  CycleMeanSpeed 22 // (km/h) Not defined
     * #define  CycleTrendSpeed 23 // (km/h) Not defined
     *
     * // Information about Trip
     * #define  TripMode 30 // Boolean [true,false]
     * #define  TripLength 31 // (m) Unsigned int [0,4294967296]
     * #define  TripLengthElapsed 32 // (m) Unsigned int [0,4294967296]
     * #define  TripLengthRemain 33 // (m) Unsigned int [0,4294967296]
     * #define  TripElevationClimbed 34 // (m) Signed short [-16384,+16384]
     * *********************************/

}
