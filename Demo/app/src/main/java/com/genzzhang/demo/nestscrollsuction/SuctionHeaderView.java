package com.genzzhang.demo.nestscrollsuction;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.genzzhang.demo.R;
import com.genzzhang.demo.util.Tools;

import static android.graphics.Canvas.ALL_SAVE_FLAG;

/**
 *  头部view
 */

public class SuctionHeaderView extends RelativeLayout {

    private static final String TAG = "SuctionHeaderView";

    /** 白色遮罩矩形，可以根据滑动距离，等比例地调整高度，盖在体检视图的上面，
     * 从而营造出首页白色背景慢慢往上滑动的效果 **/
    private Paint mWhitePaint;
    private Rect mWhiteRect;
    private int mTitleHeight;//标题栏的高度

    /** 体检内容缩放比例，随着首页滑动缩放 **/
    private float mScaleFactor = 1.0f;
    private int mRectTop;
    private int mT;

    HeaderImplView mHeaderImplView;

    public SuctionHeaderView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context){
        mTitleHeight = getResources().getDimensionPixelSize(R.dimen.auction_title_bar_height);
        mWhitePaint = new Paint();
        mWhitePaint.setColor(getResources().getColor(R.color.content_view_bg));
        mWhitePaint.setAntiAlias(true);
        mWhitePaint.setStyle(Paint.Style.FILL);

        mHeaderImplView = new HeaderImplView(context);
        RelativeLayout.LayoutParams paramsHeader = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        paramsHeader.topMargin = mTitleHeight;
        paramsHeader.leftMargin = Tools.dip2px( 40);
        paramsHeader.rightMargin = Tools.dip2px(24);
        addView(mHeaderImplView, paramsHeader);
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b){
        super.onLayout(changed, l, t, r, b);
        /** 这里判断是否采用之前滑动时缓存的top数据，以解决首页滑到底部，然后锁屏，过一会亮屏而出现矩形绘制尺寸错误的bug */
        if(mRectTop == 0){
            mRectTop = b;
        }
        mT = t;
        mWhiteRect = new Rect(l, mRectTop, r, b);
    }

    @Override
    public void dispatchDraw(Canvas canvas){
        //再叠加白色遮罩盖住View的一部分
        canvas.drawRect(mWhiteRect, mWhitePaint);
        canvas.save();
        canvas.clipRect(0, 0, getWidth(), mRectTop);
        super.dispatchDraw(canvas);
        canvas.restore();
    }


    /**
     * 响应首页滑动事件，更新UI
     * @param scroll 总滑动距离
     */
    public void updateScroll(int scroll){
        int height = getLayoutParams().height - scroll;
        if(height < mTitleHeight){
            height = mTitleHeight;
        }else if(height > getLayoutParams().height){
            height = getLayoutParams().height;
        }
        if(height != mRectTop){
            int dismissHeight = getLayoutParams().height / 2;
            if(height > dismissHeight ) {
                mScaleFactor = (float) (height - dismissHeight) / (float) (getLayoutParams().height - dismissHeight);
            }else{
                mScaleFactor = 0;
            }
            //上推可移动的总高度，去除标题栏和小管提醒的高度
            int totalY = getLayoutParams().height - mTitleHeight
                    - getResources().getDimensionPixelSize(R.dimen.suction_reminder_height) / 2
                    - Tools.dip2px(3.33f);
            //调整白色遮罩尺寸
            int y = totalY * 1 / 2;
            if (scroll > y){
                mRectTop = getLayoutParams().height - y - (int)((scroll - y) * 1.7f);
            } else {
                mRectTop = height;
            }

            Log.i(TAG, "updateScroll mRectTop=" + mRectTop);
            //因为有个marginTop的存在
            mRectTop -= mT;

            if(mRectTop < mTitleHeight){
                mRectTop = mTitleHeight;
            }

            if(mWhiteRect != null) {
                mWhiteRect.top = mRectTop;
            }

            mHeaderImplView.invalidate();

            invalidate();
        }
    }

    public class HeaderImplView extends FrameLayout {

        LinearLayout mResultLayout;
        LinearLayout mScoreLayout;
        TextView resultTv;
        TextView scoreView;

        public HeaderImplView(final Context context) {
            super(context);

            mResultLayout = new LinearLayout(context);
            mResultLayout.setOrientation(LinearLayout.VERTICAL);
            FrameLayout.LayoutParams paramsResult = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsResult.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            paramsResult.topMargin = Tools.dip2px(6);
            addView(mResultLayout, paramsResult);
            //设置结果
            resultTv = new TextView(context);
            mResultLayout.addView(resultTv);
            //加上说明
            TextView resultTipTv = new TextView(context);
            resultTipTv.setPadding(0,Tools.dip2px(4),0,0);
            resultTipTv.setText("飞行结果说明");
            mResultLayout.addView(resultTipTv);

            //分数
            mScoreLayout = new LinearLayout(context);
            mScoreLayout.setOrientation(LinearLayout.HORIZONTAL);
            FrameLayout.LayoutParams paramsScoreLayout = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsScoreLayout.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
            addView(mScoreLayout, paramsScoreLayout);

            scoreView = new TextView(context);
            scoreView.setTextSize(Tools.dip2px(22));
            mScoreLayout.addView(scoreView);
            mScoreLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "飞行里数有提升", Toast.LENGTH_SHORT).show();
                }
            });

            TextView unit = new TextView(context);
            unit.setText("公里");
            LinearLayout.LayoutParams paramsUnit = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            paramsUnit.gravity = Gravity.BOTTOM;
            paramsUnit.leftMargin = Tools.dip2px( 7);
            paramsUnit.bottomMargin = Tools.dip2px( 7);
            mScoreLayout.addView(unit, paramsUnit);

        }

        @Override
        public void dispatchDraw(Canvas canvas){
            //确定缩放比例>0才需要去画体检的内容，避免过度绘制
            if(mScaleFactor > 0) {
                int alpha = (int)(mScaleFactor * 255);
                //支持滑动时做相应的缩放和改变aplha值
                canvas.saveLayerAlpha(0, 0, getWidth(), getHeight(), alpha, ALL_SAVE_FLAG);
                super.dispatchDraw(canvas);
                canvas.restore();
            }
        }

        public void updateScore(int lastScore, int currScore) {
            String tip = "飞常准，安全又快速";
            resultTv.setText(tip);
            scoreView.setText(currScore + "");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                SuctionHeaderView.this.setAlpha(1);
                AlphaAnimation headerAlphaAnimation = new AlphaAnimation(0, 1);
                headerAlphaAnimation.setDuration(700);
                SuctionHeaderView.this.startAnimation(headerAlphaAnimation);
            }
        }

        private int getPercent(int initScore, int currScore) {
            int percent = (int)(Math.floor(((currScore - initScore) / (double)initScore) * 100));
            if (percent <= 0) {
                percent = 1;
            }
            return percent;
        }

    }

}
