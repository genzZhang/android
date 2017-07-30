package com.genzzhang.demo.porterduffanimation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.genzzhang.demo.R;

/**
 * Created by genzZHANG on 2016/9/17.
 */
public class LoadingView extends View {

    private Resources mResources;
    private Paint mBitPaint;
    private Bitmap mBitmap;

    private int mTotalWidth, mTotalHeight;
    private int mBitWidth, mBitHeight;
    private PorterDuffXfermode mXfermode;

    private Rect mDynamicRect;
    private int mCurrentTop;
    private int mStart, mEnd;

    public LoadingView(Context context) {
        super(context);
        mResources = getResources();
        initBitmap();
        initPaint();
        initXfermode();
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mResources = getResources();
        initBitmap();
        initPaint();
        initXfermode();
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mResources = getResources();
        initBitmap();
        initPaint();
        initXfermode();
    }

    private void initXfermode() {
        // 叠加处绘制源图
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    }

    private void initPaint() {
        // 初始化paint
        mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitPaint.setFilterBitmap(true);
        mBitPaint.setDither(true);
        mBitPaint.setColor(Color.RED);
    }

    private void initBitmap() {
        // 初始化bitmap
        mBitmap = ((BitmapDrawable) mResources.getDrawable(R.drawable.ic_android)) .getBitmap();
        mBitWidth = mBitmap.getWidth();
        mBitHeight = mBitmap.getHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mBitWidth, mBitHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 存为新图层
        int saveLayerCount = canvas.saveLayer(0, 0, mTotalWidth, mTotalHeight, mBitPaint,
                Canvas.ALL_SAVE_FLAG);
        // 绘制目标图
        canvas.drawBitmap(mBitmap, 0, 0, null);
        // 设置混合模式
        mBitPaint.setXfermode(mXfermode);
        // 绘制源图形
        canvas.drawRect(mDynamicRect, mBitPaint);
        // 清除混合模式
        mBitPaint.setXfermode(null);
        // 恢复保存的图层；
        canvas.restoreToCount(saveLayerCount);

        // 改变Rect区域，真实情况下时提供接口传入进度，计算高度
        mCurrentTop -= 8;
        if (mCurrentTop <= mEnd) {
            mCurrentTop = mStart;
        }
        mDynamicRect.top = mCurrentTop;
        postInvalidateDelayed(500);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTotalWidth = w;
        mTotalHeight = h;
        // 让左边和上边有些距离
        int left = (int) TypedValue.complexToDimension(20, mResources.getDisplayMetrics());
        mStart = mBitHeight + left;
        mCurrentTop = mStart;
        mEnd = left;
        mDynamicRect = new Rect(left, mStart, left + mBitWidth, left + mBitHeight);
    }
}


