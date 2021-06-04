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

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Browser;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

import com.jeesmon.malayalambible.model.adapters.BookmarksExpandableListAdapter;
import com.jeesmon.malayalambible.model.items.BookmarkItem;
import com.jeesmon.malayalambible.providers.BookmarksProviderWrapper;
import com.jeesmon.malayalambible.utils.Constants;

/**
 * Bookmarks list activity.
 */
public class BookmarksExpandableListActivity extends ExpandableListActivity {
	private static final int MENU_DELETE_BOOKMARK = Menu.FIRST;

	private static final int ACTIVITY_ADD_BOOKMARK = 0;

	private Cursor mCursor;
	private BookmarksExpandableListAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bookmarks_expandable_list_activity);

		setTitle(R.string.BookmarksListActivity_Title);

		registerForContextMenu(getExpandableListView());

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

		mAdapter = new BookmarksExpandableListAdapter(this, mCursor,
				mCursor.getColumnIndex("CREATED"));

		setListAdapter(mAdapter);

		if (getExpandableListAdapter().getGroupCount() > 0) {
			getExpandableListView().expandGroup(
					mAdapter.getLastExpandedGroupIndex());
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

		int type = ExpandableListView
				.getPackedPositionType(info.packedPosition);
		int group = ExpandableListView
				.getPackedPositionGroup(info.packedPosition);
		int child = ExpandableListView
				.getPackedPositionChild(info.packedPosition);

		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			BookmarkItem item = (BookmarkItem) getExpandableListAdapter()
					.getChild(group, child);
			menu.setHeaderTitle(item.getTitle());

			menu.add(0, MENU_DELETE_BOOKMARK, 0,
					R.string.BookmarksListActivity_MenuDeleteBookmark);
		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuItem
				.getMenuInfo();

		int type = ExpandableListView
				.getPackedPositionType(info.packedPosition);

		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			int group = ExpandableListView
					.getPackedPositionGroup(info.packedPosition);
			int child = ExpandableListView
					.getPackedPositionChild(info.packedPosition);

			BookmarkItem item = (BookmarkItem) getExpandableListAdapter()
					.getChild(group, child);

			switch (menuItem.getItemId()) {
			case MENU_DELETE_BOOKMARK:
				BookmarksProviderWrapper.deleteBookmark(getContentResolver(),
						item.getId());
				fillData();
				break;
			default:
				break;
			}
		}

		return super.onContextItemSelected(menuItem);
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

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		BookmarkItem item = (BookmarkItem) getExpandableListAdapter().getChild(
				groupPosition, childPosition);

		Intent result = new Intent();
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

		return super.onChildClick(parent, v, groupPosition, childPosition, id);
	}
}
