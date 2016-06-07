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

    private static final int ColorFiltered = 0xff2ad4ff;
    private static final int ColorBackground = 0xff000000;
    private static final int FillTransparency = 30;
    private static final int LineTransparency = 70;
    private static final float LineThickness = 5f;

    private Canvas OffScreenBuffer;
    private Paint LineMode;
    private Paint FillMode;

    private ArrayList<GeoData> FilteredPoints = null;

    private PointF GraphicCenter = new PointF(0f,0f);
    private PointF WorldOrigin = null;
    private float MeterToPixelFactor = 1f;

    private Bitmap Map;

    public MapBuilder(int width, int height) {
        Map = Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
        OffScreenBuffer = new Canvas(Map);
        LineMode = new Paint();
        LineMode.setStrokeWidth(LineThickness);
        LineMode.setStyle(Paint.Style.STROKE);
        FillMode = new Paint();

        GraphicCenter.set(OffScreenBuffer.getWidth() /2, OffScreenBuffer.getHeight()/2);
    }

    public Bitmap getMap() {
        return Map ;
    }

    public void setFilteredPoints(ArrayList<GeoData> Collection) {
        FilteredPoints = new ArrayList<GeoData>(Collection);
    }

    public void setWorldOrigin(PointF Reference) { WorldOrigin = Reference;}
    public void setMeterToPixelFactor(float Factor) { MeterToPixelFactor = Factor;}


    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        // Do all safety check
        if (FilteredPoints == null) { return; }

        PointF Cartesian = null; // Do not allocate ==> We get a copy each time
        PointF Pixel = new PointF(0f,0f); // Allocate because it is updated on the fly
        Float Radius;

        long StartRender = SystemClock.elapsedRealtime();
        // Erase existing Bitmap ...
        OffScreenBuffer.drawColor(ColorBackground);

        // Do the drawing
        Log.d("MapBuilder", "Drawing "+ FilteredPoints.size()+ " points in view");
        // Drawing all points from Storage
        LineMode.setColor(ColorFiltered);
        LineMode.setAlpha(LineTransparency);
        FillMode.setColor(ColorFiltered);
        FillMode.setAlpha(FillTransparency);
        for (GeoData Marker : FilteredPoints) {
            Cartesian = Marker.getCoordinate();
            Radius = MeterToPixelFactor * Marker.getAccuracy();
            Pixel.set(
                    GraphicCenter.x - (Cartesian.x -WorldOrigin.x)* MeterToPixelFactor,
                    GraphicCenter.y - (Cartesian.y -WorldOrigin.y)* MeterToPixelFactor
            );
            OffScreenBuffer.drawCircle(Pixel.x, Pixel.y, Radius,LineMode);
            OffScreenBuffer.drawCircle(Pixel.x, Pixel.y, Radius,FillMode);
        }

        long EndRender = SystemClock.elapsedRealtime();
        Log.d("MapBuilder", "Rendering was "+ (EndRender - StartRender)+ " ms.");
    }

}
