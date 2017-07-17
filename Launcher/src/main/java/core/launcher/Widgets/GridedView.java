package core.launcher.Widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import core.Structures.Statistic;
import core.launcher.partner.R;

public class GridedView extends VirtualView {
    private String LogTag = GridedView.class.getSimpleName();
    private Drawable IconResource;
    private Bitmap ViewIcon;
    private Bitmap WidthArrow;
    private Bitmap HeightArrow;

    private float GridCellSize;
    private float GridHeightOffset;
    private float GridWidthOffset;
    private int NbHrzCells = 10;
    private int NbVrtCells = 10;
    private final int TimeCell = 60*1000; // time in ms

    private Paint Grid;
    private float[] HrzGridLines;
    private float[] VrtGridLines;
    private final int GridColor = 0xff528B9E;

    private Paint ScalePainter;
    private float ScaleFontSize;

    private Paint Curve;
    private float[] CurvePoints;
    private final int CurveColor = 0xffFFD54A;

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
            IconResource = attributes.getDrawable(R.styleable.GridView_Thumbnail);
        }
        finally { attributes.recycle();}

        setOnTouchListener(this);
        setVisibility(INVISIBLE);

        Grid = new Paint();
        Grid.setColor(GridColor);
        VrtGridLines = new float[0];
        HrzGridLines = new float[0];

        Curve = new Paint();
        Curve.setColor(CurveColor);
        CurvePoints = new float[0];

        Frame = new RectF();
        FramePainter = new Paint();
        FramePainter.setStyle(Paint.Style.STROKE);
        FramePainter.setColor(StatsStyles.BorderColor);

        ScalePainter = new Paint();
        ScalePainter.setColor(StatsStyles.TextColor);
    }

    public void update(Statistic Info) {
        // Updating stored records
        History Appended = new History();
        Appended.Info =  Info;
        Appended.TimeStamp = Calendar.getInstance().getTimeInMillis();
        History.add(0,Appended);
        long HistoryTimeCurrent = History.get(0).TimeStamp;
        long HistoryTimeStart = History.get(History.size()-1).TimeStamp;
        if ((HistoryTimeCurrent - HistoryTimeStart) > NbHrzCells*TimeCell) History.remove(0);

        // Updating Graph data
        float HeightScale = (float)this.getHeight() / (float)(NbVrtCells * 10);
        float WidthScale = (float)this.getWidth() / (float)(NbHrzCells * TimeCell);
        long TimeZero = History.get(0).TimeStamp;
        float HeightReference = History.get(0).Info.Altitude;
        float XStart = this.getWidth();
        float YStart = (this.getHeight() / 2);
        CurvePoints = new float[History.size()*4];
        for (int i = 0; i < History.size(); i++) {
            History element = History.get(i);
            float XStop = this.getWidth() - ((TimeZero - element.TimeStamp) * WidthScale);
            float YStop = (this.getHeight() / 2) + ((HeightReference - element.Info.Altitude) * HeightScale);
            CurvePoints[i*4 +0] = XStart;
            CurvePoints[i*4 +1] = YStart;
            CurvePoints[i*4 +2] = XStop;
            CurvePoints[i*4 +3] = YStop;
            XStart = XStop;
            YStart = YStop;
        }

        // Requesting a View redraw
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            int height = bottom-top;
            int width = right-left;

            Curve.setStrokeWidth(Math.max(6, width/60));
            Padding = Math.min(width/30, height/30);

            // Drawing Grid
            GridCellSize = (float)width /NbHrzCells;
            NbVrtCells = (int)(height / GridCellSize)+1;
            VrtGridLines = new float[NbHrzCells * 4];
            HrzGridLines = new float[NbVrtCells * 4];

            Grid.setStrokeWidth(Math.max(2, width/180));
            GridHeightOffset = (height - (NbVrtCells * GridCellSize)) / 2;
            GridWidthOffset = 0;
            for(int i=0; i<NbVrtCells; i++ ) {
                float y = i * GridCellSize + GridHeightOffset;
                HrzGridLines[i*4 + 0] = 0;
                HrzGridLines[i*4 + 1] = y;
                HrzGridLines[i*4 + 2] = width;
                HrzGridLines[i*4 + 3] = y;
            }
            for (int i=0; i < NbHrzCells; i++) {
                float x = (i+1) * GridCellSize + GridWidthOffset;
                VrtGridLines[i*4 + 0] = x;
                VrtGridLines[i*4 + 1] = 0;
                VrtGridLines[i*4 + 2] = x;
                VrtGridLines[i*4 + 3] = height;
            }

            // Updating Font size
            ScaleFontSize = height/12;
            ScalePainter.setTextSize(ScaleFontSize);

            // Updating Icon sizes
            int IconSize = Math.max( height/6, width/6);
            ViewIcon = Bitmap.createScaledBitmap(((BitmapDrawable) IconResource).getBitmap(), IconSize, IconSize, false);
            HeightArrow = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.height_arrow), (int)GridCellSize,(int)GridCellSize, false);
            WidthArrow = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.width_arrow), (int)GridCellSize,(int)GridCellSize, false);
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

        // Frame settings
        FramePixelsFactor = this.getResources().getDisplayMetrics().density;
        float StrokeWidth = FramePixelsFactor  * StatsStyles.FrameBorder;
        FramePainter.setStrokeWidth(StrokeWidth);
        Frame.set(StrokeWidth/2,StrokeWidth/2,Width-StrokeWidth/2,Height-StrokeWidth/2);
        Radius = StatsStyles.FrameRadius * FramePixelsFactor;
     }

    @Override
    protected void onDraw(Canvas canvas) {

        float Height = canvas.getHeight();
        float Width = canvas.getWidth();
        if ((Width == 0) || (Height == 0)) { super.onDraw(canvas);return;}

        // Drawing Grid
        canvas.drawLines(VrtGridLines, Grid);
        canvas.drawLines(HrzGridLines, Grid);

        // Drawing Curve
        if (CurvePoints.length > 0) canvas.drawLines(CurvePoints, Curve);

        // Drawing Icon
        canvas.drawBitmap(ViewIcon, Width - Padding - ViewIcon.getWidth(), Padding, null);

        // Drawing Units
        String Unit;
        Rect Container = new Rect();
        float X,Y;
        X = 2 * GridCellSize + GridWidthOffset;
        Y = (NbVrtCells -2)* GridCellSize + GridHeightOffset;
        canvas.drawBitmap(WidthArrow,X,Y,null);
        ScalePainter.setTextAlign(Paint.Align.CENTER);
        Unit = "1mn";
        ScalePainter.getTextBounds(Unit,0,2,Container);
        canvas.drawText(Unit, X + WidthArrow.getWidth()/2, Y, ScalePainter);

        X = (NbHrzCells -2)* GridCellSize + GridWidthOffset;
        Y = (NbVrtCells -2)* GridCellSize + GridHeightOffset;
        canvas.drawBitmap(HeightArrow,X,Y, null);
        ScalePainter.setTextAlign(Paint.Align.RIGHT);
        Unit = "10 m";
        ScalePainter.getTextBounds(Unit,0,3,Container);
        canvas.drawText(Unit, X, Y + HeightArrow.getHeight()/2 + Container.height()/2 , ScalePainter);

        // Drawing surrounding Frame ...
        canvas.drawRoundRect(Frame,Radius,Radius,FramePainter);

        super.onDraw(canvas);
    }

}
