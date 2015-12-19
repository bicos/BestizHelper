package com.pockru.bestizhelper.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.pockru.bestizhelper.R;
import com.pockru.bestizhelper.database.DatabaseContract;

import java.util.ArrayList;
import java.util.List;

public class ArticleHistoryAdapter extends CursorAdapter {
	private Activity mContext;
	private String mBoardNo;
	private int mType;

	public ArticleHistoryAdapter(Activity context, String boardNo, int type) {
		super(context,getDefaultCursor(context, boardNo, type), true);
		mContext = context;
		mBoardNo = boardNo;
		mType = type;
	}

	private static Cursor getDefaultCursor(Context context, String boardNo, int type) {
		return context.getContentResolver().query(DatabaseContract.ArticleTable.CONTENT_URI,
				null,
				DatabaseContract.ArticleTable.KEY_ARTICLE_URL + " LIKE ? AND (" + DatabaseContract.ArticleTable.KEY_ARTICLE_TYPE + "&" + type + ") == " + type,
				new String[]{"%id=" + boardNo + "%"},
				DatabaseContract.ArticleTable._ID + " DESC");
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.item_main_article, parent, false);
	}

	@Override
	public void bindView(View view, Context context, final Cursor cursor) {
		final ViewHolder holder = new ViewHolder(view);
		holder.tvTitle.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_TITLE))));
		holder.tvUser.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_USER)));
		holder.tvDate.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_DATE)));
		holder.tvHit.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_HIT))));
		holder.tvVote.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_VOTE))));
		holder.tvComment.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_COMMENT))));

		if (deleteList.contains(cursor.getString(cursor.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_NUM)))){
			view.setBackgroundColor(Color.parseColor("#f4f4f4"));
		} else {
			view.setBackgroundResource(0);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void refreshAdapter(){
		Cursor c = getDefaultCursor(mContext, mBoardNo, mType);
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD){
			swapCursor(c);
		} else {
			changeCursor(c);
		}
		notifyDataSetChanged();
	}

	private List<String> deleteList = new ArrayList<>();

	public void addDeleteList(int position) {
		Cursor c = (Cursor) getItem(position);
		String articleNum = c.getString(c.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_NUM));
		if(deleteList.contains(articleNum)) {
			deleteList.remove(articleNum);
		} else {
			deleteList.add(articleNum);
		}
		notifyDataSetChanged();
	}

	public void clearDeleteList() {
		deleteList.clear();
		notifyDataSetChanged();
	}

	public int getDeleteItemCount() {
		return deleteList.size();
	}

	public List<String> getDeleteItemList() {
		return deleteList;
	}

	private class ViewHolder {
		TextView tvTitle, tvUser, tvComment, tvDate, tvHit, tvVote;
		public ViewHolder(View view) {
			tvTitle = (TextView) view.findViewById(R.id.txt_main_atc_title);
			tvUser = (TextView) view.findViewById(R.id.txt_main_atc_user);
			tvDate = (TextView) view.findViewById(R.id.txt_main_atc_date);
			tvHit = (TextView) view.findViewById(R.id.txt_main_atc_hit);
			tvVote = (TextView) view.findViewById(R.id.txt_main_atc_vote);
			tvComment = (TextView) view.findViewById(R.id.txt_main_atc_comment);
		}
	}

}
