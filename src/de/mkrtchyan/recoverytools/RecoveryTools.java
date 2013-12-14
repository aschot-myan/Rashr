package de.mkrtchyan.recoverytools;

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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Downloader;
import de.mkrtchyan.utils.FileChooser;
import de.mkrtchyan.utils.Notifyer;

public class RecoveryTools extends ActionBarActivity {

    private static final String TAG = "Recovery-Tools";
    private final ActionBarActivity mActivity = this;
    //  Declaring setting names
    public static final String PREF_NAME = "recovery_tools";
    private static final String PREF_STYLE = "style";
    private static final String PREF_KEY_ADS = "show_ads";
    private static final String PREF_KEY_CUR_VER = "current_version";
    private static final String PREF_KEY_FIRST_RUN = "first_run";
    public static final String PREF_KEY_HISTORY = "last_history_";

    private final Context mContext = this;
    private final int HOLO_DARK = R.style.Theme_AppCompat;
    private final int HOLO_LIGHT = R.style.Theme_AppCompat_Light;
    private final int HOLO_LIGHT_DARK_BAR = R.style.Theme_AppCompat_Light_DarkActionBar;

    //  Used paths and files
    private static final File PathToSd = Environment.getExternalStorageDirectory();
    private static final File PathToRecoveryTools = new File(PathToSd, "Recovery-Tools");
    private static final File PathToRecoveries = new File(PathToRecoveryTools, "recoveries");
    public static final File PathToCWM = new File(PathToRecoveries, "clockworkmod");
    public static final File PathToTWRP = new File(PathToRecoveries, "twrp");
    public static final File PathToRecoveryBackups = new File(PathToRecoveryTools, "recovery-backups");
    public static final File PathToKernelBackups = new File(PathToRecoveries, "kernel-backups");
    public static final File PathToUtils = new File(PathToRecoveryTools, "utils");
    public static final File Sums = new File(PathToUtils, "IMG_SUMS");
    public static final File LastLog = new File("/cache/recovery/last_log");
    private File fRECOVERY;

    //	Declaring needed objects
    private final Notifyer mNotifyer = new Notifyer(mContext);
    private Shell mShell;
    private DeviceHandler mDeviceHandler;
    private DrawerLayout mDrawerLayout = null;
    private FileChooser fcFlashOther = null;
    private boolean keepAppOpen = true;
    private boolean version_changed = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mShell = Shell.startRootShell();
            mDeviceHandler = new DeviceHandler(mContext);

            if (getIntent().getData() != null) {
                handleIntent();
            } else {
//      	If device is not supported, you can report it now or close the App
                if (!mDeviceHandler.isOtherSupported()) {
                    showDeviceNotSupportedDialog();
                } else {
                    if (!Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_FIRST_RUN)) {
                        Common.setIntegerPref(mContext, PREF_NAME, PREF_STYLE, HOLO_LIGHT_DARK_BAR);
                        Common.setBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS, true);
                        Common.setBooleanPref(mContext, Common.PREF_NAME, Shell.PREF_LOG, true);
                        showUsageWarning();
                    } else {
                        Notifyer.showAppRateDialog(mContext);
                        if (version_changed) {
                            showChangelog();
                        }
                    }
                }

                this.setTheme(Common.getIntegerPref(mContext, PREF_NAME, PREF_STYLE));
                new StartUpLoader().execute();
                mDeviceHandler.downloadUtils(mContext);

            }

        } catch (IOException e) {
            Notifyer.showExceptionToast(mContext, TAG, e);
        }
    }

    //	View Methods (onClick)
    public void Go(View view) {
        fRECOVERY = null;
        ArrayList<String> Versions;
        if (!mDeviceHandler.downloadUtils(mContext)) {
            //			Get device specified recovery file for example recovery-clockwork-touch-6.0.3.1-grouper.img
            final String SYSTEM = view.getTag().toString();
            if (SYSTEM.equals("clockwork")) {
                Versions = mDeviceHandler.CwmArrayList;
            } else if (SYSTEM.equals("twrp")) {
                Versions = mDeviceHandler.TwrpArrayList;
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
                        }

                        if (!fRECOVERY.exists()) {
                            Downloader RecoveryDownloader = new Downloader(mContext, DeviceHandler.HOST_URL, fRECOVERY.getName(), fRECOVERY, rFlasher);
                            RecoveryDownloader.setRetry(true);
                            RecoveryDownloader.setAskBeforeDownload(true);
                            RecoveryDownloader.setChecksumFile(Sums);
                            RecoveryDownloader.ask();
                        } else {
                            rFlasher.run();
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
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/showthread.php?t=2334554")));
    }

    public void bExit(View view) {
        finish();
        System.exit(0);
    }

    public void cbLog(View view) {
        CheckBox cbLog = (CheckBox) view;
        Common.setBooleanPref(mContext, Shell.PREF_NAME, Shell.PREF_LOG, !Common.getBooleanPref(mContext, Shell.PREF_NAME, Shell.PREF_LOG));
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
        Common.setBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS, !Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS));
        ((CheckBox) view).setChecked(Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS));
        new StartUpLoader().execute();
    }

    public void bFlashOther(View view) {
        fRECOVERY = null;
        try {
            fcFlashOther = new FileChooser(view.getContext(), PathToSd, new Runnable() {
                @Override
                public void run() {
                    fRECOVERY = fcFlashOther.getSelectedFile();
                    rFlasher.run();
                }
            });
            fcFlashOther.setTitle(R.string.search_recovery);
            fcFlashOther.setEXT(mDeviceHandler.EXT);
            fcFlashOther.setBrowseUpEnabled(true);
            fcFlashOther.show();
        } catch (NullPointerException e) {
            Notifyer.showExceptionToast(mContext, TAG, e);
        }
    }

    public void bShowHistory(View view) {
        final ArrayList<File> HistoryFiles = new ArrayList<File>();
        final ArrayList<String> HistoryFileNames = new ArrayList<String>();
        final Dialog HistoryDialog = new Dialog(mContext);
        HistoryDialog.setTitle(R.string.sHistory);
        ListView HistoryList = new ListView(mContext);
        File tmp;
        for (int i = 0; i < 5; i++) {
            tmp = new File(Common.getStringPref(mContext, PREF_NAME, PREF_KEY_HISTORY + String.valueOf(i)));
            if (!tmp.exists())
                Common.setStringPref(mContext, PREF_NAME, PREF_KEY_HISTORY + String.valueOf(i), "");
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
                    rFlasher.run();
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

    public void bCreateBackup(View view) {

        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle(R.string.setname);
        dialog.setContentView(R.layout.dialog_input);
        final Button bGoBackup = (Button) dialog.findViewById(R.id.bGoBackup);
        final EditText etFileName = (EditText) dialog.findViewById(R.id.etFileName);
        String NameHint = Calendar.getInstance().get(Calendar.DATE)
                + "-" + Calendar.getInstance().get(Calendar.MONTH)
                + "-" + Calendar.getInstance().get(Calendar.YEAR)
                + "-" + Calendar.getInstance().get(Calendar.HOUR)
                + ":" + Calendar.getInstance().get(Calendar.MINUTE);
        etFileName.setHint(NameHint);
        bGoBackup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String Name = "";
                if (etFileName.getText() != null && !etFileName.getText().toString().equals(""))
                    Name = etFileName.getText().toString();
                if (Name.equals(""))
                    Name = String.valueOf(etFileName.getHint());
                if (!Name.endsWith(mDeviceHandler.EXT))
                    Name = Name + mDeviceHandler.EXT;
                final File fBACKUP = new File(RecoveryTools.PathToRecoveryBackups, Name);
                Runnable rBackup = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new FlashUtil(mContext, mDeviceHandler, fBACKUP, FlashUtil.JOB_BACKUP).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        new StartUpLoader().execute();

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
    }

    public void bRebooter(View view) {
        showPopup(R.menu.rebooter_menu, view);
    }

    public void report(final boolean isCancelable) {
        final Dialog reportDialog = mNotifyer.createDialog(R.string.commentar, R.layout.dialog_comment, false, true);
        new Thread(new Runnable() {
            @Override
            public void run() {
//		Creates a report Email including a Comment and important device infos
                if (!isCancelable)
                    reportDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            System.exit(0);
                            finish();
                        }
                    });
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
                                        "\nMTD result:\n" + mShell.execCommand("cat /proc/mtd") + "\n" +
                                        "\nDevice Tree:\n" + "\n" + mShell.execCommand("ls -lR /dev")).getBytes());
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
                                        "\nDevice: " + Build.DEVICE + " (" + mDeviceHandler.DEV_NAME + ")" +
                                        "\nBoard: " + Build.BOARD +
                                        "\nBrand: " + Build.BRAND +
                                        "\nModel: " + Build.MODEL +
                                        "\nFingerprint: " + Build.FINGERPRINT +
                                        "\nAndroid SDK Level: " + Build.VERSION.CODENAME + " (" + Build.VERSION.SDK_INT + ")" +
                                        "\nRecovery Path: " + mDeviceHandler.RecoveryPath +
                                        "\nIs MTD: " + mDeviceHandler.isMTD() +
                                        "\nIs DD: " + mDeviceHandler.isDD() +
                                        "\n\n\n===========Comment==========\n" + comment +
                                        "\n===========Comment==========\n");
                                File CommandLogs = new File(mContext.getFilesDir(), Shell.Logs);
                                if (CommandLogs.exists())
                                    files.add(CommandLogs);
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
        reportDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            if (mDrawerLayout.isDrawerOpen(Gravity.LEFT))
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            else
                mDrawerLayout.openDrawer(Gravity.LEFT);
        return super.onOptionsItemSelected(item);
    }

    private void showChangelog() {
        if (version_changed) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setTitle(R.string.changelog);
            WebView changes = new WebView(mContext);
            changes.setWebViewClient(new WebViewClient());
            changes.loadUrl("http://forum.xda-developers.com/showpost.php?p=42839595&postcount=3");
            changes.getSettings().setJavaScriptEnabled(true);
            dialog.setView(changes);
            dialog.show();
        }
    }

    private void showUsageWarning() {
        if (mDeviceHandler.getDevType() != DeviceHandler.DEV_TYPE_RECOVERY && Common.suRecognition()) {
            final AlertDialog.Builder WarningDialog = new AlertDialog.Builder(mContext);
            WarningDialog.setTitle(R.string.warning);
            WarningDialog.setMessage(String.format(getString(R.string.bak_warning), PathToRecoveryBackups.getAbsolutePath()));
            WarningDialog.setPositiveButton(R.string.sBackup, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    bCreateBackup(null);
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
                        case R.id.iRestore:
                            new FlashUtil(mContext, mDeviceHandler, new File(PathToRecoveryBackups, text), FlashUtil.JOB_RESTORE).execute();
                            return true;
                        case R.id.iRename:
                            final Dialog dialog = new Dialog(mContext);
                            dialog.setTitle(R.string.setname);
                            dialog.setContentView(R.layout.dialog_input);
                            final Button bGo = (Button) dialog.findViewById(R.id.bGoBackup);
                            final EditText etFileName = (EditText) dialog.findViewById(R.id.etFileName);
                            final File Backup = new File(PathToRecoveryBackups, text);
                            etFileName.setHint(text);
                            bGo.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {

                                    String Name = "";
                                    if (etFileName.getText() != null && !etFileName.getText().toString().equals(""))
                                        Name = etFileName.getText().toString();
                                    if (Name.equals(""))
                                        Name = String.valueOf(etFileName.getHint());
                                    if (!Name.endsWith(mDeviceHandler.EXT))
                                        Name = Name + mDeviceHandler.EXT;
                                    final File newBackup = new File(RecoveryTools.PathToRecoveryBackups, Name);

                                    if (newBackup.exists()) {
                                        Toast
                                                .makeText(mContext, R.string.backupalready, Toast.LENGTH_SHORT)
                                                .show();
                                    } else {
                                        Backup.renameTo(newBackup);
                                    }
                                    dialog.dismiss();
                                    new StartUpLoader().execute();

                                }
                            });
                            dialog.show();
                            return true;
                        case R.id.iDeleteBackup:
                            if (((TextView) v).getText() != null) {
                                new File(PathToRecoveryBackups, text).delete();
                                Toast.makeText(mContext, String.format(mContext.getString(R.string.bak_deleted)
                                        , ((TextView) v).getText()), Toast.LENGTH_SHORT).show();
                                new StartUpLoader().execute();
                            }
                            return true;
                        default:
                            return false;
                    }
                } catch (FailedExecuteCommand e) {
                    Notifyer.showExceptionToast(mContext, TAG, e);
                    return false;
                } catch (IOException e) {
                    Notifyer.showExceptionToast(mContext, TAG, e);
                    return false;
                }
            }
        });
        popup.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (!mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            } else {
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
                        rFlasher.run();
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
        AlertDialog.Builder DeviceNotSupported = mNotifyer.createAlertDialog(R.string.warning, R.string.notsupportded,
                new Runnable() {
                    @Override
                    public void run() {
                        report(false);
                    }
                }, null,
                new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        System.exit(0);
                    }
                }
        );
        DeviceNotSupported.setCancelable(BuildConfig.DEBUG);
        DeviceNotSupported.show();
    }

    public void handleIntent() {
//      Handle with Files chosen by FileBrowsers
        if (getIntent().getData() != null) {
            keepAppOpen = false;
            fRECOVERY = new File(getIntent().getData().getPath());
            getIntent().setData(null);
            showFlashAlertDialog();
        }
    }


    //	"Methods" need a input from user (AlertDialog) or at the end of AsyncTasks
    private final Runnable rFlash = new Runnable() {
        @Override
        public void run() {
            try {
                FlashUtil flashUtil = new FlashUtil(mContext, mDeviceHandler, fRECOVERY, FlashUtil.JOB_FLASH);
                flashUtil.setKeepAppOpen(keepAppOpen);
                flashUtil.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private final Runnable rFlasher = new Runnable() {
        @Override
        public void run() {
            if (!Common.suRecognition()) {
                mNotifyer.showRootDeniedDialog();
            } else {
                if (fRECOVERY != null) {
                    if (fRECOVERY.exists()) {
                        if (fRECOVERY.getName().endsWith(mDeviceHandler.EXT)) {
//				            If the flashing don't be handle specially flash it
                            if (!mDeviceHandler.isKernelFlashed() && !mDeviceHandler.isOverRecovery()) {
                                rFlash.run();
                            } else {
//		        	            Get user input if Kernel will be modified
                                if (mDeviceHandler.isKernelFlashed())
                                    mNotifyer.createAlertDialog(R.string.warning, R.string.kernel_to, rFlash).show();
//					            Get user input if user want to install over recovery now
                                if (mDeviceHandler.isOverRecovery()) {
                                    showOverRecoveryInstructions();
                                }
                            }
                        }
                    }
                }
            }
            fcFlashOther = null;
        }
    };

    private class StartUpLoader extends AsyncTask<Void, Void, Void> {

        private String currentRecovery = "Stock";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setContentView(new ProgressBar(mContext));
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Common.checkFolder(PathToRecoveryTools);
            Common.checkFolder(PathToRecoveries);
            Common.checkFolder(PathToCWM);
            Common.checkFolder(PathToTWRP);
            Common.checkFolder(PathToRecoveryBackups);
            Common.checkFolder(PathToKernelBackups);
            Common.checkFolder(PathToUtils);
            Common.checkFolder(new File(PathToUtils, mDeviceHandler.DEV_NAME));
            try {
                if (getPackageManager() != null) {
                    PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(getPackageName(), 0);
                    final int previous_version = Common.getIntegerPref(mContext, PREF_NAME, PREF_KEY_CUR_VER);
                    final int current_version = pInfo.versionCode;
                    if (current_version > previous_version) {
                        version_changed = true;
                    }
                    Common.setIntegerPref(mContext, PREF_NAME, PREF_KEY_CUR_VER, current_version);
                }
            } catch (PackageManager.NameNotFoundException e) {
                version_changed = true;
            } catch (NullPointerException e) {
                version_changed = true;
            }
            unpackFiles();
            readLastLog();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            optimizeLayout();
        }

        public void optimizeLayout() throws NullPointerException {
            setContentView(R.layout.recovery_tools);

            ViewGroup layout = (ViewGroup) findViewById(R.id.MainLayout);
            try {
                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
                ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, R.drawable.ic_drawer, R.string.settings, R.string.app_name);
                mDrawerLayout.setDrawerListener(mDrawerToggle);
                mDrawerToggle.syncState();
                mDrawerToggle.setDrawerIndicatorEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);

                String Styles[] = getResources().getStringArray(R.array.styles);

                final LinearLayout mMenuDrawer = (LinearLayout) findViewById(R.id.menu_drawer);
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
                                Common.setIntegerPref(mContext, PREF_NAME, PREF_STYLE, HOLO_LIGHT_DARK_BAR);
                                break;
                            case 2:
                                Common.setIntegerPref(mContext, PREF_NAME, PREF_STYLE, HOLO_LIGHT);
                                break;
                            case 3:
                                Common.setIntegerPref(mContext, PREF_NAME, PREF_STYLE, HOLO_DARK);
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

                final TextView RecoveryVersion = (TextView) findViewById(R.id.tvRecoveryVersion);
                RecoveryVersion.setText(currentRecovery);
                loadBackupDrawer();

                if (!Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_ADS)) {
                    layout.removeView(layout.findViewById(R.id.adView));
                }
                if (!mDeviceHandler.isCwmSupported()) {
                    layout.removeView(layout.findViewById(R.id.bCWM));
                }
                if (!mDeviceHandler.isTwrpSupported()) {
                    layout.removeView(layout.findViewById(R.id.bTWRP));
                }
                if (mDeviceHandler.isOverRecovery()) {
                    layout.removeView(layout.findViewById(R.id.backup_drawer));
                    layout.removeView(layout.findViewById(R.id.bHistory));
                }
                if (!mDeviceHandler.isOtherSupported())
                    layout.removeAllViews();
            } catch (NullPointerException e) {
                throw new NullPointerException("Error while setting up Layout " + e.getMessage());
            }
        }

        public void loadBackupDrawer() {
            if (PathToRecoveryBackups.listFiles() != null) {

                ArrayList<File> BakList = new ArrayList<File>();
                BakList.addAll(Arrays.asList(PathToRecoveryBackups.listFiles()));
                String[] tmp = new String[BakList.toArray(new File[BakList.size()]).length];
                for (int i = 0; i < tmp.length; i++) {
                    tmp[i] = BakList.get(i).getName();
                }

                final ListView lvBackups = (ListView) mActivity.findViewById(R.id.lvBackups);
                lvBackups.setAdapter(new ArrayAdapter<String>(mContext, R.layout.custom_list_item, tmp));
                lvBackups.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                            long arg3) {
                        showPopup(R.menu.bakmgr_menu, arg1);
                    }
                });

            }
        }

        public void readLastLog() {
            try {
                File LogCopy = new File(mContext.getFilesDir(), LastLog.getName() + ".txt");
                if (LastLog.exists())
                    LogCopy.delete();
                mShell.execCommand("cp " + LastLog.getAbsolutePath() + " " + LogCopy.getAbsolutePath());
                if (LogCopy.exists()) {
                    Common.chmod(mShell ,LogCopy, "644");
                    String line;
                    boolean version_found = false;
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(LogCopy)));
                    while ((line = br.readLine()) != null && !version_found) {
                        if (line.contains("ClockworkMod Recovery") || line.contains("CWM")) {
                            version_found = true;
                            currentRecovery = line;
                        }
                        if (line.contains("Starting TWRP")) {
                            version_found = true;
                            line = line.replace("Starting ", "");
                            line = line.substring(0, 12);
                            currentRecovery = line;
                        }
                    }
                    br.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void unpackFiles() {
            try {
                if (mDeviceHandler.isMTD()) {
                    File flash_image = mDeviceHandler.getFlash_image(mContext);
                    if (!flash_image.exists()|| version_changed)
                        Common.pushFileFromRAW(mContext, flash_image, R.raw.flash_image, true);
                    File dump_image = mDeviceHandler.getDump_image(mContext);
                    if (!dump_image.exists()|| version_changed)
                        Common.pushFileFromRAW(mContext, dump_image, R.raw.dump_image, true);
                }
                File busybox = new File(mContext.getFilesDir(), "busybox");
                if (!busybox.exists() || version_changed)
                    Common.pushFileFromRAW(mContext, busybox, R.raw.busybox, true);
                if (!RecoveryTools.Sums.exists() || version_changed) {
                    Common.pushFileFromRAW(mContext, RecoveryTools.Sums, R.raw.img_sums, true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}