package com.genzzhang.demo.app;

import android.app.Application;

/**
 * Created by Administrator on 2016/7/29.
 */
public class DemoApplication extends Application {

    private static DemoApplication mInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        DemoCache.setContext(this);
    }

    public static DemoApplication getInstance() {
        return mInstance;
    }
}
