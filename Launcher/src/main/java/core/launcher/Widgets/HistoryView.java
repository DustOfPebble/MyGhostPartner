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
import java.util.ArrayList;

import core.Structures.Node;
import core.Structures.Statistic;
import core.launcher.partner.R;

public class HistoryView extends ComputedView {
    private String LogTag = HistoryView.class.getSimpleName();
    private Drawable IconResource;
    private Bitmap ViewIcon;
    private Bitmap WidthArrow;
    private Bitmap HeightArrow;

    private String VerticalUnit;
    private String HorizontalUnit;
    private float VerticalScale;
    private float HorizontalScale;

    private float GridCellSize;
    private float GridHeightOffset;
    private float GridWidthOffset;
    private int NbHrzCells = 10;
    private int NbVrtCells = 10;

    private Paint Grid;
    private float[] HrzGridLines;
    private float[] VrtGridLines;
    private final int GridColor = 0xff528B9E;

    private Paint Label;

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

    public HistoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);

        // Loading Attributes from XML definitions ...
        if (attrs == null) return;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HistoryView, 0, 0);
        try
        {
            IconResource = attributes.getDrawable(R.styleable.HistoryView_sticker);
            VerticalUnit = attributes.getString(R.styleable.HistoryView_vertical_unit);
            HorizontalUnit = attributes.getString(R.styleable.HistoryView_horizontal_unit);
            VerticalScale = attributes.getFloat(R.styleable.HistoryView_vertical_scale, 1f);
            HorizontalScale = attributes.getFloat(R.styleable.HistoryView_horizontal_scale, 1f);
        }
        finally { attributes.recycle();}

        setOnTouchListener(this);
        setVisibility(INVISIBLE);

        Grid = new Paint();
        Grid.setColor(GridColor);
        Grid.setStyle(Paint.Style.STROKE);

        Label = new Paint();
        Label.setStyle(Paint.Style.FILL);

        VrtGridLines = new float[0];
        HrzGridLines = new float[0];

        Curve = new Paint();
        Curve.setColor(CurveColor);
        CurvePoints = new float[0];

        Frame = new RectF();
        FramePainter = new Paint();
        FramePainter.setStyle(Paint.Style.STROKE);
        FramePainter.setColor(StyleSheet.BorderColor);

        ScalePainter = new Paint();
        ScalePainter.setColor(StyleSheet.TextColor);
    }

    @Override
    public void pushNodes(ArrayList<Node> Nodes, Node Live){
        this.update(Live.Stats);
    }

    public void update(Statistic Info) {
        // Updating stored records
        History Appended = new History();
        Appended.Info =  Info;
        Appended.TimeStamp = System.currentTimeMillis();
        History.add(0,Appended);
        long HistoryTimeCurrent = History.get(0).TimeStamp;
        long HistoryTimeStart = History.get(History.size()-1).TimeStamp;
        if ((HistoryTimeCurrent - HistoryTimeStart) > NbHrzCells* HorizontalScale) History.remove(History.size()-1);

        // Updating Graph data
        float HeightScale = GridCellSize / VerticalScale;
        float WidthScale = GridCellSize / HorizontalScale;
        long TimeZero = History.get(0).TimeStamp;
        float HeightReference = History.get(0).Info.Altitude;
        float XStart = this.getWidth();
        float YStart = this.getHeight() / 2;
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
            setFrameProperties();

            int height = bottom-top;
            int width = right-left;

            // Global settings
            Grid.setStrokeWidth(Math.max(4, width/60));
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
            ScaleFontSize = height/15;
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
        Rect Container = new Rect();
        float X,Y;
        float Center, Margin;

        X = GridCellSize + GridWidthOffset;
        Y = Height /2 - WidthArrow.getHeight()/2;
        canvas.drawBitmap(WidthArrow,X,Y,null);
        ScalePainter.setTextAlign(Paint.Align.CENTER);
        Center = X + WidthArrow.getWidth()/2;
        ScalePainter.getTextBounds(HorizontalUnit,0,HorizontalUnit.length(),Container);
        Margin = (float) Container.height() * 0.4f;
        canvas.drawRect(Center-Margin-Container.width()/2,Y-Container.height()-Margin,Center+Container.width()/2+Margin,Y+Margin,Label);
        canvas.drawRect(Center-Margin-Container.width()/2,Y-Container.height()-Margin,Center+Container.width()/2+Margin,Y+Margin,Grid);
        canvas.drawText(HorizontalUnit, Center , Y, ScalePainter);

        X = Width - GridCellSize -HeightArrow.getWidth()/2;
        Y = (NbVrtCells -2)* GridCellSize + GridHeightOffset;
        canvas.drawBitmap(HeightArrow,X,Y, null);
        ScalePainter.setTextAlign(Paint.Align.CENTER);
        ScalePainter.getTextBounds(VerticalUnit,0,VerticalUnit.length(),Container);
        Margin = (float) Container.height() * 0.4f;
        Center = X - (Container.width()/2) + (HeightArrow.getWidth()/2) - (3 * Margin);
        Y = Y + HeightArrow.getHeight()/2 + Container.height()/2;
        canvas.drawRect(Center-Margin-Container.width()/2,Y-Container.height()-Margin,Center+Container.width()/2+Margin,Y+Margin,Label);
        canvas.drawRect(Center-Margin-Container.width()/2,Y-Container.height()-Margin,Center+Container.width()/2+Margin,Y+Margin,Grid);
        canvas.drawText(VerticalUnit, Center , Y , ScalePainter);

        // Drawing surrounding Frame ...
        canvas.drawRoundRect(Frame,Radius,Radius,FramePainter);

        super.onDraw(canvas);
    }

}
