package com.genzzhang.demo.nestscrollsuction;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.genzzhang.demo.R;

public class RefreshListView extends ListView {

	private final static String TAG = "AutoRefreshListView";

	public enum State {
		REFRESHING, RESET,
	}

	public enum Mode {
		START, END, BOTH,
	}

	public interface OnRefreshListener {
		//顶部刷新来的数据
		void onRefreshFromStart();
		//顶部刷新来的数据
		void onRefreshFromEnd();
	}

	private OnRefreshListener refreshListener;

	private State state = State.RESET;
	private Mode mode = Mode.BOTH;
	private Mode currentMode = Mode.START;

	private ViewGroup refreshHeader;
	private ViewGroup refreshFooter;
	private int refreshHeight;
	private TextView headerTip;
	private TextView footerTip;
	private View headerView;
	private View footerView;

	public RefreshListView(Context context) {
		super(context);
		init(context);
	}

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public RefreshListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public void setOnRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
	}

	private void init(Context context) {
		addRefreshView(context);
		state = State.RESET;
	}

	private void addRefreshView(Context context) {
		// 头部
		refreshHeader = (ViewGroup) inflate(context,
				R.layout.layout_listview_fresh_more, null);
		headerTip = (TextView) refreshHeader.findViewById(R.id.refresh_tip);
		headerView = refreshHeader.findViewById(R.id.refresh_loading);
		int width = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		int height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		refreshHeader.measure(width, height);
		refreshHeight = refreshHeader.getMeasuredHeight();
		refreshHeader.setPadding(0, -refreshHeight, 0, 0);
		addHeaderView(refreshHeader, null, false);

		// 尾部，一样的布局，高度一样的
		refreshFooter = (ViewGroup) inflate(context,
				R.layout.layout_listview_fresh_more, null);
		footerTip = (TextView) refreshFooter.findViewById(R.id.refresh_tip);
		footerView = refreshFooter.findViewById(R.id.refresh_loading);
		refreshFooter.setPadding(0, 0, 0, -refreshHeight);
		addFooterView(refreshFooter, null, false);
	}

	// 注意
	private void updateRefreshView() {
		switch (state) {
		case REFRESHING:
			if (currentMode == Mode.START) {
				dealRefreshView(RefreshStatus.HeaderRefreshing);
				dealRefreshView(RefreshStatus.FooterReset);
			} else {
				dealRefreshView(RefreshStatus.HeaderReset);
				dealRefreshView(RefreshStatus.FooterRefreshing);
			}
			break;
		case RESET:
			dealRefreshView(RefreshStatus.HeaderReset);
			dealRefreshView(RefreshStatus.FooterReset);
			break;
		}
	}

	public enum RefreshStatus {
		HeaderReset, HeaderRefreshing, FooterReset, FooterRefreshing,
	}

	private void dealRefreshView(RefreshStatus status) {
		switch (status) {
		case HeaderReset:
			// 恢复头部
			refreshHeader.setPadding(0, -refreshHeight, 0, 0);
			headerTip.setText("下拉刷新...");
			headerView.setVisibility(View.GONE);
			break;
		case HeaderRefreshing:
			// 处理头部刷新
			setSelection(0);// 防止用户下拉再往上滑动，出现一个滑动距离
			refreshHeader.setPadding(0, 0, 0, 0);
			headerTip.setText("正在刷新");
			headerView.setVisibility(View.VISIBLE);
			break;
		case FooterReset:
			// 底部恢复
			refreshFooter.setPadding(0, 0, 0, -refreshHeight);
			footerTip.setText("松开刷新...");
			footerView.setVisibility(View.GONE);
			break;
		case FooterRefreshing:
			// 处理底部刷新
			refreshFooter.setPadding(0, 0, 0, 0);
			footerTip.setText("正在加载中");
			footerView.setVisibility(View.VISIBLE);
			break;
		}
	}

	/**
	 * 加载完成
	 */
	public void onRefreshComplete(int count, int requestCount) {
		state = State.RESET;
		if (currentMode == Mode.START) {
			setSelection(getHeaderViewsCount());
		}
		updateRefreshView();
	}

	/**
	 * onStartTouch处理顶部下拉的 onEndTouch处理底部上拉
	 */
	private boolean isStartBeingDragged = false;
	private int startY = 0;

	private boolean isEndBeingDragged = false;
	private int endY = 0;
	// 处理底部上滑提前加载
	private boolean isEndStartY = false;
	private int endStartY = 0;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (Build.VERSION.SDK_INT < 11) {
			try {
				return onTouchEventInternal(event);
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return onTouchEventInternal(event);
		}
	}

	// down事件经常被拦了
	private boolean onTouchEventInternal(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			onEndTouchBegin(event);
			onStartTouchBegin(event);
			break;
		case MotionEvent.ACTION_MOVE:
			onEndTouchMove(event);
			onStartTouchMove(event);
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			onEndTouchEnd(event);
			onStartTouchEnd();
			// 不要耦合到上面onEndTouch里面，updateRefreshView已经更改了上面需要判断的一些属性
			updateRefreshView();
			break;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 顶部上拉
	 */
	private void onStartTouchBegin(MotionEvent event) {
		int firstItemIndex = getFirstVisiblePosition();
		if (!isStartBeingDragged && state == State.RESET && firstItemIndex <= getHeaderViewsCount()) {
			isStartBeingDragged = true;
			startY = (int) event.getY();
		}
	}

	private void onStartTouchMove(MotionEvent event) {
		// 再次检查，按下未必满足，滑动过程中也会拉过头，有时候down未必执行
		onStartTouchBegin(event);
		if (!isStartBeingDragged) {
			return;
		}
		int offsetY = (int) (event.getY() - startY);
		offsetY = getOffsetY(offsetY);
		refreshHeader.setPadding(0, offsetY - refreshHeight, 0, 0);
		int headerShowHeight = refreshHeight + refreshHeader.getPaddingTop() + refreshHeader.getTop();
		if (headerShowHeight >= refreshHeight) {
			headerTip.setText("松开刷新...");
		} else {
			headerTip.setText("下拉刷新...");
		}
	}

	// headerShowHeight > 0都要重新定位到第一。。。offsetY偏移/2
	private void onStartTouchEnd() {
		int headerShowHeight = refreshHeight + refreshHeader.getPaddingTop() + refreshHeader.getTop();
		if (headerShowHeight >= refreshHeight) {
			currentMode = Mode.START;
			state = State.REFRESHING;
			refreshListener.onRefreshFromStart();
		} else if (headerShowHeight > 0) {
			setSelection(getHeaderViewsCount());
		}
		isStartBeingDragged = false;
	}

	/**
	 * 底部下拉
	 */
	private void onEndTouchBegin(MotionEvent event) {
		if (!isEndStartY) {
			endStartY = (int) event.getY();
			isEndStartY = true;
		}

		// 到底部
		boolean reachBottom = getLastVisiblePosition() >= getCount() - 1;
		if (!isEndBeingDragged && state == State.RESET && mode != Mode.START && reachBottom && refreshListener != null) {
			endY = (int) event.getY();
			isEndBeingDragged = true;
		}
	}

	private void onEndTouchMove(MotionEvent event) {
		if (!isEndStartY) {
			endStartY = (int) event.getY();
		}

		// 再次检查，有时候down未必执行 滑动过程
		onEndTouchBegin(event);
		if (!isEndBeingDragged) {
			return;
		}
		int offsetY = (int) (endY - event.getY());
		offsetY = getOffsetY(offsetY);
		refreshFooter.setPadding(0, 0, 0, offsetY - refreshHeight);

	}

	private void onEndTouchEnd(MotionEvent event) {
		if (isEndStartY) {
			int offsetY = (int) (endStartY - event.getY());
			if (offsetY > refreshHeight * 0.4 && state == State.RESET && mode != Mode.START && refreshListener != null) {
				// -7提前6个加载 -1到底 一屏大概6个。
				boolean load = getLastVisiblePosition() >= getCount() - 7;
				if (load) {
					state = State.REFRESHING;
					currentMode = Mode.END;
					refreshListener.onRefreshFromEnd();
				}
			}
			isEndStartY = false;
		}

		isEndBeingDragged = false;
	}

	private int getOffsetY(int offsetY) {
		int y = 0;
		if (offsetY <= 0) {
			y = 0;
		} else if (offsetY <= refreshHeight) {
			y = offsetY;
		} else {
			y = refreshHeight + (offsetY - refreshHeight) / 2;
		}
		return y;
	}

}
