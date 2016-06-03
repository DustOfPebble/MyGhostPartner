package com.dustofcloud.daytodayrace;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class ControlSwitch extends ImageView implements View.OnClickListener {

    private Drawable highIcon = null;
    private Drawable lowIcon = null;
    private EventsControlSwitch Controler = null;

    private short highStatus = -1;
    private short lowStatus = -1;

    private short Status =-1;

     public ControlSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);

        // Loading Attributes from XML definitions ...
        if (attrs == null) return;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ControlSwitch, 0, 0);
        try
        {
            highIcon = attributes.getDrawable(R.styleable.ControlSwitch_HighMode);
            lowIcon = attributes.getDrawable(R.styleable.ControlSwitch_LowMode);
        }
        finally { attributes.recycle();}
        this.setOnClickListener(this);

         Status = highStatus;
         this.setBackground(highIcon);
         invalidate();
    }

     public void setMode(short highEvent, short lowEvent)
    {
        highStatus = highEvent;
        lowStatus = lowEvent;
    }

    public  void registerControlSwitch(EventsControlSwitch Actuator) {
        this.Controler = Actuator;
        Controler.onStatusChanged(Status);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void onClick(View V) {
        Status = (Status == highStatus) ? lowStatus : highStatus;
        if (Status == highStatus)  this.setBackground(highIcon);
        if (Status == lowStatus)  this.setBackground(lowIcon);
        Controler.onStatusChanged(Status);
        invalidate();
    }
}
