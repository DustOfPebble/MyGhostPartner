package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.ImageView;

import java.util.ArrayList;

public class Monitor extends ImageView {
    private ArrayList<Pair> Collected;

    public Monitor(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);

        // Loading Attributes from XML definitions ...
        if (attrs == null) return;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Monitor, 0, 0);
        try
        {
//            highIcon = attributes.getDrawable(R.styleable.ControlSwitch_HighMode);
 //           lowIcon = attributes.getDrawable(R.styleable.ControlSwitch_LowMode);
        }
        finally { attributes.recycle();}

//        this.setImageDrawable(highIcon);
        invalidate();
    }

    public  void updateStatistics(ArrayList<Pair> values) {
        Collected = values;
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float liveValue = 0f;

        if (null != Collected) {
            for (Pair Infos:  Collected) {

            }

        }

        super.onDraw(canvas);
    }
}