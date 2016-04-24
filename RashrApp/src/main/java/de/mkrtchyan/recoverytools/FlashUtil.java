package de.mkrtchyan.recoverytools;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;

import org.sufficientlysecure.rootcommands.Toolbox;
import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Unzipper;

/**
 * Copyright (c) 2015 Aschot Mkrtchyan
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
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
    public static final int JOB_INSTALL_XZDUAL = 7;
    private final Context mContext;
    private final int mJOB;
    private final File mCustomIMG, flash_image, dump_image;
    private ProgressDialog pDialog;
    private File tmpFile, mPartition;
    private OnTaskDoneListener mOnTaskDoneListener;
    private Handler mHandler = new Handler();
    private Exception mException;

    public FlashUtil(Context context, File CustomIMG, int job) {
        mContext = context;
        mJOB = job;
        mCustomIMG = CustomIMG;
        flash_image = RashrApp.DEVICE.getFlash_image();
        dump_image = RashrApp.DEVICE.getDump_image();
        tmpFile = new File(mContext.getFilesDir(), CustomIMG.getName());
        if (isJobRecovery()) {
            mPartition = new File(RashrApp.DEVICE.getRecoveryPath());
        } else if (isJobKernel()) {
            mPartition = new File(RashrApp.DEVICE.getKernelPath());
        }
    }

    public static void uninstallXZDual() throws FailedExecuteCommand {
        RashrApp.SHELL.execCommand("rm /system/bin/recovery.twrp.cpio*");
        RashrApp.SHELL.execCommand("rm /system/bin/recovery.cwm.cpio*");
        RashrApp.SHELL.execCommand("rm /system/bin/recovery.philz.cpio*");
        RashrApp.SHELL.execCommand("rm /system/bin/charger");
        RashrApp.SHELL.execCommand("rm /system/bin/ric");
        RashrApp.SHELL.execCommand("rm /system/bin/chargermon");
        RashrApp.SHELL.execCommand("rm /system/bin/dualrecovery.sh");
        RashrApp.SHELL.execCommand("mv /system/bin/charger.stock /system/bin/charger");
        RashrApp.SHELL.execCommand("mv /system/bin/ric.stock /system/bin/ric");
        RashrApp.SHELL.execCommand("mv /system/bin/chargermon.stock /system/bin/chargermon");
        RashrApp.SHELL.execCommand("chmod 755 /system/bin/charger");
        RashrApp.SHELL.execCommand("chmod 755 /system/bin/ric");
        RashrApp.SHELL.execCommand("chmod 755 /system/bin/chargermon");
    }

    protected void onPreExecute() {
        pDialog = new ProgressDialog(mContext);

        try {

            RashrApp.TOOLBOX.remount("/system", "rw");
            if (!isJobXZDual()) {
                if ((isJobRecovery() && RashrApp.DEVICE.isRecoveryMTD())
                        || isJobKernel() && RashrApp.DEVICE.isKernelMTD()) {
                    RashrApp.SHELL.execCommand(Const.Busybox + " chmod 755 " + flash_image);
                    RashrApp.SHELL.execCommand(Const.Busybox + " chmod 755 " + dump_image);
                }
            }

        } catch (FailedExecuteCommand e) {
            RashrApp.ERRORS.add(Const.FLASH_UTIL_TAG +
                    " Failed to set permissions to flash_image and dump_image before flashing: " + e);
        }
        if (isJobFlash()) {
            pDialog.setTitle(R.string.flashing);
        } else if (isJobBackup()) {
            pDialog.setTitle(R.string.creating_bak);
        } else if (isJobRestore()) {
            pDialog.setTitle(R.string.restoring);
        } else if (isJobXZDual()) {
            pDialog.setTitle(R.string.installing_xzdual);
        }
        if (isJobBackup() && (isJobRecovery() ? RashrApp.DEVICE.isRecoveryDD() : RashrApp.DEVICE.isKernelDD())) {
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setMax(getSizeOfFile(mPartition));
        } else {
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        pDialog.setMessage(mCustomIMG.getName());
        pDialog.setCancelable(false);
        pDialog.show();

    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (!Common.getBooleanPref(mContext, Const.PREF_NAME, Const.PREF_KEY_SKIP_IMAGE_CHECK)) {
            if (!isJobBackup()) {
                if (mCustomIMG.toString().endsWith(Device.EXT_IMG)) {
                    if (!isImageValid(mCustomIMG)) {
                        mException = new ImageNotValidException(mCustomIMG);
                        return false;
                    }
                }
            }
        }
        if (isJobXZDual()) {
            try {
                installXZDual();
            } catch (FailedExecuteCommand failedExecuteCommand) {
                failedExecuteCommand.printStackTrace();
                mException = failedExecuteCommand;
                return false;
            }
            return true;
        }
        int PartitionType = 0;
        if (isJobRecovery()) {
            PartitionType = RashrApp.DEVICE.getRecoveryType();
        } else if (isJobKernel()) {
            PartitionType = RashrApp.DEVICE.getKernelType();
        }
        try {
            switch (PartitionType) {
                case Device.PARTITION_TYPE_MTD:
                    MTD();
                    break;
                case Device.PARTITION_TYPE_DD:
                    DD();
                    break;
                //case Device.PARTITION_TYPE_SONY:
                //    SONY();
                //    break;
                default:
                    return false;
            }
        } catch (FailedExecuteCommand | IOException | ImageToBigException e) {
            mException = e;
        }
        saveHistory();
        /** If there is not any exception everything was ok */
        return mException == null;
    }

    protected void onPostExecute(Boolean success) {
        pDialog.dismiss();
        tmpFile.delete();
        RashrApp.TOOLBOX.remount("/system", "ro");
        if (success) {
            if (isJobFlash() || isJobRestore()) {
                if (!Common.getBooleanPref(mContext, Const.PREF_NAME, Const.PREF_KEY_HIDE_REBOOT)) {
                    showRebootDialog();
                }
            }
            if (mOnTaskDoneListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mOnTaskDoneListener.onSuccess();
                    }
                });
            }
        } else {
            if (mOnTaskDoneListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mOnTaskDoneListener.onFail(mException);
                    }
                });
            }
        }
    }

    public void DD() throws FailedExecuteCommand, IOException, ImageToBigException {
        Thread observer;

        if (isJobBackup() && (isJobRecovery() ? RashrApp.DEVICE.isRecoveryDD() : RashrApp.DEVICE.isKernelDD())) {
            observer = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            final int progress = Common.safeLongToInt(tmpFile.length());
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    pDialog.setProgress(progress);
                                }
                            });
                            if (progress >= pDialog.getMax()) break;
                        } catch (IllegalArgumentException e) {
                            RashrApp.ERRORS.add(Const.FLASH_UTIL_TAG +
                                    " Error in ObserverThread for progress display" +
                                    " while flashing or creating backup");
                            pDialog.setProgress(pDialog.getMax());
                            break;
                        }
                    }
                }
            });
            observer.start();
        }
        if (!Common.getBooleanPref(mContext, Const.PREF_NAME, Const.PREF_KEY_SKIP_SIZE_CHECK)) {
            if (isJobFlash()) {
                int customSize = getSizeOfFile(mCustomIMG);
                int partitionSize = getSizeOfFile(mPartition);
                /** ERROR on some chinese devices. Partition size always 0 */
                if (partitionSize != 0) {
                    if (customSize > partitionSize) {
                        throw new ImageToBigException(customSize, partitionSize);
                    }
                }
            }
        }

        String Command = "";
        if (isJobFlash() || isJobRestore()) {
            if (RashrApp.DEVICE.isLoki() && isJobFlash()) {
                Command = lokiPatch();
            } else {
                Common.copyFile(mCustomIMG, tmpFile);
                Command = Const.Busybox + " dd if=\"" + tmpFile + "\" of=\"" + mPartition + "\"";
                if ((isJobRecovery() ? RashrApp.DEVICE.getRecoveryBlocksize() : RashrApp.DEVICE.getKernelBlocksize()) > 0) {
                    String bs = " bs="
                            + (isJobRecovery() ? RashrApp.DEVICE.getRecoveryBlocksize() : RashrApp.DEVICE.getKernelBlocksize());
                    Command += bs;
                }
            }
        } else if (isJobBackup()) {
            Command = Const.Busybox + " dd if=\"" + mPartition + "\" of=\"" + tmpFile + "\"";
        }
        RashrApp.SHELL.execCommand(Command);
        if (isJobBackup()) placeImgBack();
    }

    /*public void SONY() throws FailedExecuteCommand, IOException {

        String Command = "";
        if (mDevice.getName().equals("yuga")
                || mDevice.getName().equals("c6602")
                || mDevice.getName().equals("montblanc")) {
            if (isJobFlash() || isJobRestore()) {
                File charger = new File(Const.PathToUtils, "charger");
                File chargermon = new File(Const.PathToUtils, "chargermon");
                File ric = new File(Const.PathToUtils, "ric");
                mToolbox.remount(mPartition, "RW");
                try {
                    mToolbox.copyFile(charger, mPartition.getParentFile(), true, false);
                    mToolbox.copyFile(chargermon, mPartition.getParentFile(), true, false);
                    if (mDevice.getName().equals("yuga")
                            || mDevice.getName().equals("c6602")) {
                        mToolbox.copyFile(ric, mPartition.getParentFile(), true, false);
                        mToolbox.setFilePermissions(ric, "755");
                    }
                } catch (Exception e) {
                    mActivity.addError(Const.FLASH_UTIL_TAG, e, true);
                }
                mToolbox.setFilePermissions(charger, "755");
                mToolbox.setFilePermissions(chargermon, "755");
                mToolbox.setFilePermissions(mCustomIMG, "644");
                mToolbox.remount(mPartition, "RO");
                Command = "cat " + mCustomIMG.getAbsolutePath() + " >> " + mPartition.getAbsolutePath();
            } else if (isJobBackup()) {
                Command = "cat " + mPartition.getAbsolutePath() + " >> " + mCustomIMG.getAbsolutePath();
            }
        }
        mShell.execCommand(Command, true);
        if (isJobBackup()) placeImgBack();
    }*/

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
            Command = flash_image.getAbsolutePath() + Command + "\"" + tmpFile + "\"";
        } else if (isJobBackup()) {
            Command = dump_image.getAbsolutePath() + Command + "\"" + tmpFile + "\"";
        }
        RashrApp.SHELL.execCommand(Command);
        if (isJobBackup()) placeImgBack();
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
                            RashrApp.TOOLBOX.reboot(REBOOT_JOB);
                        } catch (FailedExecuteCommand e) {
                            RashrApp.ERRORS.add(Const.FLASH_UTIL_TAG + " Device could not be rebooted: " + e);
                        }
                    }
                })
                .setNeutralButton(R.string.neutral, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setNegativeButton(R.string.never_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Common.setBooleanPref(mContext, Const.PREF_NAME, Const.PREF_KEY_HIDE_REBOOT,
                                true);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                    }
                })
                .show();
    }

    private void placeImgBack() throws FailedExecuteCommand {
        RashrApp.SHELL.execCommand(Const.Busybox + " chmod 777 \"" + tmpFile + "\"");
        RashrApp.SHELL.execCommand(Const.Busybox + " mv \"" + tmpFile + "\" \"" + mCustomIMG + "\"");
    }

    public void saveHistory() {
        if (isJobFlash()) {
            switch (Common.getIntegerPref(mContext, Const.PREF_NAME, Const.PREF_KEY_FLASH_COUNTER)) {
                case 0:
                    Common.setStringPref(mContext, Const.PREF_NAME, Const.PREF_KEY_HISTORY +
                                    String.valueOf(Common.getIntegerPref(mContext, Const.PREF_NAME,
                                            Const.PREF_KEY_FLASH_COUNTER)),
                            mCustomIMG.getAbsolutePath()
                    );
                    Common.setIntegerPref(mContext, Const.PREF_NAME, Const.PREF_KEY_FLASH_COUNTER, 1);
                    return;
                default:
                    Common.setStringPref(mContext, Const.PREF_NAME, Const.PREF_KEY_HISTORY +
                                    String.valueOf(Common.getIntegerPref(mContext, Const.PREF_NAME,
                                            Const.PREF_KEY_FLASH_COUNTER)),
                            mCustomIMG.getAbsolutePath()
                    );
                    Common.setIntegerPref(mContext, Const.PREF_NAME, Const.PREF_KEY_FLASH_COUNTER,
                            Common.getIntegerPref(mContext, Const.PREF_NAME, Const.PREF_KEY_FLASH_COUNTER) + 1);
                    if (Common.getIntegerPref(mContext, Const.PREF_NAME, Const.PREF_KEY_FLASH_COUNTER) == 5) {
                        Common.setIntegerPref(mContext, Const.PREF_NAME, Const.PREF_KEY_FLASH_COUNTER, 0);
                    }
            }
        }
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

    public boolean isJobXZDual() {
        return mJOB == JOB_INSTALL_XZDUAL;
    }

    public String lokiPatch() throws FailedExecuteCommand {
        File aboot = new File("/dev/block/platform/msm_sdcc.1/by-name/aboot");
        File extracted_aboot = new File(mContext.getFilesDir(), "aboot.img");
        File patched_CustomIMG = new File(mContext.getFilesDir(), mCustomIMG.getName() + ".lok");
        File loki_patch = new File(mContext.getFilesDir(), "loki_patch");
        File loki_flash = new File(mContext.getFilesDir(), "loki_flash");
        RashrApp.SHELL.execCommand("dd if=" + aboot + " of=" + extracted_aboot, true);
        RashrApp.SHELL.execCommand(loki_patch + " recovery " + mCustomIMG + " " + patched_CustomIMG +
                "  || exit 1", true);
        return loki_flash + " recovery " + patched_CustomIMG + " || exit 1";
    }

    public int getSizeOfFile(File path) {
        try {
            String output;
            output = RashrApp.SHELL.execCommand("wc -c " + path);
            return Integer.valueOf(output.split(" ")[0]);
        } catch (Exception e) {
            RashrApp.ERRORS.add(e.toString());
            e.printStackTrace();
        }
        return -1;
    }

    private void installXZDual() throws FailedExecuteCommand {
        Unzipper.unzip(mCustomIMG, new File("/tmp"));
        RashrApp.SHELL.execCommand("chmod 755 /tmp/backupstockbinaries.sh");
        try {
            RashrApp.SHELL.execCommand("./tmp/backupstockbinaries.sh");
        } catch (FailedExecuteCommand ignore) {

        }
        RashrApp.SHELL.execCommand("cp " + Const.Busybox + " /system/.XZDualRecovery/busybox");
        RashrApp.SHELL.execCommand("chmod 755 /tmp/tmp/installstock.sh");
        RashrApp.SHELL.execCommand("./tmp/tmp/installstock.sh");
        RashrApp.SHELL.execCommand("chmod 755 /tmp/tmp/installdisableric.sh");
        RashrApp.SHELL.execCommand("./tmp/tmp/installdisableric.sh");
        RashrApp.SHELL.execCommand("chmod 755 /tmp/tmp/installndrutils.sh");
        RashrApp.SHELL.execCommand("./tmp/tmp/installndrutils.sh");
        RashrApp.SHELL.execCommand("chmod 755 /tmp/tmp/setversion.sh");
        RashrApp.SHELL.execCommand("./tmp/tmp/setversion.sh");
        RashrApp.SHELL.execCommand("chmod 644 /system/bin/recovery.cwm.cpio.lzma");
        RashrApp.SHELL.execCommand("chmod 644 /system/bin/recovery.philz.cpio.lzma");
        RashrApp.SHELL.execCommand("chmod 644 /system/bin/recovery.twrp.cpio.lzma");
        RashrApp.SHELL.execCommand("chmod 755 /system/bin/mr");
        RashrApp.SHELL.execCommand("chmod 755 /system/bin/chargemon");
        RashrApp.SHELL.execCommand("chmod 755 /system/bin/dualrecovery.sh");
        RashrApp.SHELL.execCommand("chmod 755 /system/bin/rickiller.sh");
        RashrApp.SHELL.execCommand("chmod 755 /system/xbin/busybox");
    }

    public void setOnTaskDoneListener(OnTaskDoneListener onTaskDoneListener) {
        mOnTaskDoneListener = onTaskDoneListener;
    }

    private boolean isImageValid(File img) {
        boolean result = false;
        try {
            BufferedReader br = new BufferedReader(new FileReader(img));
            String line = br.readLine();
            result = line.contains("ANDROID!") || line.contains("init/=init");
        } catch (IOException ignore) {
        }
        return result;
    }

    interface OnTaskDoneListener {
        void onSuccess();

        void onFail(Exception e);
    }

    public class ImageNotValidException extends Exception {
        private String path = "";

        public ImageNotValidException(File file) {
            super(file + " is not a valid Image to flash");
            path = file.toString();
        }

        public String getPath() {
            return path;
        }
    }

    public class ImageToBigException extends Exception {
        private int mCustomSize;
        private int mPartitionSize;

        public ImageToBigException(int customSize, int partitionSize) {
            super("IMG is to big for your device! IMG Size: " +
                    customSize / (1024 * 1024) + "MB (" + customSize + ") Partition Size: " +
                    partitionSize / (1024 * 1024) + "MB (" + partitionSize + ")");
            mCustomSize = customSize;
            mPartitionSize = partitionSize;
        }

        public int getCustomSize() {
            return mCustomSize;
        }

        public int getPartitionSize() {
            return mPartitionSize;
        }
    }

}