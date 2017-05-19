package services.Sensor;

import android.bluetooth.BluetoothDevice;

import services.Hub;

public class AccessSensor {

    private String LogTag = AccessSensor.class.getSimpleName();

    private SensorOwner SensorListener = null;
    private Detector SensorFinder = null;
    private int SensorSearchTimeOut; // in ms TimeOut
    private Hub Service = null;

    private int SensorStatus = SensorState.NotConnected;

    public AccessSensor(Hub Service, int TimeSearch){
        this.Service = Service;
        SensorSearchTimeOut = TimeSearch * 1000;
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
        Service.UpdatedBPM(Value);
    }

    public void Detected(BluetoothDevice DiscoveredSensor){
        if (DiscoveredSensor == null) return;
        SensorListener.checkDevice(DiscoveredSensor);
    }
    public void Selected(){
        SensorFinder.stopSearch();
        SensorStatus = SensorState.Connected;
        Service.UpdatedBPM(SensorStatus);
    }

    public void Failed(){
        SensorStatus = SensorState.NotConnected;
        Service.UpdatedBPM(SensorStatus);
    }

    public void Removed(){
        SensorStatus = SensorState.NotConnected;
        Service.UpdatedBPM(SensorStatus);
    }

    /**************************************************************
     *  Forwarded Queries from Service
     **************************************************************/
    public void SearchSensor() {
        if (SensorStatus == SensorState.Searching) return;
        SensorFinder.startSearch();
        SensorStatus = SensorState.Searching;
        Service.UpdatedBPM(SensorStatus);
    }

    public void Stop() {
        if (SensorStatus == SensorState.Searching) {
            SensorFinder.stopSearch();
            SensorStatus = SensorState.NotConnected;
            Service.UpdatedBPM(SensorStatus);
        }
        if (SensorStatus == SensorState.Connected) {
            SensorListener.disconnect();
        }
    }

    public int State() { return SensorStatus; }
}
