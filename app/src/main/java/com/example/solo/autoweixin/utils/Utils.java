package com.example.solo.autoweixin.utils;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by linhao on 16/12/30.
 */

public class Utils {

    /**
     * 获取微信的版本号
     *
     * @param context
     * @return
     */
    public static String getVersion(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfoList) {
            if ("com.tencent.mm".equals(packageInfo.packageName)) {
                return packageInfo.versionName;
            }
        }
        return "6.6.6";
    }

    /**
     * 获取本应用的版本号
     *
     * @param context
     * @return
     */
    public static String getMyVersion(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfoList) {
            if ("com.example.solo.autoweixin".equals(packageInfo.packageName)) {
                return packageInfo.versionName;
            }
        }
        return "";
    }

    /**
     * 判断 悬浮窗口权限是否打开
     *
     * @param context
     * @return true 允许  false禁止
     */
    public static boolean getAppOps(Context context) {
        try {
            @SuppressLint("WrongConstant")
            Object object = context.getSystemService("appops");
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = Integer.valueOf(24);
            arrayOfObject1[1] = Integer.valueOf(Binder.getCallingUid());
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1)).intValue();
            if (m == AppOpsManager.MODE_ERRORED) {
                return true;
            }
            return m == AppOpsManager.MODE_ALLOWED || m == AppOpsManager.MODE_DEFAULT;
        } catch (Exception ex) {

        }
        return false;
    }

}
