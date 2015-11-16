package com.pockru.bestizhelper.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.Query;
import com.pockru.bestizhelper.R;
import com.pockru.bestizhelper.data.ChatData;

/**
 * Created by rhpark on 2015. 11. 13..
 * JIRA: MWP-
 */
public class ChatAdapter extends FirebaseListAdapter<ChatData>{

    private static final int VIEW_TYPE_CNT = 2;

    public ChatAdapter(Query ref, Activity activity) {
        super(ref, ChatData.class, activity);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int type = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (type) {
                case ChatData.TYPE_ME:
                    convertView = mInflater.inflate(R.layout.layout_me, parent, false);
                    break;
                case ChatData.TYPE_OTHER:
                    convertView = mInflater.inflate(R.layout.layout_other, parent, false);
                    break;
                default:
                    convertView = mInflater.inflate(R.layout.layout_me, parent, false);
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
        TextView tvName;

        public void init(View view) {
            this.tvMsg = (TextView) view.findViewById(R.id.chat_msg);
            this.tvTime = (TextView) view.findViewById(R.id.chat_time);
            this.tvName = (TextView) view.findViewById(R.id.chat_user);
        }

        public void set(ChatData data) {
            this.tvMsg.setText(data.msg);
            this.tvTime.setText(data.time);
            if (this.tvName != null) this.tvName.setText(data.name);
        }
    }
}
