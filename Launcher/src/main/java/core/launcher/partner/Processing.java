package core.launcher.partner;

import java.util.ArrayList;

import core.Structures.Coords2D;
import core.Structures.Node;

public class Processing {
    static final float BearingClearance = 60f; // 60°

    static float pow(float x) {return  x*x; }
    static float distance(Coords2D A, Coords2D B) {return (float)Math.sqrt(pow(A.dx - B.dx) + pow(A.dx - B.dx)); }

    // Convert angle from [0,360°] to [-180°,180°]
    private static float signed(float Angle) { return  ((Angle > 180)? (180 - Angle) : Angle); }

    // Filter by Distance
    private static Node byDistance(ArrayList<Node> Nodes, Coords2D Origin) {
        Node Nearest = Nodes.get(0);
        float Distance = distance(Origin, Nearest.Move);
        for (Node Selected : Nodes) {
            if (distance(Origin, Selected.Move) < Distance) Nearest = Selected;
        }
        return Nearest;
    }

    // Remove Track Id
    private static ArrayList<Node> byID(ArrayList<Node> Nodes, short Id) {
        ArrayList<Node> Filtered = new ArrayList<Node>();
        for (Node Selected : Nodes) {
            if (Selected.Track == Id) Filtered.add(Selected);
            Filtered.add(Selected);
        }
        return Filtered;
    }

    // Filter by match Heading
    private static ArrayList<Node> byHeading(ArrayList<Node> Nodes, float Heading){
        ArrayList<Node> Filtered = new ArrayList<Node>();
        for (Node Selected : Nodes) {
            if (Math.abs(signed(Selected.Stats.Bearing) - Heading) < BearingClearance) Filtered.add(Selected);
        }
        return Filtered;
    }

    // Filter points for statistics
    public static ArrayList<Node> filter(ArrayList<Node> Nodes, Node GPS){
        ArrayList<Node> Filtered = new ArrayList<Node>();

        // Build a list of Track ID
        ArrayList<Short> IDs = new ArrayList<>();
        for (Node Selected : Nodes) {
            if (!IDs.contains(Selected.Track)) IDs.add(Selected.Track);
        }

        for(Short Id:IDs) {
            if (Id == -1) continue; // Do not use current trace for Statistic
            Filtered.add(byDistance(byHeading(byID(Nodes,GPS.Track),GPS.Stats.Bearing),GPS.Move));
        }

        return Filtered;
    }

}
