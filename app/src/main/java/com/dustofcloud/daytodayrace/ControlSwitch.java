package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class ControlSwitch extends ImageView implements View.OnClickListener {

    private Drawable highState =null;
    private Drawable lowState =null;
    private boolean isLocked = false;

    public ControlSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);

        // Loading Attributes from XML definitions ...
        if (attrs == null) return;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ControlSwitch, 0, 0);
        try
        {
            highState = attributes.getDrawable(R.styleable.ControlSwitch_HighMode);
            lowState = attributes.getDrawable(R.styleable.ControlSwitch_LowMode);
        }
        finally { attributes.recycle();}
        this.setOnClickListener(this);
        setMode();
    }

     public void setMode()
    {
        if (isLocked)
        {
            this.setBackground(highState);
        }
        else this.setBackground(lowState);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    public void onClick(View V) {
        isLocked=!isLocked;
        this.setMode();
    }
}
