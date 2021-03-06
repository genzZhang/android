package com.genzzhang.demo.badges.alias;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.genzzhang.demo.app.DemoCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 启动activity alias管理类
 *
 * 尽量消除切换过程中点击图标提示该应用未安装。这个时间大概持续0.2秒。
 * 整个切换过程，需要10~40秒，一般最多在10--20秒之间，中间多尝试了几次。
 * 允许切换过程，多次替activity
 *
 * 切换图标，完成一次切换需要10秒。系统是每隔10秒，处理一次。
 * 当前的技术方案是先发射一个我认为的基准信号，等到9.5秒的真正去修改
 * 但每一次enable对于系统来说都可能是一次基准处理周期开始。所以还是有一定概率发生等10秒提示未安装
 * 所以在10秒内切换多次，上面的9.5秒就可以变为0。
 *
 * app更新，如果展示的图标比以往的少，会导致升级后最终一个图标都不显示需要重启launcher才显示正常
 * 所以，不要改变mainfest中的activity-alias为不显示，包括enable设置或者隐式隐藏
 * 一旦使用了alias中的，后续更新的时候，最好不删除，也不更改enable。目前来说，只要数目不减少就不会有问题
 * 如果要更新图标，直接替换icon就可以了。
 *
 * 源码：PackageManagerService
 */
public class LauncherAliasManager {

    private final static String TAG = "LauncherAliasManager";

    private static final String set_launcher_activity = "com.genzzhang.demo.MainActivity";
    private static final String default_alias_activity = "com.genzzhang.demo.DefaultAliasActivity";
    private static final String new_alias_activity = "com.genzzhang.demo.NewAliasActivity";

    private static final int SET_DELAY = 10000;//系统是10秒
    private static final int SET_INTERVAL = 200;//每再尝试一次就减少延后时间
    private static final int TRY_COUNT = 3;//最多尝试的次数，加上最开始试着处理的try_deal，一共是4次
    private int mSetDelay = SET_DELAY;
    private long mLastTime = 0L;
    private int mTryCount;

    private String mEnableAlias;
    private boolean mNeedDeal;
    private ArrayList<String> mAliasList = new ArrayList<String>();

    private Context mContext;
    private Handler mHandler;
    private static final int RECEIVE_REQUEST = 1;//收到请求
    private static final int SEND_REQUEST = 2;//在准确基准点的基础上，发送请求处理
    private static final int DEAL_REQUEST = 3;//处理
    private static final int TRY_DEAL = 4;//收到则马上试着

    private static class Holder {
        static LauncherAliasManager sInstance = new LauncherAliasManager();
    }
    public static LauncherAliasManager getInstance() {
        return Holder.sInstance;
    }
    private LauncherAliasManager() {
        HandlerThread thread = new HandlerThread("LauncherAliasManager");
        thread.start();
        mContext = DemoCache.getContext();
        mAliasList.add(default_alias_activity);
        mAliasList.add(new_alias_activity);
        mHandler = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    doHandleMessage(msg);
                } catch (Throwable throwable) {
                    Log.e(TAG, throwable.toString());
                }
            }
        };
        //注册组件状态变化监听
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "mMsgReceiver  MSG_PACKAGE_CHANGED");
                Bundle bundle = intent.getExtras();
                if (bundle != null && bundle.getBoolean(Intent.EXTRA_DONT_KILL_APP)) {
                    String nameList[] = bundle.getStringArray(Intent.EXTRA_CHANGED_COMPONENT_NAME_LIST);
                    if (nameList != null && nameList.length > 0) {
                        for (int i = 0; i < nameList.length; i++) {
                            Log.i(TAG, "mMsgReceiver 【activity】" + nameList[i]);
                        }
                    }
                    if (mHandler.hasMessages(TRY_DEAL)) {
                        mHandler.removeMessages(TRY_DEAL);
                    }
                    //可以作为准确基准点
                    if (!mHandler.hasMessages(SEND_REQUEST)) {
                        mHandler.sendEmptyMessage(SEND_REQUEST);
                    }
                }
            }
        }, filter);
    }

    private void doHandleMessage(Message msg) {
        switch (msg.what) {
            case RECEIVE_REQUEST: {
                if (!isAvailable()) {
                    Log.e(TAG, "该桌面不支持切换图标");
                    return;
                }
                mEnableAlias = (String) msg.obj;
                Log.i(TAG, "applyAlias receive request " + mEnableAlias);
                //重复设置图标
                Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
                if (intent != null && intent.getComponent() != null) {
                    String className = intent.getComponent().getClassName();
                    Log.i(TAG, "applyAlias current alias : " + className);
                    if (!TextUtils.isEmpty(className)) {
                        if (TextUtils.equals(mEnableAlias, className)) {
                            Log.i(TAG, "applyAlias receive same alias, direct return");
                            return;
                        }
                    }
                }
                //边寻找基准点边send，试着认为当前就可以认为是基准点
                mSetDelay = SET_DELAY;
                mTryCount = 0;
                mNeedDeal = true;
                setPackageManagerServiceEnable(mContext);
                mHandler.sendEmptyMessageDelayed(TRY_DEAL, mSetDelay);
                break;
            }
            case SEND_REQUEST: {
                //确认处理完了，就不再处理
                if (!mNeedDeal || TextUtils.isEmpty(mEnableAlias)) {
                    return;
                }
                //这个send会发很多次,系统处理的时候，会集中处理发一连串的广播过来
                if (mHandler.hasMessages(DEAL_REQUEST)) {
                    Log.i(TAG, "applyAlias cancel send request");
                    mHandler.removeMessages(DEAL_REQUEST);
                    //认为中间发送间隔超过6秒的时候，还没有处理完成，则是新的一轮开始。造成的原因是默认的10秒选择太久
                    if (System.currentTimeMillis() - mLastTime > SET_DELAY * 0.6) {
                        mTryCount++;
                        mSetDelay = SET_DELAY - mTryCount * SET_INTERVAL;
                        if (mTryCount >= TRY_COUNT) {
                            mEnableAlias = null;
                            mSetDelay = SET_DELAY;
                            mTryCount = 0;
                            Log.i(TAG, "applyAlias cancel deal for more than 3 times");
                            return;
                        }
                    } else {
                        //由于每次都是取消重新再来，延时也更新下
                        mSetDelay -= (System.currentTimeMillis() - mLastTime);
                        if (mSetDelay > SET_DELAY || mSetDelay < 0) {
                            mSetDelay = SET_DELAY;
                        }
                    }
                }
                mLastTime = System.currentTimeMillis();
                Log.i(TAG, "applyAlias send request");
                //获取到基准点后，开始设置新的基准点
                setPackageManagerServiceEnable(mContext);
                mHandler.sendEmptyMessageDelayed(DEAL_REQUEST, mSetDelay);
                break;
            }
            case TRY_DEAL:
            case DEAL_REQUEST: {
                mSetDelay = SET_DELAY;
                mTryCount = 0;
                if (mNeedDeal && !TextUtils.isEmpty(mEnableAlias)) {
                    Log.i(TAG, "applyAlias deal request start");
                    enableComponent(mContext, new ComponentName(mContext, mEnableAlias));
                    for (int i = 0; i < mAliasList.size(); i++) {
                        String tmp = mAliasList.get(i);
                        if (!TextUtils.equals(mEnableAlias, tmp)) {
                            disableComponent(mContext, new ComponentName(mContext, tmp));
                        }
                    }
                    Log.i(TAG, "applyAlias deal request end");mEnableAlias = null;
                    mNeedDeal = false;
                }
                break;
            }
        }
    }

    public void applyDefaultAlias(Context context) {
        Log.i(TAG, "apply【Default】Alias");
        Message message = mHandler.obtainMessage(RECEIVE_REQUEST, default_alias_activity);
        mHandler.sendMessage(message);
    }

    public void applyNewAlias(Context context) {
        Log.i(TAG, "apply【New】Alias");
        Message message = mHandler.obtainMessage(RECEIVE_REQUEST, new_alias_activity);
        mHandler.sendMessage(message);
    }

    private void setPackageManagerServiceEnable(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
        ComponentName setAlias = new ComponentName(context, set_launcher_activity);
        int status = pm.getComponentEnabledSetting(setAlias);
        status = status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED  ?
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        pm.setComponentEnabledSetting(setAlias, status, PackageManager.DONT_KILL_APP);
    }

    private void enableComponent(@NonNull Context context, ComponentName componentName) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void disableComponent(@NonNull Context context, ComponentName componentName) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private boolean isAvailable() {
        ArrayList<String> supportLaunchers = getSupportLaunchers();
        if (supportLaunchers == null || supportLaunchers.isEmpty()) {
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfos = mContext.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos == null || resolveInfos.activityInfo == null || TextUtils.isEmpty(resolveInfos.activityInfo.packageName)) {
            return false;
        }
        String currentHomePackage = resolveInfos.activityInfo.packageName;
        Log.e(TAG, "currentHomePackage name : " + currentHomePackage);
        boolean available = false;
        for (int i = 0; i < supportLaunchers.size(); i++) {
            if (TextUtils.equals(currentHomePackage, supportLaunchers.get(i))) {
                available = true;
                break;
            }
        }
        return available;
    }

    private ArrayList<String> getSupportLaunchers() {
        ArrayList<String> list = new ArrayList<String>();
        //华为
        list.add("com.huawei.android.launcher");
        //小米
        List<String> xiaomiList = Arrays.asList(
                "com.miui.miuilite",
                "com.miui.home",
                "com.miui.miuihome",
                "com.miui.miuihome2",
                "com.miui.mihome",
                "com.miui.mihome2",
                "com.i.miui.launcher");
        list.addAll(xiaomiList);
        //oppo
        list.add("com.oppo.launcher");
        //vivo
        list.add("com.vivo.launcher");
        //三星，切换图标会变化位置，添加到主屏幕的会消失，严重影响用户体验
        return list;
    }
}
