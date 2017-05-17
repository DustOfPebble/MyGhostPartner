package services.Sensor;

import android.bluetooth.BluetoothDevice;

import services.Hub;

public class AccessSensor {

    private String LogTag = AccessSensor.class.getSimpleName();

    private SensorOwner SensorListener = null;
    private Detector SensorFinder = null;
    private int SensorSearchTimeOut = 60000; // in ms TimeOut
    private Hub Service = null;

    private int SensorStatus = SensorState.NotConnected;

    public AccessSensor(Hub Service){
        this.Service = Service;
        SensorListener = new SensorOwner(this, this.Service);
        SensorFinder = new Detector(this, SensorSearchTimeOut);
    }

    /**************************************************************
     *  Callbacks implementation from Detector
     *  - Sensor detection
     *  - Sensor selection
     *  - Sensor update value
     *  - Sensor disconnection
     **************************************************************/
    public void Value(int Value) {
        Service.Update(Value);
    }

    public void Detected(BluetoothDevice DiscoveredSensor){
        if (DiscoveredSensor == null) return;
        SensorListener.checkDevice(DiscoveredSensor);
    }
    public void Selected(){
        SensorFinder.stopSearch();
        SensorStatus = SensorState.Connected;
        Service.Update(SensorStatus);
    }

    public void Failed(){
        SensorStatus = SensorState.NotConnected;
        Service.Update(SensorStatus);
    }

    public void Removed(){
        SensorStatus = SensorState.NotConnected;
        Service.Update(SensorStatus);
    }

    /**************************************************************
     *  Forwarded Queries from Service
     **************************************************************/
    public void SearchSensor() {
        if (SensorStatus == SensorState.Searching) return;
        SensorFinder.startSearch();
        SensorStatus = SensorState.Searching;
        Service.Update(SensorStatus);
    }

    public void Stop() {
        if (SensorStatus == SensorState.Searching) {
            SensorFinder.stopSearch();
            SensorStatus = SensorState.NotConnected;
            Service.Update(SensorStatus);
        }
        if (SensorStatus == SensorState.Connected) {
            SensorListener.disconnect();
        }
    }

    public int State() { return SensorStatus; }
}
