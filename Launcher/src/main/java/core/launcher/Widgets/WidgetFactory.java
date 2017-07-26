package core.launcher.Widgets;

public class WidgetFactory {

static final int History=01;
static final int Statistic=02;

public static ComputedView create(int LayoutID, int template, Processor provider) {
    switch (template) {
        case History: {
            return null;
        }

        case Statistic: {
            return null;
        }

        default: {
            return null;
        }

    }
}

}
