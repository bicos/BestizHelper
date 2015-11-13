package com.pockru.bestizhelper.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.pockru.bestizhelper.R;
import com.pockru.bestizhelper.adapter.ChatAdapter;
import com.pockru.bestizhelper.data.ChatData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhpark on 2015. 11. 13..
 * JIRA: MWP-
 */
public class ChatView extends RelativeLayout{

    private ListView chatList;
    private EditText inputChat;
    private ChatAdapter adapter;

    public ChatView(Context context) {
        super(context);
        init();
    }

    public ChatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.layout_chat_drawer, this);

        chatList = (ListView) findViewById(R.id.chat_list_view);
        inputChat = (EditText) findViewById(R.id.input_chat_msg);

        adapter = new ChatAdapter(getContext());

        List<ChatData> chatDatas = new ArrayList<>();
        chatDatas.add(new ChatData("하이룽하이룽하이룽하이룽하이룽하이룽하이룽하이룽하이룽하이룽", ChatData.TYPE_ME));
        chatDatas.add(new ChatData("하이룽하이룽하이룽하이룽하이룽하이룽하이룽하이룽하이룽하이룽", ChatData.TYPE_ME));
        chatDatas.add(new ChatData("하이룽하이룽하이룽하이룽하이룽하이룽하이룽하이룽하이룽하이룽", ChatData.TYPE_OTHER));
        chatDatas.add(new ChatData("하이룽하이룽하이룽하이룽하이룽하이룽하이룽하이룽하이룽하이룽", ChatData.TYPE_ME));

        adapter.setChats(chatDatas);
        chatList.setAdapter(adapter);
    }


}
