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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Notifyer {

    private static final String PREF_NAME = "notifyer";
    private static final String PREF_KEY_HIDE_RATER = "show_rater";
    private final Context mContext;

    public Notifyer(Context mContext) {
        this.mContext = mContext;
    }

    public static void showRootDeniedDialog(Context mContext) {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.warning)
                .setMessage(R.string.noroot)
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
            if (e.getMessage() != null) {
                Toast.makeText(mContext, e.toString() + ":  " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                Log.e(TAG, e.getMessage());

            }
        }
    }

    public static void showAppRateDialog(final Context mContext) {
        if (!Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_HIDE_RATER))
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

    public Dialog createDialog(int Title, String Message, boolean isCancelable) {
        TextView tv = new TextView(mContext);
        tv.setTextSize(20);
        ScrollView scrollView = new ScrollView(mContext);
        scrollView.addView(tv);
        tv.setText(Message);
        Dialog dialog = new Dialog(mContext);
        dialog.setTitle(Title);
        dialog.setContentView(scrollView);
        dialog.setCancelable(isCancelable);
        return dialog;
    }

    public Dialog createDialog(int Title, int Content, boolean isMessage, boolean isCancelable) {
        Dialog dialog = new Dialog(mContext);
        dialog.setTitle(Title);
        if (isMessage) {
            ScrollView scrollView = new ScrollView(mContext);
            TextView tv = new TextView(mContext);
            scrollView.addView(tv);
            dialog.setContentView(scrollView);
            tv.setTextSize(20);
            tv.setText(Content);
        } else {
            dialog.setContentView(Content);
        }
        dialog.setCancelable(isCancelable);
        return dialog;
    }

    public AlertDialog.Builder createAlertDialog(int Title, int Message, final Runnable runOnTrue) {
        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);
        mAlertDialog
                .setTitle(Title)
                .setMessage(Message)
                .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        runOnTrue.run();
                    }
                })
                .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        return mAlertDialog;
    }

    public AlertDialog.Builder createAlertDialog(int Title, String Message, final Runnable runOnTrue) {
        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);
        mAlertDialog
                .setTitle(Title)
                .setMessage(Message)
                .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        runOnTrue.run();
                    }
                })
                .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        return mAlertDialog;
    }

    public AlertDialog.Builder createAlertDialog(int Title, int Message, final Runnable runOnTrue, final Runnable runOnNeutral, final Runnable runOnNegative) {
        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);
        mAlertDialog
                .setTitle(Title)
                .setMessage(Message);
        if (runOnTrue != null) {
            mAlertDialog.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    runOnTrue.run();
                }
            });
        }
        if (runOnNegative != null) {
            mAlertDialog.setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    runOnNegative.run();
                }
            });
        }
        if (runOnNeutral != null) {
            mAlertDialog.setNeutralButton(R.string.neutral, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    runOnNeutral.run();
                }
            });
        }
        return mAlertDialog;
    }
}