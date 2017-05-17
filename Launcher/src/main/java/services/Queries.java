package services;

import java.io.File;
import java.util.ArrayList;

import core.Structures.Frame;
import core.Structures.Node;
import services.Track.Segment;

public interface Queries {
    void GPS(boolean Enabled);

    void setLog(int Mode);

    void startSensor();
    void stopSensor();

    void selectTrack(File Source, int Mode);
    void setTracking(boolean Enabled, double Clearance);

    ArrayList<Node> getNodes(Frame Zone);
    void reload();
}
