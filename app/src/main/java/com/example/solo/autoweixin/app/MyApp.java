package com.example.solo.autoweixin.app;

import android.app.Application;

import com.tencent.bugly.Bugly;

public class MyApp extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        Bugly.init(getApplicationContext(), "c693cc566d", false);
    }
}
