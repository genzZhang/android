package com.genzzhang.demo.util;

import com.genzzhang.demo.app.DemoCache;

/**
 * Created by Administrator on 2017/7/26.
 */

public class Tools {

    /**
     * dip转换成px值
     */
    public static int dip2px(float dipValue) {
        final float scale = DemoCache.getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * px转成dip
     */
    public static int px2dip(float pxValue) {
        final float scale = DemoCache.getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
