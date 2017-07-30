package com.genzzhang.demo.porterduffanimation;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.genzzhang.demo.R;
import com.genzzhang.demo.app.AbsActivity;

public class PorterDuffActivity extends AbsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_porter_duff_ani);
        //背景
        findViewById(R.id.bg_view1).setBackgroundDrawable(new BgDrawable(false));
        findViewById(R.id.bg_view2).setBackgroundDrawable(new BgDrawable(true));
        //齿轮转动动画
        final GearAniView gearView = (GearAniView) findViewById(R.id.grant_state_view);
        final TextView mainTitle = (TextView) findViewById(R.id.grant_main_title);
        final TextView subTitle = (TextView) findViewById(R.id.grant_sub_title);
        gearView.setGrantState(GearAniView.QUICK_GRANT_ING);
        mainTitle.setText("当前状态：正在开启");
        subTitle.setText("请等待");
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gearView.mGrantState == GearAniView.QUICK_GRANT_ING) {
                    gearView.setGrantState(GearAniView.QUICK_GRANT_SUCCESS);
                    mainTitle.setText("当前状态：成功");
                    subTitle.setText("恭喜");
                } else if (gearView.mGrantState == GearAniView.QUICK_GRANT_SUCCESS){
                    gearView.setGrantState(GearAniView.QUICK_GRANT_FAILED);
                    mainTitle.setText("当前状态：失败");
                    subTitle.setText("重试");
                } else {
                    gearView.setGrantState(GearAniView.QUICK_GRANT_ING);
                    mainTitle.setText("当前状态：正在开启");
                    subTitle.setText("请等待");
                }
            }
        });

    }

}

