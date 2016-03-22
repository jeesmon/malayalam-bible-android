package com.jeesmon.malayalambible;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.TextView;

import com.jeesmon.malayalambible.service.FontService;

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

		Typeface tf = FontService.getInstance(getAssets()).getTypeface();
		final Activity activity = this;

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

			if (pref.getLanguage() == Preference.LANG_MALAYALAM) {
                setTitle(getSpannableTitleString(ComplexCharacterMapper.fix(book.getName(), renderingFix), tf));
			} else {
                setTitle(book.getEnglishName());
			}

			GridView gridview = (GridView) findViewById(R.id.gridview);
			gridview.setAdapter(new ChapterButtonAdapter(this, book, this));
		}
	}

    private void showBooks(Activity activity) {
        activity.finish();
        startActivity(new Intent(this,
                MalayalamBibleActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chapters, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_books:
                showBooks(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
