package de.mkrtchyan.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class MD5 {

    public static boolean verifyCheckSum(File file, URL checksum) {
        try {
            System.out.println(checksum);
            URLConnection con = checksum.openConnection();
            InputStream is = con.getInputStream();
            byte buffer[] = new byte[1024];
            String sum = "";
            is.read(buffer);
            for (byte b : buffer) {
                if (sum.length() >= 32) break;
                sum += String.valueOf((char) b);
            }
            is.close();
            String checkSum = generateChecksum(file);
            return sum.equals(checkSum);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean verifyCheckSum(File file, File checksum) {
        try {
            String checkSum = generateChecksum(file);
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(checksum));
            while ((line = reader.readLine()) != null) {
                if (line.contains(checkSum)) {
                    return true;
                }
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String generateChecksum(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("MD5");
        RandomAccessFile f = new RandomAccessFile(file, "r");
        byte[] b = new byte[(int)f.length()];
        f.read(b);
        m.update(b);
        return new BigInteger(1,m.digest()).toString(16);
    }
}
