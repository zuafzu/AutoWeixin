package com.example.solo.autoweixin.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.solo.autoweixin.R;
import com.example.solo.autoweixin.fragment.Fragment1;
import com.example.solo.autoweixin.fragment.Fragment2;

public class MainActivity extends AppCompatActivity {

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
        //禁止ViewPager滑动
//        viewPager.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return true;
//            }
//        });
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

}
