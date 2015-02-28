/*
 * Zirco Browser for Android
 * 
 * Copyright (C) 2010 - 2011 J. Devauchelle and contributors.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package com.jeesmon.malayalambible.providers;

import java.io.File;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.Browser;

import com.jeesmon.malayalambible.sqlite.SDCardSQLiteOpenHelper;

public class MalayalamBibleBookmarksContentProvider extends ContentProvider {
	private static final Uri CONTENT_URI = Uri.parse("content://"
			+ MalayalamBibleBookmarksContentProvider.AUTHORITY + "/"
			+ MalayalamBibleBookmarksContentProvider.BOOKMARKS_TABLE);

	public static final String AUTHORITY = "com.jeesmon.malayalambible.providers.malayalambiblebookmarkscontentprovider";

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.malayalambible.bookmarks";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.malayalambible.bookmarks";

	public static final String INTERNAL_DB_PATH = "/data/data/com.jeesmon.malayalambible/databases";
	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "bookmarks.db";

	public static final String BOOKMARKS_TABLE = "bookmarks";

	private static final String BOOKMARKS_TABLE_CREATE = "CREATE TABLE "
			+ BOOKMARKS_TABLE + " (" + Browser.BookmarkColumns._ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ Browser.BookmarkColumns.TITLE + " TEXT, "
			+ Browser.BookmarkColumns.URL + " TEXT, "
			+ Browser.BookmarkColumns.VISITS + " INTEGER, "
			+ Browser.BookmarkColumns.DATE + " LONG, "
			+ Browser.BookmarkColumns.CREATED + " LONG, "
			+ Browser.BookmarkColumns.BOOKMARK + " INTEGER, "
			+ Browser.BookmarkColumns.FAVICON + " BLOB DEFAULT NULL);";

	private static final int BOOKMARKS = 1;
	private static final int BOOKMARKS_BY_ID = 2;

	private static final UriMatcher sUriMatcher;

	private static String mDbPath = null;
	private SQLiteDatabase mDb;
	private DatabaseHelper mDbHelper;

	private Context mContext;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, BOOKMARKS_TABLE, BOOKMARKS);
		sUriMatcher.addURI(AUTHORITY, BOOKMARKS_TABLE + "/#", BOOKMARKS_BY_ID);
	}

	@Override
	public boolean onCreate() {
		mContext = getContext();
		mDbHelper = new DatabaseHelper(getDbPath());
		mDb = mDbHelper.getWritableDatabase();
		return true;
	}

	@Override
	public int delete(Uri uri, String whereClause, String[] whereArgs) {
		int count = 0;

		switch (sUriMatcher.match(uri)) {
		case BOOKMARKS:
			count = mDb.delete(BOOKMARKS_TABLE, whereClause, whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if (count > 0) {
			mContext.getContentResolver().notifyChange(uri, null);
		}

		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case BOOKMARKS:
			return CONTENT_TYPE;
		case BOOKMARKS_BY_ID:
			return CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch (sUriMatcher.match(uri)) {
		case BOOKMARKS:
			long rowId = mDb.insert(BOOKMARKS_TABLE, null, values);
			if (rowId > 0) {
				Uri rowUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
				mContext.getContentResolver().notifyChange(rowUri, null);
				return rowUri;
			}

			throw new SQLException("Failed to insert row into " + uri);

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (sUriMatcher.match(uri)) {
		case BOOKMARKS:
			qb.setTables(BOOKMARKS_TABLE);
			break;
		case BOOKMARKS_BY_ID:
			qb.setTables(BOOKMARKS_TABLE);
			qb.appendWhere(Browser.BookmarkColumns._ID + " = "
					+ uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		Cursor c = qb.query(mDb, projection, selection, selectionArgs, null,
				null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;
		switch (sUriMatcher.match(uri)) {
		case BOOKMARKS:
			count = mDb.update(BOOKMARKS_TABLE, values, selection,
					selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if (count > 0) {
			mContext.getContentResolver().notifyChange(uri, null);
		}

		return count;
	}

	public static String getDbPath() {
		if (mDbPath == null) {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				mDbPath = Environment.getExternalStorageDirectory()
						.getAbsolutePath()
						+ "/Android/data/com.jeesmon.malayalambible/db";
			} else {
				mDbPath = INTERNAL_DB_PATH;
			}

			File dir = new File(mDbPath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}

		return mDbPath;
	}

	private static class DatabaseHelper extends SDCardSQLiteOpenHelper {
		public DatabaseHelper(String dbPath) {
			super(dbPath, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(BOOKMARKS_TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

}
