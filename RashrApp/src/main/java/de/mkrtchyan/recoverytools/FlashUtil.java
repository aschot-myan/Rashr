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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.Toolbox;
import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Notifyer;

public class FlashUtil extends AsyncTask<Void, Void, Boolean> {

    public static final int JOB_FLASH_RECOVERY = 1;
    public static final int JOB_BACKUP_RECOVERY = 2;
    public static final int JOB_RESTORE_RECOVERY = 3;
    public static final int JOB_FLASH_KERNEL = 4;
    public static final int JOB_BACKUP_KERNEL = 5;
    public static final int JOB_RESTORE_KERNEL = 6;
    public static final String TAG = "FlashUtil";
    public static final String PREF_NAME = "FlashUtil";
    public static final String PREF_KEY_HIDE_REBOOT = "hide_reboot";
    public static final String PREF_KEY_FLASH_RECOVERY_COUNTER = "last_recovery_counter";
    public static final String PREF_KEY_FLASH_KERNEL_COUNTER = "last_kernel_counter";
    private final Context mContext;
    private final Device mDevice;
    final private Shell mShell;
    final private Toolbox mToolbox;
    private final int JOB;
    private final File CustomIMG, busybox, flash_image, dump_image;
    private ProgressDialog pDialog;
    private File tmpFile, CurrentPartition;
    private boolean keepAppOpen = true;
    private Runnable RunAtEnd;

    private ArrayList<String> ERRORS = new ArrayList<String>();

    private Exception mException = null;

    public FlashUtil(Shell mShell, Context mContext, Device mDevice, File CustomIMG, int JOB) {
        this.mShell = mShell;
        this.mContext = mContext;
        this.mDevice = mDevice;
        this.JOB = JOB;
        this.CustomIMG = CustomIMG;
        mToolbox = new Toolbox(mShell);
        busybox = new File(mContext.getFilesDir(), "busybox");
        flash_image = mDevice.getFlash_image();
        dump_image = mDevice.getDump_image();
        tmpFile = new File(mContext.getFilesDir(), CustomIMG.getName());
    }

    protected void onPreExecute() {
        pDialog = new ProgressDialog(mContext);

        setBinaryPermissions();

        if (isJobFlash()) {
            pDialog.setTitle(R.string.flashing);
            Log.i(TAG, "Preparing to flash");
        } else if (isJobBackup()) {
            pDialog.setTitle(R.string.creating_bak);
            Log.i(TAG, "Preparing to backup");
        } else if (isJobRestore()) {
            pDialog.setTitle(R.string.restoring);
            Log.i(TAG, "Preparing to restore");
        }

        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage(CustomIMG.getName());
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            int PartitionType = 0;
            if (isJobRecovery()) {
                PartitionType = mDevice.getRecoveryType();
                CurrentPartition = new File(mDevice.getRecoveryPath());
            } else if (isJobKernel()) {
                PartitionType = mDevice.getKernelType();
                CurrentPartition = new File(mDevice.getKernelPath());
            }

            switch (PartitionType) {
                case Device.PARTITION_TYPE_MTD:
                    MTD();
                    break;
                case Device.PARTITION_TYPE_DD:
                    DD();
                    break;
                case Device.PARTITION_TYPE_SONY:
                    SONY();
                    break;
            }
            saveHistory();
            return true;
        } catch (Exception e) {
            mException = e;
            return false;
        }
    }

    protected void onPostExecute(Boolean success) {
        pDialog.dismiss();
        if (!success || mException != null) {
            Notifyer.showExceptionToast(mContext, TAG, mException);
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
            }
        }
        tmpFile.delete();
        if (RunAtEnd != null) RunAtEnd.run();
    }

    public void DD() throws FailedExecuteCommand, IOException {
        String Command = "";
        if (isJobFlash() || isJobRestore()) {
            if (mDevice.getName().startsWith("g2") && Build.MANUFACTURER.equals("lge")
                    && isJobFlash()) {
                File aboot = new File("/dev/block/platform/msm_sdcc.1/by-name/aboot");
                File extracted_aboot = new File(mContext.getFilesDir(), "aboot.img");
                File patched_CustomIMG = new File(mContext.getFilesDir(), CustomIMG.getName() + ".lok");
                File loki_patch = new File(mContext.getFilesDir(), "loki_patch");
                File loki_flash = new File(mContext.getFilesDir(), "loki_flash");
                mShell.execCommand("dd if=" + aboot.getAbsolutePath() + " of=" + extracted_aboot.getAbsolutePath(), true);
                mShell.execCommand(loki_patch.getAbsolutePath() + " recovery "
                        + CustomIMG.getAbsolutePath() + " " + patched_CustomIMG.getAbsolutePath() + "  || exit 1", true);
                Command = loki_flash.getAbsolutePath() + " recovery " + patched_CustomIMG.getAbsolutePath() + " || exit 1";
            } else {
                Log.i(TAG, "Flash started!");
                Common.copyFile(CustomIMG, tmpFile);
                Command = busybox.getAbsolutePath() + " dd if=\"" + tmpFile.getAbsolutePath() + "\" " +
                        "of=\"" + CurrentPartition.getAbsolutePath() + "\"";
            }
        } else if (isJobBackup()) {
            Log.i(TAG, "Backup started!");

            Command = busybox.getAbsolutePath() + " dd if=\"" + CurrentPartition.getAbsolutePath() + "\" " +
                    "of=\"" + tmpFile.getAbsolutePath() + "\"";
        }
        mShell.execCommand(Command, true);
        if (isJobBackup()) placeImgBack();
    }

    public void MTD() throws FailedExecuteCommand, IOException {
        String Command = "";
        if (isJobRecovery()) {
            Command = " recovery ";
        } else if (isJobKernel()) {
            Command = " boot ";
        }
        if (isJobFlash() || isJobRestore()) {
            Log.i(TAG, "Flash started!");
            Command = flash_image.getAbsolutePath() + Command + "\"" + tmpFile.getAbsolutePath() + "\"";
        } else if (isJobBackup()) {
            Log.i(TAG, "Backup started!");
            Command = dump_image.getAbsolutePath() + Command + "\"" + tmpFile.getAbsolutePath() + "\"";
        }
        mShell.execCommand(Command, true);
        if (isJobBackup()) placeImgBack();
    }

    public void SONY() throws FailedExecuteCommand, IOException {

        String Command = "";
        if (mDevice.getName().equals("yuga")
                || mDevice.getName().equals("c6602")
                || mDevice.getName().equals("montblanc")) {
            if (isJobFlash() || isJobRestore()) {
                File charger = new File(Rashr.PathToUtils, "charger");
                File chargermon = new File(Rashr.PathToUtils, "chargermon");
                File ric = new File(Rashr.PathToUtils, "ric");
                mToolbox.remount(CurrentPartition, "RW");
                try {
                    mToolbox.copyFile(charger, CurrentPartition.getParentFile(), true, false);
                    mToolbox.copyFile(chargermon, CurrentPartition.getParentFile(), true, false);
                    if (mDevice.getName().equals("yuga")
                            || mDevice.getName().equals("c6602")) {
                        mToolbox.copyFile(ric, CurrentPartition.getParentFile(), true, false);
                        mToolbox.setFilePermissions(ric, "755");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ERRORS.add(e.toString());
                }
                mToolbox.setFilePermissions(charger, "755");
                mToolbox.setFilePermissions(chargermon, "755");
                mToolbox.setFilePermissions(CustomIMG, "644");
                mToolbox.remount(CurrentPartition, "RO");
                Log.i(TAG, "Flash started!");
                Command = "cat " + CustomIMG.getAbsolutePath() + " >> " + CurrentPartition.getAbsolutePath();
            } else if (isJobBackup()) {
                Log.i(TAG, "Backup started!");
                Command = "cat " + CurrentPartition.getAbsolutePath() + " >> " + CustomIMG.getAbsolutePath();
            }
        }
        mShell.execCommand(Command, true);
        if (isJobBackup()) placeImgBack();
    }

    private void setBinaryPermissions() {
        busybox.setExecutable(true);
        busybox.setReadable(true);
        flash_image.setExecutable(true);
        flash_image.setReadable(true);
        dump_image.setExecutable(true);
        dump_image.setReadable(true);
    }

    public void showRebootDialog() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.flashed)
                .setMessage(mContext.getString(R.string.reboot_recovery_now))
                .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        try {
                            mToolbox.reboot(Toolbox.REBOOT_RECOVERY);
                        } catch (FailedExecuteCommand e) {
                            Notifyer.showExceptionToast(mContext, TAG, e);
                        } catch (Exception e) {
                            e.printStackTrace();
                            ERRORS.add(e.toString());
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

    private void placeImgBack() throws IOException, FailedExecuteCommand {
        mToolbox.setFilePermissions(tmpFile, "666");
        Common.copyFile(tmpFile, CustomIMG);
    }

    public void saveHistory() {
        if (isJobFlash()) {
            String counter = "", history = "";
            if (isJobKernel()) {
                counter = PREF_KEY_FLASH_KERNEL_COUNTER;
                history = Rashr.PREF_KEY_KERNEL_HISTORY;
            } else if (isJobRecovery()) {
                counter = PREF_KEY_FLASH_RECOVERY_COUNTER;
                history = Rashr.PREF_KEY_RECOVERY_HISTORY;
            }
            switch (Common.getIntegerPref(mContext, Rashr.PREF_NAME, counter)) {
                case 0:
                    Common.setStringPref(mContext, Rashr.PREF_NAME, history +
                                    String.valueOf(Common.getIntegerPref(mContext, Rashr.PREF_NAME, counter)),
                            CustomIMG.getAbsolutePath()
                    );
                    Common.setIntegerPref(mContext, Rashr.PREF_NAME, counter, 1);
                    return;
                default:
                    Common.setStringPref(mContext, Rashr.PREF_NAME, history +
                                    String.valueOf(Common.getIntegerPref(mContext, Rashr.PREF_NAME, counter)),
                            CustomIMG.getAbsolutePath()
                    );
                    Common.setIntegerPref(mContext, Rashr.PREF_NAME, counter,
                            Common.getIntegerPref(mContext, Rashr.PREF_NAME, counter) + 1);
                    if (Common.getIntegerPref(mContext, Rashr.PREF_NAME, counter) == 5) {
                        Common.setIntegerPref(mContext, Rashr.PREF_NAME, counter, 0);
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
        return JOB == JOB_BACKUP_RECOVERY || JOB == JOB_RESTORE_RECOVERY || JOB == JOB_FLASH_RECOVERY;
    }

    public void setRunAtEnd(Runnable RunAtEnd) {
        this.RunAtEnd = RunAtEnd;
    }

    public ArrayList<String> getERRORS() {
        return ERRORS;
    }

}