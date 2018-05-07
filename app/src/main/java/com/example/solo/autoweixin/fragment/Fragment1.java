package com.example.solo.autoweixin.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.solo.autoweixin.R;
import com.example.solo.autoweixin.accessibility.AutoWeixinService;
import com.example.solo.autoweixin.activity.Main2Activity;
import com.example.solo.autoweixin.activity.Main3Activity;
import com.example.solo.autoweixin.activity.MainActivity;
import com.example.solo.autoweixin.utils.StringUtils;
import com.example.solo.autoweixin.utils.UrlUtils;
import com.example.solo.autoweixin.utils.Utils;
import com.example.solo.autoweixin.utils.WindowManagerUtils;
import com.example.solo.autoweixin.view.SlideShowView;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class Fragment1 extends Fragment {

    public static Fragment1 fragment1;

    private AccessibilityManager accessibilityManager;

    private SwipeRefreshLayout swipeRefreshLayout;
    private AlertDialog alertDialog;
    private SlideShowView slideShowView;
    private TextView tv_btn;
    private AppCompatSpinner appCompatSpinner;
    private EditText editText, editText2, editText3;
    private ToggleButton toggleButton1, toggleButton2, toggleButton3, toggleButton4;

    private boolean isResume = false;
    private boolean isShowWindow = true;

    // 悬浮窗
    private Toast mToast;
    private View windowView;
    private TextView textView1, textView2, textView3;

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
        if (accessibilityManager.isEnabled()) {
            initCreatFloatWindow();
        } else {
            tv_btn.setText("开启辅助服务");
        }
    }

    public void initCreatFloatWindow() {
        creatFloatWindow();
        if (AutoWeixinService.isChangeNameStart) {
            tv_btn.setText("停止备注");
            if (textView1 != null) {
                textView1.setText("停止\n备注");
            }
        } else {
            tv_btn.setText("开始备注");
            if (textView1 != null) {
                textView1.setText("开始\n备注");
            }
        }
        if (AutoWeixinService.selectNum == 0) {
            if (textView2 != null) {
                textView2.setText("群邀请\n勾选");
            }
            if (textView3 != null) {
                textView3.setText("群发\n勾选");
            }
        } else if (AutoWeixinService.selectNum == 40) {
            if (textView2 != null) {
                textView2.setText("群邀请\n关闭");
            }
            if (textView3 != null) {
                textView3.setText("群发\n勾选");
            }
        } else if (AutoWeixinService.selectNum == 200) {
            if (textView2 != null) {
                textView2.setText("群邀请\n勾选");
            }
            if (textView3 != null) {
                textView3.setText("群发\n关闭");
            }
        }
        if (windowView != null && isResume) {
            windowView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isResume = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isResume = false;
        // 防止悬浮窗与dialog共存
        if (alertDialog == null || !alertDialog.isShowing()) {
            if (windowView != null) {
                windowView.setVisibility(View.VISIBLE);
                initCreatFloatWindow();
            }
        }
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
                    try {
                        Intent intent = new Intent(Objects.requireNonNull(getActivity()), MainActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Intent intent = new Intent();
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        intent.setClassName("com.example.solo.autoweixin", "com.example.solo.autoweixin.activity.MainActivity");
                        startActivity(intent);
                    }
                    // 开启悬浮窗
                    creatFloatWindow();
                } else {
                    WindowManagerUtils.removeView();
                    windowView = null;
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
                    if (AutoWeixinService.isChangeNameStart) {
                        showStopDialog();
                    } else {
                        showStartDialog(false);
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
        ImageView iv_help = view.findViewById(R.id.iv_help);
        iv_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(UrlUtils.urlJiaocheng);
                intent.setData(content_url);
                startActivity(intent);
            }
        });
    }

    // 初始化修改名字规则
    private void initChangeName(View view) {
        StringUtils.hasPreName = false;
        StringUtils.preName = "";
        StringUtils.numType = 0;
        StringUtils.index = 0;

        editText = view.findViewById(R.id.editText);
        editText2 = view.findViewById(R.id.editText2);
        editText3 = view.findViewById(R.id.editText3);
        toggleButton1 = view.findViewById(R.id.toggleButton1);
        toggleButton2 = view.findViewById(R.id.toggleButton2);
        toggleButton3 = view.findViewById(R.id.toggleButton3);
        toggleButton4 = view.findViewById(R.id.toggleButton4);
        appCompatSpinner = view.findViewById(R.id.appCompatSpinner);

        appCompatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringUtils.numType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (toggleButton1.isChecked()) {
                    StringUtils.preName = s.toString();
                } else {
                    StringUtils.preName = "";
                }
            }
        });
        editText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (toggleButton3.isChecked()) {
                    if ("".equals(s.toString())) {
                        StringUtils.index = 0;
                    } else {
                        StringUtils.index = Integer.valueOf(s.toString());
                    }
                } else {
                    StringUtils.index = 0;
                }
            }
        });
        editText3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (toggleButton4.isChecked()) {
                    StringUtils.keyName = s.toString();
                } else {
                    StringUtils.keyName = "";
                }
            }
        });
        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                StringUtils.hasPreName = isChecked;
                if (isChecked) {
                    StringUtils.preName = editText.getText().toString();
                } else {
                    StringUtils.preName = "";
                }
            }
        });
        toggleButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if ("".equals(editText2.getText().toString())) {
                        StringUtils.index = 0;
                    } else {
                        StringUtils.index = Integer.valueOf(editText2.getText().toString());
                    }
                } else {
                    StringUtils.index = 0;
                }
            }
        });
        toggleButton4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    StringUtils.keyName = editText3.getText().toString();
                } else {
                    StringUtils.keyName = "";
                }
            }
        });

    }

    // 开启悬浮窗
    @SuppressLint("InflateParams")
    private void creatFloatWindow() {
        if (windowView == null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (!Utils.getAppOps(getActivity())) {
                    // 防止创建多个
                    if (alertDialog != null) {
                        alertDialog.dismiss();
                    }
                    // 创建对话框
                    AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                    builder.setTitle("提示");
                    builder.setMessage("需要悬浮窗权限，是否跳转到设置去开启？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog.dismiss();
                            //没有悬浮窗权限m,去开启悬浮窗权限
                            try {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    builder.setNegativeButton("不再提醒", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            isShowWindow = false;
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog = builder.create();
                    alertDialog.setCancelable(false);
                    if (isShowWindow) {
                        alertDialog.show();
                    }
                    return;
                }
            }
            windowView = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.float_normal_view, null);
            textView1 = windowView.findViewById(R.id.textView);
            textView1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AutoWeixinService.isChangeNameStart) {
                        showStopDialog();
                    } else {
                        if (showStartDialog(true)) {
                            // Toast.makeText(getActivity(), "请先开始备注，再进入微信界面", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            textView2 = windowView.findViewById(R.id.textView2);
            textView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AutoWeixinService.selectNum == 40) {
                        AutoWeixinService.selectNum = 0;
                        textView2.setText("群邀请\n勾选");
                    } else {
                        showStopDialog();
                        AutoWeixinService.selectNum = 40;
                        textView2.setText("群邀请\n关闭");
                        textView3.setText("群发\n勾选");
                        try {
                            Intent intent = new Intent(Objects.requireNonNull(getActivity()), Main3Activity.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            Intent intent = new Intent();
                            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                            intent.setClassName("com.example.solo.autoweixin", "com.example.solo.autoweixin.activity.Main3Activity");
                            startActivity(intent);
                        }
                    }
                }
            });
            textView3 = windowView.findViewById(R.id.textView3);
            textView3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AutoWeixinService.selectNum == 200) {
                        AutoWeixinService.selectNum = 0;
                        textView3.setText("群发\n勾选");
                    } else {
                        showStopDialog();
                        AutoWeixinService.selectNum = 200;
                        textView3.setText("群发\n关闭");
                        textView2.setText("群邀请\n勾选");
                        try {
                            Intent intent = new Intent(Objects.requireNonNull(getActivity()), Main3Activity.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            Intent intent = new Intent();
                            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                            intent.setClassName("com.example.solo.autoweixin", "com.example.solo.autoweixin.activity.Main3Activity");
                            startActivity(intent);
                        }
                    }
                }
            });
            TextView textView4 = windowView.findViewById(R.id.textView4);
            textView4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AutoWeixinService.selectNum = 0;
                    showStopDialog();

                    comeBack();
                }
            });
            WindowManagerUtils.startView(Objects.requireNonNull(getActivity()).getApplication(), windowView);
        }
    }

    // 开始改名确认
    private boolean showStartDialog(boolean isFloatBtn) {
        if (!StringUtils.hasPreName && StringUtils.numType == 0) {
            comeBack();
            ((MainActivity) Objects.requireNonNull(getActivity())).setPage(0);
            // 防止创建多个
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
            // 创建对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("提示");
            builder.setMessage("请至少开启昵称前缀或选择一个编号模式");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    alertDialog.dismiss();
                }
            });
            alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();
            return false;
        }
        AutoWeixinService.selectNum = 0;
        AutoWeixinService.isChangeNameStart = true;
        if (textView1 != null) {
            textView1.setText("停止\n备注");
        }
        if (textView2 != null) {
            textView2.setText("群邀请\n勾选");
        }
        if (textView3 != null) {
            textView3.setText("群发\n勾选");
        }
        tv_btn.setText("停止备注");
        if (isFloatBtn) {
            // 跳转到微信(切换下界面跳转)
            try {
                Intent intent = new Intent(Objects.requireNonNull(getActivity()), Main3Activity.class);
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName("com.example.solo.autoweixin", "com.example.solo.autoweixin.activity.Main3Activity");
                startActivity(intent);
            }
        }
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
                builder.setMessage("开始备注功能已经开启，是否跳转到微信？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                        // 跳转到微信(直接跳转)
                        Intent intent = new Intent();
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        intent.setClassName(AutoWeixinService.MM, AutoWeixinService.LauncherUI);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("手动跳转", new DialogInterface.OnClickListener() {
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
        return true;
    }

    // 结束改名
    public void showStopDialog() {
        if (textView1 != null) {
            textView1.setText("开始\n备注");
        }
        tv_btn.setText("开始备注");
        AutoWeixinService.isChangeNameStart = false;
    }

    // 回到当前应用
    private void comeBack() {
        if (getActivity() != null) {
            try {
                Intent intent = new Intent(Objects.requireNonNull(getActivity()), Main2Activity.class);
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName("com.example.solo.autoweixin", "com.example.solo.autoweixin.activity.Main2Activity");
                startActivity(intent);
            }
            if (!isResume) {
                CustomTimeToast(true);
            }
        }
    }

    public void CustomTimeToast(final boolean flag) {
        if (flag) {
            mToast = Toast.makeText(getActivity(), "正在返回应用中，请稍等...", Toast.LENGTH_LONG);
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!isResume) {
                        mToast.show();
                    }
                }
            }, 0, 3000);// 3000表示点击按钮之后，Toast延迟3000ms后显示
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    mToast.cancel();
                    timer.cancel();
                }
            }, 5000);// 5000表示Toast显示时间为5秒
        } else {
            if (mToast != null) {
                mToast.cancel();
            }
        }
    }

}
