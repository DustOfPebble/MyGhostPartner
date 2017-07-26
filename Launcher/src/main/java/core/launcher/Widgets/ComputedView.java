package core.launcher.Widgets;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import core.launcher.partner.Organizer;

public class ComputedView extends ImageView implements View.OnTouchListener {
    static private String LogTag = ComputedView.class.getSimpleName();

    private Organizer Listener;
    private Vibrator HapticFeedback;
    public int Placed = Aligns.Undefined;

    RectF Frame;
    Paint FramePainter;
    float FramePixelsFactor;
    float Radius;

    float Padding;

    public ComputedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void register(Organizer Manager) {
        Listener = Manager;
        HapticFeedback = (Vibrator) Listener.getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    void setFrameProperties() {
        FramePixelsFactor = this.getResources().getDisplayMetrics().density;
        float StrokeWidth = FramePixelsFactor  * StyleSheet.FrameBorder;
        FramePainter.setStrokeWidth(StrokeWidth);
        Frame.set(StrokeWidth/2,StrokeWidth/2,this.getWidth()-StrokeWidth/2,this.getHeight()-StrokeWidth/2);
        Radius = StyleSheet.FrameRadius * FramePixelsFactor;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view != this) return false;

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            HapticFeedback.vibrate(100);
            return  true;
        }

        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            Listener.select(this);
            return true;
        }

        return false;
    }
}
