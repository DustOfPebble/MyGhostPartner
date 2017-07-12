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
import android.text.StaticLayout;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Calendar;

import core.Structures.Node;
import core.Structures.Statistic;
import core.launcher.partner.R;

public class GridedView extends VirtualView {
    private Drawable Thumbnail;
    private Bitmap GridInfo;
    private float GridCellSize;
    private int NbHrzCells = 10;
    private int NbVrtCells = 10;
    private final int TimeCell = 60*1000; // time in ms

    private Paint Grid;
    private final int GridColor = 0xff528B9E;

    private Paint UnitPainter;
    private float UnitFontSize;

    private Paint Curve;
    private final int TraceColor = 0xffFFD54A;



    private class History {
        long TimeStamp;
        Statistic Info;
    }

    private ArrayList<History> History = new ArrayList<>();

    public GridedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);

        // Loading Attributes from XML definitions ...
        if (attrs == null) return;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GridView, 0, 0);
        try
        {
            //Enabled = attributes.getDimension(styleable.GridView_Thickness);
            Thumbnail = attributes.getDrawable(R.styleable.GridView_Thumbnail);
        }
        finally { attributes.recycle();}

        setOnTouchListener(this);
        setVisibility(INVISIBLE);

        Grid = new Paint();
        Grid.setColor(GridColor);

        Curve = new Paint();
        Curve.setColor(TraceColor);


        Frame = new RectF();
        FramePainter = new Paint();
        FramePainter.setStyle(Paint.Style.STROKE);
        FramePainter.setColor(StatsStyles.BorderColor);

        UnitPainter = new Paint();
        UnitPainter.setColor(StatsStyles.TextColor);

    }

    public void update(Statistic Info) {
        History Appended = new History();
        Appended.Info =  Info;
        Appended.TimeStamp = Calendar.getInstance().getTimeInMillis();
        History.add(0,Appended);
        long HistoryTimeCurrent = History.get(0).TimeStamp;
        long HistoryTimeStart = History.get(History.size()-1).TimeStamp;
        if ((HistoryTimeCurrent - HistoryTimeStart) > NbHrzCells*TimeCell) History.remove(0);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int Width = MeasureSpec.getSize(widthMeasureSpec);
        int Height = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(Width, Height);

        GridCellSize = (float)Width /NbHrzCells;
        NbVrtCells = (int)(Height / GridCellSize)+1;

        Grid.setStrokeWidth(Math.max(2, Width/180));
        Curve.setStrokeWidth(Math.max(6, Width/60));

        // Frame settings
        FramePixelsFactor = this.getResources().getDisplayMetrics().density;
        float StrokeWidth = FramePixelsFactor  * StatsStyles.FrameBorder;
        FramePainter.setStrokeWidth(StrokeWidth);
        Frame.set(StrokeWidth/2,StrokeWidth/2,Width-StrokeWidth/2,Height-StrokeWidth/2);
        Radius = StatsStyles.FrameRadius * FramePixelsFactor;

        int IconSize = Math.max( Height/5, Width/5);
        GridInfo = Bitmap.createScaledBitmap(((BitmapDrawable)Thumbnail).getBitmap(), IconSize,IconSize, false);
        Padding = Math.min(Width/20, Height/20);

        UnitFontSize = Height/15;
        UnitPainter.setTextSize(UnitFontSize);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        float Height = canvas.getHeight();
        float Width = canvas.getWidth();
        if ((Width == 0) || (Height == 0)) { super.onDraw(canvas);return;}

        // Drawing Grid
        float Offset = (Height - (NbVrtCells * GridCellSize)) / 2;
        for(float i=1; i<NbVrtCells; i++ ) {
            canvas.drawLine(0,i*GridCellSize +Offset , Width ,i*GridCellSize + Offset, Grid);
        }
        for(float i=1; i<NbHrzCells; i++ ) {
            canvas.drawLine(i*GridCellSize,0, i*GridCellSize, Height, Grid);
        }

        // Drawing Curve
        if (!History.isEmpty()) {
            float HeightScale = Height / (NbVrtCells * 10);
            float WidthScale = Width / (NbHrzCells * TimeCell);
            float[] Graph = new float[4];
            long TimeZero = History.get(0).TimeStamp;
            float HeightReference = History.get(0).Info.Altitude;
            Graph[0] = Width;
            Graph[1] = (Height / 2);

            // Drawing History
            for (int i = 1; i < History.size(); i++) {
                History element = History.get(i);
                Graph[2] = Width - ((TimeZero - element.TimeStamp) * WidthScale);
                Graph[3] = (Height / 2) + ((HeightReference - element.Info.Altitude) * HeightScale);
                canvas.drawLines(Graph, Curve);
                Graph[0] = Graph[2];
                Graph[1] = Graph[3];
            }
        }

        // Drawing Icon
        canvas.drawBitmap(GridInfo, Padding, Padding, null);
        // Drawing Units
        UnitPainter.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("10 m",Padding + GridInfo.getWidth(),(GridInfo.getHeight()/2)+Padding , UnitPainter);
        UnitPainter.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("1 min",Padding+(GridInfo.getWidth()/2), GridInfo.getHeight()+Padding+UnitFontSize , UnitPainter);


        // Drawing surrounding Frame ...
        canvas.drawRoundRect(Frame,Radius,Radius,FramePainter);

        super.onDraw(canvas);
    }

}
