package de.mkrtchyan.recoverytools;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

import org.sufficientlysecure.donations.DonationsFragment;
import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.Toolbox;
import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Downloader;

/**
 * Copyright (c) 2016 Aschot Mkrtchyan
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
public class RashrActivity extends AppCompatActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static boolean isDark;
    static boolean FirstSession = true;
    static boolean LastLogExists = false;
    private final File Folder[] = {
            Const.PathToRashr, Const.PathToRecoveries, Const.PathToKernel,
            Const.PathToStockRecovery, Const.PathToCWM, Const.PathToTWRP,
            Const.PathToPhilz, Const.PathToXZDual, Const.PathToStockKernel,
            Const.PathToRecoveryBackups, Const.PathToKernelBackups, Const.PathToUtils,
            Const.PathToTmp
    };
    private final RashrActivity mActivity = this;
    private final Context mContext = this;
    /**
     * Declaring needed objects
     */
    private Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private boolean mVersionChanged = false;

    /**
     * Setting first start configuration
     */
    public static void firstSetup(Context context) {
        Common.setBooleanPref(context, Const.PREF_NAME, Const.PREF_KEY_HIDE_UPDATE_HINTS, false);
        Common.setBooleanPref(context, Const.PREF_NAME, Const.PREF_KEY_ADS, true);
        Common.setBooleanPref(context, Const.PREF_NAME, Const.PREF_KEY_LOG, true);
        Common.setBooleanPref(context, Const.PREF_NAME, Const.PREF_KEY_CHECK_UPDATES, true);
        Common.setBooleanPref(context, Const.PREF_NAME, Const.PREF_KEY_SKIP_SIZE_CHECK, false);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Const.FilesDir = mContext.getFilesDir();
        Const.RashrLog = new File(Const.FilesDir, Const.LOG_NAME);
        isDark = Common.getBooleanPref(mContext, Const.PREF_NAME, Const.PREF_KEY_DARK_UI);
        setTheme(!isDark ? R.style.Rashr : R.style.Rashr_Dark);
        setContentView(R.layout.loading_layout);

        final TextView tvLoading = (TextView) findViewById(R.id.tvLoading);

        final Thread StartThread = new Thread(new Runnable() {
            @Override
            public void run() {
                /* Checking if version has changed */
                final int previous_version = Common.getIntegerPref(mContext,
                        Const.PREF_NAME, Const.PREF_KEY_CUR_VER);
                mVersionChanged = BuildConfig.VERSION_CODE > previous_version;
                Common.setIntegerPref(mContext, Const.PREF_NAME,
                        Const.PREF_KEY_CUR_VER, BuildConfig.VERSION_CODE);
                /* Try to get root access */
                try {
                    RashrApp.SHELL = startShell();
                    File logs = new File(mContext.getFilesDir(), Const.Logs);
                    RashrApp.SHELL.setLogFile(logs);
                    RashrApp.TOOLBOX = new Toolbox(RashrApp.SHELL);
                } catch (IOException e) {
                    String message;
                    if (e.toString() != null) {
                        message = e.toString();
                    } else {
                        message = "Shell could not be started.  Error: " + e.toString();
                    }
                    RashrApp.ERRORS.add(Const.RASHR_TAG + " " + message);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setContentView(R.layout.err_layout);
                        }
                    });
                    return;
                }

                /* Creating needed folder and unpacking files */
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (tvLoading != null)
                            tvLoading.setText(R.string.loading_data);
                    }
                });
                if (Const.PathToTmp.exists()) {
                    Common.deleteFolder(Const.PathToTmp, true);
                }
                for (File i : Folder) {
                    if (!i.exists()) {
                        if (!i.mkdir()) {
                            RashrApp.ERRORS.add(Const.RASHR_TAG + " " + i + " can't be created!");
                        }
                    }
                }

                try {
                    extractFiles();
                } catch (IOException e) {
                    RashrApp.ERRORS.add("Failed to extract files. Error: " + e);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, R.string.failed_unpack_files,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                try {
                    File LogCopy = new File(mContext.getFilesDir(), Const.LastLog.getName() + ".txt");
                    RashrApp.SHELL.execCommand(Const.Busybox + " chmod 777 " + Const.LastLog);
                    if (LogCopy.exists()) LogCopy.delete();
                    RashrApp.SHELL.execCommand(Const.Busybox + " cp " + Const.LastLog + " " + LogCopy);
                    RashrApp.SHELL.execCommand(Const.Busybox + " chmod 777 " + LogCopy);
                    ApplicationInfo info = getApplicationInfo();
                    RashrApp.SHELL.execCommand(
                            Const.Busybox + " chown " + info.uid + ":" + info.uid + " " + LogCopy);
                    RashrApp.SHELL.execCommand(Const.Busybox + " chmod 777 " + LogCopy);
                    LastLogExists = LogCopy.exists();
                } catch (FailedExecuteCommand e) {
                    RashrApp.ERRORS.add(Const.RASHR_TAG + " LastLog not found: " + e);
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkAppUpdates();
                        if (tvLoading != null)
                            tvLoading.setText(R.string.reading_device);
                    }
                });
                if (!RashrApp.DEVICE.isSetup())
                    RashrApp.DEVICE.setup();

                /* If device is not supported, you can report it now or close the App */
                if (!RashrApp.DEVICE.isRecoverySupported() && !RashrApp.DEVICE.isKernelSupported()) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showDeviceNotSupportedDialog();
                        }
                    });
                } else {
                    Common.setBooleanPref(mContext, Const.PREF_NAME,
                            Const.PREF_KEY_SHOW_UNIFIED, true);
                    if (!Common.getBooleanPref(mContext, Const.PREF_NAME,
                            Const.PREF_KEY_FIRST_RUN)) {
                        firstSetup(mContext);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showUsageWarning();
                            }
                        });
                    }
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            View root = View.inflate(mContext, R.layout.activity_rashr, null);
                            root.startAnimation(AnimationUtils.loadAnimation(mContext,
                                    R.anim.abc_grow_fade_in_from_bottom));
                            setContentView(root);
                            mToolbar = (Toolbar) findViewById(R.id.toolbar);
                            setSupportActionBar(mToolbar);
                            //mDevice.downloadUtils(mContext);
                            mNavigationDrawerFragment = (NavigationDrawerFragment)
                                    getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
                            mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                                    (DrawerLayout) findViewById(R.id.RashrLayout));

                            AdView ads = (AdView) findViewById(R.id.ads);
                            if (ads != null) {
                                if (Common.getBooleanPref(mContext, Const.PREF_NAME,
                                        Const.PREF_KEY_ADS)) {
                                    ads.loadAd(new AdRequest()
                                            .addTestDevice("6400A1C06B921CB807E69EC539ADC588"));
                                }
                            }
                            if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
                                /* Rashr is opened by other app to flash supported files (.zip) or (.img) */
                                File file = new File(getIntent().getData().getPath());
                                if (file.exists()) {
                                    if (file.toString().endsWith(".zip")) {
                                        /* If it is a zip file open the ScriptManager */
                                        switchTo(ScriptManagerFragment.newInstance(mActivity, file));
                                    } else {
                                        /* If it is a img file open FlashAs to choose mode (recovery or kernel) */
                                        switchTo(FlashAsFragment.newInstance(mActivity, file, true));
                                    }
                                }
                            } else {
                                onNavigationDrawerItemSelected(0);
                            }
                        } catch (NullPointerException e) {
                            setContentView(R.layout.err_layout);
                            RashrApp.ERRORS.add("Error while inflating layout:" + e);
                            AppCompatTextView tv = (AppCompatTextView) findViewById(R.id.tvErr);
                            try {
                                if (tv != null) {
                                    tv.setText(R.string.failed_setup_layout);
                                }
                            } catch (RuntimeException ex) {
                                RashrApp.ERRORS.add(Const.RASHR_TAG + e);
                                ReportDialog dialog = new ReportDialog(mActivity, e.toString());
                                dialog.show();
                                ex.printStackTrace();

                            }
                        }
                    }
                });
            }
        });
        StartThread.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mNavigationDrawerFragment.isDrawerOpen()) {
                    mNavigationDrawerFragment.closeDrawer();
                } else {
                    mNavigationDrawerFragment.openDrawer();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Let the user know that what he is doing is dangerous
     */
    private void showUsageWarning() {
        if (RashrApp.DEVICE.isRecoverySupported() || RashrApp.DEVICE.isKernelSupported()) {
            final AlertDialog.Builder WarningDialog = new AlertDialog.Builder(mContext);
            WarningDialog.setTitle(R.string.warning);
            WarningDialog.setMessage(String.format(getString(R.string.bak_warning),
                    Const.PathToRecoveryBackups + " & " + Const.PathToKernelBackups));
            WarningDialog.setPositiveButton(R.string.backup, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switchTo(BackupRestoreFragment.newInstance(mActivity));
                    Common.setBooleanPref(mContext, Const.PREF_NAME,
                            Const.PREF_KEY_FIRST_RUN, true);
                }
            });
            WarningDialog.setNegativeButton(R.string.risk, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Common.setBooleanPref(mContext, Const.PREF_NAME,
                            Const.PREF_KEY_FIRST_RUN, true);
                }
            });
            WarningDialog.setCancelable(false);
            WarningDialog.show();
        }
    }

    /**
     * If the device is not supported the user can send a report to the Developers-Email and help
     * to Implement the device.
     */
    private void showDeviceNotSupportedDialog() {
        AlertDialog.Builder DeviceNotSupported = new AlertDialog.Builder(mContext);
        DeviceNotSupported.setTitle(R.string.warning);
        DeviceNotSupported.setMessage(R.string.not_supportded);
        DeviceNotSupported.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                exit();
            }
        });
        if (!Const.LastLog.exists()) {
            /*
             * Device has never been booted to recovery or cache has been cleaned.
             * The LastLog-File normally contains a partition table so Rashr can read it out from
             * there if the user restarts into recovery. (probably)
             */
            DeviceNotSupported.setNeutralButton(R.string.sReboot, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        RashrApp.TOOLBOX.reboot(Toolbox.REBOOT_RECOVERY);
                    } catch (Exception e) {
                        Toast.makeText(mContext, R.string.reboot_failed, Toast.LENGTH_SHORT).show();
                        RashrApp.ERRORS.add(Const.RASHR_TAG + " Device could not be rebooted");
                    }
                }
            });
        } else {
            DeviceNotSupported.setPositiveButton(R.string.report, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final ReportDialog reportDialog = new ReportDialog(mActivity, "Device not supported");
                    reportDialog.setCancelable(false);
                    reportDialog.show();
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (reportDialog.isShowing()) {
                                //Wait till dialog is closed then exit the app
                            }
                            System.exit(0);
                        }
                    });
                    t.start();
                }
            });
        }
        DeviceNotSupported.show();
    }

    /**
     * Extract files from APK, the files are stored under RashrApp/src/res/raw
     *
     * @throws IOException Files can't be extracted
     */
    private void extractFiles() throws IOException {
        Common.pushFileFromRAW(mContext, Const.RecoveryCollectionFile, R.raw.recovery_sums,
                mVersionChanged);
        Common.pushFileFromRAW(mContext, Const.KernelCollectionFile, R.raw.kernel_sums,
                mVersionChanged);
        Const.Busybox = new File(mContext.getFilesDir(), "busybox");
        Common.pushFileFromRAW(mContext, Const.Busybox, R.raw.busybox_arm, mVersionChanged);
        try {
            RashrApp.SHELL.execCommand("chmod 777 " + Const.Busybox);
        } catch (FailedExecuteCommand failedExecuteCommand) {
            failedExecuteCommand.printStackTrace();
        }
        File PartLayoutsZip = new File(mContext.getFilesDir(), "partlayouts.zip");
        Common.pushFileFromRAW(mContext, PartLayoutsZip, R.raw.partlayouts, mVersionChanged);
        File flash_image = new File(getFilesDir(), "flash_image");
        Common.pushFileFromRAW(mContext, flash_image, R.raw.flash_image, mVersionChanged);
        File dump_image = new File(getFilesDir(), "dump_image");
        Common.pushFileFromRAW(mContext, dump_image, R.raw.dump_image, mVersionChanged);
        Const.LokiPatch = new File(mContext.getFilesDir(), "loki_patch");
        Common.pushFileFromRAW(mContext, Const.LokiPatch, R.raw.loki_patch, mVersionChanged);
        Const.LokiFlash = new File(mContext.getFilesDir(), "loki_flash");
        Common.pushFileFromRAW(mContext, Const.LokiFlash, R.raw.loki_flash, mVersionChanged);
    }

    /**
     * Close App.
     */
    public void exit() {
        finish();
        System.exit(0);
    }

    /**
     * @return All Preferences as String
     */
    public String getAllPrefs() {
        SharedPreferences prefs = getSharedPreferences(Const.PREF_NAME, MODE_PRIVATE);
        String Prefs = "";
        Map<String, ?> prefsMap = prefs.getAll();
        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            /*
             * Skip following Prefs (PREF_KEY_HISTORY, ...)
             */
            if (!entry.getKey().contains(Const.PREF_KEY_HISTORY)
                    && !entry.getKey().contains(Const.PREF_KEY_FLASH_COUNTER)) {
                Prefs += entry.getKey() + ": " + entry.getValue().toString() + "\n";
            }
        }

        return Prefs;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        position++;
        switch (position) {
            case 1:
                switchTo(FlashFragment.newInstance(this));
                break;
            case 2:
                switchTo(ScriptManagerFragment.newInstance(this, null));
                break;
            case 3:
                switchTo(DonationsFragment.newInstance(BuildConfig.DEBUG, true,
                        Const.GOOGLE_PUBKEY, Const.GOOGLE_CATALOG,
                        getResources().getStringArray(R.array.donation_google_catalog_values),
                        true, "ashotmkrtchyan1995@gmail.com", "EUR", "Donation - Rashr Developer - Aschot Mkrtchyan"));
                break;
            case 4:
                switchTo(SettingsFragment.newInstance());
                break;
            case 5:
                switchTo(InformationFragment.newInstance());
                break;
        }
    }

    /**
     * Checks if new version of Rashr is online and links to Play Store
     * The current version number is stored on dslnexus.de/Android/rashr/version
     * as plain text
     */
    public void checkAppUpdates() {
        try {
            File versionsFile = new File(mContext.getFilesDir(), Const.VERSION);
            Downloader downloader = new Downloader(new URL(Const.RASHR_VERSION_URL), versionsFile);
            downloader.setOverrideFile(true);
            downloader.setOnDownloadListener(new Downloader.OnDownloadListener() {
                @Override
                public void onSuccess(File file) {
                    try {
                        final int ServerVersion =  Integer.valueOf(Common.fileContent(file).replace("\n", ""));
                        if (BuildConfig.VERSION_CODE < ServerVersion) {
                            new AlertDialog.Builder(mContext)
                                    .setTitle(R.string.update_available)
                                    .setMessage(R.string.download_update)
                                    .setPositiveButton(R.string.open_playstore, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startActivity(new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("market://details?id=" + getPackageName())));
                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        } else {
                            //If update hints are enabled show Toast with information
                            if (Common.getBooleanPref(mContext, Const.PREF_NAME,
                                    Const.PREF_KEY_HIDE_UPDATE_HINTS)) {
                                Toast.makeText(mContext, R.string.app_uptodate, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (IOException | NumberFormatException ignore) {
                        // NOTE for NumberFormatException
                        // It seams so that some devices that gets a error-page instead of breaking connection
                        // For example: Your device is not connected to the internet actualize your dataplan
                        // to get connection or something else so the downloader gets the "error" HTML page
                        // and its not parsable to a Integer
                    }
                }

                @Override
                public void onFail(Exception e) {
                    Toast.makeText(mContext, R.string.failed_update, Toast.LENGTH_SHORT).show();
                }
            });
            downloader.download();
        } catch (MalformedURLException ignore) {
        }

    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    /**
     * @param fragment current fragment will be replaced by param fragment
     */
    public void switchTo(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.abc_grow_fade_in_from_bottom,
                        R.anim.abc_shrink_fade_out_from_bottom)
                .replace(R.id.container, fragment)
                .commitAllowingStateLoss();
    }

    /**
     * @return Shell with root access if is possible. If not: normal Shell will be returned
     */
    private Shell startShell() throws IOException {
        Shell shell;
        try {
            shell = Shell.startRootShell();
        } catch (IOException e) {
            RashrApp.ERRORS.add("Root-Shell could not be started " + e);
            if (BuildConfig.DEBUG) {
                /** ignore root access error on Debug Rashr, use normal shell*/
                shell = Shell.startShell();
            } else {
                throw e;
            }
        }
        return shell;
    }
}