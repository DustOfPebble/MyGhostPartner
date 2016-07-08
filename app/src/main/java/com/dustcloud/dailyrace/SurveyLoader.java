package com.dustcloud.dailyrace;

import android.location.Location;
import android.util.Log;

import org.json.JSONObject;

public class SurveyLoader {

    // Internals Fields
    private double Longitude;
    private double Latitude;
    private float Accuracy;
    private float Speed;
    private float Bearing;
    private float Altitude;
    private short Heartbeat;

    private boolean hasOrigin;
    private int Days;

    // Constructor ...
    public SurveyLoader() {
        hasOrigin = false;
        Days = 0;

        Longitude = 0.0;
        Latitude = 0.0;
        Accuracy = 0.0f;
        Speed = 0.0f;
        Bearing = 0.0f;
        Altitude = 0.0f;
        Heartbeat = -1;
    }

    // JSON Fields IDs
    private final String LongitudeID = "Long";
    private final String LatitudeID = "Lat";
    private final String SpeedID = "Spd";
    private final String AccuracyID = "Acc";
    private final String AltitudeID = "Alt";
    private final String BearingID = "Bear";
    private final String HeartbeatID = "Heart";

    // JSON Converters
    public String toJSON() {
        JSONObject SurveyJSON = new JSONObject();
        try {
            SurveyJSON.put(LongitudeID, Math.floor(Longitude * 1e7) / 1e7);// Cut at 7 digit to save space
            SurveyJSON.put(LatitudeID, Math.floor(Latitude * 1e7) / 1e7);// Cut at 7 digit to save space
            SurveyJSON.put(SpeedID, Math.floor(Speed * 10) / 10); // Cut at 1 digit to save space
            SurveyJSON.put(AccuracyID, Math.floor(Accuracy * 10) / 10); // Cut at 1 digit to save space
            SurveyJSON.put(BearingID, Math.floor(Bearing * 10) / 10); // Cut at 1 digit to save space
            SurveyJSON.put(AltitudeID, Math.floor(Altitude * 10) / 10); // Cut at 1 digit to save space
            if (Heartbeat != -1) SurveyJSON.put(HeartbeatID, Heartbeat);
        } catch (Exception JSONBuilder) { Log.d("SurveyLoader", "ToJSON => Error in JSON construction.");}
        return SurveyJSON.toString();
    }

    public boolean fromJSON(String StringJSON) {
        if (StringJSON == null) return false;
        JSONObject SurveyJSON;
        try { SurveyJSON = new JSONObject(StringJSON); }
        catch (Exception JSONBuilder)
        { Log.d("SurveyLoader", "SurveyLoader from JSon => Failed to parse ["+StringJSON+"]"); return false;}
        try {
            Longitude = SurveyJSON.getDouble(LongitudeID);
            Latitude = SurveyJSON.getDouble(LatitudeID);
            Altitude = (float)SurveyJSON.getDouble(AltitudeID);
            Speed = (float)SurveyJSON.getDouble(SpeedID);
            Bearing = (float)SurveyJSON.getDouble(BearingID);
            Accuracy = (float)SurveyJSON.getDouble(AccuracyID);
        }
        catch (Exception Missing)
        { Log.d("SurveyLoader", "SurveyLoader from JSon => Missing required value"); return false;}

        try { Heartbeat = (short) SurveyJSON.getInt(HeartbeatID);}
        catch (Exception Missing) { Heartbeat = (short) -1;}
        return true;
    }

    public double getLongitude() {return Longitude;}
    public double getLatitude() {return Latitude;}

    public void setHeartbeat(short value) { Heartbeat = value;}
    public void setDays(int value) { Days = value;}

    public void updateFromGPS(Location GPS) {
        Longitude = GPS.getLongitude();
        Latitude = GPS.getLatitude();
        Altitude = (float)GPS.getAltitude();
        Bearing = GPS.getBearing();
        Speed = GPS.getSpeed();
        Accuracy = GPS.getAccuracy();

        Log.d("DataManager", "GPS [" + Longitude + "°E," + Latitude + "°N]");
        if (!hasOrigin) useOrigin();
    }

    public Snapshot getSnapshot() {
        Snapshot Snapshot = new Snapshot();
        Snapshot.setSpeed(Speed);
        Snapshot.setAccuracy(Accuracy);
        Snapshot.setBearing(Bearing);
        Snapshot.setAltitude(Altitude);
        Snapshot.setHeartbeat(Heartbeat);
        Snapshot.setDays(Days);
        Snapshot.setCoordinates(dX(Longitude), dY(Latitude));
        return Snapshot;
    }

    // Utility function to convert Latitude/Longitude to cartesian/metric values
    private final float earthRadius = 6400000f; // Earth Radius is 6400 kms
    private float earthRadiusCorrected = earthRadius; // Value at Equator to Zero at Pole
    private double originLongitude = 0f;
    private double originLatitude = 0f;

    public void clearOrigin() {hasOrigin = false;}

    public void setOrigin(double Longitude, double Latitude) {
        this.originLatitude = Latitude;
        this.originLongitude = Longitude;
        earthRadiusCorrected = correctedRadius();
        hasOrigin = true;
    }

    private void useOrigin(){
        this.originLatitude = Latitude;
        this.originLongitude = Longitude;
        earthRadiusCorrected = correctedRadius();
        hasOrigin = true;
    }

    private float correctedRadius() { return earthRadius *(float)Math.cos( Math.toRadians(originLatitude)); }
    private float dX(double longitude) { return earthRadiusCorrected * (float) Math.toRadians(longitude-originLongitude); }
    private float dY(double latitude) { return  earthRadius * (float) Math.toRadians(latitude-originLatitude); }

}
