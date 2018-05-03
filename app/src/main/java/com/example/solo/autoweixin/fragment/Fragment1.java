package com.example.solo.autoweixin.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.solo.autoweixin.R;
import com.example.solo.autoweixin.accessibility.AutoWeixinService;
import com.example.solo.autoweixin.activity.MainActivity;
import com.example.solo.autoweixin.utils.UrlUtils;
import com.example.solo.autoweixin.utils.WindowManagerUtils;
import com.example.solo.autoweixin.view.SlideShowView;

import java.util.Objects;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class Fragment1 extends Fragment {

    public static Fragment1 fragment1;

    private AccessibilityManager accessibilityManager;

    private SwipeRefreshLayout swipeRefreshLayout;
    private AlertDialog alertDialog;
    private SlideShowView slideShowView;
    private TextView tv_btn;

    private boolean isResume = false;

    // 悬浮窗
    private TextView floatTextView;
    private LinearLayout linear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragment1 = this;
        View view = inflater.inflate(R.layout.main_tab_01, container, false);
        initView(view);
        initChangeName(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        isResume = true;
        if (!accessibilityManager.isEnabled()) {
            tv_btn.setText("开启辅助服务");
        } else {
            creatFloatWindow();
            if (AutoWeixinService.isStart) {
                tv_btn.setText("停止改名");
            } else {
                tv_btn.setText("开启改名");
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isResume = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fragment1 = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragment1 = null;
    }

    private void initView(View view) {
        accessibilityManager = (AccessibilityManager) Objects.requireNonNull(getActivity()).getSystemService(ACCESSIBILITY_SERVICE);
        accessibilityManager.addAccessibilityStateChangeListener(new AccessibilityManager.AccessibilityStateChangeListener() {
            @Override
            public void onAccessibilityStateChanged(boolean b) {
                if (b) {
                    getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
                    // 开启悬浮窗
                    creatFloatWindow();
                } else {
                    WindowManagerUtils.removeView();
                    tv_btn.setText("开启辅助服务");
                }
            }
        });
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 模拟调用接口
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
            }
        });
        slideShowView = view.findViewById(R.id.slideShowView);
        slideShowView.setTimeInterval(5);
        String imgs[] = new String[1];
        imgs[0] = UrlUtils.urlImg;
        slideShowView.initUI(getActivity(), 2, imgs);
        tv_btn = view.findViewById(R.id.tv_btn);
        tv_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accessibilityManager.isEnabled()) {
                    if (AutoWeixinService.isStart) {
                        showStopDialog();
                    } else {
                        showStartDialog();
                    }
                } else {
                    try {
                        //打开系统设置中辅助功能
                        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        });
    }

    // 初始化修改名字规则
    private void initChangeName(View view) {

    }

    // 开启悬浮窗
    private void creatFloatWindow() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.float_normal_view, null);
        floatTextView = view.findViewById(R.id.textView);
        linear = view.findViewById(R.id.linear);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            linear.setVisibility(View.VISIBLE);
        }
        floatTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AutoWeixinService.isStart) {
                    showStopDialog();
                } else {
                    Toast.makeText(getActivity(), "请先开启改名，再进入微信界面", Toast.LENGTH_SHORT).show();
                    showStartDialog();
                }
            }
        });
        linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AutoWeixinService.autoWeixinService != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        AutoWeixinService.autoWeixinService.disableSelf();
                    }
                }
                WindowManagerUtils.removeView();
                tv_btn.setText("开启辅助服务");
            }
        });
        WindowManagerUtils.startView(getActivity().getApplication(), view);
    }

    // 开始改名确认
    private void showStartDialog() {
        if (floatTextView != null) {
            floatTextView.setText("停止\n改名");
        }
        if (linear != null) {
            linear.setVisibility(View.GONE);
        }
        tv_btn.setText("停止改名");
        AutoWeixinService.isStart = true;
        if (getActivity() != null) {
            // 在界面的时候弹出
            if (isResume) {
                // 防止创建多个
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                // 创建对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("提示");
                builder.setMessage("改名功能已经开启，是否跳转到微信？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                        Intent intent = new Intent();
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        intent.setClassName(AutoWeixinService.MM, AutoWeixinService.LauncherUI);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("自己跳转", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog = builder.create();
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        }
    }

    // 结束改名
    public void showStopDialog() {
        if (floatTextView != null) {
            floatTextView.setText("开启\n改名");
        }
        if (linear != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                linear.setVisibility(View.VISIBLE);
            }
        }
        tv_btn.setText("开启改名");
        AutoWeixinService.isStart = false;
    }
}
