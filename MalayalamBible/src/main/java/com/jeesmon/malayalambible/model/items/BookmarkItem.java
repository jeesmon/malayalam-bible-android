package com.jeesmon.malayalambible.model.items;

/**
 * Represent a bookmark.
 */
public class BookmarkItem {
	private long mId;
	private String mTitle;
	private String mUrl;

	/**
	 * Constructor.
	 * 
	 * @param title
	 *            The bookmark title.
	 * @param url
	 *            The bookmark url.
	 */
	public BookmarkItem(long id, String title, String url) {
		mId = id;
		mTitle = title;
		mUrl = url;
	}

	/**
	 * Get the id.
	 * 
	 * @return The id.
	 */
	public long getId() {
		return mId;
	}

	/**
	 * Get the bookmark title.
	 * 
	 * @return The bookmark title.
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * Get the bookmark url.
	 * 
	 * @return The bookmark url.
	 */
	public String getUrl() {
		return mUrl;
	}

}
