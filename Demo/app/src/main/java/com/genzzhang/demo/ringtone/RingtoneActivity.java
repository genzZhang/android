package com.genzzhang.demo.ringtone;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.genzzhang.demo.app.AbsActivity;

public class RingtoneActivity extends AbsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout layout = new FrameLayout(mContext);
        layout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(layout);
    }
}
