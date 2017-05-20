package services.Track;

import android.util.Log;

import java.io.File;

import core.Files.Loader;
import core.Files.LoaderEvents;
import core.Files.LoaderGPX;
import core.Files.LoaderJSON;
import core.Files.PreSets;
import core.Files.SavedObject;
import core.GPS.CoordsGPS;
import core.GPS.CoreGPS;
import core.GPS.EventsGPS;
import core.Structures.Coords2D;
import core.Structures.Sample;
import services.Hub;

public class AccessTrack implements EventsGPS, LoaderEvents {

    private String LogTag = this.getClass().getSimpleName();

    private Hub Service;

    private Loader Parser = null;

    private CoordsGPS Origin = null;
    private Coords2D LastMove = null;

    private Track TrackFinder = null;

    private boolean isTracking = false;

    private double Clearance = 0.0;

    public AccessTrack(Hub Service, int Clearance) {
        this.Service = Service;
        this.Clearance = (double) Clearance;
    }

     /**************************************************************
     *  Forwarded calls from Service
     **************************************************************/
    public void Load(SavedObject Source, int Mode) {
        TrackFinder = new Track();

        if (Mode == PreSets.JSON) {
            Parser = new LoaderJSON(Source, this);
        }

        if (Mode == PreSets.GPX) {
            Parser = new LoaderGPX(Source, this);
        }
    }

     public void EnableTracking(boolean Enabled) { isTracking = Enabled;}

    /**************************************************************
     *  Callbacks implementation for CoreGPS Events
     ***************************************************************/
    @Override
    public void UpdatedGPS(CoreGPS Provider){
        if (Parser == null) return;

        if (Origin != Provider.Origin()) {
            Origin = Provider.Origin();
        }

        if (Parser.Status() == Loader.waiting) Parser.start();

        if (!isTracking) return;
        TrackFinder.search(Provider.Moved(), Clearance);
    }
    /**************************************************************
     *  Direct Calls from Track Events
     **************************************************************/
     public void Tracking(int TrackingEvent) {
         Log.d(LogTag,"Tracking result is "+((TrackingEvent == Status.OnTrack)? "[On track]":"[Not found]"));
         TrackFinder.setSearchMode(false);
     }

    /**************************************************************
     *  Callbacks implementation for Loader Events
     **************************************************************/
    @Override
    public void loaded(Sample stored) {
        if (LastMove == null) {
            LastMove = stored.MovedFrom(Origin);
            return;
        }

        Coords2D NewMove = stored.MovedFrom(Origin);
        TrackFinder.append(LastMove,NewMove);
        LastMove = NewMove;
    }

    @Override
    public void finished(boolean Success) {
        Log.d(LogTag, "Loading Track finished --> "+(Success? "[Success]":"[Failed]"));
    }
}