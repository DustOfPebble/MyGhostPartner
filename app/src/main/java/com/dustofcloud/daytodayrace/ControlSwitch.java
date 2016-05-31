package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class ControlSwitch extends FrameLayout {

    private ImageView statePicture=null;
    private Drawable highState =null;
    private Drawable lowState =null;

    public ControlSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("ControlSwitch", "Calling constructor...");
        // Inflate the Layout from XML definition
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.button_control_switch, this, true);

        statePicture = new ImageView(context);

        // Loading Attributes from XML definitions ...
        if (attrs == null) return;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ControlSwitch, 0, 0);
        try
        {
            highState = attributes.getDrawable(R.styleable.ControlSwitch_HighMode);
            lowState = attributes.getDrawable(R.styleable.ControlSwitch_LowMode);
        }
        finally { attributes.recycle();}

        setHighMode();
    }

     public void setHighMode()
    {
        statePicture.setBackground(highState);
        invalidate();
        requestLayout();
    }

    public void setLowMode()
    {
        statePicture.setBackground(lowState);
        invalidate();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
