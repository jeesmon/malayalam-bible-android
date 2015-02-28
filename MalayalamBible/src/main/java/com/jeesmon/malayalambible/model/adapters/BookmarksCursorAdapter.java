package com.jeesmon.malayalambible.model.adapters;

import java.sql.Date;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.database.Cursor;
import android.provider.Browser;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.jeesmon.malayalambible.Preference;

/**
 * Cursor adapter for bookmarks.
 */
public class BookmarksCursorAdapter extends SimpleCursorAdapter {
	private static final SimpleDateFormat df = new SimpleDateFormat(
			"dd-MMM-yyyy");
	private float fontSize = 16f;

	public BookmarksCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);

		fontSize = Preference.getInstance(context).getFontSize();

		this.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				if (columnIndex == 1) {
					TextView tv = (TextView) view;
					tv.setTextSize(fontSize);
				} else if (columnIndex == 5) {
					TextView tv = (TextView) view;
					tv.setTextSize(fontSize - 4);
					try {
						long dateLong = cursor.getLong(cursor
								.getColumnIndex(Browser.BookmarkColumns.CREATED));
						if (dateLong > 0) {
							Date d = new Date(dateLong);
							tv.setText(df.format(d));
						}
					} catch (Exception e) {
					}
					return true;
				}
				return false;
			}
		});
	}
}
