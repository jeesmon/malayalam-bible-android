package com.jeesmon.malayalambible;

import java.io.File;

import android.app.Application;
import android.util.Log;

import com.jeesmon.malayalambible.providers.MalayalamBibleBookmarksContentProvider;
import com.jeesmon.malayalambible.utils.FileUtils;

public class MalayalamBibleApplication extends Application {
	private static final String TAG = MalayalamBibleApplication.class
			.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();

		moveBookmarksDatabase();
	}

	private void moveBookmarksDatabase() {
		File db = new File(
				MalayalamBibleBookmarksContentProvider.INTERNAL_DB_PATH,
				MalayalamBibleBookmarksContentProvider.DATABASE_NAME);
		File dbJournal = new File(
				MalayalamBibleBookmarksContentProvider.INTERNAL_DB_PATH,
				MalayalamBibleBookmarksContentProvider.DATABASE_NAME
						+ "-journal");
		if (db.exists() && db.canWrite()) {
			String dbPath = MalayalamBibleBookmarksContentProvider.getDbPath();
			if (dbPath != null
					&& !dbPath
							.equals(MalayalamBibleBookmarksContentProvider.INTERNAL_DB_PATH)) {
				Log.i(TAG, "Bookmarks db exists in internal memory");
				boolean copied = FileUtils.copyTo(dbJournal, new File(dbPath,
						MalayalamBibleBookmarksContentProvider.DATABASE_NAME
								+ "-journal"));
				if (copied) {
					copied = FileUtils
							.copyTo(db,
									new File(
											dbPath,
											MalayalamBibleBookmarksContentProvider.DATABASE_NAME));
				}

				if (copied) {
					dbJournal.delete();
					db.delete();
					Log.i(TAG, "Bookmarks db copied to sd card");
				}
			}
		}
	}
}
