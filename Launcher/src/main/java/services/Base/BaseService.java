package services.Base;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

import core.GPS.EventsGPS;
import core.GPS.CoreGPS;
import core.Structures.Frame;
import core.Structures.Statistic;

public class BaseService extends Service implements BaseCommands, EventsGPS {

    private String LogTag = this.getClass().getSimpleName();

    private CoreGPS Position = null;
    private BaseConnector Connector=null;

    private Base DB = null;

    public BaseService(){
        Connector = new BaseConnector();
        DB = new Base();
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
     *  Callbacks implementation for incoming commands
     **************************************************************/
    @Override
    public ArrayList<Statistic> getStatistics(Frame Zone) {
        DB.collect(Zone);
        return DB.Collected;
    }
    @Override
    public void EnableGPS(boolean Enabled) {
        if (Enabled) Position.start();
        else Position.stop();
    }

    /**************************************************************
     *  Callbacks implementation for CoreGPS Events
     ***************************************************************/
    @Override
    public void UpdateGPS(Location location){
        Statistic set = new Statistic(location);
        if (!DB.belongs(set)) {
            Connector.NotStored();
            return;
        }
    }
}
