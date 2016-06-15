package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;

public class Monitor extends ImageView {
    private ArrayList<Statistic> Collected;
    private float MeanValue;
    private Path Frame;
    private Path FrameFilled;
    private Paint BorderPainter;
    private Paint BackgroundPainter;
    private Paint TextPainter;

    private static final int BorderColor = 0xff84e9f4;
    private static final int BackgroundColor = 0xff00bebe;
    private static final float BorderThickness = 5f;
    private static final int TextColor = 0xfffffcfc;

    private String Unit ="";
    private BitmapDrawable Icon = null;


    public Monitor(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);

        BorderPainter = new Paint();
        BorderPainter.setStyle(Paint.Style.STROKE);
        BorderPainter.setColor(BorderColor);
        BorderPainter.setStrokeWidth(BorderThickness);

        BackgroundPainter = new Paint();
        BackgroundPainter.setStyle(Paint.Style.FILL);
        BackgroundPainter.setColor(BackgroundColor);
        Frame = new Path();
        FrameFilled = new Path();

        TextPainter = new Paint();
        TextPainter.setStrokeWidth(2);
        TextPainter.setTextAlign(Paint.Align.CENTER);
        TextPainter.setColor(TextColor);

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
        int Rounded = Math.min(Width/10, Height/10);
        Frame.reset();
        Frame.addRoundRect( new RectF( BorderThickness,BorderThickness,
                                              Width - (2*BorderThickness) ,
                                              Height - (2*BorderThickness) )
                                   ,Rounded,Rounded, Path.Direction.CW);
        FrameFilled.set(Frame);
        FrameFilled.setFillType(Path.FillType.WINDING);

        TextPainter.setTextSize(Height/5);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        canvas.drawPath(FrameFilled, BackgroundPainter);
        canvas.drawPath(Frame, BorderPainter);

        canvas.drawText(String.valueOf(MeanValue),0,canvas.getHeight()/2,TextPainter);
    }
}