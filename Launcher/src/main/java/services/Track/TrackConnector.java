package services.Track;

import android.os.Binder;

public class TrackConnector extends Binder {

    private String LogTag = this.getClass().getSimpleName();

    private TrackCommands Service = null;
    private TrackEvents Listener = null;

    public void RegisterService(TrackCommands Service) { this.Service = Service; }
    public void RegisterListener(TrackEvents Listener) { this.Listener = Listener; }

    /******************************************************************
     * Events implementations
     ******************************************************************/
    void LoadedGPX(boolean Status) {Listener.LoadedGPX(Status);}
    void Tracking(int TrackingEvent) { Listener.Tracking(TrackingEvent);}

    /******************************************************************
     * SensorCommands implementations
     ******************************************************************/
    void LoadGPX(String Filename) {Service.LoadGPX(Filename);}
    void SetClearance(double Clearance){Service.SetClearance(Clearance);}
    void EnableTracking(boolean Enabled) {Service.EnableTracking(Enabled); }

}
