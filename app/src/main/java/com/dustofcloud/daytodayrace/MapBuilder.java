package com.dustofcloud.daytodayrace;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

public class MapBuilder extends Drawable implements Runnable {
    Canvas OffScreenBuffer;

    public MapBuilder(int width, int height) {
        OffScreenBuffer = new Canvas(Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888));
    }

    @Override
    public void draw(Canvas canvas) {

    }

    @Override
    public void setAlpha(int alpha) { }

    @Override
    public void setColorFilter(ColorFilter cf) {  }

    @Override
    public int getOpacity() { return 0;  }

    @Override
    public void run() {
        this.draw(OffScreenBuffer);
    }
}
