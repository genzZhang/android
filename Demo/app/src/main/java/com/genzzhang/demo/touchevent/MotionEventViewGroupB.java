package com.genzzhang.demo.touchevent;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.genzzhang.demo.util.C;

/**
 * Created by Administrator on 2016/7/26.
 */
public class MotionEventViewGroupB extends LinearLayout {

    public MotionEventViewGroupB(Context context) {
        super(context);
    }

    public MotionEventViewGroupB(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MotionEventViewGroupB(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e(C.TAG, "MotionEventViewGroupB dispatchTouchEventB:" + getAction(ev));
        //return true;表示此次事件如down被消费，接着move up新一轮的开始
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.e(C.TAG, "MotionEventViewGroupB onInterceptTouchEventB:" + getAction(ev));
        //如果down时间被拦截，则move、up也没有了
        if (getAction(ev).equals("ACTION_DOWN"))
            return true;
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.e(C.TAG, "MotionEventViewGroupB onTouchEventB:" + getAction(ev));
        if (getAction(ev).equals("ACTION_DOWN"))
            return true;
        return super.onTouchEvent(ev);
    }

    private String getAction(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            return "ACTION_DOWN";
            case MotionEvent.ACTION_MOVE:
            return "ACTION_MOVE";
            case MotionEvent.ACTION_UP:
            return "ACTION_UP";
        }
        return "null";
    }
}
