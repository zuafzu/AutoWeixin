package com.example.solo.autoweixin.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.example.solo.autoweixin.fragment.Fragment1;
import com.example.solo.autoweixin.utils.PerformClickUtils;
import com.example.solo.autoweixin.utils.StringUtils;
import com.example.solo.autoweixin.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.example.solo.autoweixin.utils.PerformClickUtils.performClick;

public class AutoWeixinService extends AccessibilityService {

    public static final String LauncherUI = "com.tencent.mm.ui.LauncherUI";
    public static final String MM = "com.tencent.mm";

    public static String wx_yonghuming = "com.tencent.mm:id/cbx";//用户名id
    public static String wx_haoyouliebiao = "com.tencent.mm:id/j8";//好友列表id
    public static String wx_gengduo = "com.tencent.mm:id/hi";//右上角更多按钮id
    public static String wx_xiugaibeizhu = "com.tencent.mm:id/i8";//修改备注按钮id
    public static String wx_name1 = "com.tencent.mm:id/ap2";//用户名文本id
    public static String wx_name2 = "com.tencent.mm:id/ap2";//用户名输入框id

    public static String wx_yonghuxiangqing = "com.tencent.mm.plugin.profile.ui.ContactInfoUI";// 好友详情页面
    public static String wx_40 = "com.tencent.mm.ui.contact.SelectContactUI";// 40人界面
    public static String wx_200 = "com.tencent.mm.plugin.masssend.ui.MassSendSelectContactUI";// 200人界面

    public static AutoWeixinService autoWeixinService;
    private String currentWindowActivity = "";
    private String errString = "";// 错误信息提示

    public static boolean isChangeNameStart = false;// 是否开启改名
    private String myName = "";// 自己的用户名
    private boolean isStarFriend = false;// 判断是不是星标朋友
    private boolean isAfterStarFriendName = false; // 判断是不是星标朋友下面的朋友
    private String afterStarFriendNameKey = "";// 星标朋友下面的第一个字母
    private List<String> nameBeforeList = new ArrayList<>();//修改前的当前用户名列表
    private String changeName;// 修改者的用户名
    private List<String> nameAfterList = new ArrayList<>();//修改后的总用户名列表(选择联系人和修改备注共用)
    private int index = 0;// 当前修改位置(选择联系人和修改备注共用)
    private ChangNameTask changNameTask;

    public static int selectNum = 0;// 改名数量，0即不选择
    private AccessibilityNodeInfo abni;//联系人翻页控件
    private SelectAllTask selectAllTask;

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
        } else if ("6.6.7".equals(wechatVersion)) {
            wx_yonghuming = "com.tencent.mm:id/y3";
            wx_haoyouliebiao = "com.tencent.mm:id/jq";
            wx_gengduo = "com.tencent.mm:id/hh";
            wx_xiugaibeizhu = "com.tencent.mm:id/ge";
            wx_name1 = "com.tencent.mm:id/arc";
            wx_name2 = "com.tencent.mm:id/arc";
        }
        autoWeixinService = this;
        // 初始化
        myName = "";
        isStarFriend = false;
        isAfterStarFriendName = false;
        afterStarFriendNameKey = "";
        nameAfterList.clear();
        nameBeforeList.clear();
        changeName = "";
        index = 0;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        //如果手机当前界面的窗口发送变化
        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            //获取当前activity的类名:
            currentWindowActivity = accessibilityEvent.getClassName().toString();
            // Log.e("cyf", "currentWindowActivity : " + currentWindowActivity);
            if (LauncherUI.equals(currentWindowActivity)) {// 批量改备注
                if (isChangeNameStart) {
                    AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
                    if (accessibilityNodeInfo == null) {
                        return;
                    }
                    if ("".equals(myName)) {
                        myName = "";
                        isStarFriend = false;
                        isAfterStarFriendName = false;
                        afterStarFriendNameKey = "";
                        nameAfterList.clear();
                        if (Fragment1.fragment1 != null) {
                            for (int i = 0; i < Fragment1.lastNumTotal; i++) {
                                nameAfterList.add(" ");
                            }
                        }
                        nameBeforeList.clear();
                        changeName = "";
                        index = 0;
                        // 获取自己用户名
                        PerformClickUtils.findTextAndClick(this, "我");
                        if (sleepChangeName()) {
                            return;
                        }
                        List<AccessibilityNodeInfo> nil = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(wx_yonghuming);
                        if (!nil.isEmpty()) {
                            AccessibilityNodeInfo ani = nil.get(0);
                            myName = ani.getText().toString();
                        }
                        PerformClickUtils.findTextAndClick(this, "通讯录");
                        PerformClickUtils.findTextAndClick(this, "通讯录");
                        changNameTask = new ChangNameTask();
                        changNameTask.execute();
                    }
                }
            } else if (wx_40.equals(currentWindowActivity)) {
                if (selectNum != 0) {
                    // selectAllTask();
                    selectAllTask = new SelectAllTask();
                    selectAllTask.execute();
                }
            } else if (wx_200.equals(currentWindowActivity)) {
                if (selectNum != 0) {
                    // selectAllTask();
                    selectAllTask = new SelectAllTask();
                    selectAllTask.execute();
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        // 辅助服务被关闭 执行此方法
        autoWeixinService = null;
    }

    /**
     * 改名服务
     */
    class ChangNameTask extends AsyncTask<List<AccessibilityNodeInfo>, Void, Boolean> {

        @Override
        protected Boolean doInBackground(List<AccessibilityNodeInfo>... lists) {
            AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
            if (accessibilityNodeInfo == null) {
                return true;
            }
            List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(wx_haoyouliebiao);
            if (!nodeInfoList.isEmpty()) {
                if (nameBeforeList.size() == 0) {
                    if (sleepChangeName()) {
                        return true;
                    }
                    nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(wx_haoyouliebiao);
                    if (!nodeInfoList.isEmpty()) {
                        AccessibilityNodeInfo abi = nodeInfoList.get(0);
                        for (int i = 0; i < abi.getChildCount(); i++) {
                            if (abi.getChild(i) != null) {
                                // 判断是不是星标朋友
                                if (abi.getChild(i).getChildCount() == 2) {
                                    AccessibilityNodeInfo ani = abi.getChild(i).getChild(0);
                                    if (ani != null && ani.getText() != null && ani.getText() != null) {
                                        String name = ani.getText().toString();
                                        if (name.equals("星标朋友")) {
                                            isStarFriend = true;
                                        } else {
                                            isStarFriend = false;
                                            if (afterStarFriendNameKey.equals("")) {
                                                afterStarFriendNameKey = name;
                                            }
                                        }
                                    }
                                }
                                if (!isStarFriend) {
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
                                                    // 检查是否包含关键字，包含不修改
                                                    if (StringUtils.keyName.equals("")) {
                                                        nameBeforeList.add(name);
                                                    } else {
                                                        StringUtils.keyName = StringUtils.keyName.replace("，", ",");
                                                        if (StringUtils.keyName.contains(",")) {
                                                            boolean hasSame = false;
                                                            String[] keyNames = StringUtils.keyName.split(",");
                                                            for (int j = 0; j < keyNames.length; j++) {
                                                                if (!keyNames[j].isEmpty() && name.contains(keyNames[j])) {
                                                                    hasSame = true;
                                                                    break;
                                                                }
                                                            }
                                                            if (!hasSame) {
                                                                nameBeforeList.add(name);
                                                            }
                                                        } else {
                                                            if (!name.contains(StringUtils.keyName)) {
                                                                nameBeforeList.add(name);
                                                            }
                                                        }
                                                    }
                                                }
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
                            // 判断会员次数是否还有
                            SharedPreferences preferences = getSharedPreferences("appInfo", Context.MODE_PRIVATE);
                            long a = preferences.getLong("totalNum", 0);
                            if (a <= 0) {
                                errString = "会员次数已经全部用光，";
                                return false;
                            }
                            // 开始改名
                            changeName = nameBeforeList.get(index);
                            PerformClickUtils.findTextAndClick(AutoWeixinService.this, changeName);
                            if (sleepChangeName()) {
                                return true;
                            }
                            PerformClickUtils.findViewIdAndClick(AutoWeixinService.this, wx_gengduo);
                            PerformClickUtils.findTextAndClick(AutoWeixinService.this, "更多");
                            if (sleepChangeName()) {
                                return true;
                            }
                            PerformClickUtils.findViewIdAndClick(AutoWeixinService.this, wx_xiugaibeizhu);
                            PerformClickUtils.findTextAndClick(AutoWeixinService.this, "设置备注及标签");
                            if (sleepChangeName()) {
                                return true;
                            }
                            accessibilityNodeInfo = getRootInActiveWindow();
                            PerformClickUtils.findViewIdAndClick(AutoWeixinService.this, wx_name1);
                            PerformClickUtils.findTextAndClick(AutoWeixinService.this, changeName);
                            if (sleepChangeName()) {
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
                            // 记录上次改名的最后一个人名字以及编号模式
                            if (Fragment1.fragment1 != null) {
                                Fragment1.lastName = string;
                                Fragment1.lastNumType = StringUtils.numType;
                                Fragment1.lastNumTotal = nameAfterList.size();
                            }

                            index++;
                            // 判断会员次数减1
                            a--;
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putLong("totalNum", a);
                            editor.apply();

                            // 修改完成并返回
                            if (sleepChangeName()) {
                                return true;
                            }
                            PerformClickUtils.findTextAndClick(AutoWeixinService.this, "完成");
                            if (sleepChangeName()) {
                                return true;
                            }
                            if (currentWindowActivity.equals(wx_yonghuxiangqing)) {
                                PerformClickUtils.performBack(AutoWeixinService.this);
                            } else {
                                errString = "修改备注过程中出现异常，";
                                return false;
                            }
                            if (sleepChangeName()) {
                                return true;
                            }
                        } else {
                            index++;
                        }
                    } else {
                        return false;
                    }
                } else {
                    // 本页不全是星标朋友
                    if (!isStarFriend) {
                        // 检查有没有没改过的
                        nameBeforeList.clear();
                        nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(wx_haoyouliebiao);
                        if (nodeInfoList.isEmpty()) {
                            return false;
                        }
                        AccessibilityNodeInfo abi = nodeInfoList.get(0);
                        for (int i = 0; i < abi.getChildCount(); i++) {
                            if (abi.getChild(i) != null) {
                                // 判断是不是星标朋友下面的朋友
                                if (abi.getChild(i).getChildCount() == 2) {
                                    AccessibilityNodeInfo ani = abi.getChild(i).getChild(0);
                                    if (ani != null && ani.getText() != null && ani.getText() != null) {
                                        String name = ani.getText().toString();
                                        if (name.equals(afterStarFriendNameKey)) {
                                            isAfterStarFriendName = true;
                                        }
                                    }
                                }
                                if (isAfterStarFriendName) {
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
                                                    // 检查是否包含关键字，包含不修改
                                                    if (StringUtils.keyName.equals("")) {
                                                        nameBeforeList.add(name);
                                                    } else {
                                                        StringUtils.keyName = StringUtils.keyName.replace("，", ",");
                                                        if (StringUtils.keyName.contains(",")) {
                                                            boolean hasSame = false;
                                                            String[] keyNames = StringUtils.keyName.split(",");
                                                            for (int j = 0; j < keyNames.length; j++) {
                                                                if (!keyNames[j].isEmpty() && name.contains(keyNames[j])) {
                                                                    hasSame = true;
                                                                    break;
                                                                }
                                                            }
                                                            if (!hasSame) {
                                                                nameBeforeList.add(name);
                                                            }
                                                        } else {
                                                            if (!name.contains(StringUtils.keyName)) {
                                                                nameBeforeList.add(name);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
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
                                hasSame = true;
                                break;
                            }
                        }
                        if (!hasSame) {
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
            if (isChangeNameStart) {
                if (s) {
                    changNameTask = new ChangNameTask();
                    changNameTask.execute();
                } else {
                    // 被动停止
                    isChangeNameStart = false;
                    myName = "";
                    isStarFriend = false;
                    isAfterStarFriendName = false;
                    afterStarFriendNameKey = "";
                    nameAfterList.clear();
                    nameBeforeList.clear();
                    changeName = "";
                    index = 0;
                    Toast.makeText(AutoWeixinService.this, errString + "修改备注已经停止了！", Toast.LENGTH_SHORT).show();
                    if (Fragment1.fragment1 != null) {
                        Fragment1.fragment1.initCreatFloatWindow();
                    }
                    if (!errString.equals("")) {
                        errString = "";
                        if (Fragment1.fragment1 != null) {
                            Fragment1.fragment1.comeBack();
                        }
                    }
                }
            } else {
                // 主动停止
                myName = "";
                isStarFriend = false;
                isAfterStarFriendName = false;
                afterStarFriendNameKey = "";
                nameAfterList.clear();
                nameBeforeList.clear();
                changeName = "";
                index = 0;
                Toast.makeText(AutoWeixinService.this, "修改备注已经停止了！", Toast.LENGTH_SHORT).show();
                if (Fragment1.fragment1 != null) {
                    Fragment1.fragment1.initCreatFloatWindow();
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            isChangeNameStart = false;
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            isChangeNameStart = false;
        }
    }

    /**
     * 改名睡眠
     */
    private boolean sleepChangeName() {
        try {
            long time = 1500;
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return !isChangeNameStart;
    }

    /**
     * 选择列表
     */
    class SelectAllTask extends AsyncTask<List<AccessibilityNodeInfo>, Void, Boolean> {

        @Override
        protected Boolean doInBackground(List<AccessibilityNodeInfo>... lists) {
            AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
            if (accessibilityNodeInfo == null) {
                return false;
            }
            recycle(accessibilityNodeInfo);
            if (nameAfterList.size() >= selectNum) {
                return false;
            } else {
                // 翻页
                if (abni != null) {
                    boolean flag = abni.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                    if (flag) {
                        if (sleepSelectAll(1000)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            if (s) {
                selectAllTask = new SelectAllTask();
                selectAllTask.execute();
            } else {
                // 停止
                selectNum = 0;
                nameAfterList.clear();
                Toast.makeText(AutoWeixinService.this, "选择联系人已经停止了！", Toast.LENGTH_SHORT).show();
                if (Fragment1.fragment1 != null) {
                    Fragment1.fragment1.initCreatFloatWindow();
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            selectNum = 0;
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            selectNum = 0;
        }

        /**
         * 选择列表递归
         */
        public void recycle(AccessibilityNodeInfo info) {
            if (info.getChildCount() == 0) {
                if (info.getClassName().toString().contains("CheckBox")) {
                    if (nameAfterList.size() >= selectNum) {
                        // 停止
                    } else {
                        if (!info.isChecked() && info.isCheckable()) {
                            performClick(info);
                            nameAfterList.add(info.getClassName().toString());
                            abni = info.getParent().getParent();
                            if (sleepSelectAll(500)) {

                            }
                        }
                    }
                }
            } else {
                for (int i = 0; i < info.getChildCount(); i++) {
                    if (info.getChild(i) != null) {
                        recycle(info.getChild(i));
                    }
                }
            }
        }

    }

    /**
     * 选择睡眠
     */
    private boolean sleepSelectAll(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return selectNum == 0;
    }

}
