package com.genzzhang.demo.shader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import com.genzzhang.demo.util.Tools;


public class LoadingView extends View {

    private Paint mHexagonPaint;
    private Path mPath;
    private SweepGradient mSweepGradient;

    private int mCenterX, mCenterY;

    private static final float SQRT3 = (float) Math.sqrt(3);

    int mShaderDegree = 0;
    Matrix mMatrix = new Matrix();

    private volatile boolean mRunning = false;
    private int width, height;

    public LoadingView(Context context) {
    	 this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPath = new Path();
        mHexagonPaint = new Paint();
        mHexagonPaint.setStrokeWidth(Tools.dip2px(2));
        mHexagonPaint.setStyle(Paint.Style.STROKE);
        mHexagonPaint.setAntiAlias(true);
        mSweepGradient = new SweepGradient(0, 0, 0x00000000,0x33000000);
        mHexagonPaint.setShader(mSweepGradient);

        width = Tools.dip2px(26);
        height = width;
        mCenterX = width / 2;
        mCenterY = height / 2;
        int radius = mCenterX > mCenterY ? mCenterY : mCenterX;
        mPath = getHexagon(radius - 5);

        startRotationAnimation();
    }

    /**
     * 设置控件width、height
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //daw radar hexagon
        canvas.translate(mCenterX, mCenterY);
        mMatrix.setRotate(mShaderDegree);
        mSweepGradient.setLocalMatrix(mMatrix);
        canvas.drawPath(mPath, mHexagonPaint);

        mShaderDegree = mShaderDegree + 6;
        if (mShaderDegree >= 360) {
            mShaderDegree = 0;
        }

        if (mRunning) {
            invalidate();
        }
    }

    private Path getHexagon(int radiu) {
        //draw a hexagon path
        Path path = new Path();
        float dx = SQRT3 * radiu / 2;
        path.moveTo(0, radiu);
        path.lineTo(0 - dx, radiu / 2);
        path.lineTo(0 - dx, 0 - radiu / 2);
        path.lineTo(0, 0 - radiu);
        path.lineTo(dx, 0 - radiu / 2);
        path.lineTo(dx, radiu / 2);
        path.close();

        return path;
    }


    /**
     * 开始动画
     **/
    public void startRotationAnimation() {
        mRunning = true;
        postInvalidate();
    }

    /**
     * 停止动画
     **/
    public void stopRotationAnimation() {
        mRunning = false;
    }


    /**
     * 覆盖父类方法
     * 以免外部忘记调用stopRotationAnimation(), 导致内存泄漏
     */
    protected void onDetachedFromWindow() {
        stopRotationAnimation();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        startRotationAnimation();
        super.onAttachedToWindow();
    }

}
