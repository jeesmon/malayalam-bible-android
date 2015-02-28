package com.jeesmon.malayalambible;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreenActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ThemeUtils.setTheme(Preference.getInstance(this).getTheme());

		setTheme(ThemeUtils.getThemeResource());
		setContentView(R.layout.splash);

		final Context context = this;

		final Handler uiHandler = new Handler();
		final Runnable uiThreadRunnable = new Runnable() {
			@Override
			public void run() {
				postInitialization();
			}
		};

		new Thread() {
			public void run() {
				try {
					// check DB and create one if necessary
					new DataBaseHelper(context);
					// load book names
					Utils.setRenderingFix(Preference.getInstance(context)
							.getRendering());
					Utils.getBooks();

				} finally {
					// switch back to ui thread to continue
					uiHandler.post(uiThreadRunnable);
				}
			}
		}.start();
	}

	protected void postInitialization() {
		startActivity(new Intent(this, ChapterViewActivity.class));
		finish();
	}
}
