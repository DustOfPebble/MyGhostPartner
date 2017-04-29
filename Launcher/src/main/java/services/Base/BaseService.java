package services.Base;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import core.GPS.EventsGPS;
import core.GPS.GPS;

public class BaseService extends Service implements BaseCommands, EventsGPS {

    private String LogTag = this.getClass().getSimpleName();

    private GPS Position = null;
    private BaseConnector Connector=null;

    private Base Storage = null;

    private Bundle EventSnapshot = null;

    public BaseService(){
        EventSnapshot = new Bundle();
        Connector = new BaseConnector();
        Storage = new Base();
    }



    /**************************************************************
     *  Callbacks implementation for Service management
     **************************************************************/
    @Override
    public void onCreate(){
        super.onCreate();

        Connector.RegisterService(this);

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
        super.onDestroy();
    }

    /**************************************************************
     *  Callbacks implementation for incoming commands
     **************************************************************/
    @Override
    public void Query() {
    }

    /**************************************************************
     *  Callbacks implementation for GPS Events
     ***************************************************************/
    @Override
    public void UpdateGPS(double Longitude, double Latitude){

    }
}
