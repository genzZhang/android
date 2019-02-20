package com.genzzhang.demo.badges.digital.impl;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.genzzhang.demo.R;
import com.genzzhang.demo.badges.BadgesActivity;
import com.genzzhang.demo.badges.digital.IBadge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;



/**
 * https://dev.mi.com/console/doc/detail?pId=939
 * 小米条数有限制，进程起来一次只能有一次
 */
public class XiaomiBadge implements IBadge {

    @Override
    public boolean executeBadge(Context context, ComponentName componentName, int badgeCount) {
        return tryNewMiuiBadge(context, badgeCount);
    }

    private boolean tryNewMiuiBadge(Context context, int badgeCount) {
        // 先要检查通知栏权限，这里算了
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent mainIntent = new Intent(context, BadgesActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //创建 Notification.Builder 对象
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                //点击通知后自动清除
                .setAutoCancel(true)
                .setContentTitle("桌面图标提示")
                .setContentText("您有重要新事件提醒")
                .setContentIntent(mainPendingIntent);
        Notification notification = builder.build();
        //发送通知
        notifyManager.notify(1, notification);
        //更新脚标
        try {
            Field field = notification.getClass().getDeclaredField("extraNotification");
            Object extraNotification = field.get(notification);
            Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
            method.invoke(extraNotification, badgeCount);
        } catch (Throwable e) {
            return false;
        }
        return true;
    }


    @Override
    public List<String> getSupportLaunchers() {
        return Arrays.asList(
                "com.miui.miuilite",
                "com.miui.home",
                "com.miui.miuihome",
                "com.miui.miuihome2",
                "com.miui.mihome",
                "com.miui.mihome2",
                "com.i.miui.launcher"
        );
    }
}
