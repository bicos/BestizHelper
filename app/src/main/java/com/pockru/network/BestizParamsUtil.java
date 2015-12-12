package com.pockru.network;

import android.text.TextUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;

/**
 * Created by 래형 on 2015-12-12.
 */
public class BestizParamsUtil {

    public static ArrayList<NameValuePair> createWriteParams(String boardId, String title, String contents, String articleNo) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("page", "1"));
        params.add(new BasicNameValuePair("id", boardId));
        params.add(new BasicNameValuePair("no", articleNo));
        params.add(new BasicNameValuePair("select_arrange", "headnum"));
        params.add(new BasicNameValuePair("desc", "asc"));
        params.add(new BasicNameValuePair("page_num", ""));
        params.add(new BasicNameValuePair("keyword", ""));
        params.add(new BasicNameValuePair("category", ""));
        params.add(new BasicNameValuePair("sn", "off"));
        params.add(new BasicNameValuePair("ss", "on"));
        params.add(new BasicNameValuePair("sc", "off"));
        params.add(new BasicNameValuePair("mode", TextUtils.isEmpty(articleNo) ? "write" : "modify"));
        params.add(new BasicNameValuePair("category", "1"));
        params.add(new BasicNameValuePair("use_html", "1"));
        params.add(new BasicNameValuePair("subject", title));
        params.add(new BasicNameValuePair("memo", contents));
        return params;
    }

    public static ArrayList<NameValuePair> createDeleteParams(String boardId, String no) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("page", "1"));
        params.add(new BasicNameValuePair("id", boardId));
        params.add(new BasicNameValuePair("no", no));
        params.add(new BasicNameValuePair("select_arrange", "headnum"));
        params.add(new BasicNameValuePair("desc", "asc"));
        params.add(new BasicNameValuePair("page_num", "25"));
        params.add(new BasicNameValuePair("keyword", ""));
        params.add(new BasicNameValuePair("category", ""));
        params.add(new BasicNameValuePair("sn", "off"));
        params.add(new BasicNameValuePair("ss", "on"));
        params.add(new BasicNameValuePair("sc", "off"));
        params.add(new BasicNameValuePair("mode", ""));
        return params;
    }

    public static ArrayList<NameValuePair> createLoginParams(String boardId, String id, String pwd) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("auto_login", "0"));
        params.add(new BasicNameValuePair("page", "1"));
        params.add(new BasicNameValuePair("id", boardId));
        params.add(new BasicNameValuePair("select_arrange", "headnum"));
        params.add(new BasicNameValuePair("desc", "asc"));
        params.add(new BasicNameValuePair("sn", "off"));
        params.add(new BasicNameValuePair("ss", "on"));
        params.add(new BasicNameValuePair("sc", "off"));
        params.add(new BasicNameValuePair("s_url", "/zboard/zboard.php?id=" + boardId));
        params.add(new BasicNameValuePair("user_id", id));
        params.add(new BasicNameValuePair("password", pwd));
        return params;
    }

    public static ArrayList<NameValuePair> createLogoutParams(String boardId) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("page", "1"));
        params.add(new BasicNameValuePair("id", boardId));
        params.add(new BasicNameValuePair("select_arrange", "headnum"));
        params.add(new BasicNameValuePair("desc", "asc"));
        params.add(new BasicNameValuePair("sn", "off"));
        params.add(new BasicNameValuePair("ss", "on"));
        params.add(new BasicNameValuePair("sc", "off"));
        params.add(new BasicNameValuePair("s_url", "/zboard/zboard.php?id=" + boardId));
        return params;
    }

    public static ArrayList<NameValuePair> createMovePageParams(String boardId, String page, String keyword, String sn, String ss, String sc) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("page", page));
        params.add(new BasicNameValuePair("id", boardId));
        params.add(new BasicNameValuePair("no", ""));
        params.add(new BasicNameValuePair("select_arrange", "headnum"));
        params.add(new BasicNameValuePair("desc", "asc"));
        params.add(new BasicNameValuePair("category", ""));
        params.add(new BasicNameValuePair("sn", sn));
        params.add(new BasicNameValuePair("ss", ss));
        params.add(new BasicNameValuePair("sn", sn));
        params.add(new BasicNameValuePair("sc", sc));
        params.add(new BasicNameValuePair("divpage", "15"));
        if (keyword != null)
            params.add(new BasicNameValuePair("keyword", keyword));
        return params;
    }

    public static ArrayList<NameValuePair> createDeleteCommentParams(String boardId, String no, String commentNo){
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("page", "1"));
        params.add(new BasicNameValuePair("id", boardId));
        params.add(new BasicNameValuePair("no", no));
        params.add(new BasicNameValuePair("select_arrange", "headnum"));
        params.add(new BasicNameValuePair("desc", "asc"));
        params.add(new BasicNameValuePair("page_num", "25"));
        params.add(new BasicNameValuePair("keyword", ""));
        params.add(new BasicNameValuePair("category", ""));
        params.add(new BasicNameValuePair("sn", "off"));
        params.add(new BasicNameValuePair("ss", "on"));
        params.add(new BasicNameValuePair("sc", "off"));
        params.add(new BasicNameValuePair("mode", ""));
        params.add(new BasicNameValuePair("c_no", commentNo));
        return params;
    }

    public static ArrayList<NameValuePair> createWriteCommentParams(String boardId, String no, String comment) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("page", "1"));
        params.add(new BasicNameValuePair("id", boardId));
        params.add(new BasicNameValuePair("no", no));
        params.add(new BasicNameValuePair("select_arrange", "headnum"));
        params.add(new BasicNameValuePair("desc", "asc"));
        params.add(new BasicNameValuePair("page_num", "25"));
        params.add(new BasicNameValuePair("keyword", ""));
        params.add(new BasicNameValuePair("category", ""));
        params.add(new BasicNameValuePair("sn", "off"));
        params.add(new BasicNameValuePair("ss", "on"));
        params.add(new BasicNameValuePair("sc", "off"));
        params.add(new BasicNameValuePair("mode", ""));
        params.add(new BasicNameValuePair("memo", comment));
        return params;
    }
}
