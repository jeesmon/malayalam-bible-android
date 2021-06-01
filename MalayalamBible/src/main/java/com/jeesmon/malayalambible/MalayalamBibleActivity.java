package com.jeesmon.malayalambible;

import java.util.ArrayList;

import com.jeesmon.malayalambible.service.FontService;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MalayalamBibleActivity extends BaseActivity {
    private static final String TAG = "MalayalamBibleActivity";

	private Context context = null;
	private static boolean preferenceChanged = false;
	private static int STORAGE_PERMISSION_CODE = 100;

	public static void setPreferenceChanged(boolean preferenceChanged) {
		MalayalamBibleActivity.preferenceChanged = preferenceChanged;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		preferenceChanged = false;
		if ((ContextCompat.checkSelfPermission(MalayalamBibleActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
				== PackageManager.PERMISSION_DENIED) && (ContextCompat.checkSelfPermission(MalayalamBibleActivity.this, Manifest.permission.RECORD_AUDIO)
				== PackageManager.PERMISSION_DENIED))
		{
			// Requesting the permission
			ActivityCompat.requestPermissions(MalayalamBibleActivity.this,
					new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO },
					STORAGE_PERMISSION_CODE);
		}
	}


	@Override
	public void onRequestPermissionsResult(int requestCode,
										   @NonNull String[] permissions,
										   @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode,
				permissions,
				grantResults);

		if (requestCode == STORAGE_PERMISSION_CODE) {
			if (grantResults.length > 0
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				getContent();
			} else {
				Toast.makeText(MalayalamBibleActivity.this,
						"Database operations cannot work without storage permissions",
						Toast.LENGTH_SHORT)
						.show();
				finish();
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (preferenceChanged) {
			preferenceChanged = false;
			getContent();
		}
	}

	private void getContent() {
		Preference pref = Preference.getInstance(this);

		setTheme(ThemeUtils.getThemeResource());
		if (pref.getLanguageLayout() == Preference.LAYOUT_SIDE_BY_SIDE) {
			setContentView(R.layout.books_sidebyside);
		} else {
			setContentView(R.layout.main);
		}

		showContent();
	}

	private void showContent() {
		// showInfo();

		Button back = (Button) findViewById(R.id.backButton);
		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		Preference pref = Preference.getInstance(this);
		int renderingFix = pref.getRendering();
		float fontSize = pref.getFontSize();

		if (pref.getSecLanguage() == Preference.LANG_NONE) {
			showSingleLanguage(renderingFix, fontSize, pref.getLanguage());
		} else {
			showTwoLanguages(renderingFix, fontSize, pref.getLanguage(),
					pref.getSecLanguage(), pref.getLanguageLayout());
		}
	}

	private void showSingleLanguage(int renderingFix, float fontSize,
			int language) {

        Log.i(TAG, "Language: " + language);

		Resources res = getResources();
		Typeface tf = language == Preference.LANG_MALAYALAM ? FontService.getInstance(getAssets())
				.getTypeface() : null;

		int rowLayout = R.layout.bookrow;
		int rowHeaderLayout = R.layout.tablerowsection;

		TextView tv = (TextView) findViewById(R.id.heading);
		if (tf == null) {
			tv.setText(R.string.bookseng);
		} else {
			tv.setTypeface(tf);
			tv.setText(ComplexCharacterMapper.fix(
					res.getString(R.string.books), renderingFix));
		}

		TableLayout tl = (TableLayout) findViewById(R.id.booksLayout);
		tl.removeAllViews();

		TableRow tr;
		TextView t;

		final ArrayList<Book> books = Utils.getBooks();

		LayoutInflater inflater = getLayoutInflater();
		Book book = null;
		for (int c = 0; c < 66; c++) {
			book = books.get(c);

			if (c == 0) {
				tr = (TableRow) inflater.inflate(rowHeaderLayout, tl, false);
				t = (TextView) tr.findViewById(R.id.section);

				t.setTextSize(fontSize);
				if (tf == null) {
					t.setText(R.string.oldtestamenteng);
				} else {
					t.setTypeface(tf);
					t.setText(R.string.oldtestament);
				}

				tl.addView(tr);
			} else if (c == 39) {
				tr = (TableRow) inflater.inflate(rowHeaderLayout, tl, false);
				t = (TextView) tr.findViewById(R.id.section);

				t.setTextSize(fontSize);
				if (tf == null) {
					t.setText(R.string.newtestamenteng);
				} else {
					t.setTypeface(tf);
					t.setText(R.string.newtestament);
				}

				tl.addView(tr);
			}

			tr = (TableRow) inflater.inflate(rowLayout, tl, false);
			t = (TextView) tr.findViewById(R.id.book);

			tr.setClickable(true);
			tr.setId(c + 1);
			tr.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Book book = books.get(v.getId() - 1);
					if (book.getChapters() == 1) {
						finish();
						Intent chapterView = new Intent(context,
								ChapterViewActivity.class);
						chapterView.putExtra("com.jeesmon.malayalambible.Book",
								book);
						chapterView.putExtra("chapterId", 1);
						startActivity(chapterView);
					} else {
						finish();
						Intent chaptersView = new Intent(context,
								ChaptersActivity.class);
						chaptersView.putExtra(
								"com.jeesmon.malayalambible.Book", book);
						startActivity(chaptersView);
					}
				}
			});
			t.setTextSize(fontSize);
			if (tf == null) {
				t.setText(book.getEnglishName());
			} else {
				t.setTypeface(tf);
				t.setText(book.getName());
			}

			tl.addView(tr);
		}
	}

	private void showTwoLanguages(int renderingFix, float fontSize,
			int language, int secLanguage, int layout) {
		Resources res = getResources();
		/*
		 * Typeface tf = Typeface.createFromAsset(getAssets(),
		 * res.getString(R.string.font_name));
		 */
		Typeface tf = FontService.getInstance(getAssets()).getTypeface();
		boolean showBoth = true;
		if (language != Preference.LANG_MALAYALAM
				&& secLanguage != Preference.LANG_MALAYALAM) {
			showBoth = false;
		}

		int rowLayout = showBoth ? R.layout.bookrowboth : R.layout.bookrow;
		int rowHeaderLayout = showBoth ? R.layout.tablerowsectionboth
				: R.layout.tablerowsection;

		TextView tv = (TextView) findViewById(R.id.heading);
		if (language == Preference.LANG_MALAYALAM) {
			tv.setTypeface(tf);
			tv.setText(ComplexCharacterMapper.fix(
					res.getString(R.string.books), renderingFix));
		} else {
			tv.setText(res.getString(R.string.bookseng));
		}

		tv = (TextView) findViewById(R.id.headingSec);
		// tv.setVisibility(View.VISIBLE);
		if (secLanguage == Preference.LANG_MALAYALAM) {
			tv.setTypeface(tf);
			tv.setText(ComplexCharacterMapper.fix(
					res.getString(R.string.books), renderingFix));
		} else {
			tv.setText(res.getString(R.string.bookseng));
		}

		TableLayout tl = (TableLayout) findViewById(R.id.booksLayout);
		tl.removeAllViews();

		TableRow tr;
		TextView t;

		final ArrayList<Book> books = Utils.getBooks();

		LayoutInflater inflater = getLayoutInflater();

		int len = 66;
		Book book = null;
		for (int c = 0; c < len; c++) {
			book = books.get(c);

			if (c == 0) {
				tr = (TableRow) inflater.inflate(rowHeaderLayout, tl, false);
				t = (TextView) tr.findViewById(R.id.section);

				t.setTextSize(fontSize);
				if (language == Preference.LANG_MALAYALAM) {
					t.setTypeface(tf);
					t.setText(R.string.oldtestament);
				} else {
					t.setText(R.string.oldtestamenteng);
				}

				if (showBoth) {
					t = (TextView) tr.findViewById(R.id.sectionSec);
					t.setTextSize(fontSize);
					if (secLanguage == Preference.LANG_MALAYALAM) {
						t.setTypeface(tf);
						t.setText(R.string.oldtestament);
					} else {
						t.setText(R.string.oldtestamenteng);
					}
				}

				tl.addView(tr);
			} else if (c == 39) {
				if (layout == Preference.LAYOUT_SIDE_BY_SIDE) {
					tl = (TableLayout) findViewById(R.id.booksLayoutNT);
				}

				tr = (TableRow) inflater.inflate(rowHeaderLayout, tl, false);
				t = (TextView) tr.findViewById(R.id.section);

				t.setTextSize(fontSize);
				if (language == Preference.LANG_MALAYALAM) {
					t.setTypeface(tf);
					t.setText(R.string.newtestament);
				} else {
					t.setText(R.string.newtestamenteng);
				}

				if (showBoth) {
					t = (TextView) tr.findViewById(R.id.sectionSec);
					t.setTextSize(fontSize);
					if (secLanguage == Preference.LANG_MALAYALAM) {
						t.setTypeface(tf);
						t.setText(R.string.newtestament);
					} else {
						t.setText(R.string.newtestamenteng);
					}
				}

				tl.addView(tr);
			}

			tr = (TableRow) inflater.inflate(rowLayout, tl, false);
			t = (TextView) tr.findViewById(R.id.book);

			tr.setClickable(true);
			tr.setId(c + 1);
			tr.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Book book = books.get(v.getId() - 1);
					if (book.getChapters() == 1) {
						finish();
						Intent chapterView = new Intent(context,
								ChapterViewActivity.class);
						chapterView.putExtra("com.jeesmon.malayalambible.Book",
								book);
						chapterView.putExtra("chapterId", 1);
						startActivity(chapterView);
					} else {
						finish();
						Intent chaptersView = new Intent(context,
								ChaptersActivity.class);
						chaptersView.putExtra(
								"com.jeesmon.malayalambible.Book", book);
						startActivity(chaptersView);
					}
				}
			});
			t.setTextSize(fontSize);
			if (language == Preference.LANG_MALAYALAM) {
				t.setTypeface(tf);
				t.setText(book.getName());
			} else {
				t.setText(book.getEnglishName());
			}

			if (showBoth) {
				t = (TextView) tr.findViewById(R.id.bookSec);
				t.setTextSize(fontSize);
				if (secLanguage == Preference.LANG_MALAYALAM) {
					t.setTypeface(tf);
					t.setText(book.getName());
				} else {
					t.setText(book.getEnglishName());
				}
			}

			tl.addView(tr);
		}
	}
}