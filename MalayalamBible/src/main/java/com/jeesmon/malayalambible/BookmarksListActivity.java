/*
 * Copied from Zirco Browser for Android
 * 
 * Copyright (C) 2010 J. Devauchelle and contributors.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package com.jeesmon.malayalambible;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Browser;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.jeesmon.malayalambible.model.adapters.BookmarksCursorAdapter;
import com.jeesmon.malayalambible.model.items.BookmarkItem;
import com.jeesmon.malayalambible.providers.BookmarksProviderWrapper;
import com.jeesmon.malayalambible.utils.Constants;

/**
 * Bookmarks list activity.
 */
public class BookmarksListActivity extends Activity {
	private static final int MENU_DELETE_BOOKMARK = Menu.FIRST;

	private static final int ACTIVITY_ADD_BOOKMARK = 0;

	private Cursor mCursor;
	private BookmarksCursorAdapter mCursorAdapter;

	private ListView mList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bookmarks_list_activity);

		setTitle(R.string.BookmarksListActivity_Title);

		View emptyView = findViewById(R.id.BookmarksListActivity_EmptyTextView);
		mList = (ListView) findViewById(R.id.BookmarksListActivity_List);

		mList.setEmptyView(emptyView);

		mList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> l, View v, int position,
					long id) {
				Intent result = new Intent();
				BookmarkItem item = BookmarksProviderWrapper.getBookmarkById(
						getContentResolver(), id);
				if (item != null) {
					result.putExtra(Constants.EXTRA_ID_BOOKMARK, item.getUrl());
				} else {
					result.putExtra(Constants.EXTRA_ID_BOOKMARK,
							Constants.DEFAULT_BOOKMARK);
				}

				if (getParent() != null) {
					getParent().setResult(RESULT_OK, result);
				} else {
					setResult(RESULT_OK, result);
				}

				finish();
			}
		});

		registerForContextMenu(mList);

		fillData();
	}

	@Override
	protected void onDestroy() {
		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
		}
		super.onDestroy();
	}

	@Override
	public void startManagingCursor(Cursor c) {
		if (c == null) {
			return;
		}
		super.startManagingCursor(c);
	}

	/**
	 * Fill the bookmark to the list UI.
	 */
	private void fillData() {
		mCursor = BookmarksProviderWrapper
				.getBookmarks(getContentResolver(), 2);
		startManagingCursor(mCursor);

		String[] from = new String[] { Browser.BookmarkColumns.TITLE,
				Browser.BookmarkColumns.CREATED };
		int[] to = new int[] { R.id.BookmarkRow_Title, R.id.BookmarkRow_Created };

		mCursorAdapter = new BookmarksCursorAdapter(this,
				R.layout.bookmark_row, mCursor, from, to);

		mList.setAdapter(mCursorAdapter);

		setAnimation();
	}

	/**
	 * Set the list loading animation.
	 */
	private void setAnimation() {
		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(100);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				-1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(100);
		set.addAnimation(animation);

		LayoutAnimationController controller = new LayoutAnimationController(
				set, 0.5f);

		mList.setLayoutAnimation(controller);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		long id = ((AdapterContextMenuInfo) menuInfo).id;
		if (id != -1) {
			BookmarkItem item = BookmarksProviderWrapper.getBookmarkById(
					getContentResolver(), id);
			if (item != null) {
				menu.setHeaderTitle(item.getTitle());
			}
		}

		menu.add(0, MENU_DELETE_BOOKMARK, 0,
				R.string.BookmarksListActivity_MenuDeleteBookmark);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case MENU_DELETE_BOOKMARK:
			BookmarksProviderWrapper.deleteBookmark(getContentResolver(),
					info.id);
			fillData();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		switch (requestCode) {
		case ACTIVITY_ADD_BOOKMARK:
			if (resultCode == RESULT_OK) {
				fillData();
			}
			break;

		default:
			break;
		}
	}

}
