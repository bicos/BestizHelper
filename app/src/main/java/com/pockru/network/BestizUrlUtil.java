package com.pockru.network;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.util.ArrayList;

/**
 * Created by rhpark on 2015. 12. 11..
 * JIRA: MWP-
 */
public class BestizUrlUtil {

    public static String createBoardListUrl(String host, String boardId) {
        return host + BestizUrlConstants.BOARD_LIST + "?" + String.format(BestizUrlConstants.QUERY_BOARD_ID, boardId);
    }

    public static String createLoginUrl(String host){
        return host + BestizUrlConstants.LOGIN;
    }

    public static String createLogoutUrl(String host){
        return host + BestizUrlConstants.LOGOUT;
    }


    public static String createCommentWriteUrl(String host) {
        return host + BestizUrlConstants.WRITE_COMMENT;
    }

    public static String createCommentWriteUrl(String host, ArrayList<NameValuePair> params) {
        return host + BestizUrlConstants.WRITE_COMMENT + "?" + URLEncodedUtils.format(params, "euc-kr");
    }

    public static String createCommentDeleteUrl(String host){
        return host + BestizUrlConstants.DELETE_COMMENT;
    }


    public static String createArticleWriteUrl(String host) {
        return host + BestizUrlConstants.WRITE_ARTICLE;
    }

    public static String createArticleDeleteUrl(String host) {
        return host + BestizUrlConstants.DELETE_ARTICLE;
    }

    public static String createArticleModifyUrl(String host) {
        return host + BestizUrlConstants.MODIFY_ARTICLE;
    }

    public static String createDetailArticleUrl(String baseUrl, String articleUrl) {
        return baseUrl + "/" + articleUrl;
    }

    public static String createUserInfoUrl(String baseUrl) {
        return baseUrl + BestizUrlConstants.MEMBER_INFO;
    }
}
