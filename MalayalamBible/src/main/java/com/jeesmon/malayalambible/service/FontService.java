package com.jeesmon.malayalambible.service;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.Log;

public class FontService {
	private static FontService instance;

    private static Typeface typeface;
    private static Typeface hackedTypeface;

	private FontService(AssetManager mgr) {

	}

	public synchronized static FontService getInstance(AssetManager mgr) {
		if (instance == null) {
			instance = new FontService(mgr);
            instance.initialize(mgr);
		}
		return instance;
	}

    private void initialize(AssetManager mgr) {
        typeface = Typeface.createFromAsset(mgr, "fonts/AnjaliNewLipi-light.ttf");
        hackedTypeface = Typeface.createFromAsset(mgr, "fonts/AnjaliOldLipi.ttf");
    }

	public Typeface getTypeface() {
		return android.os.Build.VERSION.SDK_INT >= 16 ? typeface : hackedTypeface;
	}
}
