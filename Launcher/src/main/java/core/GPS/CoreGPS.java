package core.GPS;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import core.Settings.Parameters;
import core.Structures.Coords2D;
import core.Structures.Sample;
import core.Structures.Statistic;
import core.launcher.partner.Docking;
import services.Sensor.SensorState;

import static java.lang.Math.cos;
import static java.lang.Math.toRadians;

public class CoreGPS implements LocationListener {
    private String LogTag = CoreGPS.class.getSimpleName();

    private static final float earthRadius = 6400000f; // Earth Radius is 6400 kms

    private static float earthRadiusCorrected(CoordsGPS Coords) { return earthRadius *(float)cos(toRadians(Coords.latitude)); }
    private static CoordsGPS Origin = null;

    private CoordsGPS Coords = null;
    private Coords2D Shift = null;
    private Statistic Snapshot = null;
    private Sample Basics = null;

    private LocationManager SensorGPS = null;
    private Context Owner = null;
    private ArrayList<EventsGPS> Listeners = null;
    private int SensorBPM = 0;
    private static long UpdateDelay;

    public CoreGPS(Context Owner, long Delay) {
        this.Owner =  Owner;
        Listeners = new ArrayList<>();
        UpdateDelay =  Delay;
    }

    public void setBPM(int BPM) { SensorBPM = BPM; }

    /******************************************************************
     * Behavior management
     ******************************************************************/
    @SuppressWarnings({"MissingPermission"})
    public void start() {
        Log.d(LogTag, "Starting GPS Listener");
        if (SensorGPS == null) SensorGPS = (LocationManager) Owner.getSystemService(Context.LOCATION_SERVICE);
        SensorGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, UpdateDelay , 0, this);
    }

    public void stop() {
        Log.d(LogTag, "Stopping GPS !");
        SensorGPS.removeUpdates(this);
    }

    public void reset() { Origin = null; }

    public void addListener(EventsGPS Listener) {
        Log.d(LogTag, "Registering "+Listener.getClass().getSimpleName()+" as setGPS Listener.");
        Listeners.add(Listener); }

    /******************************************************************
     * Content retrieval
     ******************************************************************/
    public CoordsGPS Origin() { return Origin; }

    public Coords2D Moved() {
        if (Origin == null) return null;
        return Shift;
    }

    public Sample ToSample() { return Basics; }

    public Statistic Statistic(int NbDays) {
        if (Origin == null) return null;
        Snapshot.Days = (short)NbDays;
        return Snapshot;
    }

    /******************************************************************
     * Events implementations
     ******************************************************************/
    @Override
    public void onLocationChanged(Location GPS) {
        if (Origin == null) {
            Origin = new CoordsGPS(GPS.getLongitude(), GPS.getLatitude());
            Log.d(LogTag, "GPS is active...");
        }

        Coords = new CoordsGPS(GPS.getLongitude(), GPS.getLatitude());

        Shift = new Coords2D( earthRadiusCorrected(Coords) * (float) toRadians(Coords.longitude - Origin.longitude),
                              earthRadius * (float) toRadians(Coords.latitude - Origin.latitude));

        Snapshot = new Statistic();
        Snapshot.Accuracy = GPS.getAccuracy();
        Snapshot.Bearing = GPS.getBearing();
        Snapshot.Speed = GPS.getSpeed();
        Snapshot.Altitude = (float)GPS.getAltitude();
        Snapshot.Heartbeat = (byte) SensorBPM;

        Basics =  new Sample();
        Basics.Longitude = GPS.getLongitude();
        Basics.Latitude = GPS.getLatitude();
        Basics.Altitude = GPS.getAltitude();
        Basics.Accuracy = GPS.getAccuracy();
        Basics.Speed = GPS.getSpeed();
        Basics.Bearing = GPS.getBearing();
        Basics.Heartbeat = (byte) SensorBPM;

        for (EventsGPS Listener:Listeners) { Listener.UpdatedGPS(this); }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) {  }

    @Override
    public void onProviderDisabled(String provider) { }
}
