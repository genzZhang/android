package com.genzzhang.demo.badges.digital.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.genzzhang.demo.badges.digital.IBadge;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static com.genzzhang.demo.util.C.TAG;


/**
 * 华为桌面指导说明
 * https://developer.huawei.com/consumer/cn/devservice/doc/30802
 * 设置HUAWEI EMUI-3.1及以上系统角标
 */
public class HuaweiBadge implements IBadge {

    @Override
    public boolean executeBadge(Context context, ComponentName componentName, int badgeCount) {
        boolean success = false;
        try {
            Bundle localBundle = new Bundle();
            localBundle.putString("package", context.getPackageName());
            localBundle.putString("class", componentName.getClassName());
            localBundle.putInt("badgenumber", badgeCount);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                context.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"), "change_badge", null, localBundle);
                success = true;
            }
        } catch (Throwable throwable) {
            Log.e(TAG, throwable.toString());
        }
        return success;
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Arrays.asList("com.huawei.android.launcher"
        );
    }

    /**
     * 当前华为手机是否支持角标
     * @param context
     * @return
     */
    public boolean isSupported(Context context){
        Context LauncherContext = null;
        try {
            LauncherContext = context.createPackageContext("com.huawei.android.launcher", Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
            Class<?> c =
                    LauncherContext.getClassLoader().loadClass("com.huawei.android.launcher.LauncherProvider");
            Object obj =c.newInstance();
            Method m = c.getMethod("isSupportChangeBadgeByCallMethod", new Class[]{});
            Boolean isSupport = (Boolean) m.invoke(obj, new Object[]{});
            return isSupport;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}
