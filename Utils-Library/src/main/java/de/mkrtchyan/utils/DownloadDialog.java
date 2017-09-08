package de.mkrtchyan.utils;

/*
 * Copyright (c) 2016 Aschot Mkrtchyan
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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.io.File;


public class DownloadDialog extends ProgressDialog {

    private static final String TAG = "DownloadDialog";

    private Context mContext;
    private boolean mFirstStart = true;
    private boolean mCancelable = true;
    private DownloadDialog mDownloadDialog = this;
    private boolean askBeforeDownload = false;
    private Downloader mDownloader;
    private OnDownloadListener onDownloadListener = null;
    private ProgressDialog ConnectingDialog;

    public DownloadDialog(Context context, Downloader downloader) {
        super(context);
        mContext = context;
        mDownloader = downloader;
        if (mDownloader.getOnCancelListener() != null
                || mDownloader.getOnDownloadListener() != null
                || mDownloader.getOnUpdateListener() != null) {
            throw new IllegalStateException("Downloader should have blank listeners");
        }
        ConnectingDialog = new ProgressDialog(mContext);
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mDownloader.cancel();
            }
        });
        ConnectingDialog.setCancelable(false);
        setCancelable(false);
    }

    public void ask() {
        if (askBeforeDownload) {
            showDownloadNowDialog();
        } else {
            download();
        }
    }

    public void download() {
        mDownloader.setOnDownloadListener(new Downloader.OnDownloadListener() {
            @Override
            public void onSuccess(File file) {
                mDownloadDialog.dismiss();
                if (onDownloadListener != null) {
                    onDownloadListener.onSuccess(file);
                }
            }

            @Override
            public void onFail(Exception e) {
                ConnectingDialog.dismiss();
                mDownloadDialog.dismiss();
                if (e != null) {
                    Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show();
                }
                if (onDownloadListener != null) {
                    onDownloadListener.onFail(e);
                }
            }
        });
        ConnectingDialog.setTitle(mContext.getResources().getString(R.string.connecting));
        ConnectingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        ConnectingDialog.setMessage(mDownloader.getURL().toString());
        if (mCancelable) {
            ConnectingDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mDownloader.cancel();
                }
            });
        }
        ConnectingDialog.show();

        mDownloader.setOnUpdateListener(new Downloader.OnUpdateListener() {
            @Override
            public void onUpdate(int MAX, int Downloaded) {
                updateProgress(MAX, Downloaded);
            }
        });
        mDownloader.download();
        Log.i(TAG, "Download finished!");

    }

    private void updateProgress(int fullLength, int downloaded) {
        if (mFirstStart) {
            ConnectingDialog.dismiss();
            setTitle(R.string.Downloading);
            setMessage(mDownloader.getOutputFile().getName());
            if (mCancelable)
                setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDownloader.cancel();
                    }
                });
            if (fullLength > 0) {
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            } else {
                setProgressStyle(ProgressDialog.STYLE_SPINNER);
            }
            show();
            setMax(fullLength);
            mFirstStart = false;
        }
        setProgress(downloaded);
    }

    public void setAskBeforeDownload(boolean askBeforeDownload) {
        this.askBeforeDownload = askBeforeDownload;
    }

    private void showDownloadNowDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.info)
                .setMessage(String.format(mContext.getString(R.string.download_now), mDownloader.getOutputFile().getName()))
                .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDownloadDialog.download();
                    }
                })
                .setNegativeButton(R.string.negative, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void retry() {
        final AlertDialog.Builder tryAgain = new AlertDialog.Builder(mContext);
        tryAgain
                .setMessage(String.format(mContext.getString(R.string.failed_download),
                        mDownloader.getOutputFile().getName()))
                .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mDownloadDialog.download();
                    }
                })
                .setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setTitle(R.string.warning)
                .show();
    }

    public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
    }
    public interface OnDownloadListener {
        void onSuccess(File file);
        void onFail(Exception e);
    }
}
