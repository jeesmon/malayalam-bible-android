package com.jeesmon.malayalambible;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

public class InfoActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(ThemeUtils.getThemeResource());
		setContentView(R.layout.info);

		final Activity activity = this;
		Button back = (Button) findViewById(R.id.backButton);
		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				activity.finish();
			}
		});

		WebView webView = (WebView) findViewById(R.id.webView);
		webView.loadUrl("file:///android_asset/info.html");
	}
}
