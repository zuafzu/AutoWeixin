package com.example.solo.autoweixin.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;

public class StringUtils {

    public static boolean hasPreName = false;
    public static String preName = "";
    public static boolean hasNum = false;
    public static int index = 0;

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
        if (hasNum) {
            string = string + "-" + mIndex;
        }
        if (name.length() > index) {
            string = name.substring(0, index) + string + name.substring(index, name.length());
        } else {
            string = name + string;
        }
        return string;
    }

}
