package com.genzzhang.demo.listviewanimation;

import java.util.ArrayList;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.genzzhang.demo.R;
import com.genzzhang.demo.app.AbsActivity;

public class ListviewAniActivity extends AbsActivity {

	private ListView mListView;
	private ArrayList<String> mDescri = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_listview_ani);
		mListView = (ListView) findViewById(R.id.listview);
		mListView.setAdapter(new ArrayAdapter<String>(mContext,
				android.R.layout.simple_list_item_1, mDescri));
		for (int i = 0; i < 27; i++) {
			mDescri.add("测试: " + i);
		}
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AniHelper.animateSwipe(mDescri,(BaseAdapter) mListView.getAdapter(), mListView, view);
			}
		});
	}

}
