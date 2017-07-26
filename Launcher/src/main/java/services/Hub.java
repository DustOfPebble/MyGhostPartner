package services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

import core.Files.SavedObject;
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
    private AccessDB Collector =  null;
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
        Collector =  new AccessDB(this, Parameters.LiveClearance);
        Tracker = new AccessTrack(this, Parameters.TrackClearance);

        Position.addListener(Recorder);
        Position.addListener(Collector);
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
    public void Logger(int Mode) { Recorder.Log(Mode); }

    @Override
    public void Sensor(boolean Enabled) {
        if (Enabled) Sensor.SearchSensor();
        else Sensor.Stop();
    }

    @Override
    public void selectTrack(SavedObject Source, int Mode) { Tracker.Load(Source, Mode); }
    @Override
    public void setTracking(boolean Enabled) { Tracker.EnableTracking(Enabled); }

    @Override
    public ArrayList<Node> getNodesByZone(Frame Zone){ return Collector.getNodes(Zone); }

    @Override
    public ArrayList<Node> getNodesByDelay(int Delay){ return Recorder.getNodes(Delay); }
    /**************************************************************
     *  Callbacks From AccessSensor
     **************************************************************/
    public void UpdatedBPM(int SensorValue) {
        Position.setBPM(SensorValue);
        Connector.UpdatedSensor(SensorValue);
    }

    /**************************************************************
     *  Callbacks From AccessDB
     **************************************************************/
    public void NotInZone() {
        Position.reset();
        Collector.reload();
    }
    /**************************************************************
     *  Callbacks implementation for CoreGPS Events
     ***************************************************************/
    @Override
    public void UpdatedGPS(CoreGPS Provider) { Connector.UpdateGPS(Provider);}

}