package core.GPS;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class CoreGPS implements LocationListener {

    private LocationManager SensorGPS = null;
    private Context Owner = null;
    private EventsGPS Listener = null;

    public CoreGPS(Context Owner, EventsGPS Listener) {
        this.Listener = Listener;
        this.Owner =  Owner;
    }
    @SuppressWarnings({"MissingPermission"})
    public void start() {
        if (SensorGPS == null) SensorGPS = (LocationManager) Owner.getSystemService(Context.LOCATION_SERVICE);
        SensorGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
    }

    public void stop() {
        SensorGPS.removeUpdates(this);
    }

     @Override
    public void onLocationChanged(Location location) {
         Listener.UpdateGPS(location);
     }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) {  }

    @Override
    public void onProviderDisabled(String provider) { }
}
