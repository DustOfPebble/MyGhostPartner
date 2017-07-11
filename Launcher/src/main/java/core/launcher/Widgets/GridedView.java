package core.launcher.Widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import core.Structures.Node;
import core.launcher.partner.R;

public class GridedView extends VirtualView {
    private Drawable Thumbnail;
    private float GridCellSize;
    private int NbHrzCells = 10;
    private int NbVrtCells = 10;
    private final int GridColor = 0xffC1C7C9;

    private Paint Grid;


    public GridedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);

        // Loading Attributes from XML definitions ...
        if (attrs == null) return;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GridView, 0, 0);
        try
        {
            //Enabled = attributes.getDimension(styleable.GridView_Thickness);
            Thumbnail = attributes.getDrawable(R.styleable.GridView_Thumbnail);
        }
        finally { attributes.recycle();}

        setOnTouchListener(this);
        setVisibility(INVISIBLE);

        Grid = new Paint();
        Grid.setStrokeWidth(2);
        Grid.setColor(GridColor);
    }

    void update(Node LastNode) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int Width = MeasureSpec.getSize(widthMeasureSpec);
        int Height = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(Width, Height);

        GridCellSize = (float)Width /10;
        NbVrtCells = (int)(Height / GridCellSize);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        float Height = canvas.getHeight();
        float Width = canvas.getWidth();
        if ((Width == 0) || (Height == 0)) { super.onDraw(canvas);return;}

        // Drawing Grid
        for(float i=0; i<NbVrtCells; i++ ) {
            canvas.drawLine(i*GridCellSize, 0, i*GridCellSize, Height,Grid);
        }
        for(int i=0; i<NbHrzCells; i++ ) {
            canvas.drawLine(0,i*GridCellSize, Width, i*GridCellSize,Grid);
        }

        // Drawing surrounding Frame ...
        canvas.drawRoundRect(Frame,Radius,Radius,FramePainter);

        super.onDraw(canvas);
    }

}
