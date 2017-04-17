package services.HeartSensor;

import android.os.Binder;
import android.util.Log;

public class ServiceAccess extends Binder {

    private String LogTag = this.getClass().getSimpleName();

    private Commands Service = null;
    private UpdateEvents Updater = null;

    public void RegisterProvider(Commands Provider) { Service = Provider; }
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
