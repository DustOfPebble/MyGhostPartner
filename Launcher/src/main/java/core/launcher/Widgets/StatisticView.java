package core.launcher.Widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

import core.Settings.Parameters;
import core.Structures.Coords2D;
import core.Structures.Extension;
import core.Structures.Frame;
import core.Structures.Node;
import core.launcher.partner.R;

public class StatisticView extends ComputedView {
    private String LogTag = StatisticView.class.getSimpleName();

    private ArrayList<Float> Collected = new ArrayList<>();
    private float LiveValue =0f;


    private Bitmap ViewIcon;
    private Bitmap ViewArrow;

    private Drawable IconResource;
    private int NbTicksShown;
    private int LabelTicksCount;
    private float TicksPhysicValue;
    private float PhysicRangeMin;
    private float PhysicRangeMax;
    private String RuleUnit;

    private Paint VuMeterPainter;
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

    private class Bounds {
        int First;
        int Last;
    }

    public StatisticView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);
        WidgetMode = WidgetEnums.StatsView;

        // Loading Attributes from XML definitions ...
        if (attrs == null) return;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StatisticView, 0, 0);
        try
        {
            IconResource = attributes.getDrawable(R.styleable.StatisticView_sticker);
            RuleUnit = attributes.getString(R.styleable.StatisticView_rule_unit);
            NbTicksShown = attributes.getInt(R.styleable.StatisticView_ticks_count,10);
            LabelTicksCount = attributes.getInt(R.styleable.StatisticView_label_ticks_count,1);
            PhysicRangeMax = attributes.getFloat(R.styleable.StatisticView_physic_max_value, 1f);
            PhysicRangeMin = attributes.getFloat(R.styleable.StatisticView_physic_min_value, 0f);
            TicksPhysicValue = attributes.getFloat(R.styleable.StatisticView_physic_ticks_value, 1f);
        }
        finally { attributes.recycle();}

        HistoryPainter = new Paint();
        HistoryPainter.setColor(StyleSheet.HistoryColor);
        HistoryPainter.setStrokeCap(Paint.Cap.ROUND);

        VuMeterPainter = new Paint();
        VuMeterPainter.setStyle(Paint.Style.FILL);
        VuMeterPainter.setColor(StyleSheet.TextColor);
        VuMeterPainter.setStrokeCap(Paint.Cap.ROUND);

        UnitPainter = new Paint(VuMeterPainter);

        UnitPainter.setTextAlign(Paint.Align.RIGHT);
        VuMeterPainter.setTextAlign(Paint.Align.CENTER);

        Frame = new RectF();
        FramePainter = new Paint();
        FramePainter.setStyle(Paint.Style.STROKE);
        FramePainter.setColor(StyleSheet.BorderColor);

        setOnTouchListener(this);
        setVisibility(INVISIBLE);
    }

    @Override
    public void pushNodes(ArrayList<Node> Nodes, Node Live){
        Collected = FieldsProcessing.get(Nodes);
        LiveValue = FieldsProcessing.get(Live);

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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            setFrameProperties();
            int Height = bottom-top;
            int Width = right-left;

            Padding = Math.min(Width/20, Height/20);

            UnitFontSize = Height/6;
            UnitPainter.setTextSize(UnitFontSize);

            // Updating Icon sizes
            int IconSize = Math.max( Height/5, Width/5);
            ViewIcon = Bitmap.createScaledBitmap(((BitmapDrawable) IconResource).getBitmap(), IconSize, IconSize, false);
            ViewArrow = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.arrow), IconSize, IconSize, false);

            VuMeterOffset = ViewArrow.getHeight() + Padding;
            VuMeterFontSize = Height/6;
            VuMeterPainter.setTextSize(VuMeterFontSize);
            VuMeterStrokeWidth = Width/50;
            VuMeterPainter.setStrokeWidth(VuMeterStrokeWidth);
            VuMeterLongTicks = Height/6 - VuMeterStrokeWidth ;
            VuMeterShortTicks = Height/8 - VuMeterStrokeWidth;

            HistoryHeight = Height/4;
            HistoryOffset = Height - (HistoryHeight + Padding);
            HistoryStrokeWidth = Width/ NbTicksShown;
            HistoryPainter.setStrokeWidth(HistoryStrokeWidth);

            // Loading for VuMeter display
            DisplayedRange = (NbTicksShown - 1) * TicksPhysicValue;
            PhysicToPixels = (Width) / DisplayedRange;

            if (VuMeter != null) CreateRulerBitmap();
            if (HistoryStats != null) CreateStatisticsBitmap();
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int Width = MeasureSpec.getSize(widthMeasureSpec);
        int Height = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(Width, Height);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float Height = canvas.getHeight();
        float Width = canvas.getWidth();
        if ((Width == 0) || (Height == 0)) { super.onDraw(canvas);return;}

        // Drawing Unit
        canvas.drawText(RuleUnit,Width - Padding,UnitFontSize +Padding, UnitPainter);

        // Drawing Icon
        canvas.drawBitmap(ViewIcon, Padding, Padding, null);

        // Drawing VuMeter ...
        float VueMeterPixelShift = (Width/2) - (PhysicToPixels *(LiveValue - (int)VuMeterStartValue));
        if (VuMeter!=null) canvas.drawBitmap(VuMeter, VueMeterPixelShift, VuMeterOffset, null);

        // Drawing Marker
        canvas.drawBitmap(ViewArrow,(Width/2) - (ViewArrow.getWidth()/2), Padding, null);

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
        int UnitLabelStep = (int) (LabelTicksCount * TicksPhysicValue);
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
        float NbTicksCount = LabelTicksCount; // Force a Label on first ticks
        float LongTickBeginY = VuMeterStrokeWidth/2;
        float LongTicksEndY = VuMeterLongTicks + LongTickBeginY ;
        float ShortTicksBeginY = VuMeterStrokeWidth/2;
        float ShortTicksEndY = VuMeterShortTicks + ShortTicksBeginY ;

        while (TicksPhysic <= VuMeterStopValue)
        {
            if ((TicksPhysic >= PhysicRangeMin) && (TicksPhysic <= PhysicRangeMax)) {
                if (NbTicksCount >= LabelTicksCount) {
                    DrawVuMeter.drawLine(TicksPixels, LongTickBeginY, TicksPixels, LongTicksEndY, VuMeterPainter);
                    DrawVuMeter.drawText(String.format("%.0f", TicksPhysic), TicksPixels, VuMeterLongTicks + VuMeterFontSize + VuMeterStrokeWidth, VuMeterPainter);
                    NbTicksCount = 0;
                } else {
                    DrawVuMeter.drawLine(TicksPixels, ShortTicksBeginY, TicksPixels, ShortTicksEndY, VuMeterPainter);
                }
            }

            TicksPixels += (PhysicToPixels* TicksPhysicValue);
            TicksPhysic += TicksPhysicValue;
            NbTicksCount++;
        }
    }

    private void CreateStatisticsBitmap()  {
        if (Collected.isEmpty()) return;
        HistoryStats = Bitmap.createBitmap(this.getWidth(),(int)(HistoryHeight), Bitmap.Config.ARGB_8888);
        Canvas DrawHistoryStats = new Canvas(HistoryStats);

        int[] Categories = new int[NbTicksShown];
        int CategoryMax = 0;
        int CategoryIndex;
        int IndexMax= NbTicksShown /2;

        for (Float Stats: Collected) {
            CategoryIndex = (int)((Stats - LiveValue)/ TicksPhysicValue)+IndexMax;
            if (CategoryIndex < 0) continue;
            if (CategoryIndex >= NbTicksShown) continue;
            Categories[CategoryIndex]++;
            if (Categories[CategoryIndex] > CategoryMax) CategoryMax = Categories[CategoryIndex];
        }

        float X = HistoryStrokeWidth/2;
        float HistoryBeginY;
        if (CategoryMax ==0) CategoryMax=1;
        float Factor =  (HistoryHeight - HistoryStrokeWidth) /CategoryMax;
        float HistoryEndY = HistoryHeight - HistoryStrokeWidth/2;
        float TicksGraphic = DrawHistoryStats.getWidth() / NbTicksShown;

        for (int Index = 0; Index < NbTicksShown; Index++) {
            HistoryBeginY = HistoryEndY - (Factor * Categories[Index]);
            DrawHistoryStats.drawLine(X,HistoryBeginY,X,HistoryEndY, HistoryPainter);
            X += TicksGraphic;
        }
    }
}