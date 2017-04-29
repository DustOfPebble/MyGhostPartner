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

    }

    public Node node() {return Coordinate;}
    public void set(float x, float y) { Coordinate.x = x; Coordinate.y = y;}
    public void set(Node Coordinate) { this.Coordinate = Coordinate;}

}
