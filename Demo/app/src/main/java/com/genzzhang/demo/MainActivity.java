package com.genzzhang.demo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.genzzhang.demo.badges.BadgesActivity;
import com.genzzhang.demo.horizontalloopview.HorizontnalLoopViewActivity;
import com.genzzhang.demo.listviewanimation.ListviewAniActivity;
import com.genzzhang.demo.nestscrollsuction.NestScrollSuctionActivity;
import com.genzzhang.demo.popup.PopupActivity;
import com.genzzhang.demo.porterduffanimation.PorterDuffActivity;
import com.genzzhang.demo.richtexts.RichTextsActivity;
import com.genzzhang.demo.ringtone.RingtoneActivity;
import com.genzzhang.demo.shader.ShaderActivity;
import com.genzzhang.demo.touchevent.TouchEventActivity;
import com.genzzhang.demo.util.C;
import com.genzzhang.demo.xmlparser.EmojiParseActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private ListView mListView;
    private List<ActivityHolder> mActivityList = new ArrayList<>();
    private String[] mDescri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadClass();
        mListView = (ListView) findViewById(R.id.listview);
        mListView.setAdapter(new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, mDescri));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, mActivityList.get(position).cls);
                intent.putExtra(C.Extra.Title, mActivityList.get(position).descri);
                startActivity(intent);
            }
        });
        findViewById(R.id.title).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Uri uri = Uri.parse("https://genzzhang.github.io/");
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
                return true;
            }
        });
        Toast.makeText(this, getIntent().getComponent().getClassName(), Toast.LENGTH_SHORT).show();
    }

    private void loadClass() {
        //add activity @ here and manifest, start
        mActivityList.add(new ActivityHolder("水平循环滚动：支持自动播放和左右滑动", HorizontnalLoopViewActivity.class));
        mActivityList.add(new ActivityHolder("触摸事件分发与拦截测试", TouchEventActivity.class));
        mActivityList.add(new ActivityHolder("ListView删除动画", ListviewAniActivity.class));
        mActivityList.add(new ActivityHolder("PorterDuff模式实践", PorterDuffActivity.class));
        mActivityList.add(new ActivityHolder("Paint的setShader之着色器", ShaderActivity.class));
        mActivityList.add(new ActivityHolder("读取assets中Xml解析展示Emoji", EmojiParseActivity.class));
        mActivityList.add(new ActivityHolder("富文本展示", RichTextsActivity.class));
        mActivityList.add(new ActivityHolder("嵌套滑动吸顶页面", NestScrollSuctionActivity.class));
        mActivityList.add(new ActivityHolder("设置联系人铃声", RingtoneActivity.class));
        mActivityList.add(new ActivityHolder("设置桌面角标和切换图标", BadgesActivity.class));
        mActivityList.add(new ActivityHolder("弹窗控制WindowManager Hook", PopupActivity.class));


        //end
        mDescri = new String[mActivityList.size()];
        for (int i = 0; i < mActivityList.size(); i++) {
            mDescri[i] = i + 1 + ". " + mActivityList.get(i).descri;
        }
    }

    private class ActivityHolder {
        public String descri;
        public Class<? extends Activity> cls;

        public ActivityHolder(String name, Class cls) {
            this.descri = name;
            this.cls = cls;
        }
    }
}
