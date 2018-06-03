package com.example.solo.autoweixin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class VipUtils {

    public static boolean canVip(Context context, int index) {
        SharedPreferences preferences = context.getSharedPreferences("appInfo", Context.MODE_PRIVATE);
        int vipType = preferences.getInt("vipType", -1);
        boolean reult = false;
        switch (index) {
            case 1:
                if (vipType > -1) {
                    reult = true;
                } else {
                    Toast.makeText(context, "请先至少开启体验会员", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if (vipType > -1) {
                    reult = true;
                } else {
                    Toast.makeText(context, "请先至少开启体验会员", Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:
                if (vipType > 2) {
                    reult = true;
                } else {
                    Toast.makeText(context, "请先至少开启半年度会员", Toast.LENGTH_SHORT).show();
                }
                break;
            case 4:
                if (vipType > 0) {
                    reult = true;
                } else {
                    Toast.makeText(context, "请先至少开启月度会员", Toast.LENGTH_SHORT).show();
                }
                break;
            case 5:
                if (vipType > 2) {
                    reult = true;
                } else {
                    Toast.makeText(context, "请先至少开启半年度会员", Toast.LENGTH_SHORT).show();
                }
                break;
            case 6:
                if (vipType == 0) {
                    if (preferences.getInt("tasteNum40", 0) == 0) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("tasteNum40", 1);
                        editor.apply();
                        return true;
                    }else{
                        Toast.makeText(context, "体验只能使用一次，请先至少开启月度会员", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (vipType > 0) {
                        reult = true;
                    } else {
                        Toast.makeText(context, "请先至少开启体验会员", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case 7:
                if (vipType == 0) {
                    if (preferences.getInt("tasteNum200", 0) == 0) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("tasteNum200", 1);
                        editor.apply();
                        return true;
                    }else{
                        Toast.makeText(context, "体验只能使用一次，请先至少开启月度会员", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (vipType > 0) {
                        reult = true;
                    } else {
                        Toast.makeText(context, "请先至少开启体验会员", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
        return reult;
    }

}
