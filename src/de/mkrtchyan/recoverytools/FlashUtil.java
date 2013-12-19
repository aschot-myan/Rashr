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

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;

import java.io.File;
import java.io.IOException;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Notifyer;

public class FlashUtil extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "FlashUtil";
    private static final String PREF_NAME = "FlashUtil";
    private static final String PREF_KEY_HIDE_REBOOT = "hide_reboot";
    private static final String PREF_KEY_FLASH_COUNTER = "last_counter";

    private int PARTITION = 0;
    public static final int RECOVERY = 1;
    public static final int KERNEL = 2;

    public static final int JOB_FLASH = 1;
    public static final int JOB_BACKUP = 2;
    public static final int JOB_RESTORE = 3;

    private final Context mContext;
    private ProgressDialog pDialog;
    private final Notifyer mNotifyer;
    private final DeviceHandler mDeviceHandler;
    private final File CustomIMG, tmpFile;
    private File CurrentPartition;
    private final int JOB;
    private Shell mShell;
    private boolean keepAppOpen = true;
    private FailedExecuteCommand mFailedExecuteCommand = null;

    public FlashUtil(Context mContext, DeviceHandler mDeviceHandler, int Partition, File CustomIMG, int JOB) throws IOException {
        this.mContext = mContext;
        this.mDeviceHandler = mDeviceHandler;
        this.CustomIMG = CustomIMG;
        this.JOB = JOB;
        this.PARTITION = Partition;
        mShell = Shell.startRootShell();
        tmpFile = new File(mContext.getFilesDir(), CustomIMG.getName());
        mNotifyer = new Notifyer(mContext);
        switch (Partition) {
            case RECOVERY:
                CurrentPartition = new File(mDeviceHandler.RecoveryPath);
                break;
            case KERNEL:
                CurrentPartition = new File(mDeviceHandler.KernelPath);
                break;
        }
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
        pDialog.setMessage(CustomIMG.getName());
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            switch (mDeviceHandler.getDevType()) {
                case DeviceHandler.DEV_TYPE_MTD:
                    MTD();
                    return true;
                case DeviceHandler.DEV_TYPE_DD:
                    DD();
                    return true;
                case DeviceHandler.DEV_TYPE_SONY:
                    SONY();
                    return true;
            }
            return false;
        } catch (FailedExecuteCommand e) {
            mFailedExecuteCommand = e;
            return false;
        }
    }

    protected void onPostExecute(Boolean success) {
        File tmpFile = new File(mContext.getFilesDir(), CustomIMG.getName());
        pDialog.dismiss();
        saveHistory();
        if (!success || mFailedExecuteCommand != null) {

                Notifyer.showExceptionToast(mContext, TAG, mFailedExecuteCommand);

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
            mNotifyer.createDialog(Title, mFailedExecuteCommand.getCommand().getOutput(), true).show();
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
                try {
                    mShell.execCommand(mContext, "chmod 777 \"" + tmpFile.getAbsolutePath() + "\"");
                    Common.copyFile(tmpFile, CustomIMG);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(mContext, R.string.bak_done, Toast.LENGTH_SHORT).show();
            }
        }
        tmpFile.delete();
    }

    public void showRebootDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.flashed)
                .setMessage(mContext.getString(R.string.reboot_recovery_now))
                .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        try {
                            mShell.execCommand(mContext, "reboot recovery");
                        } catch (FailedExecuteCommand e) {
                            Notifyer.showExceptionToast(mContext, TAG, e);
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
                    Common.setStringPref(mContext, RecoveryTools.PREF_NAME, RecoveryTools.PREF_KEY_HISTORY + String.valueOf(Common.getIntegerPref(mContext, RecoveryTools.PREF_NAME, PREF_KEY_FLASH_COUNTER)), CustomIMG.getAbsolutePath());
                    Common.setIntegerPref(mContext, RecoveryTools.PREF_NAME, PREF_KEY_FLASH_COUNTER, 1);
                    return;
                default:
                    Common.setStringPref(mContext, RecoveryTools.PREF_NAME, RecoveryTools.PREF_KEY_HISTORY + String.valueOf(Common.getIntegerPref(mContext, RecoveryTools.PREF_NAME, PREF_KEY_FLASH_COUNTER)), CustomIMG.getAbsolutePath());
                    Common.setIntegerPref(mContext, RecoveryTools.PREF_NAME, PREF_KEY_FLASH_COUNTER, Common.getIntegerPref(mContext, RecoveryTools.PREF_NAME, PREF_KEY_FLASH_COUNTER) + 1);
                    if (Common.getIntegerPref(mContext, RecoveryTools.PREF_NAME, PREF_KEY_FLASH_COUNTER) == 5)
                        Common.setIntegerPref(mContext, RecoveryTools.PREF_NAME, PREF_KEY_FLASH_COUNTER, 0);
            }
        }
    }

    public void setKeepAppOpen(boolean keepAppOpen) {
        this.keepAppOpen = keepAppOpen;
    }

    public String DD() throws FailedExecuteCommand {
        String Command = "";
        try {
            File busybox = new File(mContext.getFilesDir(), "busybox");
            Common.chmod(mShell, busybox, "744");
            if (JOB == JOB_FLASH || JOB == JOB_RESTORE) {
                Log.i(TAG, "Flash started!");
                Common.copyFile(CustomIMG, tmpFile);
                Command = busybox.getAbsolutePath() + " dd if=\"" + tmpFile.getAbsolutePath() + "\" " +
                        "of=\"" + CurrentPartition.getAbsolutePath() + "\"";
            } else if (JOB == JOB_BACKUP) {
                Log.i(TAG, "Backup started!");
                Command = busybox.getAbsolutePath() + " dd if=\"" + CurrentPartition.getAbsolutePath() + "\" " +
                        "of=\"" + tmpFile.getAbsolutePath() + "\"";
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return mShell.execCommand(mContext, Command);
    }

    public String MTD() throws FailedExecuteCommand{
        String Command = "";
        try {

            String partition;
            if (PARTITION == KERNEL) {
                partition = "boot";
            } else {
                partition = "recovery";
            }
            if (JOB == JOB_FLASH || JOB == JOB_RESTORE) {
                File flash_image = mDeviceHandler.getFlash_image(mContext);
                Common.chmod(mShell, mDeviceHandler.getFlash_image(mContext), "741");
                Log.i(TAG, "Flash started!");
                Command = flash_image.getAbsolutePath() + " " + partition + " \"" + tmpFile.getAbsolutePath() + "\"";
            } else if (JOB == JOB_BACKUP) {
                File dump_image = mDeviceHandler.getDump_image(mContext);
                Common.chmod(mShell, dump_image, "741");
                Log.i(TAG, "Backup started!");

                Command = dump_image.getAbsolutePath() + " " + partition + " \"" + tmpFile.getAbsolutePath() + "\"";
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return mShell.execCommand(mContext, Command);
    }

    public String SONY() throws FailedExecuteCommand {
        String Command = "";
        try {
            if (mDeviceHandler.DEV_NAME.equals("c6603")
                    || mDeviceHandler.DEV_NAME.equals("c6602")
                    || mDeviceHandler.DEV_NAME.equals("montblanc")) {
                if (JOB == JOB_FLASH || JOB == JOB_RESTORE) {
                    Common.mountDir(CurrentPartition, "RW");
                    mShell.execCommand(mContext, "cat " + mDeviceHandler.getCharger().getAbsolutePath() + " >> /system/bin/" + mDeviceHandler.getCharger().getName());
                    mShell.execCommand(mContext, "cat " + mDeviceHandler.getChargermon().getAbsolutePath() + " >> /system/bin/" + mDeviceHandler.getChargermon().getName());
                    if (mDeviceHandler.DEV_NAME.equals("c6603")
                            || mDeviceHandler.DEV_NAME.equals("c6602")) {
                        mShell.execCommand(mContext, "cat " + mDeviceHandler.getRic().getAbsolutePath() + " >> /system/bin/" + mDeviceHandler.getRic().getName());
                        Common.chmod(mShell, mDeviceHandler.getRic(), "755");
                    }
                    Common.chmod(mShell, mDeviceHandler.getCharger(), "755");
                    Common.chmod(mShell, mDeviceHandler.getChargermon(), "755");
                    Common.chmod(mShell, CustomIMG, "644");
                    Common.mountDir(CurrentPartition, "RO");
                    Log.i(TAG, "Flash started!");
                    Command = "cat " + CustomIMG.getAbsolutePath() + " >> " + CurrentPartition.getAbsolutePath();
                } else if (JOB == JOB_BACKUP) {
                    Log.i(TAG, "Backup started!");
                    Command = "cat " + CurrentPartition.getAbsolutePath() + " >> " + CustomIMG.getAbsolutePath();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mShell.execCommand(mContext, Command);
    }
}