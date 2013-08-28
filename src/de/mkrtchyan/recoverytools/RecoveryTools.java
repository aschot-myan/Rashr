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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.ads.AdView;

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

    private final static String TAG = "Recovery-Tools";
    private final Context mContext = this;
    //	Get path to external storage
    private static final File PathToSd = Environment.getExternalStorageDirectory();
    //	Declaring needed files and folders
    private static final File PathToRecoveryTools = new File(PathToSd, "Recovery-Tools");
    public static final File PathToRecoveries = new File(PathToRecoveryTools, "recoveries");
    public static final File PathToBackups = new File(PathToRecoveryTools, "backups");
    public static final File PathToUtils = new File(PathToRecoveryTools, "utils");
    private File fRECOVERY;
    private String SYSTEM = "";
    //	Declaring needed objects
    private final Notifyer mNotifyer = new Notifyer(mContext);
    private final Common mCommon = new Common();
    private final DeviceHandler mDeviceHandler = new DeviceHandler(mContext);
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

            if (!mCommon.suRecognition()) {
                mNotifyer.showRootDeniedDialog();
            } else {
                if (fcFlashOther != null) {
                    if (fcFlashOther.use) {
                        fRECOVERY = fcFlashOther.selectedFile;
                    }
                }

                if (fRECOVERY != (null)) {
                    if (fRECOVERY.exists()) {
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
                                    //						        Create custom AlertDialog
                                    final AlertDialog.Builder abuilder = new AlertDialog.Builder(mContext);
                                    abuilder
                                            .setTitle(R.string.info)
                                            .setMessage(R.string.flash_over_recovery)
                                            .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    bRebooter(null);
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
//				        If Recovery File don't exist ask if you want to download it now.
                        fRECOVERY.delete();
                        mNotifyer.createAlertDialog(R.string.info, R.string.getdownload, rDownload).show();
                    }
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

        final CheckBox cbMethod = (CheckBox) findViewById(R.id.cbMethod);
        AdView adView = (AdView) findViewById(R.id.adView);
        Button CWM_BUTTON = (Button) findViewById(R.id.bCWM);
        Button TWRP_BUTTON = (Button) findViewById(R.id.bTWRP);
        Button BAK_MGR = (Button) findViewById(R.id.bBackupMgr);
        Button FLASH_OTHER = (Button) findViewById(R.id.bFlashOther);

//      If device is not supported, you can report it now or close the App
        if (mDeviceHandler.RecoveryPath.equals("")
                && !mDeviceHandler.MTD
                && !mDeviceHandler.FLASH_OVER_RECOVERY) {
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
            DeviceNotSupported.setCancelable(false);
            DeviceNotSupported.show();
        } else {
            if (!mCommon.getBooleanPerf(mContext, "recovery-tools", "first_run")) {

                if (!mDeviceHandler.FLASH_OVER_RECOVERY && mCommon.suRecognition()) {
                    final AlertDialog.Builder WarningDialog = new AlertDialog.Builder(mContext);
                    WarningDialog.setTitle(R.string.warning);
                    WarningDialog.setMessage(String.format(getString(R.string.bak_warning), PathToBackups.getAbsolutePath()));
                    WarningDialog.setPositiveButton(R.string.sBackup, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new BackupHandler(mContext).backup();
                            mCommon.setBooleanPerf(mContext, "recovery-tools", "first_run", true);
                        }
                    });
                    WarningDialog.setNegativeButton(R.string.risk, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mCommon.setBooleanPerf(mContext, "recovery-tools", "first_run", true);
                        }
                    });
                    WarningDialog.setCancelable(false);
                    WarningDialog.show();
                }
                mCommon.setBooleanPerf(mContext, "recovery-tools", "show_ads", true);
            }

//		    Create needed Folder
            mCommon.checkFolder(PathToRecoveryTools);
            mCommon.checkFolder(PathToRecoveries);
            mCommon.checkFolder(PathToUtils);

//		    Show Advanced information how device will be Flashed
            cbMethod.setChecked(true);
            if (mDeviceHandler.FLASH_OVER_RECOVERY) {
                cbMethod.setText(R.string.over_recovery);
            } else if (!mDeviceHandler.RecoveryPath.equals("")) {
                cbMethod.setText(R.string.using_dd);
            } else if (mDeviceHandler.MTD) {
                cbMethod.setText(R.string.using_mtd);
            }
        }

        try {
            if (!mCommon.getBooleanPerf(mContext, "recovery-tools", "show_ads"))
                ((ViewGroup) adView.getParent()).removeView(adView);
            if (!mDeviceHandler.CWM)
                ((ViewGroup) CWM_BUTTON.getParent()).removeView(CWM_BUTTON);
            if (!mDeviceHandler.TWRP)
                ((ViewGroup) TWRP_BUTTON.getParent()).removeView(TWRP_BUTTON);
            if (!mDeviceHandler.OTHER)
                ((ViewGroup) FLASH_OTHER.getParent()).removeAllViews();
            if (mDeviceHandler.FLASH_OVER_RECOVERY)
                ((ViewGroup) BAK_MGR.getParent()).removeView(BAK_MGR);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage(), e);
        }

        if (mDeviceHandler.MTD) {
            mDeviceHandler.fflash = new File(mContext.getFilesDir(), "flash_image");
            mCommon.pushFileFromRAW(mContext, mDeviceHandler.fflash, R.raw.flash_image);
            mDeviceHandler.fdump = new File(mContext.getFilesDir(), "dump_image");
            mCommon.pushFileFromRAW(mContext, mDeviceHandler.fdump, R.raw.dump_image);
        }
    }

    //	Button Methods (onClick)
    public void Go(View view) {
        fRECOVERY = null;
        if (!mDeviceHandler.downloadUtils()) {
//			Get device specificed recovery file for example recovery-clockwork-touch-6.0.3.1-grouper.img
            mDeviceHandler.constructFile();
            SYSTEM = view.getTag().toString();
            if (SYSTEM.equals("clockwork")) {
                fRECOVERY = mDeviceHandler.CWM_IMG;
            } else {
                fRECOVERY = mDeviceHandler.TWRP_IMG;
            }
            rFlasher.run();
        }
    }

    public void bFlashOther(View view) {
        fRECOVERY = null;
        fcFlashOther = new FileChooser(mContext, PathToSd.getAbsolutePath(), mDeviceHandler.EXT, rFlasher);
        fcFlashOther.show();
    }

    public void bBackupMgr(View view) {
        if (Build.VERSION.SDK_INT >= 11) {
            showPopupMenu(R.menu.bakmgr_menu, view);
        } else {
            startActivity(new Intent(this, BackupManager.class));
        }
    }

    public void bCleareCache(View view) {
        mCommon.deleteFolder(PathToRecoveries, false);
    }

    public void bRebooter(View view) {
        if (Build.VERSION.SDK_INT >= 11) {
            showPopupMenu(R.menu.rebooter_menu, view);
        } else {
            new Rebooter(mContext).show();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recovery_tools_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
        try {
            MenuItem iLog = menu.findItem(R.id.iLog);
            MenuItem iShowLogs = menu.findItem(R.id.iShowLogs);
            MenuItem iShowAds = menu.findItem(R.id.iShowAds);
            iShowLogs.setVisible(mCommon.getBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands"));
            iShowAds.setChecked(mCommon.getBooleanPerf(mContext, "recovery-tools", "show_ads"));
            iLog.setChecked(mCommon.getBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands"));
        } catch (NullPointerException e) {
            mNotifyer.showExceptionToast(e);
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.iDonate:
                startActivity(new Intent(this, DonationsActivity.class));
                return true;
            case R.id.iProfile:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/showthread.php?t=2334554")));
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
                report(true);
                return true;
            case R.id.iShowLogs:
                Dialog LogDialog = mNotifyer.createDialog(R.string.su_logs_title, R.layout.dialog_command_logs, false, true);
                final TextView tvLog = (TextView) LogDialog.findViewById(R.id.tvSuLogs);
                final Button bClearLog = (Button) LogDialog.findViewById(R.id.bClearLog);
                bClearLog.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
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

    public void report(boolean isCancelable) {
//		Creates a report Email with Comment
        final Dialog reportDialog = mNotifyer.createDialog(R.string.commentar, R.layout.dialog_comment, false, true);
        if (!isCancelable)
            reportDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    System.exit(0);
                    finish();
                }
            });
        final Button ok = (Button) reportDialog.findViewById(R.id.bGo);
        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!mCommon.getBooleanPerf(mContext, "recovery-tools", "show_ads"))
                    mNotifyer.showToast(R.string.please_ads);

                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    EditText text = (EditText) reportDialog.findViewById(R.id.etCommentar);
                    String comment = text.getText().toString();

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ashotmkrtchyan1995@gmail.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Recovery-Tools report");
                    intent.putExtra(Intent.EXTRA_TEXT, "Package Infos:" +
                            "\n\nName: " + pInfo.packageName +
                            "\nVersionName: " + pInfo.versionName +
                            "\nVersionCode: " + pInfo.versionCode +
                            "\n\n\nProduct Info: " +
                            "\n\nManufacture: " + android.os.Build.MANUFACTURER +
                            "\nDevice: " + Build.DEVICE + " (" + mDeviceHandler.DEVICE_NAME + ")" +
                            "\nBoard: " + Build.BOARD +
                            "\nBrand: " + Build.BRAND +
                            "\nModel: " + Build.MODEL +
                            "\nAndroid SDK Level: " + Build.VERSION.CODENAME + " (" + Build.VERSION.SDK_INT + ")" +
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

    @SuppressWarnings("NewApi")
    public void showPopupMenu(int MENU, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(MENU);
        popup.setOnMenuItemClickListener(new PopupHelper(mContext));
        popup.show();
    }
}