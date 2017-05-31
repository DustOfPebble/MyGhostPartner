package core.launcher.partner.widgets;

import android.content.Context;
import android.graphics.BitmapFactory;

import core.Settings.Switches;
import core.launcher.partner.R;
import core.launcher.partner.SetStats;

class SetCardio extends SetStats {

    public SetCardio(Context Base) {
        ID = Switches.SensorStatsID;
        Thumb = BitmapFactory.decodeResource(Base.getResources(), R.drawable.heart_thumb);
        TicksShown = 22;
        TicksLabelCount = 10;
        TicksPhysicValue = 1f;
        PhysicsRangeMin = 20f;
        PhysicsRangeMax = 220f;
        Unit = "bpm";
    }

}
