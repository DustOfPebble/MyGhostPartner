package services.Sensor;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class SensorService extends Service implements SensorEvents, SensorCommands {

    private String LogTag = this.getClass().getSimpleName();

    private Manager SensorListener = null;
    private Detector SensorFinder = null;
    private int SensorSearchTimeOut = 60000; // in ms TimeOut

    private SensorConnector Connector=null;

    private int ServiceStatus = States.Waiting;
    private Bundle SensorSnapshot = null;

    public SensorService(){
        SensorSnapshot = new Bundle();
        Connector = new SensorConnector();
    }

    /**************************************************************
     *  Callbacks implementation for
     *  - Sensor detection
     *  - Sensor selection
     *  - Sensor update value
     *  - Sensor disconnection
     **************************************************************/
    @Override
    public void Updated(int Value) {
        SensorSnapshot.clear();
        SensorSnapshot.putInt(SensorStateKeys.UpdatingValue, Value);
        Connector.Update(Value);
    }

    @Override
    public void Detected(BluetoothDevice DiscoveredSensor){
        if (DiscoveredSensor == null) return;
        SensorListener.checkDevice(DiscoveredSensor);
    }
    @Override
    public void Selected(){
        SensorFinder.stopSearch();

        ServiceStatus = States.Running;

        Connector.StateChanged(ServiceStatus);

        SensorSnapshot.clear();
        SensorSnapshot.putBoolean(SensorStateKeys.isSelected, true);
    }

    @Override
    public void Failed(){
        ServiceStatus = States.Waiting;

        Connector.StateChanged(ServiceStatus);

        SensorSnapshot.clear();
        SensorSnapshot.putBoolean(SensorStateKeys.isSelected, false);
    }

    @Override
    public void Removed(){
        ServiceStatus = States.Waiting;

        Connector.StateChanged(ServiceStatus);

        SensorSnapshot.clear();
        SensorSnapshot.putBoolean(SensorStateKeys.isSelected, false);
    }

    /**************************************************************
     *  Callbacks implementation for Service management
     **************************************************************/
    @Override
    public void onCreate(){
        super.onCreate();

        SensorListener = new Manager(this, getBaseContext());
        SensorFinder = new Detector(this, SensorSearchTimeOut);
        Connector.RegisterService(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LogTag, "Executing Start command");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LogTag, "Binding service");
        return Connector;
    }

    @Override
    public void onDestroy() {
        Log.d(LogTag, "Service is exiting !");
        super.onDestroy();
    }


    /**************************************************************
     *  Callbacks implementation for incoming messages
     **************************************************************/
    @Override
    public void SearchSensor() {
        if (ServiceStatus == States.Searching) return;
        SensorFinder.startSearch();
        ServiceStatus = States.Searching;

        Connector.StateChanged(ServiceStatus);
    }

    @Override
    public void Stop() {
        if (ServiceStatus == States.Searching) {
            SensorFinder.stopSearch();
            ServiceStatus = States.Waiting;
            Connector.StateChanged(ServiceStatus);
        }
        if (ServiceStatus == States.Running) {
            SensorListener.disconnect();
        }
    }

    @Override
    public void Query() {
        Connector.StateChanged(ServiceStatus);
    }

}
