package services.Track;

import java.util.ArrayList;

import core.Structures.Node;

public class Track implements Runnable {
    ArrayList<Segment> Segments;
    int Index = -1;

    int OnTrack = 1;
    int NotFound = -1;

    private Node Coordinate = null;
    private double Clearance = 0.0;
    private static boolean isSearching = false;

    private TrackService Listener = null;
    private LoaderGPX Loader = null;

    private boolean Locked = false;

    public Track() {
        Segments = new ArrayList<>();
    }

    public void setLoader(LoaderGPX Loader){
        this.Loader = Loader;
    }
    public void Load(){
        Locked = true;
        Segments.clear();
        try { Loader.start(); }
        catch (Exception Failed) { Loaded(); }
    }

    public void Loaded(){
        Locked = false;
    }

    public void setListener(TrackService Listener) { this.Listener = Listener; }

    public int size()
    {
        return Segments.size();
    }
    public void appendSegment(Node A, Node B) { Segments.add(new Segment(A, B)); }

    /*************************************************************************
     * Distance calculation about Track
     *************************************************************************/
    public double distanceFromStart() {
        double length = 0.0;
        if ((Coordinate == null)||(Index == -1)) return length;
        length += length(1, Index - 1);
        length += Segments.get(Index).fromStart(Coordinate);
        return length;
    }

    public double distanceToEnd() {
        double length = 0.0;
        if ((Coordinate == null)||(Index == -1)) return length;
        length+= Segments.get(Index).toEnd(Coordinate);
        length += length(Index+1, Segments.size() - 1);
        return length;
    }
    public double length() { return length(0, Segments.size()-1); }

    private double length(int n, int m) {
        double length = 0.0;
        for(int i=n; i<= m; i++) length += Segments.get(i).length();
        return length;
    }

    /*************************************************************************
     * Searching for a segment that intercept given coordinate
     *************************************************************************/
    public void setSearchMode(boolean Mode) { isSearching = Mode ;}

    public void search(Node Coordinate, double Clearance)
    {
        if (isSearching) return;
        this.Coordinate = Coordinate;
        this.Clearance = Clearance;
        this.run();
    }
    public void run()
    {
        // Run in Background priority mode
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        setSearchMode(true);

        // First, try from last search success
        if (checkSegment(Index)) { Listener.Tracking(OnTrack); return;}

        // Failed then try some further segments from last result
        if (checkSegment(Index+1)) { Listener.Tracking(OnTrack); return;}
        if (checkSegment(Index+2)) { Listener.Tracking(OnTrack); return;}
        if (checkSegment(Index+3)) { Listener.Tracking(OnTrack); return;}

        // Failed then try previous segment
        if (checkSegment(Index-1)) { Listener.Tracking(OnTrack); return;}

        // At least, we do a full search
        boolean found = false;
        int i = 0;
        while ((!found) && (i < Segments.size()))
        {
            found = checkSegment(i);
            if (found) Index = i;
            i++;
        }
        if (found) { Listener.Tracking(OnTrack); return;}

        // Finally we failed to find ...
        Index = -1;
        Listener.Tracking(NotFound);
    }
    private boolean checkSegment(int i)
    {
        if (i < 0) return false;
        if (i >= Segments.size()) return false;
        return Segments.get(i).intercept(Coordinate, Clearance);
    }


}
