package com.dustofcloud.daytodayrace;

public class BluetoothConstants {
    // Used During Scanning
    public  static final int TYPE_UUID16 = 0x3; // UUID id Expected Format
    public  static final String UUID_HEART_RATE = "180d"; // MIO GLOBAL LINK Heart Rate Service

    // Used by Bluetooth GATT connection
    public static  final String CHARACTERISTIC_HEART_RATE = "00002a37-0000-1000-8000-00805f9b34fb";
    public static  final String SERVICE_HEART_RATE = "0000180d-0000-1000-8000-00805f9b34fb";
    public static  final String DESCRIPTOR_HEART_RATE = "00002902-0000-1000-8000-00805f9b34fb";
}
