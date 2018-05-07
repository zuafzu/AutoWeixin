package com.example.solo.autoweixin.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import static android.content.Context.WINDOW_SERVICE;

public class WindowManagerUtils {

    private static WindowManager windowManager = null;
    private static WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    @SuppressLint("StaticFieldLeak")
    private static View view;

    @SuppressLint("ClickableViewAccessibility")
    public static void startView(Context context, View mview) {
        // 1、获取系统级别的
        if (windowManager == null) {
            windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        }
        //添加一个UI空间，作为悬浮窗的内容 ，当然Demo是一个ImageView作为悬浮窗内容，实际项目中就需要用复杂View,ViewGroup来扩展功能了
        // 判断UI控件是否存在，存在则移除，确保开启任意次应用都只有一个悬浮窗
        if (view != null) {
            try {
                windowManager.removeView(view);
            } catch (Exception e) {

            }
        }
        // 2、使用Application context 创建UI控件，避免Activity销毁导致上下文出现问题
        view = mview;
        // 3、设置系统级别的悬浮窗的参数，保证悬浮窗悬在手机桌面上
        // 系统级别需要指定type 属性
        // TYPE_SYSTEM_ALERT 允许接收事件
        // TYPE_SYSTEM_OVERLAY 悬浮在系统上
        // 注意清单文件添加权限
        // 系统提示。它总是出现在应用程序窗口之上。
        // lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        lp.type |= WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        lp.type |= WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
        // FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按,不设置这个flag的话，home页的划屏会有问题
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        //悬浮窗默认显示的位置
        lp.gravity = Gravity.RIGHT | Gravity.CENTER;
        //显示位置与指定位置的相对位置差
        lp.x = 0;
        lp.y = 0;
        //悬浮窗的宽高
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //设置悬浮窗背景透明
        lp.format = PixelFormat.TRANSPARENT;
        windowManager.addView(view, lp);
        //设置悬浮窗监听事件
        view.setOnTouchListener(new View.OnTouchListener() {
            private float lastX; //上一次位置的X.Y坐标
            private float lastY;
            private float nowX; //当前移动位置的X.Y坐标
            private float nowY;
            private float tranX; //悬浮窗移动位置的相对值
            private float tranY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean ret = false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 获取按下时的X，Y坐标
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        ret = true;
                        break;
                    case MotionEvent.ACTION_MOVE: // 获取移动时的X，Y坐标
                        nowX = event.getRawX();
                        nowY = event.getRawY(); // 计算XY坐标偏移量
                        tranX = nowX - lastX;
                        tranY = nowY - lastY; // 移动悬浮窗
                        lp.x -= tranX;
                        lp.y += tranY; //更新悬浮窗位置
                        windowManager.updateViewLayout(view, lp); //记录当前坐标作为下一次计算的上一次移动的位置坐标
                        lastX = nowX;
                        lastY = nowY;
                        break;
                    case MotionEvent.ACTION_UP:
                        // 靠边

                        break;
                }
                return ret;
            }
        });
    }

    public static void removeView() {
        if (windowManager != null && view != null) {
            try {
                windowManager.removeView(view);
            } catch (Exception e) {

            }
        }
    }

}
