package com.pockru.bestizhelper.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pockru.bestizhelper.R;
import com.pockru.bestizhelper.data.ArticleData;

public class ArticleListAdapter extends AbstractAdapter<ArticleData> {
	public ArticleListAdapter(Context context) {
		super(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_main_article, parent , false);
			holder = new ViewHolder();
			holder.tvTitle = (TextView) convertView.findViewById(R.id.txt_main_atc_title);
			holder.tvUser = (TextView) convertView.findViewById(R.id.txt_main_atc_user);
			holder.tvDate = (TextView) convertView.findViewById(R.id.txt_main_atc_date);
			holder.tvHit = (TextView) convertView.findViewById(R.id.txt_main_atc_hit);
			holder.tvVote = (TextView) convertView.findViewById(R.id.txt_main_atc_vote);
			holder.tvComment = (TextView) convertView.findViewById(R.id.txt_main_atc_comment);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (dataList.size() > position) {
			ArticleData data = dataList.get(position);
			if (data != null) {
				holder.tvTitle.setText(Html.fromHtml(data.getAtcTitle()));
				holder.tvTitle.setTag(data.getAtcLink());
				holder.tvUser.setText(data.getAtcUser());
				holder.tvDate.setText(data.getAtcDate());
				holder.tvHit.setText(data.getAtcHit());
				holder.tvVote.setText(data.getAtcVote());
				holder.tvComment.setText(data.getAtcComment());
			}
		}
		return convertView;
	}
	
	private class ViewHolder {
		TextView tvTitle, tvUser, tvComment, tvDate, tvHit, tvVote;
	}

}
