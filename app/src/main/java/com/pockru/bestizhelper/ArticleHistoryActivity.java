package com.pockru.bestizhelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;

import com.pockru.bestizhelper.data.ArticleDB;
import com.pockru.bestizhelper.data.BoardData;
import com.pockru.bestizhelper.data.Constants;
import com.pockru.bestizhelper.database.helper.ArticleDatabaseHelper;
import com.pockru.bestizhelper.fragment.ArticleHistoryFragment;

import java.util.List;
import java.util.Map;

public class ArticleHistoryActivity extends BaseActivity {

    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("히스토리");
        }

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mBoardData = (BoardData) getIntent().getSerializableExtra(Constants.INTENT_NAME_BOARD_DATA);

        mTabHost.addTab(
                mTabHost.newTabSpec(String.valueOf(ArticleDB.TYPE_VIEW)).setIndicator("내가 본 게시물", null),
                ArticleHistoryFragment.class, createBundle(mBoardData, ArticleDB.TYPE_VIEW));

        mTabHost.addTab(
                mTabHost.newTabSpec(String.valueOf(ArticleDB.TYPE_WRITE)).setIndicator("내가 작성한 게시물", null),
                ArticleHistoryFragment.class, createBundle(mBoardData, ArticleDB.TYPE_WRITE));

        mTabHost.addTab(
                mTabHost.newTabSpec(String.valueOf(ArticleDB.TYPE_FAVORITE)).setIndicator("즐겨찾기", null),
                ArticleHistoryFragment.class, createBundle(mBoardData, ArticleDB.TYPE_FAVORITE));
    }

    private Bundle createBundle(BoardData data, int type) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.INTENT_NAME_BOARD_DATA, data);
        bundle.putInt(Constants.INTENT_NAME_HISTORY_TYPE, type);
        return bundle;
    }

    public static void startActivity(Activity activity, BoardData data) {
        Intent intent = new Intent(activity, ArticleHistoryActivity.class);
        intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, data);
        activity.startActivity(intent);
    }

    @Override
    public void onResponse(int resCode, Map<String, List<String>> headers, String html, int flag) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    private MenuItem deleteAll;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu == null) {
            return false;
        }

        deleteAll = menu.findItem(R.id.sub_menu_delete_all);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final ArticleHistoryFragment fragment = (ArticleHistoryFragment) getSupportFragmentManager().findFragmentByTag(mTabHost.getCurrentTabTag());

        switch (item.getItemId()) {
            case R.id.sub_menu_delete: {
                boolean isDeleteMode = !item.isChecked();

                if (isDeleteMode) {
                    changeDeleteMode(fragment, item, true);
                } else {
                    if (fragment.isDeleteItemExist()) {
                        new AlertDialog.Builder(ArticleHistoryActivity.this)
                                .setTitle("알림")
                                .setMessage("선택한 게시물을 삭제하시겠습니까?")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ArticleDatabaseHelper.delete(getApplicationContext(), fragment.getDeleteItemList());
                                        changeDeleteMode(fragment, item, false);
                                    }
                                })
                                .setNegativeButton("취소", null)
                                .show();
                    } else {
                        changeDeleteMode(fragment, item, false);
                    }
                }
                return true;
            }
            case R.id.sub_menu_delete_all: {
                new AlertDialog.Builder(ArticleHistoryActivity.this)
                        .setTitle("알림")
                        .setMessage("게시물을 전부 삭제하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ArticleDatabaseHelper.deleteAllByType(getApplicationContext(),
                                        mBoardData.id,
                                        Integer.valueOf(mTabHost.getCurrentTabTag()));
                                changeDeleteMode(fragment, item, false);
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void changeDeleteMode(ArticleHistoryFragment fragment, MenuItem item, boolean isDeleteMode) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isDeleteMode ? "삭제" : "히스토리");
        }
        fragment.setMode(isDeleteMode);
        item.setIcon(ContextCompat.getDrawable(this, isDeleteMode ? R.drawable.ic_done_black_24dp : R.drawable.ic_delete_black_24dp));
        item.setChecked(isDeleteMode);
        if (deleteAll != null) {
            deleteAll.setVisible(isDeleteMode);
        }
    }
}
