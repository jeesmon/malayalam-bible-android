package com.jeesmon.malayalambible;

import java.util.ArrayList;
import java.util.TreeSet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.jeesmon.malayalambible.providers.BookmarksProviderWrapper;
import com.jeesmon.malayalambible.service.FontService;

public class ChapterViewActivity extends BaseActivity implements
		IScrollListener {
	public static final int MENU_ITEM_COPY = Menu.FIRST;
	public static final int MENU_ITEM_SHARE = Menu.FIRST + 1;
	public static final int MENU_ITEM_BOOKMARK_VERSES = Menu.FIRST + 2;
	public static final int MENU_ITEM_BOOKMARK_CHAPTER = Menu.FIRST + 3;
	public static final int MENU_ITEM_BOOKMARKS = Menu.FIRST + 4;
	public static final int MENU_ITEM_CLEAR = Menu.FIRST + 5;
    public static final int MENU_ITEM_SETTINGS = Menu.FIRST + 6;
    public static final int MENU_ITEM_ABOUT = Menu.FIRST + 7;

	private static final int FLIP_PIXEL_THRESHOLD = 200;
	private static final int FLIP_TIME_THRESHOLD = 400;

	private Book book;
	private int chapterId;
	ArrayList<Integer> verseIds = null;
	private ProgressDialog dialog;

	private DataBaseAdapter adapter;
	private Cursor cursor;
	private Cursor cursorSec;

	private static boolean preferenceChanged = false;

	private ObservableScrollView oScrollViewOne;
	private ObservableScrollView oScrollViewTwo;

	int[] tl1Heights;
	int[] tl2Heights;

	private BackgroundColorSpan selectionSpan = new BackgroundColorSpan(
			ThemeUtils.getSelectionResource());
	private ArrayList<String> selectedVerses = new ArrayList<String>();

	private boolean isVerseView = false;
	private GestureDetector mGestureDetector;
	private VerseLongClickHandler mVerseLongClickHandler;

	private boolean bookmarkOnLongPress = false;

	public static void setPreferenceChanged(boolean preferenceChanged) {
		ChapterViewActivity.preferenceChanged = preferenceChanged;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(ThemeUtils.getThemeResource());
		setContentView(R.layout.chapter);

		selectedVerses.clear();

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			ArrayList<Book> books = Utils.getBooks();
			Preference pref = Preference.getInstance(this);
			int lastBook = pref.getLastBook();
			int lastChapter = pref.getLastChapter();
			if (lastBook > 0 && lastChapter > 0) {
				this.book = books.get(lastBook - 1);
				this.chapterId = lastChapter;
			} else {
				this.book = books.get(0);
				this.chapterId = 1;
			}
		} else {
			this.book = (Book) extras
					.getSerializable("com.jeesmon.malayalambible.Book");
			if (extras.containsKey("chapterId")) {
				this.chapterId = extras.getInt("chapterId");
			} else if (this.book.getSelectedChapterId() > 0) {
				this.chapterId = this.book.getSelectedChapterId();
			}

			if (this.book.getSelectedVerseIds() != null) {
				this.verseIds = this.book.getSelectedVerseIds();
			}
		}

		preferenceChanged = false;

		mGestureDetector = new GestureDetector(this, new GestureListener());
		mVerseLongClickHandler = new VerseLongClickHandler();

		getContent();
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (preferenceChanged) {
			preferenceChanged = false;
			selectionSpan = new BackgroundColorSpan(
					ThemeUtils.getSelectionResource());
			getContent();
		}
	}

	private void getContent() {
		oScrollViewOne = null;
		oScrollViewTwo = null;
		tl1Heights = null;
		tl2Heights = null;

		dialog = ProgressDialog.show(ChapterViewActivity.this, "",
				"Loading ...");
		new WorkerThread().start();
	}

	private void showContent() {
		selectedVerses.clear();

		final Preference pref = Preference.getInstance(this);
		int renderingFix = pref.getRendering();
		float fontSize = pref.getFontSize();
		bookmarkOnLongPress = pref.isBookmarkOnLongPress();

		if (pref.getSecLanguage() == Preference.LANG_NONE) {
			showSingleLanguage(renderingFix, fontSize, pref.getLanguage());
		} else {
			switch (pref.getLanguageLayout()) {
			case 0:
				showTwoLanguagesVerseByVerse(renderingFix, fontSize,
						pref.getLanguage(), pref.getSecLanguage());
				break;
			case 1:
				showTwoLanguagesSplit(renderingFix, fontSize,
						pref.getLanguage(), pref.getSecLanguage(),
						pref.getLanguageLayout());
				break;
			case 2:
				showTwoLanguagesSplit(renderingFix, fontSize,
						pref.getLanguage(), pref.getSecLanguage(),
						pref.getLanguageLayout());
				break;
			}
		}
	}

	private void showHideBackToChapter() {
		TextView backToChapter = (TextView) findViewById(R.id.backToChapter);
		if (this.isVerseView) {
			backToChapter.setVisibility(View.VISIBLE);
		} else {
			backToChapter.setVisibility(View.GONE);
		}
	}

	private void showSingleLanguage(int renderingFix, float fontSize,
			int language) {
		setTheme(ThemeUtils.getThemeResource());
		setContentView(R.layout.chapter);

		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setupToolbar();

		Resources res = getResources();
		/*
		 * Typeface tf = language == Preference.LANG_MALAYALAM ?
		 * Typeface.createFromAsset(getAssets(),
		 * res.getString(R.string.font_name)) : null;
		 */
		Typeface tf = language == Preference.LANG_MALAYALAM ? FontService.getInstance(getAssets())
				.getTypeface() : null;

		TextView tv = (TextView) findViewById(R.id.heading);
		if (tf == null) {
			tv.setText(book.getEnglishName());
		} else {
			tv.setTypeface(tf);
			tv.setText(book.getName());
		}

		tv = (TextView) findViewById(R.id.chapterNumber);
		// tv.setTextSize(fontSize);

		if (tf == null) {
			tv.setText(res.getString(R.string.chaptereng) + " " + chapterId);
		} else {
			tv.setTypeface(tf);
			tv.setText(ComplexCharacterMapper.fix(
					res.getString(R.string.chapter), renderingFix)
					+ " " + chapterId);
		}

		cursor.moveToFirst();

		int rowLayout = R.layout.verserow;

		TableLayout tl = (TableLayout) findViewById(R.id.chapterLayout);
		tl.removeAllViews();

		LayoutInflater inflater = getLayoutInflater();

		while (!cursor.isAfterLast()) {
			int verseId = cursor.getInt(0);
			String verse = tf == null ? cursor.getString(1)
					: ComplexCharacterMapper.fix(cursor.getString(1),
							renderingFix);

			TableRow tr = (TableRow) inflater.inflate(rowLayout, tl, false);
			TextView t = (TextView) tr.findViewById(R.id.verse);

			t.setTextSize(fontSize);
			if (tf != null) {
				t.setTypeface(tf);
			}
			t.setText(verseId > 0 ? verseId + ". " + verse : verse,
					TextView.BufferType.SPANNABLE);
			t.setTag("P" + verseId);
			setVerseOnLongClickHandler(t);

			tl.addView(tr);

			cursor.moveToNext();
		}

		cursor.close();
		adapter.close();

		showHideBackToChapter();
	}

	private void showTwoLanguagesVerseByVerse(int renderingFix, float fontSize,
			int language, int secLanguage) {
		setTheme(ThemeUtils.getThemeResource());
		setContentView(R.layout.chapterbothverse);

		if (cursor == null || cursor.isClosed()) {
			return;
		}

		if (cursorSec == null || cursorSec.isClosed()) {
			return;
		}

		setupToolbar();

		Resources res = getResources();
		/*
		 * Typeface tf = Typeface.createFromAsset(getAssets(),
		 * res.getString(R.string.font_name));
		 */
		Typeface tf = FontService.getInstance(getAssets()).getTypeface();
		TextView tv = (TextView) findViewById(R.id.heading);
		if (language == Preference.LANG_MALAYALAM) {
			tv.setTypeface(tf);
			tv.setText(book.getName());
		} else {
			tv.setText(book.getEnglishName());
		}

		tv = (TextView) findViewById(R.id.headingSec);
		if (secLanguage == Preference.LANG_MALAYALAM) {
			tv.setTypeface(tf);
			tv.setText(book.getName());
		} else {
			tv.setText(book.getEnglishName());
		}

		tv = (TextView) findViewById(R.id.chapterNumber);
		// tv.setTextSize(fontSize);
		if (language == Preference.LANG_MALAYALAM) {
			tv.setTypeface(tf);
			tv.setText(ComplexCharacterMapper.fix(
					res.getString(R.string.chapter), renderingFix)
					+ " " + chapterId);
		} else {
			tv.setText(res.getString(R.string.chaptereng) + " " + chapterId);
		}

		tv = (TextView) findViewById(R.id.chapterNumberSec);
		// tv.setTextSize(fontSize);
		if (secLanguage == Preference.LANG_MALAYALAM) {
			tv.setTypeface(tf);
			tv.setText(ComplexCharacterMapper.fix(
					res.getString(R.string.chapter), renderingFix)
					+ " " + chapterId);
		} else {
			tv.setText(res.getString(R.string.chaptereng) + " " + chapterId);
		}

		cursor.moveToFirst();
		cursorSec.moveToFirst();

		int rowLayout = R.layout.verserowboth;

		TableLayout tl = (TableLayout) findViewById(R.id.chapterLayout);
		tl.removeAllViews();

		LayoutInflater inflater = getLayoutInflater();

		boolean cursorAtLast = false;
		boolean cursorSecAtLast = false;

		int verseId = 0;
		String verse = null;
		int verseSecId = 0;
		String verseSec = null;

		while (!cursorAtLast || !cursorSecAtLast) {
			if (!cursorAtLast) {
				verseId = cursor.getInt(0);
				verse = language == Preference.LANG_MALAYALAM ? ComplexCharacterMapper
						.fix(cursor.getString(1), renderingFix) : cursor
						.getString(1);
			}

			if (!cursorSecAtLast) {
				verseSecId = cursorSec.getInt(0);
				verseSec = secLanguage == Preference.LANG_MALAYALAM ? ComplexCharacterMapper
						.fix(cursorSec.getString(1), renderingFix) : cursorSec
						.getString(1);
			}

			TableRow tr = (TableRow) inflater.inflate(rowLayout, tl, false);
			TextView t;

			if ((!cursorAtLast && verseId <= verseSecId) || cursorSecAtLast) {
				t = (TextView) tr.findViewById(R.id.verse);
				t.setTextSize(fontSize);
				if (language == Preference.LANG_MALAYALAM) {
					t.setTypeface(tf);
				}
				t.setText(verseId > 0 ? verseId + ". " + verse : verse,
						TextView.BufferType.SPANNABLE);
				t.setTag("P" + verseId);
				setVerseOnLongClickHandler(t);

				cursorAtLast = !cursor.moveToNext();
			}

			if ((!cursorSecAtLast && verseSecId <= verseId) || cursorAtLast) {
				t = (TextView) tr.findViewById(R.id.verseSec);
				t.setTextSize(fontSize);
				if (secLanguage == Preference.LANG_MALAYALAM) {
					t.setTypeface(tf);
				}
				t.setText(verseSecId > 0 ? verseSecId + ". " + verseSec
						: verseSec, TextView.BufferType.SPANNABLE);
				t.setTag("S" + verseSecId);
				setVerseOnLongClickHandler(t);
				cursorSecAtLast = !cursorSec.moveToNext();
			}

			tl.addView(tr);
		}

		cursor.close();
		cursorSec.close();

		adapter.close();

		showHideBackToChapter();
	}

	private void showTwoLanguagesSplit(int renderingFix, float fontSize,
			final int language, final int secLanguage, int languageLayout) {
		setTheme(ThemeUtils.getThemeResource());
		if (languageLayout == Preference.LAYOUT_BOTH_SPLIT) {
			setContentView(R.layout.chaptersplitvertical);
		} else {
			setContentView(R.layout.chaptersplithorizontal);
		}

		if (cursor == null || cursor.isClosed()) {
			return;
		}

		setupToolbar();

		Resources res = getResources();
		/*
		 * Typeface tf = Typeface.createFromAsset(getAssets(),
		 * res.getString(R.string.font_name));
		 */
		Typeface tf = FontService.getInstance(getAssets()).getTypeface();

		TextView tv = (TextView) findViewById(R.id.heading);
		if (language == Preference.LANG_MALAYALAM) {
			tv.setTypeface(tf);
			tv.setText(book.getName());
		} else {
			tv.setText(book.getEnglishName());
		}

		tv = (TextView) findViewById(R.id.headingSec);
		if (secLanguage == Preference.LANG_MALAYALAM) {
			tv.setTypeface(tf);
			tv.setText(book.getName());
		} else {
			tv.setText(book.getEnglishName());
		}

		tv = (TextView) findViewById(R.id.chapterNumber);
		// tv.setTextSize(fontSize);
		if (language == Preference.LANG_MALAYALAM) {
			tv.setTypeface(tf);
			tv.setText(ComplexCharacterMapper.fix(
					res.getString(R.string.chapter), renderingFix)
					+ " " + chapterId);
		} else {
			tv.setText(res.getString(R.string.chaptereng) + " " + chapterId);
		}

		tv = (TextView) findViewById(R.id.chapterNumberSec);
		// tv.setTextSize(fontSize);
		if (secLanguage == Preference.LANG_MALAYALAM) {
			tv.setTypeface(tf);
			tv.setText(ComplexCharacterMapper.fix(
					res.getString(R.string.chapter), renderingFix)
					+ " " + chapterId);
		} else {
			tv.setText(res.getString(R.string.chaptereng) + " " + chapterId);
		}

		cursor.moveToFirst();
		cursorSec.moveToFirst();

		int rowLayout = R.layout.verserow;

		oScrollViewOne = (ObservableScrollView) findViewById(R.id.oScrollViewOne);
		oScrollViewTwo = (ObservableScrollView) findViewById(R.id.oScrollViewTwo);

		oScrollViewOne.setScrollViewListener(this);
		oScrollViewTwo.setScrollViewListener(this);

		final TableLayout tl = (TableLayout) findViewById(R.id.chapterLayout);
		tl.removeAllViews();

		LayoutInflater inflater = getLayoutInflater();

		int lastVerseId = 0;
		boolean needZeroVerse = false;
		TableRow tr = null;
		TextView t = null;

		int tl1c = 0;
		int tl2c = 0;

		while (!cursor.isAfterLast()) {
			int verseId = cursor.getInt(0);

			String verse = language == Preference.LANG_MALAYALAM ? ComplexCharacterMapper
					.fix(cursor.getString(1), renderingFix) : cursor
					.getString(1);

			tr = (TableRow) inflater.inflate(rowLayout, tl, false);
			t = (TextView) tr.findViewById(R.id.verse);

			if (!this.isVerseView) {
				if (cursor.isFirst()) {
					int v2 = cursorSec.getInt(0);
					if (verseId > 0 && v2 == 0) {
						tl.addView(tr);
						tr = (TableRow) inflater.inflate(rowLayout, tl, false);
						t = (TextView) tr.findViewById(R.id.verse);
						tl1c++;
					} else if (verseId == 0 && v2 != 0) {
						needZeroVerse = true;
					}
				}

				if (verseId - lastVerseId > 1) {
					for (int i = 1; i <= (verseId - lastVerseId - 1); i++) {
						tl.addView(tr);
						tr = (TableRow) inflater.inflate(rowLayout, tl, false);
						t = (TextView) tr.findViewById(R.id.verse);
						tl1c++;
					}
				}
			}

			t.setTextSize(fontSize);
			if (language == Preference.LANG_MALAYALAM) {
				t.setTypeface(tf);
			}
			t.setText(verseId > 0 ? verseId + ". " + verse : verse,
					TextView.BufferType.SPANNABLE);
			t.setTag("P" + verseId);
			setVerseOnLongClickHandler(t);

			tl.addView(tr);
			tl1c++;

			lastVerseId = verseId;

			cursor.moveToNext();
		}

		cursor.close();
		lastVerseId = 0;

		ViewTreeObserver tl1Observer = tl.getViewTreeObserver();
		if (tl1Observer.isAlive()) {
			tl1Observer
					.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
						public void onGlobalLayout() {
							tl.getViewTreeObserver()
									.removeGlobalOnLayoutListener(this);
							tl1Heights = new int[tl.getChildCount()];
							for (int i = 0; i < tl.getChildCount(); i++) {
								tl1Heights[i] = ((TableRow) tl.getChildAt(i))
										.getChildAt(0).getHeight();
							}
						}
					});
		}

		final TableLayout tl2 = (TableLayout) findViewById(R.id.chapterLayoutSec);
		tl2.removeAllViews();

		while (!cursorSec.isAfterLast()) {
			int verseId = cursorSec.getInt(0);
			String verse = secLanguage == Preference.LANG_MALAYALAM ? ComplexCharacterMapper
					.fix(cursorSec.getString(1), renderingFix) : cursorSec
					.getString(1);

			tr = (TableRow) inflater.inflate(rowLayout, tl2, false);
			t = (TextView) tr.findViewById(R.id.verse);

			if (!this.isVerseView) {
				if (cursorSec.isFirst() && needZeroVerse) {
					tl2.addView(tr);
					tr = (TableRow) inflater.inflate(rowLayout, tl2, false);
					t = (TextView) tr.findViewById(R.id.verse);
					tl2c++;
				}

				if (verseId - lastVerseId > 1) {
					for (int i = 1; i <= (verseId - lastVerseId - 1); i++) {
						tl2.addView(tr);
						tr = (TableRow) inflater.inflate(rowLayout, tl2, false);
						t = (TextView) tr.findViewById(R.id.verse);
						tl2c++;
					}
				}
			}

			t.setTextSize(fontSize);
			if (secLanguage == Preference.LANG_MALAYALAM) {
				t.setTypeface(tf);
			}
			t.setText(verseId > 0 ? verseId + ". " + verse : verse,
					TextView.BufferType.SPANNABLE);
			t.setTag("S" + verseId);
			setVerseOnLongClickHandler(t);

			tl2.addView(tr);
			tl2c++;

			lastVerseId = verseId;

			cursorSec.moveToNext();
		}
		cursorSec.close();

		if (tl1c < tl2c) {
			for (int i = 1; i <= (tl2c - tl1c); i++) {
				tr = (TableRow) inflater.inflate(rowLayout, tl, false);
				t = (TextView) tr.findViewById(R.id.verse);
				tl.addView(tr);
			}
		} else if (tl1c > tl2c) {
			for (int i = 1; i <= (tl1c - tl2c); i++) {
				tr = (TableRow) inflater.inflate(rowLayout, tl2, false);
				t = (TextView) tr.findViewById(R.id.verse);
				tl2.addView(tr);
			}
		}

		ViewTreeObserver tl2Observer = tl2.getViewTreeObserver();
		if (tl2Observer.isAlive()) {
			tl2Observer
					.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
						public void onGlobalLayout() {
							tl2.getViewTreeObserver()
									.removeGlobalOnLayoutListener(this);

							tl2Heights = new int[tl2.getChildCount()];
							for (int i = 0; i < tl2.getChildCount(); i++) {
								tl2Heights[i] = ((TableRow) tl2.getChildAt(i))
										.getChildAt(0).getHeight();
							}

							if (language != secLanguage) {
								int h1 = 0;
								int h2 = 0;
								int i1 = 0;
								int i2 = 0;
								int l1 = tl1Heights.length;
								int l2 = tl2Heights.length;

								while (i1 < l1 && i2 < l2) {
									h1 = tl1Heights[i1];
									h2 = tl2Heights[i2];

									if (h1 < h2) {
										((TableRow) tl.getChildAt(i1))
												.getChildAt(0)
												.getLayoutParams().height = h2;
									} else if (h1 > h2) {
										((TableRow) tl2.getChildAt(i2))
												.getChildAt(0)
												.getLayoutParams().height = h1;
									}

									h1 = h2 = 0;
									i1++;
									i2++;
								}
								tl.requestLayout();
								tl2.requestLayout();
							}
						}
					});
		}

		adapter.close();

		showHideBackToChapter();
	}

	private void setupToolbar() {
		Button button = (Button) findViewById(R.id.backButton);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(ChapterViewActivity.this,
						MalayalamBibleActivity.class));
			}
		});

		button = (Button) findViewById(R.id.chaptersButton);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent chaptersView = new Intent(ChapterViewActivity.this,
						ChaptersActivity.class);
				chaptersView.putExtra("com.jeesmon.malayalambible.Book", book);
				startActivity(chaptersView);
			}
		});

		Button previous = (Button) findViewById(R.id.prevButton);
		if (this.chapterId > 1 || this.book.getId() > 1) {
			previous.setVisibility(View.VISIBLE);
			previous.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					showPreviousChapter();
				}
			});
		} else {
			previous.setVisibility(View.GONE);
		}

		Button next = (Button) findViewById(R.id.nextButton);
		if (this.chapterId < this.book.getChapters() || this.book.getId() < 66
				|| (this.book.getId() == 66 && this.chapterId < 22)) {
			next.setVisibility(View.VISIBLE);
			next.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					showNextChapter();
				}
			});
		} else {
			next.setVisibility(View.GONE);
		}
	}

	private class WorkerThread extends Thread {
		@Override
		public void run() {
			Preference pref = Preference.getInstance(ChapterViewActivity.this);

			adapter = new DataBaseAdapter(ChapterViewActivity.this);
			adapter.open();

			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}

			if (cursorSec != null && !cursorSec.isClosed()) {
				cursorSec.close();
			}

			String table = "verses";
			switch (pref.getLanguage()) {
			case Preference.LANG_MALAYALAM:
				table = "verses";
				break;
			case Preference.LANG_ENGLISH:
				table = "verses_kjv";
				break;
			case Preference.LANG_ENGLISH_ASV:
				table = "verses_asv";
				break;
			}

			if (pref.getSecLanguage() == Preference.LANG_NONE) {
				if (verseIds == null) {
					cursor = adapter.fetchChapter(book.getId(), chapterId,
							table);
				} else {
					cursor = adapter.fetchVerses(book.getId(), chapterId,
							table, verseIds);
				}
			} else {
				String table2 = "verses";
				switch (pref.getSecLanguage()) {
				case Preference.LANG_MALAYALAM:
					table2 = "verses";
					break;
				case Preference.LANG_ENGLISH:
					table2 = "verses_kjv";
					break;
				case Preference.LANG_ENGLISH_ASV:
					table2 = "verses_asv";
					break;
				}

				if (verseIds == null) {
					cursor = adapter.fetchChapter(book.getId(), chapterId,
							table);
					cursorSec = adapter.fetchChapter(book.getId(), chapterId,
							table2);
				} else {
					cursor = adapter.fetchVerses(book.getId(), chapterId,
							table, verseIds);
					cursorSec = adapter.fetchVerses(book.getId(), chapterId,
							table2, verseIds);
				}
			}

			pref.setLastBook(book.getId());
			pref.setLastChapter(chapterId);

			if (verseIds == null) {
				isVerseView = false;
			} else {
				isVerseView = true;
				verseIds = null;
			}

			handler.sendEmptyMessage(0);
		}

		private Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				runOnUiThread(new Runnable() {
					public void run() {
						showContent();
					}
				});
				dialog.dismiss();
			}
		};
	}

	public void onScrollChanged(ScrollView scrollView, int x, int y, int oldx,
			int oldy) {
		if (scrollView == oScrollViewOne) {
			oScrollViewTwo.scrollTo(x, y);
		} else if (scrollView == oScrollViewTwo) {
			oScrollViewOne.scrollTo(x, y);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		if (selectedVerses.size() > 0) {
			menu.add(0, MENU_ITEM_COPY, 0, R.string.copy);
			menu.add(0, MENU_ITEM_SHARE, 0, R.string.share);
			menu.add(0, MENU_ITEM_CLEAR, 0, R.string.clear);
			menu.add(0, MENU_ITEM_BOOKMARK_VERSES, 0, R.string.bookmarkVerses);
		}
		menu.add(0, MENU_ITEM_BOOKMARK_CHAPTER, 0, R.string.bookmarkChapter);
		menu.add(0, MENU_ITEM_BOOKMARKS, 0, R.string.bookmarks);
        menu.add(0, MENU_ITEM_SETTINGS, 0, R.string.settings);
        menu.add(0, MENU_ITEM_ABOUT, 0, R.string.about);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_COPY: {
			copySelectedVerses(getSelectedVerseText());
			return true;
		}
		case MENU_ITEM_SHARE: {
			shareSelectedVerses(getSelectedVerseText());
			return true;
		}
		case MENU_ITEM_CLEAR: {
			clearSelectedVerses();
			return true;
		}
		case MENU_ITEM_BOOKMARK_VERSES: {
			addBookmarkVerses();
			return true;
		}
		case MENU_ITEM_BOOKMARK_CHAPTER: {
			addBookmark();
			return true;
		}
		case MENU_ITEM_BOOKMARKS: {
			openBookmarksActivity(this);
			return true;
		}
        case MENU_ITEM_SETTINGS: {
            showPrefenceActivity(this);
            return true;
        }
        case MENU_ITEM_ABOUT: {
            showInfoActivity(this);
            return true;
        }
		}
		return false;
	}

	private void addBookmarkVerses() {
		if (selectedVerses.size() == 0) {
			return;
		}

		TreeSet<Integer> verseIds = new TreeSet<Integer>();
		for (String sid : selectedVerses) {
			try {
				verseIds.add(Integer.parseInt(sid.substring(1)));
			} catch (Exception e) {
			}
		}

		StringBuilder sb = new StringBuilder().append(this.chapterId).append(
				":");
		Integer p = 0;
		boolean inDash = false;
		for (Integer id : verseIds) {
			if (p == 0) {
				sb.append(id);
				inDash = false;
			} else if (id - p == 1) {
				if (!inDash) {
					sb.append("-");
					inDash = true;
				}
			} else {
				if (inDash) {
					sb.append(p);
				}
				sb.append(",").append(id);
				inDash = false;
			}

			p = id;
		}

		if (inDash) {
			sb.append(p);
		}

		String title = this.book.getEnglishName() + " " + sb.toString();
		String url = this.book.getId() + ":" + sb.toString();
		BookmarksProviderWrapper.setAsBookmark(getContentResolver(), -1, title,
				url, true);
		Toast.makeText(ChapterViewActivity.this, "Verses bookmarked",
				Toast.LENGTH_SHORT).show();
	}

	private void addBookmark() {
		String title = this.book.getEnglishName() + " " + this.chapterId;
		String url = this.book.getId() + ":" + this.chapterId;
		BookmarksProviderWrapper.setAsBookmark(getContentResolver(), -1, title,
				url, true);
		Toast.makeText(ChapterViewActivity.this, "Chapter bookmarked",
				Toast.LENGTH_SHORT).show();
	}

	public void onAppMenuClickEvent(View sender) {
		registerForContextMenu(sender);
		openContextMenu(sender);
		unregisterForContextMenu(sender);
	}

	public void onBackToChapterClickEvent(View sender) {
		this.isVerseView = false;
		getContent();
	}

	public void onVerseSelected(View sender) {
		try {
			TextView tv = (TextView) sender;
			Spannable sText = (Spannable) tv.getText();
			if (sText.getSpanStart(selectionSpan) > -1) {
				sText.removeSpan(selectionSpan);
				selectedVerses.remove((String) tv.getTag());
			} else {
				sText.setSpan(selectionSpan, 0, sText.length(), 0);
				selectedVerses.add((String) tv.getTag());
			}
		} catch (Exception e) {
		}
	}

	private void clearSelectedVerses() {
		TableLayout tl = (TableLayout) findViewById(R.id.chapterLayout);

		Preference pref = Preference.getInstance(ChapterViewActivity.this);
		if (pref.getSecLanguage() == Preference.LANG_NONE) {
			removeSpan(tl, 1, 0);
		} else {
			if (pref.getLanguageLayout() == Preference.LAYOUT_BOTH_VERSE) {
				removeSpan(tl, 2, 0);
				removeSpan(tl, 2, 1);
			} else {
				removeSpan(tl, 1, 0);
				tl = (TableLayout) findViewById(R.id.chapterLayoutSec);
				removeSpan(tl, 1, 0);
			}
		}

		selectedVerses.clear();
	}

	private void removeSpan(TableLayout tl, int level, int childIndex) {
		int count = tl.getChildCount();
		TextView tv = null;
		for (int i = 0; i < count; i++) {
			if (level == 1) {
				tv = (TextView) ((TableRow) tl.getChildAt(i))
						.getChildAt(childIndex);
			} else {
				tv = (TextView) ((LinearLayout) ((TableRow) tl.getChildAt(i))
						.getChildAt(0)).getChildAt(childIndex);
			}
			if (tv != null) {
				try {
					Spannable sText = (Spannable) tv.getText();
					sText.removeSpan(selectionSpan);
				} catch (Exception e) {
				}
			}
		}
	}

	private void copySelectedVerses(String text) {
		ClipboardManager ClipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipMan.setText(text);
		Toast.makeText(ChapterViewActivity.this,
				"Selected verses copied to clipboard", Toast.LENGTH_SHORT)
				.show();
	}

	private String getSelectedVerseText() {
		StringBuilder sb = new StringBuilder();

		Preference pref = Preference.getInstance(ChapterViewActivity.this);

		adapter = new DataBaseAdapter(ChapterViewActivity.this);
		adapter.open();

		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}

		if (cursorSec != null && !cursorSec.isClosed()) {
			cursorSec.close();
		}

		String table = "verses";
		switch (pref.getLanguage()) {
		case Preference.LANG_MALAYALAM:
			table = "verses";
			break;
		case Preference.LANG_ENGLISH:
			table = "verses_kjv";
			break;
		case Preference.LANG_ENGLISH_ASV:
			table = "verses_asv";
			break;
		}

		if (pref.getSecLanguage() == Preference.LANG_NONE) {
			cursor = adapter.fetchVerses(book.getId(), chapterId, table,
					selectedVerses, 'P');
		} else {
			String table2 = "verses";
			switch (pref.getSecLanguage()) {
			case Preference.LANG_MALAYALAM:
				table2 = "verses";
				break;
			case Preference.LANG_ENGLISH:
				table2 = "verses_kjv";
				break;
			case Preference.LANG_ENGLISH_ASV:
				table2 = "verses_asv";
				break;
			}

			cursor = adapter.fetchVerses(book.getId(), chapterId, table,
					selectedVerses, 'P');
			cursorSec = adapter.fetchVerses(book.getId(), chapterId, table2,
					selectedVerses, 'S');
		}

		boolean hasFirstLangSelection = false;

		if (cursor != null && !cursor.isClosed()) {
			cursor.moveToFirst();

			if (!cursor.isAfterLast()) {
				hasFirstLangSelection = true;

				if (pref.getLanguage() == Preference.LANG_MALAYALAM) {
					sb.append(book.getOriginalName());
				} else {
					sb.append(book.getEnglishName());
				}

				if (selectedVerses.size() > 1) {
					sb.append("\n");
				} else {
					sb.append(" ");
				}
			}

			while (!cursor.isAfterLast()) {
				if (cursor.getInt(0) > 0) {
					sb.append(chapterId).append(":").append(cursor.getInt(0))
							.append(" ");
				}
				sb.append(cursor.getString(1)).append("\n");
				cursor.moveToNext();
			}
			cursor.close();
		}

		if (cursorSec != null && !cursorSec.isClosed()) {
			cursorSec.moveToFirst();

			if (!cursorSec.isAfterLast()) {
				if (hasFirstLangSelection) {
					sb.append("\n");
				}

				if (pref.getSecLanguage() == Preference.LANG_MALAYALAM) {
					sb.append(book.getOriginalName());
				} else {
					sb.append(book.getEnglishName());
				}

				if (selectedVerses.size() > 1) {
					sb.append("\n");
				} else {
					sb.append(" ");
				}
			}

			while (!cursorSec.isAfterLast()) {
				if (cursorSec.getInt(0) > 0) {
					sb.append(chapterId).append(":")
							.append(cursorSec.getInt(0)).append(" ");
				}
				sb.append(cursorSec.getString(1)).append("\n");
				cursorSec.moveToNext();
			}
			cursorSec.close();
		}

		adapter.close();

		return sb.toString();
	}

	private void shareSelectedVerses(String text) {
		copySelectedVerses(text);

		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"Shared from Malayalam Bible for Android");
		startActivity(Intent.createChooser(sharingIntent, "Share verses using"));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == OPEN_BOOKMARKS_ACTIVITY) {
			Book book = getBookFromBookmark(intent);
			if (book != null) {
				this.book = book;
				this.chapterId = book.getSelectedChapterId();
				this.verseIds = book.getSelectedVerseIds();
				getContent();
			}
		}
	}

	private void showPreviousChapter() {
		if (this.chapterId > 1 || this.book.getId() > 1) {
			if (chapterId > 1) {
				chapterId--;
			} else {
				book = Utils.getBooks().get(book.getId() - 2);
				chapterId = book.getChapters();
			}
			getContent();
		}
	}

	private void showNextChapter() {
		if (this.chapterId < this.book.getChapters() || this.book.getId() < 66
				|| (this.book.getId() == 66 && this.chapterId < 22)) {
			if (chapterId < book.getChapters()) {
				chapterId++;
			} else {
				book = Utils.getBooks().get(book.getId());
				chapterId = 1;
			}
			getContent();
		}
	}

	private class GestureListener extends
			GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (e2.getEventTime() - e1.getEventTime() <= FLIP_TIME_THRESHOLD) {
				if (e2.getX() > (e1.getX() + FLIP_PIXEL_THRESHOLD)) {
					showPreviousChapter();
					return true;
				}

				// going forwards: pushing stuff to the left
				if (e2.getX() < (e1.getX() - FLIP_PIXEL_THRESHOLD)) {
					showNextChapter();
					return true;
				}
			}

			return super.onFling(e1, e2, velocityX, velocityY);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (!mGestureDetector.onTouchEvent(ev)) {
			return super.dispatchTouchEvent(ev);
		}

		return true;
	}

	private void setVerseOnLongClickHandler(View v) {
		if (bookmarkOnLongPress) {
			v.setOnLongClickListener(mVerseLongClickHandler);
		}
	}

	private class VerseLongClickHandler implements View.OnLongClickListener {
		@Override
		public boolean onLongClick(View v) {
			try {
				TextView tv = (TextView) v;
				Spannable sText = (Spannable) tv.getText();
				if (sText.getSpanStart(selectionSpan) == -1) {
					sText.setSpan(selectionSpan, 0, sText.length(), 0);
					selectedVerses.add((String) tv.getTag());
				}
			} catch (Exception e) {
			}

			addBookmarkVerses();
			return true;
		}
	}
}
