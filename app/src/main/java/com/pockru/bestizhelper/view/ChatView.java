package com.pockru.bestizhelper.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.firebase.client.Query;
import com.pockru.bestizhelper.R;
import com.pockru.bestizhelper.adapter.ChatAdapter;

/**
 * Created by rhpark on 2015. 11. 13..
 * JIRA: MWP-
 */
public class ChatView extends RelativeLayout {

    private ListView chatList;
    private EditText inputChat;
    private ImageButton btnSendMsg;
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

    private void init() {
        inflate(getContext(), R.layout.layout_chat_drawer, this);

        chatList = (ListView) findViewById(R.id.chat_list_view);
        inputChat = (EditText) findViewById(R.id.input_chat_msg);
        btnSendMsg = (ImageButton) findViewById(R.id.btn_send_msg);
    }

    public void initChatView(Query query){
        adapter = new ChatAdapter(query, (Activity) getContext());
        chatList.setAdapter(adapter);
    }

    public EditText getInputChat() {
        return inputChat;
    }

    public ImageButton getBtnSendMsg() {
        return btnSendMsg;
    }

    public ChatAdapter getAdapter() {
        return adapter;
    }

    public void cleanUp(){
        if (adapter != null) adapter.cleanup();
    }
}
