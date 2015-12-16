package com.pockru.bestizhelper.database;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.pockru.bestizhelper.database.DatabaseContract.ArticleColumns;
import com.pockru.bestizhelper.database.DatabaseContract.ArticleTable;
import com.pockru.bestizhelper.database.DatabaseContract.MemberInfoColumns;
import com.pockru.bestizhelper.database.DatabaseContract.MemberInfoTable;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "database.db";
	private static final int DATABASE_VERSION = 3;

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
		db.execSQL(Tables.CREATE_TABLE_MEMBER_INFO);
	}

	public String getDatabaseName() {
		return DATABASE_NAME;
	}

	public interface Tables {
		/** Table 의 이름 */
		public static final String TABLE_ARTICLE = "article";
		public static final String TABLE_MEMBER_INFO = "member_info";
		
		/**
		 * Create Table 테이블 생성
		 */
		String CREATE_TABLE_ARTICLE = "CREATE TABLE " + Tables.TABLE_ARTICLE +
				" (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ ArticleColumns.KEY_ARTICLE_NUM + " INTEGER,"
				+ ArticleColumns.KEY_ARTICLE_TITLE + " TEXT,"
				+ ArticleColumns.KEY_ARTICLE_USER + " TEXT,"
				+ ArticleColumns.KEY_ARTICLE_DATE + " TEXT,"
				+ ArticleColumns.KEY_ARTICLE_HIT + " INTEGER,"
				+ ArticleColumns.KEY_ARTICLE_VOTE + " INTEGER,"
				+ ArticleColumns.KEY_ARTICLE_COMMENT + " INTEGER,"
				+ ArticleColumns.KEY_ARTICLE_USER_HOMEPAGE + " TEXT,"
				+ ArticleColumns.KEY_ARTICLE_CONTENTS + " TEXT,"
				+ ArticleColumns.KEY_ARTICLE_URL + " TEXT,"
				+ ArticleColumns.KEY_ARTICLE_MODIFY_URL + " TEXT,"
				+ ArticleColumns.KEY_ARTICLE_DELETE_URL + " TEXT,"
				+ ArticleColumns.KEY_ARTICLE_FAVORITE + " INTEGER,"
				+ ArticleColumns.KEY_ARTICLE_TYPE + " INTEGER,"
				+ "UNIQUE (" + ArticleColumns.KEY_ARTICLE_NUM + ") ON CONFLICT REPLACE)";

		String CREATE_TABLE_MEMBER_INFO = "CREATE TABLE " + Tables.TABLE_MEMBER_INFO +
				" (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ MemberInfoColumns.KEY_MEM_ID + " TEXT,"
				+ MemberInfoColumns.KEY_MEM_PWD + " TEXT,"
				+ MemberInfoColumns.KEY_MEM_NAME + " TEXT,"
				+ MemberInfoColumns.KEY_MEM_LEVEL + " INTEGER,"
				+ MemberInfoColumns.KEY_MEM_EMAIL + " TEXT,"
				+ MemberInfoColumns.KEY_MEM_HOMEPAGE + " TEXT,"
				+ MemberInfoColumns.KEY_MEM_POINT + " TEXT,"
				+ MemberInfoColumns.KEY_MEM_COMMENT + " TEXT,"
				+ MemberInfoColumns.KEY_MEM_DISCLOSE_INFO + " INTEGER,"
				+ MemberInfoColumns.KEY_MEM_IS_SHOW_COMMENT + " INTEGER,"
				+ MemberInfoColumns.KEY_MEM_SERVER + " TEXT,"
				+ "UNIQUE (" + MemberInfoColumns.KEY_MEM_ID +","+MemberInfoColumns.KEY_MEM_SERVER+ ") ON CONFLICT REPLACE)";
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		switch (oldVersion) {
			case 1:
				db.execSQL(Tables.CREATE_TABLE_MEMBER_INFO);
				oldVersion++;
			case 2:
				db.execSQL("ALTER TABLE " + Tables.TABLE_ARTICLE + " ADD COLUMN " + ArticleColumns.KEY_ARTICLE_TYPE + " INTEGER");
		}
	}

	public void clearTable(Context context) {
		ContentResolver cr = context.getContentResolver();
		cr.delete(ArticleTable.CONTENT_URI, null, null);
		cr.delete(MemberInfoTable.CONTENT_URI, null, null);
	}
}
