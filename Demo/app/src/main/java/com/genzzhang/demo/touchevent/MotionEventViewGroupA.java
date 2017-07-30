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
public class MotionEventViewGroupA extends LinearLayout {
    public MotionEventViewGroupA(Context context) {
        super(context);
    }

    public MotionEventViewGroupA(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MotionEventViewGroupA(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e(C.TAG, "MotionEventViewGroupA dispatchTouchEventA " + getAction(ev));
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.e(C.TAG, "MotionEventViewGroupA onInterceptTouchEventA " + getAction(ev));
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e(C.TAG, "MotionEventViewGroupA onTouchEventA " + getAction(event));
        return super.onTouchEvent(event);
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
