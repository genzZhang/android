package com.genzzhang.demo.horizontalloopview;

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

import com.genzzhang.demo.R;

import java.util.ArrayList;

import static android.view.View.OVER_SCROLL_NEVER;
import static android.widget.GridView.NO_STRETCH;
import static com.genzzhang.demo.horizontalloopview.HorizontalLoopView.sItemSize;

/**
 * Created by genzzhang on 2017/4/16.
 */

public class HorizontalLoopAdapter extends BaseAdapter{

    private Context mContext;
    private HorizontalLoopView mGridView;
    private ArrayList<HorizontalLoopModel> mModels;

    private Toast mToast;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public HorizontalLoopAdapter(Context context, HorizontalLoopView gridView, ArrayList<HorizontalLoopModel> models) {
        mContext = context;
        mGridView = gridView;
        mModels = getAdapterModels(models);
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        initGridView();
        gridView.setAdapter(this);
        //仅仅测试，放在这里，方便而已
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mModels.remove(mModels.size() - 1);
                notifyDataSetChanged();
                mToast.setText("删除最后一个");
                mToast.show();
            }
        }, 8000);
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
        final HorizontalLoopModel model = mModels.get(position);
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
                HorizontalLoopModel model = mModels.get(i);
                mToast.setText(model.mTitle);
                mToast.show();

            }
        });

    }

    public static ArrayList<HorizontalLoopModel> getAdapterModels(ArrayList<HorizontalLoopModel> models) {
        ArrayList<HorizontalLoopModel> mModels = models;
        if (models == null || models.size() <= 0) {
            mModels = new ArrayList<>();
            mModels.add(HorizontalLoopModel.getAppModel(1));
            mModels.add(HorizontalLoopModel.getPhoneModel(2));
            mModels.add(HorizontalLoopModel.getGameModel());
            mModels.add(HorizontalLoopModel.getBikeModel());
            mModels.add(HorizontalLoopModel.getBusinessModel());
        }
        HorizontalLoopModel tmp = mModels.get(0);
        mModels.add(tmp);
        tmp = mModels.get(mModels.size() - 2);
        mModels.add(0, tmp);
        return mModels;
    }

}
