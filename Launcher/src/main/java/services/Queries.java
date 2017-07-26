package services;

import java.util.ArrayList;

import core.Files.SavedObject;
import core.Structures.Frame;
import core.Structures.Node;

public interface Queries {
    void selectTrack(SavedObject Source, int Mode);
    void setTracking(boolean Enabled);

    void GPS(boolean Enabled);
    void Logger(int Mode);
    void Sensor(boolean Enabled);

    ArrayList<Node> getNodesByZone(Frame Zone);
    ArrayList<Node> getNodesByDelay(int Delay);
}
