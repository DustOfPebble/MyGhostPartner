package services.HeartSensor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class SensorsProvider extends Service implements SensorEvents, Commands {

    private String LogTag = this.getClass().getSimpleName();

    private NotificationManager InfoProvider;
    private Notification.Builder InfoCreator;

    private SensorManager SensorListener = null;
    private SensorDetector SensorFinder = null;
    private int SensorSearchTimeOut = 60000; // in ms TimeOut

    private ServiceAccess Connector=null;

    private int ServiceStatus = States.Waiting;
    private Bundle SensorSnapshot = null;

    public SensorsProvider(){
        SensorSnapshot = new Bundle();
        Connector = new ServiceAccess();
    }

    private void PushSystemNotification() {
        int  Info = -1;
        if (ServiceStatus == States.Waiting) Info = R.string.WaitingMode;
        if (ServiceStatus == States.Running) Info = R.string.RunningMode;
        if (ServiceStatus == States.Searching) Info = R.string.SearchingMode;

        InfoCreator.setContentText(getText(Info));
        InfoProvider.notify(R.string.ID,InfoCreator.build());
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
        PushSystemNotification();

        Connector.StateChanged(ServiceStatus);

        SensorSnapshot.clear();
        SensorSnapshot.putBoolean(SensorStateKeys.isSelected, true);
    }

    @Override
    public void Failed(){
        ServiceStatus = States.Waiting;
        PushSystemNotification();

        Connector.StateChanged(ServiceStatus);

        SensorSnapshot.clear();
        SensorSnapshot.putBoolean(SensorStateKeys.isSelected, false);
    }

    @Override
    public void Removed(){
        ServiceStatus = States.Waiting;
        PushSystemNotification();

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
        InfoProvider = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        InfoCreator = new Notification.Builder(this);
        InfoCreator.setSmallIcon(R.drawable.icon_heartspy);
        InfoCreator.setContentTitle(getText(R.string.ServiceName));

        PushSystemNotification();

        SensorListener = new SensorManager(this, getBaseContext());
        SensorFinder = new SensorDetector(this, SensorSearchTimeOut);
        Connector.RegisterProvider(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LogTag, "Starting service ...");

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LogTag, "Binding service ...");
        return Connector;
    }

    @Override
    public void onDestroy() {
        Log.d(LogTag, "Service is about to quit !");
        InfoProvider.cancel(R.string.ID);
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
        PushSystemNotification();

        Connector.StateChanged(ServiceStatus);
    }

    @Override
    public void Stop() {
        if (ServiceStatus == States.Searching) {
            SensorFinder.stopSearch();
            ServiceStatus = States.Waiting;
            PushSystemNotification();
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
