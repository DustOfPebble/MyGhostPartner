package core.launcher.Widgets;

import java.util.ArrayList;

import core.Structures.Node;

public class SpeedStatistics extends Processor {

    @Override
    ArrayList<Float> get(ArrayList<Node> Nodes) {
        ArrayList<Float> Processed = new ArrayList<>();
        for (Node item: Nodes) {
            Processed.add(item.Stats.Speed*3.6f);
        }
        return Processed;
    }

    @Override
    float get(Node Live) {
        return  Live.Stats.Speed*3.6f;
    }

}
