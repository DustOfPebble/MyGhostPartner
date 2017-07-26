package core.launcher.Widgets;

import java.util.ArrayList;

import core.Structures.Node;

public class HeartStatistics extends Processor {

    @Override
    ArrayList<Float> get(ArrayList<Node> Nodes) {
        ArrayList<Float> Processed = new ArrayList<>();
        for (Node item: Nodes) {
            if (item.Stats.Heartbeat !=-1) Processed.add((float)item.Stats.Heartbeat);
        }
        return Processed;
    }

    @Override
    float get(Node Live) {
        return  (float)Live.Stats.Heartbeat;
    }

}
