package com.example.solo.autoweixin.floatwindows;

import android.content.Context;
import android.view.WindowManager;

public class MyWindowManager {

    private FloatNormalView normalView;

    private static MyWindowManager instance;

    private MyWindowManager() {
    }

    public static MyWindowManager getInstance() {
        if (instance == null)
            instance = new MyWindowManager();
        return instance;
    }


    /**
     * 创建小型悬浮窗
     */
    public void createNormalView(Context context) {
        if (normalView == null)
            normalView = new FloatNormalView(context);
    }

    /**
     * 移除悬浮窗
     *
     * @param context
     */
    public void removeNormalView(Context context) {
        if (normalView != null) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.removeView(normalView);
            normalView = null;
        }
    }

}
