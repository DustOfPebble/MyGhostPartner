package com.dustofcloud.daytodayrace;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class ViewDock extends Activity {
    Application backendServices = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dock_views);
        backendServices=getApplication();
    }

}