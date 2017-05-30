package core.launcher.Widgets;

import android.content.Context;
import android.graphics.BitmapFactory;

import core.launcher.Buttons.SwitchEnums;
import core.launcher.partner.R;

public class SetCardio extends SetStats {

    public SetCardio(Context Base) {
        ID = SwitchEnums.SensorStatsID;
        Thumb = BitmapFactory.decodeResource(Base.getResources(), R.drawable.heart_thumb);
        NbTicksShown = 22;
        TicksTextGap = 10;
        TicksPhysicValue = 1f;
        PhysicsRangeMin = 20f;
        PhysicsRangeMax = 220f;
        Unit = "bpm";
    }
}
