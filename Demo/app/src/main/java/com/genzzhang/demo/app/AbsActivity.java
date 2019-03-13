package com.genzzhang.demo.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.genzzhang.demo.util.C;

public class AbsActivity extends AppCompatActivity {

    protected final static String TAG = "AbsActivity";
    protected Toast mToast;
    protected Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getIntent().getStringExtra(C.Extra.Title));
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mContext = this;
    }
}
