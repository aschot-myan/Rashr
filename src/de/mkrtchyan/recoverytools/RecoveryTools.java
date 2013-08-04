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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.ads.AdView;

import org.rootcommands.util.RootAccessDeniedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Downloader;
import de.mkrtchyan.utils.FileChooser;
import de.mkrtchyan.utils.Notifyer;

public class RecoveryTools extends Activity {

    private final Context mContext = this;
    private AdView adView;
    private ViewGroup layout;
    //	Get path to external storage
    private static final File PathToSd = Environment.getExternalStorageDirectory();
    //	Declaring needed files and folders
    private static final File PathToRecoveryTools = new File(PathToSd, "Recovery-Tools");
    public static final File PathToRecoveries = new File(PathToRecoveryTools, "recoveries");
    public static final File PathToUtils = new File(PathToRecoveryTools, "utils");
    public static final File PathToBin = new File("/system/bin");
    private File fRECOVERY, fflash, fdump, charger, chargermon, ric;
    //	Declaring other vars
    private String SYSTEM = "";
    private boolean firstrun = true;
    private boolean download = false;
    //	Declaring needed objects
    private final Notifyer mNotifyer = new Notifyer(mContext);
    private final Common mCommon = new Common();
    private final DeviceHandler mDeviceHandler = new DeviceHandler();
    private FileChooser fcFlashOther;

    //	"Methods" need a input from user (AlertDialog) or at the end of AsyncTask
    private final Runnable rFlash = new Runnable() {
        @Override
        public void run() {
            new FlashUtil(mContext, fRECOVERY, 1).execute();
        }
    };
    private final Runnable rFlasher = new Runnable() {
        @Override
        public void run() {
            if (fcFlashOther != null) {
                if (fcFlashOther.use) {
                    if (fcFlashOther.selectedFile.getName().endsWith(mDeviceHandler.EXT)) {
                        fRECOVERY = fcFlashOther.selectedFile;
                    } else {
                        fRECOVERY = null;
                    }
                }
            }

            if (fRECOVERY != (null)) {
                if (fRECOVERY.exists() && fRECOVERY.getAbsolutePath().endsWith(mDeviceHandler.EXT)) {
                    if (fRECOVERY.length() > 1000000) {
//				        If the flashing don't be handle specially flash it
                        if (!mDeviceHandler.KERNEL_TO && !mDeviceHandler.FLASH_OVER_RECOVERY) {
                            rFlash.run();
                        } else {
//					        Get user input if Kernel will be modified
                            if (mDeviceHandler.KERNEL_TO)
                                mNotifyer.createAlertDialog(R.string.warning, R.string.kernel_to, rFlash).show();
//					        Get user input if user want to install over recovery now
                            if (mDeviceHandler.FLASH_OVER_RECOVERY) {
//						        Create coustom AlertDialog
                                final AlertDialog.Builder abuilder = new AlertDialog.Builder(mContext);
                                abuilder
                                        .setTitle(R.string.info)
                                        .setMessage(R.string.flash_over_recovery)
                                        .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    mCommon.executeSuShell("setprop service.adb.tcp.port 5555 && stop adbd && start adbd");
                                                    mCommon.executeSuShell("adb connect localhost:5555 && adb reboot recovery");
                                                } catch (RootAccessDeniedException e) {
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
                                                        abuilder.show();

                                                    }
                                                });
                                                d.show();
                                            }
                                        })
                                        .show();
                            }
                        }
                    }
                } else {
//				    If Recovery File don't exist ask if you want to download it now.
                    mNotifyer.createAlertDialog(R.string.info, R.string.getdownload, rDownload).show();
                }
            }
        }
    };

    private final Runnable rDownload = new Runnable() {
        @Override
        public void run() {
//			Download file from URL mDeviceHandler."SYSTEM"_URL + "/" + fRECOVERY.getName().toString() and write it to fRECOVERY
            if (SYSTEM.equals("clockwork")) {
                new Downloader(mContext, mDeviceHandler.CWM_URL, mDeviceHandler.CWM_IMG.getName(), mDeviceHandler.CWM_IMG, rFlasher).execute();
            } else if (SYSTEM.equals("twrp")) {
                new Downloader(mContext, mDeviceHandler.TWRP_URL, mDeviceHandler.TWRP_IMG.getName(), mDeviceHandler.TWRP_IMG, rFlasher).execute();
            }
        }
    };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recovery_tools);

        final CheckBox cbUseBinary = (CheckBox) findViewById(R.id.cbUseBinary);
        adView = (AdView) findViewById(R.id.adView);
        Button CWM_BUTTON = (Button) findViewById(R.id.bCWM);
        Button TWRP_BUTTON = (Button) findViewById(R.id.bTWRP);
        ViewGroup layout = (ViewGroup) adView.getParent();


        if (mDeviceHandler.MTD) {
            fflash = new File("/system/bin", "flash_image");
            fdump = new File("/system/bin", "dump_image");
            //		Checking if flash and dump image is implemented in ROM
            if (!fflash.exists())
                fflash = new File(mContext.getFilesDir(), fflash.getName());
            if (!fdump.exists())
                fdump = new File(mContext.getFilesDir(), fdump.getName());
            if (!fflash.exists()
                    || !fdump.exists())
                download = true;
        }

        if (mDeviceHandler.DEVICE_NAME.equals("C6603")
                || mDeviceHandler.DEVICE_NAME.equals("montblanc")) {
            charger = new File(PathToUtils, "charger");
            chargermon = new File(PathToUtils, "chargermon");
            ric = new File(mContext.getFilesDir(), "ric");
            if (!charger.exists()
                    || !chargermon.exists()
                    || !chargermon.exists()
                    || !ric.exists() && mDeviceHandler.DEVICE_NAME.equals("C6603"))
                download = true;
        }

//		Do on every First-Run
        if (firstrun) {
//			If device is not supported, you can report it now or close the App
            if (mDeviceHandler.RecoveryPath.equals("")
                    && !mDeviceHandler.MTD
                    && !mDeviceHandler.FLASH_OVER_RECOVERY) {
                Runnable runOnTrue = new Runnable() {
                    @Override
                    public void run() {
                        report();
                    }
                };
                Runnable runOnNegative = new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        System.exit(0);
                    }
                };
                mNotifyer.createAlertDialog(R.string.warning, R.string.notsupportded, runOnTrue, null, runOnNegative).show();
            }
//			Check if Su-Access is given if not the app will be closed
            if (!mCommon.suRecognition()) {
//				Show a new notification with Info
                mNotifyer.showRootDeniedDialog();
            } else {

                if (!mCommon.getBooleanPerf(mContext, "recovery-tools", "first_run")) {

                    AlertDialog.Builder WarningDialog = new AlertDialog.Builder(mContext);
                    WarningDialog.setTitle(R.string.warning);
                    WarningDialog.setMessage(R.string.bak_warning);
                    WarningDialog.setPositiveButton(R.string.bak_now, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            bBackupMgr(null);
                        }
                    });
                    WarningDialog.setNegativeButton(R.string.risk, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    WarningDialog.setCancelable(false);
                    WarningDialog.show();
                    mCommon.setBooleanPerf(mContext, "recovery-tools", "first_run", true);
                    mCommon.setBooleanPerf(mContext, "recovery-tools", "show_ads", true);
                }
            }
            if (!mCommon.getBooleanPerf(mContext, "recovery-tools", "show_ads")) {
                layout.removeView(adView);
            }
            firstrun = false;
        }

//		Create needed Folder
        mCommon.checkFolder(PathToRecoveryTools);
        mCommon.checkFolder(PathToRecoveries);
        mCommon.checkFolder(PathToUtils);

//		Show Advanced information how device will be Flashed

        cbUseBinary.setChecked(true);
        if (mDeviceHandler.FLASH_OVER_RECOVERY) {
            cbUseBinary.setText(R.string.over_recovery);
        } else if (!mDeviceHandler.RecoveryPath.equals("")) {
            cbUseBinary.setText(String.format(mContext.getString(R.string.using_dd), "\n" + mDeviceHandler.RecoveryPath));
        } else if (mDeviceHandler.MTD) {
            cbUseBinary.setText(R.string.using_mtd);
        }

//		Setting up Buttons (CWM and TWRP support)
        if (!mDeviceHandler.CWM)
            layout.removeView(CWM_BUTTON);
        if (!mDeviceHandler.TWRP)
            layout.removeView(TWRP_BUTTON);

        downloadUtils();


    }

    //	Button Methods (onClick)
    public void Go(View view) {
        if (mDeviceHandler.MTD && fflash.exists() && fdump.exists()
                || mDeviceHandler.DEVICE_NAME.equals("C6603") && ric.exists() && charger.exists() && chargermon.exists()
                || mDeviceHandler.DEVICE_NAME.equals("montblanc") && charger.exists() && chargermon.exists()
                || !mDeviceHandler.MTD && !mDeviceHandler.DEVICE_NAME.equals("C6603") || !mDeviceHandler.DEVICE_NAME.equals("montblanc")) {
//			Get device specificed recovery file for example recovery-clockwork-touch-6.0.3.1-grouper.img
            mDeviceHandler.constructFile();
            SYSTEM = view.getTag().toString();
            if (SYSTEM.equals("clockwork")) {
                fRECOVERY = mDeviceHandler.CWM_IMG;
            } else {
                fRECOVERY = mDeviceHandler.TWRP_IMG;
            }
            rFlasher.run();
        } else {
            downloadUtils();
        }
    }

    public void bFlashOther(View view) {
        fcFlashOther = new FileChooser(mContext, PathToSd.getAbsolutePath(), mDeviceHandler.EXT, rFlasher);
        fcFlashOther.show();
    }

    public void bBackupMgr(View view) {
        startActivity(new Intent(this, BackupManagerActivity.class));
    }

    public void bCleareCache(View view) {
        mCommon.deleteFolder(PathToRecoveries, false);
    }

    public void bRebooter(View view) {
        new Rebooter(mContext).show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recovery_tools_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
        MenuItem iLog = menu.findItem(R.id.iLog);
        MenuItem iShowLogs = menu.findItem(R.id.iShowLogs);
        MenuItem iShowAds = menu.findItem(R.id.iShowAds);
        try {
            iShowLogs.setVisible(mCommon.getBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands"));
            iShowAds.setChecked(mCommon.getBooleanPerf(mContext, "recovery-tools", "show_ads"));
        } catch (RuntimeException e) {
            try {
                iShowLogs.setVisible(false);
            } catch (NullPointerException e1) {
                mNotifyer.showExceptionToast(e1);
            }
            mNotifyer.showExceptionToast(e);
        }
        iLog.setChecked(mCommon.getBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands"));

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.iProfile:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/showthread.php?t=2334554"));
                startActivity(browserIntent);
                return true;
            case R.id.iExit:
                finish();
                System.exit(0);
                return true;
            case R.id.iLog:
                if (mCommon.getBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands")) {
                    mCommon.setBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands", false);
                } else {
                    mCommon.setBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands", true);
                }
                return true;
            case R.id.iReport:
                report();
                return true;
            case R.id.iShowLogs:
                Dialog LogDialog = mNotifyer.createDialog(R.string.su_logs_title, R.layout.dialog_command_logs, false, true);
                final TextView tvLog = (TextView) LogDialog.findViewById(R.id.tvSuLogs);
                final Button bClearLog = (Button) LogDialog.findViewById(R.id.bClearLog);
                bClearLog.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        new File(mContext.getFilesDir(), "command-logs.log").delete();
                        tvLog.setText("");
                    }
                });
                String sLog = "";

                try {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput("command-logs.log")));
                    while ((line = br.readLine()) != null) {
                        sLog = sLog + line + "\n";
                    }
                    br.close();
                    tvLog.setText(sLog);
                } catch (FileNotFoundException e) {
                    tvLog.setText("No log found!");
                    e.printStackTrace();
                } catch (IOException e) {
                    tvLog.setText(tvLog.getText() + "\n" + e.getMessage());
                    e.printStackTrace();
                }

                LogDialog.show();
                return true;
            case R.id.iShowAds:
                if (mCommon.getBooleanPerf(mContext, "recovery-tools", "show_ads")) {
                    mCommon.setBooleanPerf(mContext, "recovery-tools", "show_ads", false);
                } else {
                    mCommon.setBooleanPerf(mContext, "recovery-tools", "show_ads", true);
                }
                mNotifyer.showToast(R.string.please_restart);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //	Called from Button Methods, created to redundancy

    public void report() {
//		Creates a report Email with Commentar
        final Dialog reportDialog = mNotifyer.createDialog(R.string.commentar, R.layout.dialog_comment, false, true);
        final Button ok = (Button) reportDialog.findViewById(R.id.bGo);
        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    EditText text = (EditText) reportDialog.findViewById(R.id.etCommentar);
                    String comment = text.getText().toString();

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mContext.getString(R.string.REPORT_to_EMAIL)});
                    intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.EMAIL_SUBJECT));
                    intent.putExtra(Intent.EXTRA_TEXT, "Package Infos:" +
                            "\n\nName: " + pInfo.packageName +
                            "\nVersionName: " + pInfo.versionName +
                            "\nVersionCode: " + pInfo.versionCode +
                            "\n\n\nProduct Info: " +
                            "\n\nManufacture: " + android.os.Build.MANUFACTURER +
                            "\nDevice: " + android.os.Build.DEVICE + " (" + mDeviceHandler.DEVICE_NAME + ")" +
                            "\nBoard: " + android.os.Build.BOARD +
                            "\nBrand: " + android.os.Build.BRAND +
                            "\nModel: " + android.os.Build.MODEL +
                            "\n\n\n===========Comment==========\n" + comment +
                            "\n===========Comment==========");
                    startActivity(Intent.createChooser(intent, "Send as EMAIL"));
                    reportDialog.dismiss();
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });

        reportDialog.show();
    }

    public void downloadUtils() {
        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);
        mAlertDialog
                .setTitle(R.string.warning)
                .setMessage(R.string.download_utils);
        DialogInterface.OnClickListener onClick = null;

        if (mDeviceHandler.MTD
                && download) {

            onClick = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new Downloader(mContext, "http://dslnexus.nazuka.net/utils", fflash.getName(), fflash, Notifyer.rEmpty).execute();
                    new Downloader(mContext, "http://dslnexus.nazuka.net/utils", fdump.getName(), fdump, Notifyer.rEmpty).execute();
                }
            };
        } else if (mDeviceHandler.DEVICE_NAME.equals("C6603") || mDeviceHandler.DEVICE_NAME.equals("montblanc")
                && download) {

            onClick = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    new Downloader(mContext, "http://dslnexus.nazuka.net/utils/" + mDeviceHandler.DEVICE_NAME, chargermon.getName(), chargermon, Notifyer.rEmpty).execute();
                    new Downloader(mContext, "http://dslnexus.nazuka.net/utils/" + mDeviceHandler.DEVICE_NAME, charger.getName(), charger, Notifyer.rEmpty).execute();
                    if (mDeviceHandler.DEVICE_NAME.equals("C6603"))
                        new Downloader(mContext, "http://dslnexus.nazuka.net/utils/" + mDeviceHandler.DEVICE_NAME, ric.getName(), ric, Notifyer.rEmpty).execute();
                }
            };
        }
        mAlertDialog.setPositiveButton(R.string.positive, onClick);
        if (mDeviceHandler.MTD && download
                || mDeviceHandler.DEVICE_NAME.equals("C6603") && download
                || mDeviceHandler.DEVICE_NAME.equals("montblanc") && download)
            mAlertDialog.show();
    }
}
