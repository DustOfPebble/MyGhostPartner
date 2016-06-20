package com.dustofcloud.daytodayrace;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import java.util.UUID;

public class HeartBeatProvider extends BluetoothGattCallback{

    private DataManager Backend;


    private BluetoothGatt DataProvider;
    private BluetoothDevice Sensor = null;
    private BluetoothGattCharacteristic Monitor;

    private SensorFinder Finder = null;

    public HeartBeatProvider(DataManager Client){
        Sensor = null;
        Backend = Client;
        Finder = new SensorFinder(this);
        Finder.findSensor(60);
    }

    public void setDevice(BluetoothDevice Sensor){
        if (Sensor == null) return;
        this.Sensor = Sensor;
        DataProvider = this.Sensor.connectGatt(Backend,false, this);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt GATT_Server, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            DataProvider.discoverServices();
            Backend.processHeartBeatChanged(0);
        }
        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Backend.processHeartBeatChanged(-1);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt GATT_Server, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {

            BluetoothGattService GATT_Service = GATT_Server.getService(UUID.fromString(BluetoothConstants.SERVICE_HEART_RATE));
            Monitor = GATT_Service.getCharacteristic(UUID.fromString(BluetoothConstants.CHARACTERISTIC_HEART_RATE));
            GATT_Server.setCharacteristicNotification(Monitor,true);
            BluetoothGattDescriptor MonitorSpecs = Monitor.getDescriptor(UUID.fromString(BluetoothConstants.DESCRIPTOR_HEART_RATE));
            MonitorSpecs.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            GATT_Server.writeDescriptor(MonitorSpecs);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt GATT_Server, BluetoothGattCharacteristic MonitoredValue) {
        Backend.processHeartBeatChanged(MonitoredValue.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1));
    }
};
