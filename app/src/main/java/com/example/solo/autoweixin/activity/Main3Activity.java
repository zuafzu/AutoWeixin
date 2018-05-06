package com.example.solo.autoweixin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.solo.autoweixin.R;
import com.example.solo.autoweixin.accessibility.AutoWeixinService;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

// 跳转到微信
public class Main3Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Intent intent = new Intent();
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(AutoWeixinService.MM, AutoWeixinService.LauncherUI);
        startActivity(intent);
        finish();
    }
}
