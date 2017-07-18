package services.Track;

import java.util.ArrayList;
import core.Structures.Coords2D;
import core.Structures.Frame;

public class Track implements Runnable {
    private ArrayList<Segment> Segments;
    private ArrayList<Segment> Collected;
    private int Index = -1;

    private Coords2D Coords = null;
    private double Clearance = 0.0;
    private static boolean isSearching = false;

    private AccessTrack Listener = null;

    public Track() {
        Segments = new ArrayList<>();
        Collected = new ArrayList<>();
    }
    public void append(Coords2D A, Coords2D B) { Segments.add(new Segment(A, B)); }

    /************************************************************************
     * Collect Segments that intercept Zone
     ************************************************************************/
    public ArrayList<Segment> collect(Frame Zone) {
        Collected.clear();
        for (Segment Line:Segments) { Collected.add(Line); }
        return Collected;
    }
    /*************************************************************************
     * Distance calculation about Track
     *************************************************************************/
    public double distanceFromStart() {
        double length = 0.0;
        if ((Coords == null)||(Index == -1)) return length;
        length += length(1, Index - 1);
        length += Segments.get(Index).fromStart(Coords);
        return length;
    }

    public double distanceToEnd() {
        double length = 0.0;
        if ((Coords == null)||(Index == -1)) return length;
        length+= Segments.get(Index).toEnd(Coords);
        length += length(Index+1, Segments.size() - 1);
        return length;
    }
    public double length() { return length(0, Segments.size()-1); }

    private double length(int n, int m) {
        double length = 0.0;
        for(int i=n; i<= m; i++) length += Segments.get(i).length();
        return length;
    }

    /**************************************************
    * Todo : Add distance/heading calculation
    * - Nearest Segment
    * - Start Segment and End Segment
    ***************************************************/

    /*************************************************************************
     * Searching for a segment that intercept given coordinate
     *************************************************************************/
    public void setSearchMode(boolean Mode) { isSearching = Mode ;}

    public void search(Coords2D Coords, double Clearance) {
        if (isSearching) return;
        this.Coords = Coords;
        this.Clearance = Clearance;
        this.run(); // Currently run in main thread = blocking
        //new Thread(this).start(); // Will run in a asynchronous thread ...
    }

    public void run() {
        // Run in Background priority mode
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        setSearchMode(true);

        // First, try from last search success
        if (checkSegment(Index)) { Listener.Tracking(Status.OnTrack); return;}

        // Failed then try some further segments from last result
        if (checkSegment(Index+1)) { Listener.Tracking(Status.OnTrack); return;}
        if (checkSegment(Index+2)) { Listener.Tracking(Status.OnTrack); return;}
        if (checkSegment(Index+3)) { Listener.Tracking(Status.OnTrack); return;}

        // Failed then try previous segment
        if (checkSegment(Index-1)) { Listener.Tracking(Status.OnTrack); return;}

        // At least, we do a full search
        boolean found = false;
        int i = 0;
        while ((!found) && (i < Segments.size()))
        {
            found = checkSegment(i);
            if (found) Index = i;
            i++;
        }
        if (found) { Listener.Tracking(Status.OnTrack); return;}

        // Finally we failed to find ...
        Index = -1;
        Listener.Tracking(Status.NotFound);
    }

    private boolean checkSegment(int i) {
        if (i < 0) return false;
        if (i >= Segments.size()) return false;
        return Segments.get(i).intercept(Coords, Clearance);
    }
}
