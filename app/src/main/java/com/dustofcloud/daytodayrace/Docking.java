package com.dustofcloud.daytodayrace;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class Docking extends Activity {

    private PointsDrawer PointsViewer = null;
    private ControlSwitch SleepLocker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docking);
        PointsViewer = (PointsDrawer) findViewById(R.id.PointsDrawer);
        SleepLocker = (ControlSwitch) findViewById(R.id.SwitchSleepLocker);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onBackPressed() {
      //  super.onBackPressed();
    }
}
