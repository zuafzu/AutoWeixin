package com.example.solo.autoweixin.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.example.solo.autoweixin.utils.PerformClickUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linhao on 16/12/30.
 */

public class InspectWechatFriendService extends AccessibilityService {

    private String myName = "";// 当前用户名
    private String changeName;// 修改者的用户名

    private List<String> nameAfterList = new ArrayList<>();//修改后的用户名列表

    private int afterNum = 0;// 已修改页码的名字个数
    private boolean isStar = false;//是否开始
    public static boolean hasComplete = false;//是否完成了

    public static long time = 2000;
    public static String listView2 = "com.tencent.mm:id/iq";// 好友列表listView

    @Override
    protected void onServiceConnected() {//辅助服务被打开后 执行此方法
        super.onServiceConnected();
        nameAfterList.clear();
        afterNum = 0;
        isStar = true;
        hasComplete = false;
        Toast.makeText(this, "开始", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {//监听手机当前窗口状态改变 比如 Activity 跳转,内容变化,按钮点击等事件

        //如果手机当前界面的窗口发送变化
        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            //获取当前activity的类名:
            String currentWindowActivity = accessibilityEvent.getClassName().toString();
            if (!hasComplete) {
                if ("com.tencent.mm.ui.LauncherUI".equals(currentWindowActivity)) {
                    AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
                    if (accessibilityNodeInfo == null) {
                        return;
                    }
                    if ("".equals(myName)) {
                        // 获取自己用户名
                        PerformClickUtils.findTextAndClick(this, "我");
                        try {
                            Thread.sleep(time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        List<AccessibilityNodeInfo> nil = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cdm");
                        if (!nil.isEmpty()) {
                            AccessibilityNodeInfo ani = nil.get(0);
                            myName = ani.getText().toString();
                            myName = "初岩";
                        }
                        PerformClickUtils.findTextAndClick(this, "通讯录");
                        PerformClickUtils.findTextAndClick(this, "通讯录");
                        try {
                            Thread.sleep(time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (isStar) {
                        isStar = false;
                        boolean hasMyName = false;
                        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(listView2);
                        if (!nodeInfoList.isEmpty()) {
                            AccessibilityNodeInfo abi = nodeInfoList.get(0);
                            for (int i = 0; i < abi.getChildCount(); i++) {
                                if (i != 0) {
                                    if (abi.getChild(i) != null) {
                                        AccessibilityNodeInfo accessibilityNodeInfo1 = abi.getChild(i).getChild(abi.getChild(i).getChildCount() - 1);
                                        if (accessibilityNodeInfo1 != null && accessibilityNodeInfo1.getText() != null) {
                                            changeName = accessibilityNodeInfo1.getText().toString();
                                            if (changeName.equals(myName)) {
                                                hasMyName = true;
                                            }
                                            changName2();
                                        }
                                    }
                                }
                            }
                            if (hasMyName) {
                                if (abi.getChildCount() - 2 == (nameAfterList.size() - afterNum)) {
                                    changName(accessibilityNodeInfo);
                                }
                            } else {
                                if (abi.getChildCount() - 1 == (nameAfterList.size() - afterNum)) {
                                    changName(accessibilityNodeInfo);
                                }
                            }
                        }
                    }
                } else if ("com.tencent.mm.ui.contact.ContactRemarkInfoModUI".equals(currentWindowActivity)) {
                    AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
                    if (accessibilityNodeInfo == null) {
                        return;
                    }
                    changName3(accessibilityNodeInfo);
                }
            } else {
                this.stopSelf();
            }
        }

    }

    // 判断是否需要翻页
    private void changName(AccessibilityNodeInfo accessibilityNodeInfo) {
        boolean hasMyName = false;
        List<String> nameBeforeList = new ArrayList<>();// 修改后的用户名列表
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(listView2);
        if (!nodeInfoList.isEmpty()) {
            AccessibilityNodeInfo abi = nodeInfoList.get(0);
            for (int i = 0; i < abi.getChildCount(); i++) {
                if (abi.getChild(i) != null) {
                    AccessibilityNodeInfo accessibilityNodeInfo1 = abi.getChild(i).getChild(abi.getChild(i).getChildCount() - 1);
                    if (accessibilityNodeInfo1 != null && accessibilityNodeInfo1.getText() != null) {
                        String name = accessibilityNodeInfo1.getText().toString();
                        if (!name.equals(myName)) {
                            nameBeforeList.add(name);
                        } else {
                            hasMyName = true;
                        }
                    }
                }
            }
            boolean isNext = true;
            for (int i = 0; i < nameBeforeList.size(); i++) {
                boolean hasSame = false;
                for (int j = 0; j < nameAfterList.size(); j++) {
                    if (nameBeforeList.get(i).equals(nameAfterList.get(j))) {
                        hasSame = true;
                    }
                }
                if (!hasSame) {
                    isNext = false;
                    break;
                }
            }
            if (isNext) {
                afterNum = nameAfterList.size();
                nodeInfoList.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                changName(accessibilityNodeInfo);
            } else {
                for (int i = 0; i < abi.getChildCount(); i++) {
                    if (abi.getChild(i) != null) {
                        AccessibilityNodeInfo accessibilityNodeInfo1 = abi.getChild(i).getChild(abi.getChild(i).getChildCount() - 1);
                        if (accessibilityNodeInfo1 != null && accessibilityNodeInfo1.getText() != null) {
                            changeName = accessibilityNodeInfo1.getText().toString();
                            changName2();
                        }
                    }
                }
                if (hasMyName) {
                    if (abi.getChildCount() - 1 == (nameAfterList.size() - afterNum)) {
                        changName(accessibilityNodeInfo);
                    }
                } else {
                    if (abi.getChildCount() == (nameAfterList.size() - afterNum)) {
                        changName(accessibilityNodeInfo);
                    }
                }
            }
        }
    }

    // 进入修改名字页面
    private void changName2() {
        boolean isSame = false;
        if (!myName.equals(changeName)) {// 不是我自己
            for (int i = 0; i < nameAfterList.size(); i++) {
                if (nameAfterList.get(i).equals(changeName)) {
                    isSame = true;
                    break;
                }
            }
        } else {
            return;
        }
        if (isSame) {
            return;
        }
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PerformClickUtils.findTextAndClick(this, changeName);
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PerformClickUtils.findViewIdAndClick(this, "com.tencent.mm:id/ao2");
    }

    // 修改名字
    private void changName3(AccessibilityNodeInfo accessibilityNodeInfo) {
        changeName = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ap5").get(0).getText().toString();
        PerformClickUtils.findViewIdAndClick(this, "com.tencent.mm:id/ap5");
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String string = changeName.replace("cyf" + nameAfterList.size(), "");
        // String string = changeName + "cyf" + nameAfterList.size();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, string);
            if (!accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ap4").isEmpty()) {
                accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ap4").get(0).performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            }
        } else {
//                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                        ClipData clip = ClipData.newPlainText(label, text);
//                        clipboard.setPrimaryClip(clip);
//                        accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ap4").get(0).performAction(AccessibilityNodeInfo.ACTION_FOCUS);
//                        accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ap4").get(0).performAction(AccessibilityNodeInfo.ACTION_PASTE);
        }
        // 添加记录
        nameAfterList.add(string);
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PerformClickUtils.findTextAndClick(this, "完成");
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PerformClickUtils.performBack(this);
        isStar = true;
    }

    @Override
    public void onInterrupt() {
        // 辅助服务被关闭 执行此方法
        Toast.makeText(this, "完成", Toast.LENGTH_LONG).show();
    }
}
