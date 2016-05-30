package com.dustofcloud.daytodayrace;

import android.app.Activity;
import android.os.Bundle;

public class Docking extends Activity {

    private PointsDrawer PointsViewer = null;
    private ControlSwitch SleepLocker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dock_views);
        PointsViewer = (PointsDrawer) findViewById(R.id.show_waypoints);
        SleepLocker = (ControlSwitch) findViewById(R.id.SwitchSleepLocker);

    }


}
