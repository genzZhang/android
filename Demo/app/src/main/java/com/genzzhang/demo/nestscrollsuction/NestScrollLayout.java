package com.genzzhang.demo.nestscrollsuction;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by cpoopc(303727604@qq.com) on 2015-02-10.
 * 借鉴思路https://github.com/w446108264/ScrollableLayout
 * 1.头部滑动，吸顶效果
 * 2.嵌套滑动，解决Scroll不宜嵌套ListView的痛点
 * 3.建议使用官方NestedScrollView控件或者结合其原理改写
 * 4.重写的目的在于，有些app控制包大小会裁剪掉v4/v7
 */
public class NestScrollLayout extends LinearLayout {

    private static final String TAG = "NestScrollLayout";

    /** mProxyViewHeight收起来时的滑动高度 **/
    private int mSuctionUpHeight = 0;

    private boolean mIsAnimation = false;
    private long mStartTime;
    private int mStartScroll;
    private int mFinalScroll;
    private static final int ANIMATION_DURATION_MIN = 200;
    private static final int ANIMATION_DURATION_MAX = 400;
    private int mAnimationDuration;
    private DecelerateInterpolator mDecelerateInterpolator;
    private boolean mHasTouchDownProxy = false;
    //统计向上吸附
    private boolean mCanReportAni = false;

    //记录是否：没有拦截嵌套move
    private boolean mHasNotInterceptNestMove;

    private Scroller mScroller;
    private float mDownX;
    private float mDownY;
    private float mLastY;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;

    private boolean mHeaderClickEnable;
    private boolean mIsHeaderTouching;

    private boolean mIsBeingDragged;

    private DIRECTION mDirection;
    private int sysVersion;
    private int mLastScrollerY;

    private int minY = 0;
    private int maxY = 0;

    private int mNoListMoreScrollY = 0;

    private boolean isClickHead;

    enum DIRECTION {
        UP,
        DOWN
    }

    /**
     * 不包括嵌套listview滑动的监听
     * 那个自行就可以监听，这里并未做处理
     */
    public interface OnScrollListener {
        void onLayoutScroll(int currentY, int maxY);
        void onNestedScroll(boolean isUp);
        void onSuctionUp();
        void onTouch(boolean start);
    }

    private OnScrollListener onScrollListener;

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    private NestScrollHelper mHelper;

    public NestScrollHelper getHelper() {
        return mHelper;
    }

    public NestScrollLayout(Context context) { this(context, null); }

    public NestScrollLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public NestScrollLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHelper = new NestScrollHelper(context);
        mScroller = new Scroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        sysVersion = Build.VERSION.SDK_INT;
        setOrientation(VERTICAL);
        mDecelerateInterpolator = new DecelerateInterpolator(1.5f);
        //禁止多点触控
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setMotionEventSplittingEnabled(false);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getChildCount() > 2) {
            throw new IllegalStateException("ScrollableLayout can host only two direct child");
        }

        int totalHeight = MeasureSpec.getSize(heightMeasureSpec);
        int firstHeight = 0;
        int secondHeight = 0;
        //计算整体滚动的最大高度 maxY
        if (getChildCount() > 0) {
            View firstView = getChildAt(0);
            measureChildWithMargins(firstView, widthMeasureSpec, 0, MeasureSpec.UNSPECIFIED, 0);
            firstHeight = firstView.getMeasuredHeight();
        }
        if (getChildCount() > 1) {
            View secondView = getChildAt(1);
            measureChildWithMargins(secondView, widthMeasureSpec, 0, MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.AT_MOST), 0);
            secondHeight = secondView.getMeasuredHeight();
            //多快区域滑动协调起来，逻辑写的复杂，当前就允许能有两块。
            if (!getHelper().isEmpty()) {
                mNoListMoreScrollY = 0;
            }
        }

        maxY = firstHeight + secondHeight + mNoListMoreScrollY - totalHeight;
        if (maxY < 0) {
            maxY = 0;
        }

        setMeasuredDimension(widthMeasureSpec, MeasureSpec.makeMeasureSpec(firstHeight + secondHeight + mNoListMoreScrollY, MeasureSpec.EXACTLY));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int touchAction = ev.getAction() & MotionEvent.ACTION_MASK;
        if (onScrollListener != null) {
            if (touchAction == MotionEvent.ACTION_DOWN) {
                onScrollListener.onTouch(true);
            } else if (touchAction == MotionEvent.ACTION_UP ||
                    touchAction == MotionEvent.ACTION_CANCEL) {
                onScrollListener.onTouch(false);
            }
        }

        if (touchAction == MotionEvent.ACTION_DOWN) {
            if (ev.getY() <= mSuctionUpHeight - mLastScrollerY) {
                mHasTouchDownProxy = true;
            } else {
                mHasTouchDownProxy = false;
            }
        }
        if (!mHeaderClickEnable && mHasTouchDownProxy) {
            return false;
        }

        float currentX = ev.getX();
        float currentY = ev.getY();
        float deltaY;
        int shiftX;
        int shiftY;
        shiftX = (int) Math.abs(currentX - mDownX);
        shiftY = (int) Math.abs(currentY - mDownY);
        boolean isDistanceUp = currentY - mDownY <= 0;
        switch (touchAction) {
            case MotionEvent.ACTION_DOWN:
                mCanReportAni = mLastScrollerY == 0;
                mIsBeingDragged = false;
                mDownX = currentX;
                mDownY = currentY;
                mLastY = currentY;
                checkIsClickHead((int) currentY, mSuctionUpHeight, getScrollY());
                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                mScroller.forceFinished(true);
                if (mIsAnimation) {
                    mIsAnimation = false;
                    scrollTo(0, mFinalScroll);
                }
                if (mHeaderClickEnable && isClickHead) {
                    onTouchEvent(ev);
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mHasNotInterceptNestMove = true;
                initVelocityTrackerIfNotExists();
                mVelocityTracker.addMovement(ev);
                deltaY = mLastY - currentY;
                //去掉 && shiftY > shiftX
                if (!mIsBeingDragged && shiftY > mTouchSlop && shiftY > shiftX) {
                    mIsBeingDragged = true;
                }
                mLastY = currentY;
                //如果下拉到顶了，就不要拦截了
                if (!(deltaY >= 0) && isCanPullToRefresh()) {
                    Log.i(TAG, "dispatchTouchEvent ACTION_MOVE !isUp && isCanPullToRefresh()");
                    break;
                }

                Log.i(TAG, "dispatchTouchEvent ACTION_MOVE");
                boolean needInterceptor = false;
                if (mIsBeingDragged && (!isSticked((deltaY >= 0)))) {
                    //去掉 && shiftY > shiftX
                    if (shiftY > mTouchSlop && shiftY > shiftX) {
                        scrollBy(0, (int) (deltaY + 0.5));
                        Log.i(TAG, "dispatchTouchEvent ACTION_MOVE scrollBy");
                    }
                    needInterceptor = true;
                }
                if (needInterceptor) {
                    mHasNotInterceptNestMove = false;
                    Log.i(TAG, "dispatchTouchEvent ACTION_MOVE dd");
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                boolean isDrag = mIsBeingDragged;
                mIsBeingDragged = false;
                initVelocityTrackerIfNotExists();
                mVelocityTracker.addMovement(ev);
                //如果下拉到顶了，就不要拦截了
                if (!isDistanceUp && isCanPullToRefresh()) {
                    break;
                }
                if (isDrag) {
                    boolean showAnimator = false;
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    float yVelocity = -mVelocityTracker.getYVelocity();

                    mDirection = yVelocity > 0 ? DIRECTION.UP : DIRECTION.DOWN;

                    //从整体滑动切换到嵌套滑动的时候，如果嵌套没有接受都move，接着来的up事件会让他进行点击，造成滑动后点击的效果
                    if (Math.abs(yVelocity) > mMinimumVelocity) {
                        Log.i(TAG, "dispatchTouchEvent ACTION_UP yVelocity="+yVelocity);
                        if (isSticked(mDirection == DIRECTION.UP)) {
                            if (!mHasNotInterceptNestMove) {
                                ev.setAction(MotionEvent.ACTION_CANCEL);
                                super.dispatchTouchEvent(ev);
                                return true;
                            }
                            Log.i(TAG, "dispatchTouchEvent ACTION_UP isSticked");
                            if (onScrollListener != null) {
                                onScrollListener.onNestedScroll(mDirection == DIRECTION.UP);
                            }
                            break;
                        }
                    }

                    //计算距离
                    int velocityDistance = mHelper.getEstimatedDistance((int)yVelocity);
                    Log.i(TAG, "dispatchTouchEvent velocityDistance="+velocityDistance);
                    //处理吸附效果
                    int finalY = velocityDistance + mLastScrollerY;
                    if ((finalY > 0 && finalY < mSuctionUpHeight)) {
                        if(isDistanceUp){
                            Log.i(TAG, "dispatchTouchEvent ACTION_UP handlerInTouchUp go to up");
                            finalY = mSuctionUpHeight;
                        }else {
                            Log.i(TAG, "dispatchTouchEvent ACTION_UP handlerInTouchUp go to bottom");
                            finalY = 0;
                        }
                        showAnimator = true;
                        doAnimationScroll(finalY);
                    }
                    if (mCanReportAni && isDistanceUp && finalY > 0) {
                        if (onScrollListener != null) {
                            onScrollListener.onSuctionUp();
                        }
                        Log.i(TAG, "dispatchTouchEvent ACTION_UP ReportUtil up");
                    }
                    if (!showAnimator && velocityDistance != 0) {
                        mScroller.fling(0, getScrollY(), 0, (int) yVelocity, 0, 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
                        Log.i(TAG, "dispatchTouchEvent ACTION_UP fling=" + (mScroller.getFinalY() - mScroller.getStartY()));
                        mIsAnimation = false;
                        invalidate();
                    }

                    if (isClickHead || !isSticked(mDirection == DIRECTION.UP) || velocityDistance == 0) {
                        int action = ev.getAction();
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        boolean dd = super.dispatchTouchEvent(ev);
                        ev.setAction(action);
                        Log.i(TAG, "dispatchTouchEvent ACTION_UP dd="+dd);
                        return dd;
                    }
                } else {
                    Log.i(TAG, "dispatchTouchEvent ACTION_UP flag2");
                    if (mHeaderClickEnable && isClickHead) {
                        onTouchEvent(ev);
                        return true;
                    }
                }
                break;
            //关屏未抬手，直接回调cancel，暂时不特殊处理
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                if (isClickHead && (shiftX > mTouchSlop || shiftY > mTouchSlop)) {
                    int action = ev.getAction();
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    boolean dd = super.dispatchTouchEvent(ev);
                    ev.setAction(action);
                    Log.i(TAG, "dispatchTouchEvent ACTION_CANCEL dd="+dd);
                    return dd;
                } else {
                    Log.i(TAG, "dispatchTouchEvent ACTION_CANCEL xx");
                    if (mHeaderClickEnable && isClickHead) {
                        onTouchEvent(ev);
                        return true;
                    }
                }
            default:
                break;
        }
        super.dispatchTouchEvent(ev);
        return true;
    }

    /**
     * 这里方便把触摸数据代理出去
     * move直接拦截了
     */
    public float mEventTouchX;
    public float mEventTouchY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mEventTouchX = event.getX();
        mEventTouchY = event.getY();
        int touchAction = event.getAction() & MotionEvent.ACTION_MASK;
        if (touchAction == MotionEvent.ACTION_DOWN) {
            mIsHeaderTouching = true;
        }
        super.onTouchEvent(event);
        if (touchAction == MotionEvent.ACTION_UP ||
                touchAction == MotionEvent.ACTION_CANCEL) {
            mIsHeaderTouching = false;
        }
        return true;
    }

    /**
     * 将headerProxyView点击事件交给delegateView处理
     * 主要要根据mEventTouch坐标进行转换
     * MotionEvent.setLocation()
     * @param headerProxyView
     * @param delegateView
     */
    public void setHeaderTouchDelegate(final View headerProxyView, final View delegateView) {
        NestScrollLayout.this.getViewTreeObserver().removeOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Rect rect = new Rect();
                headerProxyView.getHitRect(rect);
                TouchDelegate delegate = new TouchDelegate(rect, delegateView);
                NestScrollLayout.this.setTouchDelegate(delegate);
                mHeaderClickEnable = true;
                return true;
            }
        });
    }

    /**
     * 是否是头部可以响应点击
     * @return
     */
    public boolean isHeaderTouched() {
        return mHeaderClickEnable && isClickHead && mIsHeaderTouching;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private int getScrollerVelocity(int distance, int duration) {
        if (mScroller == null) {
            return 0;
        } else if (sysVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return (int) mScroller.getCurrVelocity();
        } else {
            return distance / duration;
        }
    }

    @Override
    public void computeScroll() {
        Log.i(TAG, "computeScroll computeScrollOffset="+mScroller.computeScrollOffset());
        if (mScroller.computeScrollOffset()) {
            //这里处理fling的
            final int currY = mScroller.getCurrY();
            if (mDirection == DIRECTION.UP) {
                Log.i(TAG, "computeScroll DIRECTION.UP");
                if (isSticked(true)) {
                    Log.i(TAG, "computeScroll isSticked");
                    int distance = mScroller.getFinalY() - currY;
                    int duration = calcDuration(mScroller.getDuration(), mScroller.timePassed());
                    mHelper.smoothScrollBy(getScrollerVelocity(distance, duration), distance, duration);
                    mScroller.forceFinished(true);
                    if (onScrollListener != null) {
                        onScrollListener.onNestedScroll(true);
                    }
                    return;
                } else {
                    scrollTo(0, currY);
                }
            } else {
                Log.i(TAG, "computeScroll DIRECTION.DOWN");
                if (mHelper.isTop()) {
                    Log.i(TAG, "computeScroll isTop");
                    int deltaY = (currY - mLastScrollerY);
                    int toY = getScrollY() + deltaY;
                    scrollTo(0, toY);
                    if (mLastScrollerY <= minY) {
                        mScroller.forceFinished(true);
                        return;
                    }
                }
            }
            invalidate();
        }
    }

    @Override
    public void scrollBy(int x, int y) {
        int scrollY = getScrollY();
        int toY = scrollY + y;
        if (toY >= maxY) {
            toY = maxY;
        } else if (toY <= minY) {
            toY = minY;
        }
        y = toY - scrollY;
        super.scrollBy(x, y);
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y >= maxY) {
            y = maxY;
        } else if (y <= minY) {
            y = minY;
        }

        mLastScrollerY = y;
        if (onScrollListener != null) {
            onScrollListener.onLayoutScroll(y, maxY);
        }
        super.scrollTo(x, y);
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void checkIsClickHead(int downY, int headHeight, int scrollY) {
        isClickHead = downY + scrollY <= headHeight;
    }

    private int calcDuration(int duration, int timepass) {
        return duration - timepass;
    }


    private void doAnimationScroll(int finalScroll) {
        if (finalScroll == mLastScrollerY) { // 如果根本没有移动，直接忽略动画请求
            return;
        }
        mIsAnimation = true;
        mStartTime = System.currentTimeMillis();
        mStartScroll = mLastScrollerY;
        mFinalScroll = finalScroll;
        int count = 0;
        if(maxY > 0) {
            count = Math.abs(mFinalScroll - mStartScroll) * 100 / maxY;
        }
        mAnimationDuration = Math.min(ANIMATION_DURATION_MAX, ANIMATION_DURATION_MIN + count * 3);
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mIsAnimation) {
            float radio = 1.0f * (System.currentTimeMillis() - mStartTime) / mAnimationDuration;
            float interpolationRatio = radio;
            if (radio < 0) {
                radio = 0;
            } else if (radio > 1) {
                radio = 1;
            }
            interpolationRatio = mDecelerateInterpolator.getInterpolation(radio);
            int scroll = (int) (mStartScroll + (mFinalScroll - mStartScroll) * interpolationRatio);
            scrollTo(0, scroll);
            mLastScrollerY = scroll;
            if (radio == 1) {
                mIsAnimation = false;
            }
            invalidate();
        }

        super.dispatchDraw(canvas);
    }

    /**
     * 优先整体滑动，尽量使用！isSticked()来滑动整体先
     * 由于只有两层滑动，up && mHelper.isBottom()中只要up就够啦，暂时先加上
     * ListView不要设置top或bottom的padding或者.9图带来的paddying。未做pad的适配
     * @return 是否嵌套滑动
     */
    private boolean isSticked(boolean up) {
        Log.i(TAG, "mLastScrollerY=" + mLastScrollerY + "|maxY=" + mLastScrollerY + "|up=" + up);
        return mLastScrollerY == maxY
                && ((up && !mHelper.isBottom()) || (!up && mHelper.getFirstChildTop() < 0));
    }

    //是否滑动到顶了
    private boolean isCanPullToRefresh() {
        if (getScrollY() <= 0 && mHelper.isTop()) {
            return true;
        }
        return false;
    }

    /**
     * 设置吸顶高度
     * @param suctionUpHeight
     */
    public void setSuctionUpHeight(int suctionUpHeight) {
        mSuctionUpHeight = suctionUpHeight;
    }

    /**
     * 在没有listview的情况下，可以多滑动一点的距离
     * @param y
     */
    public void setNoListMoreScrollY(int y) {
        mNoListMoreScrollY = y;
    }

    /**
     * 自动吸顶
     * @param up
     */
    public void startSuction(boolean up) {
        mScroller.forceFinished(true);
        doAnimationScroll(!up ? 0 : mSuctionUpHeight);
    }

    /**
     * 是否已经展开
     * @return
     */
    public boolean isExpanded() {
        return getScrollY() <= 0;
    }

}
