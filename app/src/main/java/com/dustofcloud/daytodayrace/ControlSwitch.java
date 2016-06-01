package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class ControlSwitch extends ImageView implements View.OnTouchListener {

    private Drawable highState =null;
    private Drawable lowState =null;
    private boolean isLocked = false;

    public ControlSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);
        Log.d("ControlSwitch", "Calling constructor...");

        // Loading Attributes from XML definitions ...
        if (attrs == null) return;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ControlSwitch, 0, 0);
        try
        {
            highState = attributes.getDrawable(R.styleable.ControlSwitch_HighMode);
            lowState = attributes.getDrawable(R.styleable.ControlSwitch_LowMode);
        }
        finally { attributes.recycle();}
        this.setOnTouchListener(this);
        setMode();
    }

     public void setMode()
    {
        if (isLocked) this.setBackground(highState);
        else this.setBackground(lowState);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public boolean onTouch(View V, MotionEvent event) {
        Log.d("ControlSwitch", "Touch detected");
        return super.onTouchEvent(event);
    }
}
