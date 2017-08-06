package com.genzzhang.demo.richtexts;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.genzzhang.demo.R;
import com.genzzhang.demo.app.AbsActivity;
import com.genzzhang.demo.horizontalloopview.HorizontnalLoopViewActivity;
import com.genzzhang.demo.listviewanimation.ListviewAniActivity;
import com.genzzhang.demo.porterduffanimation.PorterDuffActivity;
import com.genzzhang.demo.shader.ShaderActivity;
import com.genzzhang.demo.touchevent.TouchEventActivity;
import com.genzzhang.demo.util.C;
import com.genzzhang.demo.util.Tools;
import com.genzzhang.demo.xmlparser.EmojiParseActivity;

import java.util.ArrayList;
import java.util.List;

public class RichTextsActivity extends AbsActivity {
    // 描述黄色背景
    private Drawable mDescriBg;
    private ArrayList<Integer> mDescriSize = new ArrayList<Integer>(5);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rich_texts);

        //描述设置
        mDescriBg = getResources().getDrawable(R.drawable.rich_texts_mark_yellow_bg);
        mDescriSize.add(Tools.dip2px(12));//字体大小
        mDescriSize.add(Tools.dip2px(2));//增加水平间距
        mDescriSize.add(Tools.dip2px(1));//背景边距设置的1dp mMarkBg
        Paint paint = new Paint();
        paint.setTextSize(mDescriSize.get(0));
        Paint.FontMetricsInt fMetrics = paint.getFontMetricsInt();
        mDescriSize.add(fMetrics.bottom + fMetrics.top); //字体
        mDescriSize.add(fMetrics.descent - fMetrics.ascent); //字体净高度

        TextView textView = (TextView) findViewById(R.id.title);
        textView.setText(getSpannableTitle("解决文本绘制不居中的问题", "文TextView本"));
    }

    private SpannableString getSpannableTitle(String title, String mark) {
        //净宽度
        final ArrayList<Integer> width = new ArrayList<Integer>(1);
        //字体大小
        final int fontSize = mDescriSize.get(0);
        //增加水平间距
        final int fontPadding = mDescriSize.get(1);
        //背景边距设置的1dp mMarkBg
        final int cornerSize = mDescriSize.get(2);
        SpannableString msp = new SpannableString(title + mark + "谣言不转不是中国人");
        msp.setSpan(new ImageSpan(mDescriBg) {
            @Override
            public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
                paint.setTextSize(fontSize);
                width.add(0, Math.round(paint.measureText(text, start, end)));
                //整体偏移2个fontPadding 加上文字左右各一个，一共4
                return width.get(0) + fontPadding * 4;
            }
            @Override
            public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom,
                             Paint paint) {
                //绘制背景图
                paint.setTextSize(fontSize);
                int textHeight = mDescriSize.get(4);
                int oldHeight = bottom - top;
                //字体净高度 + 背景边界宽度=总高度
                int height = textHeight + cornerSize * 2;
                int dp = (int)(oldHeight - height) / 2;
                getDrawable().setBounds(
                        0,
                        0,
                        width.get(0) + fontPadding * 2,
                        height);
                super.draw(canvas, text, start, end, x + 2 * fontPadding, top, y, bottom - dp, paint);
                // 绘制文本
                //中文字符绘制的中间位置的基准点一般设置在（总体高度/2 - 文字高度/2）
                //位置 http://blog.csdn.net/hursing/article/details/18703599
                int baseY = (int)(top + bottom - mDescriSize.get(3)) / 2;
                paint.setColor(Color.RED);
                //paint.setTypeface(Typeface.create("normal", Typeface.BOLD));
                canvas.drawText(text.subSequence(start, end).toString(),
                        x + fontPadding * 3,
                        baseY, paint);
            }
        }, title.length(), title.length() + mark.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return msp;
    }
}
