package com.pockru.bestizhelper.database;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.text.TextUtils;

public class SQLBuilder {
	private String mTable;
	private StringBuilder mSelection = new StringBuilder();
	
	private HashMap<String, String> mProjectionMap = new HashMap<String, String>();
	private ArrayList<String> mSelectionArgs = new ArrayList<String>();
	
	public SQLBuilder table(String table) {
		mTable = table;
		return this;
	}
	
	private void assertTable() {
		if (mTable == null) {
			 throw new IllegalStateException("Table not specified");
		}
	}
	
	public String[] getSelectionArgs() {
		return mSelectionArgs.toArray(new String[mSelectionArgs.size()]);
	}
	
	public SQLBuilder map(String fromColumn, String toClause) {
        mProjectionMap.put(fromColumn, toClause + " AS " + fromColumn);
        return this;
    }
	
	public SQLBuilder where(String selection, String... selectionArgs) {
		if (TextUtils.isEmpty(selection)) {
			if (selectionArgs != null && selectionArgs.length > 0) {
				
			}
			return this;
		}
		
		if (mSelection.length() > 0) {
			mSelection.append(" AND ");
		}
		
		mSelection.append("(").append(selection).append(")");
		if (selectionArgs != null) {
			for (String arg : selectionArgs) {
				mSelectionArgs.add(arg);
			}
		}
		return this;
	}
	
	private void mapColumns(String[] columns) {
		for (int i=0; i<columns.length; i++) {
			final String target = mProjectionMap.get(columns[i]);
            if (target != null) {
                columns[i] = target;
            }
		}
	}
	
	public SQLBuilder mapToTable(String column, String table) {
		mProjectionMap.put(column, table + "." + column);
        return this;
	}
	
	private String getSelection() {
        return mSelection.toString();
    }
	
	public Cursor query(SQLiteDatabase db, String[] columns, String groupBy,
            String having, String orderBy, String limit) {
		assertTable();
		if (columns != null) mapColumns(columns);
		return db.query(mTable, columns, getSelection(), getSelectionArgs(), groupBy, having, orderBy);
	}
	
	public Cursor query(SQLiteDatabase db, String[] columns, String orderBy) {
		return query(db, columns, null, null, orderBy, null);
	}
	
	public int update(SQLiteDatabase db, ContentValues cv) {
		assertTable();
		int i = 0;
		try{	// HTC error code 10: disk I/O error 대응
			i = db.update(mTable, cv, getSelection(), getSelectionArgs());
		}catch (SQLiteDiskIOException e) {
			e.printStackTrace();
		}
		return i;
	}
	
	public int delete(SQLiteDatabase db) {
		assertTable();
		int i = 0;
		try{	// HTC error code 10: disk I/O error 대응
			i = db.delete(mTable, getSelection(), getSelectionArgs());
		}catch (SQLiteDiskIOException e) {
			e.printStackTrace();
		}
		return i;
	}
}
