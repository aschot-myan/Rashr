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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Notifyer;

public class FlashUtil extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "FlashUtil";
    private static final String PREF_NAME = "FlashUtil";
    private static final String PREF_KEY_HIDE_REBOOT = "hide_reboot";
    private static final String PREF_KEY_FLASH_COUNTER = "last_counter";

    public static final int JOB_FLASH = 1;
    public static final int JOB_BACKUP = 2;
    public static final int JOB_RESTORE = 3;

    private final Context mContext;
    private ProgressDialog pDialog;
    private final Notifyer mNotifyer;
    private final DeviceHandler mDeviceHandler;
    private final File CustomRecovery;
    private final File CurrentRecovery;
    private final int JOB;
    private String output;
    private boolean keepAppOpen = true;
    private Exception exception = null;

    public FlashUtil(Context mContext, File CustomRecovery, int JOB) {
        this.mContext = mContext;
        this.CustomRecovery = CustomRecovery;
        this.JOB = JOB;
        mNotifyer = new Notifyer(mContext);
        mDeviceHandler = new DeviceHandler(mContext);
        CurrentRecovery = new File(mDeviceHandler.getRecoveryPath());
    }

    protected void onPreExecute() {

        pDialog = new ProgressDialog(mContext);

        switch (JOB) {
            case JOB_FLASH:
                pDialog.setTitle(R.string.flashing);
                Log.i(TAG, "Preparing to flash");
                break;
            case JOB_BACKUP:
                pDialog.setTitle(R.string.creating_bak);
                Log.i(TAG, "Preparing to backup");
                break;
            case JOB_RESTORE:
                pDialog.setTitle(R.string.restoring);
                Log.i(TAG, "Preparing to restore");
                break;
        }
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage(CustomRecovery.getName());
        pDialog.setCancelable(false);
        pDialog.show();

    }

    @Override
    protected Boolean doInBackground(Void... params) {

        try {
            switch (mDeviceHandler.getDevType()) {
                case DeviceHandler.DEV_TYPE_MTD:
                    output = MTD();
                    return true;
                case DeviceHandler.DEV_TYPE_DD:
                    output = DD();
                    return true;
                case DeviceHandler.DEV_TYPE_SONY:
                    output = SONY();
                    return true;
            }
            return false;
        } catch (Exception e) {
            exception = e;
            return false;
        }
    }

    protected void onPostExecute(Boolean success) {

        pDialog.dismiss();

        saveHistory();
        if (!success
                || output.endsWith("failed with error: -1\n")
                || output.endsWith("No such file or directory\n")
                || output.endsWith("style=gnu?)\n")) {
            if (exception != null) {
                Notifyer.showExceptionToast(mContext, TAG, exception);
            }
            int Title = 0;
            switch (JOB) {
                case JOB_FLASH:
                    Log.i(TAG, "Flash failed");
                    Title = R.string.flash_error;
                    break;
                case JOB_BACKUP:
                    Log.i(TAG, "Backup failed");
                    Title = R.string.bak_error;
                    break;
                case JOB_RESTORE:
                    Log.i(TAG, "Restore failed");
                    Title = R.string.res_error;
                    break;
            }
            mNotifyer.createDialog(Title, output, true).show();
        } else {
            if (JOB == JOB_FLASH || JOB == JOB_RESTORE) {
                Log.i(TAG, "Flash finished");
                if (!Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_HIDE_REBOOT)) {
                    showRebootDialog();
                } else {
                    if (!keepAppOpen) {
                        System.exit(0);
                    }
                }
            } else if (JOB == JOB_BACKUP) {
                Log.i(TAG, "Backup finished");
                Toast.makeText(mContext, R.string.bak_done, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showRebootDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.flashed)
                .setMessage(mContext.getString(R.string.reboot_recovery_now))
                .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Common.executeSuShell(mContext, "reboot recovery");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNeutralButton(R.string.neutral, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!keepAppOpen) {
                            System.exit(0);
                        }
                    }
                })
                .setNegativeButton(R.string.never_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Common.setBooleanPref(mContext, PREF_NAME, PREF_KEY_HIDE_REBOOT, true);
                        if (!keepAppOpen) {
                            System.exit(0);
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        if (!keepAppOpen) {
                            System.exit(0);
                        }
                    }
                })
                .setCancelable(keepAppOpen)
                .show();
    }

    public void saveHistory() {
        if (JOB == JOB_FLASH) {
            switch (Common.getIntegerPref(mContext, RecoveryTools.PREF_NAME, PREF_KEY_FLASH_COUNTER)) {
                case 0:
                    Common.setStringPref(mContext, RecoveryTools.PREF_NAME, RecoveryTools.PREF_KEY_HISTORY + String.valueOf(Common.getIntegerPref(mContext, RecoveryTools.PREF_NAME, PREF_KEY_FLASH_COUNTER)), CustomRecovery.getAbsolutePath());
                    Common.setIntegerPref(mContext, RecoveryTools.PREF_NAME, PREF_KEY_FLASH_COUNTER, 1);
                    return;
                default:
                    Common.setStringPref(mContext, RecoveryTools.PREF_NAME, RecoveryTools.PREF_KEY_HISTORY + String.valueOf(Common.getIntegerPref(mContext, RecoveryTools.PREF_NAME, PREF_KEY_FLASH_COUNTER)), CustomRecovery.getAbsolutePath());
                    Common.setIntegerPref(mContext, RecoveryTools.PREF_NAME, PREF_KEY_FLASH_COUNTER, Common.getIntegerPref(mContext, RecoveryTools.PREF_NAME, PREF_KEY_FLASH_COUNTER) + 1);
                    if (Common.getIntegerPref(mContext, RecoveryTools.PREF_NAME, PREF_KEY_FLASH_COUNTER) == 5)
                        Common.setIntegerPref(mContext, RecoveryTools.PREF_NAME, PREF_KEY_FLASH_COUNTER, 0);
            }
        }
    }

    public void setKeepAppOpen(boolean keepAppOpen) {
        this.keepAppOpen = keepAppOpen;
    }

    public String DD() throws Exception {
        String Command = "";
        if (JOB == JOB_FLASH || JOB == JOB_RESTORE) {
            Log.i(TAG, "Flash started!");
            Command = "dd if=\"" + CustomRecovery.getAbsolutePath() + "\" of=\"" + CurrentRecovery.getAbsolutePath() + "\"";
        } else if (JOB == JOB_BACKUP) {
            Log.i(TAG, "Backup started!");
            Command = "dd if=\"" + CurrentRecovery.getAbsolutePath() + "\" of=\"" + CustomRecovery.getAbsolutePath() + "\"";
        }
        return Common.executeSuShell(mContext, Command);
    }

    public String MTD() throws Exception {
        String Command = "";
        if (JOB == JOB_FLASH || JOB == JOB_RESTORE) {
            File flash_image = mDeviceHandler.getFlash_image();
            Common.chmod(mDeviceHandler.getFlash_image(), "741");
            Log.i(TAG, "Flash started!");
            Command = flash_image.getAbsolutePath() + " recovery " + CustomRecovery.getAbsolutePath();
        } else if (JOB == JOB_BACKUP) {
            File dump_image = mDeviceHandler.getDump_image();
            Common.chmod(dump_image, "741");
            Log.i(TAG, "Backup started!");
            Command = dump_image.getAbsolutePath() + " recovery " + CustomRecovery.getAbsolutePath();
        }
        return Common.executeSuShell(mContext, Command);

    }

    public String SONY() throws Exception {
        String Command = "";
        if (mDeviceHandler.DEV_NAME.equals("c6603")
                || mDeviceHandler.DEV_NAME.equals("c6602")
                || mDeviceHandler.DEV_NAME.equals("montblanc")) {
            if (JOB == JOB_FLASH || JOB == JOB_RESTORE) {
                Common.mountDir(CurrentRecovery, "RW");
                Common.executeSuShell(mContext, "cat " + mDeviceHandler.getCharger().getAbsolutePath() + " >> /system/bin/" + mDeviceHandler.getCharger().getName());
                Common.executeSuShell(mContext, "cat " + mDeviceHandler.getChargermon().getAbsolutePath() + " >> /system/bin/" + mDeviceHandler.getChargermon().getName());
                if (mDeviceHandler.DEV_NAME.equals("c6603")
                        || mDeviceHandler.DEV_NAME.equals("c6602")) {
                    Common.executeSuShell(mContext, "cat " + mDeviceHandler.getRic().getAbsolutePath() + " >> /system/bin/" + mDeviceHandler.getRic().getName());
                    Common.chmod(mDeviceHandler.getRic(), "755");
                }
                Common.chmod(mDeviceHandler.getCharger(), "755");
                Common.chmod(mDeviceHandler.getChargermon(), "755");
                Common.chmod(CustomRecovery, "644");
                Common.mountDir(new File(mDeviceHandler.getRecoveryPath()), "RO");
                Log.i(TAG, "Flash started!");
                Command = "cat " + CustomRecovery.getAbsolutePath() + " >> " + CurrentRecovery.getAbsolutePath();
            } else if (JOB == JOB_BACKUP) {
                Log.i(TAG, "Backup started!");
                Command = "cat " + CurrentRecovery.getAbsolutePath() + " >> " + CustomRecovery.getAbsolutePath();
            }
        }
        return Common.executeSuShell(mContext, Command);
    }
}