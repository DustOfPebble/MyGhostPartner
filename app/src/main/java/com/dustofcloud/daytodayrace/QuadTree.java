package com.dustofcloud.daytodayrace;
import android.graphics.PointF;
import android.graphics.RectF;

import com.dustofcloud.daytodayrace.WayPoint;

import java.util.ArrayList;

/**
 * Created by Xavier JAFFREZIC on 15/05/2016.
 */
public class QuadTree extends RectF {
    private float halfSize;
    private boolean isStorage=false;

    private ArrayList<WayPoint> WayPoints = null;
    private QuadTree TopLeft = null;
    private QuadTree TopRight = null;
    private QuadTree BottomLeft = null;
    private QuadTree BottomRight = null;

    private final int minSize = 100; // Minimun size is 100 meters square..

    public QuadTree(RectF area) {
        super(area);
        if (this.width() > minSize) isStorage=true;
        if (this.width() <= minSize) WayPoints = new ArrayList<>();
        this.halfSize = this.width() /2 ;
    }

    public ArrayList<WayPoint> searchWayPoints(RectF SearchArea) {
        if (isStorage) { return WayPoints; }

        ArrayList<WayPoint> Collected = new ArrayList();

        if (SearchArea.bottom < this.top ) return Collected;
        if (SearchArea.top > this.bottom ) return Collected;
        if (SearchArea.left > this.right ) return Collected;
        if (SearchArea.right < this.left ) return Collected;

        Collected.addAll(TopLeft.searchWayPoints(SearchArea));
        Collected.addAll(TopRight.searchWayPoints(SearchArea));
        Collected.addAll(BottomLeft.searchWayPoints(SearchArea));
        Collected.addAll(BottomRight.searchWayPoints(SearchArea));

        return Collected;
    }

    public void storeWayPoint(WayPoint wayPoint) {
        // Should we store this new point ?
        if (wayPoint.getX() < this.centerX() - halfSize) return;
        if (wayPoint.getX() > this.centerX() + halfSize) return;
        if (wayPoint.getY() < this.centerY() - halfSize) return;
        if (wayPoint.getY() > this.centerY() + halfSize) return;

        if (isStorage) {
            WayPoints.add(wayPoint);
            return;
        }

        if ((wayPoint.getX()< this.centerX()) && (wayPoint.getY()< this.centerY())) {
            if (TopLeft == null) {
                TopLeft = new QuadTree(
                        new RectF(this.centerX() - halfSize,
                                this.centerY() - halfSize,
                                this.centerX(),
                                this.centerY()
                            )
                        );
            }
            TopLeft.storeWayPoint(wayPoint);
        }

        if ((wayPoint.getX()> this.centerX()) && (wayPoint.getY()< this.centerY())) {
            if (TopRight == null) {
                TopRight = new QuadTree(
                        new RectF(this.centerX() ,
                                this.centerY() - halfSize,
                                this.centerX() + halfSize,
                                this.centerY()
                        )
                );
            }
            TopRight.storeWayPoint(wayPoint);
        }

        if ((wayPoint.getX()< this.centerX()) && (wayPoint.getY() > this.centerY())) {
            if (BottomLeft == null) {
                BottomLeft = new QuadTree(
                        new RectF(this.centerX() - halfSize,
                                this.centerY() ,
                                this.centerX(),
                                this.centerY() + halfSize
                        )
                );
            }
            BottomLeft.storeWayPoint(wayPoint);
        }

        if ((wayPoint.getX()> this.centerX()) && (wayPoint.getY() > this.centerY())) {
            if (BottomRight == null) {
                BottomRight = new QuadTree(
                        new RectF(this.centerX() ,
                                this.centerY() ,
                                this.centerX() + halfSize,
                                this.centerY() + halfSize
                        )
                );
            }
            BottomRight.storeWayPoint(wayPoint);
        }
    }



}
