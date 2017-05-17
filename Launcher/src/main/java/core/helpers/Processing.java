package core.helpers;

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
        float ExtractedHeading;
        for (Node Extracted : Collected) {
            ExtractedHeading = signed(Extracted.Stats.Bearing);
            if (Math.abs(ExtractedHeading - Heading) > BearingClearance) continue;
            Filtered.add(Extracted);
        }
        return Filtered;
    }

}
