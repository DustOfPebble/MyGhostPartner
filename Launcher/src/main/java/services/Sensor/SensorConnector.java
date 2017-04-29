package services.Sensor;

import android.os.Binder;
import android.util.Log;

public class SensorConnector extends Binder {

    private String LogTag = this.getClass().getSimpleName();

    private SensorCommands Service = null;
    private UpdateEvents Updater = null;

    public void RegisterService(SensorCommands Service) { this.Service = Service; }
    public void RegisterListener(UpdateEvents Listener) { Updater = Listener; }

    public void SearchSensor() { Service.SearchSensor(); }
    public void Stop() { Service.Stop(); }
    public void Query() { Service.Query(); }

    public void Update(int Value) {
        try { Updater.Update(Value);}
        catch (Exception Failed) { Log.d(LogTag, "Failed on Update event");}
    }

    public void StateChanged(int State) {
        try {Updater.StateChanged(State); }
        catch (Exception Failed) { Log.d(LogTag, "Failed on StateChanged event");}
    }

}
