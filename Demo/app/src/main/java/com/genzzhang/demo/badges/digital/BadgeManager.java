package com.genzzhang.demo.badges.digital;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;

import com.genzzhang.demo.badges.digital.impl.DefaultBadge;
import com.genzzhang.demo.badges.digital.impl.HtcBadge;
import com.genzzhang.demo.badges.digital.impl.HuaweiBadge;
import com.genzzhang.demo.badges.digital.impl.OppoBadge;
import com.genzzhang.demo.badges.digital.impl.SamsungBadge;
import com.genzzhang.demo.badges.digital.impl.SonyBadge;
import com.genzzhang.demo.badges.digital.impl.VivoBadge;
import com.genzzhang.demo.badges.digital.impl.XiaomiBadge;
import com.genzzhang.demo.badges.digital.impl.ZteBadge;

import java.util.LinkedList;
import java.util.List;


/**
 * 桌面角标管理类
 * https://github.com/leolin310148/ShortcutBadger/tree/master/ShortcutBadger
 * 问题：https://github.com/leolin310148/ShortcutBadger/issues
 */
public final class BadgeManager {

    private static final String TAG = "BadgeManager";
    private static final List<Class<? extends IBadge>> BADGERS = new LinkedList<Class<? extends IBadge>>();

    static {
        BADGERS.add(HuaweiBadge.class);
        BADGERS.add(OppoBadge.class);
        BADGERS.add(VivoBadge.class);
        BADGERS.add(XiaomiBadge.class);
        BADGERS.add(SamsungBadge.class);
        BADGERS.add(HtcBadge.class);
        BADGERS.add(ZteBadge.class);
        BADGERS.add(DefaultBadge.class);
        BADGERS.add(SonyBadge.class);
    }

    private static IBadge sIBadge;
    private static ComponentName sComponentName;
    private BadgeManager() {

    }

    /**
     * Tries to update the notification count
     *
     * @param context    Caller context
     * @param badgeCount Desired badge count
     * @return true in case of success, false otherwise
     */
    public static boolean applyCount(Context context, int badgeCount) {
        boolean success = false;
        try {
            if (sIBadge == null) {
                if (!initBadge(context)) {
                    return false;
                }
            }
            if (sIBadge != null) {
                success = sIBadge.executeBadge(context, sComponentName, badgeCount);
            }
        } catch (Throwable throwable) {

        }
        return success;
    }


    /**
     * Tries to remove the notification count
     * @param context Caller context
     * @return true in case of success, false otherwise
     */
    public static boolean removeCount(Context context) {
        return applyCount(context, 0);
    }


    // Initialize IBadge if a launcher is availalble (eg. set as default on the device)
    // Returns true if a launcher is available, in this case, the IBadge will be set and sIBadge will be non null.
    private static boolean initBadge(Context context) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (launchIntent == null) {
            Log.e(TAG, "Unable to find launch intent for package " + context.getPackageName());
            return false;
        }
        sComponentName = launchIntent.getComponent();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String currentHomePackage = resolveInfo.activityInfo.packageName;
            for (Class<? extends IBadge> badger : BADGERS) {
                IBadge shortcutIBadge = null;
                try {
                    shortcutIBadge = badger.newInstance();
                } catch (Exception ignored) {
                }
                if (shortcutIBadge != null && shortcutIBadge.getSupportLaunchers().contains(currentHomePackage)) {
                    sIBadge = shortcutIBadge;
                    break;
                }
            }
            if (sIBadge != null) {
                break;
            }
        }

        if (sIBadge == null) {
            if (Build.BRAND.equalsIgnoreCase("oppo")) {
                sIBadge = new OppoBadge();
            } else if (Build.BRAND.equalsIgnoreCase("vivo")) {
                sIBadge = new VivoBadge();
            } else if (Build.BRAND.equalsIgnoreCase("xiaomi")) {
                sIBadge = new XiaomiBadge();
            } else if (Build.BRAND.equalsIgnoreCase("zte")) {
                sIBadge = new ZteBadge();
            } else if (Build.BRAND.equalsIgnoreCase("huawei") ||
                    Build.BRAND.equalsIgnoreCase("honor")) {
                sIBadge = new HuaweiBadge();
            } else {
                sIBadge = new DefaultBadge();
            }
        }
        return true;
    }

}
