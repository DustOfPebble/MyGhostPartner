package core.launcher.partner;

import java.util.ArrayList;
import core.Structures.Node;
import core.Structures.Statistic;

public class Processing {
    static final float BearingClearance = 60f; // 60°

    // Convert angle from [0,360°] to [-180°,180°]
    public static float signed(float Angle) { return  ((Angle > 180)? (180 - Angle) : Angle); }

    // Filter and return Point that match a Bearing Range
    public static ArrayList<Node> filter(ArrayList<Node> Collected, Statistic Snapshot){
        ArrayList<Node> Filtered = new ArrayList<Node>();

        float Heading = signed(Snapshot.Bearing);
        for (Node Extracted : Collected) {
            if (Extracted.Days == 0) continue;
            if (Math.abs(signed(Extracted.Stats.Bearing) - Heading) > BearingClearance) continue;
            Filtered.add(Extracted);
        }
        return Filtered;
    }

}
