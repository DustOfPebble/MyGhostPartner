package core.launcher.partner;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import core.Settings.Parameters;
import core.launcher.Widgets.ComputedView;
import core.launcher.Widgets.WidgetEnums;

public class Organizer extends RelativeLayout {
    private Activity Owner;
    private ArrayList<ComputedView> Containers = new ArrayList<>();

    public Organizer(Context Caller) {
        super(Caller);
    }

    public Organizer(Context Caller, AttributeSet Settings)  {
        super(Caller, Settings);
    }


    public void register(Docking Owner) {
        this.Owner = Owner;
    }

    public void add(ComputedView Widget) {
        if (Containers.size() == WidgetEnums.Slots) return;
        for (ComputedView Container:Containers) {
            if (Container.Placed == WidgetEnums.LeftBottomWidget ) Container.Placed = WidgetEnums.RightBottomWidget;
            if (Container.Placed == WidgetEnums.CenterTopWidget ) Container.Placed = WidgetEnums.LeftBottomWidget;
        }
        Widget.Placed = WidgetEnums.CenterTopWidget;
        Containers.add(Widget);
        apply();
    }

    public void select(ComputedView Widget) {
        if (Widget.Placed == WidgetEnums.CenterTopWidget) return;
        ComputedView Focus = null;
        for (ComputedView Container:Containers) {
            if (Container.Placed == WidgetEnums.CenterTopWidget ) Focus = Container;
        }
        if (Focus != null) Focus.Placed = Widget.Placed;
        Widget.Placed = WidgetEnums.CenterTopWidget;
        apply();
    }

    private void apply() {
        Point ScreenSize = new Point();
        Owner.getWindowManager().getDefaultDisplay().getSize(ScreenSize);
        int Bounds = Math.min(ScreenSize.x,ScreenSize.y);
        int SecondaryWidgetWidth = (int)(Bounds * 0.45);
        int PrimaryWidgetWidth = (int)(Bounds * 0.65);
        RelativeLayout.LayoutParams ViewSlot;

        for(ComputedView Container:Containers) {
            if (Container.Placed == WidgetEnums.Undefined) continue;
            if (Container.Placed == WidgetEnums.CenterTopWidget) {
                ViewSlot = new RelativeLayout.LayoutParams(PrimaryWidgetWidth, (int) (PrimaryWidgetWidth * Parameters.WidthToHeightFactor));
                ViewSlot.addRule(RelativeLayout.CENTER_HORIZONTAL);
                ViewSlot.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            } else {
                ViewSlot = new RelativeLayout.LayoutParams(SecondaryWidgetWidth, (int) (SecondaryWidgetWidth * Parameters.WidthToHeightFactor));
                if (Container.Placed == WidgetEnums.LeftBottomWidget) {
                    ViewSlot.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    ViewSlot.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                }
                if (Container.Placed == WidgetEnums.RightBottomWidget) {
                    ViewSlot.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    ViewSlot.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                }
            }
            Container.setLayoutParams(ViewSlot);
            removeView(Container);
            addView(Container, ViewSlot);
        }
    invalidate();
    }
}
