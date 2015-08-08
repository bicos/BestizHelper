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

public class ArticleHistoryActivity extends BaseActivity {

	private ArticleHistoryAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_history);
		
		ListView listView = (ListView) findViewById(R.id.lv_main);
		mAdapter = new ArticleHistoryAdapter(this);
		listView.setAdapter(mAdapter);
	}

	@Override
	public void onResponse(int resCode, Map<String, List<String>> headers, String html, int flag) {}
	
}
