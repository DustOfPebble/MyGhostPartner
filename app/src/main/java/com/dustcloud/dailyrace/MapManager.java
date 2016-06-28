package com.dustcloud.dailyrace;

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

public class MapManager extends ImageView implements EventsProcessGPS {

    private PointF MetersToPixels = new PointF(1.0f,1.0f); //(1 m/pixels ) ==> will be adjusted in onMeasure
    private DataManager BackendService =null;
    private ArrayList<GeoData> CollectedDisplayed =null;
    private ArrayList<GeoData> CollectedStatistics =null;
    private PointF ViewCenter;
    private PointF GraphicCenter = new PointF(0f,0f) ;
    private Paint LineMode;
    private Paint FillMode;
    private GeoData InUseGeo = null;
    private RectF searchZone = new RectF();



    public MapManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);
        LineMode = new Paint();
        LineMode.setStyle(Paint.Style.STROKE);
        FillMode = new Paint();

        CollectedDisplayed = new ArrayList<GeoData>();
        CollectedStatistics = new ArrayList<GeoData>();
    }

    public void setBackend(DataManager backend) {
        BackendService = backend;
        BackendService.setUpdateCallback(this);
    }

    @Override
    public void processLocationChanged(GeoData geoInfo) {
        if ((this.getWidth() == 0) || (this.getHeight() == 0)) return;
        if ((getMeasuredHeight() == 0) || (getMeasuredWidth() == 0)) return;
        if (BackendService == null) return;

        InUseGeo = geoInfo;
        ViewCenter = geoInfo.getCoordinate();
        PointF Size;

        // Extracting active point around first because we will make a List copy ...
        Size = BackendService.getExtractStatisticsSize();
        searchZone.set(this.ViewCenter.x - Size.x / 2, this.ViewCenter.y - Size.y / 2,
                       this.ViewCenter.x + Size.x / 2, this.ViewCenter.y + Size.y / 2  );
        CollectedStatistics = BackendService.filter(BackendService.extract(searchZone));

        // Extracting Map background at least to avoid list copy...
        Size = BackendService.getExtractDisplayedSize();
        int MinSize = Math.min(getMeasuredHeight(),getMeasuredWidth());
        MetersToPixels.set((float)MinSize / Size.x,(float)MinSize / Size.y);
        Size = new PointF(this.getWidth() / MetersToPixels.x,this.getHeight() / MetersToPixels.y );
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
        PointF Pixel = new PointF(0f,0f); // Allocate because it reused directly
        Float MeterToPixelFactor = Math.max(MetersToPixels.x, MetersToPixels.y) ;
        PointF Coords;
        Float Radius;

        // Avoid crash during first initialisation
        if (null == InUseGeo ) {super.onDraw(canvas);return;}

        long StartRender = SystemClock.elapsedRealtime();

        GraphicCenter.set(canvas.getWidth() /2f, canvas.getHeight() /2f);

        canvas.rotate(-InUseGeo.getBearing(),GraphicCenter.x,GraphicCenter.y);
        Log.d("MapManager","Rotation is "+InUseGeo.getBearing()+"°");

        // Do the drawing
        Log.d("MapManager", "Drawing "+ CollectedDisplayed.size()+ " extracted points");
        // Drawing all points from Storage
        LineMode.setColor(GraphicsConstants.ExtractedColor);
        LineMode.setAlpha(GraphicsConstants.ExtractedLineTransparency);
        LineMode.setStrokeWidth(GraphicsConstants.ExtractedLineThickness);
        FillMode.setColor(GraphicsConstants.ExtractedColor);
        FillMode.setAlpha(GraphicsConstants.ExtractedFillTransparency);
        for (GeoData Marker : CollectedDisplayed) {
            Coords = Marker.getCoordinate();
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
        for (GeoData Marker : CollectedStatistics) {
            Coords = Marker.getCoordinate();
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
            Radius = MeterToPixelFactor * InUseGeo.getAccuracy();
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