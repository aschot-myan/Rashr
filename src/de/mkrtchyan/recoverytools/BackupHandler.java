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

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Notifyer;

public class BackupHandler {

    private final Context mContext;
    private File fBACKUP;
    private final Notifyer mNotifyer;
    private final DeviceHandler mDeviceHandler;
    private String NameHint = "";

    BackupHandler(final Context mContext) {
        this.mContext = mContext;
        mNotifyer = new Notifyer(mContext);
        mDeviceHandler = new DeviceHandler();
    }

    public void backup(String Hint) {
        if (!Common.suRecognition()) {
            mNotifyer.showRootDeniedDialog();
        } else {
            NameHint = Hint;
            setBakNameAndRun.run();
        }
    }

    public void restore(File backup) {
        if (!Common.suRecognition()) {
            mNotifyer.showRootDeniedDialog();
        } else {
            new FlashUtil(mContext, backup, FlashUtil.JOB_RESTORE).execute();
        }
    }

    public void deleteBackup(File backup) {
        if (RecoveryTools.PathToBackups.list().length < 1) {
            mNotifyer.createAlertDialog(R.string.warning, String.format(mContext.getString(R.string.no_backup), RecoveryTools.PathToBackups.getAbsolutePath()), setBakNameAndRun).show();
        } else {
            backup.delete();
            Toast.makeText(mContext, String.format(mContext.getString(R.string.bak_deleted), backup.getName()), Toast.LENGTH_SHORT).show();
        }
    }

    private final Runnable rBackup = new Runnable() {
        @Override
        public void run() {
            new FlashUtil(mContext, fBACKUP, FlashUtil.JOB_BACKUP).execute();
        }
    };

    private final Runnable setBakNameAndRun = new Runnable() {

        @Override
        public void run() {
            final Dialog dialog = new Dialog(mContext);
            dialog.setTitle(R.string.setname);
            dialog.setContentView(R.layout.dialog_backup);
            final Button bGoBackup = (Button) dialog.findViewById(R.id.bGoBackup);
            final EditText etFileName = (EditText) dialog.findViewById(R.id.etFileName);
            if (NameHint.equals(""))
                NameHint = Calendar.getInstance().get(Calendar.DATE)
                        + "-" + Calendar.getInstance().get(Calendar.MONTH)
                        + "-" + Calendar.getInstance().get(Calendar.YEAR)
                        + "-" + Calendar.getInstance().get(Calendar.HOUR)
                        + ":" + Calendar.getInstance().get(Calendar.MINUTE);
            etFileName.setHint(NameHint);
            bGoBackup.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    String Name = "";
                    if (etFileName.getText() != null && !etFileName.getText().toString().equals(""))
                        Name = etFileName.getText().toString();
                    if (Name.equals(""))
                        Name = String.valueOf(etFileName.getHint());
                    if (!Name.endsWith(mDeviceHandler.getEXT()))
                        Name = Name + mDeviceHandler.getEXT();
                    fBACKUP = new File(RecoveryTools.PathToBackups, Name);

                    if (fBACKUP.exists()) {
                        new Notifyer(mContext).createAlertDialog(R.string.warning, R.string.backupalready, rBackup).show();
                    } else {
                        rBackup.run();
                    }
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    };
}
