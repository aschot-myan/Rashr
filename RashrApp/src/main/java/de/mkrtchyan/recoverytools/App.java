package de.mkrtchyan.recoverytools;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.preference.PreferenceManager;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.Toolbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.ButterKnife;

/**
 * Copyright (c) 2017 Aschot Mkrtchyan
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class App extends Application {

    public static final String TAG = "Rashr";

    public static final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    /* Declaring setting names */
    public static final String PREF_KEY_HISTORY = "last_history_";
    public static final String PREF_KEY_ADS = "show_ads";
    public static final String PREF_KEY_CUR_VER = "current_version";
    public static final String PREF_KEY_FIRST_RUN = "first_run";
    //public static final String PREF_KEY_HIDE_RATER = "show_rater";
    //public static final String PREF_KEY_LOG = "log";
    public static final String PREF_KEY_SHOW_UNIFIED = "show_unified";
    public static final String PREF_KEY_DARK_UI = "use_dark_ui";
    public static final String PREF_KEY_CHECK_UPDATES = "check_updates";
    public static final String PREF_KEY_HIDE_UPDATE_HINTS = "hide_uptodate_hint";
    public static final String PREF_KEY_HIDE_REBOOT = "hide_reboot";
    public static final String PREF_KEY_FLASH_COUNTER = "last_counter";
    public static final String PREF_KEY_SKIP_SIZE_CHECK = "skip_size_check";
    public static final String PREF_KEY_SKIP_IMAGE_CHECK = "skip_image_check";
    public static final String PREF_KEY_DEVICE_NAME = "device_name";
    public static final String PREF_KEY_RECOVERY_PATH = "recovery_path";
    public static final String PREF_KEY_KERNEL_PATH = "kernel_path";
    public static final String PREF_KEY_CHANGELOG = "changelog";
    public static final String PREF_KEY_SHOW_LOGS = "showlogs";
    public static final String PREF_KEY_REPORT = "report";
    public static final String PREF_KEY_LICENSES = "licenses";
    public static final String PREF_KEY_RESET_APP = "reset_app";
    public static final String PREF_KEY_CLEAR_CACHE = "clear_cache";

    //Device.java information contains found information to prevent search it again
    //THIS ARE NOT SETTINGS OR PREFERENCES
    public static final String PREF_KEY_RECOVERY_BLOCK_SIZE = "recovery_block_size";
    public static final String PREF_KEY_KERNEL_BLOCK_SIZE = "kernel_block_size";
    public static final String PREF_KEY_XZDUAL_NAME = "xzdual_name";
    public static final String PREF_KEY_RECOVERY_TYPE = "recovery_type";
    public static final String PREF_KEY_KERNEL_TYPE = "kernel_type";
    public static final String PREF_KEY_RECOVERY_EXT = "recovery_ext";
    public static final String PREF_KEY_KERNEL_EXT = "kernel_ext";


    /* Google in-app purchases */
    public static final String GOOGLE_PUBKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhHa/9/sYU2dbF6nQqGzNktvxb+83Ed/inkK8cbiEkcRjw/t/Okge6UghlyYEXcZLJL9TDPAlraktUZZ/XH8+ZpgdNlO+UeQTD4Yl9ReZ/ujQ151g/RLrVNi7NF4SQ1jD20RmX2lCUhbl5cPi6UKL/bHFeZwjE0pOr48svW0nXbRfpgSSk3V/DaV1igTX66DuFUITKi0gQGD8XAVsrOcQRQtr4wHfdgyMQR9m0vPPzpFoDD8SZZFCp9UgvuzqdwYqY8kr7ZcyxuQhaNlcx74hpFQ9MJteRTII+ii/pHfWDh0hDMqcodm4UD9rISmPSvlLR3amfSg4Vm6ObWFiVe4qVwIDAQAB";
    public static final String[] GOOGLE_CATALOG = {
            "donate_1",
            "donate_2",
            "donate_3",
            "donate_5"
    };

    /* DevInfo for donations */
    public static final String DEV_EMAIL = "aschot.myan@gmail.com";


    /* Other constants */
    //Filename for recovery list
    public static final String RECOVERY_SUMS = "recovery_sums";
    //Filename for kernel list
    public static final String KERNEL_SUMS = "kernel_sums";
    //Filename with latest Rashr app version
    public static final String VERSION = "version";

    /* Web Address for download Recovery and Kernel IMGs */
    public static final String BASE_URL = "https://dslnexus.de/Android";
    //public static final String RECOVERY_URL = BASE_URL + "/recoveries";
    public static final String KERNEL_URL = BASE_URL + "/kernel";
    public static final String RECOVERY_SUMS_URL = BASE_URL + "/" + RECOVERY_SUMS;
    public static final String KERNEL_SUMS_URL = BASE_URL + "/" + KERNEL_SUMS;
    public static final String RASHR_VERSION_URL = BASE_URL + "/rashr/" + VERSION;
    //public static final String UTILS_URL = BASE_URL + "/utils";
    public static final String XDA_THREAD_URL = "http://forum.xda-developers.com/showthread.php?t=2334554";
    public static final String GOOGLE_PLUS_URL = "https://plus.google.com/communities/108943765577787027090";
    public static final String GITHUB_REPOSITORY_URL = "https://github.com/dslnexus/rashr";
    public static final String TWRP_SCREENSHOT_URL = BASE_URL + "/rashr/twrp_screenshots";
    public static final String CWM_SCREENSHOT_URL = BASE_URL + "/rashr/cwm_screenshots";

    public static final String CHANGELOG_URL = "https://raw.githubusercontent.com/DsLNeXuS/Rashr/master/CHANGELOG.md";
    public static final File PathToSd = Environment.getExternalStorageDirectory();
    public static final File PathToRashr = new File(PathToSd, "Rashr");
    public static final File PathToRecoveries = new File(PathToRashr, "recoveries");
    public static final File PathToStockRecovery = new File(PathToRecoveries, "stock");
    public static final File PathToCWM = new File(PathToRecoveries, "clockworkmod");
    public static final File PathToTWRP = new File(PathToRecoveries, "twrp");
    public static final File PathToPhilz = new File(PathToRecoveries, "philz");
    public static final File PathToCM = new File(PathToRecoveries, "cm");
    public static final File PathToXZDual = new File(PathToRecoveries, "xzdual");
    public static final File PathToKernel = new File(PathToRashr, "kernel");
    public static final File PathToStockKernel = new File(PathToKernel, "stock");
    public static final File PathToRecoveryBackups = new File(PathToRashr, "recovery-backups");
    public static final File PathToKernelBackups = new File(PathToRashr, "kernel-backups");
    public static final File PathToUtils = new File(PathToRashr, "utils");
    public static final File PathToTmp = new File(PathToRashr, "tmp");
    public static final File LastLog = new File("/cache/recovery/last_log");
    public static final String AppLogs = "logs.txt";
    public static final File RecoveryCollectionFile = new File(PathToRashr, RECOVERY_SUMS);
    public static final File KernelCollectionFile = new File(PathToRashr, KERNEL_SUMS);
    public static final ArrayList<String> ERRORS = new ArrayList<>();
    /*
     * Used folder and files
     */
    public static File FilesDir;
    /*
     * Files in App-Files directory
     */
    public static File Busybox;
    public static File RashrLog;
    public static File LokiFlash;
    public static File LokiPatch;
    public static Shell Shell;
    public static Toolbox Toolbox;
    public static Device Device;
    public static SharedPreferences Preferences;
    public static boolean isVersionChanged = false;
    public static boolean isDark;

    @Override
    public void onCreate() {
        super.onCreate();
        ButterKnife.setDebug(BuildConfig.DEBUG);
        Context context = getApplicationContext();
        Preferences = PreferenceManager.getDefaultSharedPreferences(context);
        isDark = App.Preferences.getBoolean(App.PREF_KEY_DARK_UI, false);
        Device = new Device();
        /* Checking if version has changed */
        final int previous_version = App.Preferences.getInt(App.PREF_KEY_CUR_VER, 0);
        isVersionChanged = BuildConfig.VERSION_CODE > previous_version | BuildConfig.DEBUG;
        App.Preferences.edit().putInt(App.PREF_KEY_CUR_VER, BuildConfig.VERSION_CODE).apply();
        FilesDir = context.getFilesDir();
        RashrLog = new File(FilesDir, AppLogs);
        /* Try to get root access */
        try {
            Shell = startShell();
            App.Shell.setLogFile(RashrLog);
            App.Toolbox = new Toolbox(App.Shell);
        } catch (IOException e) {
            String message;
            if (e.toString() != null) {
                message = e.toString();
            } else {
                message = "Shell could not be started.  Error: " + e.toString();
            }
            App.ERRORS.add(App.TAG + " " + message);
        }
    }

    /**
     * @return Shell with root access if is possible. If not: normal Shell will be returned
     */
    private Shell startShell() throws IOException {
        Shell shell;
        try {
            shell = org.sufficientlysecure.rootcommands.Shell.startRootShell();
        } catch (IOException e) {
            App.ERRORS.add("Root-Shell could not be started " + e);
            if (BuildConfig.DEBUG) {
                /* ignore root access error on Debug Rashr, use normal shell*/
                shell = org.sufficientlysecure.rootcommands.Shell.startShell();
            } else {
                throw e;
            }
        }
        return shell;
    }

}
