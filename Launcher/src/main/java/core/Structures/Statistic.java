package core.Structures;

import android.location.Location;

public class Statistic {

    private Node Coordinate;

    public float Speed = 0f;
    public float Accuracy = 0f;
    public float Bearing = 0f;
    public float Altitude = 0f;

    public byte Heartbeat = 0;
    private short Days = -1;

    public Statistic() {}
    public Statistic(Location UpdateGPS) {
        Speed = UpdateGPS.getSpeed();
        Accuracy = UpdateGPS.getAccuracy();
        Altitude = (float)UpdateGPS.getAltitude();
        Bearing = UpdateGPS.getBearing();
    }

    public Node node() {return Coordinate;}
    public void set(float x, float y) { Coordinate.dx = x; Coordinate.dy = y;}
    public void set(Node Coordinate) { this.Coordinate = Coordinate;}
}
