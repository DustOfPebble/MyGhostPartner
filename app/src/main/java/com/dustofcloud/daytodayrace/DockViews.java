package com.dustofcloud.daytodayrace;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class DockViews extends Activity {
    Application backendServices = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dock_views);

        backendServices=getApplication();
    }

}
