package com.example.solo.autoweixin.fragment;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.example.solo.autoweixin.utils.UrlUtils;
import com.example.solo.autoweixin.utils.Utils;

import java.util.Objects;

public class Fragment2 extends Fragment {

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
                    // 调用接口

                    // tv_state1.setText("未注册，请激活");
                    // tv_state2.setText("还没有激活？立即加客服要激活码\n微信:knn-1711\nQQ:1653013330");
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
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(UrlUtils.urlQQ)));
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
                Uri content_url = Uri.parse(UrlUtils.urlJiaocheng2);
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

}
