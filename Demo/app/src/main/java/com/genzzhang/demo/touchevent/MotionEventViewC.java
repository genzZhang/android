package com.genzzhang.demo.touchevent;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.genzzhang.demo.util.C;

/**
 * Created by Administrator on 2016/7/26.
 */
public class MotionEventViewC extends View {
    public MotionEventViewC(Context context) {
        super(context);
    }

    public MotionEventViewC(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MotionEventViewC(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e(C.TAG, "MotionEventViewC dispatchTouchEventC " + getAction(ev));
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e(C.TAG, "MotionEventViewC onTouchEventC " + getAction(event));
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