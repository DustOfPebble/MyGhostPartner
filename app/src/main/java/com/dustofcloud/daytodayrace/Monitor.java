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
    private static final int HistoryColor = 0xffffe57e;
    private float HistoryStrokeWidth;

    private ArrayList<Statistic> Collected;

    private Bitmap LoadedMarker;
    private Bitmap ResizedMarker;

    private Bitmap LoadedIcon = null;
    private Bitmap ResizedIcon =null;

    private String Unit ="";

    private float LiveValue =0f;

    private Paint VuMeterPainter;
    private int NbTicksDisplayed;
    private float DisplayedRange;
    private float TicksStep, NbTicksLabel;
    private float PhysicMax, PhysicMin;
    private float PhysicToPixels;
    private float VuMeterFontSize;
    private float VuMeterStrokeWidth;
    private float VuMeterLongTicks;
    private float VuMeterShortTicks;
    private float VuMeterOffset;
    private float VuMeterStartValue;

    private Bitmap VuMeter;

    private Paint UnitPainter;
    private float UnitFontSize;

    private Paint HistoryPainter;
    private int MaxOpacity = 256;
    private int MinOpacity = 90;
    private int MaxDays = 5;
    private Bitmap HistoryStats;

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

    public void setRuleSettings(int NbTicksDisplayed,  int NbTicksLabel, float TicksStep, float PhysicMin, float PhysicMax) {
        this.NbTicksDisplayed = NbTicksDisplayed;
        this.NbTicksLabel = NbTicksLabel;
        this.TicksStep = TicksStep;
        this.PhysicMax = PhysicMax;
        this.PhysicMin = PhysicMin;
    }

    public void setUnit(String Unit) { this.Unit = Unit; }
    public void setIcon(Bitmap ProvidedIcon) { this.LoadedIcon = ProvidedIcon; }

    public  void updateStatistics(ArrayList<Statistic> values) {
        Collected = values;
        LiveValue = Collected.get(0).value;

        long StartRender = SystemClock.elapsedRealtime();

        int FirstLabel = (int)LiveValue - (int)(DisplayedRange/2)-2;
        int LastLabel = (int)LiveValue + (int)(DisplayedRange/2)+3;
        int VuMeterWidth = (int) ((LastLabel - FirstLabel) * PhysicToPixels);
        VuMeter = Bitmap.createBitmap(VuMeterWidth,(int)(VuMeterFontSize+VuMeterLongTicks+VuMeterStrokeWidth), Bitmap.Config.ARGB_8888);
        Canvas DrawVuMeter = new Canvas(VuMeter);

        float TicksPhysic = (float)FirstLabel;
        float TicksPixels = 0;
        float NbTicksCount = NbTicksLabel; // Force a Label on first ticks
        float LongTickBeginY = VuMeterStrokeWidth/2;
        float LongTicksEndY = VuMeterLongTicks + LongTickBeginY ;
        float ShortTicksBeginY = VuMeterStrokeWidth/2;
        float ShortTicksEndY = VuMeterShortTicks + ShortTicksBeginY ;

        while (TicksPhysic <= (float)LastLabel)
        {
            if ((TicksPhysic >= PhysicMin) && (TicksPhysic <= PhysicMax)) {
                if (NbTicksCount >= NbTicksLabel) {
                    DrawVuMeter.drawLine(TicksPixels, LongTickBeginY, TicksPixels, LongTicksEndY, VuMeterPainter);
                    DrawVuMeter.drawText(String.format("%.0f", TicksPhysic), TicksPixels, VuMeterLongTicks + VuMeterFontSize + VuMeterStrokeWidth, VuMeterPainter);
                    NbTicksCount = 0;
                } else {
                    DrawVuMeter.drawLine(TicksPixels, ShortTicksBeginY, TicksPixels, ShortTicksEndY, VuMeterPainter);
                }
            }

            TicksPixels += (PhysicToPixels* TicksStep);
            TicksPhysic += TicksStep;
            NbTicksCount++;
        }
        VuMeterStartValue = (float) FirstLabel;

        HistoryStats = Bitmap.createBitmap(this.getWidth(),(int)(HistoryStrokeHeight + HistoryStrokeWidth), Bitmap.Config.ARGB_8888);
        Canvas DrawHistoryStats = new Canvas(HistoryStats);
        DrawHistoryStats.translate(HistoryStats.getWidth() / 2, 0f);
        float HistoryBeginY = HistoryStrokeWidth/2;
        float HistoryEndY = HistoryBeginY + HistoryStrokeHeight ;

        int Opacity;
        float X;
        for (Statistic Instant: Collected) {
            Opacity = MaxOpacity -  (((MaxOpacity - MinOpacity) / MaxDays) * Instant.nbDays);
            if (Instant.nbDays > MaxDays) Opacity = MinOpacity;
            HistoryPainter.setAlpha(Opacity);
            X = (LiveValue - Instant.value) * PhysicToPixels;
            DrawHistoryStats.drawLine(X,HistoryBeginY,X,HistoryEndY, HistoryPainter);
        }

        long EndRender = SystemClock.elapsedRealtime();
        Log.d("Monitor", "Bitmap update was "+ (EndRender - StartRender)+ " ms.");

        // Requesting a View redraw
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
        VuMeterFontSize = Height/6;
        VuMeterPainter.setTextSize(VuMeterFontSize);
        VuMeterStrokeWidth = Width/30;
        VuMeterPainter.setStrokeWidth(VuMeterStrokeWidth);
        VuMeterLongTicks = Height/6 - VuMeterStrokeWidth ;
        VuMeterShortTicks = Height/8 - VuMeterStrokeWidth;

        HistoryStrokeWidth = Width/20;
        HistoryStrokeHeight = Height/4 - HistoryStrokeWidth;
        HistoryOffset = Height - (HistoryStrokeHeight + Padding);
        HistoryPainter.setStrokeWidth(HistoryStrokeWidth);
        HistoryOffset = Height - Padding - HistoryStrokeWidth - HistoryStrokeHeight;

        // Loading for VuMeter display
        DisplayedRange = (NbTicksDisplayed - 1) * TicksStep;
        PhysicToPixels = (Width) / DisplayedRange;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float Height = canvas.getHeight();
        float Width = canvas.getWidth();
        if ((Width == 0) || (Height == 0)) { super.onDraw(canvas);return;}

        // Drawing Unit
        canvas.drawText(Unit,Width - Padding,UnitFontSize +Padding, UnitPainter);

        // Drawing Icon
        canvas.drawBitmap(ResizedIcon, Padding, Padding, null);

        // Drawing VuMeter ...
        float VueMeterPixelShift = (Width/2) - (PhysicToPixels *(LiveValue - (int)VuMeterStartValue));
        canvas.drawBitmap(VuMeter, VueMeterPixelShift, VuMeterOffset, null);

        // Drawing Marker
        canvas.drawBitmap(ResizedMarker,(Width/2) - (ResizedMarker.getWidth()/2), Padding, null);

        // Drawing History values
        canvas.drawBitmap(HistoryStats,0f, HistoryOffset, null);

        super.onDraw(canvas);
    }
}