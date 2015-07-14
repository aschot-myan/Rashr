package de.mkrtchyan.utils;

/**
 * Copyright (c) 2015 Aschot Mkrtchyan
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.content.Context;
import android.content.SharedPreferences;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;


public class Common {

    public static void pushFileFromRAW(Context mContext, File outputFile, int RAW, boolean Override) throws IOException {
        if (!outputFile.exists() || Override) {
            if (Override && outputFile.exists()) if (!outputFile.delete()) {
                throw new IOException(outputFile.getName() + " can't be deleted!");
            }
            InputStream is = mContext.getResources().openRawResource(RAW);
            OutputStream os = new FileOutputStream(outputFile);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
        }
    }

    public static boolean deleteFolder(File Folder, boolean AndFolder) {
        boolean failed = false;
        if (Folder.exists()
                && Folder.isDirectory()) {
            File[] files = Folder.listFiles();
            if (files != null) {
                for (File i : files) {
                    if (i.isDirectory()) {
                        /** Recursive delete */
                        failed = failed || !deleteFolder(i, AndFolder);
                    } else {
                        failed = failed || !i.delete();
                    }
                }
            }
            if (AndFolder) {
                failed = failed || !Folder.delete();
            }
        }
        return !failed;
    }

    public static boolean getBooleanPref(Context mContext, String PREF_NAME, String PREF_KEY) {
        return mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(PREF_KEY, false);
    }

    public static void setBooleanPref(Context mContext, String PREF_NAME, String PREF_KEY, Boolean value) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(PREF_KEY, value);
        editor.commit();
    }

    public static void toggleBooleanPref(Context mContext, String PREF_NAME, String PREF_KEY) {
        setBooleanPref(mContext, PREF_NAME, PREF_KEY, !Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY));
    }

    public static String getStringPref(Context mContext, String PrefName, String key) {
        return mContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE).getString(key, "");
    }

    public static void setStringPref(Context mContext, String PrefName, String key, String value) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static Integer getIntegerPref(Context mContext, String PrefName, String key) {
        return mContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE).getInt(key, 0);
    }

    public static void setIntegerPref(Context mContext, String PrefName, String key, int value) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static boolean deleteLogs(final Context context) {
        return new File(context.getFilesDir(), "commands.txt").delete();
    }

    public static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
	            inChannel.close();
	            outChannel.close();
            }
        }
    }

	static public boolean stringEndsWithArray(String string, String[] array) {
		boolean endsWith = false;

		for (String i : array) {
			endsWith = string.endsWith(i);
			if (endsWith) break;
		}

		return endsWith;

	}
    public static String fileContent(File file) throws IOException {
        String content = "";
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            content += line;
        }
        return content;
    }
}
