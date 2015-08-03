package com.pockru.bestizhelper.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pockru.bestizhelper.BaseActivity;
import com.pockru.bestizhelper.BestizBoxDetailActivity;
import com.pockru.bestizhelper.R;
import com.pockru.bestizhelper.data.CommentUserData;
import com.pockru.bestizhelper.data.Constants;

public class CommentListAdapter extends BaseAdapter {

	ArrayList<CommentUserData> commentUserDataList;

	private LayoutInflater inflater;
	
	private Activity activity;

	public CommentListAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		activity = (Activity) context;
	}

	@Override
	public int getCount() {
		if (commentUserDataList == null)
			return 0;
		else
			return commentUserDataList.size();
	}

	@Override
	public Object getItem(int position) {
		if (commentUserDataList == null)
			return null;
		else
			return commentUserDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if(convertView == null){
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_user_comment, null);
			holder.tvCommUserName = (TextView) convertView.findViewById(R.id.txt_user_name);
			holder.tvCommmet  = (TextView) convertView.findViewById(R.id.txt_user_comment);
			holder.tvCommUserAddr = (TextView) convertView.findViewById(R.id.txt_user_address);
			holder.ivDelete = (ImageButton) convertView.findViewById(R.id.iv_delete);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		final CommentUserData data = (CommentUserData) getItem(position);
		
		if(data != null){
			holder.tvCommUserName.setText(data.getUserName());
			holder.tvCommmet.setText(data.getUserComment());
			holder.tvCommUserAddr.setText(data.getUserAddress());
			holder.ivDelete.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					((BaseActivity) activity).requestNetwork(Constants.FLAG_REQ_DELETE_COMMENT, data.getDeleteUrl());
				}
			});
		}
		
		return convertView;
	}

	public void setData(ArrayList<CommentUserData> commentUserDataList) {
		this.commentUserDataList = commentUserDataList;
		notifyDataSetChanged();
	}
	
	class ViewHolder{
		TextView tvCommUserName , tvCommmet , tvCommUserAddr;
		ImageButton ivDelete;
	}

}
