package services;

import android.os.Binder;

import java.util.ArrayList;

import core.Files.SavedObject;
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
    public void TrackLoaded(boolean Success) { Listener.UpdateTracking(Success);}
    public void TrackEvent(int Distance) { Listener.TrackEvent(Distance);}

    public void UpdatedSensor(int Value) { Listener.UpdatedSensor(Value);}
    public void UpdateGPS(CoreGPS Provider) { Listener.UpdatedGPS(Provider);}

    /******************************************************************
     * Handling Queries to Services
     ******************************************************************/
    public void selectTrack(SavedObject Source, int Mode) {Service.selectTrack(Source, Mode); }
    public void setTracking(boolean Enabled) { Service.setTracking(Enabled); }

    public void setGPS(boolean Enabled) {Service.GPS(Enabled); }
    public void setSensor(boolean Enabled) {Service.Sensor(Enabled);}
    public void setLogger(int Mode) {Service.Logger(Mode);}


    public ArrayList<Node> getNodesByZone(Frame Zone){ return Service.getNodesByZone(Zone); }
    public ArrayList<Node> getNodesByDelay(int Delay){ return Service.getNodesByDelay(Delay); }

}
