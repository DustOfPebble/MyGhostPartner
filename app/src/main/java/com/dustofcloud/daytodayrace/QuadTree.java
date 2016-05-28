package com.dustofcloud.daytodayrace;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Xavier JAFFREZIC on 15/05/2016.
 */
public class QuadTree {
    private boolean isStorage=false;
    private PointF SubZone = null;
    private RectF Zone = null;
    private ArrayList<GeoData> Storage = null;
    private QuadTree TopLeft = null;
    private QuadTree TopRight = null;
    private QuadTree BottomLeft = null;
    private QuadTree BottomRight = null;
    private PointF SizeCell = new PointF(2f,2f); // (2m x 2m) minimum size

    public QuadTree(RectF zone) {
        Zone = zone;
        Log.d("QuadTree","New Quadtree ["+Zone.width()+" x "+Zone.height()+"]");
        if ((Zone.width() > SizeCell.x) && (Zone.height() > SizeCell.y)) {
            SubZone= new PointF(Zone.width() /2, Zone.height() / 2);
            isStorage = false;
        } else {
            isStorage = true;
            Storage = new ArrayList<GeoData>();
        }
    }

    public ArrayList<GeoData> search(RectF SearchArea) {
        if (isStorage) { return Storage; }

        ArrayList<GeoData> Collected = new ArrayList();

        if (SearchArea.bottom < Zone.top ) return Collected;
        if (SearchArea.top > Zone.bottom ) return Collected;
        if (SearchArea.left > Zone.right ) return Collected;
        if (SearchArea.right < Zone.left ) return Collected;

        if (TopLeft != null) Collected.addAll(TopLeft.search(SearchArea));
        if (TopRight != null) Collected.addAll(TopRight.search(SearchArea));
        if (BottomLeft != null) Collected.addAll(BottomLeft.search(SearchArea));
        if (BottomRight != null) Collected.addAll(BottomRight.search(SearchArea));

        return Collected;
    }

    public void store(GeoData geoData) {
        // Should we store this new point ?
        PointF Cartesian = geoData.getCartesian();
        if (isStorage) {
            Storage.add(geoData);
            Log.d("QuadTree","Stored cartesian["+ Cartesian.x+","+Cartesian.y+"]");
            return;
        }

        Log.d("QuadTree","Center is ["+ Zone.centerX()+","+Zone.centerY()+"]");

        Log.d("QuadTree","Trying to catch["+ Cartesian.x+","+Cartesian.y+"] " +
                "in [("+(Zone.centerX() - SubZone.x)+","+(Zone.centerY() - SubZone.y)+")-"+
                "("+(Zone.centerX() + SubZone.x)+","+(Zone.centerY() + SubZone.y)+")]");

        if (Cartesian.x < (Zone.centerX() - SubZone.x) ) return;
        if (Cartesian.x > (Zone.centerX() + SubZone.x) ) return;
        if (Cartesian.y < (Zone.centerY() - SubZone.y) ) return;
        if (Cartesian.y > (Zone.centerY() + SubZone.y) ) return;

        if ((Cartesian.x< Zone.centerX()) && (Cartesian.y< Zone.centerY())) {
            if (TopLeft == null) {
                TopLeft = new QuadTree(
                        new RectF(  Zone.centerX() - SubZone.x,
                                Zone.centerY() - SubZone.y,
                                Zone.centerX(),
                                Zone.centerY()
                            )
                        );
            }
            Log.d("QuadTree","TopLeft ["+ Zone.centerX()+","+Zone.centerY()+"] has catched the point");
            TopLeft.store(geoData);
        }

        if ((Cartesian.x> Zone.centerX()) && (Cartesian.y< Zone.centerY())) {
            if (TopRight == null) {
                TopRight = new QuadTree(
                        new RectF(  Zone.centerX(),
                                Zone.centerY() - SubZone.x,
                                Zone.centerX() + SubZone.y,
                                Zone.centerY() )
                            );
            }
            Log.d("QuadTree","TopRight ["+ Zone.centerX()+","+Zone.centerY()+"] has catched the point");
            TopRight.store(geoData);
        }

        if ((Cartesian.x< Zone.centerX()) && (Cartesian.y > Zone.centerY())) {
            if (BottomLeft == null) {
                BottomLeft = new QuadTree(
                        new RectF(  Zone.centerX() - SubZone.x,
                                Zone.centerY(),
                                Zone.centerX(),
                                Zone.centerY() + SubZone.y )
                            );
            }
            Log.d("QuadTree","BottomLeft ["+ Zone.centerX()+","+Zone.centerY()+"] has catched the point");
            BottomLeft.store(geoData);
        }

        if ((Cartesian.x> Zone.centerX()) && (Cartesian.y > Zone.centerY())) {
            if (BottomRight == null) {
                BottomRight = new QuadTree(
                        new RectF(  Zone.centerX(),
                                Zone.centerY(),
                                Zone.centerX() + SubZone.x,
                                Zone.centerY() + SubZone.y )
                            );
            }
            Log.d("QuadTree","BottomRight ["+ Zone.centerX()+","+Zone.centerY()+"] has catched the point");
            BottomRight.store(geoData);
        }
    }

}
