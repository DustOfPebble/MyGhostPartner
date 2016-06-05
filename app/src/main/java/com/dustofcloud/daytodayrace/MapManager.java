package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.EnumMap;

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
    private MapBuilder MapImage = null;
    private Thread MapBuilding = null;
    private Bitmap MapInUse = null;

    private static final int ColorMarker = 0xffa02c2c;
    private static final int Transparency = 60;

    public MapManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);
        BackendService = (DataManager) DataManager.getBackend();
        BackendService.setUpdateCallback(this);
        LineMode = new Paint();
        LineMode.setStrokeWidth(5f);
        LineMode.setStyle(Paint.Style.STROKE);
        FillMode = new Paint();

        GeoInView = new ArrayList<GeoData>();
        GeoInUse = new ArrayList<GeoData>();
    }

    @Override
    public void processLocationChanged(GeoData geoInfo) {
        if ((this.getWidth() == 0) || (this.getHeight() == 0)) return;
        Log.d("PointsDrawer","Geographic Area ["+this.getWidth()/MetersToPixels.x+" m x "+this.getHeight()/MetersToPixels.y+" m]");
        Log.d("PointsDrawer","Image size ["+this.getWidth()+" px x "+this.getHeight()+" px]");

        InUseGeo = geoInfo;
        this.WorldOrigin = geoInfo.getCartesian();
        PointF SizeView = BackendService.getViewArea(); // Read From backend because it's subject to change
        if ((getMeasuredHeight() != 0) && (getMeasuredWidth() != 0)) {
        int MinSize = Math.min(getMeasuredHeight(),getMeasuredWidth());
        MetersToPixels.set((float)MinSize / SizeView.x,(float)MinSize / SizeView.y);
        }

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

        if (MapBuilding.getState() == Thread.State.TERMINATED) {
            MapInUse = Bitmap.createBitmap(MapImage.getMap());
            MapBuilding = new Thread(MapImage);
        }
        if (MapBuilding.getState() == Thread.State.NEW){
            MapImage.setFilteredPoints(GeoInView);
            MapImage.setComputedPoints(GeoInUse);
            MapImage.setMeterToPixelFactor(Math.max(MetersToPixels.x, MetersToPixels.y));
            MapImage.setWorldOrigin(WorldOrigin);
            MapBuilding.start();
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int Width = MeasureSpec.getSize(widthMeasureSpec);
        int Height = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(Width, Height);
        MapImage = new MapBuilder(Width, Height);
        MapBuilding = new Thread(MapImage);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        PointF Pixel = new PointF(0f,0f); // Allocate because it is updated on the fly
        Float Radius ;

        GraphicCenter.set(canvas.getWidth() /2f, canvas.getHeight() /2f);

         if (MapInUse !=null) canvas.drawBitmap(MapInUse,0f,0f,null);

         if (WorldOrigin !=null) {
             Log.d("PointsDrawer", "Offset is ["+WorldOrigin.x+","+WorldOrigin.y+"]");
             LineMode.setColor(ColorMarker);
             FillMode.setColor(ColorMarker);
             Radius = Math.max(MetersToPixels.x, MetersToPixels.y)*InUseGeo.getAccuracy();
             Float MinRadius = (Radius/10 < 10)? 10:Radius/10;
             Pixel.set(GraphicCenter.x,GraphicCenter.y);
             canvas.drawCircle(Pixel.x, Pixel.y, Radius,LineMode);
             canvas.drawCircle(Pixel.x, Pixel.y,MinRadius ,FillMode);
             FillMode.setAlpha(Transparency);
             canvas.drawCircle(Pixel.x, Pixel.y, Radius,FillMode);
         }

        super.onDraw(canvas);
    }
}