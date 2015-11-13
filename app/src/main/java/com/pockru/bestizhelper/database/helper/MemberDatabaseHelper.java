package com.pockru.bestizhelper.database.helper;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;

import com.pockru.bestizhelper.data.UserData;
import com.pockru.bestizhelper.database.DatabaseContract;

import java.util.ArrayList;


public class MemberDatabaseHelper {

    public static void insert(Context context, UserData userData) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_ID, userData.id);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_PWD, userData.pwd);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_NAME, userData.name);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_EMAIL, userData.email);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_HOMEPAGE, userData.homepage);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_LEVEL, userData.level);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_POINT, userData.point);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_COMMENT, userData.comment);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_DISCLOSE_INFO, userData.discloseInfo == true ? 1 : 0);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_IS_SHOW_COMMENT, userData.isShowComment == true ? 1 : 0);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_SERVER, userData.server);

        operations.add(ContentProviderOperation.newInsert(DatabaseContract.MemberInfoTable.CONTENT_URI).withValues(cv).build());

        try {
            context.getContentResolver().applyBatch(DatabaseContract.CONTENT_AUTHORITY, operations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

//    public static void delete(Context context, String id) {
//        context.getContentResolver().delete(DatabaseContract.MemberInfoTable.CONTENT_URI,
//                DatabaseContract.MemberInfoTable.KEY_MEM_ID + "=?",
//                new String[]{id});
//    }

    public static void delete(Context context, String server) {
        context.getContentResolver().delete(DatabaseContract.MemberInfoTable.CONTENT_URI,
                DatabaseContract.MemberInfoTable.KEY_MEM_SERVER + "=?",
                new String[]{server});
    }

    public static void update(Context context, UserData userData) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_ID, userData.id);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_PWD, userData.pwd);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_NAME, userData.name);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_EMAIL, userData.email);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_HOMEPAGE, userData.homepage);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_LEVEL, userData.level);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_POINT, userData.point);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_COMMENT, userData.comment);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_DISCLOSE_INFO, userData.discloseInfo == true ? 1 : 0);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_IS_SHOW_COMMENT, userData.isShowComment == true ? 1 : 0);
        cv.put(DatabaseContract.MemberInfoTable.KEY_MEM_SERVER, userData.server);

        operations.add(ContentProviderOperation.newUpdate(DatabaseContract.MemberInfoTable.CONTENT_URI)
                .withSelection(DatabaseContract.MemberInfoTable.KEY_MEM_ID + "=? AND " + DatabaseContract.MemberInfoTable.KEY_MEM_SERVER + "=?",
                        new String[]{userData.id, userData.server}).withValues(cv).build());

        try {
            context.getContentResolver().applyBatch(DatabaseContract.CONTENT_AUTHORITY, operations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param context
     * @param userData
     * @return if current user is first login
     */
    public static boolean insertOrUpdate(Context context, UserData userData) {
        if (isFirstLogin(context, userData.server)) {
            insert(context, userData);
            return true;
        } else {
            update(context, userData);
            return false;
        }
    }

    public static UserData getData(Context context, String server) {
        String selection = DatabaseContract.MemberInfoTable.KEY_MEM_SERVER + "=?";
        String selectionArg[] = {server};
        Cursor cursor = context.getContentResolver().query(DatabaseContract.MemberInfoTable.CONTENT_URI, null, selection, selectionArg, null);
        UserData userData = null;
        if (cursor != null && cursor.moveToFirst()) {
            userData = new UserData();
            userData.id = cursor.getString(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_ID));
            userData.pwd = cursor.getString(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_PWD));
            userData.name = cursor.getString(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_NAME));
            userData.email = cursor.getString(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_EMAIL));
            userData.homepage = cursor.getString(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_HOMEPAGE));
            userData.level = cursor.getInt(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_LEVEL));
            userData.point = cursor.getString(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_POINT));
            userData.comment = cursor.getString(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_COMMENT));
            userData.discloseInfo = (cursor.getInt(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_DISCLOSE_INFO)) == 1);
            userData.isShowComment = (cursor.getInt(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_IS_SHOW_COMMENT)) == 1);
            userData.server = cursor.getString(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_SERVER));
        }

        return userData;
    }

    public static ArrayList<UserData> getAllData(Context context) {
        Cursor cursor = context.getContentResolver().query(DatabaseContract.MemberInfoTable.CONTENT_URI, null, null, null, null);
        ArrayList<UserData> userDatas = new ArrayList<UserData>();
        while (cursor != null && cursor.moveToNext()) {
            UserData userData = new UserData();
            userData.id = cursor.getString(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_ID));
            userData.pwd = cursor.getString(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_PWD));
            userData.name = cursor.getString(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_NAME));
            userData.email = cursor.getString(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_EMAIL));
            userData.homepage = cursor.getString(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_HOMEPAGE));
            userData.level = cursor.getInt(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_LEVEL));
            userData.point = cursor.getString(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_POINT));
            userData.comment = cursor.getString(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_COMMENT));
            userData.discloseInfo = (cursor.getInt(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_DISCLOSE_INFO)) == 1);
            userData.isShowComment = (cursor.getInt(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_IS_SHOW_COMMENT)) == 1);
            userData.server = cursor.getString(cursor.getColumnIndex(DatabaseContract.MemberInfoTable.KEY_MEM_SERVER));
            userDatas.add(userData);
        }
        return userDatas;
    }

    public static int getCountData(Context context, String server) {
        String selection = DatabaseContract.MemberInfoTable.KEY_MEM_SERVER + "=?";
        String selectionArg[] = {server};
        Cursor cursor = context.getContentResolver().query(DatabaseContract.MemberInfoTable.CONTENT_URI, null, selection, selectionArg, null);
        return cursor != null ? cursor.getCount() : 0;
    }

    public static boolean isFirstLogin(Context context, String server){
        String selection = DatabaseContract.MemberInfoTable.KEY_MEM_SERVER + "=?";
        String selectionArg[] = {server};
        Cursor cursor = context.getContentResolver().query(DatabaseContract.MemberInfoTable.CONTENT_URI, null, selection, selectionArg, null);
        Log.i("test", "isFirstLogin : "+cursor.getCount());
        return cursor != null && cursor.getCount() <= 0;
    }
}
