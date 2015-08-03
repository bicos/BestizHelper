package com.pockru.bestizhelper.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.BaseAdapter;

public abstract class AbstractAdapter<T> extends BaseAdapter {

	protected ArrayList<T> dataList = new ArrayList<T>();
	protected Context context;
	
	public AbstractAdapter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public T getItem(int arg0) {
		return dataList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	public void setDataList(ArrayList<T> dataList){
		this.dataList.clear();
		this.dataList.addAll(dataList);
		notifyDataSetChanged();
	}
	
	public List<T> getDataList(){
		return this.dataList;
	}
	
	public void addData(T data){
		this.dataList.add(data);
		notifyDataSetChanged();
	}
	
	public void addAllData(ArrayList<T> dataList){
		this.dataList.addAll(dataList);
		notifyDataSetChanged();
	}
	
	public void clearData(){
		this.dataList.clear();
		notifyDataSetChanged();
	}	

}
