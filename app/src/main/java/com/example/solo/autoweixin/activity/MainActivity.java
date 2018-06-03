package com.example.solo.autoweixin.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.solo.autoweixin.R;
import com.example.solo.autoweixin.base.BaseActivity;
import com.example.solo.autoweixin.bean.BaseBean;
import com.example.solo.autoweixin.bean.CodeBean;
import com.example.solo.autoweixin.fragment.Fragment1;
import com.example.solo.autoweixin.fragment.Fragment2;
import com.example.solo.autoweixin.url.Urls;
import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.util.Objects;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends BaseActivity {

    private AlertDialog alertDialog;
    private ViewPager viewPager;
    private RadioGroup radioGroup;
    private RadioButton radioButton1, radioButton2;
    private Fragment fragment1, fragment2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.viewPager);
        radioGroup = findViewById(R.id.radioGroup);
        radioButton1 = findViewById(R.id.radioButton1);
        radioButton2 = findViewById(R.id.radioButton2);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (radioButton1.getId() == i) {
                    viewPager.setCurrentItem(0);
                } else if (radioButton2.getId() == i) {
                    viewPager.setCurrentItem(1);
                }
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    radioButton1.setChecked(true);
                } else if (position == 1) {
                    radioButton2.setChecked(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        setupViewPager(viewPager);
        showAlert();
    }

    private void showAlert() {
        final SharedPreferences preferences = getSharedPreferences("appInfo", Context.MODE_PRIVATE);
        boolean flag = preferences.getBoolean("isFirst", true);
        if (flag) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("服务条款");
            builder.setMessage("    本软件旨在帮助微商提供便捷高效的辅助工具，提升工作效率。请合理，合法的使用本软件的产品。切勿用于违反法律，道德及影响他人利益的活动。如果因用于非法用途，由此造成的不良后果，由用户自行负责，本软件开发商不承担任何责任及损失。");
            builder.setPositiveButton("同意", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("isFirst", false);
                    editor.apply();
                    netCanProbation();
                    alertDialog.dismiss();
                }
            });
            builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        fragment1 = new Fragment1();
        fragment2 = new Fragment2();
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public Fragment getItem(int position) {
                if (position == 0) {
                    return fragment1;
                } else if (position == 1) {
                    return fragment2;
                }
                return null;
            }
        };
        viewPager.setAdapter(adapter);
    }

    public void setPage(int position) {
        if (position == 0) {
            radioButton1.setChecked(true);
        } else if (position == 1) {
            radioButton2.setChecked(true);
        }
    }

    @Override
    public void onBackPressed() {
        // 防止创建多个
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("是否退出" + getString(R.string.app_name) + "？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
                MainActivity.super.onBackPressed();
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
    }

    private void netCanProbation() {
        OkHttpUtils.post()
                .url(Urls.canProbation)
                .build()
                .execute(new Callback<BaseBean>() {

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        showProgressDialog();
                    }

                    @Override
                    public BaseBean parseNetworkResponse(Response response, int id) throws Exception {
                        String string = response.body().string();
                        return new Gson().fromJson(string, BaseBean.class);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e("cyf", e.getMessage());
                        Toast.makeText(MainActivity.this, "访问异常！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(BaseBean response, int id) {
                        if ("0".equals(response.getCode())) {
                            // 防止创建多个
                            if (alertDialog != null && alertDialog.isShowing()) {
                                alertDialog.dismiss();
                            }
                            // 创建对话框
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("提示");
                            builder.setMessage("是否使用体验会员？\n拒绝后无法再使用体验会员。(群聊40人邀请 和 群发200人邀请 只能体验一次)");
                            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    alertDialog.dismiss();
                                    netUseProbation();
                                }
                            });
                            builder.setNegativeButton("残忍拒绝", new DialogInterface.OnClickListener() {
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

                    @Override
                    public void onAfter(int id) {
                        super.onAfter(id);
                        dissmissProgressDialog();
                    }
                });
    }

    @SuppressLint("HardwareIds")
    private void netUseProbation() {
        OkHttpUtils.post()
                .url(Urls.useProbation)
                .addParams("deviceId", Settings.Secure.getString(Objects.requireNonNull(this).getContentResolver(), Settings.Secure.ANDROID_ID))
                .build()
                .execute(new Callback<BaseBean>() {

                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        showProgressDialog();
                    }

                    @Override
                    public BaseBean parseNetworkResponse(Response response, int id) throws Exception {
                        String string = response.body().string();
                        return new Gson().fromJson(string, BaseBean.class);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e("cyf", e.getMessage());
                        Toast.makeText(MainActivity.this, "访问异常！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(BaseBean response, int id) {
                        if ("0".equals(response.getCode())) {
                            CodeBean codeBean = new Gson().fromJson(response.getData().toString(), CodeBean.class);
                            // 共享参数存会员信息
                            SharedPreferences preferences = getSharedPreferences("appInfo", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("mKey", codeBean.getmKey());
                            editor.putInt("vipType", codeBean.getVipType());
                            editor.putLong("activatedDate", codeBean.getActivatedDate());
                            editor.putLong("endDate", codeBean.getEndDate());
                            editor.putLong("totalTime", codeBean.getTotalTime());
                            editor.putLong("totalNum", codeBean.getTotalNum());
                            editor.apply();
                            ((Fragment2)fragment2).setState();
                        }
                        Toast.makeText(MainActivity.this, response.getMsg(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAfter(int id) {
                        super.onAfter(id);
                        dissmissProgressDialog();
                    }
                });
    }

}
