package com.genzzhang.demo.badges.digital.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.genzzhang.demo.badges.digital.BroadcastHelper;
import com.genzzhang.demo.badges.digital.IBadge;

import java.util.Arrays;
import java.util.List;


/**
 * 默认的
 * 微信制定了一个关于角标的标准，部分厂商主动支持（目前发现的有：锤子，ZTC，乐视，部分华为等）
 * 能自由控制角标，ZUK开发平台有该方案的文章
 * http://developer.zuk.com/detail/12
 */
public class DefaultBadge implements IBadge {
    private static final String INTENT_ACTION = "android.intent.action.BADGE_COUNT_UPDATE";
    private static final String INTENT_EXTRA_BADGE_COUNT = "badge_count";
    private static final String INTENT_EXTRA_PACKAGENAME = "badge_count_package_name";
    private static final String INTENT_EXTRA_ACTIVITY_NAME = "badge_count_class_name";

    @Override
    public boolean executeBadge(Context context, ComponentName componentName, int badgeCount) {
        boolean success = false;

        //微信通用方案
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Bundle extra = new Bundle();
            extra.putStringArrayList("app_shortcut_custom_id", null);
            extra.putInt("app_badge_count", badgeCount);
            Uri CONTENT_URI = Uri.parse("content://" + "com.android.badge" + "/" + "badge");
            try {
                context.getContentResolver().call(CONTENT_URI, "setAppBadgeCount", null, extra);
                success = true;
            } catch (Throwable throwable) {

            }
        }
        //git上的通用方案
        Intent intent = new Intent(INTENT_ACTION);
        intent.putExtra(INTENT_EXTRA_BADGE_COUNT, badgeCount);
        intent.putExtra(INTENT_EXTRA_PACKAGENAME, componentName.getPackageName());
        intent.putExtra(INTENT_EXTRA_ACTIVITY_NAME, componentName.getClassName());
        try {
            BroadcastHelper.sendIntentExplicitly(context, intent);
            success = true;
        } catch (Throwable throwable) {

        }

        return success;
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Arrays.asList(
                "fr.neamar.kiss",
                "com.quaap.launchtime",
                "com.quaap.launchtime_official"
        );
    }

}
