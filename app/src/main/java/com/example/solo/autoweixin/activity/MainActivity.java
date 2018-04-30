package com.example.solo.autoweixin.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import com.example.solo.autoweixin.R;
import com.example.solo.autoweixin.accessibility.InspectWechatFriendService;
import com.example.solo.autoweixin.floatwindows.MyWindowManager;
import com.example.solo.autoweixin.utils.Utils;

public class MainActivity extends AppCompatActivity {

    MyWindowManager myWindowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", Utils.getVersion(this));
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
                accessibilityManager.addAccessibilityStateChangeListener(new AccessibilityManager.AccessibilityStateChangeListener() {
                    @Override
                    public void onAccessibilityStateChanged(boolean b) {
                        if (b) {
                            myWindowManager = MyWindowManager.getInstance();
                            myWindowManager.createNormalView(getApplicationContext());
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

                if (!accessibilityManager.isEnabled()) {
                    try {
                        //打开系统设置中辅助功能
                        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    myWindowManager = MyWindowManager.getInstance();
                    myWindowManager.createNormalView(getApplicationContext());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 强制结束服务
        InspectWechatFriendService.canCheck = true;
        InspectWechatFriendService.hasComplete = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            InspectWechatFriendService.getInspectWechatFriendService().disableSelf();
        } else {
            InspectWechatFriendService.getInspectWechatFriendService().stopSelf();
        }
    }
}
