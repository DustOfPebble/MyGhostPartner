package core.launcher.Widgets;

import android.content.Context;
import android.graphics.BitmapFactory;

import core.launcher.partner.R;

public class SetSpeed extends SetStats {

    public SetSpeed(Context Base) {
        Thumb = BitmapFactory.decodeResource(Base.getResources(), R.drawable.speed_thumb);
        NbTicksShown = 18;
        TicksTextGap = 5;
        TicksPhysicValue = 1f;
        PhysicsRangeMin = 0f;
        PhysicsRangeMax = 80f;
        Unit = "km/h";
    }
}
