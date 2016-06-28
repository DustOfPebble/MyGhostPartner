package com.dustofcloud.dailyrace;

import android.os.Build;

public class BluetoothConstants {

    // Bluetooth Low Energy Enabled
    public static final boolean isLowEnergy =(android.os.Build.VERSION.SDK_INT >=  Build.VERSION_CODES.JELLY_BEAN_MR2);

    // Searching Timeout
    public static int SCAN_TIMEOUT = 10000; // 60 seconds

    // Used During Scanning
    public  static final int TYPE_UUID16 = 0x3; // UUID id Expected Format
    public  static final String UUID_HEART_RATE = "180d"; // MIO GLOBAL LINK Heart Rate Service

    // Used by Bluetooth GATT connection
    public static  final String CHARACTERISTIC_HEART_RATE = "00002a37-0000-1000-8000-00805f9b34fb";
    public static  final String SERVICE_HEART_RATE = "0000180d-0000-1000-8000-00805f9b34fb";
    public static  final String DESCRIPTOR_HEART_RATE = "00002902-0000-1000-8000-00805f9b34fb";
}
