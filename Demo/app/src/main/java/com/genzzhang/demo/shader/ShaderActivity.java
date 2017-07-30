package com.genzzhang.demo.shader;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.genzzhang.demo.R;
import com.genzzhang.demo.app.AbsActivity;

/**
 * Created by Administrator on 2017/7/27.
 */

public class ShaderActivity extends AbsActivity {


    private TextView mInfoTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_shader);

        //线性着色器，背景绘制
        View root = getWindow().getDecorView();
        GradientBgDrawable bg = new GradientBgDrawable();
        //bg.updateRealHeight(800);//设置渐变背景高度
        root.setBackgroundDrawable(bg);

        //介绍
        mInfoTv = (TextView) findViewById(R.id.info);
        mInfoTv.setMovementMethod(LinkMovementMethod.getInstance());
        String sb = "<a href='http://www.cnblogs.com/tianzhijiexian/p/4298660.html'>《详解Paint的setShader(Shader shader)》</a> <br/><br/>";
        sb += "Shader子类：<br/>BitmapShader, ComposeShader, LinearGradient, RadialGradient, SweepGradient ";
        mInfoTv.setText(Html.fromHtml(sb));

        //BitmapShader 绘制倒立的阴影

        //闪烁的文字

    }

}
