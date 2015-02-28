package com.jeesmon.malayalambible.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

public class FileUtils {
	private static final String TAG = FileUtils.class.getSimpleName();

	public static boolean copyTo(File from, File to) {
		boolean status = false;
		try {
			OutputStream os = new FileOutputStream(to);
			byte[] buffer = new byte[1024];
			InputStream is = new FileInputStream(from);
			int count = 0;
			while ((count = is.read(buffer)) > 0) {
				os.write(buffer, 0, count);
			}
			is.close();
			os.flush();
			os.close();
			status = true;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			status = false;
		}

		return status;
	}
}
