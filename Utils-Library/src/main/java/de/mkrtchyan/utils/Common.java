package de.mkrtchyan.utils;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 * Copyright (c) 2017 Aschot Mkrtchyan
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
public class Common {

    public static void pushFileFromRAW(Context context, File outputFile, int RAW, boolean Override) throws IOException {
        if (!outputFile.exists() || Override) {
            if (Override && outputFile.exists()) if (!outputFile.delete()) {
                throw new IOException(outputFile.getName() + " can't be deleted!");
            }
            InputStream is = context.getResources().openRawResource(RAW);
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
            for (File i : files) {
                if (i.isDirectory()) {
                    /* Recursive delete */
                    failed = !deleteFolder(i, AndFolder);
                } else {
                    failed = !i.delete();
                }
                if (failed) break;
            }
        }
        if (AndFolder) {
            failed = failed || !Folder.delete();
        }
        return !failed;
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
            if (i.equals("")) continue;
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
            content += line + "\n";
        }
        return content;
    }

    public static int safeLongToInt(long l) throws IllegalArgumentException {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    @SuppressLint("NewApi")
    public static void copyToClipboard(Context context, String message) {
        ClipboardManager clipboard = (ClipboardManager)
                context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ClipData clip = ClipData.newPlainText("", message);
            clipboard.setPrimaryClip(clip);
        } else {
            clipboard.setText(message);
        }
    }
}
