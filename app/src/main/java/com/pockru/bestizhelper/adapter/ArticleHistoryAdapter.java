package com.pockru.bestizhelper.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.pockru.bestizhelper.R;
import com.pockru.bestizhelper.data.ArticleDB;
import com.pockru.bestizhelper.database.DatabaseContract;
import com.pockru.bestizhelper.database.helper.ArticleDatabaseHelper;

public class ArticleHistoryAdapter extends CursorAdapter {
	private Context mContext;

	public ArticleHistoryAdapter(Context context) {
		super(context, context.getContentResolver().query(DatabaseContract.ArticleTable.CONTENT_URI, null, null, null, null), false);
		mContext = context;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.item_main_article, parent , false);
	}

	@Override
	public void bindView(View view, Context context, final Cursor cursor) {
		ViewHolder holder = new ViewHolder(view);
		holder.tvTitle.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_TITLE))));
		holder.tvUser.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_USER)));
		holder.tvDate.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_DATE)));
		holder.tvHit.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_HIT))));
		holder.tvVote.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_VOTE))));
		holder.tvComment.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_COMMENT))));

		view.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				new AlertDialog.Builder(mContext)
						.setMessage("해당 히스토리를 삭제하시겠습니까?")
						.setPositiveButton("확인", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								ArticleDatabaseHelper.delete(mContext, cursor.getInt(cursor.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_NUM)));
								refreshAdapter();
							}
						})
						.setNegativeButton("취소", null)
						.show();

				return false;
			}
		});
	}

	private void refreshAdapter(){
		Cursor c = mContext.getContentResolver().query(DatabaseContract.ArticleTable.CONTENT_URI, null, null, null, null);
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD){
			swapCursor(c);
		} else {
			changeCursor(c);
		}
		notifyDataSetChanged();
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
