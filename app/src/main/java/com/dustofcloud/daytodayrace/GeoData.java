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
    public boolean isLoaded;
    private PointF Cartesian;

    private final String LongitudeID = "Lo";
    private final String LatitudeID = "La";
    private final String SpeedID = "S";
    private final String AltitudeID = "A";
    private final String BearingID = "B";

    Random Generator = new Random();
    // Preload Data with fake values ...
    private double Longitude = 2.0;
    private double Latitude = 48.0;
    private double Altitude = 100.;
    private double Speed = 20.0;
    private double Bearing = 0;

    public GeoData() {isLoaded = false; }

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
        isLoaded = true;
    }

    private double getNoise(double Range) {
        return ((double) (Generator.nextInt(1000) - 500)) / 1000.0 * Range;
    }

    public void fakeGPS() {
        Longitude += getNoise(1.0);
        Latitude += getNoise(1.0);
        Altitude += getNoise(15.0);
        Speed += getNoise(19.0);
        Bearing += getNoise(179.0);
        Cartesian = new PointF(
                DataManager.dX(Longitude),
                DataManager.dY(Latitude)
        );
    }

    public double getLongitude() {return Longitude;}
    public double getLatitude() {return Latitude;}
    public double getAltitude() {return Altitude;}
    public double getSpeed() {return Speed;}
    public double getBearing() {return Bearing;}

    public void toJSON(JsonWriter Writer) throws IOException {
            Writer.beginObject();
            Writer.name(LongitudeID).value(Longitude);
            Writer.name(LatitudeID).value(Latitude);
            Writer.name(AltitudeID).value(Altitude);
            Writer.name(SpeedID).value(Speed);
            Writer.name(BearingID).value(Bearing);
            Writer.endObject();
        }

    public void fromJSON(JsonReader Reader) throws IOException{
        Reader.beginObject();
        int Ids = 0;
        while (Reader.hasNext()) {
            String name = Reader.nextName();
                 if (name.equals(LongitudeID)) { Longitude = Reader.nextDouble(); Ids++; }
            else if (name.equals(LatitudeID)) { Latitude = Reader.nextDouble(); Ids++;}
            else if (name.equals(AltitudeID)) { Latitude = Reader.nextDouble(); Ids++;}
            else if (name.equals(SpeedID)) { Speed = Reader.nextDouble(); Ids++;}
            else if (name.equals(BearingID)) { Bearing = Reader.nextDouble(); Ids++;}
            else Reader.skipValue();
            isLoaded = true;
            }
        if (Ids >= 5) isLoaded = true; // At least 2 fields have been setup
        Reader.endObject();
    }

    public PointF getCartesian() {return Cartesian;}

}
