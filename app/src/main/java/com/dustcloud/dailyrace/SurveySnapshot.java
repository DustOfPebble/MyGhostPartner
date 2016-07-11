package com.dustcloud.dailyrace;

public class SurveySnapshot {

    public float x;
    public float y;

    private float Speed;
    private float Accuracy;
    private float Bearing;
    private float Altitude;

    private short Heartbeat;

    private int Days;

    public SurveySnapshot() {
        x = 0f;
        y = 0f;

        Speed = 0f;
        Altitude = 0f;
        Accuracy = 0f;
        Bearing = 0f;
        Heartbeat = -1;

        Days = -1;
    }

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

    public int getDays() {return Days;}
    public void setDays(int value) { Days = value;}

    public Vector copy() {return new Vector(this.x, this.y);}
    public void set(float x, float y) { this.x = x; this.y = y;}
    public void set(Vector Dot) { this.x = Dot.x; this.y = Dot.y;}

}
