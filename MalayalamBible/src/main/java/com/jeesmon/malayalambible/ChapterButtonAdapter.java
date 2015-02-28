package com.jeesmon.malayalambible;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

public class ChapterButtonAdapter extends BaseAdapter {
	private Context mContext = null;
	private Book book = null;
	private Activity activity = null;

	public ChapterButtonAdapter(Context c, Book book, Activity activity) {
		this.mContext = c;
		this.book = book;
		this.activity = activity;
	}

	public int getCount() {
		return book == null ? 0 : book.getChapters();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Button button = (Button) inflater.inflate(R.layout.button, parent,
				false);
		button.setId(position + 1);
		button.setText((position + 1) + "");
		button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);

		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				book.setSelectedVerseIds(null);
				activity.finish();
				Intent chapterView = new Intent(mContext,
						ChapterViewActivity.class);
				chapterView.putExtra("com.jeesmon.malayalambible.Book", book);
				chapterView.putExtra("chapterId", v.getId());
				mContext.startActivity(chapterView);
			}
		});

		return button;
	}
}
