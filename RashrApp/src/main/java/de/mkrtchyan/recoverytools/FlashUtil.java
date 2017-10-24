package de.mkrtchyan.recoverytools;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;

import org.sufficientlysecure.rootcommands.Toolbox;
import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Unzipper;

/**
 * Copyright (c) 2017 Aschot Mkrtchyan
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
    private OnFlashListener mOnFlashListener;
    private Handler mHandler = new Handler();
    private Exception mException;

    /**
     * FlashUtil handles the flashing to the device in background using a AsyncTask
     * and blocks the UI with a non-cancelable dialog
     *
     * @param context   AppContext
     * @param CustomIMG if Job is Backup so backup-path if Job is Flash so recovery or kernel image path
     * @param job       int describing what to do
     */
    public FlashUtil(Context context, File CustomIMG, int job) {
        mContext = context;
        mJOB = job;
        mCustomIMG = CustomIMG;
        flash_image = App.Device.getFlash_image();
        dump_image = App.Device.getDump_image();
        tmpFile = new File(mContext.getFilesDir(), CustomIMG.getName());
        if (isJobRecovery()) {
            mPartition = new File(App.Device.getRecoveryPath());
        } else if (isJobKernel()) {
            mPartition = new File(App.Device.getKernelPath());
        }
    }

    /**
     * Removes XZDual from System
     *
     * @throws FailedExecuteCommand error during removing files
     */
    public static void uninstallXZDual() throws FailedExecuteCommand {
        App.Shell.execCommand("rm /system/bin/recovery.twrp.cpio*");
        App.Shell.execCommand("rm /system/bin/recovery.cwm.cpio*");
        App.Shell.execCommand("rm /system/bin/recovery.philz.cpio*");
        App.Shell.execCommand("rm /system/bin/charger");
        App.Shell.execCommand("rm /system/bin/ric");
        App.Shell.execCommand("rm /system/bin/chargermon");
        App.Shell.execCommand("rm /system/bin/dualrecovery.sh");
        App.Shell.execCommand("mv /system/bin/charger.stock /system/bin/charger");
        App.Shell.execCommand("mv /system/bin/ric.stock /system/bin/ric");
        App.Shell.execCommand("mv /system/bin/chargermon.stock /system/bin/chargermon");
        App.Shell.execCommand("chmod 755 /system/bin/charger");
        App.Shell.execCommand("chmod 755 /system/bin/ric");
        App.Shell.execCommand("chmod 755 /system/bin/chargermon");
    }

    /**
     * Preparing to flash
     */
    protected void onPreExecute() {
        pDialog = new ProgressDialog(mContext);
        try {
            //Mounting System as read write
            App.Toolbox.remount("/system", "rw");
            if (!isJobXZDual()) {
                if ((isJobRecovery() && App.Device.isRecoveryMTD())
                        || isJobKernel() && App.Device.isKernelMTD()) {
                    //if partition is MTD so set execution mode to needed binary
                    App.Shell.execCommand(App.Busybox + " chmod 755 " + flash_image);
                    App.Shell.execCommand(App.Busybox + " chmod 755 " + dump_image);
                }
            }
        } catch (FailedExecuteCommand e) {
            App.ERRORS.add(App.TAG +
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
        } else {
            //job not recognized
            //Better throw a exception
            mException = new RuntimeException("FlashJob not recognized");
        }
        if (isJobBackup() && (isJobRecovery() ? App.Device.isRecoveryDD() : App.Device.isKernelDD())) {
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
        if (mException != null) {
            return false;
        }
        if (!App.Preferences.getBoolean(App.PREF_KEY_SKIP_IMAGE_CHECK, false)) {
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
            PartitionType = App.Device.getRecoveryType();
        } else if (isJobKernel()) {
            PartitionType = App.Device.getKernelType();
        }
        try {
            switch (PartitionType) {
                case Device.PARTITION_TYPE_MTD:
                    MTD();
                    break;
                case Device.PARTITION_TYPE_DD:
                    DD();
                    break;
                default:
                    mException = new Exception("Flash method unknown");
                    return false;
            }
        } catch (FailedExecuteCommand | IOException | ImageToBigException e) {
            mException = e;
        }
        saveHistory();
        /* If there is not any exception everything was ok */
        return mException == null;
    }

    protected void onPostExecute(Boolean success) {
        pDialog.dismiss();
        tmpFile.delete();
        //Mount system partition as read only
        App.Toolbox.remount("/system", "ro");
        if (success) {
            if (isJobFlash() || isJobRestore()) {
                //System changed let user choose if he wants to reboot
                if (!App.Preferences.getBoolean(App.PREF_KEY_HIDE_REBOOT, false)) {
                    showRebootDialog();
                }
            }
            if (mOnFlashListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mOnFlashListener.onSuccess();
                    }
                });
            }
        } else {
            if (mOnFlashListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mOnFlashListener.onFail(mException);
                    }
                });
            }
        }
    }

    public void DD() throws FailedExecuteCommand, IOException, ImageToBigException {
        Thread observer;

        if (isJobBackup() && (isJobRecovery() ? App.Device.isRecoveryDD() : App.Device.isKernelDD())) {
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
                            App.ERRORS.add(App.TAG +
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
        if (!App.Preferences.getBoolean(App.PREF_KEY_SKIP_SIZE_CHECK, false)) {
            if (isJobFlash()) {
                int customSize = getSizeOfFile(mCustomIMG);
                int partitionSize = getSizeOfFile(mPartition);
                /* ERROR on some chinese devices. Partition size always 0 */
                if (partitionSize != 0) {
                    if (customSize > partitionSize) {
                        throw new ImageToBigException(customSize, partitionSize);
                    }
                }
            }
        }

        String Command = "";
        if (isJobFlash() || isJobRestore()) {
            if (App.Device.isLoki() && isJobFlash()) {
                Command = lokiPatch();
            } else {
                Common.copyFile(mCustomIMG, tmpFile);
                Command = App.Busybox + " dd if=\"" + tmpFile + "\" of=\"" + mPartition.getAbsolutePath() + "\"";
                if ((isJobRecovery() ? App.Device.getRecoveryBlocksize() : App.Device.getKernelBlocksize()) > 0) {
                    String bs = " bs="
                            + (isJobRecovery() ? App.Device.getRecoveryBlocksize() : App.Device.getKernelBlocksize());
                    Command += bs;
                }
            }
        } else if (isJobBackup()) {
            Command = App.Busybox + " dd if=\"" + mPartition.getAbsolutePath() + "\" of=\"" + tmpFile + "\"";
        }
        App.Shell.execCommand(Command);
        if (isJobBackup()) placeImgBack();
    }

    public void MTD() throws FailedExecuteCommand {
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
        App.Shell.execCommand(Command);
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
                            App.Toolbox.reboot(REBOOT_JOB);
                        } catch (FailedExecuteCommand e) {
                            App.ERRORS.add(App.TAG + " Device could not be rebooted: " + e);
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
                        App.Preferences.edit().putBoolean(App.PREF_KEY_HIDE_REBOOT, true).apply();
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
        App.Shell.execCommand(App.Busybox + " chmod 777 \"" + tmpFile + "\"");
        App.Shell.execCommand(App.Busybox + " mv \"" + tmpFile + "\" \"" + mCustomIMG + "\"");
    }

    public void saveHistory() {
        if (isJobFlash()) {
            int count = App.Preferences.getInt(App.PREF_KEY_FLASH_COUNTER, 0);
            App.Preferences.edit().putString(App.PREF_KEY_HISTORY + count, mCustomIMG.toString()).apply();
            App.Preferences.edit().putInt(App.PREF_KEY_FLASH_COUNTER, (count + 1) % 5).apply();
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
        App.Shell.execCommand("dd if=\"" + aboot + "\" of=\"" + extracted_aboot + "\"", true);
        App.Shell.execCommand(loki_patch + " recovery " + mCustomIMG + " " + patched_CustomIMG +
                "  || exit 1", true);
        return loki_flash + " recovery " + patched_CustomIMG + " || exit 1";
    }

    public int getSizeOfFile(File path) {
        try {
            String output;
            //Workaround on some devices
            String command = "wc -c \"" + path.getAbsolutePath() + "\"";
            command = command.substring(0, command.indexOf("\n")) + "\"";
            output = App.Shell.execCommand(command);
            return Integer.valueOf(output.split(" ")[0]);
        } catch (Exception e) {
            App.ERRORS.add("wc -c \"" + path + "\"");
            App.ERRORS.add(e.toString());
            e.printStackTrace();
        }
        return -1;
    }

    private void installXZDual() throws FailedExecuteCommand {
        Unzipper.unzip(mCustomIMG, new File("/tmp"));
        App.Shell.execCommand("chmod 755 /tmp/backupstockbinaries.sh");
        try {
            App.Shell.execCommand("./tmp/backupstockbinaries.sh");
        } catch (FailedExecuteCommand ignore) {

        }
        App.Shell.execCommand("cp " + App.Busybox + " /system/.XZDualRecovery/busybox");
        App.Shell.execCommand("chmod 755 /tmp/tmp/installstock.sh");
        App.Shell.execCommand("./tmp/tmp/installstock.sh");
        App.Shell.execCommand("chmod 755 /tmp/tmp/installdisableric.sh");
        App.Shell.execCommand("./tmp/tmp/installdisableric.sh");
        App.Shell.execCommand("chmod 755 /tmp/tmp/installndrutils.sh");
        App.Shell.execCommand("./tmp/tmp/installndrutils.sh");
        App.Shell.execCommand("chmod 755 /tmp/tmp/setversion.sh");
        App.Shell.execCommand("./tmp/tmp/setversion.sh");
        App.Shell.execCommand("chmod 644 /system/bin/recovery.cwm.cpio.lzma");
        App.Shell.execCommand("chmod 644 /system/bin/recovery.philz.cpio.lzma");
        App.Shell.execCommand("chmod 644 /system/bin/recovery.twrp.cpio.lzma");
        App.Shell.execCommand("chmod 755 /system/bin/mr");
        App.Shell.execCommand("chmod 755 /system/bin/chargemon");
        App.Shell.execCommand("chmod 755 /system/bin/dualrecovery.sh");
        App.Shell.execCommand("chmod 755 /system/bin/rickiller.sh");
        App.Shell.execCommand("chmod 755 /system/xbin/busybox");
    }

    public void setOnFlashListener(OnFlashListener onFlashListener) {
        mOnFlashListener = onFlashListener;
    }

    private boolean isImageValid(File img) {
        boolean result = false;
        try {
            BufferedReader br = new BufferedReader(new FileReader(img));
            String line = br.readLine();
            if (line != null) {
                result = line.contains("ANDROID!") || line.contains("init=/init");
            } else {
                return false;
            }
        } catch (IOException e) {
            App.ERRORS.add(e.toString());
            e.printStackTrace();
        }
        return result;
    }

    public interface OnFlashListener {
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