package com.jeesmon.malayalambible;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class SplashScreenActivity extends Activity {
	private static int STORAGE_PERMISSION_CODE = 100;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ThemeUtils.setTheme(Preference.getInstance(this).getTheme());

		setTheme(ThemeUtils.getThemeResource());
		setContentView(R.layout.splash);

		final Context context = this;

		if (ContextCompat.checkSelfPermission(SplashScreenActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
				== PackageManager.PERMISSION_DENIED)
		{
			// Requesting the permission
			ActivityCompat.requestPermissions(SplashScreenActivity.this,
					new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
					STORAGE_PERMISSION_CODE);
		}
		else {

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
							new DataBaseHelper(SplashScreenActivity.this);
							// load book names
							Utils.setRenderingFix(Preference.getInstance(SplashScreenActivity.this)
									.getRendering());
							Utils.getBooks();

						} finally {
							// switch back to ui thread to continue
							uiHandler.post(uiThreadRunnable);
						}
					}
				}.start();
			} else {
				Toast.makeText(SplashScreenActivity.this,
						"Database operations cannot work without storage permissions",
						Toast.LENGTH_SHORT)
						.show();
				finish();
			}
		}
	}

	protected void postInitialization() {
		startActivity(new Intent(this, ChapterViewActivity.class));
		finish();
	}
}
