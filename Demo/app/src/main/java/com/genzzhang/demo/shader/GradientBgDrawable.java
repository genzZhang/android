package com.genzzhang.demo.shader;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * Created by Administrator on 2017/7/27.
 * 屏幕背景
 */

public class GradientBgDrawable extends Drawable {
    private LinearGradient mShader;
    private Paint mPaint;

    private int mStartColor;
    private int mEndColor;
    private int mAlpha = 255;

    private int mRealHeight = -1;

    public static final byte NORMAL_MODE_TYPE = 0;
    public static final byte HEALTH_MODE_TYPE = 1;
    public static final byte WARN_MODE_TYPE = 2;
    public static final byte DANGER_MODE_TYPE = 3;

    public GradientBgDrawable() {
        mStartColor = 0xff23FFFF;
        mEndColor = 0xff472BF4;
        mPaint = new Paint();
    }

    public GradientBgDrawable(byte mode) {
        switch (mode) {
            case HEALTH_MODE_TYPE: //, #C6FF72 0%, #00BC61
                mStartColor = 0xffC6FF72;
                mEndColor = 0xff00BC61;
                break;
            case WARN_MODE_TYPE: //#FBFF42 0%, #FF8200
                mStartColor = 0xffFBFF42;
                mEndColor = 0xffFF8200;
                break;
            case DANGER_MODE_TYPE: //FFBD38 0%, #F40B30
                mStartColor = 0xffFFBD38;
                mEndColor = 0xffF40B30;
                break;

            default:
                mStartColor = 0xff23FFFF;
                mEndColor = 0xff472BF4;
                break;
        }
        mPaint = new Paint();
    }

    public GradientBgDrawable(int startColor, int endColor) {
        mStartColor = startColor;
        mEndColor = endColor;
        mPaint = new Paint();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        float w = bounds.width();
        float h = 16 * w / 9;
        mShader = new LinearGradient(0, h, w, 0,
                mStartColor, mEndColor, Shader.TileMode.CLAMP);
        mPaint.setShader(mShader);
        mPaint.setAlpha(mAlpha);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        if (mRealHeight >= 0) {
            //绘制实际的高度，不按照默认的整个屏幕
            canvas.clipRect(0, 0, getBounds().width(), mRealHeight);
        }
        canvas.drawRect(getBounds(), mPaint);
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
        if (mPaint != null) {
            mPaint.setAlpha(alpha);
        }
    }

    @Override
    public int getAlpha() {
        return mAlpha;
    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    public void updateRealHeight(int h) {
        mRealHeight = h;
    }
}
