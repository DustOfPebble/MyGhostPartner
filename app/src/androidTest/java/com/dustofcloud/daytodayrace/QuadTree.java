import com.dustofcloud.daytodayrace.WayPoint;

import java.util.ArrayList;

/**
 * Created by Xavier JAFFREZIC on 15/05/2016.
 */
public class QuadTree {
    private float x;
    private float y;
    private float size;

    private ArrayList<WayPoint> WayPoints;

    private final float minSize = 100; // Minimun size is 100 meters square..

    public QuadTree(float x, float y, float size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }


    ArrayList<WayPoint> searchWayPoints(float x, float y, float size) {
    return new ArrayList(); // return NULL list by default
    }

    void storeWayPoint(WayPoint wayPoint) {}

}
