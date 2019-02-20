package com.genzzhang.demo.badges;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.genzzhang.demo.app.AbsActivity;
import com.genzzhang.demo.badges.alias.LauncherAliasManager;
import com.genzzhang.demo.badges.digital.BadgeManager;

public class BadgesActivity extends AbsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(layout);

        Button defaultBtn = new Button(BadgesActivity.this);
        defaultBtn.setText("切换到默认桌面图标");
        defaultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LauncherAliasManager.getInstance().applyDefaultAlias(BadgesActivity.this);
            }
        });
        layout.addView(defaultBtn);

        Button newBtn = new Button(BadgesActivity.this);
        newBtn.setText("切换到新桌面图标");
        newBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LauncherAliasManager.getInstance().applyNewAlias(BadgesActivity.this);
            }
        });
        layout.addView(newBtn);

        Button sixBtn = new Button(BadgesActivity.this);
        sixBtn.setText("设置6个角标");
        sixBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BadgeManager.applyCount(BadgesActivity.this, 6);
            }
        });
        layout.addView(sixBtn);

        Button zeroBtn = new Button(BadgesActivity.this);
        zeroBtn.setText("设置0个角标");
        zeroBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BadgeManager.applyCount(BadgesActivity.this, 0);
            }
        });
        layout.addView(zeroBtn);

    }
}
