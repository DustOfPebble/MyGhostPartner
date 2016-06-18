package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Locale;

public class Monitor extends ImageView {
    private ArrayList<Statistic> Collected;
    private Bitmap LoadedMarker;
    private Bitmap SizedMarker;
    private Paint MonitorPainter;

    private static final int TextColor = 0xfffffcfc;
    private static final int HistoryColor = 0xffffdd55;
    private static final float HistoryStrokeWidth = 20f;

    private String Unit ="";
    private Bitmap Thumbnail = null;
    private Bitmap SizedThumbnail =null;

    private Bitmap SlidingRule = null;
    private float MinRange, MaxRange;
    private int NbTicks;
    private float Ticks, TicksLabel;


    private int MaxOpacity = 256;
    private int MinOpacity = 90;
    private int MaxDays = 5;

    float HistoryOffset;
    float HistoryStrokeHeight;
    float Rounded;

    public Monitor(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);

        MonitorPainter = new Paint();
        LoadedMarker = BitmapFactory.decodeResource(getResources(),R.drawable.arrow);
    }

    public void setRuleSettings(float Min, float Max, int TicksDisplayed, float TicksStep, float TicksStepLabel) {
        MinRange = Min;
        MaxRange = Max;
        NbTicks = TicksDisplayed;
        Ticks = TicksStep;
        TicksLabel = TicksStepLabel;
    }

    public void setUnit(String Unit) { this.Unit = Unit; }
    public void setThumbnail(Bitmap Thumbnail) { this.Thumbnail = Thumbnail; }

    public  void updateStatistics(ArrayList<Statistic> values) {
        Collected = values;
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
        Rounded = Math.min(Width/20, Height/20);

        HistoryStrokeHeight = Height / 5;
        HistoryOffset = Height - (HistoryStrokeHeight + Rounded);

        int ThumbnailSize = Math.max( Height/5, Width/5);

        SizedThumbnail = Bitmap.createScaledBitmap(Thumbnail, ThumbnailSize,ThumbnailSize, false);
        SizedMarker = Bitmap.createScaledBitmap(LoadedMarker, ThumbnailSize,ThumbnailSize, false);

        // Rebuild Rule

    }

    @Override
    protected void onDraw(Canvas canvas) {

        float Height = canvas.getHeight();
        float Width = canvas.getWidth();
        if ((Width == 0) || (Height == 0)) { super.onDraw(canvas);return;}

        float FontValueSize = Height/5;
        float FontTicksSize = Height/8;
        float Range = (SharedConstants.NbTicks * Ticks);
        float PhysicToPixels = (Width - (2 * Rounded)) / Range;
        float LiveValue = Collected.get(0).value;
        int Opacity = 0;

        long StartRender = SystemClock.elapsedRealtime();

        // Drawing Unit string
        MonitorPainter.setStyle(Paint.Style.FILL);
        MonitorPainter.setTextAlign(Paint.Align.RIGHT);
        MonitorPainter.setColor(TextColor);
        MonitorPainter.setTextSize(FontTicksSize);
        canvas.drawText(Unit,Width-Rounded,FontValueSize,MonitorPainter);

        // Drawing Thumbnail
        canvas.drawBitmap(SizedThumbnail, Rounded,Rounded, null);

        // Drawing Marker
        canvas.drawBitmap(SizedMarker,(Width/2) - (SizedMarker.getWidth()/2),Rounded, null);

        // Drawing History values
        canvas.save();
        canvas.translate(Width / 2,HistoryOffset);
        MonitorPainter.setColor(HistoryColor);
        MonitorPainter.setStrokeWidth(HistoryStrokeWidth);
        MonitorPainter.setStrokeCap(Paint.Cap.ROUND);
        float X;
        for (Statistic Instant: Collected) {
            Opacity = MaxOpacity -  (((MaxOpacity - MinOpacity) / MaxDays) * Instant.nbDays);
            if (Instant.nbDays > MaxDays) Opacity = MinOpacity;
            MonitorPainter.setAlpha(Opacity);
            X = (LiveValue - Instant.value) * PhysicToPixels;
            canvas.drawLine(X,0,X,HistoryStrokeHeight,MonitorPainter);
        }
        canvas.restore();

        long EndRender = SystemClock.elapsedRealtime();
        Log.d("Monitor", "Rendering was "+ (EndRender - StartRender)+ " ms.");

        super.onDraw(canvas);
    }
}