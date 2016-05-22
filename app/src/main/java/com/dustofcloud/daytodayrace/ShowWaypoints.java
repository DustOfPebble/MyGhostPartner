package com.dustofcloud.daytodayrace;

import android.app.Application;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ShowWaypoints extends ImageView implements CallbackUpdateView {

    private float ScaleFactor = 0.0f;

    public ShowWaypoints(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);
    }

    public void setScaleFactor(float ScaleFactor) {
        this.ScaleFactor = ScaleFactor;
        postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int Width = MeasureSpec.getSize(widthMeasureSpec);
        int Height = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(Width, Height);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
    }
}