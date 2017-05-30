package core.launcher.Buttons;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import core.launcher.partner.Docking;
import core.launcher.partner.R;

public class Switch extends ImageView implements View.OnTouchListener, Runnable {

    protected Vibrator HapticFeedback;

    protected Drawable Enabled = null;
    protected Drawable Disabled = null;

    private Docking Controler = null;

    private Handler SyncUI = new Handler(Looper.getMainLooper());

    public short Status = -1;

     public Switch(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setAdjustViewBounds(true);

        // Loading Attributes from XML definitions ...
        if (attrs == null) return;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Switch, 0, 0);
        try
        {
            Enabled = attributes.getDrawable(R.styleable.Switch_Enabled);
            Disabled = attributes.getDrawable(R.styleable.Switch_Disabled);
        }
        finally { attributes.recycle();}

         this.setOnTouchListener(this);
         HapticFeedback = (Vibrator)  context.getSystemService(Context.VIBRATOR_SERVICE);
    }


    public void setMode(short Mode) {
        if (Mode == Status) return;
        Status = Mode;
        SyncUI.post(this);
    }

    public  void registerListener(Docking controler) { this.Controler = controler;}

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
            Controler.onClicked(this);
            return true;
        }

        return false;
    }


    @Override
    public void run() {
        if (Status == SwitchEnums.Enabled)  this.setImageDrawable(Enabled);
        if (Status == SwitchEnums.Disabled)  this.setImageDrawable(Disabled);
        invalidate();
    }
}
