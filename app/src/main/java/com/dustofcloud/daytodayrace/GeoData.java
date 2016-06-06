package com.dustofcloud.daytodayrace;

import android.graphics.PointF;
import android.location.Location;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

public class GeoData {
    private PointF Cartesian;

    private final String LongitudeID = "Long";
    private final String LatitudeID = "Lat";
    private final String SpeedID = "Spd";
    private final String AccuracyID = "Acc";
    private final String AltitudeID = "Alt";
    private final String BearingID = "Bear";
    private final String HeartbeatID = "Heart";

    private double Longitude = 0.0;
    private double Latitude = 0.0;
    private float Altitude = 0.0f;
    private float Accuracy = 0.0f;
    private float Speed = 0.0f;
    private float Bearing = 0.0f;
    private int Heartbeat = 0;

    private int ElapsedDays = 0;
    private boolean Live = true; // by default data are Live values

    public GeoData() { }

    public double getLongitude() {return Longitude;}
    public double getLatitude() {return Latitude;}
    public float getAltitude() {return Altitude;}
    public float getAccuracy() {return Accuracy;}
    public float getSpeed() {return Speed;}
    public float getBearing() {return Bearing;}
    public int getHeartbeat() {return Heartbeat;}
    public int getElapsedDays() {return ElapsedDays;}

    public boolean isLive() {return Live;}
    public void setSimulated() { Live= false;}

    public void setHeartbeat(int value) { Heartbeat = value;} // Provisionning function

    public void setElapsedDays(int value) { ElapsedDays = value;} // Provisionning function

    public void setGPS(Location GPS) {
        Longitude = GPS.getLongitude();
        Latitude = GPS.getLatitude();
        Altitude = (float)GPS.getAltitude();
        Bearing = GPS.getBearing();
        Speed = GPS.getSpeed();
        Accuracy = GPS.getAccuracy();
    }

     public String toJSON() {
        JSONObject GeoJSON = new JSONObject();
        try {
            GeoJSON.put(LongitudeID, Math.floor(Longitude * 1e7) / 1e7);// Cut at 7 digit to save space
            GeoJSON.put(LatitudeID, Math.floor(Latitude * 1e7) / 1e7);// Cut at 7 digit to save space
            GeoJSON.put(AccuracyID, Math.floor(Accuracy * 10) / 10); // Cut at 1 digit to save space
            GeoJSON.put(AltitudeID, Math.floor(Altitude * 10) / 10); // Cut at 1 digit to save space
            GeoJSON.put(SpeedID, Math.floor(Speed * 10) / 10); // Cut at 1 digit to save space
            GeoJSON.put(BearingID, Math.floor(Bearing * 10) / 10); // Cut at 1 digit to save space
            GeoJSON.put(HeartbeatID, Heartbeat);
        } catch (Exception JSONBuilder) {}
//        Log.d("GeoData", "JSon from GeoData =" + GeoJSON.toString());
        return GeoJSON.toString();
    }

     public boolean fromJSON(String GeoString) {
        if (GeoString == null) return false;
        JSONObject GeoJSON = null;
        try { GeoJSON = new JSONObject(GeoString); } catch (Exception JSONBuilder)
        { Log.d("GeoData", "GeoData from JSon => Failed to parse ["+GeoString+"]"); return false;}
        try {
               Longitude = GeoJSON.getDouble(LongitudeID);
               Latitude = GeoJSON.getDouble(LatitudeID);
               Altitude = (float)GeoJSON.getDouble(AltitudeID);
               Speed = (float)GeoJSON.getDouble(SpeedID);
               Bearing = (float)GeoJSON.getDouble(BearingID);
               Accuracy = (float)GeoJSON.getDouble(AccuracyID);
        }
        catch (Exception Missing) { Log.d("GeoData", "GeoData from JSon => Missing required value"); return false;}

        try { Heartbeat = GeoJSON.getInt(HeartbeatID);} catch (Exception Missing) { Heartbeat = 0;}
        return true;
    }

    public PointF getCoordinate() {return Cartesian;}
    public void setCoordinate(PointF Coordinate) { Cartesian = Coordinate;}

}
