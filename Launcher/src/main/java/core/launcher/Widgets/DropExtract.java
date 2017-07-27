package core.launcher.Widgets;

import java.util.ArrayList;

import core.Structures.Node;

public class DropExtract extends Fields {

    @Override
    ArrayList<Float> get(ArrayList<Node> Nodes) {
        ArrayList<Float> Processed = new ArrayList<>();
        for (Node item: Nodes) {
            Processed.add(item.Stats.Altitude);
        }
        return Processed;
    }

    @Override
    float get(Node Live) {
        return  Live.Stats.Altitude;
    }

}
