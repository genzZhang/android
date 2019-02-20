package com.genzzhang.demo.badges.digital.impl;

import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Looper;

import com.genzzhang.demo.badges.digital.IBadge;

import java.util.Arrays;
import java.util.List;


/**
 * https://github.com/leolin310148/ShortcutBadger/tree/master/ShortcutBadger
 */
public class SonyBadge implements IBadge {

    private static final String INTENT_ACTION = "com.sonyericsson.home.action.UPDATE_BADGE";
    private static final String INTENT_EXTRA_PACKAGE_NAME = "com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME";
    private static final String INTENT_EXTRA_ACTIVITY_NAME = "com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME";
    private static final String INTENT_EXTRA_MESSAGE = "com.sonyericsson.home.intent.extra.badge.MESSAGE";
    private static final String INTENT_EXTRA_SHOW_MESSAGE = "com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE";

    private static final String PROVIDER_CONTENT_URI = "content://com.sonymobile.home.resourceprovider/badge";
    private static final String PROVIDER_COLUMNS_BADGE_COUNT = "badge_count";
    private static final String PROVIDER_COLUMNS_PACKAGE_NAME = "package_name";
    private static final String PROVIDER_COLUMNS_ACTIVITY_NAME = "activity_name";
    private static final String SONY_HOME_PROVIDER_NAME = "com.sonymobile.home.resourceprovider";
    private final Uri BADGE_CONTENT_URI = Uri.parse(PROVIDER_CONTENT_URI);

    private AsyncQueryHandler mQueryHandler;

    @Override
    public boolean executeBadge(Context context, ComponentName componentName, int badgeCount) {
        if (sonyBadgeContentProviderExists(context)) {
            return executeBadgeByContentProvider(context, componentName, badgeCount);
        } else {
            return executeBadgeByBroadcast(context, componentName, badgeCount);
        }
    }

    @Override
    public List<String> getSupportLaunchers() {
        return Arrays.asList("com.sonyericsson.home", "com.sonymobile.home");
    }

    private static boolean executeBadgeByBroadcast(Context context, ComponentName componentName,int badgeCount) {
        try {
            Intent intent = new Intent(INTENT_ACTION);
            intent.putExtra(INTENT_EXTRA_PACKAGE_NAME, componentName.getPackageName());
            intent.putExtra(INTENT_EXTRA_ACTIVITY_NAME, componentName.getClassName());
            intent.putExtra(INTENT_EXTRA_MESSAGE, String.valueOf(badgeCount));
            intent.putExtra(INTENT_EXTRA_SHOW_MESSAGE, badgeCount > 0);
            context.sendBroadcast(intent);
            return true;
        } catch (Throwable throwable) {

        }
        return false;
    }

    /**
     * Send request to Sony badge content provider to set badge in Sony home launcher.
     *
     * @param context       the context to use
     * @param componentName the componentName to use
     * @param badgeCount    the badge count
     */
    private boolean executeBadgeByContentProvider(Context context, ComponentName componentName,int badgeCount) {
        if (badgeCount < 0) {
            return false;
        }
        boolean success = false;
        try {
            final ContentValues contentValues = createContentValues(badgeCount, componentName);
            if (Looper.myLooper() == Looper.getMainLooper()) {
                // We're in the main thread. Let's ensure the badge update happens in a background
                // thread by using an AsyncQueryHandler and an async update.
                if (mQueryHandler == null) {
                    mQueryHandler = new AsyncQueryHandler(
                            context.getApplicationContext().getContentResolver()) {
                    };
                }
                insertBadgeAsync(contentValues);
            } else {
                // Already in a background thread. Let's update the badge synchronously. Otherwise,
                // if we use the AsyncQueryHandler, this thread may already be dead by the time the
                // async execution finishes, which will lead to an IllegalStateException.
                insertBadgeSync(context, contentValues);
            }
            success = true;
        } catch (Throwable e) {

        }
        return success;
    }

    /**
     * Asynchronously inserts the badge counter.
     *
     * @param contentValues Content values containing the badge count, package and activity names
     */
    private void insertBadgeAsync(final ContentValues contentValues) {
        mQueryHandler.startInsert(0, null, BADGE_CONTENT_URI, contentValues);
    }

    /**
     * Synchronously inserts the badge counter.
     *
     * @param context       Caller context
     * @param contentValues Content values containing the badge count, package and activity names
     */
    private void insertBadgeSync(final Context context, final ContentValues contentValues) {
        context.getApplicationContext().getContentResolver()
                .insert(BADGE_CONTENT_URI, contentValues);
    }

    /**
     * Creates a ContentValues object to be used in the badge counter update. The package and
     * activity names must correspond to an activity that holds an intent filter with action
     * "android.intent.action.MAIN" and category android.intent.category.LAUNCHER" in the manifest.
     * Also, it is not allowed to publish badges on behalf of another client, so the package and
     * activity names must belong to the process from which the insert is made.
     * To be able to insert badges, the app must have the PROVIDER_INSERT_BADGE
     * permission in the manifest file. In case these conditions are not
     * fulfilled, or any content values are missing, there will be an unhandled
     * exception on the background thread.
     *
     * @param badgeCount    the badge count
     * @param componentName the component name from which package and class name will be extracted
     *
     */
    private ContentValues createContentValues(final int badgeCount,
            final ComponentName componentName) {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(PROVIDER_COLUMNS_BADGE_COUNT, badgeCount);
        contentValues.put(PROVIDER_COLUMNS_PACKAGE_NAME, componentName.getPackageName());
        contentValues.put(PROVIDER_COLUMNS_ACTIVITY_NAME, componentName.getClassName());
        return contentValues;
    }

    /**
     * Check if the latest Sony badge content provider exists .
     *
     * @param context the context to use
     * @return true if Sony badge content provider exists, otherwise false.
     */
    private static boolean sonyBadgeContentProviderExists(Context context) {
        boolean exists = false;
        ProviderInfo info = context.getPackageManager().resolveContentProvider(SONY_HOME_PROVIDER_NAME, 0);
        if (info != null) {
            exists = true;
        }
        return exists;
    }
}