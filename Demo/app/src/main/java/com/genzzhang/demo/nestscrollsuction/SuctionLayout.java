package com.genzzhang.demo.nestscrollsuction;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.genzzhang.demo.R;

/**
 *  吸顶布局
 */
public class SuctionLayout extends FrameLayout {

	private final static String TAG = "SuctionLayout";

	private Context mContext;

	int height;
	NestScrollLayout scrollableLayout;

	SuctionHeaderView mHeaderView;
	SuctionHeaderBg mHeaderBg;

    public SuctionLayout(Context context) {
        super(context);
        mContext = context;
		height = getResources().getDimensionPixelSize(R.dimen.auction_title_bar_height);
		init();
    }

    public void updateScroll(int scroll) {
		mHeaderBg.updateScroll(scroll);
		mHeaderView.updateScroll(scroll);
    }

	private void init() {
		int headerHeight = getResources().getDimensionPixelSize(R.dimen.suction_header_height);
		//头部背景
		mHeaderBg = new SuctionHeaderBg(mContext);
		FrameLayout.LayoutParams lpHeader = new FrameLayout.LayoutParams(-1, headerHeight);
		addView(mHeaderBg, lpHeader);
		//HeaderView头部数据
		mHeaderView = new SuctionHeaderView(mContext);
		lpHeader = new FrameLayout.LayoutParams(-1, headerHeight);
		addView(mHeaderView, lpHeader);
		//头部余下白色
		View mEmptyView = new View(mContext);
		mEmptyView.setBackgroundColor(getResources().getColor(R.color.content_view_bg));
		FrameLayout.LayoutParams lpEmpty = new FrameLayout.LayoutParams(-1, -1);
		lpEmpty.topMargin = headerHeight;
		addView(mEmptyView, lpEmpty);
	}

	/**
	 * 拦截掉，不要让下面的view有任何响应
	 * @param ev
	 * @return
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (scrollableLayout != null && scrollableLayout.isHeaderTouched()) {
			ev.setLocation(scrollableLayout.mEventTouchX, scrollableLayout.mEventTouchY + height);
			super.dispatchTouchEvent(ev);
			//是因为 TouchDelegate.mDelegateTargeted，但由于我的代理区域和实际的一致的，所以这里可要可不要
			if (ev.getAction() == MotionEvent.ACTION_UP) {
				final long now = SystemClock.uptimeMillis();
				MotionEvent event = MotionEvent.obtain(now, now,
						MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
					event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
				}
				super.dispatchTouchEvent(event);
				event.recycle();
			}
		}
		return true;
	}

	private class SuctionHeaderBg extends LinearLayout {

		public ImageView img;
		private int scrollY;
		private int maxScrollY;

		public SuctionHeaderBg(Context context) {
			super(context);

			img = new ImageView(context);
			img.setBackgroundDrawable(getResources().getDrawable(R.drawable.suction_header_bg));
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			addView(img, lp);
			maxScrollY = getResources().getDimensionPixelSize(R.dimen.suction_header_bg_max_scroll_y);
		}


		/*
		@Override
		protected void dispatchDraw(Canvas canvas) {
			canvas.save();
			//裁剪后小米5不会展示
			//canvas.clipRect(0, getHeight() - scrollY, getWidth(), 0);
			canvas.translate(0, -scrollY);
			super.dispatchDraw(canvas);
			canvas.restore();
		}
        */

		public void updateScroll(int scroll) {
			if (scroll < 0) {
				scrollY = 0;
			} else if (scroll > maxScrollY){
				scrollY = maxScrollY;
			} else {
				scrollY = scroll;
			}
			//直接滑动，不用canvas滑动
			scrollTo(0, scrollY);
			//mHeaderBg.invalidate();
		}

	}

}
