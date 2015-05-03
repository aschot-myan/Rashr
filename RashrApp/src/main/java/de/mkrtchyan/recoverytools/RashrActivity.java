package de.mkrtchyan.recoverytools;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

import org.sufficientlysecure.donations.DonationsFragment;
import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.Toolbox;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Downloader;
import de.mkrtchyan.utils.Notifyer;

/**
 * Copyright (c) 2015 Aschot Mkrtchyan
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
public class RashrActivity extends AppCompatActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    static boolean FirstSession = true;
    static boolean LastLogExists = true;

    private final File Folder[] = {
            Constants.PathToRashr, Constants.PathToRecoveries, Constants.PathToKernel,
            Constants.PathToStockRecovery, Constants.PathToCWM, Constants.PathToTWRP,
            Constants.PathToPhilz, Constants.PathToStockKernel, Constants.PathToRecoveryBackups,
            Constants.PathToKernelBackups, Constants.PathToUtils
    };
    private final RashrActivity mActivity = this;
    private final Context mContext = this;
    /**
     * Declaring needed objects
     */
    private final ArrayList<String> mERRORS = new ArrayList<>();
    private Shell mShell;
    private Toolbox mToolbox;
    private Device mDevice;
    private Toolbar mToolbar;

    public static boolean isDark;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private boolean mVersionChanged = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isDark = Common.getBooleanPref(mContext, Constants.PREF_NAME, Constants.PREF_KEY_DARK_UI);
        setTheme(!isDark ? R.style.Rashr : R.style.Rashr_Dark);
        setContentView(R.layout.loading_layout);

        final TextView tvLoading = (TextView) findViewById(R.id.tvLoading);

        final Thread StartThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvLoading.setText(R.string.getting_root);
                    }
                });
                /** Try to get root access */
                try {
                    startShell();
                    mToolbox = new Toolbox(mShell);
                } catch (IOException e) {
                    mActivity.addError(Constants.RASHR_TAG, e, false);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.pbLoading).setVisibility(View.INVISIBLE);
                            tvLoading.setTextColor(Color.RED);
                            tvLoading.setText(R.string.no_root);
                        }
                    });
                    return;
                }

                /** Creating needed folder and unpacking files */
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvLoading.setText(R.string.loading_data);
                    }
                });
                for (File i : Folder) {
                    if (!i.exists()) {
                        if (!i.mkdir()) {
                            mActivity.addError(Constants.RASHR_TAG,
                                    new IOException(i + " can't be created!"), false);
                        }
                    }
                }
                try {
                    extractFiles();
                } catch (IOException e) {
                    mActivity.addError(Constants.RASHR_TAG, e, true);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast
                                    .makeText(mContext, R.string.failed_unpack_files, Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }

                try {
                    File LogCopy = new File(mContext.getFilesDir(), Constants.LastLog.getName() + ".txt");
                    mToolbox.setFilePermissions(Constants.LastLog, "666");
                    mToolbox.copyFile(Constants.LastLog, LogCopy, false, false);
                } catch (Exception e) {
                    LastLogExists = false;
                    mActivity.addError(Constants.RASHR_TAG, e, false);
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvLoading.setText(R.string.reading_device);
                    }
                });
	            if (mDevice == null)
		            mDevice = new Device(mActivity);

                /** If device is not supported, you can report it now or close the App */
                if (!mDevice.isRecoverySupported() && !mDevice.isKernelSupported()) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showDeviceNotSupportedDialog();
                        }
                    });
                } else {
                    Common.setBooleanPref(mContext, Constants.PREF_NAME,
                            Constants.PREF_KEY_SHOW_UNIFIED, true);
                    if (!Common.getBooleanPref(mContext, Constants.PREF_NAME,
                            Constants.PREF_KEY_FIRST_RUN)) {
                        /** Setting first start configuration */
                        Common.setBooleanPref(mContext, Constants.PREF_NAME, Constants.PREF_KEY_ADS,
                                true);
                        Common.setBooleanPref(mContext, Shell.PREF_NAME, Shell.PREF_LOG, true);
                        Common.setBooleanPref(mContext, Constants.PREF_NAME,
                                Constants.PREF_KEY_CHECK_UPDATES, true);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showUsageWarning();
                            }
                        });
                    } else {
                        /** Checking if version has changed */
                        try {
                            PackageInfo pInfo;
                            String pName;
                            PackageManager pManager;
                            if ((pName = getPackageName()) != null
                                    && (pManager = getPackageManager()) != null
                                    && (pInfo = pManager.getPackageInfo(pName, 0)) != null) {
                                final int previous_version = Common.getIntegerPref(mContext,
                                        Constants.PREF_NAME, Constants.PREF_KEY_CUR_VER);
                                final int current_version = pInfo.versionCode;
                                mVersionChanged = current_version > previous_version;
                                Common.setIntegerPref(mContext, Constants.PREF_NAME,
                                        Constants.PREF_KEY_CUR_VER, current_version);
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        checkUpdates(current_version);
                                    }
                                });
                            } else {
                                mVersionChanged = true;
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            mActivity.addError(Constants.RASHR_TAG, e, false);
                            mVersionChanged = true;
                        }
                        if (mVersionChanged) {
                            /** Re-enable Ads */
                            Common.setBooleanPref(mContext, Constants.PREF_NAME,
                                    Constants.PREF_KEY_ADS, true);
                            /** Reset Shell Logs */
                            Common.deleteLogs(mContext);
                            /** Show Play Store rater dialog */
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!Common.getBooleanPref(mContext, Constants.PREF_NAME,
                                            Constants.PREF_KEY_HIDE_RATER)) {
                                        Notifyer.showAppRateDialog(mContext, Constants.PREF_NAME,
                                                Constants.PREF_KEY_HIDE_RATER);
                                    }
                                }
                            });
                        }
                    }
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            setContentView(R.layout.activity_rashr);
                            mToolbar = (Toolbar) findViewById(R.id.toolbar);
                            setSupportActionBar(mToolbar);
                            mDevice.downloadUtils(mContext);
                            mNavigationDrawerFragment = (NavigationDrawerFragment)
                                    getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
                            mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                                    (DrawerLayout) findViewById(R.id.RashrLayout));

                            AdView ads = (AdView) findViewById(R.id.ads);
                            if (ads != null) {
                                if (Common.getBooleanPref(mContext, Constants.PREF_NAME,
                                        Constants.PREF_KEY_ADS)) {
                                    ads.loadAd(new AdRequest()
                                            .addTestDevice("0559BC4D133A29D00A36F3FE8FECD883"));
                                }
                            }
                            onNavigationDrawerItemSelected(0);
                        } catch (NullPointerException e) {
                            mActivity.addError(Constants.RASHR_TAG, e, false);
                            try {
                                tvLoading.setText(R.string.failed_setup_layout);
                                tvLoading.setTextColor(Color.RED);
                            } catch (RuntimeException ex) {
                                mActivity.addError(Constants.RASHR_TAG, e, true);
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

    private void showUsageWarning() {
        if (mDevice.isRecoverySupported() || mDevice.isKernelSupported()) {
            final AlertDialog.Builder WarningDialog = new AlertDialog.Builder(mContext);
            WarningDialog.setTitle(R.string.warning);
            WarningDialog.setMessage(String.format(getString(R.string.bak_warning),
                    Constants.PathToRecoveryBackups + " & " + Constants.PathToKernelBackups));
            WarningDialog.setPositiveButton(R.string.backup, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switchTo(BackupRestoreFragment.newInstance(mActivity));
                    Common.setBooleanPref(mContext, Constants.PREF_NAME,
                            Constants.PREF_KEY_FIRST_RUN, true);
                }
            });
            WarningDialog.setNegativeButton(R.string.risk, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Common.setBooleanPref(mContext, Constants.PREF_NAME,
                            Constants.PREF_KEY_FIRST_RUN, true);
                }
            });
            WarningDialog.setCancelable(false);
            WarningDialog.show();
        }
    }

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
        if (!Constants.LastLog.exists()) {
            DeviceNotSupported.setNeutralButton(R.string.sReboot, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        mToolbox.reboot(Toolbox.REBOOT_RECOVERY);
                    } catch (Exception e) {
                        mActivity.addError(Constants.RASHR_TAG, e, false);
                    }
                }
            });
        }
        DeviceNotSupported.show();
    }

    private void extractFiles() throws IOException {
        File RecoveryCollectionFile = new File(mContext.getFilesDir(), "recovery_sums");
        File KernelCollectionFile = new File(mContext.getFilesDir(), "kernel_sums");
        File flash_image = new File(getFilesDir(), "flash_image");
        Common.pushFileFromRAW(mContext, flash_image, R.raw.flash_image, mVersionChanged);
        File dump_image = new File(getFilesDir(), "dump_image");
        Common.pushFileFromRAW(mContext, dump_image, R.raw.dump_image, mVersionChanged);
        File busybox = new File(mContext.getFilesDir(), "busybox");
        Common.pushFileFromRAW(mContext, busybox, R.raw.busybox, mVersionChanged);
        Common.pushFileFromRAW(mContext, RecoveryCollectionFile, R.raw.recovery_sums, mVersionChanged);
        Common.pushFileFromRAW(mContext, KernelCollectionFile, R.raw.kernel_sums, mVersionChanged);
        File PartLayoutsZip = new File(mContext.getFilesDir(), "partlayouts.zip");
        Common.pushFileFromRAW(mContext, PartLayoutsZip, R.raw.partlayouts, mVersionChanged);
        File loki_patch = new File(mContext.getFilesDir(), "loki_patch");
        Common.pushFileFromRAW(mContext, loki_patch, R.raw.loki_patch, mVersionChanged);
        File loki_flash = new File(mContext.getFilesDir(), "loki_flash");
        Common.pushFileFromRAW(mContext, loki_flash, R.raw.loki_flash, mVersionChanged);
    }

    public void exit() {
        finish();
        System.exit(0);
    }

    /**
     * @return All Preferences as String
     */
    public String getAllPrefs() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);
        String Prefs = "";
        Map<String, ?> prefsMap = prefs.getAll();
        try {
            for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
                /**
                 * Skip following Prefs (PREF_KEY_HISTORY, ...)
                 */
                try {
                    if (!entry.getKey().contains(Constants.PREF_KEY_HISTORY)
                            && !entry.getKey().contains(FlashUtil.PREF_KEY_FLASH_COUNTER)) {
                        Prefs += entry.getKey() + ": " + entry.getValue().toString() + "\n";
                    }
                } catch (NullPointerException e) {
                    mActivity.addError(Constants.RASHR_TAG, e, false);
                }
            }
        } catch (NullPointerException e) {
            mActivity.addError(Constants.RASHR_TAG, e, false);
        }

        return Prefs;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment = null;
        position++;
        String action;
        if ((action = mActivity.getIntent().getAction()) != null && action.equals(Intent.ACTION_VIEW)) {
            /** Rashr is opened by other app to flash supported files (.zip) or (.img) */
            if (mActivity.getIntent().getData().toString().endsWith(".zip")) {
                /** If it is a zip file open the ScriptManager */
                File zip = new File(getIntent().getData().getPath());
                if (zip.exists()) fragment = ScriptManagerFragment.newInstance(this, zip);
            } else {
                /** If it is a img file open FlashAs to choose mode (recovery or kernel) */
                File img = new File(getIntent().getData().getPath());
                if (img.exists()) fragment = FlashAsFragment.newInstance(this, img, true);
            }
        } else {
            switch (position) {
                case 1:
                    fragment = FlashFragment.newInstance(this);
                    break;
                case 2:
                    fragment = ScriptManagerFragment.newInstance(this, null);
                    break;
                case 3:
                    fragment =
                            DonationsFragment.newInstance(BuildConfig.DEBUG,
                            Constants.GOOGLE_PUBKEY, Constants.GOOGLE_CATALOG,
                            getResources().getStringArray(R.array.donation_google_catalog_values));
                    break;
                case 4:
                    fragment = SettingsFragment.newInstance();
                    break;
                case 5:
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://forum.xda-developers.com/showthread.php?t=2334554")));
                    break;
            }
        }
        if (fragment != null) {
            switchTo(fragment);
        }
    }

    public Device getDevice() {
        return mDevice;
    }

    /**
     * Share instances with root access instance with all other Classes
     */
    public Shell getShell() {
        return mShell;
    }
    public Toolbox getToolbox() {
        return mToolbox;
    }

    public void addError(String TAG, final Exception e, final boolean serious) {
        mERRORS.add(TAG + ": " + (e != null ? e.toString() : ""));
        if (e != null) {
            if (serious) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ReportDialog dialog = new ReportDialog(mActivity, e.toString());
                        dialog.setCancelable(true);
                        dialog.show();
                        Notifyer.showExceptionToast(mContext, e);
                    }
                });
            }
        }
    }

    public void checkUpdates(final int currentVersion) {
        try {
            File versionsFile = new File(mContext.getFilesDir(), "version");
            Downloader version = new Downloader(mContext, new URL(Constants.RASHR_VERSION_URL), versionsFile);
            version.setOverrideFile(true);
            version.setHidden(true);
            version.setOnDownloadListener(new Downloader.OnDownloadListener() {
                @Override
                public void success(File file) {
                    if (currentVersion < Integer.valueOf(Common.fileContent(file))) {
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
                    }
                }

                @Override
                public void failed(Exception e) {
                    Toast.makeText(mContext, R.string.failed_update, Toast.LENGTH_SHORT).show();
                }
            });
            version.execute();
        } catch (MalformedURLException ignore) {}

    }
    public Toolbar getToolbar() {
        return mToolbar;
    }

    public void switchTo(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out)
                .replace(R.id.container, fragment)
                .commitAllowingStateLoss();
    }

    public ArrayList<String> getErrors() {
        return mERRORS;
    }

    private void startShell() throws IOException{
        try {
            mShell = Shell.startRootShell(mContext);
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                /** ignore root access error on Debug Rashr, use normal shell*/
                mShell = Shell.startShell();
            } else {
                throw e;
            }
        }
    }
}