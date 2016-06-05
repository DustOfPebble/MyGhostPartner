package com.dustofcloud.daytodayrace;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;

public class MapBuilder implements Runnable {
    public static final int isAborted = 1;
    public static final int isFinished = 2;

    private Canvas OffScreenBuffer;
    private Paint Painter;


    private ArrayList<GeoData> FilteredPoints = null;
    private ArrayList<GeoData> ComputedPoints = null;
    private int Status = isAborted;



    private Bitmap Map;

    public MapBuilder(int width, int height) {
        Map = Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
        OffScreenBuffer = new Canvas(Map);
    }
    public int getStatus() {return Status;}

    public Bitmap getMap() {
        return Map ;
    }

    public void setFilteredPoints(ArrayList<GeoData> Collection) {
        FilteredPoints = new ArrayList<GeoData>(Collection);
    }

    public void setComputedPoints(ArrayList<GeoData> Collection){
        ComputedPoints = new ArrayList<GeoData>(Collection);
    }




    @Override
    public void run() {
        Status = isAborted;
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        // Do all safety check
        if (ComputedPoints == null) return;
        if (FilteredPoints == null) return;

        PointF Pixel = new PointF(0f,0f);


        long StartRender = SystemClock.elapsedRealtime();
        // Do the drawing
        Log.d("PointsDrawer", "Drawing "+ FilteredPoints.size()+ " points in view");
        // Drawing all points from Storage
        Painter.setColor(Color.MAGENTA);
        for (GeoData Marker : FilteredPoints) {
            Cartesian = Marker.getCartesian();
            Pixel = PixelsFromMeters(MetersFromOrigin(Cartesian,OffsetMeters),Center);
            OffScreenBuffer.drawCircle(GraphicPoint.x, GraphicPoint.y, scaleRadius * Marker.getAccuracy(),Painter);
        }

        Log.d("PointsDrawer", "Drawing "+ ComputedPoints.size()+ " points in use");
        // Drawing all points from Storage
        Painter.setColor(Color.GREEN);
        for (GeoData Marker : ComputedPoints) {
            Cartesian = Marker.getCartesian();
            Pixel = PixelsFromMeters(MetersFromOrigin(Cartesian,OffsetMeters),Center);
            OffScreenBuffer.drawPoint(GraphicPoint.x, GraphicPoint.y,Painter);
            OffScreenBuffer.drawCircle(GraphicPoint.x, GraphicPoint.y, scaleRadius * Marker.getAccuracy(),Painter);
        }
        long EndRender = SystemClock.elapsedRealtime();
        Log.d("MapBuilder", "Rendering was "+ (EndRender - StartRender)+ " ms.");

        Status = isFinished;
    }

}
