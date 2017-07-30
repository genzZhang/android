package com.genzzhang.demo.horizontalloopview;

import com.genzzhang.demo.R;

/**
 * Created by genzzhang on 2017/4/16.
 */

public class HorizontalLoopModel {

    public String mTitle = "";
    public String mTip = "";
    public int mBgId;

    public HorizontalLoopModel(String title, String tip, int bgId) {
        mTitle = title;
        mTip = tip;
        mBgId = bgId;
    }

    public static HorizontalLoopModel getGameModel() {
        return  new HorizontalLoopModel("王者荣耀5v5真人对战", "游戏", R.drawable.bg_red_fresh);
    }

    public static HorizontalLoopModel getBikeModel() {
        return  new HorizontalLoopModel("摩拜单车陪你度过这个暖暖春日", "单车", R.drawable.bg_red_deep);
    }

    public static HorizontalLoopModel getPhoneModel(int postion) {
        return  new HorizontalLoopModel(postion + "小米6黑科技来了", "手机", R.drawable.bg_blue);
    }

    public static HorizontalLoopModel getBusinessModel() {
        return  new HorizontalLoopModel("网易考拉海购一起海淘", "电商", R.drawable.bg_gray);
    }

    public static HorizontalLoopModel getAppModel(int postion) {
        return  new HorizontalLoopModel(postion + "最右最好玩，再现网易神贴", "App", R.drawable.bg_green);
    }
}
