package de.mkrtchyan.utils;

import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class Downloader {
    private static final String TAG = "Downloader";

    private URL mURL;
    private File mOutputFile;
    private boolean mCheckSum = false;
    private boolean mOverrideFile = false;
    private boolean mCancel = false;
    private File mChecksumFile = null;
    private String mChecksumUrl = "";
    private OnDownloadListener onDownloadListener = null;
    private OnUpdateListener onUpdateListener = null;
    private OnCancelListener onCancelListener = null;
    private Thread mDownloadThread;
    private Handler mHandler = new Handler();
    private String mReferrer;
    private boolean isDone = false;

    private Exception mError;

    public Downloader(URL url, File outputFile) {
        mURL = url;
        mOutputFile = outputFile;
    }

    public void download() {
        if (mOutputFile.exists() && !mOverrideFile) {
            Log.d(TAG, "File already downloaded");
            if (onDownloadListener != null) {
                onDownloadListener.onSuccess(mOutputFile);
            }
            return;
        }
        if (!mOutputFile.getParentFile().exists()) {
            if (!mOutputFile.getParentFile().mkdir()) {
                mError = new Exception("Cannot create parent folder");
            }
        }
        if (mOverrideFile && mOutputFile.exists()) {
            if (!mOutputFile.delete()) {
                mError = new Exception("Cannot override file");
            }
        }

        if (mError == null) {
            mDownloadThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "Connecting to " + mURL.getHost());
                        HttpURLConnection connection = (HttpURLConnection) mURL.openConnection();
                        //Workaround for twrp downloading
                        if (mReferrer != null) {
                            connection.addRequestProperty("Referer", mReferrer);
                        }
                        connection.connect();
                        //Workaround for cm downloading
                        while (connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM
                                || connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
                            // get redirect url from "location" header field
                            String newUrl = connection.getHeaderField("Location");

                            // get the cookie if need, for login
                            String cookies = connection.getHeaderField("Set-Cookie");

                            // open the new connection again
                            connection = (HttpURLConnection) new URL(newUrl).openConnection();
                            connection.setRequestProperty("Cookie", cookies);
                            connection.setRequestMethod("GET");
                            connection.setDoOutput(true);
                            connection.setDoInput(true);
                            connection.setInstanceFollowRedirects(true);
                            connection.connect();
                        }
                        FileOutputStream fileOutput = new FileOutputStream(mOutputFile);

                        InputStream inputStream = connection.getInputStream();

                        byte[] buffer = new byte[1024];
                        final int FullLength = connection.getContentLength();

                        int bufferLength;
                        int downloaded = 0;

                        Log.i(TAG, "Downloading " + mOutputFile.getName());

                        while (((bufferLength = inputStream.read(buffer)) > 0) && !mCancel) {
                            fileOutput.write(buffer, 0, bufferLength);
                            downloaded += bufferLength;
                            final int Downloaded = downloaded;
                            if (onUpdateListener != null) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        onUpdateListener.onUpdate(FullLength, Downloaded);
                                    }
                                });
                            }
                        }

                        fileOutput.close();

                        Log.i(TAG, "Download finished!");


                    } catch (IOException e) {
                        mError = e;
                        e.printStackTrace();
                    }
                    if ((!mCheckSum || !isDownloadCorrupt()) && mError == null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (onDownloadListener != null) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            onDownloadListener.onSuccess(mOutputFile);
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (onDownloadListener != null) onDownloadListener.onFail(mError);
                            }
                        });
                        mOutputFile.delete();
                    }
                }
            });
            mDownloadThread.start();

        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (onDownloadListener != null) onDownloadListener.onFail(mError);
                }
            });
        }
        isDone = true;
    }

    public void cancel() {
        mCancel = true;
        if (mOutputFile.exists()) {
            if (!mOutputFile.delete()) {
                mError = new Exception("Cannot delete output file on cancel");
            }
        }
        if (onCancelListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onCancelListener.onCancel();
                }
            });
        }
    }

    public void setReferrer(String referrer) {
        mReferrer = referrer;
    }

    public boolean isDownloadCorrupt() {
        try {
            if (!mChecksumUrl.equals("")) {
                return !MD5.verifyCheckSum(mOutputFile, new URL(mChecksumUrl));
            }
            return !MD5.verifyCheckSum(mOutputFile, mChecksumFile) && !SHA1.verifyChecksum(mOutputFile, mChecksumFile);
        } catch (IOException | SHA1.SHA1SumNotFound e) {
            Log.d(TAG, e.getMessage());
            mError = e;
            e.printStackTrace();
        }
        return true;
    }

    public void setChecksumUrl(String checksumUrl) {
        mChecksumUrl = checksumUrl;
        mChecksumFile = null;
        mCheckSum = true;
    }

    public void setChecksumFile(File checksumFile) {
        mChecksumFile = checksumFile;
        mCheckSum = mChecksumFile != null;
        mChecksumUrl = "";
    }

    public void setOverrideFile(boolean overrideFile) {
        mOverrideFile = overrideFile;
    }

    public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
    }

    public void setOnUpdateListener(OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
    }

    public void setOnCancelListener(OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    public OnDownloadListener getOnDownloadListener() {
        return onDownloadListener;
    }

    public OnUpdateListener getOnUpdateListener() {
        return onUpdateListener;
    }

    public OnCancelListener getOnCancelListener() {
        return onCancelListener;
    }

    public URL getURL() {
        return mURL;
    }

    public File getOutputFile() {
        return mOutputFile;
    }

    public interface OnDownloadListener {
        void onSuccess(File file);
        void onFail(Exception e);
    }

    public interface OnCancelListener {
        void onCancel();
    }

    public interface OnUpdateListener {
        void onUpdate(int MAX, int Downloaded);
    }

    public boolean isDone() {
        return isDone;
    }
}
