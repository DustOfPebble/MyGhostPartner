package core.launcher.application;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Vibrator;
import android.util.AttributeSet;

//ToDo: Add a animation for searching/transient state
public class SensorSwitch extends ControlSwitch implements ValueAnimator.AnimatorUpdateListener {

    private ValueAnimator BackgroundBlinking;

     public SensorSwitch(Context context, AttributeSet attrs) {
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

         BackgroundBlinking = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
         BackgroundBlinking.setDuration(250); // milliseconds
         BackgroundBlinking.addUpdateListener(this);
    }


    @Override
    public void onAnimationUpdate(ValueAnimator animation) {

    }
}
