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

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class Downloader {
    private static final String TAG = "DownloadDialog";

    private URL mURL;
    private File mOutputFile;
    private boolean mCheckSHA1 = false;
    private boolean mOverrideFile = false;
    private boolean mCancel = false;
    private File ChecksumFile = null;
    private OnDownloadListener onDownloadListener = null;
    private OnUpdateListener onUpdateListener = null;
    private boolean mErrorOccurred = false;
    private Thread mDownloadThread;

    private IOException ioException;

    public Downloader(URL url, File outputFile) {
        mURL = url;
        mOutputFile = outputFile;
    }

    public void download() {
        if (!mOutputFile.getParentFile().exists()) {
            mErrorOccurred = mErrorOccurred || !mOutputFile.getParentFile().mkdir();
            if (mOverrideFile && mOutputFile.exists())
                mErrorOccurred = mErrorOccurred || !mOutputFile.delete();
        }

        if (!mOutputFile.exists()) {
            mDownloadThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.i(TAG, "Connecting to " + mURL.getHost());
                        URLConnection connection = mURL.openConnection();

                        connection.setDoOutput(true);
                        connection.connect();

                        FileOutputStream fileOutput = new FileOutputStream(mOutputFile);

                        InputStream inputStream = connection.getInputStream();

                        byte[] buffer = new byte[1024];
                        int FullLength = connection.getContentLength();

                        int bufferLength;
                        int Downloaded = 0;

                        Log.i(TAG, "Downloading " + mOutputFile.getName());

                        while (((bufferLength = inputStream.read(buffer)) > 0) && !mCancel) {
                            fileOutput.write(buffer, 0, bufferLength);
                            Downloaded += bufferLength;
                            if (onUpdateListener != null)
                                onUpdateListener.update(FullLength, Downloaded);
                        }

                        fileOutput.close();

                        Log.i(TAG, "Download finished!");
                        if (mErrorOccurred && (!mCheckSHA1 || !isDownloadCorrupt())) {
                            if (onDownloadListener != null) onDownloadListener.success(mOutputFile);
                        } else {
                            if (onDownloadListener != null) onDownloadListener.failed(ioException);
                            mOutputFile.delete();
                        }
                    } catch (IOException e) {
                        ioException = e;
                        e.printStackTrace();
                    }
                }
            });
            mDownloadThread.start();

        }

    }

    public void cancel() {
        mCancel = true;
        if (mOutputFile.exists()) {
            mErrorOccurred = mErrorOccurred || !mOutputFile.delete();
        }
        if (onDownloadListener != null) {
            onDownloadListener.canceled();
        }
    }

    public boolean isDownloadCorrupt() {
        try {
            return !SHA1.verifyChecksum(mOutputFile, ChecksumFile);
        } catch (IOException | SHA1.SHA1SumNotFound e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    public void setChecksumFile(File checksumFile) {
        ChecksumFile = checksumFile;
        mCheckSHA1 = ChecksumFile != null;
    }

    public void setOverrideFile(boolean overrideFile) {
        this.mOverrideFile = overrideFile;
    }

    public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
    }

    public void setOnUpdateListener(OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
    }

    public URL getURL() {
        return mURL;
    }

    public File getOutputFile() {
        return mOutputFile;
    }

    public interface OnDownloadListener {
        void success(File file);
        void failed(Exception e);
        void canceled();
    }

    public interface OnUpdateListener {
        void update(int MAX, int Downloaded);
    }
}
