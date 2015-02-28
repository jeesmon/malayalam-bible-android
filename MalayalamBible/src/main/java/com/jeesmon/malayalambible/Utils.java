package com.jeesmon.malayalambible;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

public class Utils {
	private static ArrayList<Book> books = null;
	private static int renderingFix = 0;

	public static ArrayList<Book> getBooks() {
		return getBooks(false);
	}

	public static ArrayList<Book> getBooks(boolean recreate) {
		if (books == null || recreate) {
			books = new ArrayList<Book>(66);
			try {
				JSONArray ja = new JSONArray(booksJson);
				for (int i = 0; i < ja.length(); i++) {
					JSONArray ia = (JSONArray) ja.get(i);
					books.add(new Book(ia.getInt(0), ComplexCharacterMapper
							.fix(ia.getString(1), renderingFix), ia.getInt(2),
							ia.getString(3), ia.getString(1)));
				}
			} catch (JSONException e) {
			}
		}

		return books;
	}

	public static void setRenderingFix(int value) {
		renderingFix = value;
	}

	private static final String booksJson = "[[1, 'ഉല്പത്തി', 50, 'Genesis'],[2, 'പുറപ്പാട്', 40, 'Exodus'],[3, 'ലേവ്യപുസ്തകം', 27, 'Leviticus'],"
			+ "[4, 'സംഖ്യാപുസ്തകം', 36, 'Numbers'],[5, 'ആവർത്തനം', 34, 'Deuteronomy'],[6, 'യോശുവ', 24, 'Joshua'],[7, 'ന്യായാധിപന്മാർ', 21, 'Judges'],"
			+ "[8, 'രൂത്ത്', 4, 'Ruth'],[9, '1 ശമൂവേൽ', 31, '1 Samuel'],[10, '2 ശമൂവേൽ', 24, '2 Samuel'],[11, '1 രാജാക്കന്മാർ', 22, '1 Kings'],"
			+ "[12, '2 രാജാക്കന്മാർ', 25, '2 Kings'],[13, '1 ദിനവൃത്താന്തം', 29, '1 Chronicles'],[14, '2 ദിനവൃത്താന്തം', 36, '2 Chronicles'],"
			+ "[15, 'എസ്രാ', 10, 'Ezra'],[16, 'നെഹെമ്യാവു', 13, 'Nehemiah'],[17, 'എസ്ഥേർ', 10, 'Esther'],[18, 'ഇയ്യോബ്', 42, 'Job'],"
			+ "[19, 'സങ്കീർത്തനങ്ങൾ', 150, 'Psalm'],[20, 'സദൃശ്യവാക്യങ്ങൾ', 31, 'Proverbs'],[21, 'സഭാപ്രസംഗി', 12, 'Ecclesiastes'],"
			+ "[22, 'ഉത്തമ ഗീതം', 8, 'Song of Solomon'],[23, 'യെശയ്യാ', 66, 'Isaiah'],[24, 'യിരേമ്യാവു', 52, 'Jeremiah'],"
			+ "[25, 'വിലാപങ്ങൾ', 5, 'Lamentations'],[26, 'യേഹേസ്കേൽ', 48, 'Ezekiel'],[27, 'ദാനീയേൽ', 12, 'Daniel'],"
			+ "[28, 'ഹോശേയ', 14, 'Hosea'],[29, 'യോവേൽ', 3, 'Joel'],[30, 'ആമോസ്', 9, 'Amos'],[31, 'ഒബാദ്യാവു', 1, 'Obadiah'],"
			+ "[32, 'യോനാ', 4, 'Jonah'],[33, 'മീഖാ', 7, 'Micah'],[34, 'നഹൂം', 3, 'Nahum'],[35, 'ഹബക്കൂക്‍', 3, 'Habakkuk'],"
			+ "[36, 'സെഫന്യാവു', 3, 'Zephaniah'],[37, 'ഹഗ്ഗായി', 2, 'Haggai'],[38, 'സെഖർയ്യാവു', 14, 'Zechariah'],"
			+ "[39, 'മലാഖി', 4, 'Malachi'],[40, 'മത്തായി', 28, 'Matthew'],[41, 'മർക്കൊസ്', 16, 'Mark'],[42, 'ലൂക്കോസ്', 24, 'Luke'],"
			+ "[43, 'യോഹന്നാൻ', 21, 'John'],[44, 'പ്രവൃത്തികൾ', 28, 'Acts'],[45, 'റോമർ', 16, 'Romans'],[46, '1 കൊരിന്ത്യർ', 16, '1 Corinthians'],"
			+ "[47, '2 കൊരിന്ത്യർ', 13, '2 Corinthians'],[48, 'ഗലാത്യർ', 6, 'Galatians'],[49, 'എഫെസ്യർ', 6, 'Ephesians'],"
			+ "[50, 'ഫിലിപ്പിയർ', 4, 'Phillippians'],[51, 'കൊലൊസ്സ്യർ', 4, 'Colossians'],"
			+ "[52, '1 തെസ്സലൊനീക്യർ', 5, '1 Thessalonians'],[53, '2 തെസ്സലൊനീക്യർ', 3, '2 Thessalonians'],[54, '1 തിമൊഥെയൊസ്', 6, '1 Timothy'],"
			+ "[55, '2 തിമൊഥെയൊസ്', 4, '2 Timothy'],[56, 'തീത്തൊസ്', 3, 'Titus'],[57, 'ഫിലേമോൻ', 1, 'Philemon'],"
			+ "[58, 'എബ്രായർ', 13, 'Hebrews'],[59, 'യാക്കോബ്', 5, 'James'],[60, '1 പത്രൊസ്', 5, '1 Peter'],[61, '2 പത്രൊസ്', 3, '2 Peter'],"
			+ "[62, '1 യോഹന്നാൻ', 5, '1 John'],[63, '2 യോഹന്നാൻ', 1, '2 John'],[64, '3 യോഹന്നാൻ', 1, '3 John'],[65, 'യൂദാ', 1, 'Jude'],"
			+ "[66, 'വെളിപ്പാടു', 22, 'Revelation']]";
}
