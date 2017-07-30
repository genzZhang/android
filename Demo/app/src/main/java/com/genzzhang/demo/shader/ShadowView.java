package com.genzzhang.demo.shader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.genzzhang.demo.R;

/**
 * Created by Administrator on 2017/7/28.
 */

public class ShadowView extends View{

    Paint mPaint;
    Bitmap mBtp;
    Bitmap mRefBitmap;

    public ShadowView(Context context) {
        super(context);
        init();
    }

    public ShadowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mBtp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_guanjia);
        // 实例化一个矩阵对象
        Matrix matrix = new Matrix();
        matrix.setScale(1F, -1F);
        // 产生和原图大小一样的倒影图
        mRefBitmap = Bitmap.createBitmap(mBtp, 0, 0, mBtp.getWidth(), mBtp.getHeight(), matrix, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mBtp.getWidth(), (int)(mBtp.getHeight() * 1.5));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 为了突出效果，先绘制一个灰色的画布
        //canvas.drawColor(Color.TRANSPARENT);
        int x = 0, y = 0;
        // 绘制好原图
        canvas.drawBitmap(mBtp, x, y, null);
        // 保存图层。这里保存的图层宽度是原图绘制区域的宽度，高度是原图绘制区域两倍的高度，包含了绘制倒影的区域。
        int sc = canvas.saveLayer(x, y + mBtp.getHeight(), x + mBtp.getWidth(), y + (int)(mBtp.getHeight() * 1.5), null, Canvas.ALL_SAVE_FLAG);
        // 绘制倒影图片，绘制的区域紧贴原图的底部
        canvas.drawBitmap(mRefBitmap, x, y + mBtp.getHeight(), null);
        // 设置好混合模式
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        /*
         *  设置线性渐变模式。
         *  这里绘制的高度是原图的1/2，也就说倒影最终的区域也就是原图的1/2
         *  颜色是从Color.BLACK到透明，用于和倒影图做混合模式。
         *  模式是边缘拉伸模式，这里没用到
         */
        mPaint.setShader(new LinearGradient(x, y + mBtp.getHeight(),
                x, y + mBtp.getHeight() + mBtp.getHeight() / 2,
                Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP));
        // 画一个矩形区域，作为目标图片，用来做混合模式
        canvas.drawRect(x, y + mBtp.getHeight(), x + mRefBitmap.getWidth(), y + (int)(mBtp.getHeight() * 1.5), mPaint);

        mPaint.setXfermode(null);

        // 回复图层
        canvas.restoreToCount(sc);
    }
}
