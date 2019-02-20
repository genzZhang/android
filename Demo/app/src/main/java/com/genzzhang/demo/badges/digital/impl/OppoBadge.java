package com.genzzhang.demo.badges.digital.impl;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.genzzhang.demo.badges.digital.BroadcastHelper;
import com.genzzhang.demo.badges.digital.IBadge;

import java.util.Collections;
import java.util.List;

/**
 * 申请角标接入规则（应用必须适配OPPO手机，保证角标功能测试通过）
 ​ a) 系统应用
 ​ b) 国内外各区域用户量排名Top5的三方即时通讯类应用，且只允许显示即时通信消息类通知（如QQ、微信、facebook、line）
 ​ c) OPPO公司内部费商业化及运营性质的办公类型即时通信应用（如Teamtalk）
 ​ 4) 国内外邮件类应用（各区域各属于用户量第一梯队的应用）
 */

public class OppoBadge implements IBadge {

    private static final String PROVIDER_CONTENT_URI = "content://com.android.badge/badge";
    private static final String INTENT_ACTION = "com.oppo.unsettledevent";
    private static final String INTENT_EXTRA_PACKAGENAME = "packageName";
    private static final String INTENT_EXTRA_BADGE_COUNT = "number";
    private static final String INTENT_EXTRA_BADGE_UPGRADENUMBER = "upgradeNumber";
    private static final String INTENT_EXTRA_BADGEUPGRADE_COUNT = "app_badge_count";

    @Override
    public boolean executeBadge(Context context, ComponentName componentName, int badgeCount) {
        executeBadgeByBroadcast(context, componentName, badgeCount);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            return executeBadgeByContentProvider(context, badgeCount);
        } else {
            return executeBadgeByBroadcast(context, componentName, badgeCount);
        }
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Collections.singletonList("com.oppo.launcher");
    }

    private boolean executeBadgeByBroadcast(Context context, ComponentName componentName, int badgeCount) {
        if (badgeCount == 0) {
            badgeCount = -1;
        }
        Intent intent = new Intent(INTENT_ACTION);
        intent.putExtra(INTENT_EXTRA_PACKAGENAME, componentName.getPackageName());
        intent.putExtra(INTENT_EXTRA_BADGE_COUNT, badgeCount);
        intent.putExtra(INTENT_EXTRA_BADGE_UPGRADENUMBER, badgeCount);
        try {
            BroadcastHelper.sendIntentExplicitly(context, intent);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Send request to OPPO badge content provider to set badge in OPPO home launcher.
     * @param context       the context to use
     * @param badgeCount    the badge count
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private boolean executeBadgeByContentProvider(Context context, int badgeCount) {
        try {
            Bundle extras = new Bundle();
            extras.putInt(INTENT_EXTRA_BADGEUPGRADE_COUNT, badgeCount);
            context.getContentResolver().call(Uri.parse(PROVIDER_CONTENT_URI), "setAppBadgeCount", null, extras);
        } catch (Throwable throwable) {
            return false;
        }
        return true;
    }
}