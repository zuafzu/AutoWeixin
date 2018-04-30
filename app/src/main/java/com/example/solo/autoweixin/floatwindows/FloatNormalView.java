package com.example.solo.autoweixin.floatwindows;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.solo.autoweixin.R;
import com.example.solo.autoweixin.accessibility.InspectWechatFriendService;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class FloatNormalView extends LinearLayout {

    public static final String LauncherUI = "com.tencent.mm.ui.LauncherUI";
    public static final String MM = "com.tencent.mm";

    private Context context = null;
    private View view = null;
    private ImageView ivShowControlView = null;
    private TextView textView = null;
    private LinearLayout linear = null;
    private WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    private static WindowManager windowManager;

    private float mTouchStartX;
    private float mTouchStartY;
    private float x;
    private float y;
    private boolean initViewPlace = false;
    private MyWindowManager myWindowManager;

    public FloatNormalView(Context context) {
        super(context);
        this.context = context;
        myWindowManager = MyWindowManager.getInstance();
        LayoutInflater.from(context).inflate(R.layout.float_normal_view, this);
        view = findViewById(R.id.ll_float_normal);
        ivShowControlView = (ImageView) findViewById(R.id.iv_show_control_view);
        textView = findViewById(R.id.textView);
        linear = findViewById(R.id.linear);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initLayoutParams();
        initEvent();
    }


    /**
     * 初始化参数
     */
    private void initLayoutParams() {
        //屏幕宽高
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();

        //总是出现在应用程序窗口之上。
        lp.type = WindowManager.LayoutParams.TYPE_PHONE;

        // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
        // FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按,不设置这个flag的话，home页的划屏会有问题
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

        //悬浮窗默认显示的位置
        lp.gravity = Gravity.START | Gravity.TOP;
        //指定位置
        lp.x = screenWidth - view.getLayoutParams().width * 2;
        lp.y = screenHeight / 2 + view.getLayoutParams().height * 2;
        //悬浮窗的宽高
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.format = PixelFormat.TRANSPARENT;
        windowManager.addView(this, lp);
    }

    /**
     * 设置悬浮窗监听事件
     */
    private void initEvent() {
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textView.getText().toString().equals("开始")) {
                    textView.setText("停止");
                    linear.setVisibility(GONE);
                    InspectWechatFriendService.hasComplete = false;
                    InspectWechatFriendService.canCheck = true;
                    Intent intent = new Intent();
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    intent.setClassName(MM, LauncherUI);
                    getContext().startActivity(intent);
                    Toast.makeText(getContext(), "开始", Toast.LENGTH_SHORT).show();
                } else {
                    textView.setText("正在停止中...");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            myWindowManager.removeNormalView(context);
                            InspectWechatFriendService.canCheck = true;
                            InspectWechatFriendService.hasComplete = true;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                InspectWechatFriendService.getInspectWechatFriendService().disableSelf();
                            } else {
                                InspectWechatFriendService.getInspectWechatFriendService().stopSelf();
                            }
                            Toast.makeText(getContext(), "已经结束了", Toast.LENGTH_SHORT).show();
                        }
                    }, 100);
                }
            }
        });
        linear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                myWindowManager.removeNormalView(context);
                InspectWechatFriendService.canCheck = true;
                InspectWechatFriendService.hasComplete = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    InspectWechatFriendService.getInspectWechatFriendService().disableSelf();
                } else {
                    InspectWechatFriendService.getInspectWechatFriendService().stopSelf();
                }
            }
        });
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!initViewPlace) {
                            initViewPlace = true;
                            //获取初始位置
                            mTouchStartX += (event.getRawX() - lp.x);
                            mTouchStartY += (event.getRawY() - lp.y);
                        } else {
                            //根据上次手指离开的位置与此次点击的位置进行初始位置微调
                            mTouchStartX += (event.getRawX() - x);
                            mTouchStartY += (event.getRawY() - y);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 获取相对屏幕的坐标，以屏幕左上角为原点
                        x = event.getRawX();
                        y = event.getRawY();
                        updateViewPosition();
                        break;

                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 更新浮动窗口位置
     */
    private void updateViewPosition() {
        lp.x = (int) (x - mTouchStartX);
        lp.y = (int) (y - mTouchStartY);
        windowManager.updateViewLayout(this, lp);
    }
}