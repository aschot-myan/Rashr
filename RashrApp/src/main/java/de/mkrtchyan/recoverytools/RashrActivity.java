package de.mkrtchyan.recoverytools;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.sufficientlysecure.donations.DonationsFragment;
import org.sufficientlysecure.rootcommands.Toolbox;
import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.mkrtchyan.recoverytools.fragment.FlashAsFragment;
import de.mkrtchyan.recoverytools.fragment.FlashFragment;
import de.mkrtchyan.recoverytools.fragment.InformationFragment;
import de.mkrtchyan.recoverytools.fragment.ScriptManagerFragment;
import de.mkrtchyan.recoverytools.fragment.SettingsFragment;
import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Downloader;

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
public class RashrActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static File Folder[] = {
            App.PathToRashr, App.PathToRecoveries, App.PathToKernel,
            App.PathToStockRecovery, App.PathToCWM, App.PathToTWRP,
            App.PathToPhilz, App.PathToXZDual, App.PathToCM,
            App.PathToStockKernel, App.PathToRecoveryBackups, App.PathToKernelBackups,
            App.PathToUtils, App.PathToTmp
    };
    //Read out settings if theme is dark
    public static boolean FirstSession = true;
    static boolean LastLogExists = false;
    private final RashrActivity mActivity = this;
    private final Context mContext = this;
    /* Declaring needed objects */

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.navigation_view) NavigationView mNavigationView;

    @BindView(R.id.RashrLayout) DrawerLayout mRoot;
    @BindView(R.id.ads) AdView mAds;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(App.isDark ? R.style.Rashr_Dark : R.style.Rashr);
        setContentView(R.layout.loading_layout);
        if (App.Shell == null) {
            /* couldn't get root */
            setContentView(R.layout.err_layout);
            return;
        }

        final TextView tvLoading = findViewById(R.id.tvLoading);
        tvLoading.setText(R.string.requesting_permissions);

        ActivityCompat.requestPermissions(this, App.PERMISSIONS, 0);

    }

    /**
     * Let the user know that what he is doing is dangerous
     */
    private void showUsageWarning() {
        final AlertDialog.Builder WarningDialog = new AlertDialog.Builder(mContext);
        WarningDialog.setTitle(R.string.warning);
        WarningDialog.setMessage(String.format(getString(R.string.bak_warning),
                App.PathToRecoveryBackups + " & " + App.PathToKernelBackups));
        WarningDialog.setPositiveButton(R.string.backup, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(mContext, BackupActivity.class));
                App.Preferences.edit().putBoolean(App.PREF_KEY_FIRST_RUN, false).apply();
            }
        });
        WarningDialog.setNegativeButton(R.string.risk, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                App.Preferences.edit().putBoolean(App.PREF_KEY_FIRST_RUN, false).apply();
            }
        });
        WarningDialog.setCancelable(false);
        WarningDialog.show();
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
        if (!App.LastLog.exists()) {
            /*
             * Device has never been booted to recovery or cache has been cleaned.
             * The LastLog-File normally contains a partition table so Rashr can read it out from
             * there if the user restarts into recovery. (probably)
             */
            DeviceNotSupported.setNeutralButton(R.string.sReboot, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        App.Toolbox.reboot(Toolbox.REBOOT_RECOVERY);
                    } catch (Exception e) {
                        Snackbar.make(mRoot, R.string.reboot_failed, Snackbar.LENGTH_SHORT).show();
                        App.ERRORS.add(App.TAG + " Device could not be rebooted");
                        System.exit(0);
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
                    reportDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (!BuildConfig.DEBUG) {
                                System.exit(0);
                            }
                        }
                    });
                }
            });
        }
        DeviceNotSupported.show();
    }

    /**
     * Close App.
     */
    public void exit() {
        finish();
        System.exit(0);
    }

    /**
     * Checks if new version of Rashr is online and links to Play Store
     * The current version number is stored on dslnexus.de/Android/rashr/version
     * as plain text
     */
    public void checkAppUpdates() {
        try {
            File versionsFile = new File(mContext.getFilesDir(), App.VERSION);
            Downloader downloader = new Downloader(new URL(App.RASHR_VERSION_URL), versionsFile);
            downloader.setOverrideFile(true);
            downloader.setOnDownloadListener(new Downloader.OnDownloadListener() {
                @Override
                public void onSuccess(File file) {
                    try {
                        System.out.println(Common.fileContent(file));
                        final int ServerVersion = Integer.valueOf(Common.fileContent(file).replace("\n", ""));
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
                            if (!App.Preferences.getBoolean(App.PREF_KEY_HIDE_UPDATE_HINTS, false)) {
                                Toast.makeText(mContext, R.string.app_uptodate, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (IOException | NumberFormatException ignore) {
                        // NOTE for NumberFormatException
                        // It seams so that some devices that gets a error-page instead of breaking up connection
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

    /**
     * @param fragment current fragment will be replaced by param fragment
     */
    public void switchTo(Fragment fragment) {
        switchTo(fragment, false);
    }

    /**
     * @param fragment       current fragment will be replaced by param fragment
     * @param addToBackStack add fragment to Stack
     */
    public void switchTo(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out)
                .replace(R.id.container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        } else {
            getSupportFragmentManager().popBackStackImmediate();
        }
        transaction.commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_flasher:
                switchTo(FlashFragment.newInstance(mActivity));
                item.setChecked(true);
                break;
            case R.id.nav_recovery_script:
                switchTo(ScriptManagerFragment.newInstance());
                item.setChecked(true);
                break;
            case R.id.nav_donate:
                switchTo(DonationsFragment.newInstance(BuildConfig.DEBUG, true,
                        App.GOOGLE_PUBKEY, App.GOOGLE_CATALOG,
                        getResources().getStringArray(R.array.donation_google_catalog_values),
                        true, App.DEV_EMAIL, "EUR", "Donation - Rashr Developer"));
                item.setChecked(true);
                break;
            case R.id.nav_settings:
                switchTo(new SettingsFragment());
                item.setChecked(true);
                break;
            case R.id.nav_information:
                switchTo(InformationFragment.newInstance());
                item.setChecked(true);
                break;
            case R.id.xda:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(App.XDA_THREAD_URL)));
                return true;
            case R.id.google_plus:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(App.GOOGLE_PLUS_URL)));
                return true;
            case R.id.github:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(App.GITHUB_REPOSITORY_URL)));
                return true;
        }
        mRoot.closeDrawers();
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        }
        return true;
    }

    /**
     * If back is Pressed and the user isn't on main screen, go back to main screen instead of exit
     */
    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStackImmediate();
            fm
                    .beginTransaction()
                    .setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out)
                    .commitAllowingStateLoss();
        } else {
            exit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length == 2 //2 Permissions, Internet and Write storage
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

            final TextView tvLoading = findViewById(R.id.tvLoading);
            final Thread StartThread = new Thread(new Runnable() {
                @Override
                public void run() {

                /* Creating needed folder and unpacking files */
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvLoading.setText(R.string.loading_data);
                        }
                    });
                    if (App.PathToTmp.exists()) {
                        Common.deleteFolder(App.PathToTmp, true);
                    }
                    for (File i : Folder) {
                        if (!i.exists()) {
                            if (!i.mkdirs()) {
                                App.ERRORS.add(App.TAG + " " + i + " can't be created!");
                            }
                        }
                    }

                    if (!extractFiles()) {
                        App.ERRORS.add("Failed to extract files.");
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, R.string.failed_unpack_files,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    try {
                        File LogCopy = new File(App.FilesDir, App.LastLog.getName() + ".txt");
                        App.Shell.execCommand(App.Busybox + " chmod 777 " + App.LastLog);
                        if (LogCopy.exists()) LogCopy.delete();
                        App.Shell.execCommand(App.Busybox + " cp " + App.LastLog + " " + LogCopy);
                        App.Shell.execCommand(App.Busybox + " chmod 777 " + LogCopy);
                        ApplicationInfo info = getApplicationInfo();
                        App.Shell.execCommand(
                                App.Busybox + " chown " + info.uid + ":" + info.uid + " " + LogCopy);
                        App.Shell.execCommand(App.Busybox + " chmod 777 " + LogCopy);
                        LastLogExists = LogCopy.exists();
                    } catch (FailedExecuteCommand e) {
                        App.ERRORS.add(App.TAG + " LastLog not found: " + e);
                    }
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            checkAppUpdates();
                            tvLoading.setText(R.string.reading_device);
                        }
                    });
                    if (!App.Device.isSetup()) {
                        App.Device.setup();
                    }

                /* If device is not supported, you can report it now or close the App */
                    if ((!App.Device.isRecoverySupported() && !App.Device.isKernelSupported())
                            && !BuildConfig.DEBUG) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showDeviceNotSupportedDialog();
                            }
                        });
                    } else {
                    /* App is first time started so show usage warning */
                        if (App.Preferences.getBoolean(App.PREF_KEY_FIRST_RUN, true)) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (App.Device.isRecoverySupported()
                                            || App.Device.isKernelSupported()) {
                                        //Show only if we can flash
                                        showUsageWarning();
                                    }
                                }
                            });
                        }
                    }
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                mRoot = (DrawerLayout) View.inflate(mContext, R.layout.activity_rashr, null);

                                setContentView(mRoot);
                                ButterKnife.bind(mActivity);
                                mRoot.startAnimation(AnimationUtils.loadAnimation(mContext,
                                        R.anim.abc_fade_in));
                                mToolbar = findViewById(R.id.toolbar);
                                setSupportActionBar(mToolbar);
                                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(mActivity, mRoot, mToolbar,
                                        R.string.app_name, R.string.app_name);
                                //Coloring drawertoggle
                                DrawerArrowDrawable coloredArrow = new DrawerArrowDrawable(mContext);
                                TypedValue typedValue = new TypedValue();
                                Resources.Theme theme = getTheme();
                                theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
                                coloredArrow.setColor(typedValue.data);
                                toggle.setDrawerArrowDrawable(coloredArrow);

                                mRoot.addDrawerListener(toggle);
                                toggle.syncState();
                                mNavigationView.setNavigationItemSelectedListener(mActivity);

                                if (mAds != null) {
                                    if (App.Preferences.getBoolean(App.PREF_KEY_ADS, true)) {
                                        mAds.loadAd(new AdRequest.Builder()
                                                .addTestDevice("BB1FFBF880370A581E6665C69C97D711")
                                                .build());
                                    } else {
                                        mAds.setVisibility(View.GONE);
                                        mRoot.removeView(mAds);
                                    }
                                }
                                if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
                                /* Rashr is opened by other app to flash supported files (.zip) or (.img) */
                                    File file = new File(getIntent().getData().getPath());
                                    if (file.exists()) {
                                        if (file.toString().endsWith(Device.EXT_ZIP)) {
                                        /* If it is a zip file open the ScriptManager */
                                            switchTo(ScriptManagerFragment.newInstance(file));
                                        } else if (file.toString().endsWith(Device.EXT_IMG)) {
                                        /* If it is a img file open FlashAs to choose mode (recovery or kernel) */
                                            switchTo(FlashAsFragment.newInstance(mActivity, file));
                                        }
                                    }
                                } else {
                                    onNavigationItemSelected(mNavigationView.getMenu().getItem(0));
                                }
                            } catch (NullPointerException e) {
                                setContentView(R.layout.err_layout);
                                App.ERRORS.add("Error while inflating layout:" + e);
                                AppCompatTextView tv = findViewById(R.id.tvErr);
                                try {
                                    if (tv != null) {
                                        tv.setText(R.string.failed_setup_layout);
                                    }
                                } catch (RuntimeException ex) {
                                    App.ERRORS.add(App.TAG + " " + e);
                                    ReportDialog dialog = new ReportDialog(mActivity, e.toString());
                                    dialog.show();
                                    ex.printStackTrace();
                                }
                                ReportDialog dialog = new ReportDialog(mActivity, e.toString());
                                dialog.show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
            StartThread.start();
        } else {
            //Permission not garanted
            setContentView(R.layout.err_layout);
            AppCompatTextView tverr = findViewById(R.id.tvErr);
            tverr.setText(R.string.permissions_denied);
        }

    }

    /**
     * Extract files from APK, the files are stored under App/src/res/raw
     *
     * @return files extracted successfully
     */
    private boolean extractFiles() {
        try {
            Common.pushFileFromRAW(mContext, App.RecoveryCollectionFile, R.raw.recovery_links,
                    App.isVersionChanged);
            Common.pushFileFromRAW(mContext, App.KernelCollectionFile, R.raw.kernel_sums,
                    App.isVersionChanged);
            App.Busybox = new File(mContext.getFilesDir(), "busybox");
            Common.pushFileFromRAW(mContext, App.Busybox, R.raw.busybox, App.isVersionChanged);
            try {
                App.Shell.execCommand("chmod 777 " + App.Busybox);
            } catch (FailedExecuteCommand failedExecuteCommand) {
                failedExecuteCommand.printStackTrace();
            }
            File PartLayoutsZip = new File(App.FilesDir, "partlayouts.zip");
            Common.pushFileFromRAW(mContext, PartLayoutsZip, R.raw.partlayouts, App.isVersionChanged);
            File flash_image = new File(App.FilesDir, "flash_image");
            Common.pushFileFromRAW(mContext, flash_image, R.raw.flash_image, App.isVersionChanged);
            File dump_image = new File(App.FilesDir, "dump_image");
            Common.pushFileFromRAW(mContext, dump_image, R.raw.dump_image, App.isVersionChanged);
            App.LokiPatch = new File(App.FilesDir, "loki_patch");
            Common.pushFileFromRAW(mContext, App.LokiPatch, R.raw.loki_patch, App.isVersionChanged);
            App.LokiFlash = new File(App.FilesDir, "loki_flash");
            Common.pushFileFromRAW(mContext, App.LokiFlash, R.raw.loki_flash, App.isVersionChanged);
            return true;
        } catch (IOException e) {
            App.ERRORS.add(e.toString());
            return false;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.BackupItem:
                startActivity(new Intent(this, BackupActivity.class));
                break;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*
         * Backups menu only accessible if backups are possible
         */
        if (App.Device.isRecoveryDD() || App.Device.isKernelDD()
                || App.Device.isRecoveryMTD() || App.Device.isKernelMTD()) {
            getMenuInflater().inflate(R.menu.flash_menu, menu);
            return true;
        }
        return false;
    }
}