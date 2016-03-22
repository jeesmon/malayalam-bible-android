package com.jeesmon.malayalambible;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;

import com.jeesmon.malayalambible.utils.Constants;

public class BaseActivity extends ActionBarActivity {
	protected static final int OPEN_BOOKMARKS_ACTIVITY = 0;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_info:
			showInfoActivity(this);
			return true;
		case R.id.action_settings:
			showPrefenceActivity(this);
			return true;
		case R.id.action_bookmarks:
			openBookmarksActivity(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void showInfoActivity(Context context) {
		Intent infoView = new Intent(context, InfoActivity.class);
		startActivity(infoView);
	}

	protected void showPrefenceActivity(Context context) {
		Intent prefView = new Intent(context, AppPreferencesActivity.class);
		startActivity(prefView);
	}

	public void openBookmarksActivity(Context context) {
		boolean groupBy = Preference.getInstance(this).isBookmarksGroupByDate();
		Intent i = new Intent(context,
				groupBy ? BookmarksExpandableListActivity.class
						: BookmarksListActivity.class);
		startActivityForResult(i, OPEN_BOOKMARKS_ACTIVITY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == OPEN_BOOKMARKS_ACTIVITY) {
			Book book = getBookFromBookmark(intent);

			if (book != null) {
				finish();

				Intent chapterView = new Intent(this, ChapterViewActivity.class);
				chapterView.putExtra("com.jeesmon.malayalambible.Book", book);
				startActivity(chapterView);
			}
		}
	}

	protected Book getBookFromBookmark(Intent intent) {
		Book book = null;
		if (intent != null) {
			Bundle b = intent.getExtras();
			if (b != null) {
				String url = b.getString(Constants.EXTRA_ID_BOOKMARK);
				try {
					String[] array = url.split(":");
					int bid = Integer.parseInt(array[0]);
					int cid = Integer.parseInt(array[1]);
					ArrayList<Integer> verseIds = null;
					if (array.length == 3) {
						verseIds = new ArrayList<Integer>();
						String[] vs = array[2].split(",");
						for (String v : vs) {
							try {
								if (v.indexOf('-') == -1) {
									verseIds.add(Integer.parseInt(v));
								} else {
									String[] se = v.split("-");
									for (int i = Integer.parseInt(se[0]); i <= Integer
											.parseInt(se[1]); i++) {
										verseIds.add(i);
									}
								}
							} catch (Exception e) {
							}
						}
					}

					ArrayList<Book> books = Utils.getBooks();
					if (bid > 0 && cid > 0) {
						book = books.get(bid - 1);
						book.setSelectedChapterId(cid);
						book.setSelectedVerseIds(verseIds);
					}
				} catch (Exception e) {
				}
			}
		}

		return book;
	}

    public SpannableString getSpannableTitleString(String str, Typeface tf) {
        SpannableString s = new SpannableString(str);
        s.setSpan(new TypefaceSpan(this, tf), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }
}
