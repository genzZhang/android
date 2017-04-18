package com.genzzhang.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = (LinearLayout) LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.activity_main, null);
        final HorizontalLoopGridView gridView = new HorizontalLoopGridView(MainActivity.this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.width = HorizontalLoopGridView.sCardSize;
        params.gravity = Gravity.CENTER_HORIZONTAL;
        new HorizontalLoopGridAdapter(MainActivity.this, gridView, null);
        layout.addView(gridView, params);
        setContentView(layout);
    }
}
