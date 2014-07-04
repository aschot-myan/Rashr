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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class Notifyer {

    private static final String PREF_NAME = "notifyer";

    public static void showRootDeniedDialog(Context mContext) {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.warning)
                .setMessage(R.string.no_root)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.exit(0);
                    }
                })
                .setCancelable(BuildConfig.DEBUG)
                .show();
    }

    public static void showExceptionToast(Context mContext, String TAG, Exception e) {
        if (e != null) {
            Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    public static void showAppRateDialog(final Context mContext, final String PREF_NAME,
                                         final String PREF_KEY_HIDE_RATER) {
            new AlertDialog.Builder(mContext)
                    .setTitle(R.string.rate_title)
                    .setMessage(R.string.rate_message)
                    .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Common.setBooleanPref(mContext, PREF_NAME, PREF_KEY_HIDE_RATER, true);
                            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mContext.getPackageName())));
                        }
                    })
                    .setNeutralButton(R.string.neutral, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .setNegativeButton(R.string.never_ask, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Common.setBooleanPref(mContext, PREF_NAME, PREF_KEY_HIDE_RATER, true);
                        }
                    })
                    .show();
    }

}