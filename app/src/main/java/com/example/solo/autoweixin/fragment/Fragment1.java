package com.example.solo.autoweixin.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.Button;
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
import com.example.solo.autoweixin.activity.WebActivity;
import com.example.solo.autoweixin.base.BaseActivity;
import com.example.solo.autoweixin.bean.BaseBean;
import com.example.solo.autoweixin.bean.WeixinBean;
import com.example.solo.autoweixin.url.Urls;
import com.example.solo.autoweixin.url.Urls2;
import com.example.solo.autoweixin.utils.Utils;
import com.example.solo.autoweixin.utils.VipUtils;
import com.example.solo.autoweixin.utils.WindowManagerUtils;
import com.example.solo.autoweixin.view.SlideShowView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class Fragment1 extends Fragment {

    public static Fragment1 fragment1;
    public static String lastName = "";//上次修改的最后一个人名称
    public static int lastNumType = 0;//上次修改编号模式
    public static int lastNumTotal = 0;

    private Context context;
    private AccessibilityManager accessibilityManager;

    private SwipeRefreshLayout swipeRefreshLayout;
    private AlertDialog alertDialog;
    private SlideShowView slideShowView;
    private TextView tv_btn;
    private AppCompatSpinner appCompatSpinner;
    private EditText editText, editText2, editText3;
    private ToggleButton toggleButton1, toggleButton2, toggleButton3, toggleButton4, toggleButton5, toggleButton6, toggleButton7;

    private boolean isResume = false;
    private boolean isShowWindowPermission = true;
    private boolean isStartGroup = false;//判断是否开启群聊或者群发

    // 悬浮窗
    private Toast mToast;
    private View windowView;
    private Button textView1, textView2, textView3, textView4;

    private boolean isSettingAlert = false;

    // 防止初始化的时候spinner改值
    boolean isappCompatSpinnerInit = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragment1 = this;
        context = Objects.requireNonNull(getActivity()).getApplicationContext();
        View view = inflater.inflate(R.layout.main_tab_01, container, false);
        initView(view);
        initChangeName(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        context = Objects.requireNonNull(getActivity()).getApplicationContext();
        isResume = true;
        if (accessibilityManager.isEnabled()) {
            initCreatFloatWindow();
        } else {
            tv_btn.setText("开启辅助服务");
        }
    }

    public void initCreatFloatWindow() {
        creatFloatWindow();
        setWindowCanClick(0);
        if (AutoWeixinService.isChangeNameStart) {
            // tv_btn.setText("停止备注");
            tv_btn.setText("停止");
            if (textView1 != null) {
                textView1.setText("停止\n备注");
                setWindowCanClick(1);
            }
        } else {
            if (isStartGroup) {
                // tv_btn.setText("停止备注");
                tv_btn.setText("停止");
            } else {
                // tv_btn.setText("开始备注");
                tv_btn.setText("开始");
                if (textView1 != null) {
                    textView1.setText("开始\n备注");
                }
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
                setWindowCanClick(2);
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
                setWindowCanClick(3);
            }
        }
        if (isResume) {
            setWindowViewGONE();
        }
    }

    public void setWindowViewGONE() {
        if (windowView != null) {
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
        stopService();
        setWindowViewGONE();
        fragment1 = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragment1 = null;
    }

    private void initView(View view) {
        lastName = "";
        lastNumType = 0;
        lastNumTotal = 0;
        accessibilityManager = (AccessibilityManager) context.getSystemService(ACCESSIBILITY_SERVICE);
        accessibilityManager.addAccessibilityStateChangeListener(new AccessibilityManager.AccessibilityStateChangeListener() {
            @Override
            public void onAccessibilityStateChanged(boolean b) {
                if (b) {
//                    try {
//                        Intent intent = new Intent(context, MainActivity.class);
//                        context.startActivity(intent);
//                    } catch (Exception e) {
//                        Intent intent = new Intent();
//                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//                        intent.setClassName("com.example.solo.autoweixin", "com.example.solo.autoweixin.activity.MainActivity");
//                        context.startActivity(intent);
//                    }
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
        swipeRefreshLayout.setEnabled(false);
        slideShowView = view.findViewById(R.id.slideShowView);
        slideShowView.setTimeInterval(5);
        int imgs[] = new int[1];
        imgs[0] = Urls2.urlImg;
        slideShowView.initUI(context, 2, imgs);
        tv_btn = view.findViewById(R.id.tv_btn);
        tv_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (accessibilityManager.isEnabled()) {
                    if (AutoWeixinService.isChangeNameStart) {
//                        setWindowCanClick(0);
                        stopService();
                    } else {
                        if (tv_btn.getText().toString().equals("停止")) {
                            // tv_btn.setText("开始备注");
                            tv_btn.setText("开始");
                            isStartGroup = false;
                        } else {
                            SharedPreferences preferences = Objects.requireNonNull(getActivity()).getSharedPreferences("appInfo", Context.MODE_PRIVATE);
                            if (preferences.getInt("numType", 0) == 0 && !toggleButton1.isChecked()) {
                                if (toggleButton5.isChecked() || toggleButton6.isChecked()) {
                                    // tv_btn.setText("停止备注");
                                    tv_btn.setText("停止");
                                    isStartGroup = true;
                                    try {
                                        Intent intent = new Intent(context, Main3Activity.class);
                                        context.startActivity(intent);
                                    } catch (Exception e) {
                                        Intent intent = new Intent();
                                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                                        intent.setClassName("com.example.solo.autoweixin", "com.example.solo.autoweixin.activity.Main3Activity");
                                        context.startActivity(intent);
                                    }
                                } else {
                                    Toast.makeText(context, "请先开启改备注、群邀请、群发中至少任意一个功能", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                showStartDialog(false);
                            }
                        }
                    }
                } else {
                    netGetWeixin();
//                    try {
//                        //打开系统设置中辅助功能
//                        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
//                        startActivity(intent);
//                    } catch (Exception e) {
//                        Toast.makeText(getActivity(), "错误异常 ： " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        e.printStackTrace();
//                    }
                }
            }

        });
        ImageView iv_help = view.findViewById(R.id.iv_help);
        iv_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WebActivity.class);
                intent.putExtra("title", "使用教程");
                intent.putExtra("url", "file:///android_asset/help.html");
                startActivity(intent);
            }
        });
    }

    private void netGetWeixin() {
        OkHttpUtils.post()
                .url(Urls.getWeixin)
                .build()
                .execute(new Callback<BaseBean>() {

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        ((BaseActivity) Objects.requireNonNull(getActivity())).showProgressDialog();
                    }

                    @Override
                    public BaseBean parseNetworkResponse(Response response, int id) throws Exception {
                        String string = response.body().string();
                        return new Gson().fromJson(string, BaseBean.class);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Toast.makeText(getActivity(), "获取微信信息，无法使用，请稍后重试！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(BaseBean response, int id) {
                        if ("0".equals(response.getCode())) {
                            // 给微信按钮赋值id
                            String wechatVersion = Utils.getVersion(Objects.requireNonNull(getActivity()));
                            ArrayList<WeixinBean> list = new Gson().fromJson(response.getData().toString().replaceAll("com.tencent.mm:id/", ""), new TypeToken<ArrayList<WeixinBean>>() {
                            }.getType());
                            boolean flag = false;
                            for (int i = 0; i < list.size(); i++) {
                                if (wechatVersion.equals(list.get(i).getVersion_id())) {
                                    flag = true;
                                    SharedPreferences preferences = Objects.requireNonNull(getActivity()).getSharedPreferences("appInfo", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("version_id", list.get(i).getVersion_id());
                                    editor.putString("wx_yonghuming", "com.tencent.mm:id/" + list.get(i).getWx_yonghuming());
                                    editor.putString("wx_haoyouliebiao", "com.tencent.mm:id/" + list.get(i).getWx_haoyouliebiao());
                                    editor.putString("wx_gengduo", "com.tencent.mm:id/" + list.get(i).getWx_gengduo());
                                    editor.putString("wx_xiugaibeizhu", "com.tencent.mm:id/" + list.get(i).getWx_xiugaibeizhu());
                                    editor.putString("wx_name1", "com.tencent.mm:id/" + list.get(i).getWx_name1());
                                    editor.putString("wx_name2", "com.tencent.mm:id/" + list.get(i).getWx_name2());
                                    editor.apply();
                                    AutoWeixinService.wx_yonghuming = "com.tencent.mm:id/" + list.get(i).getWx_yonghuming();
                                    AutoWeixinService.wx_haoyouliebiao = "com.tencent.mm:id/" + list.get(i).getWx_haoyouliebiao();
                                    AutoWeixinService.wx_gengduo = "com.tencent.mm:id/" + list.get(i).getWx_gengduo();
                                    AutoWeixinService.wx_xiugaibeizhu = "com.tencent.mm:id/" + list.get(i).getWx_xiugaibeizhu();
                                    AutoWeixinService.wx_name1 = "com.tencent.mm:id/" + list.get(i).getWx_name1();
                                    AutoWeixinService.wx_name2 = "com.tencent.mm:id/" + list.get(i).getWx_name2();
                                }
                            }
                            if (flag) {
                                // 打开系统设置中辅助功能
                                try {
                                    Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    // Toast.makeText(getActivity(), "错误异常 ： " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getActivity(), "不支持当前版本的微信，请联系管理员！", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), response.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAfter(int id) {
                        super.onAfter(id);
                        ((BaseActivity) Objects.requireNonNull(getActivity())).dissmissProgressDialog();
                    }
                });
    }

    // 初始化修改名字规则
    private void initChangeName(View view) {
        final SharedPreferences preferences = Objects.requireNonNull(getActivity()).getSharedPreferences("appInfo", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putBoolean("hasPreName", false);
//        editor.putString("preName", "");
//        editor.putInt("numType", 0);
//        editor.putInt("index", 0);
//        editor.putString("keyName", "");
//        editor.putBoolean("isChange", false);
//        editor.apply();

        editText = view.findViewById(R.id.editText);
        editText2 = view.findViewById(R.id.editText2);
        editText3 = view.findViewById(R.id.editText3);
        toggleButton1 = view.findViewById(R.id.toggleButton1);
        toggleButton2 = view.findViewById(R.id.toggleButton2);
        toggleButton3 = view.findViewById(R.id.toggleButton3);
        toggleButton4 = view.findViewById(R.id.toggleButton4);
        toggleButton5 = view.findViewById(R.id.toggleButton5);
        toggleButton6 = view.findViewById(R.id.toggleButton6);
        toggleButton7 = view.findViewById(R.id.toggleButton7);
        appCompatSpinner = view.findViewById(R.id.appCompatSpinner);
        appCompatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isappCompatSpinnerInit) {
                    isappCompatSpinnerInit = false;
                } else {
                    if (!VipUtils.canVip(Objects.requireNonNull(getActivity()), 2)) {
                        appCompatSpinner.setSelection(0, true);
                    } else {
                        if (position == 0 && !toggleButton1.isChecked()) {
                            if (textView1 != null) {
                                textView1.setVisibility(View.GONE);
                            }
                        } else {
                            if (textView1 != null) {
                                textView1.setVisibility(View.VISIBLE);
                            }
                        }
                        // StringUtils.numType = position;
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("numType", position);
                        editor.apply();
                    }
                }
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
                SharedPreferences.Editor editor = preferences.edit();
                if (toggleButton1.isChecked()) {
                    // StringUtils.preName = s.toString();
                    editor.putString("preName", s.toString());
                } else {
                    // StringUtils.preName = "";
                    editor.putString("preName", "");
                }
                editor.apply();
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
                SharedPreferences.Editor editor = preferences.edit();
                if (toggleButton3.isChecked()) {
                    if ("".equals(s.toString())) {
                        // StringUtils.index = 0;
                        editor.putInt("index", 0);
                    } else {
                        // StringUtils.index = Integer.valueOf(s.toString());
                        editor.putInt("index", Integer.valueOf(s.toString()));
                    }
                } else {
                    // StringUtils.index = 0;
                    editor.putInt("index", 0);
                }
                editor.apply();
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
                SharedPreferences.Editor editor = preferences.edit();
                if (toggleButton4.isChecked()) {
                    // StringUtils.keyName = "【," + s.toString();
                    editor.putString("keyName", "【," + s.toString().replace("，", ","));
                } else {
                    // StringUtils.keyName = "";
                    editor.putString("keyName", "");
                }
                editor.apply();
            }
        });
        toggleButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!VipUtils.canVip(Objects.requireNonNull(getActivity()), 1)) {
                    buttonView.setChecked(false);
                } else {
                    if (preferences.getInt("numType", 0) == 0 && !isChecked) {
                        if (textView1 != null) {
                            textView1.setVisibility(View.GONE);
                        }
                    } else {
                        if (textView1 != null) {
                            textView1.setVisibility(View.VISIBLE);
                        }
                    }
                    // StringUtils.hasPreName = isChecked;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hasPreName", isChecked);
                    if (isChecked) {
                        // StringUtils.preName = editText.getText().toString();
                        editor.putString("preName", editText.getText().toString());
                    } else {
                        // StringUtils.preName = "";
                        editor.putString("preName", "");
                    }
                    editor.apply();
                }
            }
        });
        toggleButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!VipUtils.canVip(Objects.requireNonNull(getActivity()), 3)) {
                    buttonView.setChecked(false);
                } else {
                    SharedPreferences.Editor editor = preferences.edit();
                    if (isChecked) {
                        if ("".equals(editText2.getText().toString())) {
                            // StringUtils.index = 0;
                            editor.putInt("index", 0);
                        } else {
                            // StringUtils.index = Integer.valueOf(editText2.getText().toString());
                            editor.putInt("index", Integer.valueOf(editText2.getText().toString()));
                        }
                    } else {
                        // StringUtils.index = 0;
                        editor.putInt("index", 0);
                    }
                    editor.apply();
                }
            }
        });
        toggleButton4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!VipUtils.canVip(Objects.requireNonNull(getActivity()), 4)) {
                    buttonView.setChecked(false);
                } else {
                    SharedPreferences.Editor editor = preferences.edit();
                    if (isChecked) {
                        // StringUtils.keyName = "【," + editText3.getText().toString();
                        editor.putString("keyName", "【," + editText3.getText().toString().replace("，", ","));
                    } else {
                        // StringUtils.keyName = "";
                        editor.putString("keyName", "");
                    }
                    editor.apply();
                }
            }
        });
        toggleButton7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!VipUtils.canVip(Objects.requireNonNull(getActivity()), 5)) {
                    buttonView.setChecked(false);
                } else {
                    // StringUtils.isChange = isChecked;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("isChange", isChecked);
                    editor.apply();
                }
            }
        });
        toggleButton5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!VipUtils.canVip(Objects.requireNonNull(getActivity()), 6)) {
                    buttonView.setChecked(false);
                } else {
                    if (textView2 != null) {
                        if (isChecked) {
                            textView2.setVisibility(View.VISIBLE);
                        } else {
                            textView2.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
        toggleButton6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!VipUtils.canVip(Objects.requireNonNull(getActivity()), 7)) {
                    buttonView.setChecked(false);
                } else {
                    if (textView3 != null) {
                        if (isChecked) {
                            textView3.setVisibility(View.VISIBLE);
                        } else {
                            textView3.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
    }

    // 显示上次配置的参数
    @SuppressLint("SetTextI18n")
    public void setOldConfig() {
        final SharedPreferences preferences = Objects.requireNonNull(getActivity()).getSharedPreferences("appInfo", Context.MODE_PRIVATE);

        boolean hasPreName = preferences.getBoolean("hasPreName", false);// 是否有前缀名
        String preName = preferences.getString("preName", "");// 前缀名
        int numType = preferences.getInt("numType", 0);// 编号模式
        int index = preferences.getInt("index", 0);// 位置
        String keyName = preferences.getString("keyName", "");// 关键字
        boolean isChange = preferences.getBoolean("isChange", false);// 是否替换备注名

        if (appCompatSpinner != null) {
            appCompatSpinner.setSelection(numType, true);
        }
        if (editText != null) {
            editText.setText(preName);
        }
        if (editText2 != null) {
            if (index != 0) {
                editText2.setText("" + index);
            }
        }
        if (editText3 != null) {
            editText3.setText(keyName.replace("【,", ""));
        }
        if (toggleButton1 != null) {
            toggleButton1.setChecked(hasPreName);
        }
        if (toggleButton3 != null) {
            toggleButton3.setChecked(index != 0);
        }
        if (toggleButton4 != null) {
            toggleButton4.setChecked(!keyName.equals(""));
        }
        if (toggleButton7 != null) {
            toggleButton7.setChecked(isChange);
        }
    }

    // 开启悬浮窗
    @SuppressLint("InflateParams")
    private void creatFloatWindow() {
        if (windowView == null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (!Utils.commonROMPermissionCheck(context)) {
                    if (getActivity() != null) {
                        if (isSettingAlert) {
                            getActivity().finish();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent();
                                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                                    intent.setClassName("com.example.solo.autoweixin", "com.example.solo.autoweixin.activity.MainActivity");
                                    context.startActivity(intent);
                                }
                            }, 500);
                            return;
                        }
                        // 防止创建多个
                        if (alertDialog != null && alertDialog.isShowing()) {
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
                                    isSettingAlert = true;
                                    // Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getActivity().getPackageName()));
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(getActivity(), "错误异常 ： " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        builder.setNegativeButton("不再提醒", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                isShowWindowPermission = false;
                                alertDialog.dismiss();
                            }
                        });
                        alertDialog = builder.create();
                        alertDialog.setCancelable(false);
                        if (isShowWindowPermission) {
                            alertDialog.show();
                        }
                        return;
                    }
                }
            }
            if (getActivity() != null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (inflater != null) {
                    windowView = inflater.inflate(R.layout.float_normal_view, null);
                    textView1 = windowView.findViewById(R.id.textView);
                    textView1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (AutoWeixinService.isChangeNameStart) {
//                        setWindowCanClick(0);
                                stopService();
                            } else {
                                if (showStartDialog(true)) {

                                }
                            }
                        }
                    });
                    SharedPreferences preferences = Objects.requireNonNull(getActivity()).getSharedPreferences("appInfo", Context.MODE_PRIVATE);
                    if (toggleButton1.isChecked() || preferences.getInt("numType", 0) != 0) {
                        textView1.setVisibility(View.VISIBLE);
                    } else {
                        textView1.setVisibility(View.GONE);
                    }
                    textView2 = windowView.findViewById(R.id.textView2);
                    textView2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (AutoWeixinService.selectNum == 40) {
//                        setWindowCanClick(0);
//                        AutoWeixinService.selectNum = 0;
                                stopService();
                                textView2.setText("群邀请\n勾选");
                            } else {
                                if (!VipUtils.canVip(Objects.requireNonNull(getActivity()), 6)) {
                                    textView2.setVisibility(View.GONE);
                                    toggleButton5.setChecked(false);
                                } else {
                                    stopService();
                                    setWindowCanClick(2);
                                    AutoWeixinService.selectNum = 40;
                                    textView2.setText("群邀请\n关闭");
                                    textView3.setText("群发\n勾选");
                                    try {
                                        Intent intent = new Intent(context, Main3Activity.class);
                                        context.startActivity(intent);
                                    } catch (Exception e) {
                                        Intent intent = new Intent();
                                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                                        intent.setClassName("com.example.solo.autoweixin", "com.example.solo.autoweixin.activity.Main3Activity");
                                        context.startActivity(intent);
                                    }
                                }
                            }
                        }
                    });
                    if (toggleButton5.isChecked()) {
                        textView2.setVisibility(View.VISIBLE);
                    } else {
                        textView2.setVisibility(View.GONE);
                    }
                    textView3 = windowView.findViewById(R.id.textView3);
                    textView3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (AutoWeixinService.selectNum == 200) {
//                        setWindowCanClick(0);
//                        AutoWeixinService.selectNum = 0;
                                stopService();
                                textView3.setText("群发\n勾选");
                            } else {
                                if (!VipUtils.canVip(Objects.requireNonNull(getActivity()), 7)) {
                                    textView3.setVisibility(View.GONE);
                                    toggleButton6.setChecked(false);
                                } else {
                                    stopService();
                                    setWindowCanClick(3);
                                    AutoWeixinService.selectNum = 200;
                                    textView3.setText("群发\n关闭");
                                    textView2.setText("群邀请\n勾选");
                                    try {
                                        Intent intent = new Intent(context, Main3Activity.class);
                                        context.startActivity(intent);
                                    } catch (Exception e) {
                                        Intent intent = new Intent();
                                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                                        intent.setClassName("com.example.solo.autoweixin", "com.example.solo.autoweixin.activity.Main3Activity");
                                        context.startActivity(intent);
                                    }
                                }
                            }
                        }
                    });
                    if (toggleButton6.isChecked()) {
                        textView3.setVisibility(View.VISIBLE);
                    } else {
                        textView3.setVisibility(View.GONE);
                    }
                    textView4 = windowView.findViewById(R.id.textView4);
                    textView4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            stopService();
                            comeBack();
                        }
                    });
                    WindowManagerUtils.startView(context, windowView);
                }
            }
        }
    }

    @SuppressLint("ResourceType")
    private void setWindowCanClick(int type) {
        if (windowView != null) {
            textView1.setClickable(false);
            textView2.setClickable(false);
            textView3.setClickable(false);
            textView4.setClickable(false);
            textView1.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            textView2.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            textView3.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            textView4.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            if (type == 0) {
                textView1.setClickable(true);
                textView2.setClickable(true);
                textView3.setClickable(true);
                textView4.setClickable(true);
                textView1.setTextColor(context.getResources().getColorStateList(R.drawable.window_btn_selector));
                textView2.setTextColor(context.getResources().getColorStateList(R.drawable.window_btn_selector));
                textView3.setTextColor(context.getResources().getColorStateList(R.drawable.window_btn_selector));
                textView4.setTextColor(context.getResources().getColorStateList(R.drawable.window_btn_selector));
            } else if (type == 1) {
                textView1.setClickable(true);
                textView1.setTextColor(context.getResources().getColorStateList(R.drawable.window_btn_selector));
            } else if (type == 2) {
                textView2.setClickable(true);
                textView2.setTextColor(context.getResources().getColorStateList(R.drawable.window_btn_selector));
            } else if (type == 3) {
                textView3.setClickable(true);
                textView3.setTextColor(context.getResources().getColorStateList(R.drawable.window_btn_selector));
            }
        }
    }

    // 开始改名确认
    private boolean showStartDialog(boolean isFloatBtn) {
        SharedPreferences preferences = Objects.requireNonNull(getActivity()).getSharedPreferences("appInfo", Context.MODE_PRIVATE);
        if (!preferences.getBoolean("hasPreName", false) && preferences.getInt("numType", 0) == 0) {
            comeBack();
            ((MainActivity) Objects.requireNonNull(getActivity())).setPage(0);
            // 防止创建多个
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            // 创建对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
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
        setWindowCanClick(1);
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
        // tv_btn.setText("停止备注");
        tv_btn.setText("停止");
        if (isFloatBtn) {
            lastNumTotal = 0;
            // 跳转到微信(切换下界面跳转)
            try {
                Intent intent = new Intent(context, Main3Activity.class);
                context.startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName("com.example.solo.autoweixin", "com.example.solo.autoweixin.activity.Main3Activity");
                context.startActivity(intent);
            }
        }
        if (context != null) {
            // 在界面的时候弹出
            if (isResume) {
                if (isShowGoOnChangeNameDialog()) {

                } else {
                    showAutoWeiXin();
                }
            }
        }
        return true;
    }

    // 是否自动跳转对话框
    private void showAutoWeiXin() {
        // 防止创建多个
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        builder.setTitle("提示");
        builder.setMessage("备注功能已经开启，是否跳转到微信？");
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

    // 是否记忆改备注对话框
    private boolean isShowGoOnChangeNameDialog() {
        SharedPreferences preferences = Objects.requireNonNull(getActivity()).getSharedPreferences("appInfo", Context.MODE_PRIVATE);
        if (lastNumType != 0 && lastNumType == preferences.getInt("numType", 0) && !lastName.equals("")) {
            // 在界面的时候弹出
            if (isResume) {
                // 防止创建多个
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                // 创建对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                builder.setTitle("提示");
                builder.setMessage("检测到上次修改到“" + lastName + "”用户，编号模式是" + getResources().getStringArray(R.array.num2)[lastNumType] + ",是否继续编号修改？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                        showAutoWeiXin();
                    }
                });
                builder.setNegativeButton("重新开始", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                        lastNumTotal = 0;
                        showAutoWeiXin();
                    }
                });
                alertDialog = builder.create();
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
            return true;
        } else {
            lastNumTotal = 0;
            return false;
        }
    }

    // 结束所有功能
    public void stopService() {
        setWindowCanClick(0);
        if (textView1 != null) {
            textView1.setText("开始\n备注");
        }
        isShowWindowPermission = false;
        tv_btn.setText("开始");
        AutoWeixinService.isChangeNameStart = false;
        AutoWeixinService.selectNum = 0;
    }

    // 返回到当前应用
    public void comeBack() {
        if (context != null) {
            try {
                Intent intent = new Intent(context, Main2Activity.class);
                context.startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName("com.example.solo.autoweixin", "com.example.solo.autoweixin.activity.Main2Activity");
                context.startActivity(intent);
            }
            if (!isResume) {
                CustomTimeToast(true);
            }
        }
    }

    public void CustomTimeToast(final boolean flag) {
        if (flag) {
            mToast = Toast.makeText(context, "正在返回应用中，请稍等...", Toast.LENGTH_LONG);
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

