package com.dustofcloud.daytodayrace;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Xavier JAFFREZIC on 13/05/2016.
 */
public class WayPoint implements Serializable {

    // Collected  datas from GPS sensor
    private double longitude;
    private double latitude;
    private float elevation;

    // Time stamp
    private int hour;
    private int minute;
    private int second;

    // Calculated datas for extraction/filtering
    private float yaw;
    private float pitch;
    private float velocity;

    private float x;
    private float y;

    public WayPoint(double longitude, double latitude, float elevation) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.elevation = elevation;

        // Timestamping received datas
        Calendar calendar = Calendar.getInstance();
        this.hour= calendar.get(Calendar.HOUR);
        this.minute = calendar.get(Calendar.MINUTE);
        this.second = calendar.get(Calendar.SECOND);

        // Converting Longitude & Latitude to 2D cartesian distance from an origin
        this.x = dX(this.longitude);
        this.y = dY(this.latitude);
    }

    public float getX() {return this.x;}
    public float getY() {return this.y;}
}
