package core.launcher.Widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

import core.launcher.partner.R;

public class StatsScaled extends VirtualView {
    private String LogTag = StatsScaled.class.getSimpleName();

    private ArrayList<Float> Collected = new ArrayList<>();

    private Bitmap LoadedMarker;
    private Bitmap ResizedMarker;

    private Bitmap ResizedIcon =null;

    private float LiveValue =0f;

    private Paint VuMeterPainter;
    private SetStats Setup;
    private float DisplayedRange;
    private float PhysicToPixels;
    private float VuMeterFontSize;
    private float VuMeterStrokeWidth;
    private float VuMeterLongTicks;
    private float VuMeterShortTicks;
    private float VuMeterOffset;
    private float VuMeterStartValue = 0f;
    private float VuMeterStopValue = 0f;

    private Bitmap VuMeter;

    private Paint UnitPainter;
    private float UnitFontSize;

    private Paint HistoryPainter;
    private float HistoryStrokeWidth;
    private Bitmap HistoryStats;

    float HistoryOffset;
    float HistoryHeight;
    float Padding;

    private class Bounds {
        int First;
        int Last;
    }

    public StatsScaled(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);

        LoadedMarker = BitmapFactory.decodeResource(getResources(), R.drawable.arrow);
        HistoryPainter = new Paint();
        HistoryPainter.setColor(StatsStyles.HistoryColor);
        HistoryPainter.setStrokeCap(Paint.Cap.ROUND);

        VuMeterPainter = new Paint();
        VuMeterPainter.setStyle(Paint.Style.FILL);
        VuMeterPainter.setColor(StatsStyles.TextColor);
        VuMeterPainter.setStrokeCap(Paint.Cap.ROUND);

        UnitPainter = new Paint(VuMeterPainter);

        UnitPainter.setTextAlign(Paint.Align.RIGHT);
        VuMeterPainter.setTextAlign(Paint.Align.CENTER);

        Frame = new RectF();
        FramePainter = new Paint();
        FramePainter.setStyle(Paint.Style.STROKE);
        FramePainter.setColor(StatsStyles.BorderColor);

        setOnTouchListener(this);
        setVisibility(INVISIBLE);
    }

    public void setParams(SetStats Settings) {
        Setup = Settings;
    }

    public void setValues(float Live, ArrayList<Float> History) {
        Collected = History;
        LiveValue = Live;

        if (getVisibility() == View.INVISIBLE) return;

        Bounds Limits = RulerBounds();
        boolean Changed =  false ;
        if (VuMeterStartValue != (float) Limits.First) Changed = true;
        if (VuMeterStopValue != (float) Limits.Last) Changed = true;
        if (Changed) {
            VuMeterStartValue = Limits.First;
            VuMeterStopValue =  Limits.Last;
            CreateRulerBitmap();
        }
        if (VuMeter == null) CreateRulerBitmap();
        CreateStatisticsBitmap();
        invalidate();
    }

    private void setDrawParams(int Width, int Height) {
        if ((Width == 0) || (Height == 0)) return;
        if (null == Setup) return;

        Padding = Math.min(Width/20, Height/20);

        int IconSize = Math.max( Height/5, Width/5);
        ResizedIcon = Bitmap.createScaledBitmap(Setup.Thumb, IconSize,IconSize, false);
        ResizedMarker = Bitmap.createScaledBitmap(LoadedMarker, IconSize,IconSize, false);

        UnitFontSize = Height/6;
        UnitPainter.setTextSize(UnitFontSize);

        VuMeterOffset = ResizedMarker.getHeight() + Padding;
        VuMeterFontSize = Height/6;
        VuMeterPainter.setTextSize(VuMeterFontSize);
        VuMeterStrokeWidth = Width/50;
        VuMeterPainter.setStrokeWidth(VuMeterStrokeWidth);
        VuMeterLongTicks = Height/6 - VuMeterStrokeWidth ;
        VuMeterShortTicks = Height/8 - VuMeterStrokeWidth;

        HistoryHeight = Height/4;
        HistoryOffset = Height - (HistoryHeight + Padding);
        HistoryStrokeWidth = Width/ Setup.NbTicksShown;
        HistoryPainter.setStrokeWidth(HistoryStrokeWidth);

        // Loading for VuMeter display
        DisplayedRange = (Setup.NbTicksShown - 1) * Setup.TicksPhysicValue;
        PhysicToPixels = (Width) / DisplayedRange;

        // Frame settings
        FramePixelsFactor = this.getResources().getDisplayMetrics().density;
        float StrokeWidth = FramePixelsFactor  * StatsStyles.FrameBorder;
        FramePainter.setStrokeWidth(StrokeWidth);
        Frame.set(StrokeWidth/2,StrokeWidth/2,Width-StrokeWidth/2,Height-StrokeWidth/2);
        Radius = StatsStyles.FrameRadius * FramePixelsFactor;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int Width = MeasureSpec.getSize(widthMeasureSpec);
        int Height = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(Width, Height);
        setDrawParams(Width, Height);
        if (VuMeter != null) CreateRulerBitmap();
        if (HistoryStats != null) CreateStatisticsBitmap();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float Height = canvas.getHeight();
        float Width = canvas.getWidth();
        if ((Width == 0) || (Height == 0)) { super.onDraw(canvas);return;}

        // Drawing Unit
        canvas.drawText(Setup.Unit,Width - Padding,UnitFontSize +Padding, UnitPainter);

        // Drawing Icon
        canvas.drawBitmap(ResizedIcon, Padding, Padding, null);

        // Drawing VuMeter ...
        float VueMeterPixelShift = (Width/2) - (PhysicToPixels *(LiveValue - (int)VuMeterStartValue));
        if (VuMeter!=null) canvas.drawBitmap(VuMeter, VueMeterPixelShift, VuMeterOffset, null);

        // Drawing Marker
        canvas.drawBitmap(ResizedMarker,(Width/2) - (ResizedMarker.getWidth()/2), Padding, null);

        // Drawing History values
        if (HistoryStats!=null) canvas.drawBitmap(HistoryStats,0f, HistoryOffset, null);

        // Drawing Frame ...
        canvas.drawRoundRect(Frame,Radius,Radius,FramePainter);

        super.onDraw(canvas);
    }

    private Bounds RulerBounds() {
        int PreviousUnit = (int) LiveValue - (int) (DisplayedRange / 2);
        int NextUnit = (int) LiveValue + (int) (DisplayedRange / 2);
        int PreviousLabel = PreviousUnit;
        int NextLabel = NextUnit;

        int Modulo;
        int UnitLabelStep = (int) (Setup.TicksTextGap * Setup.TicksPhysicValue);
        if (UnitLabelStep < 1) UnitLabelStep = 1;
        Modulo = PreviousUnit % UnitLabelStep;
        if (Modulo > 0) PreviousLabel = PreviousUnit - Modulo;
        if ((PreviousUnit - PreviousLabel) < 1) PreviousLabel = PreviousLabel - UnitLabelStep;

        Modulo = NextUnit % UnitLabelStep;
        if (Modulo > 0) NextLabel = NextUnit + (UnitLabelStep - Modulo);
        if ((NextLabel - NextUnit) < 2) NextLabel = NextLabel + UnitLabelStep;

        Bounds Limits = new Bounds();
        Limits.First = PreviousLabel;
        Limits.Last = NextLabel;
        return Limits;
    }

    private void CreateRulerBitmap() {
        int RulerWidth = (int) ((VuMeterStopValue - VuMeterStartValue) * PhysicToPixels);
        VuMeter = Bitmap.createBitmap(RulerWidth,(int)(VuMeterFontSize+VuMeterLongTicks+VuMeterStrokeWidth), Bitmap.Config.ARGB_8888);
        Canvas DrawVuMeter = new Canvas(VuMeter);

        float TicksPhysic = VuMeterStartValue;
        float TicksPixels = 0;
        float NbTicksCount = Setup.TicksTextGap; // Force a Label on first ticks
        float LongTickBeginY = VuMeterStrokeWidth/2;
        float LongTicksEndY = VuMeterLongTicks + LongTickBeginY ;
        float ShortTicksBeginY = VuMeterStrokeWidth/2;
        float ShortTicksEndY = VuMeterShortTicks + ShortTicksBeginY ;

        while (TicksPhysic <= VuMeterStopValue)
        {
            if ((TicksPhysic >= Setup.PhysicsRangeMin) && (TicksPhysic <= Setup.PhysicsRangeMax)) {
                if (NbTicksCount >= Setup.TicksTextGap) {
                    DrawVuMeter.drawLine(TicksPixels, LongTickBeginY, TicksPixels, LongTicksEndY, VuMeterPainter);
                    DrawVuMeter.drawText(String.format("%.0f", TicksPhysic), TicksPixels, VuMeterLongTicks + VuMeterFontSize + VuMeterStrokeWidth, VuMeterPainter);
                    NbTicksCount = 0;
                } else {
                    DrawVuMeter.drawLine(TicksPixels, ShortTicksBeginY, TicksPixels, ShortTicksEndY, VuMeterPainter);
                }
            }

            TicksPixels += (PhysicToPixels* Setup.TicksPhysicValue);
            TicksPhysic += Setup.TicksPhysicValue;
            NbTicksCount++;
        }
    }

    private void CreateStatisticsBitmap()  {
        if (Collected.isEmpty()) return;
        HistoryStats = Bitmap.createBitmap(this.getWidth(),(int)(HistoryHeight), Bitmap.Config.ARGB_8888);
        Canvas DrawHistoryStats = new Canvas(HistoryStats);

        int[] Classes = new int[Setup.NbTicksShown];
        int MaxClasse = 0;
        int ClasseIndex;
        int IndexMax= Setup.NbTicksShown /2;

        for (Float Stats: Collected) {
            ClasseIndex = (int)((Stats - LiveValue)/ Setup.TicksPhysicValue)+IndexMax;
            if (ClasseIndex < 0) continue;
            if (ClasseIndex >= Setup.NbTicksShown) continue;
            Classes[ClasseIndex]++;
            if (Classes[ClasseIndex] > MaxClasse) MaxClasse = Classes[ClasseIndex];
        }

        float X = HistoryStrokeWidth/2;
        float HistoryBeginY;
        if (MaxClasse ==0) MaxClasse=1;
        float Factor =  (HistoryHeight - HistoryStrokeWidth) /MaxClasse;
        float HistoryEndY = HistoryHeight - HistoryStrokeWidth/2;
        float TicksGraphic = DrawHistoryStats.getWidth() / Setup.NbTicksShown;

        for (int Index = 0; Index < Setup.NbTicksShown; Index++) {
            HistoryBeginY = HistoryEndY - (Factor * Classes[Index]);
            DrawHistoryStats.drawLine(X,HistoryBeginY,X,HistoryEndY, HistoryPainter);
            X += TicksGraphic;
        }
    }
}