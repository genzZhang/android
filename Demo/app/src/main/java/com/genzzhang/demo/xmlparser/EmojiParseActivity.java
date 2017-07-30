package com.genzzhang.demo.xmlparser;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.genzzhang.demo.R;
import com.genzzhang.demo.app.AbsActivity;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/7/27.
 */

public class EmojiParseActivity extends AbsActivity {


    private ArrayList<EmojiInfo> mEmojiInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoji_parse);
        final GridView gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return mEmojiInfos == null ? 0 : mEmojiInfos.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                EmojiInfo info = mEmojiInfos.get(position);
                EmojiViewHolder viewHolder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_emoji_parse_grid_view_item, null);
                    viewHolder = new EmojiViewHolder();
                    viewHolder.nameTv = (TextView) convertView.findViewById(R.id.name);
                    viewHolder.icImage = (ImageView) convertView.findViewById(R.id.ic);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (EmojiViewHolder) convertView.getTag();
                }
                viewHolder.nameTv.setText(info.name);
                viewHolder.icImage.setBackgroundDrawable(info.ic);
                return convertView;
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = EmojiParseUtil.getDisplayCount();
                String name;
                Drawable ic;
                for (int i = 0; i < count; i++) {
                    name = EmojiParseUtil.getDisplayText(i);
                    ic = EmojiParseUtil.getDisplayDrawable(mContext, i);
                    EmojiInfo info = new EmojiInfo();
                    info.name = name;
                    info.ic = ic;
                    mEmojiInfos.add(info);
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ((BaseAdapter)gridView.getAdapter()).notifyDataSetChanged();
                    }
                });
            }
        }).start();

    }

    private class EmojiInfo {
        String name;
        Drawable ic;
    }

    private class EmojiViewHolder {
        TextView nameTv;
        ImageView icImage;
    }

}
