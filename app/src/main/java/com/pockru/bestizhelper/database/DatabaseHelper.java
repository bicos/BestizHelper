package com.pockru.bestizhelper.database;

import com.pockru.bestizhelper.database.DatabaseContract.ArticleColumns;
import com.pockru.bestizhelper.database.DatabaseContract.ArticleTable;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "database.db";
	private static final int DATABASE_VERSION = 1;

	private static DatabaseHelper instance = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static DatabaseHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DatabaseHelper(context);
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Tables.CREATE_TABLE_ARTICLE);
	}

	public String getDatabaseName() {
		return DATABASE_NAME;
	}

	public interface Tables {
		/** Table 의 이름 */
		public static final String TABLE_ARTICLE = "article";
		
		/**
		 * Create Table 테이블 생성
		 */
		String CREATE_TABLE_ARTICLE = "CREATE TABLE " + Tables.TABLE_ARTICLE + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ArticleColumns.KEY_ARTICLE_NUM + " INTEGER," + ArticleColumns.KEY_ARTICLE_TITLE + " TEXT," + ArticleColumns.KEY_ARTICLE_USER + " TEXT,"
				+ ArticleColumns.KEY_ARTICLE_DATE + " TEXT," + ArticleColumns.KEY_ARTICLE_HIT + " INTEGER," + ArticleColumns.KEY_ARTICLE_VOTE + " INTEGER,"
				+ ArticleColumns.KEY_ARTICLE_COMMENT + " INTEGER," + ArticleColumns.KEY_ARTICLE_USER_HOMEPAGE + " TEXT," + ArticleColumns.KEY_ARTICLE_CONTENTS
				+ " TEXT," + ArticleColumns.KEY_ARTICLE_URL + " TEXT," + ArticleColumns.KEY_ARTICLE_MODIFY_URL + " TEXT,"
				+ ArticleColumns.KEY_ARTICLE_DELETE_URL + " TEXT," + ArticleColumns.KEY_ARTICLE_FAVORITE + " INTEGER," + "UNIQUE ("
				+ ArticleColumns.KEY_ARTICLE_NUM + ") ON CONFLICT REPLACE)";
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1) {
			oldVersion++;
		}
	}

	public void clearTable(Context context) {
		ContentResolver cr = context.getContentResolver();
		cr.delete(ArticleTable.CONTENT_URI, null, null);
	}
}
