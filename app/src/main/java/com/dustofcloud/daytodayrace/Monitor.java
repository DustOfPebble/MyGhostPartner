package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.ImageView;

import java.util.ArrayList;

public class Monitor extends ImageView {
    private ArrayList<Statistic> Collected;
    private float MeanValue;
    private Path MonitorFrame;
    private Path MonitorFrameFilled;
    private Paint FrameBorderPainter;
    private Paint FrameBackgroundPainter;
    private static final int FrameBorderColor = 0xff84e9f4;
    private static final int FrameBackgroundColor = 0xff00bebe;
    private static final float FrameBorderThickness = 5f;


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
        FrameBorderPainter = new Paint();
        FrameBorderPainter.setStyle(Paint.Style.STROKE);
        FrameBorderPainter.setColor(FrameBorderColor);
        FrameBorderPainter.setStrokeWidth(FrameBorderThickness);

        FrameBackgroundPainter = new Paint();
        FrameBackgroundPainter.setStyle(Paint.Style.FILL);
        FrameBorderPainter.setColor(FrameBackgroundColor);
        MonitorFrame = new Path();
        MonitorFrameFilled = MonitorFrame;

    }

    public  void updateStatistics(ArrayList<Statistic> values) {
        Collected = values;
        MeanValue = 0f;

        if (null != Collected) {
            for (Statistic item:  Collected) { MeanValue+=item.value; }
            if (Collected.size() > 0) MeanValue = MeanValue / Collected.size();
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int Width = MeasureSpec.getSize(widthMeasureSpec);
        int Height = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(Width, Height);
        if ((Width == 0) || (Height == 0)) return;
        MonitorFrame.moveTo(0,0);
        MonitorFrame.lineTo(Width,0);
        MonitorFrame.lineTo(Width,Height);
        MonitorFrame.lineTo(0,Height);
        MonitorFrame.close();
        MonitorFrameFilled = new Path(MonitorFrame);
        MonitorFrameFilled.setFillType(Path.FillType.WINDING);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        canvas.drawPath(MonitorFrameFilled,FrameBackgroundPainter);
        canvas.drawPath(MonitorFrame,FrameBorderPainter);

    }
}