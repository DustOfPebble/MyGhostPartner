package com.dustcloud.dailyrace;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.util.Log;
import java.util.Arrays;

@SuppressLint("NewApi")
public class SensorFinder implements BluetoothAdapter.LeScanCallback, Runnable {

    private BluetoothAdapter Bluetooth;
    private HeartBeatProvider Listener;
    private boolean isScanning;
    private Handler EventTrigger;

    @Override
    public void run() {
        Log.d("SensorFinder", "Timeout reached...");
        Bluetooth.stopLeScan(this);
        isScanning = false;
    }

    public SensorFinder(HeartBeatProvider Client) {
        Listener = Client;
        EventTrigger = new Handler();
        isScanning = false;
        Bluetooth = BluetoothAdapter.getDefaultAdapter();
    }

    public void findSensor(int Timeout){
        if (isScanning) return;
        Bluetooth.startLeScan(this);
        isScanning = true;
        EventTrigger.postDelayed(this, Timeout);
    }

    @Override
    public void onLeScan(final BluetoothDevice DeviceFound, final int RSSI, final byte[] scanRecord) {
        int index = 0;
        while (index < scanRecord.length) {

            int length = scanRecord[index++];
            if (length == 0) break; //Done once we run out of records

            int type = scanRecord[index];
            if (type == 0) break; //Done if our record isn't a valid type

            if (type == BluetoothConstants.TYPE_UUID16) {
                byte[] data = Arrays.copyOfRange(scanRecord, index + 1, index + length);
                int uuid = (data[1] & 0xFF) << 8;
                uuid += (data[0] & 0xFF);
                String UUID = Integer.toHexString(uuid);
                if (BluetoothConstants.UUID_HEART_RATE.equals(UUID)) {
                    Listener.setDevice(DeviceFound);
                    Log.d("SensorFinder", "HeartBeat sensor found...");
                    Bluetooth.stopLeScan(this);
                    EventTrigger.removeCallbacks(this);
                    break;
                }
            }
            //Advance
            index += length;
        }
    }

}

