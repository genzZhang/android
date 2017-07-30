package com.genzzhang.demo.horizontalloopview;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.genzzhang.demo.R;
import com.genzzhang.demo.app.AbsActivity;

public class HorizontnalLoopViewActivity extends AbsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = (LinearLayout) LayoutInflater.from(HorizontnalLoopViewActivity.this)
                .inflate(R.layout.activity_horizontal_loop_gridview, null);
        final HorizontalLoopView gridView = new HorizontalLoopView(HorizontnalLoopViewActivity.this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.width = HorizontalLoopView.sCardSize;
        params.gravity = Gravity.CENTER_HORIZONTAL;
        new HorizontalLoopAdapter(HorizontnalLoopViewActivity.this, gridView, null);
        layout.addView(gridView, params);
        setContentView(layout);
    }
}
