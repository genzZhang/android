package com.genzzhang.demo.listviewanimation;

import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.HashMap;
import java.util.List;

/**
 * https://github.com/yjwfn/SlideItem/blob/master/app/src/main/java/com/lw/widget/slideitem/ItemSlideHelper.java#L368
 * Created by genzzhang on 2016/11/10.
 * RecycleView 完全可以替换
 */

public class AniHelper {

    private static final String TAG = "AniHelper";
    private static final int TIME = 400;  //各自使用time时间，一共*2
   
    //改变自启状态时的从左往右消失的删除动画
    public static void animateSwipe(final List<String> mMessageItems, final BaseAdapter mAdapter, final ListView mListView, final View view) {
        Log.i(TAG, "animateSwipe");
        if (view == null) {
            return;
        }

		final int position = mListView.getPositionForView(view);
		if (position == AdapterView.INVALID_POSITION) {
			mAdapter.notifyDataSetChanged();
			return;
		}
 
        TranslateAnimation swipeAnim = new TranslateAnimation(0.0f, mListView.getWidth(), 0, 0);
        float currentAlpha = 1.0f;
        AlphaAnimation alphaAnim = new AlphaAnimation(currentAlpha, 0);
        AnimationSet set = new AnimationSet(true);
        DecelerateInterpolator di = new DecelerateInterpolator();
        set.addAnimation(swipeAnim);
        set.addAnimation(alphaAnim);
        set.setInterpolator(di);
        set.setDuration(TIME);
        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            	mListView.setEnabled(false);
                mListView.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            	mListView.setEnabled(true);
            	mListView.setClickable(true);
                animateOtherViews(mMessageItems, mListView, mAdapter, view);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(set);
        Log.i(TAG, "animateSwipe  startAnimation");
    }

    // 改变自启状态时，未改变状态的其他Item向上的动画
    private static void animateOtherViews(final List<String> mMessageItems, final ListView mListView, final BaseAdapter adapter, View viewToRemove) {
        //record showed item's getTop()
		final HashMap<String, Integer> itemTopMap = new HashMap<String, Integer>();
		
    	int firstVisiblePosition = mListView.getFirstVisiblePosition();
		for (int i = 0; i < mListView.getChildCount(); i++) {
        	View child = mListView.getChildAt(i);
        	int position = firstVisiblePosition + i;
        	if (position == AdapterView.INVALID_POSITION) {
        		break;
        	}
        	if (child != viewToRemove) {
        		//注意下 INVALID_POSITION
        		itemTopMap.put(mMessageItems.get(mListView.getPositionForView(child)), child.getTop());
        	}
        }
    	
        //remove the ViewtoRemoved,then notifyDataSetChanged
        int position = AdapterView.INVALID_POSITION;
        position = mListView.getPositionForView(viewToRemove);
        if (position == AdapterView.INVALID_POSITION) {
        	adapter.notifyDataSetChanged();
        	return;
        }
        mMessageItems.remove(position);
        adapter.notifyDataSetChanged();
        
        //after layout runs, compare to pre-layout, then animate changes
        final ViewTreeObserver observer = mListView.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			
			@Override
			public boolean onPreDraw() {
				// TODO Auto-generated method stub
				observer.removeOnPreDrawListener(this);
				Log.i(TAG, "onPreDraw");
				int firstVisiblePosition = mListView.getFirstVisiblePosition();
				//preTop == null means the new child add to screen, attention coming from bottom or top;
				int newViewCount = 0;
				//animate one by one
				for (int i = 0; i < mListView.getChildCount(); i++) {
					final View child = mListView.getChildAt(i);
					int position = firstVisiblePosition + i;
					Integer preTop = itemTopMap.get(mMessageItems.get(position));
					int top = child.getTop();
					if (preTop == null) {
						Log.i(TAG, newViewCount + " "+ i);
						int childHeight = child.getHeight() + mListView.getDividerHeight();
						preTop = top + (i != newViewCount++ ? childHeight : -childHeight);
					}
					int delta = preTop - top;
                    // if delta == 0, we don't moveView, the view maybe shimmer
                    moveView(mListView, child, 0, 0, delta, 0);
				}
				Log.i(TAG, "onPreDraw finished");
				return true;
			}
		});

    }

    //把某个view向上移动的动画
    private static void moveView(final ListView mListView, View view, float startX, float endX, float startY, float endY) {
        TranslateAnimation translator = new TranslateAnimation(startX, endX, startY, endY);
        translator.setDuration(TIME);
        translator.setInterpolator(new DecelerateInterpolator());
        view.startAnimation(translator);
        view.getAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            	mListView.setEnabled(false);
            	mListView.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            	mListView.setEnabled(true);
            	mListView.setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            	
            }
        });
    }

}
