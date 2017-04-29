package services.Sensor;

import java.util.UUID;

public class UUIDs {
    static final String BluetoothReserved = "0000-1000-8000-00805f9b34fb";
    static final UUID CHARACTERISTIC_HEART_RATE = UUID.fromString("00002a37-"+BluetoothReserved);
    static final UUID SERVICE_HEART_RATE = UUID.fromString("0000180d-"+BluetoothReserved);
    static final UUID  DESCRIPTOR_HEART_RATE = UUID.fromString("00002902-"+BluetoothReserved);
}
