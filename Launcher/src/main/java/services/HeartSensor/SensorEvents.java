package services.HeartSensor;

import android.bluetooth.BluetoothDevice;

/*****************************************************************************
 *  This interface is shared between
 *   - Wrist Sensor based on Smart Bluetooth
 *   - Chest Sensor (Not available) based standard bluetooth
 *****************************************************************************/

public interface SensorEvents {
    void Detected(BluetoothDevice Sensor);
    void Failed();
    void Removed();
    void Updated(int Frequency);
    void Selected();
}

