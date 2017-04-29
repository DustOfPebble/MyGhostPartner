package services.Sensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.BluetoothLeScanner;

import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.util.Log;
import java.util.List;


public class Detector extends ScanCallback implements Runnable {

    private String LogTag = this.getClass().getSimpleName();

    private int TimeOut = 1000; // default TimeOut in ms ...
    private Handler TriggerEvent;
    private SensorEvents SensorHandler;
    private BluetoothLeScanner DeviceScanner;
    private Boolean isScanning = false;

    public Detector(SensorEvents Callback, int  TimeOut) {
        SensorHandler = Callback;
        TriggerEvent = new Handler();
        if (TimeOut > 1000) this.TimeOut = TimeOut;
        DeviceScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    }

    // Start scanning devices
    public void startSearch(){
        DeviceScanner.startScan(this);
        isScanning = true;
        if (TimeOut != 0) TriggerEvent.postDelayed(this, TimeOut);
        Log.d(LogTag, "Start scanning for "+TimeOut+" ms");
    }

    // Stop scanning on TimeOut --> TerminateSearch
    public void run() {
        if (!isScanning) return;
        isScanning = false;
        DeviceScanner.stopScan(this);
        DeviceScanner.flushPendingScanResults(this);
        SensorHandler.Failed();
        Log.d(LogTag, "Scanning reached TimeOut limit");
    }

    // Stop scanning on Request...
    public void stopSearch() {
        isScanning = false;
        DeviceScanner.stopScan(this);
        DeviceScanner.flushPendingScanResults(this);
        SensorHandler.Failed();
        TriggerEvent.removeCallbacks(this);
        Log.d(LogTag, "Scanning requested to stop");
    }

    @Override
    public void onScanResult(int callbackType, ScanResult Infos) {
         BluetoothDevice SensorDevice = Infos.getDevice();
        if (SensorDevice != null ) {
            Log.d(LogTag, "Device detected --> Forwarding device to device Manager");
            SensorHandler.Detected(SensorDevice);
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.d(LogTag, "Scan failed with Error["+errorCode+"]");
    }

}

