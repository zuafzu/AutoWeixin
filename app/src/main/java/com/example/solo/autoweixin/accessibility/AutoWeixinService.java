package com.example.solo.autoweixin.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.example.solo.autoweixin.fragment.Fragment1;
import com.example.solo.autoweixin.utils.PerformClickUtils;
import com.example.solo.autoweixin.utils.StringUtils;
import com.example.solo.autoweixin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class AutoWeixinService extends AccessibilityService {

    public static final String LauncherUI = "com.tencent.mm.ui.LauncherUI";
    public static final String MM = "com.tencent.mm";

    public static String wx_zhujiemian = "com.tencent.mm.ui.LauncherUI";//主界面
    public static String wx_yonghuming = "com.tencent.mm:id/cbx";//用户名id("com.tencent.mm:id/cdm"6.6.5)
    public static String wx_haoyouliebiao = "com.tencent.mm:id/j8";//好友列表id（"com.tencent.mm:id/iq"6.6.5）
    public static String wx_gengduo = "com.tencent.mm:id/hi";//右上角更多按钮id（"com.tencent.mm:id/he"6.6.5）
    public static String wx_xiugaibeizhu = "com.tencent.mm:id/i8";//修改备注按钮id（"com.tencent.mm:id/i3"6.6.5）
    public static String wx_name1 = "com.tencent.mm:id/ap2";//用户名文本id（"com.tencent.mm:id/ap5"6.6.5）
    public static String wx_name2 = "com.tencent.mm:id/ap2";//用户名输入框id（"com.tencent.mm:id/ap4"6.6.5）

    public static AutoWeixinService autoWeixinService;
    public static boolean isStart = false;// 是否开启改名

    private final long time = 1500;

    private String myName = "";// 自己的用户名
    private List<String> nameAfterList = new ArrayList<>();//修改后的总用户名列表
    private List<String> nameBeforeList = new ArrayList<>();//修改前的当前用户名列表
    private String changeName;// 修改者的用户名
    private int index = 0;// 当前修改位置
    private MyTask myTask;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // 辅助服务被打开后 执行此方法
        String wechatVersion = Utils.getVersion(this);
        if ("6.6.5".equals(wechatVersion)) {
            wx_yonghuming = "com.tencent.mm:id/cdm";
            wx_haoyouliebiao = "com.tencent.mm:id/iq";
            wx_gengduo = "com.tencent.mm:id/he";
            wx_xiugaibeizhu = "com.tencent.mm:id/i3";
            wx_name1 = "com.tencent.mm:id/ap5";
            wx_name2 = "com.tencent.mm:id/ap4";
        } else if ("6.6.6".equals(wechatVersion)) {
            wx_yonghuming = "com.tencent.mm:id/cbx";
            wx_haoyouliebiao = "com.tencent.mm:id/j8";
            wx_gengduo = "com.tencent.mm:id/hi";
            wx_xiugaibeizhu = "com.tencent.mm:id/i8";
            wx_name1 = "com.tencent.mm:id/ap2";
            wx_name2 = "com.tencent.mm:id/ap2";
        }
        // 初始化
        autoWeixinService = this;
        myName = "";
        nameAfterList.clear();
        nameBeforeList.clear();
        changeName = "";
        index = 0;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if (isStart) {
            //如果手机当前界面的窗口发送变化
            if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                //获取当前activity的类名:
                String currentWindowActivity = accessibilityEvent.getClassName().toString();
                if (wx_zhujiemian.equals(currentWindowActivity)) {
                    AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
                    if (accessibilityNodeInfo == null) {
                        return;
                    }
                    if ("".equals(myName)) {
                        // 获取自己用户名
                        PerformClickUtils.findTextAndClick(this, "我");
                        if (sleep()) {
                            return;
                        }
                        List<AccessibilityNodeInfo> nil = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(wx_yonghuming);
                        if (!nil.isEmpty()) {
                            AccessibilityNodeInfo ani = nil.get(0);
                            myName = ani.getText().toString();
                        }
                        PerformClickUtils.findTextAndClick(this, "通讯录");
                        PerformClickUtils.findTextAndClick(this, "通讯录");
                        myTask = new MyTask();
                        myTask.execute();
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        // 辅助服务被关闭 执行此方法
        autoWeixinService = null;
    }

    class MyTask extends AsyncTask<List<AccessibilityNodeInfo>, Void, Boolean> {

        @Override
        protected Boolean doInBackground(List<AccessibilityNodeInfo>... lists) {
            AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
            if (accessibilityNodeInfo == null) {
                return true;
            }
            List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(wx_haoyouliebiao);
            if (!nodeInfoList.isEmpty()) {

                if (nameBeforeList.size() == 0) {
                    if (sleep()) {
                        return true;
                    }
                    nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(wx_haoyouliebiao);
                    if (!nodeInfoList.isEmpty()) {
                        AccessibilityNodeInfo abi = nodeInfoList.get(0);
                        for (int i = 0; i < abi.getChildCount(); i++) {
                            if (abi.getChild(i) != null) {
                                AccessibilityNodeInfo ani = abi.getChild(i).getChild(abi.getChild(i).getChildCount() - 1);
                                if (ani != null && ani.getText() != null && ani.getText() != null) {
                                    String name = ani.getText().toString();
                                    if (!name.equals(myName)) {
                                        boolean flag = true;
                                        for (int j = 0; j < nameAfterList.size(); j++) {
                                            if (nameAfterList.get(j).equals(name)) {
                                                flag = false;
                                            }
                                        }
                                        if (flag) {
                                            if (!"微信团队".equals(name) && !"文件传输助手".equals(name)) {
                                                nameBeforeList.add(name);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (nameBeforeList.size() > index) {
                    if (!nameBeforeList.get(index).endsWith("位联系人")) {
                        // 判断是否已经在列表里了
                        boolean hasSame = false;
                        for (int j = 0; j < nameAfterList.size(); j++) {
                            if (nameBeforeList.get(index).equals(nameAfterList.get(j))) {
                                hasSame = true;
                                break;
                            }
                        }
                        // 好友列表没有
                        if (!hasSame) {
                            changeName = nameBeforeList.get(index);
                            // 开始改名
                            PerformClickUtils.findTextAndClick(AutoWeixinService.this, changeName);
                            if (sleep()) {
                                return true;
                            }
                            PerformClickUtils.findViewIdAndClick(AutoWeixinService.this, wx_gengduo);
                            PerformClickUtils.findTextAndClick(AutoWeixinService.this, "更多");
                            if (sleep()) {
                                return true;
                            }
                            PerformClickUtils.findViewIdAndClick(AutoWeixinService.this, wx_xiugaibeizhu);
                            PerformClickUtils.findTextAndClick(AutoWeixinService.this, "设置备注及标签");
                            if (sleep()) {
                                return true;
                            }
                            accessibilityNodeInfo = getRootInActiveWindow();
                            PerformClickUtils.findViewIdAndClick(AutoWeixinService.this, wx_name1);
                            PerformClickUtils.findTextAndClick(AutoWeixinService.this, changeName);
                            if (sleep()) {
                                return true;
                            }
                            // 修改名字
                            String string = StringUtils.getName(changeName, nameAfterList.size());
                            Bundle arguments = new Bundle();
                            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, string);
                            if (!accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(wx_name2).isEmpty()) {
                                accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(wx_name2).get(0).performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                            }
                            // 添加记录
                            nameAfterList.add(string);
                            index++;
                            // 修改完成并返回
                            if (sleep()) {
                                return true;
                            }
                            PerformClickUtils.findTextAndClick(AutoWeixinService.this, "完成");
                            if (sleep()) {
                                return true;
                            }
                            PerformClickUtils.performBack(AutoWeixinService.this);
                            if (sleep()) {
                                return true;
                            }
                        } else {
                            index++;
                        }
                    } else {
                        return false;
                    }
                } else {
                    // 检查有没有没改过的
                    nameBeforeList.clear();
                    nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(wx_haoyouliebiao);
                    AccessibilityNodeInfo abi = nodeInfoList.get(0);
                    for (int i = 0; i < abi.getChildCount(); i++) {
                        if (abi.getChild(i) != null) {
                            AccessibilityNodeInfo ani = abi.getChild(i).getChild(abi.getChild(i).getChildCount() - 1);
                            if (ani != null && ani.getText() != null && ani.getText() != null) {
                                String name = ani.getText().toString();
                                if (!name.equals(myName)) {
                                    boolean flag = true;
                                    for (int j = 0; j < nameAfterList.size(); j++) {
                                        if (nameAfterList.get(j).equals(name)) {
                                            flag = false;
                                        }
                                    }
                                    if (flag) {
                                        if (!"微信团队".equals(name) && !"文件传输助手".equals(name)) {
                                            nameBeforeList.add(name);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    boolean isNext = true;
                    for (int i = 0; i < nameBeforeList.size(); i++) {
                        boolean hasSame = false;
                        for (int j = 0; j < nameAfterList.size(); j++) {
                            if (nameBeforeList.get(i).equals(nameAfterList.get(j))) {
                                Log.e("cyf", "nameBeforeList.get(i) : " + nameBeforeList.get(i));
                                Log.e("cyf", "nameAfterList.get(j) : " + nameAfterList.get(j));
                                hasSame = true;
                                break;
                            }
                        }
                        if (hasSame) {
                            isNext = false;
                            break;
                        }
                    }
                    index = 0;
                    if (isNext) {
                        nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(wx_haoyouliebiao);
                        if (!nodeInfoList.isEmpty()) {
                            return nodeInfoList.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                        }
                    }
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            if (isStart) {
                if (s) {
                    myTask = new MyTask();
                    myTask.execute();
                } else {
                    // 被动停止
                    isStart = false;
                    myName = "";
                    nameAfterList.clear();
                    nameBeforeList.clear();
                    changeName = "";
                    index = 0;
                    Toast.makeText(AutoWeixinService.this, "改名已经全部完成了！", Toast.LENGTH_SHORT).show();
                    if (Fragment1.fragment1 != null) {
                        Fragment1.fragment1.showStopDialog();
                    }
                }
            } else {
                // 主动停止
                myName = "";
                nameAfterList.clear();
                nameBeforeList.clear();
                changeName = "";
                index = 0;
                Toast.makeText(AutoWeixinService.this, "改名已经被停止了！", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            isStart = false;
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            isStart = false;
        }
    }

    private boolean sleep() {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return !isStart;
    }

}
