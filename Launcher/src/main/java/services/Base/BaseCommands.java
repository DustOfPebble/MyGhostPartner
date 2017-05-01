package services.Base;

import java.util.ArrayList;

import core.Structures.Frame;
import core.Structures.Statistic;

public interface BaseCommands {
    public ArrayList<Statistic> getStatistics(Frame Zone);
    void EnableGPS(boolean Enabled);



}
