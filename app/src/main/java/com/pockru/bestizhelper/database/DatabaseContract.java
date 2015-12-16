package com.pockru.bestizhelper.database;

import android.net.Uri;
import android.provider.BaseColumns;

public class DatabaseContract {

    public static final String CONTENT_AUTHORITY = "com.pockru.bestizhelper";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final Uri AUTHORITY_URI = BASE_CONTENT_URI;

    interface ArticleColumns {
        public static final String KEY_ARTICLE_NUM = "article_id";
        public static final String KEY_ARTICLE_TITLE = "article_title";
        public static final String KEY_ARTICLE_USER = "article_user";
        public static final String KEY_ARTICLE_DATE = "article_date";
        public static final String KEY_ARTICLE_HIT = "article_hit";
        public static final String KEY_ARTICLE_VOTE = "article_vote";
        public static final String KEY_ARTICLE_COMMENT = "article_comment";
        public static final String KEY_ARTICLE_USER_HOMEPAGE = "article_user_homepage";
        public static final String KEY_ARTICLE_CONTENTS = "article_contents";
        public static final String KEY_ARTICLE_URL = "article_url";
        public static final String KEY_ARTICLE_MODIFY_URL = "article_modify_url";
        public static final String KEY_ARTICLE_DELETE_URL = "article_delete_url";
        public static final String KEY_ARTICLE_FAVORITE = "article_favorite";
        public static final String KEY_ARTICLE_TYPE = "article_type";
    }

    public static class ArticleTable implements ArticleColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(DatabaseHelper.Tables.TABLE_ARTICLE).build();
        public static final String CONTENT_TYPE = "vnd.pockru.bestizhelper.cursor.dir/vnd.pockru.bestizhelper.article";
        public static final String CONTENT_ITEM_TYPE = "vnd.pockru.bestizhelper.item/vnd.pockru.bestizhelper.article";

        public static Uri buildArticleUri(String menuId) {
            return CONTENT_URI.buildUpon().appendPath(menuId).build();
        }

        //article/article_id/#
        public static String getArticleId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        //article/article_id/#
        public static String getArticleNum(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static final String[] PROJECTION = new String[]{
                ArticleTable._ID,
                KEY_ARTICLE_NUM,
                KEY_ARTICLE_TITLE,
                KEY_ARTICLE_USER,
                KEY_ARTICLE_DATE,
                KEY_ARTICLE_HIT,
                KEY_ARTICLE_VOTE,
                KEY_ARTICLE_COMMENT,
                KEY_ARTICLE_USER_HOMEPAGE,
                KEY_ARTICLE_CONTENTS,
                KEY_ARTICLE_URL,
                KEY_ARTICLE_MODIFY_URL,
                KEY_ARTICLE_DELETE_URL,
                KEY_ARTICLE_FAVORITE,
                KEY_ARTICLE_TYPE
        };
    }

    interface MemberInfoColumns {
        String KEY_MEM_ID = "mem_id";
        String KEY_MEM_PWD = "mem_pwd";
        String KEY_MEM_LEVEL = "mem_level";
        String KEY_MEM_NAME = "mem_name";
        String KEY_MEM_EMAIL = "mem_email";
        String KEY_MEM_HOMEPAGE = "mem_homepage";
        String KEY_MEM_DISCLOSE_INFO = "mem_disclose_info";
        String KEY_MEM_COMMENT = "mem_comment";
        String KEY_MEM_POINT = "mem_point";
        String KEY_MEM_IS_SHOW_COMMENT = "mem_is_show_comment";
        String KEY_MEM_SERVER = "mem_server";
    }

    /**
     * uri example
     */
    public static class MemberInfoTable implements MemberInfoColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(DatabaseHelper.Tables.TABLE_MEMBER_INFO).build();
        public static final String CONTENT_TYPE = "vnd.pockru.bestizhelper.cursor.dir/vnd.pockru.bestizhelper.memberinfo";
        public static final String CONTENT_ITEM_TYPE = "vnd.pockru.bestizhelper.item/vnd.pockru.bestizhelper.memberinfo";

        public static Uri buildMemberInfoUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        // member_info/#
        public static String getMemberInfoId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        // member_info/mem_server/*/mem_id/*
        public static String getMemberInfoServer(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        // member_info/mem_server/*/mem_id/*
        public static String getMemberInfoMemberId(Uri uri) {
            return uri.getPathSegments().get(4);
        }

        public static final String[] PROJECTION = new String[]{
                MemberInfoTable._ID,
                KEY_MEM_ID,
                KEY_MEM_PWD,
                KEY_MEM_LEVEL,
                KEY_MEM_NAME,
                KEY_MEM_EMAIL,
                KEY_MEM_HOMEPAGE,
                KEY_MEM_DISCLOSE_INFO,
                KEY_MEM_COMMENT,
                KEY_MEM_POINT,
                KEY_MEM_IS_SHOW_COMMENT,
                KEY_MEM_SERVER
        };
    }

}
