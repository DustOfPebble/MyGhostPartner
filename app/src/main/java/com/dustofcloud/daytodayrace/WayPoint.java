package com.dustofcloud.daytodayrace;

import android.graphics.PointF;
import android.location.Location;
import java.io.Serializable;


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

    public PointF getCartesian() {return Cartesian;}
}
