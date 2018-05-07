package com.example.solo.autoweixin.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;

public class StringUtils {

    public static boolean hasPreName = false;// 是否有前缀名
    public static String preName = "";// 前缀名
    public static int numType = 0;// 编号模式
    public static int index = 0;// 位置
    public static String keyName = "";// 关键字

    public static String getName(String name, int mIndex) {
        String string = "";
        if (hasPreName) {
            if ("".equals(preName)) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("M.d");
                string = sdf.format(new java.util.Date());
            } else {
                string = preName;
            }
        }
        if (numType == 0) {
            string = string + "";
        } else if (numType == 1) {
            string = string + "-" + (mIndex + 1);
        } else if (numType == 2) {
            string = string + "-" + "邀" + (mIndex / 40 + 1) + "-" + (mIndex % 40 + 1);
        } else if (numType == 3) {
            string = string + "-" + "发" + (mIndex / 200 + 1) + "-" + (mIndex % 200 + 1);
        }
        if (name.length() > index) {
            string = name.substring(0, index) + string + name.substring(index, name.length());
        } else {
            string = name + string;
        }
        return string;
    }

}
