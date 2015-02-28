package com.jeesmon.malayalambible;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preference {
	public static final int LANG_NONE = -1;
	public static final int LANG_MALAYALAM = 0;
	public static final int LANG_ENGLISH = 1;
	public static final int LANG_ENGLISH_ASV = 2;
	public static final int LAYOUT_BOTH_VERSE = 0;
	public static final int LAYOUT_BOTH_SPLIT = 1;
	public static final int LAYOUT_SIDE_BY_SIDE = 2;

	public static final int RENDERING_DEFAULT_FIX = 0;
	public static final int RENDERING_ALTERNATE_FIX = 1;
	public static final int RENDERING_NO_FIX = 2;

	private static Preference instance;
	private SharedPreferences pref;

	private int language;
	private int secLanguage;
	private float fontSize;
	private int rendering;
	private int theme;
	private int languageLayout;

	private Preference(Context context) {
		this.pref = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static Preference getInstance(Context context) {
		if (instance == null) {
			instance = new Preference(context);
		}

		return instance;
	}

	public int getLanguage() {
		try {
			this.language = Integer.parseInt(pref.getString(
					"com.jeesmon.malayalambible.language.primary", "0"));
		} catch (Exception e) {
			this.language = 0;
		}
		return language;
	}

	public float getFontSize() {
		String font = pref.getString("com.jeesmon.malayalambible.fontsizekey",
				"4");
		switch (font.charAt(0)) {
		case '0':
			this.fontSize = 8f;
			break;
		case '1':
			this.fontSize = 10f;
			break;
		case '2':
			this.fontSize = 12f;
			break;
		case '3':
			this.fontSize = 14f;
			break;
		case '4':
			this.fontSize = 16f;
			break;
		case '5':
			this.fontSize = 18f;
			break;
		case '6':
			this.fontSize = 20f;
			break;
		case '7':
			this.fontSize = 22f;
			break;
		case '8':
			this.fontSize = 24f;
			break;
		}

		return fontSize;
	}

	public int getRendering() {
		try {
			this.rendering = Integer.parseInt(pref.getString(
					"com.jeesmon.malayalambible.rendering.option", "0"));
		} catch (Exception e) {
			this.rendering = 0;
		}
		return rendering;
	}

	public int getTheme() {
		try {
			this.theme = Integer.parseInt(pref.getString(
					"com.jeesmon.malayalambible.theme", "0"));
		} catch (Exception e) {
			this.theme = 0;
		}
		return theme;
	}

	public int getSecLanguage() {
		try {
			this.secLanguage = Integer.parseInt(pref.getString(
					"com.jeesmon.malayalambible.language.secondary", "-1"));
		} catch (Exception e) {
			this.secLanguage = 0;
		}
		return secLanguage;
	}

	public int getLanguageLayout() {
		try {
			this.languageLayout = Integer.parseInt(pref.getString(
					"com.jeesmon.malayalambible.language.layout", "0"));
		} catch (Exception e) {
			this.languageLayout = 0;
		}
		return languageLayout;
	}

	public int getLastBook() {
		return pref.getInt("com.jeesmon.malayalambible.last.book", 0);
	}

	public void setLastBook(int lastBook) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt("com.jeesmon.malayalambible.last.book", lastBook);
		editor.commit();
	}

	public int getLastChapter() {
		return pref.getInt("com.jeesmon.malayalambible.last.chapter", 0);
	}

	public void setLastChapter(int lastChapter) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt("com.jeesmon.malayalambible.last.chapter", lastChapter);
		editor.commit();
	}

	public boolean isBookmarksGroupByDate() {
		return pref.getBoolean("com.jeesmon.malayalambible.bookmark.group",
				false);
	}

	public boolean isBookmarkOnLongPress() {
		return pref.getBoolean("com.jeesmon.malayalambible.bookmark.longpress",
				true);
	}
}
