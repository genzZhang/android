package com.genzzhang.demo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.GridView;
import android.widget.Scroller;
import android.widget.Toast;

/**
 * Created by genzzhang on 2017/4/16.
 */

public class HorizontalLoopGridView extends GridView {
    public static final int sItemSize = 800;
    public static final int sCardSize = 960;

    public static final int sDuration = 3000;
    private Context mContext;
    private Scroller mScroller;
    private GestureDetector mGestureDetetor;
    private float mLastTouchX;
    private enum ScrollStatus {
        None,
        Scrolled,
        Left,
        Right
    };
    private ScrollStatus mScrollStatus = ScrollStatus.None;
    private int mPosition = 1;

    private Handler mHandler;
    private Runnable mRunnable;

    public HorizontalLoopGridView(Context context) {
        super(context);
        init(context);
    }

    public HorizontalLoopGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mScroller = new Scroller(context);
        mGestureDetetor = new GestureDetector(mContext, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                Log.e("mGestureDetetor", "onDown");
                mLastTouchX = motionEvent.getX();
                mScrollStatus = ScrollStatus.None;
                mHandler.removeCallbacks(mRunnable);
                return false;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {
                Log.e("mGestureDetetor", "onShowPress");
            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                Log.e("mGestureDetetor", "onSingleTapUp");
                OnItemClickListener clickListener = getOnItemClickListener();
                if (clickListener != null) {
                    int postion = mPosition;
                    if (motionEvent.getX() > sItemSize) {
                        postion++;
                    }
                    getOnItemClickListener().onItemClick(null, null, postion, 0);
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                Log.e("mGestureDetetor", "onScroll");
                float downX = motionEvent.getX();
                float curX = motionEvent1.getX();
                if (downX - curX > sItemSize * 0.3){
                    mScrollStatus = ScrollStatus.Left;
                } else if (curX - downX > sItemSize * 0.3) {
                    mScrollStatus = ScrollStatus.Right;
                } else if (curX != downX) {
                    mScrollStatus = ScrollStatus.Scrolled;
                } else {
                    mScrollStatus = ScrollStatus.None;
                }
                scrollBy((int)(mLastTouchX - curX), 0);
                mLastTouchX = curX;
                return true;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {
                Log.e("mGestureDetetor", "onLongPress");
            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                Log.e("mGestureDetetor", "onFling");
                //up了，但是这个函数有时候不执行
                return false;
            }
        });

        mHandler = new Handler(Looper.getMainLooper());
        mRunnable = new Runnable() {
            @Override
            public void run() {
                autoScrollNext();
                mHandler.postDelayed(mRunnable, sDuration);
            }
        };
        mHandler.postDelayed(mRunnable, sDuration);
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        } else {
            super.computeScroll();
        }
    }

    //向左滑动
    public void smoothScroll(int fromX, int destX) {
        mPosition = destX / sItemSize;
        int deltaX = destX - fromX;
        mScroller.startScroll(fromX, 0, deltaX, 0, Math.abs(deltaX * 2));
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //
        switch (ev.getAction()) {
            //onFling未必一定执行，也就是没有up了
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                autoScroll();
                mHandler.postDelayed(mRunnable, sDuration);
                break;
        }
        mGestureDetetor.onTouchEvent(ev);
        //这里强行true，onItemClick就不执行了
        return true;
    }

    /**
     * 循环  总元素个数n>3 实际元素至少2个
     * card显示2个item，起始都是一个item开始处  第一次滑动到1
     * item大块显示在1-2-...n-2中 滑动距离默认不超过1个item处理的
     * index:    0-1-2-3-...[n-2]-[n-1]
     * items:[n-2]-1-2-3-...[n-2]-1
     */
    private void autoScroll() {
        int offSet = getScrollX();
        int count = getModelsCount();
        int from = 0;
        int dest = 0;
        switch (mScrollStatus) {
            case Left:
                if (mPosition == count - 2) {
                    dest = sItemSize;
                    from = getScrollX() % sItemSize;
                } else {
                    dest = sItemSize * (mPosition + 1);
                    from = offSet;
                }
                break;

            case Right:
                if (mPosition == 1) {
                    dest = (count - 2) * sItemSize;
                    from = dest + sItemSize - offSet % sItemSize;
                } else {
                    dest = sItemSize * (mPosition - 1);
                    from = offSet;
                }
                break;

            case Scrolled:
            case None:
                dest = sItemSize * mPosition;
                from = offSet;
                break;
        }
        smoothScroll(from, dest);
    }

    //0-(1-2-3)-4 往右走
    private void autoScrollNext() {
        int from = 0;
        int to = 0;
        int position = mPosition + 1;
        int x = getScrollX();
        if (position < 1) {
            to = (getNumColumns() - 2) * sItemSize;
            from = to - x;
        } else if (position > getNumColumns() - 2) {
            to = sItemSize;
            //自动滑动中，有可能还没有划过去就重新一轮了,把速度调慢就可以看到了
            if (x + sItemSize < position * sItemSize) {
                from = 0;
            } else {
                from = getScrollX() % sItemSize;
            }
        } else {
            to = position * sItemSize;
            from = x;
        }
        smoothScroll(from, to);
    }

    private int getModelsCount() {
        return getAdapter().getCount();
    }

}
