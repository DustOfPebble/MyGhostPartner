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
    private final String AltitudeID = "Alt";
    private final String BearingID = "Bear";

    Random Generator = new Random();
    // Preload Data with fake values ...
    private double Longitude = 0.0;
    private double Latitude = 0.0;
    private double Altitude = 0.0;
    private double Speed = 0.0;
    private double Bearing = 0.0;

    public GeoData() {
        Generator = new Random();
        // Preload Data with home values  [48.781687, 2.046504] ...
        Longitude = 2.0465;
        Latitude = 48.7816;
        Altitude = 100.;
        Speed = 20.0;
        Bearing = 0;
    }

    public void setGPS(Location GPS) {
        // Converting Longitude & Latitude to 2D cartesian distance from an origin
        Cartesian = new PointF(
                DataManager.dX(GPS.getLongitude()),
                DataManager.dY(GPS.getLatitude())
            );
        Longitude = GPS.getLongitude();
        Latitude = GPS.getLatitude();
        Bearing = GPS.getBearing();
        Speed = GPS.getSpeed();
        Altitude = GPS.getAltitude();
    }

    private double getNoise(double Range) {
        return ((double) (Generator.nextInt(1000) - 500)) / 1000.0 * Range;
    }

    public void fakeGPS() {
        Longitude += getNoise(0.005); // ~ 250 m (considering Latitude value)
        Latitude += getNoise(0.005); // ~ 500 m
        Altitude += getNoise(15.0);
        Speed += getNoise(19.0);
        Bearing += getNoise(179.0);
        Cartesian = new PointF(DataManager.dX(Longitude),DataManager.dY(Latitude));
    }

    public double getLongitude() {return Longitude;}
    public double getLatitude() {return Latitude;}

     public String toJSON() {
        JSONObject GeoJSON = new JSONObject();
        try {
            GeoJSON.put(LongitudeID, Math.floor(Longitude * 1e7) / 1e7);// Cut at 7 digit to save space
            GeoJSON.put(LatitudeID, Math.floor(Latitude * 1e7) / 1e7);// Cut at 7 digit to save space
            GeoJSON.put(AltitudeID, Math.floor(Altitude * 10) / 10); // Cut at 1 digit to save space
            GeoJSON.put(SpeedID, Math.floor(Speed * 10) / 10); // Cut at 1 digit to save space
            GeoJSON.put(BearingID, Math.floor(Bearing * 10) / 10); // Cut at 1 digit to save space
        } catch (Exception JSONBuilder) {}
//        Log.d("GeoData", "JSon from GeoData =" + GeoJSON.toString());
        return GeoJSON.toString();
    }

     public boolean fromJSON(String GeoString) {
        if (GeoString == null) return false;
        JSONObject GeoJSON = null;
        try {
            GeoJSON = new JSONObject(GeoString);
            Longitude = GeoJSON.getDouble(LongitudeID);
            Latitude = GeoJSON.getDouble(LatitudeID);
            Altitude = GeoJSON.getDouble(AltitudeID) ;
            Speed = GeoJSON.getDouble(SpeedID);
            Bearing = GeoJSON.getDouble(BearingID);
        }
        catch (Exception JSONBuilder) {
//            Log.d("GeoData", "GeoData from JSon => Failed to parse");
            JSONBuilder.printStackTrace();
            return false;
        }
        Cartesian = new PointF(DataManager.dX(Longitude),DataManager.dY(Latitude));
        return true;
    }

    public PointF getCartesian() {return Cartesian;}
}
