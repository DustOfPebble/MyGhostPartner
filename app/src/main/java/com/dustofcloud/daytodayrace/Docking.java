package com.dustofcloud.daytodayrace;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

public class Docking extends Activity implements EventsControlSwitch {

    private ControlSwitch SleepLocker = null;
    private ControlSwitch BatterySaver = null;
    private ControlSwitch LightEnhancer = null;
    private ControlSwitch GPSProvider = null;

    private int BackPressedCount = 0;
    private DataManager BackendService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docking);

        SleepLocker = (ControlSwitch) findViewById(R.id.switch_sleep_locker);
        SleepLocker.setMode(SharedConstants.ScreenLocked, SharedConstants.ScreenUnLocked);
        SleepLocker.registerControlSwitch(this);

        LightEnhancer = (ControlSwitch) findViewById(R.id.switch_light_enhancer);
        LightEnhancer.setMode(SharedConstants.LightEnhanced, SharedConstants.LightNormal);
        LightEnhancer.registerControlSwitch(this);

        BatterySaver = (ControlSwitch) findViewById(R.id.switch_battery_saver);
        BatterySaver.setMode(SharedConstants.BatteryDrainMode, SharedConstants.BatterySaveMode);
        BatterySaver.registerControlSwitch(this);

        GPSProvider = (ControlSwitch) findViewById(R.id.gps_provider);
        GPSProvider.setMode(SharedConstants.LiveGPS, SharedConstants.ReplayedGPS);
        GPSProvider.registerControlSwitch(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        BackendService = (DataManager) DataManager.getBackend();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BackendService.setMode(SharedConstants.SwitchBackground);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BackPressedCount = 0;
        BackendService = (DataManager) DataManager.getBackend();
        BackendService.setMode(SharedConstants.SwitchForeground);
    }

    @Override
    public void onStatusChanged(short Status) {
        if (Status == SharedConstants.ScreenLocked)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Status == SharedConstants.ScreenUnLocked)
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Status == SharedConstants.LiveGPS)
            BackendService.setMode(SharedConstants.LiveGPS);
        if (Status == SharedConstants.ReplayedGPS)
            BackendService.setMode(SharedConstants.ReplayedGPS);

    }

    @Override
    public void onBackPressed() {
        BackPressedCount++;
        if (BackPressedCount > 1) {
            BackendService.setMode(SharedConstants.SwitchBackground);
            super.onBackPressed();
        }
        else { Toast.makeText(Docking.this, "Press back again to exit !", Toast.LENGTH_SHORT).show(); }
    }
}
