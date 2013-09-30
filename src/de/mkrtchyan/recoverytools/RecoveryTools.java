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
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.devspark.appmsg.AppMsg;

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

public class RecoveryTools extends ActionBarActivity {

    private static final String TAG = "Recovery-Tools";
    private final Context mContext = this;

    //	Get path to external storage
    private static final File PathToSd = Environment.getExternalStorageDirectory();
    //	Declaring needed files and folders
    private static final File PathToRecoveryTools = new File(PathToSd, "Recovery-Tools");
    private static final File PathToRecoveries = new File(PathToRecoveryTools, "recoveries");
    public static final File PathToCWM = new File(PathToRecoveries, "clockworkmod");
    public static final File PathToTWRP = new File(PathToRecoveries, "twrp");
    public static final File PathToBackups = new File(PathToRecoveryTools, "backups");
    public static final File PathToUtils = new File(PathToRecoveryTools, "utils");
    private File fRECOVERY;
    private String SYSTEM = "";
    //	Declaring needed objects
    private final Notifyer mNotifyer = new Notifyer(mContext);
    private final Common mCommon = new Common();
    private DeviceHandler mDeviceHandler = new DeviceHandler(mContext);
    private DrawerLayout mDrawerLayout = null;
    private FileChooser fcFlashOther = null;

    //	"Methods" need a input from user (AlertDialog) or at the end of AsyncTask
    private final Runnable rFlash = new Runnable() {
        @Override
        public void run() {
            new FlashUtil(mContext, fRECOVERY, FlashUtil.JOB_FLASH).execute();
        }
    };
    private final Runnable rFlasher = new Runnable() {
        @Override
        public void run() {

            if (!mCommon.suRecognition()
                    && !BuildConfig.DEBUG) {
                mNotifyer.showRootDeniedDialog();
            } else {
                if (fcFlashOther != null) {
                    if (fcFlashOther.isChoosed()) {
                        fRECOVERY = fcFlashOther.getSelectedFile();
                    }
                }

                if (fRECOVERY != (null)) {
                    if (fRECOVERY.exists()) {
//				        If the flashing don't be handle specially flash it
                        if (!mDeviceHandler.isKernelFlashed() && !mDeviceHandler.isOverRecovery()) {
                            rFlash.run();
                        } else {
//		        	        Get user input if Kernel will be modified
                            if (mDeviceHandler.isKernelFlashed())
                                mNotifyer.createAlertDialog(R.string.warning, R.string.kernel_to, rFlash).show();
//					        Get user input if user want to install over recovery now
                            if (mDeviceHandler.isOverRecovery()) {
//						        Create custom AlertDialog
                                final AlertDialog.Builder abuilder = new AlertDialog.Builder(mContext);
                                abuilder
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
                                                        abuilder.show();
                                                    }
                                                });
                                                d.show();
                                            }
                                        })
                                        .show();
                            }
                        }
                    } else {
//  				    If Recovery File don't exist ask if you want to download it now.
                        mNotifyer.createAlertDialog(R.string.info, R.string.img_not_found, rDownload).show();
                    }
                }
            }
        }
    };

    private final Runnable rDownload = new Runnable() {
        @Override
        public void run() {
            Downloader RecoveryDownloader;
//			Download file from URL mDeviceHandler."SYSTEM"_URL + "/" + fRECOVERY.getName().toString() and write it to fRECOVERY
            if (SYSTEM.equals("clockwork")) {
                RecoveryDownloader = new Downloader(mContext, mDeviceHandler.getCWM_URL(), mDeviceHandler.getCWM_IMG().getName(), mDeviceHandler.getCWM_IMG(), rFlasher);
            } else {
                RecoveryDownloader = new Downloader(mContext, mDeviceHandler.getTWRP_URL(), mDeviceHandler.getTWRP_IMG().getName(), mDeviceHandler.getTWRP_IMG(), rFlasher);
            }
            File SampleCorruptFile = new File(mContext.getFilesDir(), "corruptDownload");
            mCommon.pushFileFromRAW(mContext, SampleCorruptFile, R.raw.corrupt_download);
            RecoveryDownloader.setCheckFile(true);
            RecoveryDownloader.setSampleCorruptFile(SampleCorruptFile);
            RecoveryDownloader.execute();
        }
    };

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.recovery_tools);

        final CheckBox cbMethod = (CheckBox) findViewById(R.id.cbMethod);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final LinearLayout mDrawerLinear = (LinearLayout) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {

            public void onDrawerClosed(View view) {
            }

            public void onDrawerOpened(View drawerView) {
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        CheckBox cbShowAds = (CheckBox) mDrawerLinear.findViewById(R.id.cbShowAds);
        CheckBox cbLog = (CheckBox) mDrawerLinear.findViewById(R.id.cbLog);
        cbShowAds.setChecked(mCommon.getBooleanPerf(mContext, "recovery-tools", "show_ads"));
        cbLog.setChecked(mCommon.getBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands"));
        if (cbLog.isChecked()) {
            findViewById(R.id.bShowLogs).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.bShowLogs).setVisibility(View.INVISIBLE);
        }

        mDrawerToggle.syncState();
        mDrawerToggle.setDrawerIndicatorEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (BuildConfig.DEBUG) {
            showFakeDialog();
        }
//      If device is not supported, you can report it now or close the App
        if (!mDeviceHandler.isOtherSupported()) {
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

                if (!mDeviceHandler.isOverRecovery() && mCommon.suRecognition()) {
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
            mCommon.checkFolder(PathToCWM);
            mCommon.checkFolder(PathToTWRP);
            mCommon.checkFolder(PathToBackups);
            mCommon.checkFolder(PathToUtils);

//		    Show Advanced information how device will be Flashed
            cbMethod.setChecked(true);
            if (mDeviceHandler.isOverRecovery()) {
                cbMethod.setText(R.string.over_recovery);
            } else if (!mDeviceHandler.getRecoveryPath().equals("")) {
                cbMethod.setText(R.string.using_dd);
            } else if (mDeviceHandler.isMTD()) {
                cbMethod.setText(R.string.using_mtd);
            }
        }

        try {
            if (!mCommon.getBooleanPerf(mContext, "recovery-tools", "show_ads"))
                ((ViewGroup) findViewById(R.id.adView).getParent()).removeView(findViewById(R.id.adView));
            if (!mDeviceHandler.isCwmSupported())
                ((ViewGroup) findViewById(R.id.bCWM).getParent()).removeView(findViewById(R.id.bCWM));
            if (!mDeviceHandler.isTwrpSupported())
                ((ViewGroup) findViewById(R.id.bTWRP).getParent()).removeView(findViewById(R.id.bTWRP));
            if (mDeviceHandler.isOverRecovery()) {
                ((ViewGroup) findViewById(R.id.bBAK_MGR).getParent()).removeView(findViewById(R.id.bBAK_MGR));
                ((ViewGroup) findViewById(R.id.bHistory).getParent()).removeView(findViewById(R.id.bHistory));
            }
            if (!mDeviceHandler.isOtherSupported())
                ((ViewGroup) findViewById(R.id.bFlashOther).getParent()).removeAllViews();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage(), e);
        }

        if (mDeviceHandler.isMTD()) {
            mDeviceHandler.fflash = new File(mContext.getFilesDir(), "flash_image");
            mCommon.pushFileFromRAW(mContext, mDeviceHandler.fflash, R.raw.flash_image);
            mDeviceHandler.fdump = new File(mContext.getFilesDir(), "dump_image");
            mCommon.pushFileFromRAW(mContext, mDeviceHandler.fdump, R.raw.dump_image);
        }

    }

    public void bDonate(View view) {
        startActivity(new Intent(mContext, DonationsActivity.class));
    }

    public void bXDA(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/showthread.php?t=2334554")));
    }

    public void bExit(View view) {
        finish();
        System.exit(0);
    }

    public void cbLog(View view) {
        mCommon.setBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands", !mCommon.getBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands"));
        ((CheckBox) view).setChecked(mCommon.getBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands"));
        if (((CheckBox) view).isChecked()) {
            findViewById(R.id.bShowLogs).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.bShowLogs).setVisibility(View.INVISIBLE);
        }
    }

    public void bReport(View view) {
        report(true);
    }

    public void bShowLogs(View view) {
        final Dialog LogDialog = mNotifyer.createDialog(R.string.su_logs_title, R.layout.dialog_command_logs, false, true);
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
            LogDialog.dismiss();
        } catch (IOException e) {
            LogDialog.dismiss();
        }

        LogDialog.show();
    }

    public void cbShowAds(View view) {
        mCommon.setBooleanPerf(mContext, "recovery-tools", "show_ads", !mCommon.getBooleanPerf(mContext, "recovery-tools", "show_ads"));
        ((CheckBox) view).setChecked(mCommon.getBooleanPerf(mContext, "recovery-tools", "show_ads"));
        mNotifyer.showToast(R.string.please_restart, AppMsg.STYLE_INFO);
    }

    //	Button Methods (onClick)
    public void Go(View view) {
        fRECOVERY = null;
        if (!mDeviceHandler.downloadUtils()) {
//			Get device specificed recovery file for example recovery-clockwork-touch-6.0.3.1-grouper.img
            SYSTEM = view.getTag().toString();
            if (SYSTEM.equals("clockwork")) {
                fRECOVERY = mDeviceHandler.getCWM_IMG();
            } else {
                fRECOVERY = mDeviceHandler.getTWRP_IMG();
            }
            rFlasher.run();

        }
    }

    public void bFlashOther(View view) {
        fRECOVERY = null;
        fcFlashOther = new FileChooser(mContext, PathToSd.getAbsolutePath(), mDeviceHandler.getEXT(), rFlasher);
        fcFlashOther.show();
    }

    public void bShowHistory(View view) {
        File tmpFile[] = {null, null, null, null, null};
        String tmpFileNames[] = {"", "", "", "", ""};
        final Dialog d = new Dialog(mContext);
        d.setTitle(R.string.sHistory);
        ListView list = new ListView(mContext);

        for (int i = 0; i < 5; i++) {
            tmpFile[i] = new File(mCommon.getStringPerf(mContext, "recovery-tools", "last_history_" + String.valueOf(i)));
            if (!tmpFile[i].exists())
                mCommon.setStringPerf(mContext, "recovery-tools", "last_history_" + String.valueOf(i), "");
            else {
                tmpFileNames[i] = tmpFile[i].getName();
            }
        }
        final File file[] = tmpFile;
        list.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, tmpFileNames));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                if (!arg0.getItemAtPosition(arg2).toString().equals("")) {
                    fRECOVERY = file[arg2];
                    rFlasher.run();
                } else {
                    d.dismiss();
                    mNotifyer.showToast("No File choosed!", AppMsg.STYLE_CONFIRM);
                }
            }
        });
        d.setContentView(list);
        d.show();
    }

    public void bBackupMgr(View view) {
        showPopup(R.menu.bakmgr_menu, view);
    }

    public void bCleareCache(View view) {
        mCommon.deleteFolder(PathToRecoveries, false);
        mCommon.deleteFolder(PathToCWM, false);
        mCommon.deleteFolder(PathToTWRP, false);
    }

    public void bRebooter(View view) {
        showPopup(R.menu.rebooter_menu, view);
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
        final Button bGo = (Button) reportDialog.findViewById(R.id.bGo);
        bGo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!mCommon.getBooleanPerf(mContext, "recovery-tools", "show_ads"))
                    mNotifyer.showToast(R.string.please_ads, AppMsg.STYLE_ALERT);

                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    EditText text = (EditText) reportDialog.findViewById(R.id.etCommentar);
                    String comment = text.getText().toString();
                    String message = "Package Infos:" +
                            "\n\nName: " + pInfo.packageName +
                            "\nVersionName: " + pInfo.versionName +
                            "\nVersionCode: " + pInfo.versionCode +
                            "\n\n\nProduct Info: " +
                            "\n\nManufacture: " + android.os.Build.MANUFACTURER +
                            "\nDevice: " + Build.DEVICE + " (" + mDeviceHandler.DEVICE_NAME + ")" +
                            "\nBoard: " + Build.BOARD +
                            "\nBrand: " + Build.BRAND +
                            "\nModel: " + Build.MODEL +
                            "\nFingerprint: " + Build.FINGERPRINT +
                            "\nAndroid SDK Level: " + Build.VERSION.CODENAME + " (" + Build.VERSION.SDK_INT + ")" +
                            "\n\n\n===========Comment==========\n" + comment +
                            "\n===========Comment==========\n" +
                            "\nMTD Testresult:\n" +
                            mCommon.executeSuShell("cat /proc/mtd") + "\n" +
                            "\nDevice Tree:\n" +
                            "\n" + mCommon.executeSuShell("ls -lR /dev/block");
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ashotmkrtchyan1995@gmail.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Recovery-Tools report");
                    intent.putExtra(Intent.EXTRA_TEXT, message);
                    startActivity(Intent.createChooser(intent, "Send over GMAIL"));
                    reportDialog.dismiss();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (RootAccessDeniedException e) {
                    e.printStackTrace();
                }
            }
        });
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

    public void showPopup(int Menu, View v) {
        PopupMenu popup = new PopupMenu(mContext, v);
        popup.getMenuInflater().inflate(Menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {
                    switch (menuItem.getItemId()) {
                        case R.id.iReboot:
                            mCommon.executeSuShell(mContext, "reboot");
                            return true;
                        case R.id.iRebootRecovery:
                            mCommon.executeSuShell(mContext, "reboot recovery");
                            return true;
                        case R.id.iRebootBootloader:
                            new Common().executeSuShell(mContext, "reboot bootloader");
                            return true;
                        case R.id.iCreateBackup:
                            new BackupHandler(mContext).backup();
                            return true;
                        case R.id.iRestoreBackup:
                            new BackupHandler(mContext).restore();
                            return true;
                        case R.id.iDeleteBackup:
                            new BackupHandler(mContext).deleteBackup();
                            return true;
                        default:
                            return false;
                    }
                } catch (RootAccessDeniedException e) {
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

    private void showFakeDialog() {
        // Fake other devices
        final Dialog d = new Dialog(mContext);
        d.setTitle("Set your perfered device name");
        final LinearLayout ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.VERTICAL);
        final EditText et = new EditText(mContext);
        if (!mCommon.getStringPerf(mContext, "recovery-tools", "custom_device_name").equals("")) {
            et.setText(mCommon.getStringPerf(mContext, "recovery-tools", "custom_device_name"));
            mDeviceHandler = new DeviceHandler(mContext, mCommon.getStringPerf(mContext, "recovery-tools", "custom_device_name"));
        } else {
            et.setText(Build.DEVICE);
        }
        final Button setCustom = new Button(mContext);
        setCustom.setText(R.string.go);
        setCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommon.setStringPerf(mContext, "recovery-tools", "custom_device_name", et.getText().toString());
                d.dismiss();
                mNotifyer.showToast(R.string.please_restart, AppMsg.STYLE_INFO);

            }
        });
        final Button setDefault = new Button(mContext);
        setDefault.setText("Reset to Default");
        setDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommon.setStringPerf(mContext, "recovery-tools", "custom_device_name", Build.DEVICE);
                mCommon.setBooleanPerf(mContext, "recovery-tools", "use_custom_device_name", false);
                et.setText(Build.DEVICE);
                d.dismiss();
                mNotifyer.showToast(R.string.please_restart, AppMsg.STYLE_INFO);

            }
        });
        ll.addView(et);
        ll.addView(setCustom);
        ll.addView(setDefault);
        d.setContentView(ll);
        d.show();
    }
}