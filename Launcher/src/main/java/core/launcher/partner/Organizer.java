package core.launcher.partner;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import core.Settings.Parameters;
import core.launcher.Widgets.Infos;
import core.launcher.Widgets.StatsEnums;

public class Organizer extends RelativeLayout {
    private Activity Owner;
    private ArrayList<Infos> Containers;

    public Organizer(Context Caller) {
        super(Caller);
        Containers = new ArrayList<>();
    }

    public void register(Activity Owner) {
        this.Owner = Owner;
    }

    public void add(Infos Widget) {
        if (Containers.size() == StatsEnums.Slots) return;
        for (Infos Container:Containers) {
            if (Container.Placed == StatsEnums.LeftBottomWidget ) Container.Placed = StatsEnums.RightBottomWidget;
            if (Container.Placed == StatsEnums.CenterTopWidget ) Container.Placed = StatsEnums.LeftBottomWidget;
        }
        Widget.Placed = StatsEnums.CenterTopWidget;
        Containers.add(Widget);
        apply();
    }

    public void select(Infos Widget) {
        if (Widget.Placed == StatsEnums.CenterTopWidget) return;
        Infos Focus = null;
        for (Infos Container:Containers) {
            if (Container.Placed == StatsEnums.CenterTopWidget ) Focus = Container;
        }
        if (Focus != null) Focus.Placed = Widget.Placed;
        Widget.Placed = StatsEnums.CenterTopWidget;
        apply();
    }

    private void apply() {
        Point ScreenSize = new Point();
        Owner.getWindowManager().getDefaultDisplay().getSize(ScreenSize);
        int Bounds = Math.min(ScreenSize.x,ScreenSize.y);
        int SecondaryWidgetWidth = (int)(Bounds * 0.45);
        int PrimaryWidgetWidth = (int)(Bounds * 0.65);
        RelativeLayout.LayoutParams MonitorConfig;

        for(Infos Container:Containers) {
            if (Container.Placed == StatsEnums.Undefined) continue;
            if (Container.Placed == StatsEnums.CenterTopWidget) {
                MonitorConfig = new RelativeLayout.LayoutParams(PrimaryWidgetWidth, (int) (PrimaryWidgetWidth * Parameters.WidthToHeightFactor));
                MonitorConfig.addRule(RelativeLayout.CENTER_HORIZONTAL);
                MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            } else {
                MonitorConfig = new RelativeLayout.LayoutParams(SecondaryWidgetWidth, (int) (SecondaryWidgetWidth * Parameters.WidthToHeightFactor));
                if (Container.Placed == StatsEnums.LeftBottomWidget) {
                    MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                }
                if (Container.Placed == StatsEnums.RightBottomWidget) {
                    MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    MonitorConfig.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                }
            }
            Container.setLayoutParams(MonitorConfig);
            removeView(Container);
            addView(Container, MonitorConfig);
        }
    invalidate();
    }
}
