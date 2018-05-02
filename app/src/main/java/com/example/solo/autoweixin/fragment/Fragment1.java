package com.example.solo.autoweixin.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

import com.example.solo.autoweixin.R;
import com.example.solo.autoweixin.accessibility.AutoWeixinService;
import com.example.solo.autoweixin.view.SlideShowView;

import java.util.Objects;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class Fragment1 extends Fragment {

    private AccessibilityManager accessibilityManager;

    private AlertDialog alertDialog;
    private SlideShowView slideShowView;
    private TextView tv_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_tab_01, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!accessibilityManager.isEnabled()) {
            tv_btn.setText("开启辅助服务");
        } else {
            if (AutoWeixinService.isStart) {
                tv_btn.setText("停止改名");
            } else {
                tv_btn.setText("开启改名");
            }
        }
    }

    private void initView(View view) {
        accessibilityManager = (AccessibilityManager) Objects.requireNonNull(getActivity()).getSystemService(ACCESSIBILITY_SERVICE);
        accessibilityManager.addAccessibilityStateChangeListener(new AccessibilityManager.AccessibilityStateChangeListener() {
            @Override
            public void onAccessibilityStateChanged(boolean b) {
                if (b) {
                    getActivity().startActivity(getActivity().getIntent());
                }
            }
        });
        slideShowView = view.findViewById(R.id.slideShowView);
        slideShowView.setTimeInterval(5);
        String imgs[] = new String[1];
        imgs[0] = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1525199329122&di=05a755485f0b6471e62152da5fbf0831&imgtype=0&src=http%3A%2F%2Fpic9.nipic.com%2F20100918%2F2100520_111941003754_2.jpg";
        slideShowView.initUI(getActivity(), 2, imgs);
        tv_btn = view.findViewById(R.id.tv_btn);
        tv_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accessibilityManager.isEnabled()) {
                    if (AutoWeixinService.isStart) {
                        tv_btn.setText("开启改名");
                        AutoWeixinService.isStart = false;
                    } else {
                        tv_btn.setText("停止改名");
                        AutoWeixinService.isStart = true;
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

}
