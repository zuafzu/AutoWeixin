package com.example.solo.autoweixin.utils;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class StringUtils {

//    public static boolean hasPreName = false;// 是否有前缀名
//    public static String preName = "";// 前缀名
//    public static int numType = 0;// 编号模式
//    public static int index = 0;// 位置
//    public static String keyName = "";// 关键字
//    public static boolean isChange = false;// 是否替换备注名

    public static String getName(Service service, String name, int mIndex) {
        SharedPreferences preferences = Objects.requireNonNull(service.getSharedPreferences("appInfo", Context.MODE_PRIVATE));

        String string = "";
        if (preferences.getBoolean("hasPreName", false)) {
            if ("".equals(preferences.getString("preName", ""))) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("M.d");
                string = "【" + sdf.format(new java.util.Date()) + "】";
            } else {
                string = "【" + preferences.getString("preName", "") + "】";
            }
        }
        if (preferences.getInt("numType", 0) == 0) {
            string = string + "";
        } else if (preferences.getInt("numType", 0) == 1) {
            if (string.equals("")) {
                string = string + "" + (mIndex + 1);
            } else {
                string = string + "-" + (mIndex + 1);
            }
        } else if (preferences.getInt("numType", 0) == 2) {
            if (string.equals("")) {
                string = string + "" + "邀" + (mIndex / 40 + 1) + "-" + (mIndex % 40 + 1);
            } else {
                string = string + "-" + "邀" + (mIndex / 40 + 1) + "-" + (mIndex % 40 + 1);
            }
        } else if (preferences.getInt("numType", 0) == 3) {
            if (string.equals("")) {
                string = string + "" + "发" + (mIndex / 200 + 1) + "-" + (mIndex % 200 + 1);
            } else {
                string = string + "-" + "发" + (mIndex / 200 + 1) + "-" + (mIndex % 200 + 1);
            }
        }
        if (preferences.getBoolean("isChange", false)) {
            if (preferences.getInt("numType", 0) == 0) {
                string = string + "-" + "改名宝";
            }
        } else {
            if (name.length() > preferences.getInt("index", 0)) {
                string = name.substring(0, preferences.getInt("index", 0)) +
                        string +
                        name.substring(preferences.getInt("index", 0), name.length());
            } else {
                string = name + string;
            }
        }
        return string;
    }

}
