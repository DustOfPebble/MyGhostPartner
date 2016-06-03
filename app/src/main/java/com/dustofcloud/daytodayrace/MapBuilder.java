package com.dustofcloud.daytodayrace;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

public class MapBuilder extends Drawable implements Runnable {
    private Canvas OffScreenBuffer;
    private ArrayList<GeoData> FilteredPoints = null;
    private ArrayList<GeoData> ComputedPoints = null;
    public static final int isAborted = 1;
    public static final int isFinished = 2;
    private int Status = 0;

    public MapBuilder(int width, int height) {
        OffScreenBuffer = new Canvas(Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888));
    }

    /*public Drawable getMap() {
        return new .;
    }*/

    public void setFilteredPoints(ArrayList<GeoData> Collection) {
        FilteredPoints = new ArrayList<GeoData>(Collection);
    }

    public void setComputedPoints(ArrayList<GeoData> Collection){
        ComputedPoints = new ArrayList<GeoData>(Collection);
    }

    public int getStatus() {return Status;}

    @Override
    public void run() {
        Status = isAborted;
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        // Do all safety check
        if (ComputedPoints == null) return;
        if (FilteredPoints == null) return;

        // Do the drawing
        this.draw(OffScreenBuffer);
        Status = isFinished;
    }

    @Override
    public void draw(Canvas canvas) {


    }

    @Override
    public void setAlpha(int alpha) { }

    @Override
    public void setColorFilter(ColorFilter cf) {  }

    @Override
    public int getOpacity() { return 0; }

}
