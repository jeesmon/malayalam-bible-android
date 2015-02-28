/*
 * Zirco Browser for Android
 * 
 * Copyright (C) 2010 - 2011 J. Devauchelle and contributors.
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

package com.jeesmon.malayalambible.model.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.Browser;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.jeesmon.malayalambible.Preference;
import com.jeesmon.malayalambible.R;
import com.jeesmon.malayalambible.model.items.BookmarkItem;

public class BookmarksExpandableListAdapter extends BaseExpandableListAdapter {
	private LayoutInflater mInflater = null;

	private int[] mItemMap;
	private int mNumberOfBins;
	private int mIdIndex;

	private Context mContext;
	private Cursor mCursor;
	private int mDateIndex;

	private ArrayList<String> groupKeys = new ArrayList<String>();

	private static final SimpleDateFormat df = new SimpleDateFormat(
			"dd-MMM-yyyy");

	private float fontSize = 16f;

	private static String lastExpandedGroup = null;
	private int lastExpandedGroupIndex = 0;

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            The current context.
	 * @param cursor
	 *            The data cursor.
	 * @param dateIndex
	 *            The date index ?
	 */
	public BookmarksExpandableListAdapter(Context context, Cursor cursor,
			int dateIndex) {
		mContext = context;
		mCursor = cursor;
		mDateIndex = dateIndex;

		mIdIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);

		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		fontSize = Preference.getInstance(context).getFontSize();

		buildMap();
	}

	/**
	 * Split the data in the cursor into several "bins"
	 */
	private void buildMap() {
		HashMap<String, Integer> itemCountMap = new HashMap<String, Integer>();
		groupKeys.clear();

		if (mCursor.moveToFirst() && mCursor.getCount() > 0) {
			while (!mCursor.isAfterLast()) {
				long date = getLong(mDateIndex);
				Date d = new Date(date);
				String sd = df.format(d);

				Integer c = 0;
				if (itemCountMap.containsKey(sd)) {
					c = itemCountMap.get(sd);
				} else {
					groupKeys.add(sd);
				}

				itemCountMap.put(sd, ++c);

				mCursor.moveToNext();
			}
		}

		mNumberOfBins = groupKeys.size();
		int[] array = new int[mNumberOfBins];
		for (int i = 0; i < mNumberOfBins; i++) {
			String k = groupKeys.get(i);
			array[i] = itemCountMap.get(k);
			if (k.equals(lastExpandedGroup)) {
				lastExpandedGroupIndex = i;
			}
		}

		mItemMap = array;
	}

	/**
	 * Get a long-typed data from mCursor.
	 * 
	 * @param cursorIndex
	 *            The column index.
	 * @return The long data.
	 */
	private long getLong(int cursorIndex) {
		return mCursor.getLong(cursorIndex);
	}

	private int groupPositionToBin(int groupPosition) {
		return groupPosition;
	}

	/**
	 * Move the cursor to the record corresponding to the given group position
	 * and child position.
	 * 
	 * @param groupPosition
	 *            The group position.
	 * @param childPosition
	 *            The child position.
	 * @return True if the move has succeeded.
	 */
	private boolean moveCursorToChildPosition(int groupPosition,
			int childPosition) {
		if (mCursor.isClosed()) {
			return false;
		}
		groupPosition = groupPositionToBin(groupPosition);
		int index = childPosition;
		for (int i = 0; i < groupPosition; i++) {
			index += mItemMap[i];
		}
		return mCursor.moveToPosition(index);
	}

	/**
	 * Create a new view.
	 * 
	 * @return The created view.
	 */
	private TextView getGenericView() {
		// Layout parameters for the ExpandableListView
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT, (int) (45 * mContext
						.getResources().getDisplayMetrics().density));

		TextView textView = new TextView(mContext);
		textView.setLayoutParams(lp);
		// Center the text vertically
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		// Set the text starting position
		textView.setPadding((int) (35 * mContext.getResources()
				.getDisplayMetrics().density), 0, 0, 0);
		textView.setTextSize(fontSize);
		return textView;
	}

	/**
	 * Create a new child view.
	 * 
	 * @return The created view.
	 */
	private View getCustomChildView() {
		View view = mInflater.inflate(R.layout.bookmark_row_group, null, false);

		return view;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		moveCursorToChildPosition(groupPosition, childPosition);

		return new BookmarkItem(mCursor.getLong(mCursor
				.getColumnIndex(Browser.BookmarkColumns._ID)),
				mCursor.getString(mCursor
						.getColumnIndex(Browser.BookmarkColumns.TITLE)),
				mCursor.getString(mCursor
						.getColumnIndex(Browser.BookmarkColumns.URL)));
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		if (moveCursorToChildPosition(groupPosition, childPosition)) {
			return getLong(mIdIndex);
		}
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View view = getCustomChildView();

		TextView titleView = (TextView) view
				.findViewById(R.id.BookmarkRow_Title);
		titleView.setTextSize(fontSize);

		BookmarkItem item = (BookmarkItem) getChild(groupPosition,
				childPosition);
		titleView.setText(item.getTitle());

		return view;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mItemMap[groupPositionToBin(groupPosition)];
	}

	@Override
	public Object getGroup(int groupPosition) {
		int binIndex = groupPositionToBin(groupPosition);

		return String.format("%s    (%d)", groupKeys.get(binIndex),
				mItemMap[binIndex]);
	}

	@Override
	public int getGroupCount() {
		return mNumberOfBins;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		TextView textView = getGenericView();
		textView.setText(getGroup(groupPosition).toString());
		return textView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public void onGroupExpanded(int groupPosition) {
		lastExpandedGroup = groupKeys.get(groupPosition);
		super.onGroupExpanded(groupPosition);
	}

	@Override
	public void onGroupCollapsed(int groupPosition) {
		if (groupKeys.get(groupPosition).equals(lastExpandedGroup)) {
			lastExpandedGroup = null;
		}
		super.onGroupCollapsed(groupPosition);
	}

	public int getLastExpandedGroupIndex() {
		return lastExpandedGroupIndex;
	}
}
