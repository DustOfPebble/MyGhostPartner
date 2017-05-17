package services.Database;

import java.util.ArrayList;

import core.Structures.Extension;
import core.Structures.Frame;
import core.Structures.Node;
import core.Structures.Statistic;

public class StorageDB {

    static public ArrayList<Node> Collected = new ArrayList<>();
    static final Extension Limit = new Extension(2f,2f); // in meters

    private boolean isStorage = false;
    private Frame Zone = null;

    private StorageDB TopLeft = null;
    private StorageDB TopRight = null;
    private StorageDB BottomLeft = null;
    private StorageDB BottomRight = null;

    private ArrayList<Node> Statistics = null;

    public StorageDB(Frame zone) {
        isStorage = false;
        Zone = zone;
        if ((Zone.Size().w <= Limit.w) || (Zone.Size().h <= Limit.h)) isStorage = true;
    }

    public void clear() {
        if (Collected == null) Collected = new ArrayList<>();
        Collected.clear();
    }

    public void reset() {
        if (isStorage && (Statistics != null)) {
            Statistics.clear();
            Statistics = null;
            return;
        }
        if (TopLeft != null) { TopLeft.reset(); TopLeft = null; }
        if (TopRight != null) { TopRight.reset(); TopRight = null; }
        if (BottomLeft != null) { BottomLeft.reset(); BottomLeft = null; }
        if (BottomRight != null) { BottomRight.reset(); BottomRight = null; }
    }

    public void collect(Frame zone) {
        if (isStorage) {
            Collected.addAll(Statistics);
            return;
        }

        if (zone.Bottom() < Zone.Top() ) return ;
        if (zone.Top() > Zone.Bottom() ) return ;
        if (zone.Left() > Zone.Right() ) return ;
        if (zone.Right() < Zone.Left() ) return ;

        if (TopLeft != null) TopLeft.collect(zone);
        if (TopRight != null) TopRight.collect(zone);
        if (BottomLeft != null) BottomLeft.collect(zone);
        if (BottomRight != null) BottomRight.collect(zone);
    }

    public boolean belongs(Node set) {
        if (set.Move.dx < Zone.Left()) return false;
        if (set.Move.dx > Zone.Right()) return false;
        if (set.Move.dy < Zone.Top()) return false;
        if (set.Move.dy > Zone.Bottom()) return false;
        return true;
    }

    public void store(Node set) {
        if (isStorage) {
            if (Statistics == null) Statistics = new ArrayList<>();
            Statistics.add(set);
            return;
        }

        if (!belongs(set)) return;

        if ((set.Move.dx < Zone.Center().dx) && (set.Move.dy < Zone.Center().dy)) {
            if (TopLeft == null) TopLeft = new StorageDB(new Frame(Zone.TopLeft(), Zone.Center()));
            TopLeft.store(set);
        }

        if ((set.Move.dx > Zone.Center().dx) && (set.Move.dy < Zone.Center().dy)) {
            if (TopRight == null) TopRight = new StorageDB(new Frame( Zone.TopRight(), Zone.Center()));
            TopRight.store(set);
        }

        if ((set.Move.dx < Zone.Center().dx) && (set.Move.dy > Zone.Center().dy)) {
            if (BottomLeft == null) BottomLeft = new StorageDB(new Frame( Zone.BottomLeft(),Zone.Center()));
            BottomLeft.store(set);
        }

        if ((set.Move.dx > Zone.Center().dx) && (set.Move.dy > Zone.Center().dy)) {
            if (BottomRight == null) BottomRight = new StorageDB(new Frame( Zone.BottomRight(), Zone.Center()));
            BottomRight.store(set);
        }
    }
}
