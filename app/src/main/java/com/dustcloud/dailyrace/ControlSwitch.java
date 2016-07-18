package com.dustcloud.dailyrace;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class ControlSwitch extends ImageView implements View.OnClickListener {

    private Drawable highIcon = null;
    private Drawable lowIcon = null;
    private Docking Controler = null;

    private short highStatus = -1;
    private short lowStatus = -1;

    private short Status =-1;
    private Vibrator HapticFeedback;

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
         HapticFeedback = (Vibrator)  context.getSystemService(Context.VIBRATOR_SERVICE);
    }

     public void registerModes(short highEvent, short lowEvent)
    {
        highStatus = highEvent;
        lowStatus = lowEvent;
    }

    public void setMode(short modeEvent) {
        if (modeEvent == Status) return;
        Status = modeEvent;
        if (Status == highStatus)  this.setImageDrawable(highIcon);
        if (Status == lowStatus)  this.setImageDrawable(lowIcon);
        invalidate();
    }

    public  void registerManager(Docking controler) { this.Controler = controler;}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void onClick(View V) {
        Controler.onStatusChanged( ((Status == highStatus) ? lowStatus : highStatus) );
        HapticFeedback.vibrate(50);
    }
}
