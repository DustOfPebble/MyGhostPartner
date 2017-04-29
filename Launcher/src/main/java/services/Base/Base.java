package services.Base;

import java.util.ArrayList;

import core.Structures.Extension;
import core.Structures.Frame;
import core.Structures.Statistic;

public class Base {

    static public ArrayList<Statistic> Collected = null;
    static final Extension Limit = new Extension(2f,2f); // in meters

    private boolean isStorage = false;
    private Frame Zone = null;

    private Base TopLeft = null;
    private Base TopRight = null;
    private Base BottomLeft = null;
    private Base BottomRight = null;

    private ArrayList<Statistic> Statistics = null;

    public Base(Frame zone) {
        isStorage = false;
        Zone = zone;
        if ((Zone.Size().w <= Limit.w) || (Zone.Size().h <= Limit.h)) isStorage = true;
    }

    public void clear() {
        if (Collected == null) Collected = new ArrayList<>();
        Collected.clear();
    }

    public void reset() {
        if (isStorage && (Statistics != null) {
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

    public void store(Statistic set) {
        if (isStorage) {
            Statistics.add(set);
            return;
        }

        if (set.node().x < Zone.Left()) return;
        if (set.node().x > Zone.Right()) return;
        if (set.node().y < Zone.Top()) return;
        if (set.node().y > Zone.Bottom()) return;

        if ((set.node().x < Zone.Center().x) && (set.node().y < Zone.Center().y)) {
            if (TopLeft == null) TopLeft = new Base(new Frame(Zone.TopLeft(), Zone.Center()));
            TopLeft.store(set);
        }

        if ((set.node().x > Zone.Center().x) && (set.node().y < Zone.Center().y)) {
            if (TopRight == null) TopRight = new Base(new Frame( Zone.TopRight(), Zone.Center()));
            TopRight.store(set);
        }

        if ((set.node().x < Zone.Center().x) && (set.node().y > Zone.Center().y)) {
            if (BottomLeft == null) BottomLeft = new Base(new Frame( Zone.BottomLeft(),Zone.Center()));
            BottomLeft.store(set);
        }

        if ((set.node().x> Zone.Center().x) && (set.node().y > Zone.Center().y)) {
            if (BottomRight == null) BottomRight = new Base(new Frame( Zone.BottomRight(), Zone.Center()));
            BottomRight.store(set);
        }
    }
}
