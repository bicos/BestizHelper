package com.pockru.bestizhelper.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pockru.bestizhelper.R;
import com.pockru.bestizhelper.data.ArticleDB;
import com.pockru.bestizhelper.database.helper.ArticleDatabaseHelper;

public class ArticleHistoryAdapter extends AbstractAdapter<ArticleDB> {
	public ArticleHistoryAdapter(Context context) {
		super(context);
		setDataList(ArticleDatabaseHelper.getAllData(context));
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

		ArticleDB data = getItem(position);
		if (data != null) {
			holder.tvTitle.setText(Html.fromHtml(data.articleTitle));
			holder.tvTitle.setTag(data.articleUrl);
			holder.tvUser.setText(data.articleUser);
			holder.tvDate.setText(data.articleDate);
			holder.tvHit.setText(String.valueOf(data.articleHit));
			holder.tvVote.setText(String.valueOf(data.articleVote));
			holder.tvComment.setText(String.valueOf(data.articleComment));
		}
		convertView.setTag(data);
		
		return convertView;
	}
	
	private class ViewHolder {
		TextView tvTitle, tvUser, tvComment, tvDate, tvHit, tvVote;
	}

}
