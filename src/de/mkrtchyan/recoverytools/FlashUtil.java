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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.sbstrm.appirater.Appirater;

import org.rootcommands.util.RootAccessDeniedException;

import java.io.File;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Notifyer;

public class FlashUtil extends AsyncTask<Void, Void, Boolean> {

    private Context mContext;
    private ProgressDialog pDialog;
    final private Common mCommon = new Common();
    private Notifyer mNotifyer;
    private final Support mSupport = new Support();
    private File charger, chargermon, ric, fflash, fdump, file;
    private int JOB;

    public FlashUtil(Context context, File file, int JOB) {
        mContext = context;
        this.file = file;
        this.JOB = JOB;
        mNotifyer = new Notifyer(mContext);

        if (mSupport.MTD) {
            fdump = new File(RecoveryTools.PathToUtils, "dump_image");
            fflash = new File(RecoveryTools.PathToUtils, "flash_image");
        }

        charger = new File(mContext.getFilesDir(), "charger");
        chargermon = new File(mContext.getFilesDir(), "chargermon");
        ric = new File(mContext.getFilesDir(), "ric");
        if (mSupport.DEVICE.equals("C6603")
                || mSupport.DEVICE.equals("montblanc")) {
            mCommon.chmod(charger, "755");
            mCommon.chmod(chargermon, "755");
            if (mSupport.DEVICE.equals("C6603"))
                mCommon.chmod(ric, "755");
        }
    }


    protected void onPreExecute() {

        pDialog = new ProgressDialog(mContext);
        int Title;
        if (JOB == 1) {
            Title = R.string.flashing;
        } else {
            Title = R.string.creating_bak;
        }
        pDialog.setTitle(Title);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        pDialog.setMessage(file.getAbsolutePath());
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        try {
            if (mSupport.DEVICE.equals("C6603")
                    || mSupport.DEVICE.equals("montblanc")) {
                mCommon.mountDir(new File(mSupport.RecoveryPath), "RW");
                mCommon.executeSuShell(mContext, "cat " + charger.getAbsolutePath() + " >> /system/bin/" + charger.getName());
                mCommon.executeSuShell(mContext, "cat " + chargermon.getAbsolutePath() + " >> /system/bin/" + chargermon.getName());
                if (mSupport.DEVICE.equals("C6603")) {
                    mCommon.executeSuShell(mContext, "cat " + ric.getAbsolutePath() + " >> /system/bin/" + ric.getName());
                    mCommon.chmod(ric, "755");
                }
                mCommon.chmod(charger, "755");
                mCommon.chmod(chargermon, "755");
            }

            switch (JOB) {

                case 1:
                    if (file.exists()) {

                        if (mSupport.MTD) {
                            if (!new File(RecoveryTools.PathToBin, fflash.getName()).exists())
                                mCommon.copy(fflash, new File(RecoveryTools.PathToBin, fflash.getName()), true);

                            fflash = new File(RecoveryTools.PathToBin, fflash.getName());
                            mCommon.chmod(fflash, "777");
                            mCommon.executeSuShell(mContext, fflash.getAbsolutePath() + " recovery " + file.getAbsolutePath());

                        }

                        if (!mSupport.MTD
                                && !mSupport.RecoveryPath.equals(""))
                            mCommon.executeSuShell(mContext, "dd if=" + file.getAbsolutePath() + " of=" + mSupport.RecoveryPath);
                        if (mSupport.DEVICE.equals("C6603")
                                || mSupport.DEVICE.equals("montblanc")) {
                            mCommon.chmod(file, "644");
                            mCommon.mountDir(new File(mSupport.RecoveryPath), "RO");
                        }
                    }
                    break;

                case 2:
                    if (mSupport.MTD) {
                        if (!new File(RecoveryTools.PathToBin, fdump.getName()).exists())
                            mCommon.copy(fflash, new File(RecoveryTools.PathToBin, fdump.getName()), true);

                        fdump = new File(RecoveryTools.PathToBin, fflash.getName());
                        mCommon.chmod(fdump, "777");
                        mCommon.executeSuShell(mContext, fdump.getAbsolutePath() + " recovery " + file.getAbsolutePath());
                    }

                    if (!mSupport.MTD
                            && !mSupport.RecoveryPath.equals(""))
                        mCommon.executeSuShell(mContext, "dd if=" + mSupport.RecoveryPath + " of=" + file.getAbsolutePath());
                    break;
            }
        } catch (RootAccessDeniedException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(Boolean succes) {

        pDialog.dismiss();
        if (JOB == 1) {
            mNotifyer.createAlertDialog(R.string.tsk_end, mContext.getString(R.string.flashed) + " " + mContext.getString(R.string.reboot_recovery_now), new Runnable() {
                @Override
                public void run() {
                    try {
                        mCommon.executeSuShell("reboot recovery");
                    } catch (RootAccessDeniedException e) {
                        e.printStackTrace();
                    }
                }
            }).show();
        } else {
            mNotifyer.showToast(R.string.bak_done);
        }

        Appirater.appLaunched(mContext);
        mCommon.setBooleanPerf(mContext, "flash-util", "first-flash", false);
    }
}