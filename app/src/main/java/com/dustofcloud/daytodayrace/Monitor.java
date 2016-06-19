package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;

public class Monitor extends ImageView {
    private static final int TextColor = 0xfffffcfc;
    private static final int HistoryColor = 0xffffdd55;
    private float HistoryStrokeWidth;

    private ArrayList<Statistic> Collected;

    private Bitmap LoadedMarker;
    private Bitmap ResizedMarker;

    private Bitmap LoadedIcon = null;
    private Bitmap ResizedIcon =null;

    private String Unit ="";

    private Paint VuMeterPainter;
    private int NbTicks;
    private float DisplayedRange;
    private float Ticks, TicksLabel;
    private float PhysicToPixels;
    private float VuMeterFontSize;
    private float VuMeterStrokeWidth;
    private float VuMeterLongTicks;
    private float VuMeterShortTicks;
    private float VuMeterOffset;

    private Paint UnitPainter;
    private float UnitFontSize;

    private Paint HistoryPainter;
    private int MaxOpacity = 256;
    private int MinOpacity = 90;
    private int MaxDays = 5;

    float HistoryOffset;
    float HistoryStrokeHeight;
    float Padding;

    public Monitor(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);

        LoadedMarker = BitmapFactory.decodeResource(getResources(),R.drawable.arrow);
        HistoryPainter = new Paint();
        HistoryPainter.setColor(HistoryColor);
        HistoryPainter.setStrokeCap(Paint.Cap.ROUND);

        VuMeterPainter = new Paint();
        VuMeterPainter.setStyle(Paint.Style.FILL);
        VuMeterPainter.setColor(TextColor);
        VuMeterPainter.setStrokeCap(Paint.Cap.ROUND);

        UnitPainter = new Paint(VuMeterPainter);

        UnitPainter.setTextAlign(Paint.Align.RIGHT);
        VuMeterPainter.setTextAlign(Paint.Align.CENTER);
    }

    public void setRuleSettings(int TicksDisplayed, float TicksStep, float TicksStepLabel) {
        NbTicks = TicksDisplayed;
        Ticks = TicksStep;
        TicksLabel = TicksStepLabel;
    }

    public void setUnit(String Unit) { this.Unit = Unit; }
    public void setIcon(Bitmap ProvidedIcon) { this.LoadedIcon = ProvidedIcon; }

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

        Padding = Math.min(Width/20, Height/20);

        int IconSize = Math.max( Height/5, Width/5);
        ResizedIcon = Bitmap.createScaledBitmap(LoadedIcon, IconSize,IconSize, false);
        ResizedMarker = Bitmap.createScaledBitmap(LoadedMarker, IconSize,IconSize, false);

        UnitFontSize = Height/6;
        UnitPainter.setTextSize(UnitFontSize);

        VuMeterOffset = ResizedMarker.getHeight() + Padding;
        VuMeterFontSize = Height / 6;
        VuMeterPainter.setTextSize(VuMeterFontSize);
        VuMeterStrokeWidth = Width / 30;
        VuMeterPainter.setStrokeWidth(VuMeterStrokeWidth);
        VuMeterLongTicks = Height / 6;
        VuMeterShortTicks = Height / 8;

        HistoryOffset = VuMeterOffset + VuMeterLongTicks + VuMeterFontSize;
        HistoryStrokeHeight = Height / 5;
        HistoryOffset = Height - (HistoryStrokeHeight + Padding);
        HistoryStrokeWidth = Width / 20;
        HistoryPainter.setStrokeWidth(HistoryStrokeWidth);

        // Loading for VuMeter display
        DisplayedRange = (NbTicks * Ticks);
        PhysicToPixels = (Width - (2 * Padding)) / DisplayedRange;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float Height = canvas.getHeight();
        float Width = canvas.getWidth();
        if ((Width == 0) || (Height == 0)) { super.onDraw(canvas);return;}

        float LiveValue = Collected.get(0).value;

        long StartRender = SystemClock.elapsedRealtime();

        // Drawing Unit
        canvas.drawText(Unit,Width - Padding,UnitFontSize +Padding, UnitPainter);

        // Drawing Icon
        canvas.drawBitmap(ResizedIcon, Padding, Padding, null);

        // Drawing VuMeter ...
        canvas.save();
        canvas.translate(Width / 2,VuMeterOffset);

        float Extension = DisplayedRange /2;
        float TicksPhysic = -Extension + LiveValue;
        float TicksPixels = -Extension * PhysicToPixels;
        float TicksIsLabel = 0f;

        while (TicksPhysic < (LiveValue + Extension))
        {
            if (TicksIsLabel >= TicksLabel)
            {
                canvas.drawLine(TicksPixels,0f,TicksPixels, VuMeterLongTicks, VuMeterPainter);
                canvas.drawText(String.format("%.0f", TicksPhysic),TicksPixels, VuMeterLongTicks +VuMeterFontSize, VuMeterPainter);
                TicksIsLabel = 0f;
            }
            else
            {   canvas.drawLine(TicksPixels,0f,TicksPixels, VuMeterShortTicks, VuMeterPainter); }

            TicksPixels += (PhysicToPixels*Ticks);
            TicksPhysic += Ticks;
            TicksIsLabel += Ticks;
        }
        canvas.restore();

        // Drawing Marker
        canvas.drawBitmap(ResizedMarker,(Width/2) - (ResizedMarker.getWidth()/2), Padding, null);

        // Drawing History values
        int Opacity;
        canvas.save();
        canvas.translate(Width / 2,HistoryOffset);
        float X;
        for (Statistic Instant: Collected) {
            Opacity = MaxOpacity -  (((MaxOpacity - MinOpacity) / MaxDays) * Instant.nbDays);
            if (Instant.nbDays > MaxDays) Opacity = MinOpacity;
            HistoryPainter.setAlpha(Opacity);
            X = (LiveValue - Instant.value) * PhysicToPixels;
            canvas.drawLine(X,0,X,HistoryStrokeHeight, HistoryPainter);
        }
        canvas.restore();

        long EndRender = SystemClock.elapsedRealtime();
        Log.d("Monitor", "Rendering was "+ (EndRender - StartRender)+ " ms.");

        super.onDraw(canvas);
    }
}