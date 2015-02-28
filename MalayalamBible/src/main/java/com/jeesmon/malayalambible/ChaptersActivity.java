package com.jeesmon.malayalambible;

import com.jeesmon.malayalambible.service.FontService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

public class ChaptersActivity extends BaseActivity {
	private static boolean preferenceChanged = false;

	public static void setPreferenceChanged(boolean preferenceChanged) {
		ChaptersActivity.preferenceChanged = preferenceChanged;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		preferenceChanged = false;

		getContent();
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
		setTheme(ThemeUtils.getThemeResource());
		setContentView(R.layout.chapters);

		Resources res = getResources();
		/*
		 * Typeface tf = Typeface.createFromAsset(getAssets(),
		 * res.getString(R.string.font_name));
		 */

		Typeface tf = FontService.getInstance(getAssets()).getTypeface();
		final Activity activity = this;
		Button back = (Button) findViewById(R.id.backButton);
		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				activity.finish();
				startActivity(new Intent(ChaptersActivity.this,
						MalayalamBibleActivity.class));
			}
		});

		final Preference pref = Preference.getInstance(this);
		int renderingFix = pref.getRendering();

		TextView tv = (TextView) findViewById(R.id.chapters);
		if (pref.getLanguage() == Preference.LANG_MALAYALAM) {
			tv.setTypeface(tf);
			tv.setText(ComplexCharacterMapper.fix(
					res.getString(R.string.chapters), renderingFix));
		} else {
			tv.setText(res.getString(R.string.chapterseng));
		}
		tv.setTextSize(pref.getFontSize());

		if (pref.getSecLanguage() != Preference.LANG_NONE) {
			tv = (TextView) findViewById(R.id.chaptersSec);
			// tv.setVisibility(View.VISIBLE);
			if (pref.getSecLanguage() == Preference.LANG_MALAYALAM) {
				tv.setTypeface(tf);
				tv.setText(ComplexCharacterMapper.fix(
						res.getString(R.string.chapters), renderingFix));
			} else {
				tv.setText(res.getString(R.string.chapterseng));
			}
			tv.setTextSize(pref.getFontSize());
		}

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Book book = (Book) extras
					.getSerializable("com.jeesmon.malayalambible.Book");

			tv = (TextView) findViewById(R.id.heading);
			if (pref.getLanguage() == Preference.LANG_MALAYALAM) {
				tv.setTypeface(tf);
				tv.setText(book.getName());
			} else {
				tv.setText(book.getEnglishName());
			}

			if (pref.getSecLanguage() != Preference.LANG_NONE) {
				tv = (TextView) findViewById(R.id.headingSec);
				// tv.setVisibility(View.VISIBLE);
				if (pref.getSecLanguage() == Preference.LANG_MALAYALAM) {
					tv.setTypeface(tf);
					tv.setText(book.getName());
				} else {
					tv.setText(book.getEnglishName());
				}
			}

			GridView gridview = (GridView) findViewById(R.id.gridview);
			gridview.setAdapter(new ChapterButtonAdapter(this, book, this));
		}
	}
}
