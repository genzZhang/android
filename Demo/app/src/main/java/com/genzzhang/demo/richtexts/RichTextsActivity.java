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
import android.util.Log;
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
    private final static String TAG = "RichTextsActivity";
    // 描述黄色背景
    private Drawable mDescriBg;
    private ArrayList<Integer> mDescriSize = new ArrayList<Integer>(5);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rich_texts);

        //描述设置
        mDescriBg = getResources().getDrawable(R.drawable.rich_texts_mark_yellow_bg);
        mDescriSize.add(Tools.dip2px(8));//字体大小
        mDescriSize.add(Tools.dip2px(2));//增加水平间距
        mDescriSize.add(Tools.dip2px(1));//背景边距设置的1dp mMarkBg

        ((TextView) findViewById(R.id.title)).setText(
                getSpannableTitle("设置行距、边距、行间距倍数等，保持居中显示问题", "文本"));

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
        SpannableString msp = new SpannableString(title + mark);
        msp.setSpan(new ImageSpan(mDescriBg) {
            @Override
            public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
                paint.setTextSize(fontSize);
                width.add(0, Math.round(paint.measureText(text, start, end)));
                //加上文字左右各一个
                return width.get(0) + 2 * fontPadding;
            }
            @Override
            public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom,
                             Paint paint) {
                canvas.drawLine(x, top, x + 300, top, paint);
                canvas.drawLine(x, y, x + 300, y, paint);
                canvas.drawLine(x, bottom, x + 300, bottom, paint);

                float oldad = paint.descent() + paint.ascent();
                paint.setTextSize(fontSize);
                float newad = paint.descent() + paint.ascent();
                //以改变前的为中间基准
                int baseY = (int)(y * 2 + oldad - newad ) / 2;
                int textHeight = (int)(paint.descent() - paint.ascent());
                //字体净高度 + 背景边界宽度=总高度
                int height = textHeight + cornerSize * 2;
                //以新的文字中心点基准
                int bgBottom = (int)((2 * baseY + newad + height) / 2.0);
                getDrawable().setBounds(
                        0,
                        0,
                        width.get(0) + 2 * fontPadding,
                        height);
                //绘制背景图
                super.draw(canvas, text, start, end, x, top, y, bgBottom, paint);
                paint.setColor(Color.RED);
                //paint.setTypeface(Typeface.create("normal", Typeface.BOLD));
                canvas.drawText(text.subSequence(start, end).toString(),
                        x + fontPadding,
                        baseY, paint);
            }
        }, title.length(), title.length() + mark.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return msp;
    }
}
