package com.pockru.bestizhelper.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.pockru.bestizhelper.R;

public class ImageViewPagerAdapter extends PagerAdapter {

	Activity activity;
	LayoutInflater inflater;

	ArrayList<String> dataList;

	public ImageViewPagerAdapter(Activity activity, ArrayList<String> dataList) {
		this.activity = activity;
		this.dataList = dataList;
		this.inflater = LayoutInflater.from(activity);
	}
	
	public ImageViewPagerAdapter(Activity activity) {
		this.activity = activity;
		this.dataList = new ArrayList<String>();
		this.inflater = LayoutInflater.from(activity);
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	public void addData(String data) {
		dataList.add(data);
		notifyDataSetChanged();
	}

	public void deleteData(int index) {
		if (dataList.size() > index)
			dataList.remove(index);
		notifyDataSetChanged();
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		FrameLayout layout = (FrameLayout) inflater.inflate(R.layout.item_viewpager_img, null);
//		ImageButton btnClose = (ImageButton) layout.findViewById(R.id.btnClose);
//		btnClose.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				deleteData(position);
//			}
//		});
		
		ImageView iv = (ImageView) layout.findViewById(R.id.imageView1);
		Glide.with(activity).load(dataList.get(position)).into(iv);
		
		container.addView(layout);

		return layout;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);

	}

}
