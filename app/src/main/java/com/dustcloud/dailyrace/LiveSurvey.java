package com.dustcloud.dailyrace;

import android.graphics.PointF;
import android.location.Location;
import android.util.Log;

import org.json.JSONObject;

public class LiveSurvey {
    private PointF Cartesian;

    private float Altitude = 0.0f;
    private float Accuracy = 0.0f;
    private float Speed = 0.0f;
    private float Bearing = 0.0f;
    private short Heartbeat = -1;

    private int ElapsedDays = 0;
    private boolean Live = true; // by default data are Live values

    public LiveSurvey() { }

    public float getAltitude() {return Altitude;}
    public void setAltitude(float value) {Altitude = value;}

    public float getAccuracy() {return Accuracy;}
    public void setAccuracy(float value) {Accuracy = value;}

    public float getSpeed() {return Speed;}
    public void setSpeed(float value) {Speed = value;}

    public float getBearing() {return Bearing;}
    public void setBearing(float value) {Bearing = value;}

    public int getHeartbeat() {return Heartbeat;}
    public void setHeartbeat(short value) { Heartbeat = value;}

    public int getElapsedDays() {return ElapsedDays;}
    public void setElapsedDays(int value) { ElapsedDays = value;}

    public boolean isLive() {return Live;}
    public void setSimulated() { Live = false;}

    public PointF getCoordinate() {return Cartesian;}
    public void setCoordinate(PointF Coordinate) { Cartesian = Coordinate;}

}
