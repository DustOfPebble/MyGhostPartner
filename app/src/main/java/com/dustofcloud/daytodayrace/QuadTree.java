package com.dustofcloud.daytodayrace;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Xavier JAFFREZIC on 15/05/2016.
 */
public class QuadTree extends RectF {
    private boolean isStorage=false;
    private RectF SubZone = null;
    private ArrayList<GeoData> Storage = null;
    private QuadTree TopLeft = null;
    private QuadTree TopRight = null;
    private QuadTree BottomLeft = null;
    private QuadTree BottomRight = null;

    private final int minWidth = 100; //  100 meters width..
    private final int minHeight = 100; // 100 meters height..

    public QuadTree(RectF Zone) {
        super(Zone);

        if ((Zone.width() > minWidth) && (Zone.height() > minHeight)) {
            SubZone= new RectF(
                    Zone.centerX() - (Zone.width() /2),
                    Zone.centerY() - (Zone.height() / 2),
                    Zone.centerX() + (Zone.width() / 2),
                    Zone.centerY() + (Zone.height() / 2)
            );
        } else {
            isStorage = true;
            Storage = new ArrayList<GeoData>();
        }
    }

    public ArrayList<GeoData> searchWayPoints(RectF SearchArea) {
        if (isStorage) { return Storage; }

        ArrayList<GeoData> Collected = new ArrayList();

        if (SearchArea.bottom < this.top ) return Collected;
        if (SearchArea.top > this.bottom ) return Collected;
        if (SearchArea.left > this.right ) return Collected;
        if (SearchArea.right < this.left ) return Collected;

        if (TopLeft != null) Collected.addAll(TopLeft.searchWayPoints(SearchArea));
        if (TopRight != null) Collected.addAll(TopRight.searchWayPoints(SearchArea));
        if (BottomLeft != null) Collected.addAll(BottomLeft.searchWayPoints(SearchArea));
        if (BottomRight != null) Collected.addAll(BottomRight.searchWayPoints(SearchArea));

        return Collected;
    }

    public void storeWayPoint(GeoData geoData) {
        // Should we store this new point ?
        PointF Cartesian = geoData.getCartesian();
        if (isStorage) {
            Storage.add(geoData);
            Log.d("QuadTree","Stored cartesian["+ Cartesian.x+","+Cartesian.y+"]");
            return;
        }

        Log.d("QuadTree","Center is ["+ this.centerX()+","+this.centerY()+"]");

        Log.d("QuadTree","Trying to catch["+ Cartesian.x+","+Cartesian.y+"] " +
                "in [("+(this.centerX() - SubZone.width())+","+(this.centerY() - SubZone.height())+")-"+
                "("+(this.centerX() + SubZone.width())+","+(this.centerY() + SubZone.height())+")]");

        if (Cartesian.x < (this.centerX() - SubZone.width()) ) return;
        if (Cartesian.x > (this.centerX() + SubZone.width()) ) return;
        if (Cartesian.y < (this.centerY() - SubZone.height()) ) return;
        if (Cartesian.y > (this.centerY() + SubZone.height()) ) return;

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
            Log.d("QuadTree","TopLeft QuadTree["+ this.centerX()+","+this.centerY()+"] has catched the point");
            TopLeft.storeWayPoint(geoData);
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
            Log.d("QuadTree","TopRight QuadTree["+ this.centerX()+","+this.centerY()+"] has catched the point");
            TopRight.storeWayPoint(geoData);
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
            Log.d("QuadTree","BottomLeft QuadTree["+ this.centerX()+","+this.centerY()+"] has catched the point");
            BottomLeft.storeWayPoint(geoData);
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
            Log.d("QuadTree","BottomRight QuadTree["+ this.centerX()+","+this.centerY()+"] has catched the point");
            BottomRight.storeWayPoint(geoData);
        }
    }

}
