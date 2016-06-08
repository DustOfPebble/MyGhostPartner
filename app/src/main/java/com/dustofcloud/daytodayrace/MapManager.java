package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;

public class MapManager extends ImageView implements EventsGPS {

    private PointF MetersToPixels = new PointF(1.0f,1.0f); //(1 m/pixels ) ==> will be adjusted in onMeasure
    private DataManager BackendService;
    private ArrayList<GeoData> GeoInView =null;
    private ArrayList<GeoData> GeoInUse =null;
    private PointF WorldOrigin ;
    private PointF GraphicCenter = new PointF(0f,0f) ;
    private Paint LineMode;
    private Paint FillMode;
    private GeoData InUseGeo = null;

    private static final int MarkerColor = 0xffff5555;
    private static final int MarkerTransparency = 100;
    private static final float MarkerLineThickness = 4f;

    private static final int ComputedColor = 0xff55ff99;
    private static final int ComputedFillTransparency = 80;
    private static final int ComputedLineTransparency = 120;
    private static final float ComputedLineThickness = 5f;

    private static final int ExtractedColor = 0xff2ad4ff;
    private static final int ExtractedFillTransparency = 30;
    private static final int ExtractedLineTransparency = 70;
    private static final float ExtractedLineThickness = 5f;



    public MapManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);
        BackendService = (DataManager) DataManager.getBackend();
        BackendService.setUpdateCallback(this);
        LineMode = new Paint();
        LineMode.setStyle(Paint.Style.STROKE);
        FillMode = new Paint();

        GeoInView = new ArrayList<GeoData>();
        GeoInUse = new ArrayList<GeoData>();
    }

    @Override
    public void processLocationChanged(GeoData geoInfo) {
        if ((this.getWidth() == 0) || (this.getHeight() == 0)) return;
        if ((getMeasuredHeight() == 0) || (getMeasuredWidth() == 0)) return;

        InUseGeo = geoInfo;
        this.WorldOrigin = geoInfo.getCoordinate();
        PointF SizeView = BackendService.getViewArea(); // Read From backend because it's subject to change

        int MinSize = Math.min(getMeasuredHeight(),getMeasuredWidth());
        MetersToPixels.set((float)MinSize / SizeView.x,(float)MinSize / SizeView.y);

        PointF Size = new PointF(this.getWidth() / MetersToPixels.x,this.getHeight() / MetersToPixels.y );
        GeoInView = new ArrayList<GeoData>(BackendService.getInView(
                new RectF(this.WorldOrigin.x - Size.x/2,this.WorldOrigin.y - Size.y/2,
                          this.WorldOrigin.x + Size.x/2, this.WorldOrigin.y + Size.y/2
                        ))
                );

        PointF SizeSelection = BackendService.getSelectionArea(); // Read From backend because it's subject to change
        ArrayList<GeoData> CollectedSelection = BackendService.getInUse(
                new RectF(this.WorldOrigin.x - SizeSelection.x / 2, this.WorldOrigin.y - SizeSelection.y / 2,
                          this.WorldOrigin.x + SizeSelection.x / 2, this.WorldOrigin.y + SizeSelection.y / 2
                        )
                );
        // Filtering InUse
        GeoInUse = new ArrayList<GeoData>(CollectedSelection);

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
        PointF Pixel = new PointF(0f,0f); // Allocate because it is updated on the fly
        PointF Coords; // Allocate because it is updated on the fly
        Float MeterToPixelFactor = Math.max(MetersToPixels.x, MetersToPixels.y) ;
        Float Radius;

        if (null == InUseGeo ) {super.onDraw(canvas);return;}

        long StartRender = SystemClock.elapsedRealtime();

        GraphicCenter.set(canvas.getWidth() /2f, canvas.getHeight() /2f);

        canvas.rotate(InUseGeo.getBearing(),GraphicCenter.x,GraphicCenter.y);
        Log.d("MapManager","Rotation is "+InUseGeo.getBearing()+"°");

        // Do the drawing
        Log.d("MapManager", "Drawing "+ GeoInView.size()+ " extracted points");
        // Drawing all points from Storage
        LineMode.setColor(ExtractedColor);
        LineMode.setAlpha(ExtractedLineTransparency);
        LineMode.setStrokeWidth(ExtractedLineThickness);
        FillMode.setColor(ExtractedColor);
        FillMode.setAlpha(ExtractedFillTransparency);
        for (GeoData Marker : GeoInView) {
            Coords = Marker.getCoordinate();
            Radius = MeterToPixelFactor * Marker.getAccuracy();
            Pixel.set(
                    (Coords.x - WorldOrigin.x)* MeterToPixelFactor + GraphicCenter.x ,
                    (WorldOrigin.y - Coords.y)* MeterToPixelFactor + GraphicCenter.y
            );

            canvas.drawCircle(Pixel.x, Pixel.y, Radius,LineMode);
            canvas.drawCircle(Pixel.x, Pixel.y, Radius,FillMode);
        }

        Log.d("MapManager", "Drawing "+ GeoInUse.size()+ " computed points");
        // Drawing all points from Storage
        LineMode.setColor(ComputedColor);
        LineMode.setAlpha(ComputedLineTransparency);
        LineMode.setStrokeWidth(ComputedLineThickness);
        FillMode.setColor(ComputedColor);
        FillMode.setAlpha(ComputedFillTransparency);
        for (GeoData Marker : GeoInUse) {
            Coords = Marker.getCoordinate();
            Radius = MeterToPixelFactor * Marker.getAccuracy();
            Pixel.set(
                    (Coords.x - WorldOrigin.x)* MeterToPixelFactor + GraphicCenter.x ,
                    (WorldOrigin.y - Coords.y)* MeterToPixelFactor + GraphicCenter.y
            );
            canvas.drawCircle(Pixel.x, Pixel.y, Radius,LineMode);
            canvas.drawCircle(Pixel.x, Pixel.y, Radius,FillMode);
        }


         if (WorldOrigin !=null) {
             Log.d("MapManager", "Offset is ["+WorldOrigin.x+","+WorldOrigin.y+"]");
             LineMode.setColor(MarkerColor);
             FillMode.setColor(MarkerColor);
             LineMode.setStrokeWidth(MarkerLineThickness);
             Radius = MeterToPixelFactor * InUseGeo.getAccuracy();
             Float MinRadius = (Radius/10 < 10)? 10:Radius/10;
             Pixel.set(GraphicCenter.x,GraphicCenter.y);
             canvas.drawCircle(Pixel.x, Pixel.y, Radius,LineMode);
             canvas.drawCircle(Pixel.x, Pixel.y,MinRadius ,FillMode);
             FillMode.setAlpha(MarkerTransparency);
             canvas.drawCircle(Pixel.x, Pixel.y, Radius,FillMode);
         }

        long EndRender = SystemClock.elapsedRealtime();
        Log.d("MapManager", "Rendering was "+ (EndRender - StartRender)+ " ms.");

        super.onDraw(canvas);
    }
}