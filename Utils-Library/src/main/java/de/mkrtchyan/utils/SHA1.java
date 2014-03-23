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

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1 {

    private static final String TAG = "SHA1";

    /**
     * Verifies file's SHA1 checksum
     *
     * @param file         and name of a file that is to be verified
     * @param ChecksumFile the file with generated checksum
     * @return true if the expeceted SHA1 checksum matches the file's SHA1 checksum; false otherwise.
     */
    public static boolean verifyChecksum(final File file, final File ChecksumFile) throws IOException, SHA1SumNotFound {
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ChecksumFile)));
        while ((line = br.readLine()) != null) {
            if (line.endsWith(file.getName())) {
                Log.d(TAG, "Checksum found!");
                if (line.startsWith(generateChecksum(file))) {
                    Log.d(TAG, "Checksum correct");
                    br.close();
                    return true;
                } else {
                    Log.d(TAG, "Checksum incorrect");
                    br.close();
                    return false;
                }
            }
        }
        Log.d(TAG, "Checksum not found! Throwing Exception!");
        throw new SHA1SumNotFound("SHA1Checksum not found!");
    }

    public static String generateChecksum(File file) throws IOException {

        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            FileInputStream fis = new FileInputStream(file);

            byte[] data = new byte[1024];
            int read;
            while ((read = fis.read(data)) != -1) {
                sha1.update(data, 0, read);
            }
            byte[] hashBytes = sha1.digest();

            StringBuilder sb = new StringBuilder();
            for (byte i : hashBytes) {
                sb.append(Integer.toString((i & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static class SHA1SumNotFound extends Exception {

        public SHA1SumNotFound(String detailMessage) {
            super(detailMessage);
        }
    }

    public static class SHA1ShellException extends Exception {
        public SHA1ShellException(String detailMessage) {
            super(detailMessage);
        }
    }
}
