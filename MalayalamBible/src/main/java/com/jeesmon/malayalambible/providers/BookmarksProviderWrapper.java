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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Browser;
import android.util.Log;

import com.jeesmon.malayalambible.model.items.BookmarkItem;

public class BookmarksProviderWrapper {
	private static final Uri BOOKMARKS_URI = Uri.parse("content://"
			+ MalayalamBibleBookmarksContentProvider.AUTHORITY + "/"
			+ MalayalamBibleBookmarksContentProvider.BOOKMARKS_TABLE);

	private static String[] sBookmarksProjection = new String[] {
			"_id", "TITLE",
			"URL", "VISITS",
			"DATE", "CREATED",
			"BOOKMARK", "FAVICON" };

	/**
	 * Bookmarks management.
	 */
	/**
	 * Get a Cursor on the whole content of the history/bookmarks database.
	 * 
	 * @param contentResolver
	 *            The content resolver.
	 * @return A Cursor.
	 * @see Cursor
	 */
	public static Cursor getAllRecords(ContentResolver contentResolver) {
		return contentResolver.query(BOOKMARKS_URI, sBookmarksProjection, null,
				null, null);
	}

	public static Cursor getBookmarks(ContentResolver contentResolver,
			int sortMode) {
		String whereClause = "BOOKMARK" + " = 1";

		String orderClause;
		switch (sortMode) {
		case 0:
			orderClause = "VISITS" + " DESC, "
					+ "TITLE" + " COLLATE NOCASE";
			break;
		case 1:
			orderClause = "TITLE" + " COLLATE NOCASE";
			break;
		case 2:
			orderClause = "CREATED" + " DESC";
			break;
		default:
			orderClause = "TITLE" + " COLLATE NOCASE";
			break;
		}

		return contentResolver.query(BOOKMARKS_URI, sBookmarksProjection,
				whereClause, null, orderClause);
	}

	/**
	 * Get a list of most visited bookmarks items, limited in size.
	 * 
	 * @param contentResolver
	 *            The content resolver.
	 * @param limit
	 *            The size limit.
	 * @return A list of BookmarkItem.
	 */
	public static List<BookmarkItem> getBookmarksWithLimit(
			ContentResolver contentResolver, int limit) {
		List<BookmarkItem> result = new ArrayList<BookmarkItem>();

		String whereClause = "BOOKMARK"+ " = 1";
		String orderClause = "VISITS" + " DESC";
		String[] colums = new String[] { "_id",
				"TITLE", "URL",
				"FAVICON" };

		Cursor cursor = contentResolver.query(BOOKMARKS_URI, colums,
				whereClause, null, orderClause);

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int columnId = cursor
						.getColumnIndex("_id");
				int columnTitle = cursor
						.getColumnIndex("TITLE");
				int columnUrl = cursor
						.getColumnIndex("URL");

				int count = 0;
				while (!cursor.isAfterLast() && (count < limit)) {

					BookmarkItem item = new BookmarkItem(
							cursor.getLong(columnId),
							cursor.getString(columnTitle),
							cursor.getString(columnUrl));

					result.add(item);

					count++;
					cursor.moveToNext();
				}
			}

			cursor.close();
		}

		return result;
	}

	public static BookmarkItem getBookmarkById(ContentResolver contentResolver,
			long id) {
		BookmarkItem result = null;
		String whereClause = "_id" + " = " + id;

		Cursor c = contentResolver.query(BOOKMARKS_URI, sBookmarksProjection,
				whereClause, null, null);
		if (c != null) {
			if (c.moveToFirst()) {
				String title = c.getString(c
						.getColumnIndex("TITLE"));
				String url = c.getString(c
						.getColumnIndex("URL"));
				result = new BookmarkItem(id, title, url);
			}

			c.close();
		}

		return result;
	}

	public static void deleteBookmark(ContentResolver contentResolver, long id) {
		String whereClause = "_id" + " = " + id;

		Cursor c = contentResolver.query(BOOKMARKS_URI, sBookmarksProjection,
				whereClause, null, null);
		if (c != null) {
			if (c.moveToFirst()) {
				if (c.getInt(c.getColumnIndex("BOOKMARK")) == 1) {
					if (c.getInt(c
							.getColumnIndex("VISITS")) > 0) {

						// If this record has been visited, keep it in history,
						// but remove its bookmark flag.
						ContentValues values = new ContentValues();
						values.put("BOOKMARK", 0);
						values.putNull("CREATED");

						contentResolver.update(BOOKMARKS_URI, values,
								whereClause, null);

					} else {
						// never visited, it can be deleted.
						contentResolver
								.delete(BOOKMARKS_URI, whereClause, null);
					}
				}
			}

			c.close();
		}
	}

	/**
	 * Modify a bookmark/history record. If an id is provided, it look for it
	 * and update its values. If not, values will be inserted. If no id is
	 * provided, it look for a record with the given url. It found, its values
	 * are updated. If not, values will be inserted.
	 * 
	 * @param contentResolver
	 *            The content resolver.
	 * @param id
	 *            The record id to look for.
	 * @param title
	 *            The record title.
	 * @param url
	 *            The record url.
	 * @param isBookmark
	 *            If True, the record will be a bookmark.
	 */
	public static void setAsBookmark(ContentResolver contentResolver, long id,
			String title, String url, boolean isBookmark) {

		boolean bookmarkExist = false;

		if (id != -1) {
			String[] colums = new String[] { "_id" };
			String whereClause = "_id" + " = " + id;

			Cursor cursor = contentResolver.query(BOOKMARKS_URI, colums,
					whereClause, null, null);
			bookmarkExist = (cursor != null) && (cursor.moveToFirst());
		} else {
			String[] colums = new String[] { "_id",
					"CREATED" };
			String whereClause = "URL" + " = \"" + url
					+ "\"";
			String orderClause = "CREATED" + " DESC";

			Cursor cursor = contentResolver.query(BOOKMARKS_URI, colums,
					whereClause, null, orderClause);
			bookmarkExist = (cursor != null) && (cursor.moveToFirst());
			if (bookmarkExist) {
				try {
					long dateLong = cursor.getLong(cursor
							.getColumnIndex("CREATED"));
					Date date = new Date(dateLong);
					Calendar cal = Calendar.getInstance();
					int m = cal.get(Calendar.MONTH);
					int d = cal.get(Calendar.DAY_OF_MONTH);
					int y = cal.get(Calendar.YEAR);
					cal.setTime(date);
					if (cal.get(Calendar.DAY_OF_MONTH) != d
							|| cal.get(Calendar.MONTH) != m
							|| cal.get(Calendar.YEAR) != y) {
						bookmarkExist = false;
					} else {
						id = cursor.getLong(cursor
								.getColumnIndex("_id"));
					}
				} catch (Exception e) {
					bookmarkExist = false;
				}
			}
		}

		ContentValues values = new ContentValues();
		if (title != null) {
			values.put("TITLE", title);
		}

		if (url != null) {
			values.put("URL", url);
		}

		if (isBookmark) {
			values.put("BOOKMARK", 1);
			values.put("CREATED", new Date().getTime());
		} else {
			values.put("BOOKMARK", 0);
		}

		if (bookmarkExist) {
			contentResolver.update(BOOKMARKS_URI, values,
					"_id" + " = " + id, null);
		} else {
			contentResolver.insert(BOOKMARKS_URI, values);
		}
	}

	public static void toggleBookmark(ContentResolver contentResolver, long id,
			boolean bookmark) {
		String[] colums = new String[] { "_id" };
		String whereClause = "_id" + " = " + id;

		Cursor cursor = contentResolver.query(BOOKMARKS_URI, colums,
				whereClause, null, null);
		boolean recordExists = (cursor != null) && (cursor.moveToFirst());

		if (recordExists) {
			ContentValues values = new ContentValues();

			values.put("BOOKMARK", bookmark);
			if (bookmark) {
				values.put("CREATED",
						new Date().getTime());
			} else {
				values.putNull("CREATED");
			}

			contentResolver.update(BOOKMARKS_URI, values, whereClause, null);
		}
	}

	public static Cursor getHistory(ContentResolver contentResolver) {
		String whereClause = "VISITS" + " > 0";
		String orderClause = "DATE" + " DESC";

		return contentResolver.query(BOOKMARKS_URI, sBookmarksProjection,
				whereClause, null, orderClause);
	}

	/**
	 * Delete an history record, e.g. reset the visited count and visited date
	 * if its a bookmark, or delete it if not.
	 * 
	 * @param contentResolver
	 *            The content resolver.
	 * @param id
	 *            The history id.
	 */
	public static void deleteHistoryRecord(ContentResolver contentResolver,
			long id) {
		String whereClause = "_id" + " = " + id;

		Cursor cursor = contentResolver.query(BOOKMARKS_URI,
				sBookmarksProjection, whereClause, null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				if (cursor.getInt(cursor
						.getColumnIndex("BOOKMARK")) == 1) {
					// The record is a bookmark, so we cannot delete it.
					// Instead, reset its visited count and last visited date.
					ContentValues values = new ContentValues();
					values.put("VISITS", 0);
					values.putNull("DATE");

					contentResolver.update(BOOKMARKS_URI, values, whereClause,
							null);
				} else {
					// The record is not a bookmark, we can delete it.
					contentResolver.delete(BOOKMARKS_URI, whereClause, null);
				}
			}

			cursor.close();
		}
	}

	/**
	 * Update the history: visit count and last visited date.
	 * 
	 * @param contentResolver
	 *            The content resolver.
	 * @param title
	 *            The title.
	 * @param url
	 *            The url.
	 * @param originalUrl
	 *            The original url
	 */
	public static void updateHistory(ContentResolver contentResolver,
			String title, String url, String originalUrl) {
		String[] colums = new String[] { "_id",
				"URL", "BOOKMARK",
				"VISITS" };
		String whereClause = "URL" + " = \"" + url
				+ "\" OR " + "URL" + " = \""
				+ originalUrl + "\"";

		Cursor cursor = contentResolver.query(BOOKMARKS_URI, colums,
				whereClause, null, null);

		if (cursor != null) {
			if (cursor.moveToFirst()) {

				long id = cursor.getLong(cursor
						.getColumnIndex("_id"));
				int visits = cursor.getInt(cursor
						.getColumnIndex("VISITS")) + 1;

				ContentValues values = new ContentValues();

				// If its not a bookmark, we can update the title. If we were
				// doing it on bookmarks, we would override the title choosen by
				// the user.
				if (cursor.getInt(cursor
						.getColumnIndex("BOOKMARK")) != 1) {
					values.put("TITLE", title);
				}

				values.put("DATE", new Date().getTime());
				values.put("VISITS", visits);

				contentResolver.update(BOOKMARKS_URI, values,
						"_id" + " = " + id, null);

			} else {
				ContentValues values = new ContentValues();
				values.put("TITLE", title);
				values.put("URL", url);
				values.put("DATE", new Date().getTime());
				values.put("VISITS", 1);
				values.put("BOOKMARK", 0);

				contentResolver.insert(BOOKMARKS_URI, values);
			}

			cursor.close();
		}
	}

	/**
	 * Remove from history values prior to now minus the number of days defined
	 * in preferences. Only delete history items, not bookmarks.
	 * 
	 * @param contentResolver
	 *            The content resolver.
	 */
	public static void truncateHistory(ContentResolver contentResolver,
			String prefHistorySize) {
		int historySize;
		try {
			historySize = Integer.parseInt(prefHistorySize);
		} catch (NumberFormatException e) {
			historySize = 90;
		}

		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.DAY_OF_YEAR, -historySize);

		String whereClause = "(" + "BOOKMARK"
				+ " = 0 OR " + "BOOKMARK"
				+ " IS NULL) AND " + "DATE" + " < "
				+ c.getTimeInMillis();

		try {
			contentResolver.delete(BOOKMARKS_URI, whereClause, null);
		} catch (Exception e) {
			e.printStackTrace();
			Log.w("BookmarksProviderWrapper", "Unable to truncate history: "
					+ e.getMessage());
		}
	}

	/**
	 * Update the favicon in history/bookmarks database.
	 * 
	 * @param currentActivity
	 *            The current acitivity.
	 * @param url
	 *            The url.
	 * @param originalUrl
	 *            The original url.
	 * @param favicon
	 *            The favicon.
	 */
	public static void updateFavicon(Activity currentActivity, String url,
			String originalUrl, Bitmap favicon) {
		String whereClause;

		if (!url.equals(originalUrl)) {
			whereClause = "URL" + " = \"" + url
					+ "\" OR " + "URL" + " = \""
					+ originalUrl + "\"";
		} else {
			whereClause = "URL" + " = \"" + url + "\"";
		}

		// BitmapDrawable icon =
		// ApplicationUtils.getNormalizedFaviconForBookmarks(currentActivity,
		// favicon);
		BitmapDrawable icon = new BitmapDrawable(favicon);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		icon.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, os);

		ContentValues values = new ContentValues();
		values.put("FAVICON", os.toByteArray());

		// Hack: Starting from Honeycomb, simple update of the favicon through
		// an error, it need another field to update correctly...
		if (Build.VERSION.SDK_INT >= 11) {
			values.put("URL", url);
		}

		try {
			currentActivity.getContentResolver().update(BOOKMARKS_URI, values,
					whereClause, null);
		} catch (Exception e) {
			e.printStackTrace();
			Log.w("BookmarksProviderWrapper",
					"Unable to update favicon: " + e.getMessage());
		}
	}

	/**
	 * Clear the history/bookmarks table.
	 * 
	 * @param contentResolver
	 *            The content resolver.
	 * @param clearHistory
	 *            If true, history items will be cleared.
	 * @param clearBookmarks
	 *            If true, bookmarked items will be cleared.
	 */
	public static void clearHistoryAndOrBookmarks(
			ContentResolver contentResolver, boolean clearHistory,
			boolean clearBookmarks) {

		if (!clearHistory && !clearBookmarks) {
			return;
		}

		String whereClause = null;
		if (clearHistory && clearBookmarks) {
			whereClause = null;
		} else if (clearHistory) {
			whereClause = "(" + "BOOKMARK" + " = 0) OR ("
					+ "BOOKMARK" + " IS NULL)";
		} else if (clearBookmarks) {
			whereClause = "BOOKMARK" + " = 1";
		}

		contentResolver.delete(BOOKMARKS_URI, whereClause, null);
	}

	/**
	 * Insert a full record in history/bookmarks database.
	 * 
	 * @param contentResolver
	 *            The content resolver.
	 * @param title
	 *            The record title.
	 * @param url
	 *            The record url.
	 * @param visits
	 *            The record visit count.
	 * @param date
	 *            The record last visit date.
	 * @param created
	 *            The record bookmark creation date.
	 * @param bookmark
	 *            The bookmark flag.
	 */
	public static void insertRawRecord(ContentResolver contentResolver,
			String title, String url, int visits, long date, long created,
			int bookmark) {
		ContentValues values = new ContentValues();
		values.put("TITLE", title);
		values.put("URL", url);
		values.put("VISITS", visits);

		if (date > 0) {
			values.put("DATE", date);
		} else {
			values.putNull("DATE");
		}

		if (created > 0) {
			values.put("CREATED", created);
		} else {
			values.putNull("CREATED");
		}

		if (bookmark > 0) {
			values.put("BOOKMARK", 1);
		} else {
			values.put("BOOKMARK", 0);
		}

		contentResolver.insert(BOOKMARKS_URI, values);
	}
}
