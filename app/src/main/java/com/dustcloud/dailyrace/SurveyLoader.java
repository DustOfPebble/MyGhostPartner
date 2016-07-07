package com.dustcloud.dailyrace;

import android.location.Location;
//TODO : Find a new name for SurveyLoader Class

public class SurveyLoader {

    private double Longitude = 0.0;
    private double Latitude = 0.0;
    private float Altitude = 0.0f;
    private float Accuracy = 0.0f;
    private float Speed = 0.0f;
    private float Bearing = 0.0f;
    private short Heartbeat = -1;

    private int ElapsedDays = 0;

    public SurveyLoader() { }

    public double getLongitude() {return Longitude;}
    public double getLatitude() {return Latitude;}
    public float getAltitude() {return Altitude;}
    public float getAccuracy() {return Accuracy;}
    public float getSpeed() {return Speed;}
    public float getBearing() {return Bearing;}
    public int getHeartbeat() {return Heartbeat;}
    public int getElapsedDays() {return ElapsedDays;}

    public void setLongitude(double value) {Longitude = value;}
    public void setLatitude(double value) {Latitude = value;}
    public void setAltitude(float value) {Altitude = value;}
    public void setAccuracy(float value) {Accuracy = value;}
    public void setSpeed(float value) {Speed = value;}
    public void setBearing(float value) {Bearing = value;}
    public void setHeartbeat(short value) { Heartbeat = value;}
    public void setElapsedDays(int value) { ElapsedDays = value;}

    public void setGPS(Location GPS) {
        Longitude = GPS.getLongitude();
        Latitude = GPS.getLatitude();
        Altitude = (float)GPS.getAltitude();
        Bearing = GPS.getBearing();
        Speed = GPS.getSpeed();
        Accuracy = GPS.getAccuracy();
    }

    public LiveSurvey getSnapshot() {
        LiveSurvey Snapshot = new LiveSurvey();
        Snapshot.setAltitude(Altitude);
        Snapshot.setSpeed(Speed);
        Snapshot.setAccuracy(Accuracy);
        Snapshot.setHeartbeat(Heartbeat);
        Snapshot.setElapsedDays(ElapsedDays);
        Snapshot.setBearing(Bearing);
        return Snapshot;
    }


}
