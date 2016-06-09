package com.dustofcloud.daytodayrace;

import android.app.Activity;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;

public class Docking extends Activity implements EventsProcessGPS {

    private ControlSwitch SleepLocker = null;
    private ControlSwitch BatterySaver = null;
    private ControlSwitch LightEnhancer = null;
    private ControlSwitch GPSProvider = null;

    private Monitor SpeedInfo = null;

    private MapManager MapView = null;

    private RectF searchZone = new RectF();
    private PointF ViewCenter;
    private ArrayList<GeoData> CollectedSelection;
    private ArrayList<Statistic> Speeds;

    private int BackPressedCount = 0;
    private DataManager BackendService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docking);

        BackendService = (DataManager) getApplication();
        BackendService.setUpdateCallback(this);

        MapView = (MapManager)  findViewById(R.id.map_manager);
        MapView.setBackend(BackendService);

        SleepLocker = (ControlSwitch) findViewById(R.id.switch_sleep_locker);
        SleepLocker.setMode(SharedConstants.ScreenLocked, SharedConstants.ScreenUnLocked);
        SleepLocker.registerManager(this);

        LightEnhancer = (ControlSwitch) findViewById(R.id.switch_light_enhancer);
        LightEnhancer.setMode(SharedConstants.LightEnhanced, SharedConstants.LightNormal);
        LightEnhancer.registerManager(this);

        BatterySaver = (ControlSwitch) findViewById(R.id.switch_battery_saver);
        BatterySaver.setMode(SharedConstants.BatteryDrainMode, SharedConstants.BatterySaveMode);
        BatterySaver.registerManager(this);

        GPSProvider = (ControlSwitch) findViewById(R.id.gps_provider);
        GPSProvider.setMode(SharedConstants.LiveGPS, SharedConstants.ReplayedGPS);
        GPSProvider.registerManager(this);

        SpeedInfo = (Monitor) findViewById(R.id.speed_info);
        Speeds = new ArrayList<Statistic>();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

    @Override
    public void processLocationChanged(GeoData geoInfo){
        if (BackendService == null) return;
        PointF SizeSelection = BackendService.getComputedSize();
        ViewCenter = geoInfo.getCoordinate();
        searchZone.set(this.ViewCenter.x - SizeSelection.x / 2, this.ViewCenter.y - SizeSelection.y / 2,
                       this.ViewCenter.x + SizeSelection.x / 2, this.ViewCenter.y + SizeSelection.y / 2  );
        CollectedSelection = BackendService.extract(searchZone);

        Speeds.clear();
        Speeds.add(new Statistic(geoInfo.getSpeed(),geoInfo.getElapsedDays()));
        for (GeoData item: CollectedSelection) {
            Speeds.add(new Statistic(item.getSpeed(),item.getElapsedDays()));
        }
        SpeedInfo.updateStatistics(Speeds);

    }
}
