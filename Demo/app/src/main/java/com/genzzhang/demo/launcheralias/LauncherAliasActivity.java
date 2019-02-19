package com.genzzhang.demo.launcheralias;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.genzzhang.demo.app.AbsActivity;

public class LauncherAliasActivity extends AbsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(layout);

        Button defaultBtn = new Button(LauncherAliasActivity.this);
        defaultBtn.setText("切换到默认桌面图标");
        defaultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LauncherAliasManager.getInstance().applyDefaultAlias(LauncherAliasActivity.this);
            }
        });
        layout.addView(defaultBtn);

        Button newBtn = new Button(LauncherAliasActivity.this);
        newBtn.setText("切换到新桌面图标");
        newBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LauncherAliasManager.getInstance().applyNewAlias(LauncherAliasActivity.this);
            }
        });
        layout.addView(newBtn);

    }
}
