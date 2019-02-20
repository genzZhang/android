package com.genzzhang.demo.badges.digital.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.genzzhang.demo.badges.digital.BroadcastHelper;
import com.genzzhang.demo.badges.digital.IBadge;

import java.util.Collections;
import java.util.List;

/**
 * HTC桌面
 * 通知栏和桌面需要同时发
 */
public class HtcBadge implements IBadge {

    public static final String INTENT_UPDATE_SHORTCUT = "com.htc.launcher.action.UPDATE_SHORTCUT";
    public static final String INTENT_SET_NOTIFICATION = "com.htc.launcher.action.SET_NOTIFICATION";
    public static final String PACKAGENAME = "packagename";
    public static final String COUNT = "count";
    public static final String EXTRA_COMPONENT = "com.htc.launcher.extra.COMPONENT";
    public static final String EXTRA_COUNT = "com.htc.launcher.extra.COUNT";

    @Override
    public boolean executeBadge(Context context, ComponentName componentName, int badgeCount) {
        Intent notificationIntent = new Intent(INTENT_SET_NOTIFICATION);
        boolean notificationIntentSuccess;
        notificationIntent.putExtra(EXTRA_COMPONENT, componentName.flattenToShortString());
        notificationIntent.putExtra(EXTRA_COUNT, badgeCount);
        Intent intent = new Intent(INTENT_UPDATE_SHORTCUT);
        boolean intentSuccess;
        intent.putExtra(PACKAGENAME, componentName.getPackageName());
        intent.putExtra(COUNT, badgeCount);
        try {
            BroadcastHelper.sendIntentExplicitly(context, notificationIntent);
            notificationIntentSuccess = true;
        } catch (Throwable e) {
            notificationIntentSuccess = false;
        }
        try {
            BroadcastHelper.sendIntentExplicitly(context, intent);
            intentSuccess = true;
        } catch (Throwable e) {
            intentSuccess = false;
        }

        return notificationIntentSuccess && intentSuccess;
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Collections.singletonList("com.htc.launcher");
    }
}
