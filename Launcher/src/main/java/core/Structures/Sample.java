package core.Structures;

import core.GPS.CoordsGPS;

import static java.lang.Math.cos;
import static java.lang.Math.toRadians;

/***********************************************************************
 *  Commodity class for temporary storing/exporting values from files or services
 ***********************************************************************/
public class Sample {
    private static final float earthRadius = 6400000f; // Earth Radius is 6400 kms
    private static float earthRadiusCorrected(double Latitude) { return earthRadius *(float)cos(toRadians(Latitude)); }

    /*********************************
     * List of managed Fields
     *********************************/
    public double Longitude = 0.0;
    public double Latitude = 0.0;
    public double Altitude = 0.0;

    public float Speed = 0f;
    public float Accuracy = 0f;
    public float Bearing = 0f;

    public byte Heartbeat = -1;
    /*********************************
     * Calls to retrieve Structures
     *********************************/
    public Coords2D MovedFrom(CoordsGPS Origin) {
        float dx = earthRadiusCorrected(Latitude) * (float) toRadians(Longitude - Origin.longitude);
        float dy = earthRadius * (float) toRadians(Latitude - Origin.latitude);
        return new Coords2D( dx, dy);
    }
    
    public Statistic Statistic(int NbDays) {
        Statistic Snapshot = new Statistic();
        Snapshot.Speed = Speed;
        Snapshot.Accuracy = Accuracy ;
        Snapshot.Bearing = Bearing;

        Snapshot.Altitude = (float)Altitude;
        Snapshot.Heartbeat = Heartbeat;
        Snapshot.Days = (short) NbDays;
        return Snapshot;
    }
}
