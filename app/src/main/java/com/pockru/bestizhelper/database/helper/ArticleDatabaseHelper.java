package com.pockru.bestizhelper.database.helper;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.text.TextUtils;

import com.pockru.bestizhelper.data.ArticleDB;
import com.pockru.bestizhelper.database.DatabaseContract;
import com.pockru.bestizhelper.database.DatabaseContract.ArticleTable;

import java.util.ArrayList;
import java.util.List;

public class ArticleDatabaseHelper {

    public static void insert(Context context, ArticleDB articleDB) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        ContentValues cv = new ContentValues();
        cv.put(ArticleTable.KEY_ARTICLE_NUM, articleDB.articleNum);
        cv.put(ArticleTable.KEY_ARTICLE_TITLE, articleDB.articleTitle);
        cv.put(ArticleTable.KEY_ARTICLE_USER, articleDB.articleUser);
        cv.put(ArticleTable.KEY_ARTICLE_DATE, articleDB.articleDate);
        cv.put(ArticleTable.KEY_ARTICLE_HIT, articleDB.articleHit);
        cv.put(ArticleTable.KEY_ARTICLE_VOTE, articleDB.articleVote);
        cv.put(ArticleTable.KEY_ARTICLE_COMMENT, articleDB.articleComment);
        cv.put(ArticleTable.KEY_ARTICLE_USER_HOMEPAGE, articleDB.articleUserHomepage);
        cv.put(ArticleTable.KEY_ARTICLE_CONTENTS, articleDB.articleContents);
        cv.put(ArticleTable.KEY_ARTICLE_URL, articleDB.articleUrl);
        cv.put(ArticleTable.KEY_ARTICLE_MODIFY_URL, articleDB.articleModifyUrl);
        cv.put(ArticleTable.KEY_ARTICLE_DELETE_URL, articleDB.articleDeleteUrl);
        cv.put(ArticleTable.KEY_ARTICLE_FAVORITE, articleDB.articleFavorite);
        cv.put(ArticleTable.KEY_ARTICLE_TYPE, articleDB.articleType);

        operations.add(ContentProviderOperation.newInsert(ArticleTable.CONTENT_URI).withValues(cv).build());

        try {
            context.getContentResolver().applyBatch(DatabaseContract.CONTENT_AUTHORITY, operations);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public static void delete(Context context, int articleNum) {
        context.getContentResolver().delete(ArticleTable.CONTENT_URI,
                ArticleTable.KEY_ARTICLE_NUM + "=?",
                new String[]{String.valueOf(articleNum)});
    }

    public static void delete(Context context, List<String> articleList) {
        String args = TextUtils.join(", ", articleList);
        context.getContentResolver().delete(ArticleTable.CONTENT_URI,
                ArticleTable.KEY_ARTICLE_NUM + " IN ("+args +")",
                null);
    }

    public static void update(Context context, ArticleDB articleDB) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        ContentValues cv = new ContentValues();
        cv.put(ArticleTable.KEY_ARTICLE_NUM, articleDB.articleNum);
        cv.put(ArticleTable.KEY_ARTICLE_TITLE, articleDB.articleTitle);
        cv.put(ArticleTable.KEY_ARTICLE_USER, articleDB.articleUser);
        cv.put(ArticleTable.KEY_ARTICLE_DATE, articleDB.articleDate);
        cv.put(ArticleTable.KEY_ARTICLE_HIT, articleDB.articleHit);
        cv.put(ArticleTable.KEY_ARTICLE_VOTE, articleDB.articleVote);
        cv.put(ArticleTable.KEY_ARTICLE_COMMENT, articleDB.articleComment);
        cv.put(ArticleTable.KEY_ARTICLE_USER_HOMEPAGE, articleDB.articleUserHomepage);
        cv.put(ArticleTable.KEY_ARTICLE_CONTENTS, articleDB.articleContents);
        cv.put(ArticleTable.KEY_ARTICLE_URL, articleDB.articleUrl);
        cv.put(ArticleTable.KEY_ARTICLE_MODIFY_URL, articleDB.articleModifyUrl);
        cv.put(ArticleTable.KEY_ARTICLE_DELETE_URL, articleDB.articleDeleteUrl);
        cv.put(ArticleTable.KEY_ARTICLE_FAVORITE, articleDB.articleFavorite);
        cv.put(ArticleTable.KEY_ARTICLE_TYPE, articleDB.articleType);

        operations.add(ContentProviderOperation.newUpdate(ArticleTable.CONTENT_URI)
                .withSelection(ArticleTable.KEY_ARTICLE_NUM + "=?", new String[]{String.valueOf(articleDB.articleNum)}).withValues(cv).build());

        try {
            context.getContentResolver().applyBatch(DatabaseContract.CONTENT_AUTHORITY, operations);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public static void insertOrUpdate(Context context, ArticleDB articleDB) {
        if (getCountData(context, articleDB.articleNum) > 0) {
            update(context, articleDB);
        } else {
            insert(context, articleDB);
        }
    }

    public static ArticleDB getData(Context context, int articleNum) {
        String selection = ArticleTable.KEY_ARTICLE_NUM + "=?";
        String selectionArg[] = {String.valueOf(articleNum)};
        Cursor cursor = context.getContentResolver().query(ArticleTable.CONTENT_URI, null, selection, selectionArg, null);
        ArticleDB articleDB = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                articleDB = new ArticleDB();
                articleDB.articleNum = cursor.getInt(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_NUM));
                articleDB.articleTitle = cursor.getString(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_TITLE));
                articleDB.articleUser = cursor.getString(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_USER));
                articleDB.articleDate = cursor.getString(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_DATE));
                articleDB.articleHit = cursor.getInt(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_HIT));
                articleDB.articleVote = cursor.getInt(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_VOTE));
                articleDB.articleComment = cursor.getInt(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_COMMENT));
                articleDB.articleUserHomepage = cursor.getString(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_USER_HOMEPAGE));
                articleDB.articleContents = cursor.getString(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_CONTENTS));
                articleDB.articleUrl = cursor.getString(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_URL));
                articleDB.articleModifyUrl = cursor.getString(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_MODIFY_URL));
                articleDB.articleDeleteUrl = cursor.getString(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_DELETE_URL));
                articleDB.articleFavorite = cursor.getInt(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_FAVORITE));
                articleDB.articleType = cursor.getInt(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_TYPE));
                cursor.close();
            }
        }

        return articleDB;
    }

    public static ArrayList<ArticleDB> getAllData(Context context) {
        Cursor cursor = context.getContentResolver().query(ArticleTable.CONTENT_URI, null, null, null, null);
        ArrayList<ArticleDB> articleDBList = new ArrayList<ArticleDB>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ArticleDB articleDB = new ArticleDB();
                articleDB.articleNum = cursor.getInt(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_NUM));
                articleDB.articleTitle = cursor.getString(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_TITLE));
                articleDB.articleUser = cursor.getString(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_USER));
                articleDB.articleDate = cursor.getString(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_DATE));
                articleDB.articleHit = cursor.getInt(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_HIT));
                articleDB.articleVote = cursor.getInt(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_VOTE));
                articleDB.articleComment = cursor.getInt(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_COMMENT));
                articleDB.articleUserHomepage = cursor.getString(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_USER_HOMEPAGE));
                articleDB.articleContents = cursor.getString(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_CONTENTS));
                articleDB.articleUrl = cursor.getString(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_URL));
                articleDB.articleModifyUrl = cursor.getString(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_MODIFY_URL));
                articleDB.articleDeleteUrl = cursor.getString(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_DELETE_URL));
                articleDB.articleFavorite = cursor.getInt(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_FAVORITE));
                articleDB.articleType = cursor.getInt(cursor.getColumnIndex(ArticleTable.KEY_ARTICLE_TYPE));
                articleDBList.add(articleDB);
            }
            cursor.close();
        }
        return articleDBList;
    }

    public static int getCountData(Context context, int articleNum) {
        String selection = ArticleTable.KEY_ARTICLE_NUM + "=?";
        String selectionArg[] = {String.valueOf(articleNum)};
        Cursor cursor = context.getContentResolver().query(ArticleTable.CONTENT_URI, null, selection, selectionArg, null);
        int cnt = 0;
        if (cursor != null) {
            cnt = cursor.getCount();
            cursor.close();
        }
        return cnt;
    }
}
