package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Locale;

public class Monitor extends ImageView {
    private ArrayList<Statistic> Collected;
    private Path Arrow;
    private Path ArrowFilled;
    private Paint MonitorPainter;

    private static final int ArrowBorderColor = 0xff84e9f4;
    private static final int ArrowColor = 0xff00ffe5;
    private static final int TextColor = 0xfffffcfc;
    private static final int HistoryColor = 0xffffdd55;
    private static final float HistoryStrokeWidth = 20f;

    private String Unit ="";
    private Bitmap Thumbnail = null;
    private Bitmap ResizedThumbnail =null;
    private int TicksCount = 1;
    private float TicksScale = 1f;
    private String FormatDigits = "%.0f";
    private String DisplayedValue;

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
        Arrow = new Path();
        ArrowFilled = new Path();
    }

    public void setDigits(int nb) { FormatDigits = "%."+String.valueOf(nb)+"f"; }
    public void setTicksCount(int nb) { TicksCount = nb;}
    public void setTicksScale(float scale) { TicksScale = scale;}
    public void setUnit(String Unit) { this.Unit = Unit; }
    public void setThumbnail(Bitmap Thumbnail) { this.Thumbnail = Thumbnail; }

    public  void updateStatistics(ArrayList<Statistic> values) {
        Collected = values;
        Log.d("Monitor","Live value:"+Collected.get(0).value);
        DisplayedValue = String.format(Locale.ENGLISH,FormatDigits,Collected.get(0).value);
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
        Rounded = Math.min(Width/10, Height/10);

        float ArrowWidth = Width / 5;
        float ArrowHeight = ArrowWidth /2;
        Arrow.reset();
        Arrow.moveTo(0f,0f);
        Arrow.lineTo(ArrowWidth /2, 0f);
        Arrow.lineTo(0f, ArrowHeight);
        Arrow.lineTo(-ArrowWidth /2,0f);
        Arrow.close();

        ArrowFilled.set(Arrow);
        ArrowFilled.setFillType(Path.FillType.WINDING);

        HistoryStrokeHeight = Height / 5;
        HistoryOffset = Height - (HistoryStrokeHeight + Rounded);

        ResizedThumbnail = Bitmap.createScaledBitmap(Thumbnail, Height/5, Width/5, false);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        float Height = canvas.getHeight();
        float Width = canvas.getWidth();
        if ((Width == 0) || (Height == 0)) { super.onDraw(canvas);return;}

        float FontValueSize = Height/5;
        float FontTicksSize = Height/8;
        float Range = (SharedConstants.NbTicks * TicksScale);
        float PhysicToPixels = (Width - (2 * Rounded)) / Range;
        float LiveValue = Collected.get(0).value;
        int Opacity = 0;

        long StartRender = SystemClock.elapsedRealtime();


        // Drawing Live Value
        MonitorPainter.setStyle(Paint.Style.FILL);
        MonitorPainter.setTextAlign(Paint.Align.CENTER);
        MonitorPainter.setColor(TextColor);
        MonitorPainter.setTextSize(FontValueSize);
        canvas.drawText(DisplayedValue,
                        canvas.getWidth()/2,FontValueSize,
                        MonitorPainter
                       );

        // Drawing Unit string
        MonitorPainter.setStyle(Paint.Style.FILL);
        MonitorPainter.setTextAlign(Paint.Align.RIGHT);
        MonitorPainter.setColor(TextColor);
        MonitorPainter.setTextSize(FontTicksSize);
        canvas.drawText(Unit,Width-Rounded,FontValueSize,MonitorPainter);

        // Drawing Thumbnail
        canvas.drawBitmap(ResizedThumbnail, 0f,0f, null);

        // Drawing Arrow
        canvas.save();
        canvas.translate(Width / 2,FontValueSize);
        MonitorPainter.setStyle(Paint.Style.FILL);
        MonitorPainter.setColor(ArrowColor);
        canvas.drawPath(ArrowFilled, MonitorPainter);

        MonitorPainter.setStyle(Paint.Style.STROKE);
        MonitorPainter.setColor(ArrowBorderColor);
        canvas.drawPath(Arrow, MonitorPainter);
        canvas.restore();

        // Drawing History values
        canvas.save();
        canvas.translate(Width / 2,HistoryOffset);
        MonitorPainter.setColor(HistoryColor);
        MonitorPainter.setStrokeWidth(HistoryStrokeWidth);
        MonitorPainter.setStrokeCap(Paint.Cap.ROUND);
        float X;
        for (Statistic infos: Collected) {
            Opacity = MaxOpacity -  (((MaxOpacity - MinOpacity) / MaxDays) * infos.nbDays);
            if (infos.nbDays > MaxDays) Opacity = MinOpacity;
            MonitorPainter.setAlpha(Opacity);
            X = (LiveValue - infos.value) * PhysicToPixels;
            canvas.drawLine(X,0,X,HistoryStrokeHeight,MonitorPainter);
        }
        canvas.restore();

        long EndRender = SystemClock.elapsedRealtime();
        Log.d("Monitor", "Rendering was "+ (EndRender - StartRender)+ " ms.");

        super.onDraw(canvas);
    }
}