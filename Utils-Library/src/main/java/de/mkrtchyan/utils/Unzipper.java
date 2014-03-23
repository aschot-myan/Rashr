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

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Unzipper {

    private static final String TAG = "Unzipper";

    static public void unzip(final File zipFile, final File OutputFolder) {
        OutputFolder.mkdir();
        Log.d(TAG, "unzipping " + zipFile.getName() + " to " + OutputFolder.getAbsolutePath());
        new UnZipTask().execute(zipFile, OutputFolder);
    }

    private static class UnZipTask extends AsyncTask<File, Void, Boolean> {

        @SuppressWarnings("rawtypes")
        @Override
        protected Boolean doInBackground(File... params) {

            File archive = params[0];
            try {
                File destination = params[1];
                ZipFile zipfile = new ZipFile(archive);
                for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    unzipEntry(zipfile, entry, destination);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error while extracting file " + archive.getName(), e);
                return false;
            }

            return true;
        }


        private void unzipEntry(ZipFile zipfile, ZipEntry entry,
                                File outputDir) throws IOException {

            if (entry.isDirectory()) {
                new File(outputDir, entry.getName()).mkdir();
                return;
            }

            File outputFile = new File(outputDir, entry.getName());
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdir();
            }

            Log.v(TAG, "Extracting: " + entry);
            BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            int bufferLength;
            byte[] buffer = new byte[1024];
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bufferLength);
            }
            outputStream.close();
            inputStream.close();
        }
    }
}