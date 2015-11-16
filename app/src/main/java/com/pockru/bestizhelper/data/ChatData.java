package com.pockru.bestizhelper.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by rhpark on 2015. 11. 13..
 * JIRA: MWP-
 */
public class ChatData {
    public static final int TYPE_ME = 0;
    public static final int TYPE_OTHER = 1;

    public String userId;
    public String msg;
    public String time;
    public String name;

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    public ChatData() {

    }

    public ChatData(String userId, String msg, String name) {
        this.userId = userId;
        this.msg = msg;
        SimpleDateFormat format = new SimpleDateFormat("a hh:mm:ss", Locale.KOREA);
        this.time = format.format(new Date());
        this.name = name;
    }
}
