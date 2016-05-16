package com.dustofcloud.daytodayrace;

import com.dustofcloud.daytodayrace.WayPoint;

import java.util.ArrayList;

/**
 * Created by Xavier JAFFREZIC on 15/05/2016.
 */
public class QuadTree {
    private float x;
    private float y;
    private float halfSize;

    private ArrayList<WayPoint> WayPoints = null;
    private ArrayList<QuadTree> SubLevels = null;

    private final float minSize = 100; // Minimun size is 100 meters square..

    public QuadTree(int size) {
        this.halfSize = size /2 ;
        if (size > minSize) SubLevels = new ArrayList<>();
        if (size <= minSize) WayPoints = new ArrayList<>();
    }

    public void setCenter(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public ArrayList<WayPoint> searchWayPoints(float x, float y, float size) {
        return new ArrayList(); // return NULL list by default

    }

    public void storeWayPoint(WayPoint wayPoint) {
        // Should we store this new point ?
        if (wayPoint.getX() < x-halfSize) return;
        if (wayPoint.getX() > x+halfSize) return;
        if (wayPoint.getY() < y-halfSize) return;
        if (wayPoint.getY() > y+halfSize) return;

        if (SubLevels==null) WayPoints.add(wayPoint);

    }



}
