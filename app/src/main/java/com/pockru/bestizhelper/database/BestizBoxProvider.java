package com.pockru.bestizhelper.database;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;

import com.pockru.bestizhelper.database.DatabaseContract.ArticleTable;
import com.pockru.bestizhelper.database.DatabaseHelper.Tables;

import java.util.ArrayList;
import java.util.HashSet;

public class BestizBoxProvider extends ContentProvider{

	public static final String AUTHORITY		=	DatabaseContract.CONTENT_AUTHORITY;
	
	private final ThreadLocal<Boolean> mApplyingBatch = new ThreadLocal<Boolean>();
	
	private boolean applyingBatch() {
		return mApplyingBatch != null && (mApplyingBatch.get() != null && mApplyingBatch.get());
	}
	
	private DatabaseHelper mDbHelper;
	
	private static final UriMatcher sUriMatcher = buildUriMatcher();
	
	private static final int ARTICLE				= 100;
	private static final int ARTICLE_ID				= 101;
	private static final int ARTICLE_NUM			= 102;
	private static final int MEM_INFO				= 103;
	private static final int MEM_INFO_ID			= 104;
	private static final int MEM_INFO_MEM_ID		= 105;
	
	/**
	 * Item 의 type 을 return 
	 */
	@Override
	public String getType(Uri uri) {
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case ARTICLE:
				return ArticleTable.CONTENT_TYPE;
			case ARTICLE_ID:
			case ARTICLE_NUM:
				return ArticleTable.CONTENT_ITEM_TYPE;
			case MEM_INFO:
				return DatabaseContract.MemberInfoTable.CONTENT_TYPE;
			case MEM_INFO_ID:
			case MEM_INFO_MEM_ID:
				return DatabaseContract.MemberInfoTable.CONTENT_ITEM_TYPE;
			default:
				throw new UnsupportedOperationException("UnKonwn Uri : " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		
		final int match = sUriMatcher.match(uri);
		Cursor cCursor = null;
		final SQLBuilder builder = setSQLBuild(uri, match).where(selection, selectionArgs);
		cCursor = builder.query(db, projection, sortOrder);
		if (cCursor != null)
			cCursor.setNotificationUri(getContext().getContentResolver(), uri);
		
		return cCursor;
	}
	
	/**
	 * Uri 에 맞춰서 Query 를 만든다.
	 * 
	 * @param uri
	 * @param match
	 * @return
	 */
	private SQLBuilder setSQLBuild(Uri uri, int match) {
		final SQLBuilder builder = new SQLBuilder();
		switch (match) {
			case ARTICLE: {
				return builder.table(Tables.TABLE_ARTICLE);
			}
			case ARTICLE_ID: {
				final String id = ArticleTable.getArticleId(uri);
				return builder.table(Tables.TABLE_ARTICLE)
						.where(ArticleTable._ID + "=?", id);
			}
			case ARTICLE_NUM: {
				final String cid = ArticleTable.getArticleNum(uri);
				return builder.table(Tables.TABLE_ARTICLE)
						.where(ArticleTable.KEY_ARTICLE_NUM + "=?", cid);
			}
			case MEM_INFO: {
				return builder.table(Tables.TABLE_MEMBER_INFO);
			}
			case MEM_INFO_ID: {
				final String id = DatabaseContract.MemberInfoTable.getMemberInfoId(uri);
				return builder.table(Tables.TABLE_MEMBER_INFO)
						.where(DatabaseContract.MemberInfoTable._ID + "=?", id);
			}
			case MEM_INFO_MEM_ID: {
				final String memServer = DatabaseContract.MemberInfoTable.getMemberInfoServer(uri);
				final String memId = DatabaseContract.MemberInfoTable.getMemberInfoMemberId(uri);
				return builder.table(Tables.TABLE_MEMBER_INFO)
						.where(DatabaseContract.MemberInfoTable.KEY_MEM_SERVER + "=? AND "+
								DatabaseContract.MemberInfoTable.KEY_MEM_ID + "=?", memServer, memId);
			}
			default:
				return null;
		}
	}
	
	@Override
	public boolean onCreate() {		
		final Context context = getContext();
		mDbHelper = new DatabaseHelper(context);
		return true;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) throws SQLiteException{
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		final int matcher = sUriMatcher.match(uri);
		Uri returnUri = null;
		
		try{
			switch (matcher) {
				case ARTICLE: {
					final long id = db.insertOrThrow(Tables.TABLE_ARTICLE, null, values);
					returnUri = ArticleTable.buildArticleUri(String.valueOf(id));
					break;
				}
				case MEM_INFO: {
					final long id = db.insertOrThrow(Tables.TABLE_MEMBER_INFO, null, values);
					returnUri = DatabaseContract.MemberInfoTable.buildMemberInfoUri(String.valueOf(id));
				}
				default: 
					break;
				
			}
			if (!applyingBatch()) {
				getContext().getContentResolver().notifyChange(returnUri, null);
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return returnUri;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) throws SQLiteException{
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		final int matcher = sUriMatcher.match(uri);
		int returnInteger = -1;
		SQLBuilder builder = setSQLBuild(uri, matcher);
		
		returnInteger = builder.where(selection, selectionArgs).update(db, values);
		if (!applyingBatch() && returnInteger > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return returnInteger;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) throws SQLiteException{
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		final int matcher = sUriMatcher.match(uri);
		int returnInteger = -1;
		if(setSQLBuild(uri, matcher) == null)
			return returnInteger;
		final SQLBuilder builder = setSQLBuild(uri, matcher).where(selection, selectionArgs);

		returnInteger = builder.delete(db);
		if (!applyingBatch() && returnInteger > 0){
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return returnInteger;
	}

	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = DatabaseContract.CONTENT_AUTHORITY;
		
		matcher.addURI(authority, Tables.TABLE_ARTICLE, ARTICLE);
		// article/#
		matcher.addURI(authority, Tables.TABLE_ARTICLE + "/#", ARTICLE_ID);
		// article/article_id/#
		matcher.addURI(authority, Tables.TABLE_ARTICLE + "/" + ArticleTable.KEY_ARTICLE_NUM+ "/#", ARTICLE_NUM);

		matcher.addURI(authority, Tables.TABLE_MEMBER_INFO, MEM_INFO);
		// member_info/#
		matcher.addURI(authority, Tables.TABLE_MEMBER_INFO + "/#", MEM_INFO_ID);
		// member_info/mem_server/*/mem_id/*
		matcher.addURI(authority, Tables.TABLE_MEMBER_INFO +"/"
				+ DatabaseContract.MemberInfoTable.KEY_MEM_SERVER+"/*/"
				+ DatabaseContract.MemberInfoTable.KEY_MEM_ID+"/*", MEM_INFO_MEM_ID);
		
		return matcher;
	}
	
	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException, SQLiteException {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		HashSet<Uri> mUriSet = new HashSet<Uri>();
		
		db.beginTransaction();
		try {
			mApplyingBatch.set(true);
			final int numOperation = operations.size();
			final ContentProviderResult[] result = new ContentProviderResult[numOperation];
			for (int i=0; i<numOperation; i++) {
				result[i] = operations.get(i).apply(this, result, i);
				mUriSet.add(operations.get(i).getUri());
			}
			db.setTransactionSuccessful();
			return result;
		} finally {
			db.endTransaction();
			mApplyingBatch.set(false);
			for (Uri uri : mUriSet) {
				getContext().getContentResolver().notifyChange(uri, null);
			}
		}
	}
}
