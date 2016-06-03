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
    private PointF OffsetMeters =null;
    private Paint Painter;
    private PointF Center= new PointF(0f,0f);
    private PointF ZeroXY = new PointF(0f,0f);
    private GeoData InUseGeo = null;
    private MapBuilder MapImage = null;
    private Thread MapBuilding = null;
    private Bitmap MapInUse = null;

    public MapManager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);
        BackendService = (DataManager) DataManager.getBackend();
        BackendService.setUpdateCallback(this);
        Painter = new Paint();
        Painter.setStrokeWidth(2f);

        GeoInView = new ArrayList<GeoData>();
        GeoInUse = new ArrayList<GeoData>();
    }

    @Override
    public void processLocationChanged(GeoData geoInfo) {
        if ((this.getWidth() == 0) || (this.getHeight() == 0)) return;
        Log.d("PointsDrawer","Geographic Area ["+this.getWidth()/MetersToPixels.x+" m x "+this.getHeight()/MetersToPixels.y+" m]");
        Log.d("PointsDrawer","Image size ["+this.getWidth()+" px x "+this.getHeight()+" px]");

        InUseGeo = geoInfo;
        this.OffsetMeters = geoInfo.getCartesian();
        PointF SizeView = BackendService.getViewArea(); // Read From backend because it's subject to change
        if ((getMeasuredHeight() != 0) && (getMeasuredWidth() != 0)) {
        int MinSize = Math.min(getMeasuredHeight(),getMeasuredWidth());
        MetersToPixels.set((float)MinSize / SizeView.x,(float)MinSize / SizeView.y);
        }

        PointF Size = new PointF(this.getWidth() / MetersToPixels.x,this.getHeight() / MetersToPixels.y );
        GeoInView = new ArrayList<GeoData>(BackendService.getInView(
                new RectF(this.OffsetMeters.x - Size.x/2,this.OffsetMeters.y - Size.y/2,
                          this.OffsetMeters.x + Size.x/2, this.OffsetMeters.y + Size.y/2
                        ))
                );

        PointF SizeSelection = BackendService.getSelectionArea(); // Read From backend because it's subject to change
        ArrayList<GeoData> CollectedSelection = BackendService.getInUse(
                new RectF(this.OffsetMeters.x - SizeSelection.x / 2, this.OffsetMeters.y - SizeSelection.y / 2,
                          this.OffsetMeters.x + SizeSelection.x / 2, this.OffsetMeters.y + SizeSelection.y / 2
                        )
                );
        // Filtering InUse
        GeoInUse = new ArrayList<GeoData>(CollectedSelection);

        if (MapImage.getStatus() == MapBuilder.isFinished) {
            MapInUse = Bitmap.createBitmap(MapImage.getMap());
        }

        if (!MapBuilding.isAlive()){
            MapImage.setFilteredPoints(GeoInView);
            MapImage.setComputedPoints(GeoInUse);
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

    private PointF PixelsFromMeters(PointF Meters, PointF Offset) {
        return new PointF((Meters.x * MetersToPixels.x) + Offset.x,
                          (Meters.y * MetersToPixels.y) + Offset.y);
    }

    private PointF MetersFromOrigin(PointF Meters, PointF Origin) {
        return new PointF(Meters.x - Origin.x, Meters.y - Origin.y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        PointF Cartesian = null;
        PointF GraphicPoint = null;

        float scaleRadius = Math.max(MetersToPixels.x, MetersToPixels.y);
        Center.set(canvas.getWidth() /2f, canvas.getHeight() /2f);

        Log.d("PointsDrawer", "Drawing "+ GeoInView.size()+ " points in view");
        // Drawing all points from Storage
        Painter.setColor(Color.MAGENTA);
        for (GeoData Marker : GeoInView) {
            Cartesian = Marker.getCartesian();
            GraphicPoint = PixelsFromMeters(MetersFromOrigin(Cartesian,OffsetMeters),Center);
            canvas.drawCircle(GraphicPoint.x, GraphicPoint.y, scaleRadius * Marker.getAccuracy(),Painter);
        }

        Log.d("PointsDrawer", "Drawing "+ GeoInUse.size()+ " points in use");
        // Drawing all points from Storage
        Painter.setColor(Color.GREEN);
        for (GeoData Marker : GeoInUse) {
            Cartesian = Marker.getCartesian();
            GraphicPoint = PixelsFromMeters(MetersFromOrigin(Cartesian,OffsetMeters),Center);
            canvas.drawPoint(GraphicPoint.x, GraphicPoint.y,Painter);
            canvas.drawCircle(GraphicPoint.x, GraphicPoint.y, scaleRadius * Marker.getAccuracy(),Painter);
        }

         if (OffsetMeters !=null) {
             Log.d("PointsDrawer", "Offset is ["+OffsetMeters.x+","+OffsetMeters.y+"]");
             Painter.setColor(Color.RED);
             GraphicPoint = PixelsFromMeters(ZeroXY,Center);
             canvas.drawPoint(GraphicPoint.x, GraphicPoint.y,Painter);
             canvas.drawCircle(GraphicPoint.x, GraphicPoint.y, scaleRadius * InUseGeo.getAccuracy(),Painter);
         }

        super.onDraw(canvas);
    }

}