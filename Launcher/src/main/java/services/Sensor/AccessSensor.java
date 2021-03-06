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
    void Value(int Value) {
        Service.UpdatedBPM(Value);
    }

    void Detected(BluetoothDevice DiscoveredSensor){
        if (DiscoveredSensor == null) return;
        SensorListener.checkDevice(DiscoveredSensor);
    }
    void Selected(){
        SensorFinder.stopSearch();
        SensorStatus = SensorState.Connected;
        Service.UpdatedBPM(SensorStatus);
    }

    void Failed(){
        SensorStatus = SensorState.NotConnected;
        Service.UpdatedBPM(SensorStatus);
    }

    void Removed(){
        SensorStatus = SensorState.NotConnected;
        Service.UpdatedBPM(SensorStatus);
    }

    /**************************************************************
     *  Forwarded Queries from Service
     **************************************************************/
    public void startSearch() {
        if (SensorStatus == SensorState.Searching) return;
        SensorFinder.startSearch();
        SensorStatus = SensorState.Searching;
    }

    public void stopSearch() {
        if (SensorStatus == SensorState.Searching) {
            SensorFinder.stopSearch();
            SensorStatus = SensorState.NotConnected;
        }
        if (SensorStatus == SensorState.Connected) {
            SensorListener.disconnect();
        }
    }
}
