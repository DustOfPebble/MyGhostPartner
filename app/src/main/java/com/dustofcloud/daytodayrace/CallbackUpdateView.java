package com.dustofcloud.daytodayrace;

import android.graphics.PointF;
import android.location.Location;

import java.util.ArrayList;

public interface CallbackUpdateView {
    void updateInView(ArrayList<WayPoint> WayPointsInView);
    void updateInUse(ArrayList<WayPoint> WayPointsInUse);
    void updateOffset(PointF Offset);
}
