package core.launcher.Widgets;

import android.content.Context;
import android.graphics.BitmapFactory;

import core.Settings.Switches;
import core.launcher.partner.R;

public class SetSpeed extends SetStats {

    public SetSpeed(Context Base) {
        ID = Switches.SpeedStatsID;
        Thumb = BitmapFactory.decodeResource(Base.getResources(), R.drawable.speed_thumb);
        TicksShown = 18;
        TicksLabelCount = 5;
        TicksPhysicValue = 1f;
        PhysicsRangeMin = 0f;
        PhysicsRangeMax = 80f;
        Unit = "km/h";
    }

}
