package com.genzzhang.demo.badges.digital;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.Collections;
import java.util.List;


public class BroadcastHelper {

    private static List<ResolveInfo> resolveBroadcast(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> receivers = packageManager.queryBroadcastReceivers(intent, 0);
        return receivers != null ? receivers : Collections.<ResolveInfo>emptyList();
    }

    public static void sendIntentExplicitly(Context context, Intent intent) throws Exception {
        List<ResolveInfo> resolveInfos = resolveBroadcast(context, intent);
        if (resolveInfos.size() == 0) {
            throw new Exception("unable to resolve intent: " + intent.toString());
        }
        for (ResolveInfo info : resolveInfos) {
            Intent actualIntent = new Intent(intent);
            if (info != null) {
                actualIntent.setPackage(info.resolvePackageName);
                context.sendBroadcast(actualIntent);
            }
        }
    }

}
