package de.mkrtchyan.recoverytools;

import android.os.Environment;

import java.io.File;

/**
 * Copyright (c) 2014 Aschot Mkrtchyan
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class Constants {

    public static final String RASHR_TAG            =    "Rashr";
    public static final String FLASH_UTIL_TAG       =    "FlashUtil";
    public static final String DEVICE_TAG           =    "Device";
    public static final String FLASH_AS_TAG         =    "FlashAs";

    /**
     * Declaring setting names
     */
    public static final String PREF_KEY_HISTORY     =    "last__history_";
    public static final String PREF_KEY_ADS         =    "show_ads";
    public static final String PREF_KEY_CUR_VER     =    "current_version";
    public static final String PREF_KEY_FIRST_RUN   =    "first_run";
    public static final String PREF_KEY_HIDE_RATER  =    "show_rater";
    public static final String PREF_KEY_SHOW_UNIFIED =   "show_unified";

    /**
     * Google
     */
    public static final boolean GOOGLE_PLAY        =     true;
    public static final String GOOGLE_PUBKEY       =     "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhHa/9/sYU2dbF6nQqGzNktvxb+83Ed/inkK8cbiEkcRjw/t/Okge6UghlyYEXcZLJL9TDPAlraktUZZ/XH8+ZpgdNlO+UeQTD4Yl9ReZ/ujQ151g/RLrVNi7NF4SQ1jD20RmX2lCUhbl5cPi6UKL/bHFeZwjE0pOr48svW0nXbRfpgSSk3V/DaV1igTX66DuFUITKi0gQGD8XAVsrOcQRQtr4wHfdgyMQR9m0vPPzpFoDD8SZZFCp9UgvuzqdwYqY8kr7ZcyxuQhaNlcx74hpFQ9MJteRTII+ii/pHfWDh0hDMqcodm4UD9rISmPSvlLR3amfSg4Vm6ObWFiVe4qVwIDAQAB";
    public static final String[] GOOGLE_CATALOG    =    {
                                                         "donate_0_50",
                                                         "donate_1",
                                                         "donate_2",
                                                         "donate_3",
                                                         "donate_5"
                                                        };

    /**
     * Flattr
     */
    public static final boolean FLATTR             =     true;
    public static final String FLATTR_PROJECT_URL  =     "http://github.com/DsLNeXuS/Rashr";
    public static final String FLATTR_URL          =     "flattr.com/thing/1853888/ashotmkrtchyan1995Recovery-Tools-on-GitHub";

    /**
     * Web Address for download Recovery and Kernel IMGs
     */
    public static final String RECOVERY_URL        =     "http://dslnexus.de/Android/recoveries";
    public static final String KERNEL_URL          =     "http://dslnexus.de/Android/kernel";
    public static final String RECOVERY_SUMS_URL   =     "http://dslnexus.de/Android/";
    public static final String KERNEL_SUMS_URL     =     "http://dslnexus.de/Android/";
    /**
     * Used folder and files
     */
    public static final File PathToSd              =      Environment.getExternalStorageDirectory();
    public static final File PathToRashr           =      new File(PathToSd, "Rashr");
    public static final File PathToRecoveries      =      new File(PathToRashr, "recoveries");
    public static final File PathToStockRecovery   =      new File(PathToRecoveries, "stock");
    public static final File PathToCWM             =      new File(PathToRecoveries, "clockworkmod");
    public static final File PathToTWRP            =      new File(PathToRecoveries, "twrp");
    public static final File PathToPhilz           =      new File(PathToRecoveries, "philz");
    public static final File PathToKernel          =      new File(PathToRashr, "kernel");
    public static final File PathToStockKernel     =      new File(PathToKernel, "stock");
    public static final File PathToRecoveryBackups =      new File(PathToRashr, "recovery-backups");
    public static final File PathToKernelBackups   =      new File(PathToRashr, "kernel-backups");
    public static final File PathToUtils           =      new File(PathToRashr, "utils");
    public static final File LastLog               =      new File("/cache/recovery/last_log");

    /**
     * OnFragmentInteraction Constants
     */
    public static final int OPEN_RASHR_FRAGMENT    =      19273;     /** Random constant No */
    public static final int OPEN_FLASH_AS_FRAGMENT  =      12309;
}
