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

    private static final int ColorFiltered = 0xff84e9f4;
    private static final int ColorComputed = 0xff00d4aa;
    private static final int Transparency = 60;

    private Canvas OffScreenBuffer;
    private Paint LineMode;
    private Paint FillMode;

    private ArrayList<GeoData> FilteredPoints = null;
    private ArrayList<GeoData> ComputedPoints = null;

    private PointF GraphicCenter = new PointF(0f,0f);
    private PointF WorldOrigin = null;
    private float MeterToPixelFactor = 1f;

    private Bitmap Map;

    public MapBuilder(int width, int height) {
        Map = Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
        OffScreenBuffer = new Canvas(Map);
        LineMode = new Paint();
        LineMode.setStrokeWidth(5f);
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

    public void setComputedPoints(ArrayList<GeoData> Collection){
        ComputedPoints = new ArrayList<GeoData>(Collection);
    }

    public void setWorldOrigin(PointF Reference) { WorldOrigin = Reference;}
    public void setMeterToPixelFactor(float Factor) { MeterToPixelFactor = Factor;}


    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        // Do all safety check
        if ((ComputedPoints == null) || (FilteredPoints == null)) {
             return;
        }

        PointF Cartesian = null; // Do not allocate ==> We get a copy each time
        PointF Pixel = new PointF(0f,0f); // Allocate because it is updated on the fly
        Float Radius;

        long StartRender = SystemClock.elapsedRealtime();
        // Erase existing Bitmap ...
        OffScreenBuffer.drawColor(Color.BLACK);

        // Do the drawing
        Log.d("MapBuilder", "Drawing "+ FilteredPoints.size()+ " points in view");
        // Drawing all points from Storage
        LineMode.setColor(ColorFiltered);
        FillMode.setColor(ColorFiltered);
        FillMode.setAlpha(Transparency);
        for (GeoData Marker : FilteredPoints) {
            Cartesian = Marker.getCartesian();
            Radius = MeterToPixelFactor * Marker.getAccuracy();
            Pixel.set(
                    (Cartesian.x -WorldOrigin.x)* MeterToPixelFactor + GraphicCenter.x,
                    (Cartesian.y -WorldOrigin.y)* MeterToPixelFactor + GraphicCenter.y
            );
            OffScreenBuffer.drawCircle(Pixel.x, Pixel.y, Radius,LineMode);
            OffScreenBuffer.drawCircle(Pixel.x, Pixel.y, Radius,FillMode);
        }

        Log.d("MapBuilder", "Drawing "+ ComputedPoints.size()+ " points in use");
        // Drawing all points from Storage
        LineMode.setColor(ColorComputed);
        FillMode.setColor(ColorComputed);
        FillMode.setAlpha(Transparency);
        for (GeoData Marker : ComputedPoints) {
            Cartesian = Marker.getCartesian();
            Radius = MeterToPixelFactor * Marker.getAccuracy();
            Pixel.set(
                    (Cartesian.x -WorldOrigin.x)* MeterToPixelFactor + GraphicCenter.x,
                    (Cartesian.y -WorldOrigin.y)* MeterToPixelFactor + GraphicCenter.y
            );
            OffScreenBuffer.drawCircle(Pixel.x, Pixel.y, Radius,LineMode);
            OffScreenBuffer.drawCircle(Pixel.x, Pixel.y, Radius,FillMode);
        }
        long EndRender = SystemClock.elapsedRealtime();
        Log.d("MapBuilder", "Rendering was "+ (EndRender - StartRender)+ " ms.");
    }

}
