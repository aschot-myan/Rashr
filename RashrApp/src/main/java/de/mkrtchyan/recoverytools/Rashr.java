package de.mkrtchyan.recoverytools;

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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.devspark.appmsg.AppMsg;
import com.fima.cardsui.objects.Card;
import com.fima.cardsui.objects.CardStack;
import com.fima.cardsui.views.CardUI;
import com.fima.cardsui.views.MyCard;
import com.fima.cardsui.views.MyImageCard;
import com.google.ads.AdView;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.Toolbox;
import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Downloader;
import de.mkrtchyan.utils.FileChooserDialog;
import de.mkrtchyan.utils.Notifyer;

public class Rashr extends ActionBarActivity {

    private static final String TAG = "Rashr";

    /**
     * Declaring setting names
     */
    public static final String PREF_NAME = "rashr";
    public static final String PREF_KEY_RECOVERY_HISTORY = "last_recovery_history_";
    public static final String PREF_KEY_KERNEL_HISTORY = "last_kernel_history_";
    private static final String PREF_STYLE = "style";
    private static final String PREF_KEY_ADS = "show_ads";
    private static final String PREF_KEY_CUR_VER = "current_version";
    private static final String PREF_KEY_FIRST_RUN = "first_run";
    private static final String PREF_KEY_HIDE_RATER = "show_rater";
    private static final String PREF_KEY_SHOW_UNIFIED = "show_unified";
    /**
     * Web Address for download Recovery and Kernel IMGs
     */
    private static final String RECOVERY_URL = "http://dslnexus.de/Android/recoveries";
    private static final String KERNEL_URL = "http://dslnexus.de/Android/kernel";
    private static final String RECOVERY_SUMS_URL = "http://dslnexus.de/Android/";
    private static final String KERNEL_SUMS_URL = "http://dslnexus.de/Android/";
    /**
     * Used folder and files
     */
    private static final File PathToSd = Environment.getExternalStorageDirectory();
    private static final File PathToRashr = new File(PathToSd, "Rashr");
    private static final File PathToRecoveries = new File(PathToRashr, "recoveries");
    public static final File PathToStockRecovery = new File(PathToRecoveries, "stock");
    public static final File PathToCWM = new File(PathToRecoveries, "clockworkmod");
    public static final File PathToTWRP = new File(PathToRecoveries, "twrp");
    public static final File PathToPhilz = new File(PathToRecoveries, "philz");
    private static final File PathToKernel = new File(PathToRashr, "kernel");
    public static final File PathToStockKernel = new File(PathToKernel, "stock");
    public static final File PathToRecoveryBackups = new File(PathToRashr, "recovery-backups");
    public static final File PathToKernelBackups = new File(PathToRashr, "kernel-backups");
    public static final File PathToUtils = new File(PathToRashr, "utils");
    public static final File LastLog = new File("/cache/recovery/last_log");
    public final File Folder[] = {PathToRashr, PathToRecoveries, PathToKernel, PathToStockRecovery,
            PathToCWM, PathToTWRP, PathToPhilz, PathToStockKernel, PathToRecoveryBackups,
            PathToKernelBackups, PathToUtils};
    private final ActionBarActivity mActivity = this;
    private final Context mContext = this;
    /**
     * Declaring needed objects
     */
    private final int EMAIL_REQ_CODE = 8451;
    private final int APPCOMPAT_DARK = R.style.MyDark;
    private final int APPCOMPAT_LIGHT = R.style.MyLight;
    private final int APPCOMPAT_LIGHT_DARK_BAR = R.style.MyLightDarkBar;
    private final ArrayList<String> ERRORS = new ArrayList<String>();
    private final ArrayList<String> FlashUtils_ERRORS = new ArrayList<String>();
    private File RecoveryCollectionFile, KernelCollectionFile;
    private File fRECOVERY, fKERNEL;
    private Shell mShell;
    private Toolbox mToolbox;
    private Device mDevice;
    private DrawerLayout mRashrLayout = null;
    private SwipeRefreshLayout mSwipeUpdater = null;
    private FileChooserDialog fcFlashOtherRecovery = null;
    private final Runnable rRecoveryFlasher = new Runnable() {
        @Override
        public void run() {
            if (fRECOVERY != null) {
                if (fRECOVERY.exists() && fRECOVERY.getName().endsWith(mDevice.getRecoveryExt())
                        && !fRECOVERY.isDirectory()) {
                    if (!mDevice.isFOTAFlashed() && !mDevice.isRecoveryOverRecovery()) {
                        /** Flash not need to be handled specially */
                        rFlashRecovery.run();
                    } else {
                        /** Flashing needs to be handled specially (not standard flash method)*/
                        if (mDevice.isFOTAFlashed()) {
                            /** Show warning if FOTAKernel will be flashed */
                            new AlertDialog.Builder(mContext)
                                    .setTitle(R.string.warning)
                                    .setMessage(R.string.fota)
                                    .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            rFlashRecovery.run();
                                        }
                                    })
                                    .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();
                        } else {
                            /** Get user input if user want to install over recovery now */
                            showOverRecoveryInstructions();
                        }
                    }
                }
            }
            fcFlashOtherRecovery = null;
        }
    };
    private ArrayAdapter<String> RecoveryBakAdapter;
    private ArrayAdapter<String> KernelBakAdapter;
    private FileChooserDialog fcFlashOtherKernel = null;
    private final Runnable rKernelFlasher = new Runnable() {
        @Override
        public void run() {
            if (fKERNEL != null) {
                if (fKERNEL.exists() && fKERNEL.getName().endsWith(mDevice.getKernelExt())
                        && !fKERNEL.isDirectory()) {
                    rFlashKernel.run();
                }
            }
            fcFlashOtherKernel = null;
        }
    };
    private boolean version_changed = false;
    private boolean keepAppOpen = true;
    //	"Methods" need a input from user (AlertDialog) or at the end of AsyncTasks
    private final Runnable rFlashRecovery = new Runnable() {
        @Override
        public void run() {
            final FlashUtil flashUtil = new FlashUtil(mShell, mContext, mDevice, fRECOVERY,
                    FlashUtil.JOB_FLASH_RECOVERY);
            flashUtil.setKeepAppOpen(keepAppOpen);
            flashUtil.setRunAtEnd(new Runnable() {
                @Override
                public void run() {
                    FlashUtils_ERRORS.addAll(flashUtil.getERRORS());
                }
            });
            flashUtil.execute();
        }
    };
    private final Runnable rFlashKernel = new Runnable() {
        @Override
        public void run() {
            final FlashUtil flashUtil = new FlashUtil(mShell, mContext, mDevice, fKERNEL,
                    FlashUtil.JOB_FLASH_KERNEL);
            flashUtil.setKeepAppOpen(keepAppOpen);
            flashUtil.setRunAtEnd(new Runnable() {
                @Override
                public void run() {
                    FlashUtils_ERRORS.addAll(flashUtil.getERRORS());
                }
            });
            flashUtil.execute();
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RecoveryCollectionFile = new File(getFilesDir(), "recovery_sums");
        KernelCollectionFile = new File(getFilesDir(), "kernel_sums");

        if (Common.getIntegerPref(mContext, PREF_NAME, PREF_STYLE) == 0) {
            /**
             * If theme preference is not set (usually on first start)
             * using AppCompat_Light_Dark_Bar theme
             */
            setTheme(APPCOMPAT_LIGHT_DARK_BAR);
        } else {
            /** Using predefined theme */
            setTheme(Common.getIntegerPref(mContext, PREF_NAME, PREF_STYLE));
        }

        setContentView(R.layout.loading_layout);

        final TextView tvLoading = (TextView) findViewById(R.id.tvLoading);

        final Thread StartThread = new Thread(new Runnable() {
            @Override
            public void run() {
                /** Try to get root access */
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvLoading.setText(R.string.getting_root);
                    }
                });

	            try {
                    mShell = Shell.startRootShell(mContext);
                    mToolbox = new Toolbox(mShell);
                } catch (IOException e) {

                    ERRORS.add(e.toString());
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

                try {
                    unpackFiles();
                } catch (IOException e) {
                    ERRORS.add(e.toString());
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvLoading.setText(R.string.failed_unpack_files);
                            tvLoading.setTextColor(Color.RED);
                        }
                    });
                    return;
                }

                try {
                    File LogCopy = new File(mContext.getFilesDir(), LastLog.getName() + ".txt");
                    mToolbox.setFilePermissions(LastLog, "666");
                    mToolbox.copyFile(LastLog, LogCopy, false, false);
                } catch (Exception e) {
                    ERRORS.add(e.toString());
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvLoading.setText(R.string.reading_device);
                    }
                });
                mDevice = new Device(mContext);
                /** Creating needed folder and unpacking files */
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvLoading.setText(R.string.loading_data);
                    }
                });
                for (File i : Folder) {
                    if (!i.exists()) {
                        if (i.mkdir()) {
                            ERRORS.add(i.getAbsolutePath() + " can't be created!");
                        }
                    }
                }

                /** If device is not supported, you can report it now or close the App */
                if (!mDevice.isRecoverySupported() && !mDevice.isKernelSupported()) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showDeviceNotSupportedDialog();
                        }
                    });
                } else {
                    Common.setBooleanPref(mContext, PREF_NAME, PREF_KEY_SHOW_UNIFIED, true);
                    if (!Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_FIRST_RUN)) {
                        /** Setting first start configuration */
                        Common.setIntegerPref(mContext, PREF_NAME, PREF_STYLE, APPCOMPAT_LIGHT_DARK_BAR);
                        Common.setBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS, true);
                        Common.setBooleanPref(mContext, Shell.PREF_NAME, Shell.PREF_LOG, true);
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
                                final int previous_version = Common.getIntegerPref(mContext, PREF_NAME, PREF_KEY_CUR_VER);
                                final int current_version = pInfo.versionCode;
                                version_changed = current_version > previous_version;
                                Common.setIntegerPref(mContext, PREF_NAME, PREF_KEY_CUR_VER, current_version);
                            } else {
                                version_changed = true;
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            ERRORS.add(e.toString());
                            version_changed = true;
                        }
                        if (version_changed) {
                            Common.setBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS, true);
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_HIDE_RATER)) {
                                        Notifyer.showAppRateDialog(mContext, PREF_NAME, PREF_KEY_HIDE_RATER);
                                    }
                                    showChangelog();
                                }
                            });
                        }
                    }
                }
                try {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            optimizeLayout();
                            mDevice.downloadUtils(mContext);
                        }
                    });
                } catch (NullPointerException e) {
                    ERRORS.add(e.toString());
                    try {
                        tvLoading.setText(R.string.failed_setup_layout);
                        tvLoading.setTextColor(Color.RED);
                    } catch (RuntimeException ex) {
                        ERRORS.add(ex.toString());
                        ex.printStackTrace();

                    }
                }
            }
        });
        StartThread.start();
    }

    /**
     * Buttons on FlashRecovery and FlashKernel Dialog
     */
    public void FlashSupportedRecovery(Card card) {
        fRECOVERY = null;
        final File path;
        final ArrayList<String> Versions;
        if (!mDevice.downloadUtils(mContext)) {
            /**
             * If there files be needed to flash download it and listing device specified
             * recovery file for example recovery-clockwork-touch-6.0.3.1-grouper.img
             * (read out from RECOVERY_SUMS)
             */
            String SYSTEM = card.getData().toString();
            ArrayAdapter<String> VersionsAdapter = new ArrayAdapter<String>(mContext,
                    R.layout.custom_version_list_item);
            if (SYSTEM.equals("stock")) {
                Versions = mDevice.getStockRecoveryVersions();
                path = PathToStockRecovery;
                for (String i : Versions) {
                    try {
                        VersionsAdapter.add("Stock " + i.split("-")[3].replace(mDevice.getRecoveryExt(), ""));
                    } catch (ArrayIndexOutOfBoundsException e) {
	                    ERRORS.add(e.toString() + " failed while formatting version (Stock Recovery)");
                        VersionsAdapter.add(i);
                    }
                }
            } else if (SYSTEM.equals("clockwork")) {
                Versions = mDevice.getCwmRecoveryVersions();
                path = PathToCWM;
                for (String i : Versions) {
                    try {
                        if (i.contains("-touch-")) {
                            String device = "(";
                            for (int splitNr = 4; splitNr < i.split("-").length; splitNr++) {
                                if (!device.equals("(")) device += "-";
                                device += i.split("-")[splitNr].replace(mDevice.getRecoveryExt(), "");
                            }
                            device += ")";
                            VersionsAdapter.add("ClockworkMod Touch " + i.split("-")[3] + " " + device);
                        } else {
							String device = "(";
                            for (int splitNr = 3; splitNr < i.split("-").length; splitNr++) {
                                if (!device.equals("(")) device += "-";
                                device += i.split("-")[splitNr].replace(mDevice.getRecoveryExt(), "");
                            }
                            device += ")";
                            VersionsAdapter.add("ClockworkMod " + i.split("-")[2] + " " + device);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
	                    ERRORS.add(e.toString() + " failed while formatting version (CWM Recovery)");
                        VersionsAdapter.add(i);
                    }
                }
            } else if (SYSTEM.equals("twrp")) {
                Versions = mDevice.getTwrpRecoveryVersions();
                path = PathToTWRP;
                for (String i : Versions) {
                    try {
                        if (i.contains("openrecovery")) {
                            String device = "(";
                            for (int splitNr = 3; splitNr < i.split("-").length; splitNr++) {
                                if (!device.equals("(")) device += "-";
                                device += i.split("-")[splitNr].replace(mDevice.getRecoveryExt(), "");
                            }
                            device += ")";
                            VersionsAdapter.add("TWRP " + i.split("-")[2] + " " + device);
                        } else {
                            VersionsAdapter.add("TWRP " + i.split("-")[1].replace(mDevice.getRecoveryExt(), "") + ")");
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
	                    ERRORS.add(e.toString() + " failed while formatting version (TWRP Recovery)");
                        VersionsAdapter.add(i);
                    }
                }
            } else if (SYSTEM.equals("philz")) {
                Versions = mDevice.getPhilzRecoveryVersions();
                path = PathToPhilz;
                for (String i : Versions) {
                    try {
                        String device = "(";
                        for (int splitNr = 1; splitNr < i.split("-").length; splitNr++) {
                            if (!device.equals("(")) device += "-";
                            device += i.split("-")[splitNr].replace(mDevice.getRecoveryExt(), "");
                        }
                        device += ")";
                        VersionsAdapter.add("PhilZ Touch " + i.split("_")[2].split("-")[0] + " " + device);
                    } catch (ArrayIndexOutOfBoundsException e) {
	                    ERRORS.add(e.toString() + " failed while formatting version (PHILZ Recovery)");
                        VersionsAdapter.add(i);
                    }
                }
            } else {
                return;
            }

            final Dialog RecoveriesDialog = new Dialog(mContext);
            RecoveriesDialog.setTitle(SYSTEM.toUpperCase());
            ListView VersionList = new ListView(mContext);
            RecoveriesDialog.setContentView(VersionList);

            VersionList.setAdapter(VersionsAdapter);
            RecoveriesDialog.show();
            VersionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    RecoveriesDialog.dismiss();

                    final String fileName = Versions.get(i);
                    fRECOVERY = new File(path, fileName);
                    if (!fRECOVERY.exists()) {
                        Downloader RecoveryDownloader = new Downloader(mContext, RECOVERY_URL,
                                fRECOVERY, new Downloader.OnDownloadListener() {
                            @Override
                            public void success(File file) {
                                rRecoveryFlasher.run();
                            }

                            @Override
                            public void failed(Exception e) {
	                            ERRORS.add(e.toString());
                            }
                        });
                        RecoveryDownloader.setRetry(true);
                        RecoveryDownloader.setAskBeforeDownload(true);
                        RecoveryDownloader.setChecksumFile(RecoveryCollectionFile);
                        RecoveryDownloader.ask();
                    } else {
                        rRecoveryFlasher.run();
                    }
                }
            });
        }
    }

    /**
     * Flash Recovery from storage (separate downloaded)
     */
    public void bFlashOtherRecovery(View view) {
        fRECOVERY = null;
        if (fcFlashOtherRecovery == null) {
            String AllowedEXT[] = {mDevice.getRecoveryExt()};
            fcFlashOtherRecovery = new FileChooserDialog(view.getContext());
            fcFlashOtherRecovery.setAllowedEXT(AllowedEXT);
            fcFlashOtherRecovery.setBrowseUpAllowed(true);
            fcFlashOtherRecovery.setOnFileChooseListener(new FileChooserDialog.OnFileChooseListener() {
                @Override
                public void OnFileChoose(File file) {
                    fRECOVERY = file;
                    rRecoveryFlasher.run();
                }
            });
            fcFlashOtherRecovery.setStartFolder(PathToSd);
            fcFlashOtherRecovery.setWarn(true);
        }
        fcFlashOtherRecovery.show();
    }

    public void FlashSupportedKernel(Card card) {
        fKERNEL = null;
        final File path;
        ArrayList<String> Versions;
        if (!mDevice.downloadUtils(mContext)) {
            /**
             * If there files be needed to flash download it and listing device specified recovery
             * file for example stock-boot-grouper-4.4.img (read out from kernel_sums)
             */
            String SYSTEM = card.getData().toString();
            if (SYSTEM.equals("stock")) {
                Versions = mDevice.getStockKernelVersions();
                path = PathToStockKernel;
            } else {
                return;
            }

            final Dialog KernelDialog = new Dialog(mContext);
            KernelDialog.setTitle(SYSTEM);
            ListView VersionList = new ListView(mContext);
            KernelDialog.setContentView(VersionList);
            VersionList.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, Versions));
            KernelDialog.show();
            VersionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    KernelDialog.dismiss();
                    final CharSequence fileName;
                    if ((fileName = ((TextView) view).getText()) != null) {
                        fKERNEL = new File(path, fileName.toString());

                        if (!fKERNEL.exists()) {
                            Downloader RecoveryDownloader = new Downloader(mContext, KERNEL_URL,
                                    fKERNEL, new Downloader.OnDownloadListener() {
                                @Override
                                public void success(File file) {
                                    rRecoveryFlasher.run();
                                }

                                @Override
                                public void failed(Exception e) {

                                }
                            });
                            RecoveryDownloader.setRetry(true);
                            RecoveryDownloader.setAskBeforeDownload(true);
                            RecoveryDownloader.setChecksumFile(KernelCollectionFile);
                            RecoveryDownloader.ask();
                        } else {
                            rKernelFlasher.run();
                        }
                    }
                }
            });
        }
    }

    /**
     * Flash Kernel from storage (separate downloaded)
     */
    public void bFlashOtherKernel(View view) {
        fKERNEL = null;
        if (fcFlashOtherKernel == null) {
            String AllowedEXT[] = {mDevice.getKernelExt()};
            fcFlashOtherKernel = new FileChooserDialog(view.getContext());
            fcFlashOtherKernel.setOnFileChooseListener(new FileChooserDialog.OnFileChooseListener() {
                @Override
                public void OnFileChoose(File file) {
                    fKERNEL = file;
                    rKernelFlasher.run();
                }
            });
            fcFlashOtherKernel.setStartFolder(PathToSd);
            fcFlashOtherKernel.setAllowedEXT(AllowedEXT);
            fcFlashOtherKernel.setBrowseUpAllowed(true);
            fcFlashOtherKernel.setWarn(true);
        }
        fcFlashOtherKernel.show();
    }

    public void showFlashHistory(Card card) {
        final boolean RecoveryHistory = card.getData().toString().equals("recovery");
        final String PREF_KEY = RecoveryHistory ? PREF_KEY_RECOVERY_HISTORY : PREF_KEY_KERNEL_HISTORY;
        final String EXT = RecoveryHistory ? mDevice.getRecoveryExt() : mDevice.getKernelExt();
        final ArrayList<File> HistoryFiles = new ArrayList<File>();
        final ArrayList<String> HistoryFileNames = new ArrayList<String>();
        final Dialog HistoryDialog = new Dialog(mContext);
        HistoryDialog.setTitle(R.string.sHistory);
        ListView HistoryList = new ListView(mContext);
        File tmp;
        for (int i = 0; i < 5; i++) {
            tmp = new File(Common.getStringPref(mContext, PREF_NAME, PREF_KEY + String.valueOf(i)));
            if (tmp.exists() && !tmp.isDirectory() && tmp.getName().endsWith(EXT)) {
                HistoryFiles.add(tmp);
                HistoryFileNames.add(tmp.getName());
            } else {
                Common.setStringPref(mContext, PREF_NAME, PREF_KEY + String.valueOf(i), "");
            }
        }
        HistoryList.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, HistoryFileNames));
        HistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                if (HistoryFiles.get(arg2).exists()) {
                    if (RecoveryHistory) {
                        fRECOVERY = HistoryFiles.get(arg2);
                        rRecoveryFlasher.run();
                    } else {
                        fKERNEL = HistoryFiles.get(arg2);
                        rKernelFlasher.run();
                    }
                    HistoryDialog.dismiss();
                } else {
                    AppMsg
                            .makeText(mActivity, R.string.no_choosed, AppMsg.STYLE_ALERT)
                            .show();
                }
            }
        });
        HistoryDialog.setContentView(HistoryList);
        if (HistoryFileNames.toArray().length > 0) {
            HistoryDialog.show();
        } else {
            AppMsg
                    .makeText(mActivity, R.string.no_history, AppMsg.STYLE_ALERT)
                    .show();
        }
    }

    /**
     * RadioButtons on FlashAs Layout
     */
    public void FlashAsOpt(View view) {
        if (view.getTag().toString().equals("recovery")) {
            RadioButton optAsKernel = (RadioButton) findViewById(R.id.optAsKernel);
            optAsKernel.setChecked(false);
        } else {
            RadioButton optAsRecovery = (RadioButton) findViewById(R.id.optAsRecovery);
            optAsRecovery.setChecked(false);
        }
        findViewById(R.id.bFlashAs).setEnabled(true);
    }

    public void flashAs(View view) {
        Uri path;
        keepAppOpen = false;
        if ((path = getIntent().getData()) != null) {
            final File IMG = new File(path.getPath());
            if (IMG.exists()) {
                RadioButton optAsRecovery = (RadioButton) findViewById(R.id.optAsRecovery);
                if (optAsRecovery.isChecked()) {
                    fRECOVERY = IMG;
                    rRecoveryFlasher.run();
                } else {
                    fKERNEL = IMG;
                    rKernelFlasher.run();
                }
            } else {
                exit();
            }
        } else {
            exit();
        }
    }

    /**
     * Buttons from MenuDrawer
     */
    public void bOpenRecoveryScriptManager(View view) {
        startActivity(new Intent(this, RecoveryScriptManager.class));
    }

    public void bDonate(View view) {
        startActivity(new Intent(view.getContext(), DonationsActivity.class));
    }

    public void bXDA(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://forum.xda-developers.com/showthread.php?t=2334554")));
    }

    public void bExit(View view) {
        exit();
    }

    public void cbLog(View view) {
        CheckBox cbLog = (CheckBox) view;
        Common.setBooleanPref(mContext, Shell.PREF_NAME, Shell.PREF_LOG,
                !Common.getBooleanPref(mContext, Shell.PREF_NAME, Shell.PREF_LOG));
        cbLog.setChecked(Common.getBooleanPref(mContext, Shell.PREF_NAME, Shell.PREF_LOG));
        if (cbLog.isChecked()) {
            findViewById(R.id.bShowLogs).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.bShowLogs).setVisibility(View.INVISIBLE);
        }
    }

    public void bReport(View view) {
        report(true);
    }

    public void bShowLogs(View view) {
        Common.showLogs(view.getContext());
    }

    public void cbShowAds(View view) {
        Common.setBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS,
                !Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS));
        ((CheckBox) view).setChecked(Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS));
        optimizeLayout();
    }

    /**
     * Buttons on BackupDrawer
     */
    public void bCreateBackup(View view) {
        createBackup(view.getTag().equals("recovery"));
    }

    public void createBackup(final boolean RecoveryBackup) {
        String prefix;
        String CurrentName;
        String EXT;
        if (RecoveryBackup) {
            prefix = "recovery";
            EXT = mDevice.getRecoveryExt();
            CurrentName = mDevice.getRecoveryVersion();
        } else {
            prefix = "kernel";
            EXT = mDevice.getKernelExt();
            CurrentName = mDevice.getKernelVersion();
        }
        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle(R.string.setname);
        dialog.setContentView(R.layout.dialog_input);
        final Button bGoBackup = (Button) dialog.findViewById(R.id.bGoBackup);
        final EditText etFileName = (EditText) dialog.findViewById(R.id.etFileName);
        final CheckBox optName = (CheckBox) dialog.findViewById(R.id.cbOptInput);
        final String NameHint = prefix + "-from-" + Calendar.getInstance().get(Calendar.DATE)
                + "-" + Calendar.getInstance().get(Calendar.MONTH)
                + "-" + Calendar.getInstance().get(Calendar.YEAR)
                + "-" + Calendar.getInstance().get(Calendar.HOUR)
                + ":" + Calendar.getInstance().get(Calendar.MINUTE) + EXT;
        optName.setText(CurrentName);
        optName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etFileName.setEnabled(!optName.isChecked());
            }
        });

        etFileName.setHint(NameHint);
        bGoBackup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String EXT;
                File Path;
                final int JOB;
                if (RecoveryBackup) {
                    EXT = mDevice.getRecoveryExt();
                    Path = PathToRecoveryBackups;
                    JOB = FlashUtil.JOB_BACKUP_RECOVERY;
                } else {
                    EXT = mDevice.getKernelExt();
                    Path = PathToKernelBackups;
                    JOB = FlashUtil.JOB_BACKUP_KERNEL;
                }

                CharSequence Name = "";
                if (optName.isChecked()) {
                    Name = optName.getText() + EXT;
                } else {
                    if (etFileName.getText() != null && !etFileName.getText().toString().equals("")) {
                        Name = etFileName.getText().toString();
                    }

                    if (Name.equals("")) {
                        Name = String.valueOf(etFileName.getHint());
                    }

                    if (!Name.toString().endsWith(EXT)) {
                        Name = Name + EXT;
                    }
                }

                final File fBACKUP = new File(Path, Name.toString());
                if (fBACKUP.exists()) {
                    AppMsg
                            .makeText(mActivity, R.string.backupalready, AppMsg.STYLE_ALERT)
                            .show();
                } else {
                    final FlashUtil BackupCreator = new FlashUtil(mShell, mContext, mDevice, fBACKUP,
                            JOB);
                    BackupCreator.setRunAtEnd(new Runnable() {
                        @Override
                        public void run() {
                            FlashUtils_ERRORS.addAll(BackupCreator.getERRORS());
                            loadBackups();
                        }
                    });
                    BackupCreator.execute();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showFlashRecoveryDialog() {
        final Dialog FlashRecoveryDialog = new Dialog(mContext);
        FlashRecoveryDialog.setTitle(R.string.flash_options);
        CardUI RecoveryCards = new CardUI(mContext);
        FlashRecoveryDialog.setContentView(RecoveryCards);
        if (mDevice.isCwmRecoverySupported()) {
            final MyImageCard CWMCard = new MyImageCard(getString(R.string.sCWM), R.drawable.ic_cwm,
                    getString(R.string.cwm_description));
            CWMCard.setData("clockwork");
            CWMCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FlashSupportedRecovery(CWMCard);
                }
            });
            RecoveryCards.addCard(CWMCard, true);
        }
        if (mDevice.isTwrpRecoverySupported()) {
            final MyImageCard TWRPCard = new MyImageCard(getString(R.string.sTWRP), R.drawable.ic_twrp,
                    getString(R.string.twrp_description));
            TWRPCard.setData("twrp");
            TWRPCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FlashSupportedRecovery(TWRPCard);
                }
            });
            RecoveryCards.addCard(TWRPCard);
        }
        if (mDevice.isPhilzRecoverySupported()) {
            final MyCard PHILZCard = new MyCard(getString(R.string.sPhilz), getString(R.string.philz_description));
            PHILZCard.setData("philz");
            PHILZCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FlashSupportedRecovery(PHILZCard);
                }
            });
            RecoveryCards.addCard(PHILZCard, true);
        }
        if (mDevice.isStockRecoverySupported()) {
            final MyImageCard StockCard = new MyImageCard(getString(R.string.stock), R.drawable.ic_stock,
                    getString(R.string.stock_recovery_description));
            StockCard.setData("stock");
            StockCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FlashSupportedRecovery(StockCard);
                }
            });
            RecoveryCards.addCard(StockCard, true);
        }

        final MyCard OtherCard = new MyCard(getString(R.string.sOTHER), getString(R.string.other_storage_description));
        OtherCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bFlashOtherRecovery(v);
            }
        });
        RecoveryCards.addCard(OtherCard, true);

        final MyCard HistoryCard = new MyCard(getString(R.string.sHistory),
                getString(R.string.history_description));
        HistoryCard.setData("recovery");
        HistoryCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFlashHistory(HistoryCard);
            }
        });
        RecoveryCards.addCard(HistoryCard, true);

        /** Check if device uses unified builds */
        if (Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_SHOW_UNIFIED)) {
            if ((mDevice.getName().startsWith("d2lte") || mDevice.getName().startsWith("hlte")
                    || mDevice.getName().startsWith("jflte")
                    || (mDevice.getManufacture().equals("motorola") && mDevice.getBOARD().equals("msm8960")))
                    && (!mDevice.isStockRecoverySupported() || !mDevice.isCwmRecoverySupported()
                    || !mDevice.isTwrpRecoverySupported() || !mDevice.isPhilzRecoverySupported())) {
                showUnifiedBuildsDialog();
            } else {
                FlashRecoveryDialog.show();
            }
        } else {
            FlashRecoveryDialog.show();
        }
    }

    private void showFlashKernelDialog() {
        final Dialog FlashKernelDialog = new Dialog(mContext);
        FlashKernelDialog.setTitle(R.string.flash_options);
        CardUI KernelCards = new CardUI(mContext);
        FlashKernelDialog.setContentView(KernelCards);
        if (mDevice.isStockKernelSupported()) {
            final MyImageCard StockCard = new MyImageCard(getString(R.string.stock), R.drawable.ic_stock,
                    getString(R.string.stock_kernel_description));
            StockCard.setData("stock");
            StockCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FlashSupportedKernel(StockCard);
                }
            });
            KernelCards.addCard(StockCard, true);
        }

        final MyCard OtherCard = new MyCard(getString(R.string.sOTHER),
                getString(R.string.other_storage_description));
        OtherCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bFlashOtherKernel(v);
            }
        });
        KernelCards.addCard(OtherCard, true);

        final MyCard HistoryCard = new MyCard(getString(R.string.sHistory),
                getString(R.string.history_description));
        HistoryCard.setData("kernel");
        HistoryCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFlashHistory(HistoryCard);
            }
        });
        KernelCards.addCard(HistoryCard, true);

        FlashKernelDialog.show();

        /** Check if device uses unified builds */
        if (Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_SHOW_UNIFIED)) {
            if ((mDevice.getName().startsWith("d2lte") || mDevice.getName().startsWith("hlte")
                    || mDevice.getName().startsWith("jflte") || mDevice.getName().equals("moto_msm8960"))
                    && (!mDevice.isStockRecoverySupported() || !mDevice.isCwmRecoverySupported()
                    || !mDevice.isTwrpRecoverySupported() || !mDevice.isPhilzRecoverySupported())) {
                showUnifiedBuildsDialog();
            } else {
                FlashKernelDialog.show();
            }
        } else {
            FlashKernelDialog.show();
        }
    }

    public void report(final boolean isCancelable) {
        final Dialog reportDialog = new Dialog(mContext);
        reportDialog.setTitle(R.string.commentar);
        reportDialog.setContentView(R.layout.dialog_comment);
        reportDialog.setCancelable(isCancelable);
        new Thread(new Runnable() {
            @Override
            public void run() {
                /** Creates a report Email including a Comment and important device infos */
                final Button bGo = (Button) reportDialog.findViewById(R.id.bGo);
                bGo.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (!Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS))
                            AppMsg.makeText(mActivity, R.string.please_ads, AppMsg.STYLE_ALERT).show();
                        AppMsg.makeText(mActivity, R.string.donate_to_support, AppMsg.STYLE_ALERT).show();
                        try {
                            ArrayList<File> files = new ArrayList<File>();
                            File TestResults = new File(mContext.getFilesDir(), "results.txt");
                            try {
                                if (TestResults.exists()) {
                                    if (TestResults.delete()) {
                                        FileOutputStream fos = openFileOutput(TestResults.getName(), Context.MODE_PRIVATE);
                                        fos.write(("Rashr:\n\n" + mShell.execCommand("ls -lR " + PathToRashr.getAbsolutePath()) +
                                                "\nCache Tree:\n" + mShell.execCommand("ls -lR /cache") + "\n" +
                                                "\nMTD result:\n" + mShell.execCommand("cat /proc/mtd") + "\n" +
                                                "\nDevice Tree:\n\n" + mShell.execCommand("ls -lR /dev")).getBytes());
                                    }
                                    files.add(TestResults);
                                }
                            } catch (Exception e) {
                                ERRORS.add(e.toString());
                            }
                            if (getPackageManager() != null) {
                                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                EditText text = (EditText) reportDialog.findViewById(R.id.etComment);
                                String comment = "";
                                if (text.getText() != null) comment = text.getText().toString();
                                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ashotmkrtchyan1995@gmail.com"});
                                intent.putExtra(Intent.EXTRA_SUBJECT, "Rashr " + pInfo.versionName + " report");
                                String message = "Package Infos:" +
                                        "\n\nName: " + pInfo.packageName +
                                        "\nVersion Code: " + pInfo.versionCode;
                                message +=
                                        "\n\n\nProduct Info: " +
                                                "\n\nManufacture: " + Build.MANUFACTURER + " (" + mDevice.getManufacture() + ") " +
                                                "\nDevice: " + Build.DEVICE + " (" + mDevice.getName() + ")" +
                                                "\nBoard: " + Build.BOARD +
                                                "\nBrand: " + Build.BRAND +
                                                "\nModel: " + Build.MODEL +
                                                "\nFingerprint: " + Build.FINGERPRINT +
                                                "\nAndroid SDK Level: " + Build.VERSION.CODENAME + " (" + Build.VERSION.SDK_INT + ")";

                                if (mDevice.isRecoverySupported()) {
                                    message += "\n\nRecovery Path: " + mDevice.getRecoveryPath() +
                                            "\nRecovery Version: " + mDevice.getRecoveryVersion() +
                                            "\nRecovery MTD: " + mDevice.isRecoveryMTD() +
                                            "\nRecovery DD: " + mDevice.isRecoveryDD() +
                                            "\nStock: " + mDevice.isStockRecoverySupported() +
                                            "\nCWM: " + mDevice.isCwmRecoverySupported() +
                                            "\nTWRP: " + mDevice.isTwrpRecoverySupported() +
                                            "\nPHILZ: " + mDevice.isPhilzRecoverySupported();
                                }
                                if (mDevice.isKernelSupported()) {
                                    message += "\n\nKernel Path: " + mDevice.getKernelPath() +
                                            "\nKernel Version: " + mDevice.getKernelVersion() +
                                            "\nKernel MTD: " + mDevice.isKernelMTD() +
                                            "\nKernel DD: " + mDevice.isKernelDD();
                                }
                                if (!comment.equals("")) {
                                    message +=
                                            "\n\n\n===========COMMENT==========\n"
                                                    + comment +
                                                    "\n=========COMMENT END========\n";
                                }
                                message +=
                                        "\n===========PREFS==========\n"
                                                + getAllPrefs() +
                                                "\n=========PREFS END========\n";

                                if (ERRORS.size() > 0) {
                                    message += "Rashr ERRORS:\n";
                                    for (String error : ERRORS) {
                                        message += error + "\n";
                                    }
                                }

                                if (mDevice.getERRORS().size() > 0) {
                                    message += "Device ERRORS:\n";
                                    for (String error : mDevice.getERRORS()) {
                                        message += error + "\n";
                                    }
                                }

                                if (FlashUtils_ERRORS.size() > 0) {
                                    message += "FlashUtils ERRORS:\n";
                                    for (String error : FlashUtils_ERRORS) {
                                        message += error + "\n";
                                    }
                                }

                                intent.putExtra(Intent.EXTRA_TEXT, message);
                                files.add(new File(getFilesDir(), Shell.Logs));
                                files.add(new File(getFilesDir(), "last_log.txt"));
                                ArrayList<Uri> uris = new ArrayList<Uri>();
                                for (File file : files) {
                                    if (file.exists()) {
                                        try {
                                            mToolbox.setFilePermissions(file, "666");
                                            uris.add(Uri.fromFile(file));
                                        } catch (FailedExecuteCommand e) {
                                            ERRORS.add(e.toString());
                                        }
                                    }
                                }
                                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                                startActivityForResult(Intent.createChooser(intent, "Send over Gmail"),
                                        EMAIL_REQ_CODE);
                                reportDialog.dismiss();
                            }
                        } catch (Exception e) {
                            ERRORS.add(e.toString());
                            reportDialog.dismiss();
                            Notifyer.showExceptionToast(mContext, TAG, e);
                        }
                    }
                });
            }
        }).start();
        reportDialog.setCancelable(isCancelable);
        reportDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mRashrLayout.isDrawerOpen(Gravity.LEFT)) {
                    mRashrLayout.closeDrawer(Gravity.LEFT);
                } else {
                    mRashrLayout.openDrawer(Gravity.LEFT);
                }
                if (mRashrLayout.isDrawerOpen(Gravity.RIGHT)) {
                    mRashrLayout.closeDrawer(Gravity.LEFT);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChangelog() {
        if (version_changed) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setTitle(R.string.changelog);
            WebView changes = new WebView(mContext);
            changes.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            changes.setWebViewClient(new WebViewClient());
            changes.loadUrl("http://forum.xda-developers.com/showpost.php?p=42839329&postcount=2");
            changes.clearCache(true);
            dialog.setView(changes);
            dialog.show();
        }
    }

    private void showUsageWarning() {
        if (mDevice.isRecoverySupported() || mDevice.isKernelSupported()) {
            final AlertDialog.Builder WarningDialog = new AlertDialog.Builder(mContext);
            WarningDialog.setTitle(R.string.warning);
            WarningDialog.setMessage(String.format(getString(R.string.bak_warning),
                    PathToRecoveryBackups + " & " + PathToKernelBackups));
            WarningDialog.setPositiveButton(R.string.backup, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    createBackup(mDevice.isRecoveryDD() || mDevice.isRecoveryMTD());
                    createBackup(!mDevice.isKernelDD() && !mDevice.isKernelMTD());
                    Common.setBooleanPref(mContext, PREF_NAME, PREF_KEY_FIRST_RUN, true);
                }
            });
            WarningDialog.setNegativeButton(R.string.risk, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Common.setBooleanPref(mContext, PREF_NAME, PREF_KEY_FIRST_RUN, true);
                }
            });
            WarningDialog.setCancelable(false);
            WarningDialog.show();
        }
    }

    public void showPopup(int Menu, final View v) {
        PopupMenu popup = new PopupMenu(mContext, v);
        popup.getMenuInflater().inflate(Menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {
                    CharSequence text = ((TextView) v).getText();
                    if (text != null) {
                        final String FileName = text.toString();

                        final Dialog dialog = new Dialog(mContext);
                        dialog.setTitle(R.string.setname);
                        dialog.setContentView(R.layout.dialog_input);
                        final Button bGo = (Button) dialog.findViewById(R.id.bGoBackup);
                        final EditText etFileName = (EditText) dialog.findViewById(R.id.etFileName);


                        switch (menuItem.getItemId()) {
                            case R.id.iRestoreRecovery:
                                final FlashUtil RestoreRecoveryUtil = new FlashUtil(mShell, mContext, mDevice,
                                        new File(PathToRecoveryBackups, FileName), FlashUtil.JOB_RESTORE_RECOVERY);
                                RestoreRecoveryUtil.setRunAtEnd(new Runnable() {
                                    @Override
                                    public void run() {
                                        FlashUtils_ERRORS.addAll(RestoreRecoveryUtil.getERRORS());
                                    }
                                });
                                RestoreRecoveryUtil.execute();
                                return true;
                            case R.id.iRenameRecovery:
                                etFileName.setHint(FileName);
                                bGo.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {

                                        String Name;
                                        if (etFileName.getText() != null && etFileName.isEnabled()
                                                && !etFileName.getText().toString().equals("")) {
                                            Name = etFileName.getText().toString();
                                        } else {
                                            Name = String.valueOf(etFileName.getHint());
                                        }

                                        if (!Name.endsWith(mDevice.getRecoveryExt())) {
                                            Name = Name + mDevice.getRecoveryExt();
                                        }

                                        File renamedBackup = new File(PathToRecoveryBackups, Name);

                                        if (renamedBackup.exists()) {
                                            AppMsg
                                                    .makeText(mActivity, R.string.backupalready, AppMsg.STYLE_ALERT)
                                                    .show();
                                        } else {
                                            File Backup = new File(PathToRecoveryBackups, FileName);
                                            if (Backup.renameTo(renamedBackup)) {
                                                loadBackups();
                                            } else {
                                                AppMsg
                                                        .makeText(mActivity, R.string.rename_failed, AppMsg.STYLE_ALERT)
                                                        .show();
                                            }

                                        }
                                        dialog.dismiss();
                                    }
                                });
                                dialog.show();
                                return true;
                            case R.id.iDeleteRecoveryBackup:
                                if (new File(PathToRecoveryBackups, text.toString()).delete()) {
                                    AppMsg.makeText(mActivity, mContext.getString(R.string.bak_deleted),
                                            AppMsg.STYLE_INFO).show();
                                    loadBackups();
                                }
                                return true;
                            case R.id.iRestoreKernel:
                                final FlashUtil RestoreKernelUtil = new FlashUtil(mShell, mContext, mDevice,
                                        new File(PathToKernelBackups, text.toString()), FlashUtil.JOB_RESTORE_KERNEL);
                                RestoreKernelUtil.setRunAtEnd(new Runnable() {
                                    @Override
                                    public void run() {
                                        FlashUtils_ERRORS.addAll(RestoreKernelUtil.getERRORS());
                                    }
                                });
                                RestoreKernelUtil.execute();
                                return true;
                            case R.id.iRenameKernel:
                                etFileName.setHint(text);
                                bGo.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {

                                        String Name;
                                        if (etFileName.getText() != null && etFileName.isEnabled()
                                                && !etFileName.getText().toString().equals("")) {
                                            Name = etFileName.getText().toString();
                                        } else {
                                            Name = String.valueOf(etFileName.getHint());
                                        }

                                        if (!Name.endsWith(mDevice.getKernelExt())) {
                                            Name = Name + mDevice.getKernelExt();
                                        }

                                        final File renamedBackup = new File(PathToKernelBackups, Name);

                                        if (renamedBackup.exists()) {
                                            AppMsg
                                                    .makeText(mActivity, R.string.backupalready, AppMsg.STYLE_ALERT)
                                                    .show();
                                        } else {
                                            File Backup = new File(PathToKernelBackups, FileName);
                                            if (Backup.renameTo(renamedBackup)) {
                                                loadBackups();
                                            } else {
                                                AppMsg
                                                        .makeText(mActivity, R.string.rename_failed, AppMsg.STYLE_ALERT)
                                                        .show();
                                            }
                                        }
                                        dialog.dismiss();

                                    }
                                });
                                dialog.show();
                                return true;
                            case R.id.iDeleteKernelBackup:

                                if (new File(PathToKernelBackups, text.toString()).delete()) {
                                    AppMsg
                                            .makeText(mActivity, mContext.getString(R.string.bak_deleted),
                                                    AppMsg.STYLE_INFO)
                                            .show();
                                    loadBackups();
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                } catch (Exception e) {
                    ERRORS.add(e.toString());
                    return false;
                }
                return false;
            }
        });
        popup.show();
    }

    public void showOverRecoveryInstructions() {
        final AlertDialog.Builder Instructions = new AlertDialog.Builder(mContext);
        Instructions
                .setTitle(R.string.info)
                .setMessage(R.string.flash_over_recovery)
                .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            mToolbox.reboot(Toolbox.REBOOT_RECOVERY);
                        } catch (Exception e) {
                            ERRORS.add(e.toString());
                            e.printStackTrace();
                        }
                    }
                })
                .setNeutralButton(R.string.instructions, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog d = new Dialog(mContext);
                        d.setTitle(R.string.instructions);
                        TextView tv = new TextView(mContext);
                        tv.setTextSize(20);
                        tv.setText(R.string.instruction);
                        d.setContentView(tv);
                        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                Instructions.show();
                            }
                        });
                        d.show();
                    }
                })
                .show();
    }

    private void showDeviceNotSupportedDialog() {
        AlertDialog.Builder DeviceNotSupported = new AlertDialog.Builder(mContext);
        DeviceNotSupported.setTitle(R.string.warning);
        DeviceNotSupported.setMessage(R.string.not_supportded);
        DeviceNotSupported.setPositiveButton(R.string.report, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                report(false);
            }
        });
        DeviceNotSupported.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                exit();
            }
        });
        if (!LastLog.exists()) {
            DeviceNotSupported.setNeutralButton(R.string.sReboot, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        mToolbox.reboot(Toolbox.REBOOT_RECOVERY);
                    } catch (Exception e) {
                        ERRORS.add(e.toString());
                        e.printStackTrace();
                    }
                }
            });
        }
        DeviceNotSupported.setCancelable(BuildConfig.DEBUG);
        DeviceNotSupported.show();
    }

    private void unpackFiles() throws IOException {
        File flash_image = new File(getFilesDir(), "flash_image");
        Common.pushFileFromRAW(mContext, flash_image, R.raw.flash_image, version_changed);
        File dump_image = new File(getFilesDir(), "dump_image");
        Common.pushFileFromRAW(mContext, dump_image, R.raw.dump_image, version_changed);
        File busybox = new File(mContext.getFilesDir(), "busybox");
        Common.pushFileFromRAW(mContext, busybox, R.raw.busybox, version_changed);
        Common.pushFileFromRAW(mContext, RecoveryCollectionFile, R.raw.recovery_sums, version_changed);
        Common.pushFileFromRAW(mContext, KernelCollectionFile, R.raw.kernel_sums, version_changed);
        File PartLayoutsZip = new File(mContext.getFilesDir(), "partlayouts.zip");
        Common.pushFileFromRAW(mContext, PartLayoutsZip, R.raw.partlayouts, version_changed);
        File loki_patch = new File(mContext.getFilesDir(), "loki_patch");
        Common.pushFileFromRAW(mContext, loki_patch, R.raw.loki_patch, version_changed);
        File loki_flash = new File(mContext.getFilesDir(), "loki_flash");
        Common.pushFileFromRAW(mContext, loki_flash, R.raw.loki_flash, version_changed);
    }

    public void loadBackups() {

        if (RecoveryBakAdapter != null) {
            if (mDevice.isRecoveryDD() || mDevice.isRecoveryMTD()) {
                if (PathToRecoveryBackups.listFiles() != null) {
                    ArrayList<File> RecoveryBakFiles = new ArrayList<File>();
                    File FileList[] = PathToRecoveryBackups.listFiles();
                    if (FileList != null) {
                        RecoveryBakFiles.addAll(Arrays.asList(FileList));
                    }
                    RecoveryBakAdapter.clear();
                    for (File backup : RecoveryBakFiles) {
                        if (!backup.isDirectory()) RecoveryBakAdapter.add(backup.getName());
                    }
                }
            }
        }

        if (KernelBakAdapter != null) {
            if (mDevice.isKernelDD() || mDevice.isKernelMTD()) {
                if (PathToKernelBackups.listFiles() != null) {
                    ArrayList<File> KernelBakList = new ArrayList<File>();
                    File FileList[] = PathToKernelBackups.listFiles();
                    if (FileList != null) {
                        KernelBakList.addAll(Arrays.asList(FileList));
                    }
                    KernelBakAdapter.clear();
                    for (File backup : KernelBakList) {
                        if (!backup.isDirectory()) KernelBakAdapter.add(backup.getName());
                    }
                }
            }
        }
    }

    public void optimizeLayout() throws NullPointerException {

        if (mDevice.isRecoverySupported() || mDevice.isKernelSupported()) {
            /** If device is supported start setting up layout */

            String action;

            if ((action = getIntent().getAction()) != null && action.equals(Intent.ACTION_VIEW)) {
                startFlashAs();
            } else {
                setContentView(R.layout.rashr);
                mRashrLayout = (DrawerLayout) findViewById(R.id.RashrLayout);

                setupSwipeUpdater();

                setupDrawer();

                final TextView RecoveryVersion = (TextView) findViewById(R.id.tvVersion);
                RecoveryVersion.setText(mDevice.getRecoveryVersion() + "\n" + mDevice.getKernelVersion());

                AdView adView = (AdView) findViewById(R.id.adView);
                ViewGroup MainParent = (ViewGroup) adView.getParent();

                if (MainParent != null) {
                    if (!Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS)) {
                        /** Removing ads if user has turned off */
                        MainParent.removeView(adView);
                    }
                }

                CardUI mFlashCards = (CardUI) findViewById(R.id.FlashCards);

                CardStack FlashStack = new CardStack();
                FlashStack.setTitle(getString(R.string.flash));

                if (mDevice.isKernelSupported()) {
                    MyImageCard KernelCard = new MyImageCard(getString(R.string.kernel), R.drawable.ic_flash,
                            getString(R.string.kernel_description));
                    KernelCard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showFlashKernelDialog();
                        }
                    });
                    FlashStack.add(KernelCard);
                }

                if (mDevice.isRecoverySupported()) {
                    MyImageCard RecoveryCard = new MyImageCard(getString(R.string.recovery), R.drawable.ic_update,
                            getString(R.string.recovery_description));
                    RecoveryCard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showFlashRecoveryDialog();
                        }
                    });
                    FlashStack.add(RecoveryCard);
                }

                mFlashCards.addStack(FlashStack, true);

                CardUI mOptionsCards = (CardUI) findViewById(R.id.Options);

                CardStack OptionsStack = new CardStack(getString(R.string.options));

                MyCard ResetCard = new MyCard(getString(R.string.reset_app),
                        getString(R.string.reset_app_description));
                ResetCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
                        editor.clear().commit();
                        editor = getSharedPreferences(FlashUtil.PREF_NAME, MODE_PRIVATE).edit();
                        editor.clear().commit();
                        editor = getSharedPreferences(Shell.PREF_NAME, MODE_PRIVATE).edit();
                        editor.clear().commit();
                        restartActivity();
                    }
                });

                OptionsStack.add(ResetCard);

                MyImageCard ClearCache = new MyImageCard(getString(R.string.sClearCache), R.drawable.ic_delete,
                        getString(R.string.clear_cache_description));
                ClearCache.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder ConfirmationDialog = new AlertDialog.Builder(mContext);
                        ConfirmationDialog.setTitle(R.string.warning);
                        ConfirmationDialog.setMessage(R.string.delete_confirmation);
                        ConfirmationDialog.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!Common.deleteFolder(PathToCWM, false)
                                    || !Common.deleteFolder(PathToTWRP, false)
                                    || !Common.deleteFolder(PathToPhilz, false)
                                    || !Common.deleteFolder(PathToStockRecovery, false)
                                    || !Common.deleteFolder(PathToStockKernel,false)) {
                                    AppMsg
                                            .makeText(mActivity, R.string.delete_failed, AppMsg.STYLE_CONFIRM)
                                            .show();
                                } else {
                                    AppMsg
                                            .makeText(mActivity, R.string.files_deleted, AppMsg.STYLE_INFO)
                                            .show();
                                }
                            }
                        });
                        ConfirmationDialog.setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        ConfirmationDialog.show();
                    }
                });
                OptionsStack.add(ClearCache);
                mOptionsCards.addStack(OptionsStack, true);

                CardUI mRebootCards = (CardUI) findViewById(R.id.Rebooter);

                CardStack RebooterStack = new CardStack(getString(R.string.sRebooter));

                MyCard Reboot = new MyCard(getString(R.string.sReboot), getString(R.string.reboot_description));
                Reboot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        final AlertDialog.Builder ConfirmationDialog = new AlertDialog.Builder(mContext);
                        ConfirmationDialog.setTitle(R.string.warning);
                        ConfirmationDialog.setMessage(R.string.reboot_confirmation);
                        ConfirmationDialog.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    mToolbox.reboot(Toolbox.REBOOT_REBOOT);
                                } catch (Exception e) {
	                                ERRORS.add(e.toString());
                                    e.printStackTrace();
                                }
                            }
                        });
                        ConfirmationDialog.setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        ConfirmationDialog.show();
                    }
                });
                MyCard RebootRecovery = new MyCard(getString(R.string.sRebootRecovery),
                        getString(R.string.reboot_recovery_description));
                RebootRecovery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder ConfirmationDialog = new AlertDialog.Builder(mContext);
                        ConfirmationDialog.setTitle(R.string.warning);
                        ConfirmationDialog.setMessage(R.string.reboot_confirmation);
                        ConfirmationDialog.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    mToolbox.reboot(Toolbox.REBOOT_RECOVERY);
                                } catch (Exception e) {
	                                ERRORS.add(e.toString());
                                    e.printStackTrace();
                                }
                            }
                        });
                        ConfirmationDialog.setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        ConfirmationDialog.show();
                    }
                });
                MyCard RebootBootloader = new MyCard(getString(R.string.sRebootBootloader),
                        getString(R.string.reboot_bootloader_description));
                RebootBootloader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder ConfirmationDialog = new AlertDialog.Builder(mContext);
                        ConfirmationDialog.setTitle(R.string.warning);
                        ConfirmationDialog.setMessage(R.string.reboot_confirmation);
                        ConfirmationDialog.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    mToolbox.reboot(Toolbox.REBOOT_BOOTLOADER);
                                } catch (Exception e) {
	                                ERRORS.add(e.toString());
                                    e.printStackTrace();
                                }
                            }
                        });
                        ConfirmationDialog.setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        ConfirmationDialog.show();
                    }
                });
                MyCard Shutdown = new MyCard(getString(R.string.sRebootShutdown),
                        getString(R.string.shutdown_description));
                Shutdown.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder ConfirmationDialog = new AlertDialog.Builder(mContext);
                        ConfirmationDialog.setTitle(R.string.warning);
                        ConfirmationDialog.setMessage(R.string.shutdown_confirmation);
                        ConfirmationDialog.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    mToolbox.reboot(Toolbox.REBOOT_SHUTDOWN);
                                } catch (Exception e) {
	                                ERRORS.add(e.toString());
                                    e.printStackTrace();
                                }
                            }
                        });
                        ConfirmationDialog.setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        ConfirmationDialog.show();
                    }
                });

                RebooterStack.add(Shutdown);
                RebooterStack.add(RebootBootloader);
                RebooterStack.add(RebootRecovery);
                RebooterStack.add(Reboot);

                mRebootCards.addStack(RebooterStack, true);
            }
        }

    }

    public void startFlashAs() throws NullPointerException {
        /** Setting layout for flashing over external App for example File Browser */
        setContentView(R.layout.flash_as);
        Uri path;
        if ((path = getIntent().getData()) != null) {
            final File IMG = new File(path.getPath());
            if (IMG.exists()) {
                TextView tvFlashAs = (TextView) findViewById(R.id.tvFlashAs);
                tvFlashAs.setText(String.format(getString(R.string.flash_as), IMG.getName()));
            } else {
                exit();
            }
            RadioButton optAsRecovery = (RadioButton) findViewById(R.id.optAsRecovery);
            RadioButton optAsKernel = (RadioButton) findViewById(R.id.optAsKernel);

	        ViewGroup parent;
            if (!mDevice.isRecoverySupported()) {
	            if ((parent = (ViewGroup) optAsRecovery.getParent()) != null) {
		            parent.removeView(optAsRecovery);
		            optAsKernel.setChecked(true);
	            }
            }
            if (!mDevice.isKernelSupported()) {
	            if ((parent = (ViewGroup) optAsKernel.getParent()) != null) {
		            parent.removeView((optAsKernel));
	            }
            }
        } else {
            exit();
        }
    }

    public void setupSwipeUpdater() {
        mSwipeUpdater = (SwipeRefreshLayout) findViewById(R.id.swipe_updater);
	    mSwipeUpdater.setColorSchemeColors(R.color.custom_green,
                R.color.custom_yellow,
                R.color.custom_green,
                android.R.color.darker_gray);
        mSwipeUpdater.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                final int img_count = mDevice.getStockRecoveryVersions().size()
                        + mDevice.getCwmRecoveryVersions().size()
                        + mDevice.getTwrpRecoveryVersions().size()
                        + mDevice.getPhilzRecoveryVersions().size()
                        + mDevice.getStockKernelVersions().size();
                Downloader RecoveryUpdater = new Downloader(mContext, RECOVERY_SUMS_URL, RecoveryCollectionFile);
                RecoveryUpdater.setOverrideFile(true);
                RecoveryUpdater.setHidden(true);

                AppMsg
                        .makeText(mActivity, R.string.refresh_list, AppMsg.STYLE_INFO)
                        .show();
                final Downloader KernelUpdater = new Downloader(mContext, KERNEL_SUMS_URL, KernelCollectionFile);
                KernelUpdater.setOverrideFile(true);
                KernelUpdater.setHidden(true);
                RecoveryUpdater.setOnDownloadListener(new Downloader.OnDownloadListener() {
                    @Override
                    public void success(File file) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mDevice.loadRecoveryList();
                                KernelUpdater.execute();
                            }
                        }).start();
                    }

                    @Override
                    public void failed(Exception e) {
	                    ERRORS.add(e.toString());
                        AppMsg
                                .makeText(mActivity, e.getMessage(), AppMsg.STYLE_ALERT)
                                .show();
                    }
                });

                KernelUpdater.setOnDownloadListener(new Downloader.OnDownloadListener() {
                    @Override
                    public void success(File file) {
                        mDevice.loadKernelList();
                        mSwipeUpdater.setRefreshing(false);
                        final int new_img_count = (mDevice.getStockRecoveryVersions().size()
                                + mDevice.getCwmRecoveryVersions().size()
                                + mDevice.getTwrpRecoveryVersions().size()
                                + mDevice.getPhilzRecoveryVersions().size()
                                + mDevice.getStockKernelVersions().size()) - img_count;
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AppMsg
                                        .makeText(mActivity, String.format(getString(R.string.new_imgs_loaded),
                                                new_img_count), new_img_count > 0 ? AppMsg.STYLE_INFO : AppMsg.STYLE_CONFIRM)
                                        .show();
                            }
                        });
                    }

                    @Override
                    public void failed(Exception e) {
	                    ERRORS.add(e.toString());
                        AppMsg
                                .makeText(mActivity, e.getMessage(), AppMsg.STYLE_ALERT)
                                .show();
                    }
                });
                RecoveryUpdater.execute();
            }
        });
    }

    public void setupDrawer() {
        LayoutInflater layoutInflater = getLayoutInflater();

        mRashrLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(mActivity,
                mRashrLayout, R.drawable.ic_navigation_drawer, R.string.settings, R.string.app_name);
        mRashrLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setupMenuDrawer(layoutInflater);
        setupBackupDrawer(layoutInflater);
    }

    public void setupMenuDrawer(LayoutInflater layoutInflater) throws NullPointerException {
        DrawerLayout mMenuDrawer =
                (DrawerLayout) layoutInflater.inflate(R.layout.menu_drawer, mRashrLayout, true);
        if (mMenuDrawer != null) {
            Spinner spStyle = (Spinner) mMenuDrawer.findViewById(R.id.spStyle);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity,
                    R.layout.custom_list_item, getResources().getStringArray(R.array.styles));
            adapter.setDropDownViewResource(R.layout.custom_list_item);

            spStyle.setAdapter(adapter);
            spStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 1:
                            Common.setIntegerPref(mContext, PREF_NAME, PREF_STYLE, APPCOMPAT_LIGHT_DARK_BAR);
                            restartActivity();
                            break;
                        case 2:
                            Common.setIntegerPref(mContext, PREF_NAME, PREF_STYLE, APPCOMPAT_LIGHT);
                            restartActivity();
                            break;
                        case 3:
                            Common.setIntegerPref(mContext, PREF_NAME, PREF_STYLE, APPCOMPAT_DARK);
                            restartActivity();
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            CheckBox cbShowAds = (CheckBox) mMenuDrawer.findViewById(R.id.cbShowAds);
            CheckBox cbLog = (CheckBox) mMenuDrawer.findViewById(R.id.cbLog);
            Button bShowLogs = (Button) mMenuDrawer.findViewById(R.id.bShowLogs);

            cbShowAds.setChecked(Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS));
            cbLog.setChecked(Common.getBooleanPref(mContext, Shell.PREF_NAME, Shell.PREF_LOG));
            bShowLogs.setVisibility(cbLog.isChecked() ? View.VISIBLE : View.INVISIBLE);
            cbShowAds.setChecked(Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS));

        }
    }

    public void setupBackupDrawer(LayoutInflater layoutInflater) throws NullPointerException {
        DrawerLayout mBackupDrawer =
                (DrawerLayout) layoutInflater.inflate(R.layout.backup_drawer, mRashrLayout, true);
        if (mBackupDrawer != null) {
            ViewGroup BackupDrawerParent = (ViewGroup) mBackupDrawer.getParent();
            if (mDevice.isRecoveryOverRecovery()) {
                if (BackupDrawerParent != null) BackupDrawerParent.removeView(mBackupDrawer);
            } else {
                View createKernelBackup = findViewById(R.id.bCreateKernelBackup);
                View kernelBackups = findViewById(R.id.lvKernelBackups);
                View createRecoveryBackup = findViewById(R.id.bCreateRecoveryBackup);
                View recoveryBackups = findViewById(R.id.lvRecoveryBackups);
                if (!mDevice.isKernelSupported()) {
                    /** If Kernel flashing is not supported remove backup views */
                    ViewGroup parent;
                    if ((parent = (ViewGroup) createKernelBackup.getParent()) != null) {
                        parent.removeView(createKernelBackup);
                    }
                    if ((parent = (ViewGroup) kernelBackups.getParent()) != null) {
                        parent.removeView(kernelBackups);
                    }
                } else {
                    KernelBakAdapter = new ArrayAdapter<String>(mContext, R.layout.custom_list_item);
                    final ListView lvKernelBackups = (ListView) mActivity.findViewById(R.id.lvKernelBackups);
                    lvKernelBackups.setAdapter(KernelBakAdapter);
                    lvKernelBackups.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                                long arg3) {
                            showPopup(R.menu.bakmgr_kernel_menu, arg1);
                        }
                    });
                }
                if (!mDevice.isRecoverySupported()) {
                    /** If Recovery flashing is not supported remove backup views */
                    ViewGroup parent;
                    if ((parent = (ViewGroup) createRecoveryBackup.getParent()) != null) {
                        parent.removeView(createRecoveryBackup);
                    }
                    if ((parent = (ViewGroup) recoveryBackups.getParent()) != null) {
                        parent.removeView(recoveryBackups);
                    }
                } else {
                    RecoveryBakAdapter = new ArrayAdapter<String>(mContext, R.layout.custom_list_item);
                    final ListView lvRecoveryBackups = (ListView) mActivity.findViewById(R.id.lvRecoveryBackups);
                    lvRecoveryBackups.setAdapter(RecoveryBakAdapter);
                    lvRecoveryBackups.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                                long arg3) {
                            showPopup(R.menu.bakmgr_recovery_menu, arg1);
                        }
                    });
                }
            }
            loadBackups();
        }
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void exit() {
        finish();
        System.exit(0);
    }

    /**
     * @return All Preferences as String
     */
    public String getAllPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String Prefs = "";
        Map<String, ?> prefsMap = prefs.getAll();
        try {
            for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
                /**
                 * Skip following Prefs (PREF_KEY_KERNEL_HISTORY, PREF_KEY_RECOVERY_HISTORY ...)
                 */
                try {
                    if (!entry.getKey().contains(PREF_KEY_KERNEL_HISTORY)
                            && !entry.getKey().contains(PREF_KEY_RECOVERY_HISTORY)
                            && !entry.getKey().contains(FlashUtil.PREF_KEY_FLASH_KERNEL_COUNTER)
                            && !entry.getKey().contains(FlashUtil.PREF_KEY_FLASH_RECOVERY_COUNTER)) {
                        Prefs += entry.getKey() + ": " + entry.getValue().toString() + "\n";
                    }
                } catch (NullPointerException e) {
                    ERRORS.add(e.toString());
                }
            }
        } catch (NullPointerException e) {
            ERRORS.add(e.toString());
        }

        return Prefs;
    }

    public void showUnifiedBuildsDialog() {

        final Dialog UnifiedBuildsDialog = new Dialog(mContext);
        UnifiedBuildsDialog.setTitle(R.string.make_choice);
        final ArrayList<String> DevName = new ArrayList<String>();
        ArrayList<String> DevNamesCarriers = new ArrayList<String>();

        UnifiedBuildsDialog.setContentView(R.layout.unified_build_dialog);
        ListView UnifiedList = (ListView) UnifiedBuildsDialog.findViewById(R.id.lvUnifiedList);
        ArrayAdapter<String> UnifiedAdapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_list_item_1, DevNamesCarriers);
        UnifiedList.setAdapter(UnifiedAdapter);

        if (mDevice.getManufacture().equals("samsung")) {
            String[] unifiedGalaxyS3 = {"d2lte", "d2att", "d2cri", "d2mtr",
                    "d2spr", "d2tmo", "d2usc", "d2vzw"};
            String[] unifiedGalaxyNote3 = {"hlte", "hltespr", "hltetmo", "hltevzw", "htlexx"};
            String[] unifiedGalaxyS4 = {"jflte", "jflteatt", "jfltecan", "jfltecri", "jfltecsp",
                    "jfltespr", "jfltetmo", "jflteusc", "jfltevzw", "jfltexx", "jgedlte"};
            if (mDevice.getName().startsWith("d2lte")) {
                DevName.addAll(Arrays.asList(unifiedGalaxyS3));
            } else if (mDevice.getName().startsWith("hlte")) {
                DevName.addAll(Arrays.asList(unifiedGalaxyNote3));
            } else if (mDevice.getName().startsWith("jflte")) {
                DevName.addAll(Arrays.asList(unifiedGalaxyS4));
            }
        }

        if (mDevice.getManufacture().equals("motorola")) {
            String[] unifiedMsm8960 = {"moto_msm8960"};
            if (mDevice.getBOARD().equals("msm8960")) {
                DevName.addAll(Arrays.asList(unifiedMsm8960));
            }
        }

        for (String i : DevName) {
            if (i.contains("att")) {
                DevNamesCarriers.add(i + " (AT&T Mobility)");
            } else if (i.contains("can")) {
                DevNamesCarriers.add(i + " (Canada)");
            } else if (i.contains("cri")) {
                DevNamesCarriers.add(i + " (Cricket Wireless)");
            } else if (i.contains("csp")) {
                DevNamesCarriers.add(i + " (C Spire Wireless)");
            } else if (i.contains("mtr")) {
                DevNamesCarriers.add(i + " (MetroPCS)");
            } else if (i.contains("spr")) {
                DevNamesCarriers.add(i + " (Sprint Corporation)");
            } else if (i.contains("tmo")) {
                DevNamesCarriers.add(i + " (T-Mobile US)");
            } else if (i.contains("usc")) {
                DevNamesCarriers.add(i + " (U.S. Cellular)");
            } else if (i.contains("vzw")) {
                DevNamesCarriers.add(i + " (Verizon Wireless)");
            } else if (i.contains("xx")) {
                DevNamesCarriers.add(i + " (International)");
            } else if (i.contains("ged")) {
                DevNamesCarriers.add(i + " (Google Play Edition)");
            } else {
                DevNamesCarriers.add(i + " (Unified)");
            }
        }

        UnifiedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                UnifiedBuildsDialog.dismiss();
                final ProgressDialog reloading = new ProgressDialog(mContext);
                reloading.setMessage(mContext.getString(R.string.reloading));
                reloading.setCancelable(false);
                reloading.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Common.setBooleanPref(mContext, PREF_NAME, PREF_KEY_SHOW_UNIFIED, false);
                        mDevice.setName(DevName.get(position));
                        mDevice.loadRecoveryList();
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                reloading.dismiss();
                                showFlashRecoveryDialog();
                            }
                        });
                    }
                }).start();

            }
        });
        Button KeepCurrent = (Button) UnifiedBuildsDialog.findViewById(R.id.bKeepCurrent);
        KeepCurrent.setText(String.format(getString(R.string.keep_current_name), mDevice.getName()));
        KeepCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.setBooleanPref(mContext, PREF_NAME, PREF_KEY_SHOW_UNIFIED, false);
                UnifiedBuildsDialog.dismiss();
                showFlashRecoveryDialog();
            }
        });
        UnifiedBuildsDialog.show();

        UnifiedBuildsDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Common.setBooleanPref(mContext, PREF_NAME, PREF_KEY_SHOW_UNIFIED, false);
                showFlashRecoveryDialog();
            }
        });
    }
}