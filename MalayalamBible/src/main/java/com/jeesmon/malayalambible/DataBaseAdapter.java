package com.jeesmon.malayalambible;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DataBaseAdapter {
	private Context context;
	private SQLiteDatabase database;
	private DataBaseHelper dbHelper;

	public DataBaseAdapter(Context context) {
		this.context = context;
	}

	public DataBaseAdapter open() throws SQLException {
		dbHelper = new DataBaseHelper(context);
		database = dbHelper.getDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	public Cursor fetchAllBooks() {
		return database.query("books", new String[] { "book_id",
				"MalayalamShortName", "num_chptr", "EnglishShortName" }, null,
				null, null, null, null);
	}

	public Cursor fetchChapter(int bookId, int chapterId, String table) {
		return database.query(table, new String[] { "verse_id", "verse_text" },
				"book_id = ? AND chapter_id = ?", new String[] { bookId + "",
						chapterId + "" }, null, null, null);
	}

	public Cursor fetchVerses(int bookId, int chapterId, String table,
			ArrayList<String> verses, char lang) {
		StringBuilder q = new StringBuilder("SELECT verse_id, verse_text FROM ");
		q.append(table).append(" WHERE ");
		q.append("book_id = ? AND chapter_id = ? AND verse_id IN (");
		if (verses != null) {
			int count = 0;
			for (String v : verses) {
				if (v.charAt(0) == lang) {
					if (count++ > 0) {
						q.append(",");
					}
					q.append(v.substring(1));
				}
			}
		}
		q.append(") order by verse_id");

		return database.rawQuery(q.toString(), new String[] { bookId + "",
				chapterId + "" });
	}

	public Cursor fetchVerses(int bookId, int chapterId, String table,
			ArrayList<Integer> verses) {
		StringBuilder q = new StringBuilder("SELECT verse_id, verse_text FROM ");
		q.append(table).append(" WHERE ");
		q.append("book_id = ? AND chapter_id = ? AND verse_id IN (");
		if (verses != null) {
			int count = 0;
			for (Integer v : verses) {
				if (count++ > 0) {
					q.append(",");
				}
				q.append(v);
			}
		}
		q.append(") order by verse_id");

		return database.rawQuery(q.toString(), new String[] { bookId + "",
				chapterId + "" });
	}
}
