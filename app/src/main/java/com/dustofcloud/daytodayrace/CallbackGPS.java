package com.dustofcloud.daytodayrace;

/**
 * Created by Xavier JAFFREZIC on 16/05/2016.
 */
public interface CallbackGPS {
    void updatedPosition(double longitude, double latitude, float elevation);
}
