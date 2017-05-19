package services;

import android.os.Binder;

import java.io.File;
import java.util.ArrayList;

import core.GPS.CoreGPS;
import core.Structures.Frame;
import core.Structures.Node;


public class Junction extends Binder {

    private String LogTag = Junction.class.getSimpleName();

    private Queries Service = null;
    private Signals Listener = null;

    public void RegisterService(Queries Service) { this.Service = Service; }
    public void RegisterListener(Signals Listener) { this.Listener = Listener; }

    /******************************************************************
     * Forwarding Service Events application
     ******************************************************************/
    public void TrackLoaded(boolean Success) { Listener.TrackLoaded(Success);}
    public void TrackEvent(int Distance) { Listener.TrackEvent(Distance);}

    public void UpdatedSensor(int Value) { Listener.UpdatedSensor(Value);}

    public void UpdateGPS(CoreGPS Provider) { Listener.UpdatedGPS(Provider);}

    public void OutOfRange() { Listener.OutOfRange();}

    /******************************************************************
     * Handling Queries to Services
     ******************************************************************/
    public void GPS(boolean Enabled) {Service.GPS(Enabled); }

    public void setLog(int Mode) {Service.setLog(Mode);}

    public void startSensor() {Service.startSensor();}
    public void stopSensor() {Service.stopSensor();}

    public void selectTrack(File Source, int Mode) {Service.selectTrack(Source, Mode); }
    public void setTracking(boolean Enabled) { Service.setTracking(Enabled); }

    public ArrayList<Node> getNodes(Frame Zone){ return Service.getNodes(Zone); }
    public void reload() { Service.reload();}

}
