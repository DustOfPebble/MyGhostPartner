package com.dustofcloud.daytodayrace;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.util.Log;

import java.util.Arrays;


public class SensorFinder implements BluetoothAdapter.LeScanCallback, Runnable {

    private BluetoothAdapter Bluetooth;
    private HeartBeatProvider Listener;

    private int TimeOut = 0; // 0 means scanning forever ..
    private Handler EventTrigger;

    @Override
    public void run() {
        Bluetooth.stopLeScan(this);
        Listener.setDevice(null); // ==> Null means TimeOut reached ...
    }

    public SensorFinder(HeartBeatProvider Client) {
        Listener = Client;
        EventTrigger = new Handler();
        Bluetooth = BluetoothAdapter.getDefaultAdapter();
    }

    public void findSensor(int Timeout){
        Bluetooth.startLeScan(this);
        EventTrigger.postDelayed(this, TimeOut);
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
                Log.d("UUID", UUID);
                if (BluetoothConstants.UUID_HEART_RATE.equals(UUID)) {
                    Log.d("Bluetooth ====>", "Device found");
                    Listener.setDevice(DeviceFound);
                    break;
                }
            }
            //Advance
            index += length;
        }
    }

}

