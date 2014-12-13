package de.mkrtchyan.recoverytools;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.Toolbox;
import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;

import java.io.File;
import java.io.IOException;

import de.mkrtchyan.utils.Common;

/**
 * Copyright (c) 2014 Aschot Mkrtchyan
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class FlashUtil extends AsyncTask<Void, Void, Boolean> {

    public static final int JOB_FLASH_RECOVERY = 1;
    public static final int JOB_BACKUP_RECOVERY = 2;
    public static final int JOB_RESTORE_RECOVERY = 3;
    public static final int JOB_FLASH_KERNEL = 4;
    public static final int JOB_BACKUP_KERNEL = 5;
    public static final int JOB_RESTORE_KERNEL = 6;
    public static final String PREF_NAME = "FlashUtil";
    public static final String PREF_KEY_HIDE_REBOOT = "hide_reboot";
    public static final String PREF_KEY_FLASH_COUNTER = "last_counter";
    private final RashrActivity mActivity;
    private final Context mContext;
    private final Device mDevice;
    final private Shell mShell;
    final private Toolbox mToolbox;
    private final int mJOB;
    private final File mCustomIMG, mBusybox, flash_image, dump_image;
    private ProgressDialog pDialog;
    private File tmpFile, CurrentPartition;
    private boolean keepAppOpen = true;
    private Runnable RunAtEnd;

    private Exception mException = null;

    public FlashUtil(RashrActivity activity, File CustomIMG, int job) {
        mActivity = activity;
        mShell = activity.getShell();
        mContext = activity;
        mDevice = activity.getDevice();
        mJOB = job;
        mCustomIMG = CustomIMG;
        mToolbox = activity.getToolbox();
        mBusybox = new File(mContext.getFilesDir(), "busybox");
        flash_image = mDevice.getFlash_image();
        dump_image = mDevice.getDump_image();
        tmpFile = new File(mContext.getFilesDir(), CustomIMG.getName());
    }

    protected void onPreExecute() {
        pDialog = new ProgressDialog(mContext);

        try {
            setBinaryPermissions();
            if (isJobFlash()) {
                pDialog.setTitle(R.string.flashing);
            } else if (isJobBackup()) {
                pDialog.setTitle(R.string.creating_bak);
            } else if (isJobRestore()) {
                pDialog.setTitle(R.string.restoring);
            }

            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setMessage(mCustomIMG.getName());
            pDialog.setCancelable(false);
            pDialog.show();
        } catch (FailedExecuteCommand e) {
            mActivity.addError(Constants.FLASH_UTIL_TAG, e, true);
        }


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
        if (!success) {
            if (mException != null) {
                mActivity.addError(Constants.FLASH_UTIL_TAG, mException, true);
            }
        } else if (tmpFile.delete()) {
            if (RunAtEnd != null) RunAtEnd.run();
            if (isJobFlash() || isJobRestore()) {
                if (!Common.getBooleanPref(mContext, PREF_NAME, PREF_KEY_HIDE_REBOOT)) {
                    showRebootDialog();
                } else {
                    if (!keepAppOpen) {
                        System.exit(0);
                    }
                }
            }
        }
    }

    public void DD() throws FailedExecuteCommand, IOException {
        String Command = "";
        if (isJobFlash() || isJobRestore()) {
            if (mDevice.getName().startsWith("g2") && Build.MANUFACTURER.equals("lge")
                    && isJobFlash()) {
                File aboot = new File("/dev/block/platform/msm_sdcc.1/by-name/aboot");
                File extracted_aboot = new File(mContext.getFilesDir(), "aboot.img");
                File patched_CustomIMG = new File(mContext.getFilesDir(), mCustomIMG.getName() + ".lok");
                File loki_patch = new File(mContext.getFilesDir(), "loki_patch");
                File loki_flash = new File(mContext.getFilesDir(), "loki_flash");
                mShell.execCommand("dd if=" + aboot.getAbsolutePath() + " of=" + extracted_aboot.getAbsolutePath(), true);
                mShell.execCommand(loki_patch.getAbsolutePath() + " recovery "
                        + mCustomIMG.getAbsolutePath() + " " + patched_CustomIMG.getAbsolutePath() + "  || exit 1", true);
                Command = loki_flash.getAbsolutePath() + " recovery " + patched_CustomIMG.getAbsolutePath() + " || exit 1";
            } else {
                Common.copyFile(mCustomIMG, tmpFile);
                Command = mBusybox.getAbsolutePath() + " dd if=\"" + tmpFile.getAbsolutePath() + "\" " +
                        "of=\"" + CurrentPartition.getAbsolutePath() + "\"";
            }
        } else if (isJobBackup()) {

            Command = mBusybox.getAbsolutePath() + " dd if=\"" + CurrentPartition.getAbsolutePath() + "\" " +
                    "of=\"" + tmpFile.getAbsolutePath() + "\"";
        }
        mShell.execCommand(Command, true);
        if (isJobBackup()) placeImgBack();
    }

    public void MTD() throws FailedExecuteCommand, IOException {
        String Command;
        if (isJobRecovery()) {
            Command = " recovery ";
        } else if (isJobKernel()) {
            Command = " boot ";
        } else {
            return;
        }
        if (isJobFlash() || isJobRestore()) {
            Command = flash_image.getAbsolutePath() + Command + "\"" + tmpFile.getAbsolutePath() + "\"";
        } else if (isJobBackup()) {
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
                File charger = new File(Constants.PathToUtils, "charger");
                File chargermon = new File(Constants.PathToUtils, "chargermon");
                File ric = new File(Constants.PathToUtils, "ric");
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
                    mActivity.addError(Constants.FLASH_UTIL_TAG, e, true);
                }
                mToolbox.setFilePermissions(charger, "755");
                mToolbox.setFilePermissions(chargermon, "755");
                mToolbox.setFilePermissions(mCustomIMG, "644");
                mToolbox.remount(CurrentPartition, "RO");
                Command = "cat " + mCustomIMG.getAbsolutePath() + " >> " + CurrentPartition.getAbsolutePath();
            } else if (isJobBackup()) {
                Command = "cat " + CurrentPartition.getAbsolutePath() + " >> " + mCustomIMG.getAbsolutePath();
            }
        }
        mShell.execCommand(Command, true);
        if (isJobBackup()) placeImgBack();
    }

    private void setBinaryPermissions() throws FailedExecuteCommand {
        mToolbox.setFilePermissions(mBusybox, "755");
		try {
			mToolbox.setFilePermissions(flash_image, "755");
		} catch (FailedExecuteCommand e) {
			mToolbox.remount(flash_image, "rw");
			mToolbox.setFilePermissions(flash_image, "755");
			mToolbox.remount(flash_image, "ro");
		}
		try {
			mToolbox.setFilePermissions(dump_image, "755");
		} catch (FailedExecuteCommand e) {
			mToolbox.remount(dump_image, "rw");
			mToolbox.setFilePermissions(dump_image, "755");
			mToolbox.remount(dump_image, "ro");
		}
    }

    public void showRebootDialog() {
	    int Message;
	    final int REBOOT_JOB;
	    if (isJobKernel()) {
		    Message = R.string.reboot_now;
		    REBOOT_JOB = Toolbox.REBOOT_REBOOT;
	    } else {
		    Message = R.string.reboot_recovery_now;
		    REBOOT_JOB = Toolbox.REBOOT_RECOVERY;
	    }

        new AlertDialog.Builder(mContext)
                .setTitle(R.string.flashed)
                .setMessage(Message)
                .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
	                @Override
	                public void onClick(DialogInterface dialogInterface, int i) {

		                try {
			                mToolbox.reboot(REBOOT_JOB);
		                } catch (Exception e) {
                            mActivity.addError(Constants.FLASH_UTIL_TAG, e, false);
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
        Common.copyFile(tmpFile, mCustomIMG);
    }

    public void saveHistory() {
        if (isJobFlash()) {
            switch (Common.getIntegerPref(mContext, Constants.PREF_NAME, PREF_KEY_FLASH_COUNTER)) {
                case 0:
                    Common.setStringPref(mContext, Constants.PREF_NAME, Constants.PREF_KEY_HISTORY +
                                    String.valueOf(Common.getIntegerPref(mContext, Constants.PREF_NAME,
                                            PREF_KEY_FLASH_COUNTER)),
                            mCustomIMG.getAbsolutePath()
                    );
                    Common.setIntegerPref(mContext, Constants.PREF_NAME, PREF_KEY_FLASH_COUNTER, 1);
                    return;
                default:
                    Common.setStringPref(mContext, Constants.PREF_NAME, Constants.PREF_KEY_HISTORY +
                                    String.valueOf(Common.getIntegerPref(mContext, Constants.PREF_NAME,
                                            PREF_KEY_FLASH_COUNTER)),
                            mCustomIMG.getAbsolutePath()
                    );
                    Common.setIntegerPref(mContext, Constants.PREF_NAME, PREF_KEY_FLASH_COUNTER,
                            Common.getIntegerPref(mContext, Constants.PREF_NAME, PREF_KEY_FLASH_COUNTER) + 1);
                    if (Common.getIntegerPref(mContext, Constants.PREF_NAME, PREF_KEY_FLASH_COUNTER) == 5) {
                        Common.setIntegerPref(mContext, Constants.PREF_NAME, PREF_KEY_FLASH_COUNTER, 0);
                    }
            }
        }
    }

    public void setKeepAppOpen(boolean keepAppOpen) {
        this.keepAppOpen = keepAppOpen;
    }

    public boolean isJobFlash() {
        return mJOB == JOB_FLASH_RECOVERY || mJOB == JOB_FLASH_KERNEL;
    }

    public boolean isJobRestore() {
        return mJOB == JOB_RESTORE_KERNEL || mJOB == JOB_RESTORE_RECOVERY;
    }

    public boolean isJobBackup() {
        return mJOB == JOB_BACKUP_RECOVERY || mJOB == JOB_BACKUP_KERNEL;
    }

    public boolean isJobKernel() {
        return mJOB == JOB_BACKUP_KERNEL || mJOB == JOB_RESTORE_KERNEL || mJOB == JOB_FLASH_KERNEL;
    }

    public boolean isJobRecovery() {
        return mJOB == JOB_BACKUP_RECOVERY || mJOB == JOB_RESTORE_RECOVERY || mJOB == JOB_FLASH_RECOVERY;
    }

    public void setRunAtEnd(Runnable RunAtEnd) {
        this.RunAtEnd = RunAtEnd;
    }
}