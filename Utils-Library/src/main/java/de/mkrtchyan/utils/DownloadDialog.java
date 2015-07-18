package de.mkrtchyan.utils;

/**
 * Copyright (c) 2014 Ashot Mkrtchyan
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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class DownloadDialog extends AsyncTask<Void, Integer, Boolean> {

    private static final String TAG = "DownloadDialog";

    private Context mContext;
    private ProgressDialog downloadDialog;
    private boolean mFirstStart = true;
    private URL mURL;
    private File mOutputFile;
    private boolean mCheckSHA1 = false;
    private boolean mOverrideFile = false;
    private boolean mHide = false;
    private boolean mRetry = false;
    private boolean mCancelable = true;
    private File ChecksumFile = null;
    private DownloadDialog thisDownloadDialog = this;
    private boolean askBeforeDownload = false;
    private OnDownloadListener onDownloadListener = null;
    private boolean mErrorOccurred = false;

    private IOException ioException;
    private MalformedURLException urlException;

    public DownloadDialog(Context context, URL url, File outputFile) {
        mContext = context;
        mURL = url;
        mOutputFile = outputFile;
    }

    public void ask() {
        final DownloadDialog thisDownloadDialog = this;
        if (askBeforeDownload) {
            showDownloadNowDialog();
        } else {
            thisDownloadDialog.execute();
        }
    }

    protected void onPreExecute() {
        if (mOverrideFile)
            mErrorOccurred = mErrorOccurred || !mOutputFile.delete();
        if (!mOutputFile.getParentFile().exists()) {
            mErrorOccurred = mErrorOccurred || !mOutputFile.getParentFile().mkdir();
        }

        if (!mHide) {
            downloadDialog = new ProgressDialog(mContext);
            downloadDialog.setTitle(mContext.getResources().getString(R.string.connecting));
            downloadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            downloadDialog.setCancelable(false);
            downloadDialog.setMessage(mURL.toString());
            if (mCancelable)
                downloadDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        thisDownloadDialog.cancel(false);
                        if (mOutputFile.exists())
                            mErrorOccurred = mErrorOccurred || !mOutputFile.delete();
                    }
                });
            downloadDialog.show();
        }
    }

    protected Boolean doInBackground(Void... params) {

        if (!mOutputFile.exists() || mOverrideFile) {
            ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo != null
                    && networkInfo.isConnected()) {
                try {

                    Log.i(TAG, "Connecting to " + mURL.getHost());
                    URLConnection connection = mURL.openConnection();

                    connection.setDoOutput(true);
                    connection.connect();

                    FileOutputStream fileOutput = new FileOutputStream(mOutputFile);

                    InputStream inputStream = connection.getInputStream();

                    byte[] buffer = new byte[1024];
                    int fullLength = connection.getContentLength();

                    int bufferLength;
                    int downloaded = 0;

                    Log.i(TAG, "Downloading " + mOutputFile.getName());

                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        fileOutput.write(buffer, 0, bufferLength);
                        downloaded += bufferLength;
                        if (!mHide)
                            publishProgress(downloaded, fullLength);
                    }

                    fileOutput.close();

                    Log.i(TAG, "Download finished!");

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    urlException = e;
                    Log.i(TAG, e.getMessage());
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    ioException = e;
                    Log.i(TAG, e.getMessage());
                    return false;
                }
            } else {
                return false;
            }
        }
        return !mCheckSHA1 || !isDownloadCorrupt();
    }

    @Override
    protected void onProgressUpdate(final Integer... progress) {
        super.onProgressUpdate(progress);
        if (mFirstStart) {
            downloadDialog.dismiss();
            downloadDialog = new ProgressDialog(mContext);
            downloadDialog.setTitle(R.string.Downloading);
            downloadDialog.setMessage(mOutputFile.getName());
            downloadDialog.setCancelable(false);
            if (mCancelable)
                downloadDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        thisDownloadDialog.cancel(false);
                        if (mOutputFile.exists())
                            mOutputFile.delete();
                    }
                });
            if (progress[1] >= 0) {
                downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            } else {
                downloadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            }
            downloadDialog.show();
            downloadDialog.setMax(progress[1]);
            downloadDialog.setCancelable(false);
            mFirstStart = false;
        }
        downloadDialog.setProgress(progress[0]);
    }

    protected void onPostExecute(Boolean success) {
        if (!mHide) {
            if (downloadDialog.isShowing()) {
                downloadDialog.dismiss();
            }
        }
        if (success) {
            if (onDownloadListener != null) {
                onDownloadListener.success(mOutputFile);
            }
        } else {
            if (!mHide) {
                if (onDownloadListener != null) {
                    if (ioException != null)
                        onDownloadListener.failed(ioException);
                    if (urlException != null)
                        onDownloadListener.failed(urlException);
                }

            }
            if (mOutputFile.delete() || mRetry) {
                loop();
            }
        }

    }

    public boolean isDownloadCorrupt() {
        try {
            return !SHA1.verifyChecksum(mOutputFile, ChecksumFile);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        } catch (SHA1.SHA1SumNotFound sha1SumNotFound) {
            Log.d(TAG, sha1SumNotFound.getMessage());
            sha1SumNotFound.printStackTrace();
        }
        return true;
    }

    private void loop() {
        final DownloadDialog newInstance = new DownloadDialog(mContext, mURL, mOutputFile);
        newInstance.setOnDownloadListener(onDownloadListener);
        newInstance.setOverrideFile(mOverrideFile);
        newInstance.setCancelable(mCancelable);
        newInstance.setHidden(mHide);
        newInstance.setAskBeforeDownload(askBeforeDownload);
        newInstance.setRetry(mRetry);
        if (mCheckSHA1)
            newInstance.setChecksumFile(ChecksumFile);
        if (!mHide) {
            final AlertDialog.Builder tryAgain = new AlertDialog.Builder(mContext);
            tryAgain
                    .setMessage(String.format(mContext.getString(R.string.failed_download), mOutputFile.getName()))
                    .setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            newInstance.execute();
                        }
                    })
                    .setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .setTitle(R.string.warning)
                    .show();
        } else {
            newInstance.execute();
        }
    }

    public void setChecksumFile(File checksumFile) {
        ChecksumFile = checksumFile;
        mCheckSHA1 = ChecksumFile != null;
    }

    public void setOverrideFile(boolean overrideFile) {
        this.mOverrideFile = overrideFile;
    }

    public void setHidden(boolean hide) {
        this.mHide = hide;
    }

    public void setRetry(boolean retry) {
        this.mRetry = retry;
    }

    public void setCancelable(boolean cancelable) {
        mCancelable = cancelable;
    }

    public void setAskBeforeDownload(boolean askBeforeDownload) {
        this.askBeforeDownload = askBeforeDownload;
    }

    public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
    }

    private void showDownloadNowDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.info)
                .setMessage(String.format(mContext.getString(R.string.download_now), mOutputFile.getName()))
                .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        thisDownloadDialog.execute();
                    }
                })
                .show();
    }

    public interface OnDownloadListener {
        void success(File file);
        void failed(Exception e);
    }
}
