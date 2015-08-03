package com.pockru.bestizhelper.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import com.pockru.utils.Utils;

public class TouchWebView extends WebView {

	private OnChangedScrollListener listener;

	private boolean isEdge = false;

	public TouchWebView(Context context) {
		super(context);
		init();
	}

	public TouchWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TouchWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			requestDisallowInterceptTouchEvent(true);
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (isEdge) {
				requestDisallowInterceptTouchEvent(false);
			} else {
				requestDisallowInterceptTouchEvent(true);
			}
		}

		return super.onTouchEvent(event);
	}

	public void setOnChangedScrollListener(OnChangedScrollListener listener) {
		this.listener = listener;
	}

	private void init() {
		listener = new OnChangedScrollListener() {

			@Override
			public void onChangedScroll(int state, TouchWebView webview) {
				if (STATE_EDGE == state) {
					isEdge = true;
				} else {
					isEdge = false;
				}
			}
		};
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {

		int move = 0;

		if (Utils.isOverCurrentAndroidVersion(Build.VERSION_CODES.GINGERBREAD) <= 0) {
			if (listener != null) {
				int height = (int) Math.floor(this.getContentHeight() * this.getScale());
				int webViewHeight = this.getMeasuredHeight();

				if (this.getScrollY() + webViewHeight >= height) {
					listener.onChangedScroll(OnChangedScrollListener.STATE_EDGE, this);
				} else {
					move = t - oldt;

					if (Math.abs(move) > 10) {
						if (move > 0) {
							listener.onChangedScroll(OnChangedScrollListener.STATE_DOWN, this);
						} else if (move < 0) {
							listener.onChangedScroll(OnChangedScrollListener.STATE_UP, this);
						}
					}
				}
			}
		}

		super.onScrollChanged(l, t, oldl, oldt);
	}

	@SuppressLint("NewApi")
	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX,
			int maxOverScrollY, boolean isTouchEvent) {

		if (Utils.isOverCurrentAndroidVersion(Build.VERSION_CODES.GINGERBREAD) > 0) {

			if (listener != null) {
				if (scrollRangeY <= scrollY + deltaY) {
					listener.onChangedScroll(OnChangedScrollListener.STATE_EDGE, this);
				} else {
					if (Math.abs(deltaY) > 10) {
						if (deltaY > 0) {
							listener.onChangedScroll(OnChangedScrollListener.STATE_DOWN, this);
						} else if (deltaY < 0) {
							listener.onChangedScroll(OnChangedScrollListener.STATE_UP, this);
						}
					}
				}
			}
		}

		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
	}

	public interface OnChangedScrollListener {

		public static final int STATE_EDGE = 0;
		public static final int STATE_UP = 1;
		public static final int STATE_DOWN = 2;

		abstract void onChangedScroll(int state, TouchWebView webview);
	}
}
