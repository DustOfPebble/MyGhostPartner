package com.dustofcloud.daytodayrace;

import android.location.Location;

import java.util.AbstractList;

/**
 * Created by Xavier JAFFREZIC on 16/05/2016.
 */
public interface CallbackUpdateView {
    void updateInView(AbstractList<WayPoint> WayPointsInView);
    void updateInUse(AbstractList<WayPoint> WayPointsInUse);
}
