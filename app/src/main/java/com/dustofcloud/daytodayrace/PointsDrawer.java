package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;

public class PointsDrawer extends ImageView implements EventsDataManager {

    private float MetersToPixels = 0.1f; //(10 cm / pixels ) ==> 100 pixels = 10 metres
    private PointF SizeInUse = new PointF(10f,10f); // In Use area is 10 meters square
    private DataManager BackendService;
    private ArrayList<WayPoint> WaypointsInView=null;
    private ArrayList<WayPoint> WaypointsInUse=null;
    private PointF OffsetMeters =null;
    private Paint Painter;

    public PointsDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);
        BackendService = (DataManager) DataManager.getBackend();
        BackendService.setUpdateViewCallback(this);
        Painter = new Paint();
        Painter.setStrokeWidth(5f);

        WaypointsInView = new ArrayList<>();
        WaypointsInUse = new ArrayList<>();
    }

    @Override
    public void updateOffset(PointF OffsetMeters) {
        if ((this.getWidth() == 0) || (this.getHeight() == 0)) return;
        this.OffsetMeters = OffsetMeters;
        PointF Size = new PointF(
                    this.getWidth() / MetersToPixels,
                    this.getHeight() /MetersToPixels
                );

        WaypointsInView = BackendService.getInView(
                new RectF(
                        this.OffsetMeters.x - Size.x/2,
                        this.OffsetMeters.y - Size.y/2,
                        this.OffsetMeters.x + Size.x/2,
                        this.OffsetMeters.y + Size.y/2
                        )
        );

        WaypointsInUse = BackendService.getInUse(
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

        // Drawing all points from Storage
        Painter.setColor(Color.MAGENTA);
        for (WayPoint Marker :WaypointsInView ) {
            Cartesian = Marker.getCartesian();
            canvas.drawPoint(
                    PixelsFromMeters(Cartesian.x - OffsetMeters.x, canvas.getWidth() /2f),
                    PixelsFromMeters(Cartesian.y - OffsetMeters.y, canvas.getHeight() /2f),
                    Painter);
        }

        // Drawing all points from Storage
        Painter.setColor(Color.GREEN);
        for (WayPoint Marker :WaypointsInUse ) {
            Cartesian = Marker.getCartesian();
            canvas.drawPoint(
                    PixelsFromMeters(Cartesian.x - OffsetMeters.x, canvas.getWidth() /2f),
                    PixelsFromMeters(Cartesian.y - OffsetMeters.y, canvas.getHeight() /2f),
                    Painter);
        }

        if (OffsetMeters !=null) {
            Painter.setColor(Color.RED);
            canvas.drawPoint(
                    PixelsFromMeters(OffsetMeters.x, canvas.getWidth() / 2f),
                    PixelsFromMeters(OffsetMeters.y, canvas.getHeight() / 2f),
                    Painter);
        }
        super.onDraw(canvas);
    }

}