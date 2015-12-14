package com.pockru.bestizhelper.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.firebase.client.Firebase;
import com.pockru.bestizhelper.R;
import com.pockru.bestizhelper.adapter.CommentAlarmAdapter;
import com.pockru.firebase.UrlConstants;

/**
 * Created by 래형 on 2015-12-14.
 */
public class CommentAlarmFragment extends Fragment{

    public static final String TAG = CommentAlarmFragment.class.getSimpleName();

    public static final String PARAM_TITLE = "title";
    public static final String PARAM_LINK = "link";
    public static final String PARAM_BOARD_ID = "boardId";
    public static final String PARAM_ARTICLE_NO = "articleNo";

    public static CommentAlarmFragment getInstance(String title, String link, String boardId, String articleNo){
        CommentAlarmFragment fragment = new CommentAlarmFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_TITLE, title);
        bundle.putString(PARAM_LINK, link);
        bundle.putString(PARAM_BOARD_ID, boardId);
        bundle.putString(PARAM_ARTICLE_NO, articleNo);
        fragment.setArguments(bundle);
        return fragment;
    }

    private ExpandableListView listView;
    private CommentAlarmAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_alarm, container, false);
        listView = (ExpandableListView) view.findViewById(R.id.lv_comment_alarm);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String title = getArguments().getString(PARAM_TITLE);
        String link = getArguments().getString(PARAM_LINK);
        String boardId = getArguments().getString(PARAM_BOARD_ID);
        String articleNo = getArguments().getString(PARAM_ARTICLE_NO);

        Log.i(TAG, "title : "+ title + ",link : "+link+", boardId : "+boardId + ", articleNo : "+articleNo);

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(link) || TextUtils.isEmpty(boardId)) {
            return;
        }

        Firebase query = new Firebase(UrlConstants.FIREBASE_URL).child(boardId).child(articleNo);
        Log.i("test", "query : "+query.getPath());
        adapter = new CommentAlarmAdapter(title, link, query);
        listView.setAdapter(adapter);
    }

    public void addArticle(String title, String link, String boardId, String articleNo){
        if (adapter != null) {
            Firebase query = new Firebase(UrlConstants.FIREBASE_URL).child(boardId).child(articleNo);
            adapter.addGroupData(new CommentAlarmAdapter.CommentGroupData(adapter, title,link, query));
        }
    }

}
