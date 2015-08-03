package com.pockru.bestizhelper;

import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.pockru.bestizhelper.adapter.ArticleHistoryAdapter;
import com.pockru.bestizhelper.data.ArticleDB;
import com.pockru.bestizhelper.database.helper.ArticleDatabaseHelper;

public class ArticleHistoryActivity extends BaseActivity implements OnItemClickListener, OnItemLongClickListener {

	private ArticleHistoryAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_history);
		
		ListView listView = (ListView) findViewById(R.id.lv_main);
		mAdapter = new ArticleHistoryAdapter(this);
		listView.setAdapter(mAdapter);
		
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
	}

	@Override
	public void getResponse(int resCode, Map<String, List<String>> headers, String html, int flag) {}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
		if (view.getTag() != null) {
			new AlertDialog.Builder(ArticleHistoryActivity.this)
			.setMessage("해당 히스토리를 삭제하시겠습니까?")
			.setPositiveButton("확인", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ArticleDB articleDB = (ArticleDB) view.getTag();
					ArticleDatabaseHelper.delete(ArticleHistoryActivity.this, articleDB.articleNum);
				}
			})
			.setNegativeButton("취소", null)
			.show();
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (view.getTag() != null) {
			
		}
	}
	
}
