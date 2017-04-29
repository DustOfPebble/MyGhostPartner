package core.launcher.application;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;

import services.GPS.EventsGPS;
import core.Structures.Statistics;
import core.Structures.Node;

//ToDo: Use Compas data to get Map direction
//ToDo: add a scrolling feature
public class MapManager extends ImageView implements EventsGPS {

    private PointF MetersToPixels = new PointF(1.0f,1.0f); //(1 m/pixels ) ==> will be adjusted in onMeasure
    private DataManager BackendService =null;
    private ArrayList<Statistics> CollectedDisplayed =null;
    private ArrayList<Statistics> CollectedStatistics =null;
    private Node ViewCenter;
    private Node GraphicCenter = new Node(0f,0f) ;
    private Paint LineMode;
    private Paint FillMode;
    private Statistics LastSnapshot = null;
    private RectF searchZone = new RectF();

    public MapManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);
        LineMode = new Paint();
        LineMode.setStyle(Paint.Style.STROKE);
        FillMode = new Paint();

        CollectedDisplayed = new ArrayList<Statistics>();
        CollectedStatistics = new ArrayList<Statistics>();
    }

    public void setBackend(DataManager backend) {
        BackendService = backend;
        BackendService.setUpdateCallback(this);
    }

    @Override
    public void processLocationChanged(Statistics Snapshot) {
        if ((this.getWidth() == 0) || (this.getHeight() == 0)) return;
        if ((getMeasuredHeight() == 0) || (getMeasuredWidth() == 0)) return;
        if (BackendService == null) return;

        LastSnapshot = Snapshot;
        ViewCenter = Snapshot.copy();
        Node Size;

        // Extracting active point around first because we will make a List copy ...
        Size = BackendService.getExtractStatisticsSize();
        searchZone.set(this.ViewCenter.x - Size.x / 2, this.ViewCenter.y - Size.y / 2,
                       this.ViewCenter.x + Size.x / 2, this.ViewCenter.y + Size.y / 2  );
        CollectedStatistics = BackendService.filter(BackendService.extract(searchZone),Snapshot);

        // Extracting Map background at least to avoid list copy...
        Size = BackendService.getExtractDisplayedSize();
        int MinSize = Math.min(getMeasuredHeight(),getMeasuredWidth());
        MetersToPixels.set((float)MinSize / Size.x,(float)MinSize / Size.y);
        Size = new Node(this.getWidth() / MetersToPixels.x,this.getHeight() / MetersToPixels.y );
        float Extract = Math.max(Size.x, Size.y);
        searchZone.set(this.ViewCenter.x - Extract/2,this.ViewCenter.y - Extract/2,
                       this.ViewCenter.x + Extract/2, this.ViewCenter.y + Extract/2);
        CollectedDisplayed = BackendService.extract(searchZone);

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
        Node Pixel = new Node(0f,0f); // Allocate because it reused directly
        Float MeterToPixelFactor = Math.max(MetersToPixels.x, MetersToPixels.y) ;
        Node Coords;
        Float Radius;

        // Avoid crash during first initialisation
        if (null == LastSnapshot) {super.onDraw(canvas);return;}

        long StartRender = SystemClock.elapsedRealtime();

        GraphicCenter.set(canvas.getWidth() /2f, canvas.getHeight() /2f);

        canvas.rotate(-LastSnapshot.getBearing(),GraphicCenter.x,GraphicCenter.y);
        Log.d("MapManager","Rotation is "+ LastSnapshot.getBearing()+"°");

        // Do the drawing
        Log.d("MapManager", "Drawing "+ CollectedDisplayed.size()+ " extracted points");
        // Drawing all points from Storage
        LineMode.setColor(GraphicsConstants.ExtractedColor);
        LineMode.setAlpha(GraphicsConstants.ExtractedLineTransparency);
        LineMode.setStrokeWidth(GraphicsConstants.ExtractedLineThickness);
        FillMode.setColor(GraphicsConstants.ExtractedColor);
        FillMode.setAlpha(GraphicsConstants.ExtractedFillTransparency);
        for (Statistics Marker : CollectedDisplayed) {
            Coords = Marker.copy();
            Radius = MeterToPixelFactor * Marker.getAccuracy();
            Pixel.set(
                    (Coords.x - ViewCenter.x)* MeterToPixelFactor + GraphicCenter.x ,
                    (ViewCenter.y - Coords.y)* MeterToPixelFactor + GraphicCenter.y
            );

            canvas.drawCircle(Pixel.x, Pixel.y, Radius,LineMode);
            canvas.drawCircle(Pixel.x, Pixel.y, Radius,FillMode);
        }

        Log.d("MapManager", "Drawing "+ CollectedStatistics.size()+ " computed points");
        // Drawing all points from Storage
        LineMode.setColor(GraphicsConstants.FilteredColor);
        LineMode.setAlpha(GraphicsConstants.FilteredLineTransparency);
        LineMode.setStrokeWidth(GraphicsConstants.FilteredLineThickness);
        FillMode.setColor(GraphicsConstants.FilteredColor);
        FillMode.setAlpha(GraphicsConstants.FilteredFillTransparency);
        for (Statistics Marker : CollectedStatistics) {
            Coords = Marker.copy();
            Radius = MeterToPixelFactor * Marker.getAccuracy();
            Pixel.set(
                    (Coords.x - ViewCenter.x)* MeterToPixelFactor + GraphicCenter.x ,
                    (ViewCenter.y - Coords.y)* MeterToPixelFactor + GraphicCenter.y
            );
            canvas.drawCircle(Pixel.x, Pixel.y, Radius,LineMode);
            canvas.drawCircle(Pixel.x, Pixel.y, Radius,FillMode);
        }


        if (ViewCenter !=null) {
            Log.d("MapManager", "Offset is ["+ ViewCenter.x+","+ ViewCenter.y+"]");
            LineMode.setColor(GraphicsConstants.MarkerColor);
            FillMode.setColor(GraphicsConstants.MarkerColor);
            LineMode.setStrokeWidth(GraphicsConstants.MarkerLineThickness);
            Radius = MeterToPixelFactor * LastSnapshot.getAccuracy();
            Float MinRadius = (Radius/10 < 10)? 10:Radius/10;
            Pixel.set(GraphicCenter.x,GraphicCenter.y);
            canvas.drawCircle(Pixel.x, Pixel.y, Radius,LineMode);
            canvas.drawCircle(Pixel.x, Pixel.y,MinRadius ,FillMode);
            FillMode.setAlpha(GraphicsConstants.MarkerTransparency);
            canvas.drawCircle(Pixel.x, Pixel.y, Radius,FillMode);
        }

        long EndRender = SystemClock.elapsedRealtime();
        Log.d("MapManager", "Rendering was "+ (EndRender - StartRender)+ " ms.");

        super.onDraw(canvas);
    }
}