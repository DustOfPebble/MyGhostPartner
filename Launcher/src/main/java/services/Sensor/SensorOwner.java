package services.Sensor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

public class SensorOwner extends BluetoothGattCallback{

    private String LogTag = this.getClass().getSimpleName();

    private Context SavedContext;
    private AccessSensor SensorListener;
    private BluetoothDevice SelectedSensor = null;
    private BluetoothGatt SelectedSocket = null;

    public SensorOwner(AccessSensor Listener, Context context){
        SensorListener = Listener;
        SavedContext = context;
    }

    public void checkDevice(BluetoothDevice Sensor){
        if (Sensor == null) return;
        if (SelectedSensor != null) return;
        Sensor.connectGatt(SavedContext,false,this);
    }

    public void disconnect() {
        if (SelectedSocket == null ) return;
        SelectedSocket.disconnect();
    }

    /********************************************************************************************
     * Callback implementation for Bluetooth GATT
     *********************************************************************************************/
    @Override
    public void onConnectionStateChange(BluetoothGatt DeviceSocket, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            DeviceSocket.discoverServices();
            Log.d(LogTag, "Connected to Device --> Starting Services discovery");
            return;
        }
        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            BluetoothDevice DisconnectedDevice = DeviceSocket.getDevice();
            DeviceSocket.close();
            if (DisconnectedDevice != SelectedSensor) return;
            SensorListener.Removed();
            SelectedSensor = null;
            SelectedSocket = null;
            Log.d(LogTag, "Selected Device has disconnected.");
            return;
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt DeviceSocket, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(LogTag, "Services on Device discovered --> Checking for matching service");
            BluetoothGattService DeviceService = DeviceSocket.getService(UUIDs.SERVICE_HEART_RATE);
            if ( DeviceService == null ) {
                DeviceSocket.disconnect();
                Log.d(LogTag, "Device not providing expected service --> Disconnecting");
                return;
            }
            SensorListener.Selected();
            SelectedSocket = DeviceSocket;
            SelectedSensor = SelectedSocket.getDevice();
            Log.d(LogTag, "Matching service found on device:"+SelectedSensor.getAddress()+" --> Configuring device");

            BluetoothGattCharacteristic Monitor = DeviceService.getCharacteristic(UUIDs.CHARACTERISTIC_HEART_RATE);
            SelectedSocket.setCharacteristicNotification(Monitor,true);

            BluetoothGattDescriptor MonitorSpecs = Monitor.getDescriptor(UUIDs.DESCRIPTOR_HEART_RATE);
            MonitorSpecs.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            SelectedSocket.writeDescriptor(MonitorSpecs);
        }
     }

    @Override
    public void onCharacteristicChanged(BluetoothGatt DeviceSocket, BluetoothGattCharacteristic MonitoredValue) {
        int SensorValue = MonitoredValue.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
        SensorListener.Value(SensorValue);
        Log.d(LogTag, "Updating --> Value["+SensorValue+"]");
    }
}
