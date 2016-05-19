package com.dustofcloud.daytodayrace;

import android.graphics.PointF;
import android.location.Location;

import java.io.Serializable;

/**
 * Created by Xavier JAFFREZIC on 13/05/2016.
 */
public class WayPoint extends Location implements Serializable {
    PointF Cartesian;
    public WayPoint(Location updatedPosition) {
        super(updatedPosition);

        // Converting Longitude & Latitude to 2D cartesian distance from an origin
        Cartesian = new PointF(
                DataManager.dX(this.getLongitude()),
                DataManager.dY(this.getLatitude())
            );
    }

    public float getX() {return Cartesian.x;}
    public float getY() {return Cartesian.y;}
}
