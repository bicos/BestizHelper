package com.pockru.bestizhelper.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pockru.bestizhelper.R;
import com.pockru.bestizhelper.data.ChatData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhpark on 2015. 11. 13..
 * JIRA: MWP-
 */
public class ChatAdapter extends BaseAdapter{

    private static final int VIEW_TYPE_CNT = 2;

    private List<ChatData> mChats;

    private Context mContext;

    public ChatAdapter(Context context) {
        this.mContext = context;
        this.mChats = new ArrayList<>();
    }

    public ChatAdapter(Context context, List<ChatData> chats) {
        this.mContext = context;
        this.mChats = chats;
    }

    public List<ChatData> getChats() {
        return mChats;
    }

    public void setChats(List<ChatData> mChats) {
        this.mChats = mChats;
    }

    @Override
    public int getCount() {
        return mChats.size();
    }

    @Override
    public Object getItem(int position) {
        return mChats.size() > position ? mChats.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int type = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (type) {
                case ChatData.TYPE_ME:
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_me, parent, false);
                    break;
                case ChatData.TYPE_OTHER:
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_other, parent, false);
                    break;
                default:
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_me, parent, false);
                    break;
            }
            holder.init(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        ChatData data = (ChatData) getItem(position);

        if (data != null) {
            holder.set(data);
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        ChatData data = (ChatData) getItem(position);
        return data != null ? data.type : ChatData.TYPE_ME;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_CNT;
    }

    class ViewHolder {
        TextView tvMsg;
        TextView tvTime;

        public void init(View view) {
            this.tvMsg = (TextView) view.findViewById(R.id.chat_msg);
            this.tvTime = (TextView) view.findViewById(R.id.chat_time);
        }

        public void set(ChatData data) {
            this.tvMsg.setText(data.msg);
            this.tvTime.setText(data.time);
        }
    }
}
