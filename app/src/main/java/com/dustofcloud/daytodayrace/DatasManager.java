package com.dustofcloud.daytodayrace;

import android.app.Application;

/**
 * Created by Xavier JAFFREZIC on 16/05/2016.
 */
public class DatasManager extends Application{
    final int StorageArea = 9600; // Storage Area is 9,6 km in both direction (Power of 2 x 100)

    @Override
    public void onCreate()
    {
        super.onCreate();

        // Create Datas WayPoints QuadTree storage
        QuadTree WayPoints = new QuadTree(StorageArea);

        // Start GPS engine
        GPS positions = new GPS(this);

        // Load previous files .... (low priority thread)
    }}
