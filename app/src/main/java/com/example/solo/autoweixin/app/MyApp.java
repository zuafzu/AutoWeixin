package com.example.solo.autoweixin.app;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

public class MyApp extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "c693cc566d", false);
    }
}
