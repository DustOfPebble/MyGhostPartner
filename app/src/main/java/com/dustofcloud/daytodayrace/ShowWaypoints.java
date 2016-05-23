package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;

public class ShowWaypoints extends ImageView implements EventsDataManager {

    private float MetersToPixels = 0.1f; //(10 cm / pixels ) ==> 100 pixels = 10 metres
    private DataManager BackendService = null;
    private ArrayList<WayPoint> WaypointsInView=null;
    private ArrayList<WayPoint> WaypointsInUse=null;
    private PointF OffsetMeters =null;
    private Paint Painter;

    public ShowWaypoints(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);
        BackendService = (DataManager) DataManager.getBackend();
        BackendService.setUpdateViewCallback(this);
        Painter = new Paint();
        Painter.setStrokeWidth(5f);
    }

    @Override
    public void updateInView(ArrayList<WayPoint> ExtractedWayPoints) {
        this.WaypointsInView = ExtractedWayPoints;
        invalidate();
    }

    @Override
    public void updateInUse(ArrayList<WayPoint> ExtractedWayPoints) {
        this.WaypointsInUse = ExtractedWayPoints;
        invalidate();
    }

    @Override
    public void updateOffset(PointF OffsetMeters) {
        this.OffsetMeters = OffsetMeters;
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
        // Drawing all points from Storage
        for (WayPoint Marker :WaypointsInView ) {
            Cartesian = Marker.getCartesian();
            Painter.setColor(Color.MAGENTA);
            canvas.drawPoint(
                    PixelsFromMeters(Cartesian.x - OffsetMeters.x, canvas.getWidth() /2f),
                    PixelsFromMeters(Cartesian.y - OffsetMeters.y, canvas.getHeight() /2f),
                    Painter);
        }

        Painter.setColor(Color.RED);
        canvas.drawPoint(
                PixelsFromMeters(OffsetMeters.x , canvas.getWidth() /2f),
                PixelsFromMeters(OffsetMeters.y, canvas.getHeight() /2f),
                Painter);

        super.onDraw(canvas);
    }

}