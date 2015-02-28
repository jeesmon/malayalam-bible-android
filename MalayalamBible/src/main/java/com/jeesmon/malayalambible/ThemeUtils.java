package com.jeesmon.malayalambible;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;

public class ThemeUtils {
	private static int theme;

	public final static int THEME_WHITE = 0;
	public final static int THEME_BLACK = 1;
	public final static int THEME_MAROON = 2;
	public final static int THEME_LIGHT_GREEN = 3;
	public final static int THEME_YELLOW = 4;

	/**
	 * Set the theme of the Activity, and restart it by creating a new Activity
	 * of the same type.
	 */
	public static void changeToTheme(Activity activity, int theme) {
		ThemeUtils.theme = theme;
		activity.finish();

		activity.startActivity(new Intent(activity, activity.getClass()));
	}

	/** Set the theme of the activity, according to the configuration. */
	public static void onActivityCreateSetTheme(Activity activity) {
		switch (theme) {
		default:
		case THEME_WHITE:
			activity.setTheme(R.style.Theme_White);
			break;
		case THEME_BLACK:
			activity.setTheme(R.style.Theme_Black);
			break;
		case THEME_MAROON:
			activity.setTheme(R.style.Theme_Maroon);
			break;
		case THEME_LIGHT_GREEN:
			activity.setTheme(R.style.Theme_LightGreen);
			break;
		case THEME_YELLOW:
			activity.setTheme(R.style.Theme_Yellow);
			break;
		}
	}

	public static int getThemeResource() {
		int resource = 0;
		switch (theme) {
		default:
		case THEME_WHITE:
			resource = R.style.Theme_White;
			break;
		case THEME_BLACK:
			resource = R.style.Theme_Black;
			break;
		case THEME_MAROON:
			resource = R.style.Theme_Maroon;
			break;
		case THEME_LIGHT_GREEN:
			resource = R.style.Theme_LightGreen;
			break;
		case THEME_YELLOW:
			resource = R.style.Theme_Yellow;
			break;
		}

		return resource;
	}

	public static void setTheme(int theme) {
		ThemeUtils.theme = theme;
	}

	public static int getTheme() {
		return theme;
	}

	public static int getSelectionResource() {
		int resource = 0;
		switch (theme) {
		default:
		case THEME_WHITE:
		case THEME_LIGHT_GREEN:
		case THEME_YELLOW:
			resource = Color.YELLOW;
			break;
		case THEME_MAROON:
		case THEME_BLACK:
			resource = Color.GRAY;
			break;
		}

		return resource;
	}
}
