package com.android.lucia.dribbble;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

public class DribbbleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
