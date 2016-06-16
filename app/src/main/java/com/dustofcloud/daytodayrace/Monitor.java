package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;

public class Monitor extends ImageView {
    private ArrayList<Statistic> Collected;
    private float MeanValue;
    private Path Frame;
    private Path FrameFilled;
    private Paint MonitorPainter;

    private static final int BorderColor = 0xff84e9f4;
    private static final int BackgroundColor = 0xff00bebe;
    private static final float BorderThickness = 5f;
    private static final int TextColor = 0xfffffcfc;
    private static final int HistoryColor = 0xfffffcfc;

    private String Unit ="";
    private Bitmap Thumbnail = null;
    private int TicksCount = 1;
    private float TicksScale = 1f;
    private String FormatDigits = "%.0f";


    public Monitor(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);

        MonitorPainter = new Paint();
        Frame = new Path();
        FrameFilled = new Path();
    }

    public void setDigits(int nb) { FormatDigits = "%."+String.valueOf(nb)+"f"; }
    public void setTicksCount(int nb) { TicksCount = nb;}
    public void setTicksScale(float scale) { TicksScale = scale;}
    public void setUnit(String Unit) { this.Unit = Unit; }
    public void setThumbnail(Bitmap Thumbnail) { this.Thumbnail = Thumbnail; }

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

    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        float FontValueSize = canvas.getHeight()/5;
        float FontTicksSize = canvas.getHeight()/8;
        float Range = SharedConstants.NbTicks * TicksScale;
        float LiveValue = Collected.get(0).value;


        MonitorPainter.setStyle(Paint.Style.FILL);
        MonitorPainter.setColor(BackgroundColor);
        canvas.drawPath(FrameFilled, MonitorPainter);

        MonitorPainter.setStyle(Paint.Style.STROKE);
        MonitorPainter.setColor(BorderColor);
        MonitorPainter.setStrokeWidth(BorderThickness);
        canvas.drawPath(Frame, MonitorPainter);

        MonitorPainter.setStrokeWidth(2);
        MonitorPainter.setStyle(Paint.Style.FILL);
        MonitorPainter.setTextAlign(Paint.Align.CENTER);
        MonitorPainter.setColor(TextColor);
        MonitorPainter.setTextSize(FontValueSize);
        canvas.drawText(String.format(FormatDigits,LiveValue),canvas.getWidth()/2,FontValueSize,MonitorPainter);
    }
}