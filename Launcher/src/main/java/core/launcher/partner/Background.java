package core.launcher.partner;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;

import core.Settings.Parameters;
import core.Structures.Coords2D;
import core.GPS.CoreGPS;
import core.GPS.EventsGPS;
import services.Database.AccessDB;
import services.Track.AccessTrack;

class Background implements ServiceConnection, EventsGPS {

    private String LogTag = this.getClass().getSimpleName();

    private Bitmap Map;
    private CoreGPS Position = null;
    private Coords2D Shifted = null;

    private static AccessDB LiveStorage = null;
    private static AccessTrack LiveTracking = null;

    public Background(Context Owner) {

        Intent ServiceStarter;

        ServiceStarter = new Intent(Owner, AccessDB.class);
        Owner.bindService(ServiceStarter, this, 0);

        ServiceStarter = new Intent(Owner, AccessTrack.class);
        Owner.bindService(ServiceStarter, this, 0);

        Position = new CoreGPS(Owner, Parameters.TimeUpdateGPS);
    }

    public Bitmap Map() {return Map;}
    /************************************************************************
     * Managing GPS Events
     * **********************************************************************/
    @Override
    public void UpdatedGPS(CoreGPS Provider) {
        Shifted = Provider.Moved();
    }
    /************************************************************************
     * Managing connection to Service
     * **********************************************************************/
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(LogTag, "Connected to " + name.getClassName() + " Service");

        if (AccessDB.class.getName().equals(name.getClassName())) {
            LiveStorage = (AccessDB) service;
        }

        if (AccessTrack.class.getName().equals(name.getClassName())) {
            LiveTracking = (AccessTrack) service;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(LogTag, "Disconnected from " + name.getClassName()  + " Service");

        if (AccessDB.class.getName().equals(name.getClassName())) {
            LiveStorage = null;
        }

        if (AccessTrack.class.getName().equals(name.getClassName())) {
            LiveTracking = null;
        }
    }
}
