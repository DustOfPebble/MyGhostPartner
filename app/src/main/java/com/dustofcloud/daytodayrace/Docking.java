package com.dustofcloud.daytodayrace;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;

public class Docking extends Activity implements EventsProcessGPS {

    private ControlSwitch SleepLocker = null;
    private ControlSwitch BatterySaver = null;
    private ControlSwitch LightEnhancer = null;
    private ControlSwitch GPSProvider = null;
    private ControlSwitch HeartBeatProvider = null;

    private Monitor LeftMonitor = null;
    private Monitor RightMonitor = null;

    private Bitmap SpeedThumb = null;
    private Bitmap HeartThumb = null;

    private MapManager MapView = null;

    private RectF searchZone = new RectF();
    private PointF ViewCenter;
    private ArrayList<GeoData> CollectedSelection;

    private ArrayList<Statistic> Speeds;
    private ArrayList<Statistic> HeartBeats;

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
        SleepLocker.registerModes(SharedConstants.SleepLocked, SharedConstants.SleepUnLocked);
        SleepLocker.registerManager(this);
        SleepLocker.setMode(BackendService.getModeSleep());
        if (BackendService.getModeSleep() == SharedConstants.SleepLocked)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        LightEnhancer = (ControlSwitch) findViewById(R.id.switch_light_enhancer);
        LightEnhancer.registerModes(SharedConstants.LightEnhanced, SharedConstants.LightNormal);
        LightEnhancer.registerManager(this);
        LightEnhancer.setMode(BackendService.getModeLight());
        LightEnhancer.setVisibility(View.INVISIBLE);

        BatterySaver = (ControlSwitch) findViewById(R.id.switch_battery_saver);
        BatterySaver.registerModes(SharedConstants.BatteryDrainMode, SharedConstants.BatterySaveMode);
        BatterySaver.registerManager(this);
        BatterySaver.setMode(BackendService.getModeBattery());
        BatterySaver.setVisibility(View.INVISIBLE);

        GPSProvider = (ControlSwitch) findViewById(R.id.gps_provider);
        GPSProvider.registerModes(SharedConstants.LiveGPS, SharedConstants.ReplayedGPS);
        GPSProvider.registerManager(this);
        GPSProvider.setMode(BackendService.getModeGPS());

        HeartBeatProvider = (ControlSwitch) findViewById(R.id.heartbeat_provider);
        HeartBeatProvider.registerModes(SharedConstants.ConnectedHeartBeat, SharedConstants.DisconnetedHeartBeat);
        HeartBeatProvider.registerManager(this);
        HeartBeatProvider.setMode(BackendService.getModeHeartBeat());

        SpeedThumb = BitmapFactory.decodeResource(getResources(), R.drawable.speed_thumb);
        HeartThumb = BitmapFactory.decodeResource(getResources(), R.drawable.heart_thumb);

        // Hardcoded settings for Speed in left Monitor
        LeftMonitor = (Monitor) findViewById(R.id.left_monitor);
        LeftMonitor.setIcon(SpeedThumb);
        LeftMonitor.setRuleSettings(10,4, 0.25f,0f,80f); // One Label  every 1 km/h
        LeftMonitor.setUnit("km/h");
        LeftMonitor.setVisibility(View.INVISIBLE);

        // Hardcoded settings for Heartbeat in right Monitor
        RightMonitor = (Monitor) findViewById(R.id.right_monitor);
        RightMonitor.setIcon(HeartThumb);
        RightMonitor.setRuleSettings(10,5,1f,20f,220f); // One Label every 5 bpm
        RightMonitor.setUnit("bpm");
        RightMonitor.setVisibility(View.INVISIBLE);

        Speeds = new ArrayList<Statistic>();
        HeartBeats = new ArrayList<Statistic>();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BackendService.setActivityMode(SharedConstants.SwitchBackground);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BackPressedCount = 0;
        BackendService = (DataManager) DataManager.getBackend();
        BackendService.setActivityMode(SharedConstants.SwitchForeground);
        GPSProvider.setMode(BackendService.getModeGPS());
        LightEnhancer.setMode(BackendService.getModeLight());
        BatterySaver.setMode(BackendService.getModeBattery());
        SleepLocker.setMode(BackendService.getModeSleep());

        // Force a refreshed display
        GeoData LastGPS = BackendService.getLastUpdate();
        if (null == LastGPS) return;
        processLocationChanged(LastGPS);
    }

    public void onStatusChanged(short Status) {
        if (Status == SharedConstants.SleepLocked) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            BackendService.storeModeSleep(Status);
            SleepLocker.setMode(Status);
            }
        if (Status == SharedConstants.SleepUnLocked) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            BackendService.storeModeSleep(Status);
            SleepLocker.setMode(Status);
        }

        if (Status == SharedConstants.LiveGPS) {
            BackendService.storeModeGPS(Status);
            GPSProvider.setMode(Status);
        }
        if (Status == SharedConstants.ReplayedGPS) {
            BackendService.storeModeGPS(Status);
            GPSProvider.setMode(Status);
        }

        if (Status == SharedConstants.LightEnhanced) {
            BackendService.storeModeLight(Status);
            LightEnhancer.setMode(Status);
        }
        if (Status == SharedConstants.LightNormal) {
            BackendService.storeModeLight(Status);
            LightEnhancer.setMode(Status);
        }

        if (Status == SharedConstants.BatteryDrainMode) {
            BackendService.storeModeBattery(Status);
            BatterySaver.setMode(Status);
        }
        if (Status == SharedConstants.BatterySaveMode) {
            BackendService.storeModeBattery(Status);
            BatterySaver.setMode(Status);
        }
    }

    @Override
    public void onBackPressed() {
        BackPressedCount++;
        if (BackPressedCount > 1) {
            BackendService.setActivityMode(SharedConstants.SwitchBackground);
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
        Speeds.add(new Statistic(geoInfo.getSpeed()*3.6f,geoInfo.getElapsedDays()));
        for (GeoData item: CollectedSelection) {
            Speeds.add(new Statistic(item.getSpeed()*3.6f,item.getElapsedDays()));
        }
        LeftMonitor.updateStatistics(Speeds);
        if (LeftMonitor.getVisibility() == View.INVISIBLE) LeftMonitor.setVisibility(View.VISIBLE);

    }
}
