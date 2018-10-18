package com.genzzhang.demo.nestscrollsuction;


import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ScrollView;

/**
 * ListView背景或者本身paddying没有做到很好的适配
 * 比如，.9影响
 */
public class NestScrollHelper {

    private static final String TAG = "NestScrollLayout";

    private ScrollableTool mScrollableTool;
    private View mCurrentScrollableView;

    private int sysVersion = android.os.Build.VERSION.SDK_INT;


    public NestScrollHelper(Context context) {
        mScrollableTool = new ScrollableTool(context);
    }

    public void setScrollableView(View view) {
        this.mCurrentScrollableView = view;
    }

    private View getScrollableView() {
        return mCurrentScrollableView;
    }

    public boolean isTop() {
        View scrollableView = getScrollableView();
        if (scrollableView == null) {
            return true;
        }
        if (scrollableView instanceof AdapterView) {
            return isAdapterViewTop((AdapterView) scrollableView);
        }
        if (scrollableView instanceof ScrollView) {
            return isScrollViewTop((ScrollView) scrollableView);
        }
        if (scrollableView instanceof WebView) {
            return isWebViewTop((WebView) scrollableView);
        }
        throw new IllegalStateException("scrollableView must be a instance of AdapterView|ScrollView|RecyclerView");
    }

    private static boolean isAdapterViewTop(AdapterView adapterView) {
        if (adapterView != null) {
            int firstVisiblePosition = adapterView.getFirstVisiblePosition();
            View childAt = adapterView.getChildAt(0);
            if (childAt != null) {
                Log.i(TAG, "childAt.getTop()=" + childAt.getTop());
            }
            if (childAt == null || (firstVisiblePosition == 0 && childAt != null && childAt.getTop() == adapterView.getPaddingTop())) {
                return true;
            }
        }
        return false;
    }

    public int getFirstChildTop() {
        View scrollableView = getScrollableView();
        int top = 0;
        if (scrollableView instanceof AdapterView) {
            AdapterView adapterView = (AdapterView)scrollableView;
            View childAt = adapterView.getChildAt(0);
            if (childAt != null) {
                top = childAt.getTop();
                Log.i(TAG, "childAt.getTop()=" + top);
            }
        }
        return top;
    }

    public int getPaddingTop() {
        View scrollableView = getScrollableView();
        int top = 0;
        if (scrollableView instanceof AdapterView) {
            AdapterView adapterView = (AdapterView)scrollableView;
            top = adapterView.getPaddingTop();
            Log.i(TAG, "adapterView.getTop()=" + top);
        }
        return top;
    }

    private static boolean isScrollViewTop(ScrollView scrollView) {
        if (scrollView != null) {
            int scrollViewY = scrollView.getScrollY();
            return scrollViewY <= 0;
        }
        return false;
    }

    private static boolean isWebViewTop(WebView scrollView) {
        if (scrollView != null) {
            int scrollViewY = scrollView.getScrollY();
            return scrollViewY <= 0;
        }
        return false;
    }

    public boolean isBottom() {
        View scrollableView = getScrollableView();
        if (scrollableView == null) {
            return true;
        }
        if (scrollableView instanceof AdapterView) {
            return isAdapterViewBottom((AdapterView) scrollableView);
        }
        throw new IllegalStateException("scrollableView just only support a instance of AdapterView, you add other");
    }

    private static boolean isAdapterViewBottom(AdapterView adapterView) {
        if (adapterView != null) {
            int lastVisiblePosition = adapterView.getLastVisiblePosition();
            if (lastVisiblePosition == adapterView.getCount() - 1) {
                int firstVisiblePosition = adapterView.getFirstVisiblePosition();
                View childAt = adapterView.getChildAt(lastVisiblePosition - firstVisiblePosition);
                if (childAt == null ||
                        (childAt != null && adapterView.getHeight() >= childAt.getBottom())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isEmpty() {
        View scrollableView = getScrollableView();
        if (scrollableView == null) {
            return true;
        }
        if (scrollableView instanceof AdapterView) {
            return isAdapterViewEmpty((AdapterView) scrollableView);
        }
        throw new IllegalStateException("scrollableView just only support a instance of AdapterView, you add other");
    }

    /**
     * 空数据，除开包含foot head的真实数据
     * adapterView.getAdapter().getCount() ==adapterView.getCount() 包括head或者foot之类的
     */
    private static boolean isAdapterViewEmpty(AdapterView adapterView) {
        if (adapterView != null && !adapterView.getAdapter().isEmpty()) {
            return false;
        }
        return true;
    }

    @SuppressLint("NewApi")
    public void smoothScrollBy(int velocityY, int distance, int duration) {
        View scrollableView = getScrollableView();
        if (scrollableView instanceof AbsListView) {
            AbsListView absListView = (AbsListView) scrollableView;
            if (sysVersion >= 21) {
                absListView.fling(velocityY);
            } else {
                absListView.smoothScrollBy(distance, duration);
            }
        } else if (scrollableView instanceof ScrollView) {
            ((ScrollView) scrollableView).fling(velocityY);
        } else if (scrollableView instanceof WebView) {
            ((WebView)scrollableView).flingScroll(0,velocityY);
        }
    }


    public static class ScrollableTool {

        //为了根据速度估算距离
        private static float PHYSICAL_COEF;
        private float mFlingFriction = ViewConfiguration.getScrollFriction();
        private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
        private static final float INFLEXION = 0.35f; //

        public ScrollableTool(Context context) {
            final float ppi = context.getResources().getDisplayMetrics().density * 160.0f;
            PHYSICAL_COEF = SensorManager.GRAVITY_EARTH // g (m/s^2)
                    * 39.37f // inch/meter
                    * ppi
                    * 0.84f; // look and feel tuning
        }

        /***
         * 根据速度获取估算距离
         * 和mScroller.fling一样的计算，只是要单独new一个mScroller出来专用
         * @param velocity
         * @return
         */
        public int getEstimatedDistance(int velocity) {
            double totalDistance = 0.0;
            if (velocity != 0) {
                totalDistance = getSplineFlingDistance(velocity);
            }
            int distance1 = (int)(totalDistance * Math.signum(velocity));
            //mScroller.fling(0, 0, 0, velocity, 0, 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
            //int distance2 = mScroller.getFinalY();
            //Log.i(TAG, "getEstimatedDistance distance1=" + distance1 + "|distance2=" + distance2);
            return distance1;
        }

        private double getSplineFlingDistance(int velocity) {
            final double l = getSplineDeceleration(velocity);
            final double decelMinusOne = DECELERATION_RATE - 1.0;
            return mFlingFriction * PHYSICAL_COEF * Math.exp(DECELERATION_RATE /
                    decelMinusOne * l);
        }

        private double getSplineDeceleration(int velocity) {
            return Math.log(INFLEXION * Math.abs(velocity) /
                    (mFlingFriction * PHYSICAL_COEF));
        }

    }



    /***
     * 根据速度获取估算距离
     */
    public int getEstimatedDistance(int velocity) {
        return mScrollableTool.getEstimatedDistance(velocity);
    }

}
