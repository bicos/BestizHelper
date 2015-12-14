package com.pockru.bestizhelper.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.pockru.bestizhelper.R;
import com.pockru.bestizhelper.data.CommentData;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 래형 on 2015-12-14.
 */
public class CommentAlarmAdapter extends BaseExpandableListAdapter {

    private List<CommentGroupData> mGroupList;

    public static class CommentGroupData {
        public CommentAlarmAdapter mAdapter;
        public String mLink;
        public String mTitle;
        public Query mRef;
        public ChildEventListener mListener;
        public List<CommentData> mModels;
        public List<String> mKeys;

        public CommentGroupData(CommentAlarmAdapter adapter, String title, String link, Query query) {
            mAdapter = adapter;
            setQuery(query);
            mTitle = title;
            mLink = link;
            mModels = new ArrayList<>();
            mKeys = new ArrayList<>();
        }

        private void setQuery(Query query) {
            mRef = query;
            // Look for all child events. We will then map them to our own internal ArrayList, which backs ListView
            mListener = this.mRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    if (dataSnapshot != null) {
                        Log.i("test", "dataSnapshot : " + dataSnapshot.toString());
                    }

                    CommentData model = dataSnapshot.getValue(CommentData.class);
                    String key = dataSnapshot.getKey();

                    // Insert into the correct location, based on previousChildName
                    if (previousChildName == null) {
                        mModels.add(0, model);
                        mKeys.add(0, key);
                    } else {
                        int previousIndex = mKeys.indexOf(previousChildName);
                        int nextIndex = previousIndex + 1;
                        if (nextIndex == mModels.size()) {
                            mModels.add(model);
                            mKeys.add(key);
                        } else {
                            mModels.add(nextIndex, model);
                            mKeys.add(nextIndex, key);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot != null) {
                        Log.i("test", "dataSnapshot : " + dataSnapshot.toString());

                    }
                    // One of the mModels changed. Replace it in our list and name mapping
                    String key = dataSnapshot.getKey();
                    CommentData newModel = dataSnapshot.getValue(CommentData.class);
                    int index = mKeys.indexOf(key);

                    mModels.set(index, newModel);

                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        Log.i("test", "dataSnapshot : " + dataSnapshot.toString());

                    }
                    // A model was removed from the list. Remove it from our list and the name mapping
                    String key = dataSnapshot.getKey();
                    int index = mKeys.indexOf(key);

                    mKeys.remove(index);
                    mModels.remove(index);

                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    if (dataSnapshot != null) {
                        Log.i("test", "dataSnapshot : " + dataSnapshot.toString());

                    }
                    // A model changed position in the list. Update our list accordingly
                    String key = dataSnapshot.getKey();
                    CommentData newModel = dataSnapshot.getValue(CommentData.class);
                    int index = mKeys.indexOf(key);
                    mModels.remove(index);
                    mKeys.remove(index);
                    if (previousChildName == null) {
                        mModels.add(0, newModel);
                        mKeys.add(0, key);
                    } else {
                        int previousIndex = mKeys.indexOf(previousChildName);
                        int nextIndex = previousIndex + 1;
                        if (nextIndex == mModels.size()) {
                            mModels.add(newModel);
                            mKeys.add(key);
                        } else {
                            mModels.add(nextIndex, newModel);
                            mKeys.add(nextIndex, key);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e("FirebaseListAdapter", "Listen was cancelled, no more updates will occur");
                }

            });
        }
    }

    public CommentAlarmAdapter(String title, String link, Query query) {
        mGroupList = new ArrayList<>();
        mGroupList.add(new CommentGroupData(this, title, link, query));
    }

    public void addGroupData(CommentGroupData groupData) {
        mGroupList.add(groupData);
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return mGroupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return getGroup(groupPosition).mModels.size();
    }

    @Override
    public CommentGroupData getGroup(int groupPosition) {
        return mGroupList != null && mGroupList.size() > groupPosition ?
                mGroupList.get(groupPosition) :
                null;
    }

    @Override
    public CommentData getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition).mModels.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder holder;
        if (convertView != null) {
            holder = (GroupViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_group, parent, false);
            holder = new GroupViewHolder(convertView);
            convertView.setTag(holder);
        }


        CommentGroupData data = getGroup(groupPosition);
        if (data != null) {
            holder.title.setText(data.mTitle + " 에 새로운 코멘트가 달렸습니다.");
            holder.commentCnt.setText("+" + getChildrenCount(groupPosition));
        }
        return convertView;
    }

    private class GroupViewHolder {
        TextView title;
        TextView commentCnt;

        public GroupViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.txt_title);
            commentCnt = (TextView) view.findViewById(R.id.txt_comment_cnt);
        }
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder;
        if (convertView != null) {
            holder = (ChildViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_child, parent, false);
            holder = new ChildViewHolder(convertView);
            convertView.setTag(holder);
        }

        CommentData data = getChild(groupPosition, childPosition);
        if (data != null) {
            holder.comment.setText(data.msg);
            holder.user.setText(data.userName);
            holder.date.setText(data.time);
        }

        return convertView;
    }

    private class ChildViewHolder {
        TextView comment;
        TextView user;
        TextView date;

        public ChildViewHolder(View view) {
            comment = (TextView) view.findViewById(R.id.txt_user_comment);
            user = (TextView) view.findViewById(R.id.txt_user_name);
            date = (TextView) view.findViewById(R.id.txt_comment_date);
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
