package de.mkrtchyan.utils;

/*
 * Copyright (c) 2013 Ashot Mkrtchyan
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;


public class Common {

    public static final String PREF_NAME = "de_mkrtchyan_utils_common";

    public static void pushFileFromRAW(Context mContext, File outputFile, int RAW, boolean Override) throws IOException {
        if (!outputFile.exists() || Override) {
            if (Override)
                outputFile.delete();
            InputStream is = mContext.getResources().openRawResource(RAW);
            OutputStream os = new FileOutputStream(outputFile);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
        }
    }

    public static void deleteFolder(File Folder, boolean AndFolder) {
        if (Folder.exists()
                && Folder.isDirectory()) {
            File[] files = Folder.listFiles();
            for (File i : files) {
                if (i.isDirectory()) {
                    deleteFolder(i, AndFolder);
                } else {
                    i.delete();
                }
            }
            if (AndFolder) {
                Folder.delete();
            }
        }
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

    public static void showLogs(final Context mContext) {
        final Notifyer mNotifyer = new Notifyer(mContext);
        final Dialog LogDialog = mNotifyer.createDialog(R.string.logs_title, R.layout.dialog_command_logs, false, true);
        final TextView tvLog = (TextView) LogDialog.findViewById(R.id.tvSuLogs);
        final Button bClearLog = (Button) LogDialog.findViewById(R.id.bClearLog);
        bClearLog.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                new File(mContext.getFilesDir(), "commands.txt").delete();
                tvLog.setText("");
            }
        });
        String sLog = "";

        try {
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(mContext.openFileInput("commands.txt")));
            while ((line = br.readLine()) != null) {
                sLog = sLog + line + "\n";
            }
            br.close();
            tvLog.setText(sLog);
        } catch (FileNotFoundException e) {
            LogDialog.dismiss();
        } catch (IOException e) {
            LogDialog.dismiss();
        }
        LogDialog.show();
    }

    public static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }
}
