package com.genzzhang.demo.porterduffanimation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Created by genzzhang on 2016年9月12日.
 * 设置特殊背景
 * 参数：背景颜色、位置
 * 大小默认：15dp 30px
 * 位置：top || bottom
 */
public class BgDrawable extends Drawable {
	private final static int SECTOR_RADIUS = 10;
	private Paint mPaint;
	private boolean mTopable;
	RectF rectLeft = null;
	RectF rectRight = null;

	public BgDrawable(boolean top) {
		mTopable = top;
		initPaint();
	}

	private void initPaint() {
		mPaint = new Paint();
		mPaint.setColor(Color.TRANSPARENT);
		mPaint.setAlpha(0);
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setXfermode(new PorterDuffXfermode(
				android.graphics.PorterDuff.Mode.SRC_IN));
	}

	private void OnDraw(Canvas canvas) {
		int sectorRadius = 2 * SECTOR_RADIUS;
		int height = getBounds().bottom - getBounds().top;
		int width = getBounds().right - getBounds().left;

		//canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //清空会系统默认的，存在机型适配

		Bitmap bitmap= Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas tempCanvas=new Canvas(bitmap);

		tempCanvas.drawColor(mTopable ? Color.WHITE : Color.RED);

		if (mTopable) {
			rectLeft = new RectF(-sectorRadius,
					-sectorRadius,
					sectorRadius,
					sectorRadius);
			rectRight = new RectF(width - sectorRadius,
					-sectorRadius,
					width + sectorRadius,
					sectorRadius);
			tempCanvas.drawArc(rectLeft,  0, 90, true, mPaint);
			tempCanvas.drawArc(rectRight,  90, 90, true, mPaint);
		} else {
			rectLeft = new RectF(-sectorRadius,
					height - sectorRadius,
					sectorRadius,
					height + sectorRadius);
			rectRight = new RectF(width - sectorRadius,
					height - sectorRadius,
					width + sectorRadius,
					height + sectorRadius);
			tempCanvas.drawArc(rectLeft,  0, -90, true, mPaint);
			tempCanvas.drawArc(rectRight,  -90, -90, true, mPaint);
		}
		canvas.drawBitmap(bitmap, 0, 0, null);
	}

	@Override
	public void draw(Canvas canvas) {
		OnDraw(canvas);
	}

	@Override
	public void setAlpha(int alpha) {

	}

	@Override
	public void setColorFilter(ColorFilter cf) {

	}

	@Override
	public int getOpacity() {
		return 0;
	}


}
