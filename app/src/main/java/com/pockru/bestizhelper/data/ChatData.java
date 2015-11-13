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

    public int type;
    public String msg;
    public String time;

    public ChatData() {
        msg = "";

        SimpleDateFormat format = new SimpleDateFormat("a hh:mm:ss", Locale.KOREA);
        this.time = format.format(new Date());
    }

    public ChatData(String msg) {
        this.msg = msg;

        SimpleDateFormat format = new SimpleDateFormat("a hh:mm:ss", Locale.KOREA);
        this.time = format.format(new Date());
    }

    public ChatData(String msg, int type) {
        this.msg = msg;
        this.type = type;
        SimpleDateFormat format = new SimpleDateFormat("a hh:mm:ss", Locale.KOREA);
        this.time = format.format(new Date());
    }
}
