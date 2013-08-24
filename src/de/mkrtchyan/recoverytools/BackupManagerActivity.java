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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.util.Calendar;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.FileChooser;
import de.mkrtchyan.utils.Notifyer;

public class BackupManagerActivity extends Activity {

    private File fBACKUP;

    final private Context mContext = this;
    final private Notifyer mNotifyer = new Notifyer(mContext);
    final private Common mCommon = new Common();
    private FileChooser fcRestore, fcDelete;

    private final Runnable rBackup = new Runnable() {
        @Override
        public void run() {

            new FlashUtil(mContext, fBACKUP, 2).execute();

        }
    };
    private final Runnable rRestore = new Runnable() {
        @Override
        public void run() {

            if (fcRestore.use) {
                fBACKUP = fcRestore.selectedFile;
                new FlashUtil(mContext, fBACKUP, 1).execute();
            }
        }
    };

    private final Runnable rDelete = new Runnable() {
        @Override
        public void run() {
            fcDelete.selectedFile.delete();
            if (RecoveryTools.PathToBackups.listFiles().length > 0) {
                fcDelete = new FileChooser(mContext, RecoveryTools.PathToBackups.getAbsolutePath(), "", rDelete);
            }
        }
    };

    private final Runnable setBakNameAndRun = new Runnable() {

        @Override
        public void run() {

            final Dialog dialog = new Dialog(mContext);
            dialog.setTitle(R.string.setname);
            dialog.setContentView(R.layout.dialog_input);
            Button dobackup = (Button) dialog.findViewById(R.id.bGoBackup);
            final EditText etFileName = (EditText) dialog.findViewById(R.id.etFileName);
            dobackup.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        String Name;
                        if (!etFileName.getText().toString().equals("")) {
                            Name = etFileName.getText().toString();
                        } else {
                            Calendar c = Calendar.getInstance();
                            Name = c.get(Calendar.DATE)
                                    + "-" + c.get(Calendar.MONTH)
                                    + "-" + c.get(Calendar.YEAR)
                                    + "-" + c.get(Calendar.HOUR)
                                    + ":" + c.get(Calendar.MINUTE);
                        }

                        Name = Name + mDeviceHandler.EXT;

                        fBACKUP = new File(RecoveryTools.PathToBackups, Name);

                        if (fBACKUP.exists()) {
                            mNotifyer.createAlertDialog(R.string.warning, R.string.backupalready, rBackup).show();
                        } else {
                            rBackup.run();
                        }
                        dialog.dismiss();
                    } catch (NullPointerException e) {
                        mNotifyer.showExceptionToast(e);
                    }
                }
            });
            dialog.show();
        }
    };

    private final DeviceHandler mDeviceHandler = new DeviceHandler(mContext);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_manager);

        mCommon.checkFolder(RecoveryTools.PathToBackups);
    }

    public void bBackup(View view) {
        if (!mCommon.suRecognition()) {
            mNotifyer.showRootDeniedDialog();
        } else {
            setBakNameAndRun.run();
        }
    }

    public void bRestore(View view) {
        if (!mCommon.suRecognition()) {
            mNotifyer.showRootDeniedDialog();
        } else {
            if (RecoveryTools.PathToBackups.list().length < 1) {
                mNotifyer.createAlertDialog(R.string.warning, R.string.nobackup, setBakNameAndRun).show();
            } else {
                fcRestore = new FileChooser(mContext, RecoveryTools.PathToBackups.getAbsolutePath(), mDeviceHandler.EXT, rRestore);
            }
        }
    }

    public void bDeleteBackup(View view) {
        if (RecoveryTools.PathToBackups.list().length < 1) {
            mNotifyer.createAlertDialog(R.string.warning, R.string.nobackup, setBakNameAndRun).show();
        } else {
            fcDelete = new FileChooser(mContext, RecoveryTools.PathToBackups.getAbsolutePath(), "", rDelete);
        }
    }
}
