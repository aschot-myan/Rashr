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
    private static final String PREF_KEY_FLASH_RECOVERY_COUNTER = "last_recovery_counter";
    private static final String PREF_KEY_FLASH_KERNEL_COUNTER = "last_kernel_counter";

    public static final int JOB_FLASH_RECOVERY = 1;
    public static final int JOB_BACKUP_RECOVERY = 2;
    public static final int JOB_RESTORE_RECOVERY = 3;

    public static final int JOB_FLASH_KERNEL = 4;
    public static final int JOB_BACKUP_KERNEL = 5;
    public static final int JOB_RESTORE_KERNEL= 6;

    private final Context mContext;
    private ProgressDialog pDialog;
    private final Device mDevice;
    private Shell mShell;
    private final int JOB;
    private File tmpFile, CurrentPartition;
    private final File CustomIMG;
    private boolean keepAppOpen = true;
    private Runnable RunAtEnd;

    private FailedExecuteCommand mFailedExecuteCommand = null;

    public FlashUtil(Shell mShell, Context mContext, Device mDevice, File CustomIMG, int JOB){
        this.mShell = mShell;
        this.mContext = mContext;
        this.mDevice = mDevice;
        this.JOB = JOB;
        this.CustomIMG = CustomIMG;
        tmpFile = new File(mContext.getFilesDir(), CustomIMG.getName());
    }

    protected void onPreExecute() {
        pDialog = new ProgressDialog(mContext);

        switch (JOB) {
            case JOB_FLASH_RECOVERY:
                pDialog.setTitle(R.string.flashing);
                Log.i(TAG, "Preparing to flash recovery");
                break;
            case JOB_BACKUP_RECOVERY:
                pDialog.setTitle(R.string.creating_bak);
                Log.i(TAG, "Preparing to backup recovery");
                break;
            case JOB_RESTORE_RECOVERY:
                pDialog.setTitle(R.string.restoring);
                Log.i(TAG, "Preparing to restore recovery");
                break;
            case JOB_FLASH_KERNEL:
                pDialog.setTitle(R.string.flashing);
                Log.i(TAG, "Preparing to flash kernel");
                break;
            case JOB_BACKUP_KERNEL:
                pDialog.setTitle(R.string.creating_bak);
                Log.i(TAG, "Preparing to backup kernel");
                break;
            case JOB_RESTORE_KERNEL:
                pDialog.setTitle(R.string.restoring);
                Log.i(TAG, "Preparing to restore kernel");
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
        if (isJobRecovery()) {
            CurrentPartition = new File(mDevice.RecoveryPath);
            switch (mDevice.getRecoveryType()) {
                case Device.PARTITION_TYPE_MTD:
                    MTD();
                    return true;
                case Device.PARTITION_TYPE_DD:
                    DD();
                    return true;
                case Device.PARTITION_TYPE_SONY:
                    SONY();
                    return true;
//            case Device.DEV_TYPE_MTK:
//                MTK();
//                return true;
            }
            return false;
        } else if (isJobKernel()) {
            CurrentPartition = new File(mDevice.KernelPath);
            switch (mDevice.getKernelType()) {
                case Device.PARTITION_TYPE_MTD:
                    MTD();
                    return true;
                case Device.PARTITION_TYPE_DD:
                    DD();
                    return true;
//            case Device.DEV_TYPE_MTK:
//                MTK();
//                return true;
            }
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
        } else {
            if (isJobFlash() || isJobRestore()) {
                Log.i(TAG, "Flash finished");
                if (!Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_HIDE_REBOOT)) {
                    showRebootDialog();
                } else {
                    if (!keepAppOpen) {
                        System.exit(0);
                    }
                }
            } else if (JOB == JOB_BACKUP_KERNEL || JOB == JOB_BACKUP_RECOVERY) {
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
        if (RunAtEnd != null) {
            RunAtEnd.run();
        }
    }

    public String DD() throws FailedExecuteCommand {
        String Command = "";
        try {
            File busybox = new File(mContext.getFilesDir(), "busybox");
            Common.chmod(mShell, busybox, "744");
            if (isJobFlash() || isJobRestore()) {
                Log.i(TAG, "Flash started!");
                Common.copyFile(CustomIMG, tmpFile);
                Command = busybox.getAbsolutePath() + " dd if=\"" + tmpFile.getAbsolutePath() + "\" " +
                        "of=\"" + CurrentPartition.getAbsolutePath() + "\"";
            } else if (isJobBackup()) {
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
        if (isJobRecovery()) {
            Command = " recovery ";
        } else if(isJobKernel()) {
            Command = " boot ";
        }
        try {
            if (isJobFlash() || isJobRestore()) {
                File flash_image = mDevice.getFlash_image(mContext);
                Common.chmod(mShell, mDevice.getFlash_image(mContext), "741");
                Log.i(TAG, "Flash started!");
                Command = flash_image.getAbsolutePath() + Command +"\"" + tmpFile.getAbsolutePath() + "\"";
            } else if (isJobBackup()) {
                File dump_image = mDevice.getDump_image(mContext);
                Common.chmod(mShell, dump_image, "741");
                Log.i(TAG, "Backup started!");
                Command = dump_image.getAbsolutePath() + Command +"\"" + tmpFile.getAbsolutePath() + "\"";
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return mShell.execCommand(mContext, Command);
    }

        public String SONY() throws FailedExecuteCommand {
            String Command = "";
            try {
                if (mDevice.DEV_NAME.equals("c6603")
                        || mDevice.DEV_NAME.equals("c6602")
                        || mDevice.DEV_NAME.equals("montblanc")) {
                    if (isJobFlash() || isJobRestore()) {
                        Common.mountDir(CurrentPartition, "RW");
                        mShell.execCommand(mContext, "cat " + mDevice.getCharger().getAbsolutePath() +
                                " >> /system/bin/" + mDevice.getCharger().getName());
                        mShell.execCommand(mContext, "cat " + mDevice.getChargermon().getAbsolutePath() +
                                " >> /system/bin/" + mDevice.getChargermon().getName());
                        if (mDevice.DEV_NAME.equals("c6603")
                                || mDevice.DEV_NAME.equals("c6602")) {
                            mShell.execCommand(mContext, "cat " + mDevice.getRic().getAbsolutePath() +
                                    " >> /system/bin/" + mDevice.getRic().getName());
                            Common.chmod(mShell, mDevice.getRic(), "755");
                        }
                        Common.chmod(mShell, mDevice.getCharger(), "755");
                        Common.chmod(mShell, mDevice.getChargermon(), "755");
                        Common.chmod(mShell, CustomIMG, "644");
                        Common.mountDir(CurrentPartition, "RO");
                        Log.i(TAG, "Flash started!");
                        Command = "cat " + CustomIMG.getAbsolutePath() + " >> " + CurrentPartition.getAbsolutePath();
                    } else if (isJobBackup()) {
                        Log.i(TAG, "Backup started!");
                        Command = "cat " + CurrentPartition.getAbsolutePath() + " >> " + CustomIMG.getAbsolutePath();
                    }
                }
                return mShell.execCommand(mContext, Command);
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }

//    public String MTK() throws FailedExecuteCommand {
//        File busybox = new File(mContext.getFilesDir(), "busybox");
//
//        String Command = "";
//
//        switch (PARTITION) {
//            case RECOVERY:
//                if (JOB == JOB_FLASH || JOB == JOB_RESTORE) {
//                    Command = busybox.getAbsolutePath() + " dd if=\"" + CustomIMG.getAbsolutePath() +
//                            "\" of=\"" + CurrentPartition.getAbsolutePath() +
//                            "\" count=" + Integer.parseInt(mDevice.MTK_Recovery_Length_HEX, 16) +
//                            " seek=" + Integer.parseInt(mDevice.MTK_Recovery_START_HEX, 16) + " conv=notrunc";
//                } else if (JOB == JOB_BACKUP) {
//                    Command = busybox.getAbsolutePath() + " dd if=\"" + CurrentPartition.getAbsolutePath() +
//                            "\" of=\"" + CustomIMG.getAbsolutePath() +
//                            "\" count=" + Integer.parseInt(mDevice.MTK_Recovery_Length_HEX, 16) +
//                            " seek=" + Integer.parseInt(mDevice.MTK_Recovery_START_HEX, 16) + " conv=notrunc";
//                }
//                break;
//            case KERNEL:
//                if (JOB == JOB_FLASH || JOB == JOB_RESTORE) {
//                    Command = busybox.getAbsolutePath() + " dd if=\"" + CustomIMG.getAbsolutePath() +
//                            "\" of=\"" + CurrentPartition.getAbsolutePath() +
//                            "\" count=" + Integer.parseInt(mDevice.MTK_Kernel_Length_HEX, 16) +
//                            " seek=" + Integer.parseInt(mDevice.MTK_Kernel_START_HEX, 16) + " conv=notrunc";
//                } else if (JOB == JOB_BACKUP) {
//                    Command = busybox.getAbsolutePath() + " dd if=\"" + CurrentPartition.getAbsolutePath() +
//                            "\" of=\"" + CustomIMG.getAbsolutePath() +
//                            "\" count=" + Integer.parseInt(mDevice.MTK_Kernel_Length_HEX, 16) +
//                            " seek=" + Integer.parseInt(mDevice.MTK_Kernel_START_HEX, 16) + " conv=notrunc";
//                }
//                break;
//        }
//
//        return mShell.execCommand(mContext, Command);
//    }

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
        if (isJobFlash()) {
            String counter = "", history = "";
            if (isJobKernel()) {
                counter = PREF_KEY_FLASH_KERNEL_COUNTER;
                history = RecoveryTools.PREF_KEY_KERNEL_HISTORY;
            } else if (isJobRecovery()) {
                counter = PREF_KEY_FLASH_RECOVERY_COUNTER;
                history = RecoveryTools.PREF_KEY_RECOVERY_HISTORY;
            }
            switch (Common.getIntegerPref(mContext, RecoveryTools.PREF_NAME, counter)) {
                case 0:
                    Common.setStringPref(mContext, RecoveryTools.PREF_NAME, history +
                            String.valueOf(Common.getIntegerPref(mContext, RecoveryTools.PREF_NAME, counter)),
                            CustomIMG.getAbsolutePath());
                    Common.setIntegerPref(mContext, RecoveryTools.PREF_NAME, counter, 1);
                    return;
                default:
                    Common.setStringPref(mContext, RecoveryTools.PREF_NAME, history +
                            String.valueOf(Common.getIntegerPref(mContext, RecoveryTools.PREF_NAME, counter)),
                            CustomIMG.getAbsolutePath());
                    Common.setIntegerPref(mContext, RecoveryTools.PREF_NAME, counter,
                            Common.getIntegerPref(mContext, RecoveryTools.PREF_NAME, counter) + 1);
                    if (Common.getIntegerPref(mContext, RecoveryTools.PREF_NAME, counter) == 5) {
                        Common.setIntegerPref(mContext, RecoveryTools.PREF_NAME, counter, 0);
                    }
            }
        }
    }

    public void setKeepAppOpen(boolean keepAppOpen) {
        this.keepAppOpen = keepAppOpen;
    }

    public boolean isJobFlash() {
        return JOB == JOB_FLASH_RECOVERY || JOB == JOB_FLASH_KERNEL;
    }

    public boolean isJobRestore() {
        return JOB == JOB_RESTORE_KERNEL || JOB == JOB_RESTORE_RECOVERY;
    }

    public boolean isJobBackup() {
        return JOB == JOB_BACKUP_RECOVERY || JOB == JOB_BACKUP_KERNEL;
    }

    public boolean isJobKernel() {
        return JOB == JOB_BACKUP_KERNEL || JOB == JOB_RESTORE_KERNEL || JOB == JOB_FLASH_KERNEL;
    }

    public boolean isJobRecovery() {
        return JOB == JOB_BACKUP_RECOVERY || JOB == JOB_RESTORE_RECOVERY || JOB ==JOB_FLASH_RECOVERY;
    }

    public void setRunAtEnd(Runnable RunAtEnd) {
        this.RunAtEnd = RunAtEnd;
    }

}