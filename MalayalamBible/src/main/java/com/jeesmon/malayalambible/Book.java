package com.jeesmon.malayalambible;

import java.io.Serializable;
import java.util.ArrayList;

public class Book implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private String name;
	private int chapters;
	private String englishName;
	private String originalName;
	private int selectedChapterId;
	private ArrayList<Integer> selectedVerseIds;

	public Book(int id, String name, int chapters, String englishName,
			String originalName) {
		this.id = id;
		this.name = name;
		this.chapters = chapters;
		this.englishName = englishName;
		this.originalName = originalName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getChapters() {
		return chapters;
	}

	public void setChapters(int chapters) {
		this.chapters = chapters;
	}

	public String getEnglishName() {
		return englishName;
	}

	public void setEnglishName(String englishName) {
		this.englishName = englishName;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	public int getSelectedChapterId() {
		return selectedChapterId;
	}

	public void setSelectedChapterId(int selectedChapterId) {
		this.selectedChapterId = selectedChapterId;
	}

	public ArrayList<Integer> getSelectedVerseIds() {
		return selectedVerseIds;
	}

	public void setSelectedVerseIds(ArrayList<Integer> selectedVerseIds) {
		this.selectedVerseIds = selectedVerseIds;
	}
}
