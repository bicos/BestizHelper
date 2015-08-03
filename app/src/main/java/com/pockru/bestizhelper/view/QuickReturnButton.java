package com.pockru.bestizhelper.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.RelativeLayout;

public class QuickReturnButton extends Button implements OnScrollListener {

	private class ChildDescriptor {
		public int index;
		public int total;
		public int drawable;

		public ChildDescriptor(int index) {
			this.index = index;
		}

		public int getApproximateScrollPosition() {
			return index * total + (total - drawable);
		}
	}

	public int MAX_HEIGHT_DP = 60;
	public int MIN_HEIGHT_DP = 0;

	public static final int SCROLL_DIRECTION_INVALID = 0;
	public static final int SCROLL_DIRECTION_UP = 1;
	public static final int SCROLL_DIRECTION_DOWN = 2;

	private int direction = SCROLL_DIRECTION_INVALID;

	private ChildDescriptor lastchild;

	private OnScrollListener onscrolllistener;

	private int maxheight;

	private int minheight;

	public QuickReturnButton(Context context) {
		super(context);
		calculateMinMax();
	}

	public QuickReturnButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		calculateMinMax();
	}

	private void calculateMinMax() {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		maxheight = (int) (metrics.density * (float) MAX_HEIGHT_DP);
		minheight = (int) (metrics.density * (float) MIN_HEIGHT_DP);
	}

	private void adjustHeight(int howmuch, int max, int min) {

		if ((howmuch < 0) && (direction != SCROLL_DIRECTION_UP)) {
			direction = SCROLL_DIRECTION_UP;
			return;
		} else if ((howmuch > 20) && (direction != SCROLL_DIRECTION_DOWN)) {
			direction = SCROLL_DIRECTION_DOWN;
			return;
		}

		int current = getHeight();
		current += howmuch;
		if (current < min) {
			current = min;
		} else if (current > max) {
			current = max;
		}

		RelativeLayout.LayoutParams f = (RelativeLayout.LayoutParams) getLayoutParams();
		if (f.height != current) {
			f.height = current;
			setLayoutParams(f);
		}

		if (direction == SCROLL_DIRECTION_UP && Math.abs(f.topMargin) <= current) {
			f.bottomMargin = -100;
			setLayoutParams(f);

		} else if (direction == SCROLL_DIRECTION_DOWN) {

			if (f.bottomMargin != 0) {
				f.bottomMargin = 0;
				setLayoutParams(f);
			}
		}

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (onscrolllistener != null) {
			onscrolllistener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		ChildDescriptor currentchild = getFirstChildItemDescriptor(view, firstVisibleItem);
		try {

			adjustHeight(lastchild.getApproximateScrollPosition() - currentchild.getApproximateScrollPosition(), maxheight, minheight);
			lastchild = currentchild;
		} catch (NullPointerException e) {

			lastchild = currentchild;
		} catch (Exception e) {
		}

		if (onscrolllistener != null) {
			onscrolllistener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}

	private ChildDescriptor getFirstChildItemDescriptor(AbsListView view, int index) {
		ChildDescriptor h = new ChildDescriptor(index);
		try {
			Rect r = new Rect();
			View child = view.getChildAt(0);
			child.getDrawingRect(r);
			h.total = r.height();
			view.getChildVisibleRect(child, r, null);
			h.drawable = r.height();
			return h;
		} catch (Exception e) {
		}
		return null;
	}

	public void attach(AbsListView view) {
		view.setOnScrollListener(this);
	}

	public void setOnScrollListener(OnScrollListener l) {
		onscrolllistener = l;
	}
}
