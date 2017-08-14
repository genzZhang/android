package com.genzzhang.demo.richtexts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.genzzhang.demo.util.Tools;

/**
 * Created by Administrator on 2017/8/14.
 */

public class TextTestView extends View {

    private Paint mPaint;
    private float x;
    private float y;

    public TextTestView(Context context) {
        super(context);
        init();
    }

    public TextTestView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        mPaint = new Paint();
        mPaint.setTextSize(Tools.dip2px(50));
        mPaint.setColor(Color.RED);
        x = 400f;
        y = 400f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText("基", x, y, mPaint);
        float size = mPaint.measureText("基");
        canvas.drawLine(x, y + mPaint.ascent(), x + size, y + mPaint.ascent(), mPaint);
        canvas.drawLine(x, y, x + size, y, mPaint);
        canvas.drawLine(x, y + mPaint.descent(), x + size, y + mPaint.descent(), mPaint);
    }
}
