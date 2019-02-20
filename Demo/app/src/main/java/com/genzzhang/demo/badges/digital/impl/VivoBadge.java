package com.genzzhang.demo.badges.digital.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.genzzhang.demo.badges.digital.IBadge;

import java.util.Arrays;
import java.util.List;


/**
 * vivo 应该不支持，网上说，客服表示不支持没有申请渠道
 */
public class VivoBadge implements IBadge {

    @Override
    public boolean executeBadge(Context context, ComponentName componentName, int badgeCount) {
        boolean success = false;
        Intent intent = new Intent("launcher.action.CHANGE_APPLICATION_NOTIFICATION_NUM");
        intent.putExtra("packageName", context.getPackageName());
        intent.putExtra("className", componentName.getClassName());
        intent.putExtra("notificationNum", badgeCount);
        try {
            context.sendBroadcast(intent);
            success = true;
        } catch (Throwable throwable) {

        }
        return success;
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Arrays.asList("com.vivo.launcher");
    }
}
