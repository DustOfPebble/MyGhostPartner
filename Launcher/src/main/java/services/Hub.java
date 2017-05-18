package services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import core.GPS.CoreGPS;
import core.GPS.EventsGPS;
import core.Settings.Parameters;
import core.Structures.Frame;
import core.Structures.Node;
import services.Database.AccessDB;
import services.Recorder.AccessLogs;
import services.Sensor.AccessSensor;
import services.Sensor.SensorState;
import services.Track.AccessTrack;

public class Hub extends Service implements Queries, EventsGPS {

    private static String LogTag = Hub.class.getSimpleName();

    private Junction Connector = null;

    private AccessSensor Sensor = null;
    private AccessLogs Recorder = null;
    private AccessDB Database =  null;
    private AccessTrack Tracker = null;

    private CoreGPS Position = null;

    public Hub() {
        Connector = new Junction();
    }

    /**************************************************************
     *  Callbacks implementation for Service management
     **************************************************************/
    @Override
    public void onCreate() {
        super.onCreate();
        Connector.RegisterService(this);
        Position = new CoreGPS(getBaseContext(), Parameters.TimeUpdateGPS);
        Position.setBPM(SensorState.NotConnected);

        Sensor = new AccessSensor(this, Parameters.TimeSearchLimit);
        Recorder = new AccessLogs(this, Parameters.LogClearance);
        Database =  new AccessDB(this, Parameters.LiveClearance);
        Tracker = new AccessTrack(this, Parameters.TrackClearance);

        Position.addListener(Recorder);
        Position.addListener(Database);
        Position.addListener(Tracker);
        Position.addListener(this);
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
     *  Callbacks implementation for incoming Queries
     **************************************************************/
    @Override
    public void GPS(boolean Enabled) {
        if (Enabled) Position.start();
        else Position.stop();
    }

    @Override
    public void setLog(int Mode) { Recorder.Log(Mode); }

    @Override
    public void startSensor() { Sensor.SearchSensor(); }
    @Override
    public void stopSensor() { Sensor.Stop(); }

    @Override
    public void selectTrack(File Source, int Mode) { Tracker.Load(Source, Mode); }
    @Override
    public void setTracking(boolean Enabled) { Tracker.EnableTracking(Enabled); }

    @Override
    public ArrayList<Node> getNodes(Frame Zone){ return Database.getNodes(Zone); }

    @Override
    public void reload(){ Database.reload(); }

    /**************************************************************
     *  Callbacks From AccessSensor
     **************************************************************/
    public void Update(int SensorValue) {
        Position.setBPM(SensorValue);
        Connector.UpdateBPM(SensorValue);
    }

    /**************************************************************
     *  Callbacks From AccessDB
     **************************************************************/
    public void OutOfRange() { Connector.OutOfRange(); }
    /**************************************************************
     *  Callbacks implementation for CoreGPS Events
     ***************************************************************/
    @Override
    public void UpdatedGPS(CoreGPS Provider) { Connector.UpdateGPS(Provider);}

}