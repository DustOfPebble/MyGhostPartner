package services;


import java.util.ArrayList;

import core.Files.SavedObject;
import core.Structures.Frame;
import core.Structures.Node;


public interface Queries {
    void GPS(boolean Enabled);

    void setLog(int Mode);

    void startSensor();
    void stopSensor();

    void selectTrack(SavedObject Source, int Mode);
    void setTracking(boolean Enabled);

    ArrayList<Node> getNodes(Frame Zone);
    void reload();
}
