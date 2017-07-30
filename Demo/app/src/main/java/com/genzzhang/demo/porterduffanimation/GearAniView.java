package com.genzzhang.demo.porterduffanimation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import com.genzzhang.demo.R;
import com.genzzhang.demo.util.Tools;

/**
 * Created by genzzhang on 2016年10月6日.
 * 背景图有要求，合适的地方要透明
 */
public class GearAniView extends View {

    private Context mContext;
    //画笔
    private Paint mBitPaint;
    //钟罩、大小齿轮和大小圆环 失败和成功的图标
    private Bitmap mBellBitmap;
    private Bitmap mBigGearBitmap;
    private Bitmap mSmallGearBitmap;
    private Bitmap mBigRing;
    private Bitmap mSmallRing;
    private Bitmap mFailedImg;
    private Bitmap mSuccessImg;
    //以钟罩为背景，大小齿轮偏移放置，初始化设置
    private float mBigGearDx;
    private float mBigGearDy;
    private float mSmallGearDx;
    private float mSmallGearDy;
    //转动角度，大小齿轮和大小圆环都设置为一样的,每次变化20°
    private static final float ROTATE_DEGREE_INCREASE = 0.6f;
    private float mRotateDegree;
    //是否重复绘制，绘制的周期200ms
    private boolean mPostInvalidate = true;
    private static final int INVALIDATE_PEROID = 200;
    //一键开启状态
    public int mGrantState = QUICK_GRANT_ING;
    public static final int QUICK_GRANT_ING = 0;
    public static final int QUICK_GRANT_SUCCESS = 1;
    public static final int QUICK_GRANT_FAILED = 2;


    public GearAniView(Context context) {
        super(context);
        init(context);
    }

    public GearAniView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GearAniView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        // 初始化paint
        mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitPaint.setFilterBitmap(true);
        mBitPaint.setDither(true);
        // 初始化bitmap
        mBellBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gear_ani_bell);
        mBigGearBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gear_ani_big_gear);
        mSmallGearBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gear_ani_small_gear);
        mBigRing = BitmapFactory.decodeResource(getResources(), R.drawable.gear_ani_big_ring);
        mSmallRing = BitmapFactory.decodeResource(getResources(), R.drawable.gear_ani_small_ring);
        mSuccessImg = BitmapFactory.decodeResource(getResources(), R.drawable.gear_ani_do_success);
        mFailedImg = BitmapFactory.decodeResource(getResources(), R.drawable.gear_ani_do_failed);
        //偏移量  这里的数据都是根据图的具体大小算出来的，不同的xh文件夹，大小不一样的
        mBigGearDx = Tools.dip2px(25);
        mBigGearDy = Tools.dip2px(74);
        mSmallGearDx = Tools.dip2px(103);
        mSmallGearDy = Tools.dip2px(29);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        switch(mGrantState) {
            case QUICK_GRANT_ING:
                invalidateGrantIng(canvas);
                break;
            case QUICK_GRANT_SUCCESS:
                invalidateGrantSuccess(canvas);
                break;
            case QUICK_GRANT_FAILED:
                invalidateGrantFailed(canvas);
                break;
        }
    }

    public void setGrantState(int state) {
        mGrantState = state;
        postInvalidate();
    }

    private void invalidateGrantIng(Canvas canvas) {
        if (mBellBitmap == null ||
                mBigGearBitmap == null ||
                mSmallGearBitmap == null ||
                mBigRing == null ||
                mSmallRing == null) {
            return;
        }

        //Bitmap bitmap=Bitmap.createBitmap(mBellBitmap.getWidth(), mBellBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = canvas;//new Canvas(bitmap);

        mBitPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        Matrix matrixBigGear = new Matrix();
        matrixBigGear.postTranslate(mBigGearDx, mBigGearDy);
        matrixBigGear.postRotate(mRotateDegree += ROTATE_DEGREE_INCREASE,
                mBigGearDx + mBigGearBitmap.getWidth() / 2,
                mBigGearDy + mBigGearBitmap.getHeight() / 2);
        tempCanvas.drawBitmap(mBigGearBitmap, matrixBigGear, null);

        Matrix matrixSmallGear = new Matrix();
        matrixSmallGear.postTranslate(mSmallGearDx, mSmallGearDy);
        matrixSmallGear.postRotate(mRotateDegree += ROTATE_DEGREE_INCREASE,
                mSmallGearDx + mSmallGearBitmap.getWidth() / 2,
                mSmallGearDy + mSmallGearBitmap.getHeight() / 2);
        tempCanvas.drawBitmap(mSmallGearBitmap, matrixSmallGear, null);

        tempCanvas.drawBitmap(mBellBitmap, 0, 0, mBitPaint);

        //注意over硬件加速无效，需要设置一下
        mBitPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        tempCanvas.drawBitmap(mBellBitmap, 0, 0, mBitPaint);

        Matrix matrixBigRing = new Matrix();
        matrixBigRing.postRotate(mRotateDegree += ROTATE_DEGREE_INCREASE, mBellBitmap.getWidth()/2, mBellBitmap.getHeight()/2);
        tempCanvas.drawBitmap(mBigRing, matrixBigRing, null);

        Matrix matrixSmallRing = new Matrix();
        matrixSmallRing.postRotate(mRotateDegree += ROTATE_DEGREE_INCREASE, mBellBitmap.getWidth()/2, mBellBitmap.getHeight()/2);
        tempCanvas.drawBitmap(mSmallRing, matrixSmallRing, null);

        //canvas.drawBitmap(bitmap, 0, 0, null);

        postInvalidate();
    }

    private void invalidateGrantSuccess(Canvas canvas) {
        if (mBellBitmap == null || mSuccessImg == null) {
            return;
        }

        //Bitmap bitmap=Bitmap.createBitmap(mBellBitmap.getWidth(), mBellBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = canvas;//new Canvas(bitmap);

        tempCanvas.drawBitmap(mBellBitmap, 0, 0, null);
        tempCanvas.drawBitmap(mSuccessImg, 0, 0, null);

        //canvas.drawBitmap(bitmap, 0, 0, null);
    }

    private void invalidateGrantFailed(Canvas canvas) {
        if (mBellBitmap == null || mFailedImg == null) {
            return;
        }

        //Bitmap bitmap=Bitmap.createBitmap(mBellBitmap.getWidth(), mBellBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = canvas;//new Canvas(bitmap);

        tempCanvas.drawBitmap(mBellBitmap, 0, 0, null);
        tempCanvas.drawBitmap(mFailedImg, 0, 0, null);

        //canvas.drawBitmap(bitmap, 0, 0, null);
    }

    public void clearBitmap() {
        if (mBellBitmap != null) {
            mBellBitmap = null;
        }
        if (mBigGearBitmap != null) {
            mBigGearBitmap = null;
        }
        if (mSmallGearBitmap != null) {
            mSmallGearBitmap = null;
        }
        if (mBigRing != null) {
            mBigRing = null;
        }
        if (mSmallRing != null) {
            mSmallRing = null;
        }
        if (mSuccessImg != null) {
            mSuccessImg = null;
        }
        if (mFailedImg != null) {
            mFailedImg = null;
        }
    }

}
