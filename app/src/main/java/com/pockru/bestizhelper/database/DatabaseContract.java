package com.pockru.bestizhelper.database;

import android.net.Uri;
import android.provider.BaseColumns;

public class DatabaseContract {
	
	public static final String CONTENT_AUTHORITY 			= "com.pockru.bestizhelper";
	
	private static final Uri BASE_CONTENT_URI 				= Uri.parse("content://" + CONTENT_AUTHORITY);
	
	public static final Uri AUTHORITY_URI					= BASE_CONTENT_URI;
	
	interface ArticleColumns {
		public static final String KEY_ARTICLE_NUM				= "article_id";
		public static final String KEY_ARTICLE_TITLE			= "article_title";
		public static final String KEY_ARTICLE_USER				= "article_user";
		public static final String KEY_ARTICLE_DATE				= "article_date";
		public static final String KEY_ARTICLE_HIT				= "article_hit";
		public static final String KEY_ARTICLE_VOTE				= "article_vote";
		public static final String KEY_ARTICLE_COMMENT			= "article_comment";
		public static final String KEY_ARTICLE_USER_HOMEPAGE	= "article_user_homepage";
		public static final String KEY_ARTICLE_CONTENTS			= "article_contents";
		public static final String KEY_ARTICLE_URL				= "article_url";
		public static final String KEY_ARTICLE_MODIFY_URL		= "article_modify_url";
		public static final String KEY_ARTICLE_DELETE_URL		= "article_delete_url";
		public static final String KEY_ARTICLE_FAVORITE			= "article_favorite";	
	}
	
	public static class ArticleTable implements ArticleColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(DatabaseHelper.Tables.TABLE_ARTICLE).build();
		public static final String CONTENT_TYPE = "vnd.pockru.bestizhelper.cursor.dir/vnd.pockru.bestizhelper.article";
		public static final String CONTENT_ITEM_TYPE = "vnd.pockru.bestizhelper.item/vnd.pockru.bestizhelper.article";
		public static Uri buildArticleUri(String menuId) {
			return CONTENT_URI.buildUpon().appendPath(menuId).build();
		}
		public static String getArticleId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
		public static String getArticleNum(Uri uri) {
			return uri.getPathSegments().get(2);
		}
		public static final String[] PROJECTION = new String[] {
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
			KEY_ARTICLE_FAVORITE
		};
	}

}
