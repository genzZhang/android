package com.genzzhang.demo.badges.digital.impl;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import com.genzzhang.demo.badges.digital.IBadge;

import java.util.Arrays;
import java.util.List;


/**
 * https://github.com/leolin310148/ShortcutBadger/tree/master/ShortcutBadger
 * 三星这个方案有问题，那个微桌面是可以的。可以参考下微桌面是怎么做的
 */
public class SamsungBadge implements IBadge {
    private static final String CONTENT_URI = "content://com.sec.badge/apps?notify=true";
    private static final String[] CONTENT_PROJECTION = new String[]{"_id", "class"};

    private DefaultBadge defaultBadger;

    public SamsungBadge() {
        if (Build.VERSION.SDK_INT >= 21) {
            defaultBadger = new DefaultBadge();
        }
    }

    @Override
    public boolean executeBadge(Context context, ComponentName componentName, int badgeCount) {
        if (defaultBadger != null) {
            return defaultBadger.executeBadge(context, componentName, badgeCount);
        } else {
            Uri mUri = Uri.parse(CONTENT_URI);
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = contentResolver.query(mUri, CONTENT_PROJECTION, "package=?", new String[]{componentName.getPackageName()}, null);
                if (cursor != null) {
                    String entryActivityName = componentName.getClassName();
                    boolean entryActivityExist = false;
                    while (cursor.moveToNext()) {
                        int id = cursor.getInt(0);
                        ContentValues contentValues = getContentValues(componentName, badgeCount, false);
                        contentResolver.update(mUri, contentValues, "_id=?", new String[]{String.valueOf(id)});
                        if (entryActivityName.equals(cursor.getString(cursor.getColumnIndex("class")))) {
                            entryActivityExist = true;
                        }
                    }
                    if (!entryActivityExist) {
                        ContentValues contentValues = getContentValues(componentName, badgeCount, true);
                        contentResolver.insert(mUri, contentValues);
                    }
                }
            } catch (Throwable throwable) {
                return false;
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }
        return true;
    }

    private ContentValues getContentValues(ComponentName componentName, int badgeCount, boolean isInsert) {
        ContentValues contentValues = new ContentValues();
        if (isInsert) {
            contentValues.put("package", componentName.getPackageName());
            contentValues.put("class", componentName.getClassName());
        }
        contentValues.put("badgecount", badgeCount);
        return contentValues;
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Arrays.asList(
                "com.sec.android.app.launcher",
                "com.sec.android.app.twlauncher"
        );
    }
}
