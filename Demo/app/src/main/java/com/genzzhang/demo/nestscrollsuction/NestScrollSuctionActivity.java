package com.genzzhang.demo.nestscrollsuction;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.genzzhang.demo.R;
import com.genzzhang.demo.app.AbsActivity;
import com.genzzhang.demo.util.Tools;

public class NestScrollSuctionActivity extends AbsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout layout = new FrameLayout(mContext);
        layout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(layout);

        //吸顶层
        final SuctionLayout suctionLayout = new SuctionLayout(mContext);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.addView(suctionLayout, lp);
        suctionLayout.mHeaderView.mHeaderImplView.updateScore(78, 90);
        //标题
        TextView title = new TextView(mContext);
        title.setText("吸顶标题");
        int titleHeight = getResources().getDimensionPixelSize(R.dimen.auction_title_bar_height);
        lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, titleHeight);
        layout.addView(title, lp);
        //嵌套滑动
        NestScrollLayout nestScrollLayout = new NestScrollLayout(mContext);
        nestScrollLayout.setBackgroundColor(Color.TRANSPARENT);
        lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.topMargin = titleHeight;
        lp.leftMargin = Tools.dip2px(13);
        lp.rightMargin = lp.leftMargin;
        layout.addView(nestScrollLayout, lp);
        nestScrollLayout.setOnScrollListener(new NestScrollLayout.OnScrollListener() {
            @Override
            public void onLayoutScroll(int currentY, int maxY) {
                suctionLayout.updateScroll(currentY);
            }

            @Override
            public void onNestedScroll(boolean isUp) {

            }

            @Override
            public void onSuctionUp() {

            }

            @Override
            public void onTouch(boolean start) {

            }
        });

        //业务
        LinearLayout listHeader = new LinearLayout(mContext);
        listHeader.setOrientation(LinearLayout.VERTICAL);
        nestScrollLayout.addView(listHeader);
        //add listHeader-proxyview
        final View proxyView = new View(mContext);
        proxyView.setBackgroundColor(Color.TRANSPARENT);
        proxyView.setFocusable(true);
        proxyView.setFocusableInTouchMode(true);
        listHeader.addView(proxyView);
        //代理点击事件
        boolean hasSetDelegate = true;
        int headerWidth = 1;
        if (hasSetDelegate) {
            headerWidth = ViewGroup.LayoutParams.MATCH_PARENT;
            suctionLayout.scrollableLayout = nestScrollLayout;
            nestScrollLayout.setHeaderTouchDelegate(proxyView, suctionLayout);
        }
        final int headerHeight = getResources().getDimensionPixelSize(R.dimen.suction_header_height)
                - titleHeight
                - getResources().getDimensionPixelSize(R.dimen.suction_reminder_height) / 2
                + Tools.dip2px( 3.33f);
        proxyView.setLayoutParams(new LinearLayout.LayoutParams(headerWidth, headerHeight));
        final int hideHeight = headerHeight;
        nestScrollLayout.setSuctionUpHeight(hideHeight);
        //加个卡片
        TextView tip1 = new TextView(mContext);
        tip1.setText("卡片1");
        tip1.setGravity(Gravity.CENTER_VERTICAL);
        tip1.setBackgroundDrawable(getResources().getDrawable(R.drawable.common_bg));
        listHeader.addView(tip1);
        TextView tip2 = new TextView(mContext);
        tip2.setGravity(Gravity.CENTER_VERTICAL);
        tip2.setText("卡片2");
        tip2.setBackgroundDrawable(getResources().getDrawable(R.drawable.common_bg));
        listHeader.addView(tip2);

        //如果listview为空，可以多移动
        nestScrollLayout.setNoListMoreScrollY(Tools.dip2px( 80));
        //加载listview
        String[] mDescri = new String[20];
        for (int i = 0; i < mDescri.length; i++) {
            mDescri[i] = i + 1 + "测试ListView";
        }
        //ListView不要设置top或bottom的padding或者.9图带来的paddying。未做pad的适配 同样禁止listview划过头OVER_SCROLL_NEVER
        Drawable transparent = new ColorDrawable(Color.TRANSPARENT);
        final RefreshListView listView = new RefreshListView(mContext);
        listView.setBackgroundColor(getResources().getColor(android.R.color.white));
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.setDivider(transparent);
        listView.setSelector(transparent);
        listView.setVerticalScrollBarEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            listView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
        }

        final Handler handler = new Handler(Looper.getMainLooper());
        listView.setOnRefreshListener(new RefreshListView.OnRefreshListener() {

            @Override
            public void onRefreshFromStart() {
                //顶部刷新结束后，模拟1秒钟拿到数据
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listView.onRefreshComplete(0, 6);
                    }
                }, 1000);
            }

            @Override
            public void onRefreshFromEnd() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listView.onRefreshComplete(0, 6);
                    }
                }, 1000);
            }
        });

        listView.setAdapter(new ArrayAdapter<String>(NestScrollSuctionActivity.this,
                android.R.layout.simple_list_item_1, mDescri));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(mContext, "click " + (position + 1), Toast.LENGTH_SHORT).show();
            }
        });
        //设置滑动
        nestScrollLayout.addView(listView);
        nestScrollLayout.getHelper().setScrollableView(listView);
    }

}
