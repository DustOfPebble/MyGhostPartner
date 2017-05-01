package services.Base;

import android.os.Binder;

import java.util.ArrayList;

import core.Structures.Frame;
import core.Structures.Statistic;

public class BaseConnector extends Binder {

    private String LogTag = this.getClass().getSimpleName();

    private BaseCommands Service = null;
    private BaseEvents Listener = null;

    public void RegisterService(BaseCommands Service) { this.Service = Service; }
    public void RegisterListener(BaseEvents Listener) { this.Listener = Listener; }

    /******************************************************************
     * SensorCommands implementations
     ******************************************************************/
    void EnableGPS(boolean Enabled) {Service.EnableGPS(Enabled); }
    public ArrayList<Statistic> getStatistics(Frame Zone) { return Service.getStatistics(Zone); }
    /******************************************************************
     * Events implementations
     ******************************************************************/
    void NotStored() { Listener.NotStored(); }

}
