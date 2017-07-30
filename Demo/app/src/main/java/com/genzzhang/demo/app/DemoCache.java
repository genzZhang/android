package com.genzzhang.demo.app;

import android.content.Context;

/**
 * Created by genzZHANG on 2015/7/13.
 */
public class DemoCache {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        DemoCache.context = context.getApplicationContext();
    }

}

