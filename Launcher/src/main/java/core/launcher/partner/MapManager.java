package core.launcher.partner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;

import core.GPS.CoreGPS;
import core.Settings.Parameters;
import core.Structures.Coords2D;
import core.Structures.Extension;
import core.Structures.Frame;
import core.Structures.Statistic;
import core.helpers.Processing;
import core.Structures.Node;
import services.Junction;

class MapManager extends ImageView {
    private static String LogTag = MapManager.class.getSimpleName();

    private Extension MetersToPixels = new Extension(1.0f,1.0f); //(1 m/pixels ) ==> will be adjusted in onMeasure
    private Junction BackendService = null;
    private ArrayList<Node> CollectedDisplayed =null;
    private ArrayList<Node> CollectedStatistics =null;
    private Coords2D ViewCenter;
    private Coords2D GraphicCenter = new Coords2D(0f,0f);
    private Coords2D Pixel = new Coords2D(0f,0f);

    private Paint LineMode;
    private Paint FillMode;
    private Statistic NowStats = null;
    private Frame searchZone = null;

    public MapManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);
        LineMode = new Paint();
        LineMode.setStyle(Paint.Style.STROKE);
        FillMode = new Paint();

        CollectedDisplayed = new ArrayList<>();
        CollectedStatistics = new ArrayList<>();
     }

    public void setBackend(Junction Service) {
        BackendService = Service;
    }

    public void setGPS(CoreGPS GPS) {
        if ((this.getWidth() == 0) || (this.getHeight() == 0)) return;
        if ((getMeasuredHeight() == 0) || (getMeasuredWidth() == 0)) return;
        if (BackendService == null) return;

        NowStats = GPS.Statistic(0);
        ViewCenter = GPS.Moved();
        Extension StatsSize = Parameters.StatisticsSelectionSize;

        // Extracting active point around first because we will make a List copy ...
        searchZone = new Frame(ViewCenter, StatsSize);
        CollectedStatistics = Processing.filter(BackendService.getNodes(searchZone),NowStats);

        // Extracting Map background at least to avoid list copy...
        Extension ViewSize = Parameters.DisplayedSelectionSize;
        int MinSize = Math.min(getMeasuredHeight(),getMeasuredWidth());
        MetersToPixels = new Extension((float)MinSize / ViewSize.w,(float)MinSize / ViewSize.h);

        ViewSize = new Extension(this.getWidth() / MetersToPixels.w,this.getHeight() / MetersToPixels.h );
        float Extract = Math.max(ViewSize.w, ViewSize.h);
        searchZone = new Frame(ViewCenter, new Extension(Extract,Extract));
        CollectedDisplayed = BackendService.getNodes(searchZone);

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int Width = MeasureSpec.getSize(widthMeasureSpec);
        int Height = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(Width, Height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Float MeterToPixelFactor = Math.max(MetersToPixels.w, MetersToPixels.h) ;
        Coords2D Coords;
        Float Radius;

        // Avoid crash during first initialisation
        if (null == NowStats) {super.onDraw(canvas);return;}

        long StartRender = SystemClock.elapsedRealtime();

        GraphicCenter.set(canvas.getWidth() /2f, canvas.getHeight() /2f);

        canvas.rotate(-NowStats.Bearing,GraphicCenter.dx,GraphicCenter.dy);
//        Log.d(LogTag,"Rotation is "+ NowStats.Bearing+"°");

        // Do the drawing
//        Log.d(LogTag, "Drawing "+ CollectedDisplayed.size()+ " extracted points");
        // Drawing all points from Storage
        LineMode.setColor(Styles.ExtractedColor);
        LineMode.setAlpha(Styles.ExtractedLineTransparency);
        LineMode.setStrokeWidth(Styles.ExtractedLineThickness);
        FillMode.setColor(Styles.ExtractedColor);
        FillMode.setAlpha(Styles.ExtractedFillTransparency);
        for (Node Marker : CollectedDisplayed) {
            Coords = Marker.Move;
            Radius = MeterToPixelFactor * Marker.Stats.Accuracy;
            Pixel.set(
                    (Coords.dx - ViewCenter.dx)* MeterToPixelFactor + GraphicCenter.dx,
                    (ViewCenter.dy - Coords.dy)* MeterToPixelFactor + GraphicCenter.dy
            );

            canvas.drawCircle(Pixel.dx, Pixel.dy, Radius,LineMode);
            canvas.drawCircle(Pixel.dx, Pixel.dy, Radius,FillMode);
        }

//        Log.d(LogTag, "Drawing "+ CollectedStatistics.size()+ " computed points");
        // Drawing all points from Storage
        LineMode.setColor(Styles.FilteredColor);
        LineMode.setAlpha(Styles.FilteredLineTransparency);
        LineMode.setStrokeWidth(Styles.FilteredLineThickness);
        FillMode.setColor(Styles.FilteredColor);
        FillMode.setAlpha(Styles.FilteredFillTransparency);
        for (Node Marker : CollectedStatistics) {
            Coords = Marker.Move;
            Radius = MeterToPixelFactor * Marker.Stats.Accuracy;
            Pixel.set(
                    (Coords.dx - ViewCenter.dx)* MeterToPixelFactor + GraphicCenter.dx,
                    (ViewCenter.dy - Coords.dy)* MeterToPixelFactor + GraphicCenter.dy
            );
            canvas.drawCircle(Pixel.dx, Pixel.dy, Radius,LineMode);
            canvas.drawCircle(Pixel.dx, Pixel.dy, Radius,FillMode);
        }

        if (ViewCenter !=null) {//           Log.d(LogTag, "Offset is ["+ ViewCenter.dx +","+ ViewCenter.dy +"]");
            LineMode.setColor(Styles.MarkerColor);
            FillMode.setColor(Styles.MarkerColor);
            LineMode.setStrokeWidth(Styles.MarkerLineThickness);
            Radius = MeterToPixelFactor * NowStats.Accuracy;
            Float MinRadius = (Radius/10 < 10)? 10:Radius/10;
            Pixel.set(GraphicCenter.dx,GraphicCenter.dy);
            canvas.drawCircle(Pixel.dx, Pixel.dy, Radius,LineMode);
            canvas.drawCircle(Pixel.dx, Pixel.dy,MinRadius ,FillMode);
            FillMode.setAlpha(Styles.MarkerTransparency);
            canvas.drawCircle(Pixel.dx, Pixel.dy, Radius,FillMode);
        }

        long EndRender = SystemClock.elapsedRealtime();
//        Log.d(LogTag, "Rendering was "+ (EndRender - StartRender)+ " ms.");

        super.onDraw(canvas);
    }
}