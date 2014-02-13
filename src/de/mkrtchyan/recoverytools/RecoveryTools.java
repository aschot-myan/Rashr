package de.mkrtchyan.recoverytools;

/**
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Downloader;
import de.mkrtchyan.utils.FileChooserDialog;
import de.mkrtchyan.utils.Notifyer;

public class RecoveryTools extends ActionBarActivity {

    private static final String TAG = "Recovery-Tools";
    private final ActionBarActivity mActivity = this;
    /** Declaring setting names */
    public static final String PREF_NAME = "recovery_tools";
    private static final String PREF_STYLE = "style";
    private static final String PREF_KEY_ADS = "show_ads";
    private static final String PREF_KEY_CUR_VER = "current_version";
    private static final String PREF_KEY_FIRST_RUN = "first_run";
    public static final String PREF_KEY_RECOVERY_HISTORY = "last_recovery_history_";
    public static final String PREF_KEY_KERNEL_HISTORY = "last_kernel_history_";

    private final Context mContext = this;
    private final int APPCOMPAT_DARK = R.style.MyDark;
    private final int APPCOMPAT_LIGHT = R.style.MyLight;
    private final int APPCOMPAT_LIGHT_DARK_BAR = R.style.MyLightDarkBar;

    /** Used paths and files */
    private static final File PathToSd = Environment.getExternalStorageDirectory();
    private static final File PathToRecoveryTools = new File(PathToSd, "Recovery-Tools");
    private static final File PathToRecoveries = new File(PathToRecoveryTools, "recoveries");
    private static final File PathToKernel = new File(PathToRecoveryTools, "kernel");
    public static final File PathToCWM = new File(PathToRecoveries, "clockworkmod");
    public static final File PathToTWRP = new File(PathToRecoveries, "twrp");
    public static final File PathToPhilz = new File(PathToRecoveries, "philz");
    public static final File PathToRecoveryBackups = new File(PathToRecoveryTools, "recovery-backups");
    public static final File PathToKernelBackups = new File(PathToRecoveryTools, "kernel-backups");
    public static final File PathToUtils = new File(PathToRecoveryTools, "utils");
    private File RecoveryCollection;
    public static final File LastLog = new File("/cache/recovery/last_log");
    private File fRECOVERY, fKERNEL;

    /** Declaring needed objects */
    private final Notifyer mNotifyer = new Notifyer(mContext);
    private Shell mShell;
    private Device mDevice;
    private DrawerLayout mDrawerLayout = null;
    private FileChooserDialog fcFlashOtherRecovery = null, fcFlashOtherKernel = null;
    private boolean keepAppOpen = true;
    private boolean version_changed = false;

    private final Runnable ReloadBackupDrawer = new Runnable() {
        @Override
        public void run() {
            loadBackupDrawer();
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** Creating needed folder and unpacking files */
        RecoveryCollection = new File(getFilesDir(), "IMG_SUMS");
        PathToRecoveryTools.mkdir();
        PathToRecoveries.mkdir();
        PathToKernel.mkdir();
        PathToCWM.mkdir();
        PathToTWRP.mkdir();
        PathToPhilz.mkdir();
        PathToRecoveryBackups.mkdir();
        PathToKernelBackups.mkdir();
        PathToUtils.mkdir();
        unpackFiles();

        try {
            try {
                /** Try to get root access */
                mShell = Shell.startRootShell();
            } catch (IOException e) {
                /** Try operation with normal shell */
                mShell = Shell.startShell();
                Notifyer.showRootDeniedDialog(mContext);
            }
            try {
                File LogCopy = new File(mContext.getFilesDir(), LastLog.getName() + ".txt");
                Common.chmod(mShell, LastLog, "644");
                mShell.execCommand("cp " + LastLog.getAbsolutePath() + " " + LogCopy.getAbsolutePath());
            } catch (FailedExecuteCommand failedExecuteCommand) {
                failedExecuteCommand.printStackTrace();
            }

            mDevice = new Device(mContext);
            new File(PathToUtils, mDevice.DEV_NAME).mkdir();

            if (getIntent().getData() != null) {
                handleIntent();
            } else {
                /** If device is not supported, you can report it now or close the App */
                if (!mDevice.isRecoverySupported() && !mDevice.isKernelSupported()) {
                    showDeviceNotSupportedDialog();
                } else {
                    if (!Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_FIRST_RUN)) {
                        /** Setting first start configuration */
                        Common.setIntegerPref(mContext, PREF_NAME, PREF_STYLE, APPCOMPAT_LIGHT_DARK_BAR);
                        Common.setBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS, true);
                        Common.setBooleanPref(mContext, Shell.PREF_NAME, Shell.PREF_LOG, true);
                        showUsageWarning();
                    } else {
                        /** Checking if version has changed */
                        try {
                            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                            final int previous_version = Common.getIntegerPref(mContext, PREF_NAME, PREF_KEY_CUR_VER);
                            final int current_version = pInfo.versionCode;
                            version_changed = current_version > previous_version;
                            Common.setIntegerPref(mContext, PREF_NAME, PREF_KEY_CUR_VER, current_version);
                        } catch (PackageManager.NameNotFoundException e) {
                            version_changed = true;
                        } catch (NullPointerException e) {
                            version_changed = true;
                        }
                        if (version_changed) {
                            Notifyer.showAppRateDialog(mContext);
                            showChangelog();
                        }
                    }
                }

                this.setTheme(Common.getIntegerPref(mContext, PREF_NAME, PREF_STYLE));

                optimizeLayout();
                mDevice.downloadUtils(mContext);
            }

//        if (mDevice.isMTK()) {
//            Toast.makeText(mContext, "YES!! MTK RECOGNIZED!!", Toast.LENGTH_LONG).show();
//        }

        } catch (IOException e) {
            Notifyer.showExceptionToast(mContext, TAG, e);
        }
    }

    /** View Methods (onClick) */
    public void bFlashRecovery(View view) {
        showFlashRecoveryDialog();
    }

    public void bFlashKernel(View view) {
        showFlashKernelDialog();
    }


    public void bFlashOtherKernel(View view) {
        fKERNEL = null;
        try {
            fcFlashOtherKernel = new FileChooserDialog(view.getContext(), PathToSd, new Runnable() {
                @Override
                public void run() {
                    fKERNEL = fcFlashOtherKernel.getSelectedFile();
                    rKernelFlasher.run();
                }
            });
            fcFlashOtherKernel.setTitle(R.string.pick_file);
            String AllowedEXT[] = {mDevice.KERNEL_EXT};
            fcFlashOtherKernel.setAllowedEXT(AllowedEXT);
            fcFlashOtherKernel.setBrowseUpEnabled(true);
            fcFlashOtherKernel.setWarn(true);
            fcFlashOtherKernel.show();
        } catch (NullPointerException e) {
            Notifyer.showExceptionToast(mContext, TAG, e);
        }
    }

    public void Go(View view) {
        fRECOVERY = null;
        ArrayList<String> Versions;
        if (!mDevice.downloadUtils(mContext)) {
            /**
             * If there files be needed to flash download it and listing
             * device specified recovery file for example recovery-clockwork-touch-6.0.3.1-grouper.img
             * (read out from IMG_SUMS)
             */
            final String SYSTEM = view.getTag().toString();
            if (SYSTEM.equals("clockwork")) {
                Versions = mDevice.CwmArrayList;
            } else if (SYSTEM.equals("twrp")) {
                Versions = mDevice.TwrpArrayList;
            } else if (SYSTEM.equals("philz")) {
                Versions = mDevice.PhilzArrayList;
            } else {
                return;
            }

            final Dialog recoveries = new Dialog(mContext);
            recoveries.setTitle(SYSTEM);
            ListView lv = new ListView(mContext);
            recoveries.setContentView(lv);
            lv.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, Versions));
            recoveries.show();
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    recoveries.dismiss();

                    final String fileName = ((TextView) view).getText().toString();

                    if (fileName != null) {
                        if (SYSTEM.equals("clockwork")) {
                            fRECOVERY = new File(PathToCWM, fileName);
                        } else if (SYSTEM.equals("twrp")) {
                            fRECOVERY = new File(PathToTWRP, fileName);
                        } else if (SYSTEM.equals("philz")) {
                            fRECOVERY = new File(PathToPhilz, fileName);
                        }

                        if (!fRECOVERY.exists()) {
                            Downloader RecoveryDownloader = new Downloader(mContext, Device.RECOVERY_HOST_URL, fRECOVERY.getName(), fRECOVERY, rRecoveryFlasher);
                            RecoveryDownloader.setRetry(true);
                            RecoveryDownloader.setAskBeforeDownload(true);
                            RecoveryDownloader.setChecksumFile(RecoveryCollection);
                            RecoveryDownloader.ask();
                        } else {
                            rRecoveryFlasher.run();
                        }
                    }
                }
            });
        }
    }

    public void bDonate(View view) {
        startActivity(new Intent(view.getContext(), DonationsActivity.class));
    }

    public void bXDA(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://forum.xda-developers.com/showthread.php?t=2334554")));
    }

    public void bExit(View view) {
        finish();
        System.exit(0);
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

    public void bFlashOtherRecovery(View view) {
        fRECOVERY = null;
        try {
            fcFlashOtherRecovery = new FileChooserDialog(view.getContext(), PathToSd, new Runnable() {
                @Override
                public void run() {
                    fRECOVERY = fcFlashOtherRecovery.getSelectedFile();
                    rRecoveryFlasher.run();
                }
            });
            fcFlashOtherRecovery.setTitle(R.string.pick_file);
            String AllowedEXT[] = {mDevice.RECOVERY_EXT};
            fcFlashOtherRecovery.setAllowedEXT(AllowedEXT);
            fcFlashOtherRecovery.setBrowseUpEnabled(true);
            fcFlashOtherRecovery.show();
        } catch (NullPointerException e) {
            Notifyer.showExceptionToast(mContext, TAG, e);
        }
    }

    public void bShowRecoveryHistory(View view) {
        final ArrayList<File> HistoryFiles = new ArrayList<File>();
        final ArrayList<String> HistoryFileNames = new ArrayList<String>();
        final Dialog HistoryDialog = new Dialog(mContext);
        HistoryDialog.setTitle(R.string.sHistory);
        ListView HistoryList = new ListView(mContext);
        File tmp;
        for (int i = 0; i < 5; i++) {
            tmp = new File(Common.getStringPref(mContext, PREF_NAME, PREF_KEY_RECOVERY_HISTORY + String.valueOf(i)));
            if (!tmp.exists())
                Common.setStringPref(mContext, PREF_NAME, PREF_KEY_RECOVERY_HISTORY + String.valueOf(i), "");
            else {
                HistoryFiles.add(tmp);
                HistoryFileNames.add(tmp.getName());
            }
        }
        HistoryList.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, HistoryFileNames));
        HistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                if (HistoryFiles.get(arg2).exists()) {
                    fRECOVERY = HistoryFiles.get(arg2);
                    rRecoveryFlasher.run();
                    HistoryDialog.dismiss();
                } else {
                    Toast.makeText(mContext, R.string.no_choosed, Toast.LENGTH_SHORT).show();
                }
            }
        });
        HistoryDialog.setContentView(HistoryList);
        if (HistoryFileNames.toArray().length > 0) {
            HistoryDialog.show();
        } else {
            Toast
                    .makeText(mContext, R.string.no_history, Toast.LENGTH_SHORT)
                    .show();
        }

    }

    public void bShowKernelHistory(View view) {
        final ArrayList<File> HistoryFiles = new ArrayList<File>();
        final ArrayList<String> HistoryFileNames = new ArrayList<String>();
        final Dialog HistoryDialog = new Dialog(mContext);
        HistoryDialog.setTitle(R.string.sHistory);
        ListView HistoryList = new ListView(mContext);
        File tmp;
        for (int i = 0; i < 5; i++) {
            tmp = new File(Common.getStringPref(mContext, PREF_NAME, PREF_KEY_KERNEL_HISTORY + String.valueOf(i)));
            if (!tmp.exists())
                Common.setStringPref(mContext, PREF_NAME, PREF_KEY_KERNEL_HISTORY + String.valueOf(i), "");
            else {
                HistoryFiles.add(tmp);
                HistoryFileNames.add(tmp.getName());
            }
        }
        HistoryList.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, HistoryFileNames));
        HistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                if (HistoryFiles.get(arg2).exists()) {
                    fKERNEL = HistoryFiles.get(arg2);
                    rKernelFlasher.run();
                    HistoryDialog.dismiss();
                } else {
                    Toast.makeText(mContext, R.string.no_choosed, Toast.LENGTH_SHORT).show();
                }
            }
        });
        HistoryDialog.setContentView(HistoryList);
        if (HistoryFileNames.toArray().length > 0) {
            HistoryDialog.show();
        } else {
            Toast
                    .makeText(mContext, R.string.no_history, Toast.LENGTH_SHORT)
                    .show();
        }

    }

    public void bCreateRecoveryBackup(View view) {

        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle(R.string.setname);
        dialog.setContentView(R.layout.dialog_input);
        final Button bGoBackup = (Button) dialog.findViewById(R.id.bGoBackup);
        final EditText etFileName = (EditText) dialog.findViewById(R.id.etFileName);
        String NameHint = "recovery-from-" + Calendar.getInstance().get(Calendar.DATE)
                + "-" + Calendar.getInstance().get(Calendar.MONTH)
                + "-" + Calendar.getInstance().get(Calendar.YEAR)
                + "-" + Calendar.getInstance().get(Calendar.HOUR)
                + ":" + Calendar.getInstance().get(Calendar.MINUTE) + mDevice.RECOVERY_EXT;
        etFileName.setHint(NameHint);
        bGoBackup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String Name = "";

                if (etFileName.getText() != null && !etFileName.getText().toString().equals("")) {
                    Name = etFileName.getText().toString();
                }

                if (Name.equals("")) {
                    Name = String.valueOf(etFileName.getHint());
                }

                if (!Name.endsWith(mDevice.RECOVERY_EXT)) {
                    Name = Name + mDevice.RECOVERY_EXT;
                }

                final File fBACKUP = new File(RecoveryTools.PathToRecoveryBackups, Name);
                Runnable rBackup = new Runnable() {
                    @Override
                    public void run() {
                        FlashUtil BackupCreator = new FlashUtil(mShell, mContext, mDevice, fBACKUP,
                                FlashUtil.JOB_BACKUP_RECOVERY);
                        BackupCreator.setRunAtEnd(ReloadBackupDrawer);
                        BackupCreator.execute();
                    }
                };
                if (fBACKUP.exists()) {
                    Toast
                            .makeText(mContext, R.string.backupalready, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    rBackup.run();
                }
                dialog.dismiss();

            }
        });
        dialog.show();
    }

    public void bCreateKernelBackup(View view) {

        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle(R.string.setname);
        dialog.setContentView(R.layout.dialog_input);
        final Button bGoBackup = (Button) dialog.findViewById(R.id.bGoBackup);
        final EditText etFileName = (EditText) dialog.findViewById(R.id.etFileName);
        String NameHint = "kernel-from-" + Calendar.getInstance().get(Calendar.DATE)
                + "-" + Calendar.getInstance().get(Calendar.MONTH)
                + "-" + Calendar.getInstance().get(Calendar.YEAR)
                + "-" + Calendar.getInstance().get(Calendar.HOUR)
                + ":" + Calendar.getInstance().get(Calendar.MINUTE) + mDevice.KERNEL_EXT;
        etFileName.setHint(NameHint);
        bGoBackup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String Name = "";

                if (etFileName.getText() != null && !etFileName.getText().toString().equals("")) {
                    Name = etFileName.getText().toString();
                }

                if (Name.equals("")) {
                    Name = String.valueOf(etFileName.getHint());
                }

                if (!Name.endsWith(mDevice.KERNEL_EXT)) {
                    Name = Name + mDevice.KERNEL_EXT;
                }

                final File fBACKUP = new File(RecoveryTools.PathToKernelBackups, Name);
                Runnable rBackup = new Runnable() {
                    @Override
                    public void run() {
                        FlashUtil BackupCreator = new FlashUtil(mShell, mContext, mDevice, fBACKUP,
                                FlashUtil.JOB_BACKUP_KERNEL);
                        BackupCreator.setRunAtEnd(ReloadBackupDrawer);
                        BackupCreator.execute();
                    }
                };
                if (fBACKUP.exists()) {
                    Toast
                            .makeText(mContext, R.string.backupalready, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    rBackup.run();
                }
                dialog.dismiss();

            }
        });
        dialog.show();
    }

    public void bClearCache(View view) {
        Common.deleteFolder(PathToCWM, false);
        Common.deleteFolder(PathToTWRP, false);
        Common.deleteFolder(PathToPhilz, false);
    }

    public void bRebooter(View view) {
        showPopup(R.menu.rebooter_menu, view);
    }

    private void showFlashRecoveryDialog() {
        final Dialog FlashRecoveryDialog = new Dialog(mContext);
        LayoutInflater inflater = getLayoutInflater();
        FlashRecoveryDialog.setTitle(R.string.flash_options);
        try {
            final ScrollView RecoveryLayout = (ScrollView) inflater.inflate(R.layout.recovery_dialog, null);
            LinearLayout layout = (LinearLayout) RecoveryLayout.getChildAt(0);

            FlashRecoveryDialog.setContentView(RecoveryLayout);
            if (!mDevice.isCwmSupported()) {
                layout.removeView(layout.findViewById(R.id.bCWM));
            }
            if (!mDevice.isTwrpSupported()) {
                layout.removeView(layout.findViewById(R.id.bTWRP));
            }
            if (!mDevice.isPhilzSupported()) {
                layout.removeView(layout.findViewById(R.id.bPHILZ));
            }

            FlashRecoveryDialog.show();
        } catch (NullPointerException e) {
            Notifyer.showExceptionToast(mContext, TAG, e);
        }
    }

    private void showFlashKernelDialog() {
        final AlertDialog.Builder FlashKernelDialog = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = getLayoutInflater();
        FlashKernelDialog.setTitle(R.string.flash_options);
        FlashKernelDialog.setView(inflater.inflate(R.layout.kernel_dialog, null));
        FlashKernelDialog.show();
    }

    public void report(final boolean isCancelable) {
        final Dialog reportDialog = mNotifyer.createDialog(R.string.commentar, R.layout.dialog_comment, false, true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                /** Creates a report Email including a Comment and important device infos */
                final Button bGo = (Button) reportDialog.findViewById(R.id.bGo);
                bGo.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (!Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS))
                            Toast.makeText(mContext, R.string.please_ads, Toast.LENGTH_SHORT).show();
                        Toast.makeText(mContext, R.string.donate_to_support, Toast.LENGTH_SHORT).show();
                        try {
                            ArrayList<File> files = new ArrayList<File>();
                            File TestResults = new File(mContext.getFilesDir(), "results.txt");
                            try {
                                if (TestResults.exists())
                                    TestResults.delete();
                                FileOutputStream fos = openFileOutput(TestResults.getName(), Context.MODE_PRIVATE);
                                fos.write(("Recovery-Tools:\n\n" + mShell.execCommand("ls -lR " + PathToRecoveryTools.getAbsolutePath()) +
                                        "\nCache Tree:\n" + mShell.execCommand("ls -lR /cache") + "\n" +
                                        "\nMTD result:\n" + mShell.execCommand("cat /proc/mtd") + "\n" +
                                        "\nDevice Tree:\n\n" + mShell.execCommand("ls -lR /dev")).getBytes());
                                files.add(TestResults);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            if (getPackageManager() != null) {
                                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                EditText text = (EditText) reportDialog.findViewById(R.id.etCommentar);
                                String comment = "";
                                if (text.getText() != null)
                                    comment = text.getText().toString();
                                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ashotmkrtchyan1995@gmail.com"});
                                intent.putExtra(Intent.EXTRA_SUBJECT, "Recovery-Tools report");
                                intent.putExtra(Intent.EXTRA_TEXT, "Package Infos:" +
                                        "\n\nName: " + pInfo.packageName +
                                        "\nVersionName: " + pInfo.versionName +
                                        "\nVersionCode: " + pInfo.versionCode +
                                        "\n\n\nProduct Info: " +
                                        "\n\nManufacture: " + android.os.Build.MANUFACTURER +
                                        "\nDevice: " + Build.DEVICE + " (" + mDevice.DEV_NAME + ")" +
                                        "\nBoard: " + Build.BOARD +
                                        "\nBrand: " + Build.BRAND +
                                        "\nModel: " + Build.MODEL +
                                        "\nFingerprint: " + Build.FINGERPRINT +
                                        "\nAndroid SDK Level: " + Build.VERSION.CODENAME + " (" + Build.VERSION.SDK_INT + ")" +
                                        "\nRecovery Supported: " + mDevice.isRecoverySupported() +
                                        "\nRecovery Path: " + mDevice.RecoveryPath +
                                        "\nRecovery MTD: " + mDevice.isRecoveryMTD() +
                                        "\nRecovery DD: " + mDevice.isRecoveryDD() +
                                        "\nKernel Supported: " + mDevice.isKernelSupported() +
                                        "\nKernel Path: " + mDevice.KernelPath +
                                        "\nKernel MTD: " + mDevice.isKernelMTD() +
                                        "\nKernel DD: " + mDevice.isKernelDD() +
                                        "\n\nCWM: " + mDevice.isCwmSupported() +
                                        "\nTWRP: " + mDevice.isTwrpSupported() +
                                        "\nPHILZ: " + mDevice.isPhilzSupported() +
                                        "\n\n\n===========Comment==========\n" + comment +
                                        "\n===========Comment==========\n");
                                File CommandLogs = new File(mContext.getFilesDir(), Shell.Logs);
                                if (CommandLogs.exists()) {
                                    files.add(CommandLogs);
                                }
                                files.add(new File(getFilesDir(), "last_log.txt"));
                                ArrayList<Uri> uris = new ArrayList<Uri>();
                                for (File file : files) {
                                    mShell.execCommand("cp " + file.getAbsolutePath() + " " + new File(mContext.getFilesDir(), file.getName()).getAbsolutePath());
                                    file = new File(mContext.getFilesDir(), file.getName());
                                    Common.chmod(mShell, file, "644");
                                    uris.add(Uri.fromFile(file));
                                }
                                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                                startActivity(Intent.createChooser(intent, "Send over Gmail"));
                                reportDialog.dismiss();
                            }
                        } catch (Exception e) {
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
                if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    mDrawerLayout.openDrawer(Gravity.LEFT);

                }
                break;
            case R.id.update:
                Downloader updater = new Downloader(mContext, "http://dslnexus.org/Android/",
                        "img_sums", RecoveryCollection, new Runnable() {
                    @Override
                    public void run() {
                        Toast
                                .makeText(mContext, R.string.please_restart, Toast.LENGTH_SHORT)
                                .show();
                    }
                });
                updater.setOverrideFile(true);
                updater.setRetry(true);
                updater.execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChangelog() {
        if (version_changed) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setTitle(R.string.changelog);
            WebView changes = new WebView(mContext);
            changes.setWebViewClient(new WebViewClient());
            changes.loadUrl("http://forum.xda-developers.com/showpost.php?p=42839595&postcount=3");
            dialog.setView(changes);
            dialog.show();
        }
    }

    private void showUsageWarning() {
        if (mDevice.isRecoveryOverRecovery() || mDevice.isKernelSupported()) {
            final AlertDialog.Builder WarningDialog = new AlertDialog.Builder(mContext);
            WarningDialog.setTitle(R.string.warning);
            WarningDialog.setMessage(String.format(getString(R.string.bak_warning),
                    PathToRecoveryBackups.getAbsolutePath() +  " & " +
                            PathToKernelBackups.getAbsolutePath()));
            WarningDialog.setPositiveButton(R.string.sRecoveryBackup, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    bCreateRecoveryBackup(null);
                    bCreateKernelBackup(null);
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
                    final String text = ((TextView) v).getText().toString();

                    final Dialog dialog = new Dialog(mContext);
                    dialog.setTitle(R.string.setname);
                    dialog.setContentView(R.layout.dialog_input);
                    final Button bGo = (Button) dialog.findViewById(R.id.bGoBackup);
                    final EditText etFileName = (EditText) dialog.findViewById(R.id.etFileName);
                    final File Backup = new File(PathToRecoveryBackups, text);

                    switch (menuItem.getItemId()) {
                        case R.id.iReboot:
                            mShell.execCommand(mContext, "reboot");
                            return true;
                        case R.id.iRebootRecovery:
                            mShell.execCommand(mContext, "reboot recovery");
                            return true;
                        case R.id.iRebootBootloader:
                            mShell.execCommand(mContext,"reboot bootloader");
                            return true;
                        case R.id.iRestoreRecovery:
                            new FlashUtil(mShell, mContext, mDevice, new File(PathToRecoveryBackups,
                                    text), FlashUtil.JOB_RESTORE_RECOVERY).execute();
                            return true;
                        case R.id.iRenameRecovery:
                            etFileName.setHint(text);
                            bGo.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {

                                    String Name;
                                    if (etFileName.getText() != null
                                            && !etFileName.getText().toString().equals("")) {
                                        Name = etFileName.getText().toString();
                                    } else {
                                        Name = String.valueOf(etFileName.getHint());
                                    }

                                    if (!Name.endsWith(mDevice.RECOVERY_EXT)) {
                                        Name = Name + mDevice.RECOVERY_EXT;
                                    }

                                    final File newBackup = new File(RecoveryTools.PathToRecoveryBackups, Name);

                                    if (newBackup.exists()) {
                                        Toast
                                                .makeText(mContext, R.string.backupalready, Toast.LENGTH_SHORT)
                                                .show();
                                    } else {
                                        Backup.renameTo(newBackup);
                                    }
                                    dialog.dismiss();
                                    loadBackupDrawer();
                                }
                            });
                            dialog.show();
                            return true;
                        case R.id.iDeleteRecoveryBackup:
                            if (((TextView) v).getText() != null) {
                                new File(PathToRecoveryBackups, text).delete();
                                Toast.makeText(mContext, String.format(mContext.getString(R.string.bak_deleted),
                                        ((TextView) v).getText()), Toast.LENGTH_SHORT).show();
                                loadBackupDrawer();
                            }
                            return true;
                        case R.id.iRestoreKernel:
                            new FlashUtil(mShell, mContext, mDevice, new File(PathToKernelBackups, text),
                                    FlashUtil.JOB_RESTORE_KERNEL).execute();
                            return true;
                        case R.id.iRenameKernel:
                            etFileName.setHint(text);
                            bGo.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {

                                    String Name;
                                    if (etFileName.getText() != null && !etFileName.getText().toString().equals("")) {
                                        Name = etFileName.getText().toString();
                                    } else {
                                        Name = String.valueOf(etFileName.getHint());
                                    }

                                    if (!Name.endsWith(mDevice.KERNEL_EXT)) {
                                        Name = Name + mDevice.KERNEL_EXT;
                                    }

                                    final File newBackup = new File(RecoveryTools.PathToKernelBackups, Name);

                                    if (newBackup.exists()) {
                                        Toast
                                                .makeText(mContext, R.string.backupalready, Toast.LENGTH_SHORT)
                                                .show();
                                    } else {
                                        Backup.renameTo(newBackup);
                                    }
                                    dialog.dismiss();
                                    loadBackupDrawer();

                                }
                            });
                            dialog.show();
                            return true;
                        case R.id.iDeleteKernelBackup:
                            if (((TextView) v).getText() != null) {
                                new File(PathToKernelBackups, text).delete();
                                Toast.makeText(mContext, String.format(mContext.getString(R.string.bak_deleted),
                                        ((TextView) v).getText()), Toast.LENGTH_SHORT).show();
                                loadBackupDrawer();
                            }
                            return true;
                        default:
                            return false;
                    }
                } catch (FailedExecuteCommand e) {
                    Notifyer.showExceptionToast(mContext, TAG, e);
                    return false;
                } catch (NullPointerException e) {
                    Notifyer.showExceptionToast(mContext, TAG, e);
                    return false;
                }
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
                        bRebooter(findViewById(R.id.bRebooter));
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

    public void showFlashAlertDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.warning)
                .setMessage(String.format(mContext.getString(R.string.choose_message), fRECOVERY.getName()))
                .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        rRecoveryFlasher.run();
                    }
                })
                .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                        System.exit(0);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {

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
                finish();
                System.exit(0);
            }
        });
        if (!LastLog.exists()) {
            DeviceNotSupported.setNeutralButton(R.string.sReboot, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        mShell.execCommand(mContext, "reboot recovery");
                    } catch (FailedExecuteCommand e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        DeviceNotSupported.setCancelable(BuildConfig.DEBUG);
        DeviceNotSupported.show();
    }

    public void handleIntent() {
//      Handle Files chosen by FileBrowsers
        if (getIntent().getData() != null) {
            setTheme(R.style.Theme_AppCompat_CompactMenu_Dialog);
            keepAppOpen = false;
            fRECOVERY = new File(getIntent().getData().getPath());
            getIntent().setData(null);
            showFlashAlertDialog();
            this.setTheme(R.style.Theme_AppCompat_Base_CompactMenu_Dialog);
        }
    }

    //	"Methods" need a input from user (AlertDialog) or at the end of AsyncTasks
    private final Runnable rFlashRecovery = new Runnable() {
        @Override
        public void run() {
            FlashUtil flashUtil = new FlashUtil(mShell, mContext, mDevice, fRECOVERY,
                    FlashUtil.JOB_FLASH_RECOVERY);
            flashUtil.setKeepAppOpen(keepAppOpen);
            flashUtil.execute();
        }
    };
    private final Runnable rFlashKernel = new Runnable() {
        @Override
        public void run() {
            FlashUtil flashUtil = new FlashUtil(mShell, mContext, mDevice, fKERNEL,
                    FlashUtil.JOB_FLASH_KERNEL);
            flashUtil.setKeepAppOpen(keepAppOpen);
            flashUtil.execute();
        }
    };
    private final Runnable rRecoveryFlasher = new Runnable() {
        @Override
        public void run() {
           if (fRECOVERY != null) {
               if (fRECOVERY.exists()) {
                   if (fRECOVERY.getName().endsWith(mDevice.RECOVERY_EXT)) {
                       if (!mDevice.isKernelFlashed() && !mDevice.isRecoveryOverRecovery()) {
                           /** Flash not need to be handled specially */
                           rFlashRecovery.run();
                       } else {
                           /** Flashing needs to be handled specially (not standard flash method)*/
                           if (mDevice.isKernelFlashed()) {
                               /** Get user input if Kernel will be modified */
                               mNotifyer.createAlertDialog(R.string.warning, R.string.kernel_to, rFlashRecovery).show();
                           }
                           if (mDevice.isRecoveryOverRecovery()) {
                               /** Get user input if user want to install over recovery now */
                               showOverRecoveryInstructions();
                           }
                       }
                   }
               }
           }
            fcFlashOtherRecovery = null;
        }
    };

    private final Runnable rKernelFlasher = new Runnable() {
        @Override
        public void run() {
            if (fKERNEL != null) {
                if (fKERNEL.exists()) {
                    if (fKERNEL.getName().endsWith(mDevice.KERNEL_EXT)) {
                        rFlashKernel.run();
                    }
                }
            }
            fcFlashOtherKernel = null;
        }
    };

    private void unpackFiles() {
        try {
            File flash_image = new File(getFilesDir(), "flash_image");
            Common.pushFileFromRAW(mContext, flash_image, R.raw.flash_image, version_changed);
            File dump_image = new File(getFilesDir(), "dump_image");
            Common.pushFileFromRAW(mContext, dump_image, R.raw.dump_image, version_changed);
            File busybox = new File(mContext.getFilesDir(), "busybox");
            Common.pushFileFromRAW(mContext, busybox, R.raw.busybox, version_changed);
            Common.pushFileFromRAW(mContext, RecoveryCollection, R.raw.img_sums, version_changed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadBackupDrawer() {
        if (mDevice.isRecoveryDD() || mDevice.isRecoveryMTD()) {
            if (PathToRecoveryBackups.listFiles() != null) {
                ArrayList<File> RecoveryBakList = new ArrayList<File>();
                File FileList[] = PathToRecoveryBackups.listFiles();
                if (FileList != null) {
                    RecoveryBakList.addAll(Arrays.asList(FileList));
                }
                String[] tmp = new String[RecoveryBakList.toArray(new File[RecoveryBakList.size()]).length];
                for (int i = 0; i < tmp.length; i++) {
                    tmp[i] = RecoveryBakList.get(i).getName();
                }

                final ListView lvRecoveryBackups = (ListView) mActivity.findViewById(R.id.lvRecoveryBackups);
                lvRecoveryBackups.setAdapter(new ArrayAdapter<String>(mContext, R.layout.custom_list_item, tmp));
                lvRecoveryBackups.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                            long arg3) {
                        showPopup(R.menu.bakmgr_recovery_menu, arg1);
                    }
                });
            }
        }
        if (mDevice.isKernelDD() || mDevice.isKernelMTD()) {
            if (PathToKernelBackups.listFiles() != null) {
                ArrayList<File> KernelBakList = new ArrayList<File>();
                File FileList[] = PathToKernelBackups.listFiles();
                if (FileList != null) {
                    KernelBakList.addAll(Arrays.asList(FileList));
                }
                String[] tmp = new String[KernelBakList.toArray(new File[KernelBakList.size()]).length];
                for (int i = 0; i < tmp.length; i++) {
                    tmp[i] = KernelBakList.get(i).getName();
                }

                final ListView lvKernelBackups = (ListView) mActivity.findViewById(R.id.lvKernelBackups);
                lvKernelBackups.setAdapter(new ArrayAdapter<String>(mContext, R.layout.custom_list_item, tmp));
                lvKernelBackups.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                            long arg3) {
                        showPopup(R.menu.bakmgr_kernel_menu, arg1);
                    }
                });
            }
        }
    }

    public void optimizeLayout() throws NullPointerException {

        try {
            if (mDevice.isRecoverySupported() || mDevice.isKernelSupported()) {
                /** If device is supported start setting up layout */
                setContentView(R.layout.recovery_tools);

                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                LayoutInflater layoutInflater = getLayoutInflater();

                DrawerLayout mMenuDrawer =
                        (DrawerLayout) layoutInflater.inflate(R.layout.menu_drawer, mDrawerLayout, true);
                DrawerLayout mBackupDrawer =
                        (DrawerLayout) layoutInflater.inflate(R.layout.backup_drawer, mDrawerLayout, true);
                mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
                ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(mActivity,
                        mDrawerLayout, R.drawable.ic_drawer, R.string.settings, R.string.app_name);
                mDrawerLayout.setDrawerListener(mDrawerToggle);
                mDrawerToggle.syncState();
                mDrawerToggle.setDrawerIndicatorEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);

                String Styles[] = getResources().getStringArray(R.array.styles);
                if (mMenuDrawer != null) {
                    Spinner spStyle = (Spinner) mMenuDrawer.findViewById(R.id.spStyle);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity,
                            R.layout.custom_list_item, Styles);
                    adapter.setDropDownViewResource(R.layout.custom_list_item);

                    spStyle.setAdapter(adapter);
                    spStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            switch (position) {
                                case 0:
                                    return;
                                case 1:
                                    Common.setIntegerPref(mContext, PREF_NAME, PREF_STYLE, APPCOMPAT_LIGHT_DARK_BAR);
                                    break;
                                case 2:
                                    Common.setIntegerPref(mContext, PREF_NAME, PREF_STYLE, APPCOMPAT_LIGHT);
                                    break;
                                case 3:
                                    Common.setIntegerPref(mContext, PREF_NAME, PREF_STYLE, APPCOMPAT_DARK);
                                    break;
                                default:
                                    Toast
                                            .makeText(mContext, R.string.please_restart, Toast.LENGTH_SHORT)
                                            .show();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                    CheckBox cbShowAds = (CheckBox) mMenuDrawer.findViewById(R.id.cbShowAds);
                    CheckBox cbLog = (CheckBox) mMenuDrawer.findViewById(R.id.cbLog);

                    cbShowAds.setChecked(Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS));
                    cbLog.setChecked(Common.getBooleanPref(mContext, Shell.PREF_NAME, Shell.PREF_LOG));
                    if (cbLog.isChecked()) {
                        findViewById(R.id.bShowLogs).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.bShowLogs).setVisibility(View.INVISIBLE);
                    }
                    cbShowAds.setChecked(Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS));

                }

                final TextView RecoveryVersion = (TextView) findViewById(R.id.tvRecoveryVersion);
                RecoveryVersion.setText(mDevice.CurrentRecoveryVersion);
                loadBackupDrawer();

                if (!Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS)) {
                    mDrawerLayout.removeView(mDrawerLayout.findViewById(R.id.adView));
                }
                if (mDevice.isRecoveryOverRecovery() && mBackupDrawer != null) {
                    mDrawerLayout.removeView(mBackupDrawer);
                }

                if (!mDevice.isKernelSupported()) {
                    /** If Kernel flashing is not supported remove flash and backup options */
                    mDrawerLayout.removeView((mDrawerLayout.findViewById(R.id.bFlashKernel)));
                    if (mBackupDrawer != null) {
                        mBackupDrawer.removeView(mBackupDrawer.findViewById(R.id.bCreateKernelBackup));
                        mBackupDrawer.removeView(mBackupDrawer.findViewById(R.id.lvKernelBackups));
                    }
                }
                if (!mDevice.isRecoverySupported()) {
                    /** If Kernel flashing is not supported remove flash and backup options */
                    mDrawerLayout.removeView((mDrawerLayout.findViewById(R.id.bFlashRecovery)));
                    if (mBackupDrawer != null) {
                        mBackupDrawer.removeView(mBackupDrawer.findViewById(R.id.bCreateRecoveryBackup));
                        mBackupDrawer.removeView(mBackupDrawer.findViewById(R.id.lvRecoveryBackups));
                    }
                }
            }

        } catch (NullPointerException e) {
            throw new NullPointerException("Error while setting up Layout " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recovery_tools_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}