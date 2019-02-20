package com.genzzhang.demo.badges.digital.impl;

import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.genzzhang.demo.badges.digital.IBadge;

import java.util.ArrayList;
import java.util.List;


public class ZteBadge implements IBadge {

    @Override
    public boolean executeBadge(Context context, ComponentName componentName, int badgeCount) {
        boolean success = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Bundle extra = new Bundle();
            extra.putInt("app_badge_count", badgeCount);
            extra.putString("app_badge_component_name", componentName.flattenToString());
            context.getContentResolver().call(
                    Uri.parse("content://com.android.launcher3.cornermark.unreadbadge"),
                    "setAppUnreadCount", null, extra);
            success = true;
        }
        return success;
    }

    @Override
    public List<String> getSupportLaunchers() {
        return new ArrayList<String>(0);
    }
} 

