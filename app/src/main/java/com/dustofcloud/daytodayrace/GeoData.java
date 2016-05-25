package com.dustofcloud.daytodayrace;

import android.graphics.PointF;
import android.location.Location;
import java.io.Serializable;


public class GeoData {
    PointF Cartesian;

    private double Longitude = 0;
    private double Latitude = 0;
    private double Altitude = 0;
    private double Speed = 0;
    private double Bearing = 0;

    public GeoData() { }

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

    public String toJSON() {return "[]";}
    public void fromJSON(String JSON) {}

    public PointF getCartesian() {return Cartesian;}


}
