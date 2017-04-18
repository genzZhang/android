package com.genzzhang.demo;

/**
 * Created by genzzhang on 2017/4/16.
 */

public class HorizontalLoopGridModel {

    public String mTitle = "";
    public String mTip = "";
    public int mBgId;

    public HorizontalLoopGridModel(String title, String tip, int bgId) {
        mTitle = title;
        mTip = tip;
        mBgId = bgId;
    }

    public static HorizontalLoopGridModel getGameModel() {
        return  new HorizontalLoopGridModel("王者荣耀5v5真人对战", "游戏", R.drawable.bg_red_fresh);
    }

    public static HorizontalLoopGridModel getBikeModel() {
        return  new HorizontalLoopGridModel("摩拜单车陪你度过这个暖暖春日", "单车", R.drawable.bg_red_deep);
    }

    public static HorizontalLoopGridModel getPhoneModel(int postion) {
        return  new HorizontalLoopGridModel(postion + "小米6黑科技来了", "手机", R.drawable.bg_blue);
    }

    public static HorizontalLoopGridModel getBusinessModel() {
        return  new HorizontalLoopGridModel("网易考拉海购一起海淘", "电商", R.drawable.bg_gray);
    }

    public static HorizontalLoopGridModel getAppModel(int postion) {
        return  new HorizontalLoopGridModel(postion + "最右最好玩，再现网易神贴", "App", R.drawable.bg_green);
    }
}
