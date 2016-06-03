package com.dustofcloud.daytodayrace;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

public class Docking extends Activity {

    private PointsDrawer PointsViewer = null;
    private ControlSwitch SleepLocker = null;
    private int BackPressedCount = 0;
    private DataManager BackendService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docking);
        PointsViewer = (PointsDrawer) findViewById(R.id.PointsDrawer);
        SleepLocker = (ControlSwitch) findViewById(R.id.SwitchSleepLocker);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        BackendService = (DataManager) DataManager.getBackend();

    }

    @Override
    protected void onResume() {
        super.onResume();
        BackPressedCount = 0;
        BackendService = (DataManager) DataManager.getBackend();
    }

    @Override
    public void onBackPressed() {
        BackPressedCount++;
        if (BackPressedCount > 1) { super.onBackPressed(); }
        else { Toast.makeText(Docking.this, "Press back again to exit !", Toast.LENGTH_SHORT).show(); }
    }
}
