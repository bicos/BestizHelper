package com.pockru.bestizhelper.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by rhpark on 2015. 11. 13..
 * JIRA: MWP-
 */
public class CommentData {

    public String msg;
    public String time;
    public String userName;

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    public CommentData() {

    }

    public CommentData(String userName, String msg) {
        this.userName = userName;
        this.msg = msg;
        SimpleDateFormat format = new SimpleDateFormat("a hh:mm:ss", Locale.KOREA);
        this.time = format.format(new Date());
    }
}
