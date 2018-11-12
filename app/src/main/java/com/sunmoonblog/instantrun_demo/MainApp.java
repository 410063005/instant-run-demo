package com.sunmoonblog.instantrun_demo;

import android.app.Application;
import android.util.Log;

public class MainApp extends Application {
    private static final String TAG = "InstantRun-MainApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
    }
}
