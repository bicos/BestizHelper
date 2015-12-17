package com.pockru.bestizhelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;

import com.pockru.bestizhelper.data.ArticleDB;
import com.pockru.bestizhelper.data.BoardData;
import com.pockru.bestizhelper.data.Constants;
import com.pockru.bestizhelper.fragment.ArticleHistoryFragment;

import java.util.List;
import java.util.Map;

public class ArticleHistoryActivity extends BaseActivity {

	private FragmentTabHost mTabHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_article_history);

		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

		BoardData data = (BoardData) getIntent().getSerializableExtra(Constants.INTENT_NAME_BOARD_DATA);

		mTabHost.addTab(
				mTabHost.newTabSpec("viewArticle").setIndicator("내가 본 게시물", null),
				ArticleHistoryFragment.class, createBundle(data, ArticleDB.TYPE_VIEW));

		mTabHost.addTab(
				mTabHost.newTabSpec("writeArticle").setIndicator("내가 작성한 게시물", null),
				ArticleHistoryFragment.class, createBundle(data, ArticleDB.TYPE_WRITE));

		mTabHost.addTab(
				mTabHost.newTabSpec("favoriteArticle").setIndicator("즐겨찾기", null),
				ArticleHistoryFragment.class, createBundle(data, ArticleDB.TYPE_WRITE));
	}

	private Bundle createBundle(BoardData data, int type){
		Bundle bundle = new Bundle();
		bundle.putSerializable(Constants.INTENT_NAME_BOARD_DATA, data);
		bundle.putInt(Constants.INTENT_NAME_HISTORY_TYPE, type);
		return bundle;
	}

	public static void startActivity(Activity activity, BoardData data){
		Intent intent = new Intent(activity, ArticleHistoryActivity.class);
		intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, data);
		activity.startActivity(intent);
	}

	@Override
	public void onResponse(int resCode, Map<String, List<String>> headers, String html, int flag) {

	}
	
}
