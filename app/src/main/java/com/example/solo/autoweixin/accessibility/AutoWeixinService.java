package com.example.solo.autoweixin.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
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
import java.util.Objects;

import static com.example.solo.autoweixin.utils.PerformClickUtils.performClick;

public class AutoWeixinService extends AccessibilityService {

    public static final String LauncherUI = "com.tencent.mm.ui.LauncherUI";
    public static final String MM = "com.tencent.mm";

    public static String wx_yonghuming = "";//用户名id
    public static String wx_biaoqian = "";//用户标签
    public static String wx_haoyouliebiao = "";//好友列表id
    public static String wx_gengduo = "";//右上角更多按钮id
    public static String wx_xiugaibeizhu = "";//修改备注按钮id
    public static String wx_name1 = "";//用户名文本id
    public static String wx_name2 = "";//用户名输入框id

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
    private List<String> nameAfterList = new ArrayList<>();// 修改后的总用户名列表(选择联系人和修改备注共用)
    private List<String> userTagNameList = new ArrayList<>();// 包含标签的用户名
    private int index = 0;// 当前修改位置(选择联系人和修改备注共用)
    private ChangNameTask changNameTask;

    public static int selectNum = 0;// 改名数量，0即不选择
    private AccessibilityNodeInfo abni;//联系人翻页控件
    private SelectAllTask selectAllTask;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        SharedPreferences preferences = Objects.requireNonNull(getSharedPreferences("appInfo", Context.MODE_PRIVATE));
        String wechatVersion = Utils.getVersion(this);
        if ("".equals(preferences.getString("version_id", ""))) {
            Toast.makeText(AutoWeixinService.this, "请完全退出并重新启动改名宝，点击应用内开启辅助服务！", Toast.LENGTH_LONG).show();
        } else {
            if (wechatVersion.equals(preferences.getString("version_id", ""))) {
                if ("".equals(wx_yonghuming)) {
                    wx_yonghuming = preferences.getString("wx_yonghuming", "");
                    wx_biaoqian = preferences.getString("wx_biaoqian", "");
                    wx_haoyouliebiao = preferences.getString("wx_haoyouliebiao", "");
                    wx_gengduo = preferences.getString("wx_gengduo", "");
                    wx_xiugaibeizhu = preferences.getString("wx_xiugaibeizhu", "");
                    wx_name1 = preferences.getString("wx_name1", "");
                    wx_name2 = preferences.getString("wx_name2", "");
                }
            } else {
                Toast.makeText(AutoWeixinService.this, "微信版本发生变化，请完全退出并重新启动改名宝，点击应用内开启辅助服务！", Toast.LENGTH_LONG).show();
            }
        }
        // 辅助服务被打开后 执行此方法
        autoWeixinService = this;
        myName = "";
        isStarFriend = false;
        isAfterStarFriendName = false;
        afterStarFriendNameKey = "";
        nameAfterList.clear();
        userTagNameList.clear();
        nameBeforeList.clear();
        changeName = "";
        index = 0;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        //如果手机当前界面的窗口发送变化
        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            //获取当前activity的类名:
            if (accessibilityEvent.getClassName() != null) {
                currentWindowActivity = accessibilityEvent.getClassName().toString();
            }
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
                        userTagNameList.clear();
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
            SharedPreferences preferences = Objects.requireNonNull(getSharedPreferences("appInfo", Context.MODE_PRIVATE));

            AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
            if (accessibilityNodeInfo == null) {
                return true;
            }
            Log.e("cyf", "11111111111111    " + wx_haoyouliebiao);
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
                                // 判断是不是星标朋友或者我的企业
                                if (abi.getChild(i).getChildCount() == 2) {
                                    AccessibilityNodeInfo ani = abi.getChild(i).getChild(0);
                                    if (ani != null && ani.getText() != null && ani.getText() != null) {
                                        String name = ani.getText().toString();
                                        if (name.equals("星标朋友") || name.equals("我的企业")) {
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
                                                    if (preferences.getString("keyName", "").equals("")) {
                                                        nameBeforeList.add(name);
                                                    } else {
                                                        // StringUtils.keyName = StringUtils.keyName.replace("，", ",");
                                                        if (preferences.getString("keyName", "").contains(",")) {
                                                            boolean hasSame = false;
                                                            String[] keyNames = preferences.getString("keyName", "").split(",");
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
                                                            if (!name.contains(preferences.getString("keyName", ""))) {
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
                            // 新版本取消次数限制
                            long a = 9L;
                            // long a = preferences.getLong("totalNum", 0);
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
                            // 判断是否有标签，标签是否需要跳过
                            accessibilityNodeInfo = getRootInActiveWindow();
                            String wechatVersion = Utils.getVersion(getApplication());
                            String userTag = preferences.getString("userTag", "");// 用户标签
                            // 判断微信版本名称大于等于6.7.2，并且标签不是空，并且该用户有标签
                            if (Integer.valueOf(wechatVersion.replace(".", "").substring(0, 3)) >= 672 &&
                                    !userTag.equals("") &&
                                    accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(wx_biaoqian).size() > 0) {
                                String[] userTags = userTag.split(",");
                                for (String userTag1 : userTags) {
                                    String mUserTagName = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(wx_biaoqian).get(0).getText().toString();
                                    if (mUserTagName.equals(userTag1)) {
                                        // 包含该标签，跳过
                                        index++;
                                        userTagNameList.add(changeName);
                                        if (currentWindowActivity.equals(wx_yonghuxiangqing)) {
                                            PerformClickUtils.performBack(AutoWeixinService.this);
                                        } else {
                                            errString = "修改备注过程中出现异常，";
                                            return false;
                                        }
                                        if (sleepChangeName()) {
                                            return true;
                                        }
                                        return true;
                                    }
                                }
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
                            String string = StringUtils.getName(AutoWeixinService.this, changeName, nameAfterList.size());
                            Bundle arguments = new Bundle();
                            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, string);
                            // 防止奔溃校验
                            if (accessibilityNodeInfo == null) {
                                accessibilityNodeInfo = getRootInActiveWindow();
                            }
                            if (accessibilityNodeInfo != null && !accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(wx_name2).isEmpty()) {
                                accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(wx_name2).get(0).performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                            }
                            // 添加记录
                            nameAfterList.add(string);
                            // 记录上次改名的最后一个人名字以及编号模式
                            if (Fragment1.fragment1 != null) {
                                Fragment1.lastName = string;
                                Fragment1.lastNumType = preferences.getInt("numType", 0);
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
                                                    if (preferences.getString("keyName", "").equals("")) {
                                                        nameBeforeList.add(name);
                                                    } else {
                                                        // StringUtils.keyName = StringUtils.keyName.replace("，", ",");
                                                        if (preferences.getString("keyName", "").contains(",")) {
                                                            boolean hasSame = false;
                                                            String[] keyNames = preferences.getString("keyName", "").split(",");
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
                                                            if (!name.contains(preferences.getString("keyName", ""))) {
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
                            for (int k = 0; k < userTagNameList.size(); k++) {
                                if (nameBeforeList.get(i).equals(userTagNameList.get(k))) {
                                    hasSame = true;
                                    break;
                                }
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
                            nameBeforeList.clear();
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
            long time = 2000;
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
                        if (sleepSelectAll(1500)) {
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
                            if (sleepSelectAll(800)) {

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
