package com.dustofcloud.daytodayrace;
import android.graphics.PointF;
import android.graphics.RectF;

import com.dustofcloud.daytodayrace.WayPoint;

import java.util.ArrayList;

/**
 * Created by Xavier JAFFREZIC on 15/05/2016.
 */
public class QuadTree extends RectF {
    private boolean isStorage=false;
    private RectF SubZone = null;
    private ArrayList<WayPoint> WayPoints = null;
    private QuadTree TopLeft = null;
    private QuadTree TopRight = null;
    private QuadTree BottomLeft = null;
    private QuadTree BottomRight = null;

    private final int minWidth = 100; //  100 meters width..
    private final int minHeight = 100; // 100 meters height..

    public QuadTree(RectF Zone) {
        super(Zone);

        if ((Zone.width() < minWidth) || (Zone.height() < minHeight)) {
            SubZone= new RectF(
                    Zone.centerX() - Zone.width() /2,
                    Zone.centerY() - Zone.height() / 2,
                    Zone.centerX() + Zone.width() / 2,
                    Zone.centerY() + Zone.height() / 2) ;
        } else {
            isStorage = true;
            WayPoints = new ArrayList<>();
        }
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
        PointF Cartesian = wayPoint.getCartesian();
        if (Cartesian.x < this.centerX() - SubZone.width() ) return;
        if (Cartesian.x > this.centerX() + SubZone.width() ) return;
        if (Cartesian.y < this.centerY() - SubZone.height() ) return;
        if (Cartesian.y > this.centerY() + SubZone.height() ) return;

        if (isStorage) {
            WayPoints.add(wayPoint);
            return;
        }

        if ((Cartesian.x< this.centerX()) && (Cartesian.y< this.centerY())) {
            if (TopLeft == null) {
                TopLeft = new QuadTree(
                        new RectF(  this.centerX() - SubZone.width(),
                                    this.centerY() - SubZone.height(),
                                    this.centerX(),
                                    this.centerY()
                            )
                        );
            }
            TopLeft.storeWayPoint(wayPoint);
        }

        if ((Cartesian.x> this.centerX()) && (Cartesian.y< this.centerY())) {
            if (TopRight == null) {
                TopRight = new QuadTree(
                        new RectF(  this.centerX(),
                                    this.centerY() - SubZone.height(),
                                    this.centerX() + SubZone.width(),
                                    this.centerY() )
                            );
            }
            TopRight.storeWayPoint(wayPoint);
        }

        if ((Cartesian.x< this.centerX()) && (Cartesian.y > this.centerY())) {
            if (BottomLeft == null) {
                BottomLeft = new QuadTree(
                        new RectF(  this.centerX() - SubZone.width(),
                                    this.centerY(),
                                    this.centerX(),
                                    this.centerY() + SubZone.height() )
                            );
            }
            BottomLeft.storeWayPoint(wayPoint);
        }

        if ((Cartesian.x> this.centerX()) && (Cartesian.y > this.centerY())) {
            if (BottomRight == null) {
                BottomRight = new QuadTree(
                        new RectF(  this.centerX(),
                                    this.centerY(),
                                    this.centerX() + SubZone.width(),
                                    this.centerY() + SubZone.height() )
                            );
            }
            BottomRight.storeWayPoint(wayPoint);
        }
    }

}
