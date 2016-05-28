package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;

public class PointsDrawer extends ImageView implements EventsDataManager {

    private float MetersToPixels = 0.1f; //(10 cm / pixels ) ==> 100 pixels = 10 metres
    private PointF SizeInUse = new PointF(10f,10f); // In Use area is 10 meters square
    private DataManager BackendService;
    private ArrayList<GeoData> GeoInView =null;
    private ArrayList<GeoData> GeoInUse =null;
    private PointF OffsetMeters =null;
    private Paint Painter;

    public PointsDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);
        BackendService = (DataManager) DataManager.getBackend();
        BackendService.setUpdateViewCallback(this);
        Painter = new Paint();
        Painter.setStrokeWidth(30f);

        GeoInView = new ArrayList();
        GeoInUse = new ArrayList();
    }

    @Override
    public void updateOffset(PointF OffsetMeters) {
        if ((this.getWidth() == 0) || (this.getHeight() == 0)) return;
        Log.d("PointsDrawer","Drawing area ["+this.getWidth()/MetersToPixels+
                " m x "+this.getHeight()/MetersToPixels+" m]");
        this.OffsetMeters = OffsetMeters;
        PointF Size = new PointF(
                    this.getWidth() / MetersToPixels,
                    this.getHeight() / MetersToPixels
                );

        GeoInView = BackendService.getInView(
                new RectF(
                        this.OffsetMeters.x - Size.x/2,
                        this.OffsetMeters.y - Size.y/2,
                        this.OffsetMeters.x + Size.x/2,
                        this.OffsetMeters.y + Size.y/2
                        )
        );

        GeoInUse = BackendService.getInUse(
                new RectF(
                        this.OffsetMeters.x - SizeInUse.x / 2,
                        this.OffsetMeters.y - SizeInUse.y / 2,
                        this.OffsetMeters.x + SizeInUse.x / 2,
                        this.OffsetMeters.y + SizeInUse.y / 2
                )
        );
                invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int Width = MeasureSpec.getSize(widthMeasureSpec);
        int Height = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(Width, Height);
    }

    private float PixelsFromMeters(float Meters, float Offset) {
        return (Meters * MetersToPixels) + Offset;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        PointF Cartesian = null;

        Log.d("PointsDrawer", "Drawing "+ GeoInView.size()+ " points in view");
        // Drawing all points from Storage
        Painter.setColor(Color.MAGENTA);
        for (GeoData Marker : GeoInView) {
            Cartesian = Marker.getCartesian();
            canvas.drawPoint(
                    PixelsFromMeters(Cartesian.x - OffsetMeters.x, canvas.getWidth() /2f),
                    PixelsFromMeters(Cartesian.y - OffsetMeters.y, canvas.getHeight() /2f),
                    Painter);
        }

        Log.d("PointsDrawer", "Drawing "+ GeoInUse.size()+ " points in use");
        // Drawing all points from Storage
        Painter.setColor(Color.GREEN);
        for (GeoData Marker : GeoInUse) {
            Cartesian = Marker.getCartesian();
            canvas.drawPoint(
                    PixelsFromMeters(Cartesian.x - OffsetMeters.x, canvas.getWidth() /2f),
                    PixelsFromMeters(Cartesian.y - OffsetMeters.y, canvas.getHeight() /2f),
                    Painter);
        }

         if (OffsetMeters !=null) {
             Log.d("PointsDrawer", "Offset is ["+OffsetMeters.x+","+OffsetMeters.y+"]");
             Painter.setColor(Color.RED);
             canvas.drawPoint(
                    PixelsFromMeters(OffsetMeters.x, canvas.getWidth() / 2f),
                    PixelsFromMeters(OffsetMeters.y, canvas.getHeight() / 2f),
                    Painter);
        }
        super.onDraw(canvas);
    }

}