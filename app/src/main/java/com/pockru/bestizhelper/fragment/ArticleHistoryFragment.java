package com.pockru.bestizhelper.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pockru.bestizhelper.BaseActivity;
import com.pockru.bestizhelper.BestizBoxDetailActivity;
import com.pockru.bestizhelper.R;
import com.pockru.bestizhelper.adapter.ArticleHistoryAdapter;
import com.pockru.bestizhelper.data.ArticleDB;
import com.pockru.bestizhelper.data.BoardData;
import com.pockru.bestizhelper.data.Constants;
import com.pockru.bestizhelper.database.DatabaseContract;
import com.pockru.bestizhelper.database.helper.ArticleDatabaseHelper;

import java.util.List;

/**
 * Created by 래형 on 2015-12-13.
 */
public class ArticleHistoryFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private BoardData boardData;
    private int type;

    public static Fragment newInstance(BoardData boardData, int type){
        ArticleHistoryFragment fragment = new ArticleHistoryFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.INTENT_NAME_BOARD_DATA, boardData);
        bundle.putInt(Constants.INTENT_NAME_HISTORY_TYPE, type);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boardData = (BoardData) getArguments().getSerializable(Constants.INTENT_NAME_BOARD_DATA);
        type = getArguments().getInt(Constants.INTENT_NAME_HISTORY_TYPE, ArticleDB.TYPE_VIEW);
    }

    private ListView mListView;
    private ArticleHistoryAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_history, container, false);
        mListView = (ListView) view.findViewById(R.id.lv_history);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new ArticleHistoryAdapter(getActivity(), boardData.id, type);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (isDeleteMode == false) {
            Cursor c= (Cursor) mAdapter.getItem(position);
            BestizBoxDetailActivity.startDetailActivity(getActivity(),
                    c.getString(c.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_URL)),
                    BaseActivity.REQ_CODE_DETAIL_ARTICLE,
                    type == ArticleDB.TYPE_WRITE);
        } else {
            mAdapter.addDeleteList(position);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final Cursor c= (Cursor) mAdapter.getItem(position);
        new AlertDialog.Builder(getActivity())
                .setMessage("해당 히스토리를 삭제하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArticleDatabaseHelper.delete(getActivity(), c.getInt(c.getColumnIndex(DatabaseContract.ArticleTable.KEY_ARTICLE_NUM)));
//                        mAdapter.refreshAdapter();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
        return true;
    }

    private boolean isDeleteMode = false;

    public void setMode(boolean isDeleteMode){
        this.isDeleteMode = isDeleteMode;

        if(isDeleteMode == false) {
            mAdapter.clearDeleteList();
        }
    }

    public boolean isDeleteItemExist() {
        return mAdapter.getDeleteItemCount() > 0;
    }

    public List<String> getDeleteItemList() {
        return mAdapter.getDeleteItemList();
    }
}
