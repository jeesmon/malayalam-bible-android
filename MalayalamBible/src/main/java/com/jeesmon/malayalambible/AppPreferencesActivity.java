package com.jeesmon.malayalambible;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AppPreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
	
	@Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
	
	@Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		MalayalamBibleActivity.setPreferenceChanged(true);
		ChaptersActivity.setPreferenceChanged(true);
		ChapterViewActivity.setPreferenceChanged(true);
		
		if("com.jeesmon.malayalambible.theme".equals(key)) {
			try {
				ThemeUtils.setTheme(Integer.parseInt(sharedPreferences.getString(key, "0")));
			}
			catch(Exception e) {
				ThemeUtils.setTheme(0);
			}
		}
		else if("com.jeesmon.malayalambible.rendering.option".equals(key)) {
			int currentapiVersion = android.os.Build.VERSION.SDK_INT;
			if(currentapiVersion<16){
				try {
					Utils.setRenderingFix(Integer.parseInt(sharedPreferences.getString(key, "0")));
				}
				catch(Exception e) {
					Utils.setRenderingFix(0);
				}
				Utils.getBooks(true);
			}
		}
    }
}
