package com.example.solo.autoweixin.fragment;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.solo.autoweixin.R;
import com.example.solo.autoweixin.activity.SettingLoginActivity;
import com.example.solo.autoweixin.base.BaseActivity;
import com.example.solo.autoweixin.bean.BaseBean;
import com.example.solo.autoweixin.bean.CodeBean;
import com.example.solo.autoweixin.url.Urls;
import com.example.solo.autoweixin.url.Urls2;
import com.example.solo.autoweixin.utils.Utils;
import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class Fragment2 extends Fragment {

    private AlertDialog alertDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText editText;
    private TextView tv_state1;
    private TextView tv_state2;
    private TextView tv_deviceId;
    private Button btn_alive;
    private LinearLayout ll_version;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_tab_02, container, false);
        initView(view);
        return view;
    }

    @SuppressLint("HardwareIds")
    private void initView(View view) {
        editText = view.findViewById(R.id.editText);
        btn_alive = view.findViewById(R.id.btn_alive);
        btn_alive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = editText.getText().toString();
                if (!"".equals(code)) {
                    // 防止创建多个
                    if (alertDialog != null && alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                    // 创建对话框
                    AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                    builder.setTitle("提示");
                    builder.setMessage("激活会覆盖之前的会员信息，既会员类型和到期时间将重新设定，改名次数会累加，请确定激活。\n激活设备不可卸载app或主动清除app数据，否则激活信息无法找回。");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog.dismiss();
                            netCodeActivated();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog = builder.create();
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                } else {
                    Toast.makeText(getActivity(), "激活码不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        tv_state1 = view.findViewById(R.id.tv_state1);
        tv_state2 = view.findViewById(R.id.tv_state2);
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
        tv_deviceId = view.findViewById(R.id.tv_deviceId);
        tv_deviceId.setText(Settings.Secure.getString(Objects.requireNonNull(getActivity()).getContentResolver(), Settings.Secure.ANDROID_ID));
        Button btn_copy = view.findViewById(R.id.btn_copy);
        btn_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                Objects.requireNonNull(cm).setText(tv_deviceId.getText());
                Toast.makeText(getActivity(), "复制成功", Toast.LENGTH_SHORT).show();
            }
        });
        TextView tv_version = view.findViewById(R.id.tv_version);
        tv_version.setText(Utils.getMyVersion(Objects.requireNonNull(getActivity())));
        LinearLayout ll_fankui = view.findViewById(R.id.ll_fankui);
        ll_fankui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Urls2.urlQQ)));
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "请安装手机QQ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        LinearLayout ll_jiaocheng = view.findViewById(R.id.ll_jiaocheng);
        ll_jiaocheng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(Urls2.urlJiaocheng2);
                intent.setData(content_url);
                startActivity(intent);
            }
        });
        LinearLayout ll_fuzu = view.findViewById(R.id.ll_fuzu);
        ll_fuzu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //打开系统设置中辅助功能
                    Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ll_version = view.findViewById(R.id.ll_version);
        ll_version.setOnClickListener(new View.OnClickListener() {

            //需要监听几次点击事件数组的长度就为几
            //如果要监听双击事件则数组长度为2，如果要监听3次连续点击事件则数组长度为3...
            long[] mHints = new long[3];//初始全部为0

            @Override
            public void onClick(View v) {
                //将mHints数组内的所有元素左移一个位置
                System.arraycopy(mHints, 1, mHints, 0, mHints.length - 1);
                //获得当前系统已经启动的时间
                mHints[mHints.length - 1] = SystemClock.uptimeMillis();
                if (SystemClock.uptimeMillis() - mHints[0] <= 500) {
                    startActivity(new Intent(getActivity(), SettingLoginActivity.class));
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        setState();
    }

    @SuppressLint("SetTextI18n")
    public void setState() {
        // 校验会员信息是否有效
        final SharedPreferences preferences = getActivity().getSharedPreferences("appInfo", Context.MODE_PRIVATE);
        if (preferences.getString("mKey", "").equals("")) {
            if (tv_state1 != null) {
                tv_state1.setText("未注册，请激活");
            }
            if (tv_state2 != null) {
                tv_state2.setText("还没有激活？立即加客服要激活码\n微信:knn-1711\nQQ:1653013330");
            }
        } else {
            if (preferences.getLong("endDate", 0) > System.currentTimeMillis()) {
                if (preferences.getLong("totalNum", 0) != 0) {
                    if (tv_state1 != null) {
                        tv_state1.setText("已激活");
                        String type = "";
                        if (preferences.getInt("vipType", -1) == 0) {
                            type = "体验会员";
                        } else if (preferences.getInt("vipType", -1) == 1) {
                            type = "月度会员";
                        } else if (preferences.getInt("vipType", -1) == 2) {
                            type = "季度会员";
                        } else if (preferences.getInt("vipType", -1) == 3) {
                            type = "半年度会员";
                        } else if (preferences.getInt("vipType", -1) == 4) {
                            type = "年度会员";
                        }
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        Date d1 = new Date(preferences.getLong("endDate", 0));
                        String date = format.format(d1);
                        String num = "" + preferences.getLong("totalNum", 0);
                        if (preferences.getLong("totalNum", 0) > 8999999) {
                            num = "无限";
                        }
                        tv_state2.setText("会员类型：" + type + "\n到期日期：" + date + "\n剩余改名次数：" + num);
                    }
                }
            } else {
                // 会员到期了
                // 防止创建多个
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                // 创建对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                builder.setTitle("提示");
                builder.setMessage("会员已经到期，请重新激活");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("mKey", "");
                        editor.putInt("vipType", -1);
                        editor.putLong("activatedDate", 0);
                        editor.putLong("endDate", 0);
                        editor.putLong("totalTime", 0);
                        editor.putLong("totalNum", 0);
                        editor.apply();
                        if (tv_state1 != null) {
                            tv_state1.setText("未注册，请激活");
                        }
                        if (tv_state2 != null) {
                            tv_state2.setText("还没有激活？立即加客服要激活码\n微信:knn-1711\nQQ:1653013330");
                        }
                        alertDialog.dismiss();
                    }
                });
                alertDialog = builder.create();
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        }
    }

    @SuppressLint("HardwareIds")
    private void netCodeActivated() {
        OkHttpUtils.post()
                .url(Urls.codeActivated)
                .addParams("mKey", editText.getText().toString())
                .addParams("deviceId", Settings.Secure.getString(Objects.requireNonNull(getActivity()).getContentResolver(), Settings.Secure.ANDROID_ID))
                .build()
                .execute(new Callback<BaseBean>() {

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        ((BaseActivity) getActivity()).showProgressDialog();
                    }

                    @Override
                    public BaseBean parseNetworkResponse(Response response, int id) throws Exception {
                        String string = response.body().string();
                        return new Gson().fromJson(string, BaseBean.class);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e("cyf", e.getMessage());
                        Toast.makeText(getActivity(), "访问异常！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(BaseBean response, int id) {
                        if ("0".equals(response.getCode())) {
                            CodeBean codeBean = new Gson().fromJson(response.getData().toString(), CodeBean.class);
                            // 共享参数存会员信息
                            SharedPreferences preferences = getActivity().getSharedPreferences("appInfo", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("mKey", codeBean.getmKey());
                            editor.putInt("vipType", codeBean.getVipType());
                            editor.putLong("activatedDate", codeBean.getActivatedDate());
                            editor.putLong("endDate", codeBean.getEndDate());
                            editor.putLong("totalTime", codeBean.getTotalTime());
                            int a = preferences.getInt("totalNum", 0);
                            editor.putLong("totalNum", codeBean.getTotalNum() + a);
                            editor.apply();
                            setState();
                        }
                        Toast.makeText(getActivity(), response.getMsg(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAfter(int id) {
                        super.onAfter(id);
                        ((BaseActivity) getActivity()).dissmissProgressDialog();
                    }
                });
    }

}
