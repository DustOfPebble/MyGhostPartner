package services.Track;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import core.GPS.CoreGPS;
import core.GPS.EventsGPS;
import core.Structures.Node;
import core.launcher.application.SmartwatchConstants;
import lib.smartwatch.WatchLink;


public class TrackService extends Service implements TrackCommands, EventsGPS {

    private String LogTag = this.getClass().getSimpleName();

    private WatchLink Watch = null;

    private TrackConnector Connector = null;
    private Track TrackFinder = null;
    private CoreGPS Position = null;
    private boolean isTracking = false;

    private double Clearance = 0.0;

    public TrackService() {
        Connector = new TrackConnector();
    }

    /**************************************************************
     *  Callbacks implementation for Service management
     **************************************************************/
    @Override
    public void onCreate() {
        super.onCreate();

        Connector.RegisterService(this);
        Watch = new WatchLink(getBaseContext(), SmartwatchConstants.WatchUUID);
        Position = new CoreGPS(getBaseContext(), this);
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
    public void LoadGPX(String Filename) {
        TrackFinder = new Track();
        TrackFinder.setLoader(new LoaderGPX(TrackFinder, Filename));
        TrackFinder.setListener(this);
        TrackFinder.Load();
    }

    @Override
    public void SetClearance(double Clearance) { this.Clearance = Clearance;}

    @Override
    public void EnableGPS(boolean Enabled) {
        if (Enabled) Position.start();
        else Position.stop();
    }

    @Override
    public void EnableTracking(boolean Enabled) { isTracking = Enabled;}

    /**************************************************************
     *  Callbacks implementation for CoreGPS Events
     ***************************************************************/
    @Override
    public void UpdateGPS(Location location){
        if (!isTracking) return;
        TrackFinder.search(new Node(location.getLongitude(), location.getLatitude()),Clearance);
    }
    /**************************************************************
     *  Direct Calls from Track Events
     **************************************************************/
     public void Tracking(int TrackingEvent) {
         Log.d(LogTag,"Search result is "+((TrackingEvent == TrackFinder.OnTrack)? "On track":"Not found"));
         TrackFinder.setSearchMode(false);
     }

}