package core.launcher.application;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
//ToDo: Add a animation for searching/transient state
public class ControlSwitch extends ImageView implements View.OnTouchListener {

    protected Drawable highIcon = null;
    protected Drawable lowIcon = null;
    private Docking Controler = null;

    private short highStatus = -1;
    private short lowStatus = -1;

    private short Status =-1;
    protected Vibrator HapticFeedback;

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

         this.setOnTouchListener(this);
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

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view != this) return false;

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            HapticFeedback.vibrate(100);
            return  true;
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            Controler.onButtonStatusChanged(((Status == highStatus) ? lowStatus : highStatus));
            return true;
        }

        return false;
    }
}
