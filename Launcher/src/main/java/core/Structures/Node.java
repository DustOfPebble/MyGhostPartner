package core.Structures;

public class Node {
    public Coords2D Move = null;
    public Statistic Stats = null;
    public short Days = -1;
    public short Track = -1;

    public Node(Coords2D Move, Statistic Stats) {
        this.Move = Move;
        this.Stats = Stats;
    }
}

