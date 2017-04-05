package dummy.core.dailyrace;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;

import lib.sensors.events.Events;
import lib.wrist.sensor.SensorDetector;
import lib.wrist.sensor.SensorManager;

public class SensorProvider implements Events, Runnable {

    private String LogTag = this.getClass().getSimpleName();

    private SensorManager SensorListener = null;
    private SensorDetector SensorFinder = null;

    private int SearchTimeOut = 60000; // in ms TimeOut

    private Handler Event = null;
    private int EventDelay = 1000; // in ms

    private int StoredValue = -1;
    private int Status = SensorState.NotConnected;

    public SensorProvider(Context context)
    {
        SensorListener = new SensorManager(this, context);
        SensorFinder = new SensorDetector(this, SearchTimeOut);
        // Start Event simulator
        Event = new Handler();
        //this.run();
    }

    public void connect(){
        SensorFinder.startSearch();
        Status = SensorState.Searching;
    }

    public int getValue(){
        return StoredValue;
    }

    public boolean isValid() {
        if (Status == SensorState.Connected) return true;
        else return false;
    }

    // CallBack on Frequency Updated
    @Override
    public void Updated(int Value) {
        StoredValue = Value;
    }

    // CallBack on Bluetooth Device detection
    @Override
    public void Detected(BluetoothDevice DiscoveredSensor){
        if (DiscoveredSensor == null) return;
        SensorListener.checkDevice(DiscoveredSensor);
    }
    @Override
    public void Selected(){
        SensorFinder.stopSearch();
        Status = SensorState.Connected;
    }

    @Override
    public void Failed(){
        SensorFinder.startSearch();
        Status = SensorState.NotConnected;
    }

    @Override
    public void Removed(){
        SensorFinder.startSearch();
        Status = SensorState.NotConnected;
    }

    @Override
    public void run() {
        Event.postDelayed(this, EventDelay);
    }
}
