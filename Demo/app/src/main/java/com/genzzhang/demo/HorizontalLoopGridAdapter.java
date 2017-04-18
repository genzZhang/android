package com.genzzhang.demo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.view.View.OVER_SCROLL_NEVER;
import static android.widget.GridView.NO_STRETCH;
import static com.genzzhang.demo.HorizontalLoopGridModel.getAppModel;
import static com.genzzhang.demo.HorizontalLoopGridModel.getBusinessModel;
import static com.genzzhang.demo.HorizontalLoopGridModel.getPhoneModel;
import static com.genzzhang.demo.HorizontalLoopGridView.sItemSize;

/**
 * Created by genzzhang on 2017/4/16.
 */

public class HorizontalLoopGridAdapter extends BaseAdapter{

    private Context mContext;
    private HorizontalLoopGridView mGridView;
    private ArrayList<HorizontalLoopGridModel> mModels;

    private Toast mToast;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public HorizontalLoopGridAdapter(Context context, HorizontalLoopGridView gridView, ArrayList<HorizontalLoopGridModel> models) {
        mContext = context;
        mGridView = gridView;
        mModels = getAdapterModels(models);
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        initGridView();
        gridView.setAdapter(this);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mModels.remove(2);
                notifyDataSetChanged();
                mToast.setText("删除小米的");
                mToast.show();
            }
        }, 16000);
    }

    @Override
    public int getCount() {
        return mModels == null ? 0 : mModels.size();
    }

    @Override
    public Object getItem(int position) {
        return mModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final HorizontalLoopGridModel model = mModels.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_horizonal_loop_grid_item_view, null);
            holder = new ViewHolder();
            holder.titleTv = (TextView) convertView.findViewById(R.id.title);
            holder.tipTv = (TextView) convertView.findViewById(R.id.tip);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        convertView.setBackgroundResource(model.mBgId);
        holder.titleTv.setText(model.mTitle);
        holder.tipTv.setText(model.mTip);
        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mGridView.setNumColumns(mModels.size());
    }

    public static class ViewHolder {
        TextView titleTv;
        TextView tipTv;
    }

    private void initGridView() {
        mGridView.setVerticalSpacing(0);
        mGridView.setHorizontalSpacing(0);
        mGridView.setStretchMode(NO_STRETCH);
        mGridView.setOverScrollMode(OVER_SCROLL_NEVER);
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mGridView.setColumnWidth(sItemSize);
        mGridView.setNumColumns(mModels.size());
        mGridView.scrollTo(sItemSize, 0);
        mGridView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e("setOnItemClickListener", "setOnItemClickListener i=" + i);
                HorizontalLoopGridModel model = mModels.get(i);
                mToast.setText(model.mTitle);
                mToast.show();

            }
        });

    }

    public static ArrayList<HorizontalLoopGridModel> getAdapterModels(ArrayList<HorizontalLoopGridModel> models) {
        ArrayList<HorizontalLoopGridModel> mModels = models;
        if (models == null || models.size() <= 0) {
            mModels = new ArrayList<>();
            mModels.add(getAppModel(1));
            mModels.add(getPhoneModel(2));
            mModels.add(getBusinessModel());
        }
        HorizontalLoopGridModel tmp = mModels.get(0);
        //mModels.add(new HorizontalLoopGridModel("第一个", tmp.mTip, tmp.mBgId));
        mModels.add(tmp);
        tmp = mModels.get(mModels.size() - 2);
        //mModels.add(0, new HorizontalLoopGridModel("最后一个", tmp.mTip, tmp.mBgId));
        mModels.add(0, tmp);
        return mModels;
    }

}
