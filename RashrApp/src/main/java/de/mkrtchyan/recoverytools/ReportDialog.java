package de.mkrtchyan.recoverytools;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import de.mkrtchyan.utils.Common;

/**
 * Copyright (c) 2016 Aschot Mkrtchyan
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
public class ReportDialog extends AppCompatDialog {

    public ReportDialog(final RashrActivity activity, String message) {
        super(activity);
        setTitle(R.string.comment);
        setContentView(R.layout.dialog_comment);
        final EditText text = (EditText) findViewById(R.id.etComment);
        if (!message.equals(""))
            if (text != null)
                text.setText(message);
        new Thread(new Runnable() {
            @Override
            public void run() {
                /** Creates a report Email including a Comment and important device infos */
                final Button bGo = (Button) findViewById(R.id.bGo);
                if (bGo != null) {
                    bGo.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            if (!Common.getBooleanPref(activity, Const.PREF_NAME,
                                    Const.PREF_KEY_ADS)) {
                                Toast
                                        .makeText(activity, R.string.please_ads, Toast.LENGTH_SHORT)
                                        .show();
                            } else {
                                Toast
                                        .makeText(activity, R.string.donate_to_support, Toast.LENGTH_SHORT)
                                        .show();
                            }
                            try {
                                ArrayList<File> files = new ArrayList<>();
                                File TestResults = new File(activity.getFilesDir(), "results.txt");
                                try {
                                    if (TestResults.exists()) {
                                        if (TestResults.delete()) {
                                            FileOutputStream fos = activity.openFileOutput(
                                                    TestResults.getName(), Context.MODE_PRIVATE);
                                            fos.write(("Rashr:\n\n" + RashrApp.SHELL
                                                    .execCommand("ls -lR " + Const.PathToRashr.getAbsolutePath()) +
                                                    "\nCache Tree:\n" + RashrApp.SHELL
                                                    .execCommand("ls -lR /cache") + "\n" +
                                                    "\nMTD result:\n" + RashrApp.SHELL
                                                    .execCommand("cat /proc/mtd") + "\n" +
                                                    "\nDevice Tree:\n\n" + RashrApp.SHELL
                                                    .execCommand("ls -lR /dev")).getBytes());
                                        }
                                        files.add(TestResults);
                                    }
                                } catch (Exception e) {
                                    RashrApp.ERRORS.add(Const.RASHR_TAG + " Failed to list files: " + e);
                                }
                                String comment = "";
                                if (text != null) comment = text.getText().toString();
                                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ashotmkrtchyan1995@gmail.com"});
                                intent.putExtra(Intent.EXTRA_SUBJECT, "Rashr " + BuildConfig.VERSION_CODE + " report");
                                String message = "Package Infos:" +
                                        "\n\nName: " + BuildConfig.APPLICATION_ID +
                                        "\nVersion Name: " + BuildConfig.VERSION_NAME;
                                message +=
                                        "\n\n\nProduct Info: " +
                                                "\n\nManufacture: " + Build.MANUFACTURER + " (" + RashrApp.DEVICE.getManufacture() + ") " +
                                                "\nDevice: " + Build.DEVICE + " (" + RashrApp.DEVICE.getName() + ")" +
                                                "\nBoard: " + Build.BOARD +
                                                "\nBrand: " + Build.BRAND +
                                                "\nModel: " + Build.MODEL +
                                                (RashrApp.DEVICE.isXZDualRecoverySupported() ?
                                                        "XZDualName: " + RashrApp.DEVICE.getXZDualName() + "\n" : "") +
                                                "\nFingerprint: " + Build.FINGERPRINT +
                                                "\nAndroid SDK Level: " + Build.VERSION.CODENAME + " (" + Build.VERSION.SDK_INT + ")";

                                if (RashrApp.DEVICE.isRecoverySupported()) {
                                    message += "\n\nRecovery Path: " + RashrApp.DEVICE.getRecoveryPath() +
                                            "\nRecovery Version: " + RashrApp.DEVICE.getRecoveryVersion() +
                                            "\nRecovery MTD: " + RashrApp.DEVICE.isRecoveryMTD() +
                                            "\nRecovery DD: " + RashrApp.DEVICE.isRecoveryDD() +
                                            "\nStock: " + RashrApp.DEVICE.isStockRecoverySupported() +
                                            "\nCWM: " + RashrApp.DEVICE.isCwmRecoverySupported() +
                                            "\nTWRP: " + RashrApp.DEVICE.isTwrpRecoverySupported() +
                                            "\nPHILZ: " + RashrApp.DEVICE.isPhilzRecoverySupported() +
                                            "\nXZDual: " + RashrApp.DEVICE.isXZDualRecoverySupported();
                                }
                                if (RashrApp.DEVICE.isKernelSupported()) {
                                    message += "\n\nKernel Path: " + RashrApp.DEVICE.getKernelPath() +
                                            "\nKernel Version: " + RashrApp.DEVICE.getKernelVersion() +
                                            "\nKernel MTD: " + RashrApp.DEVICE.isKernelMTD() +
                                            "\nKernel DD: " + RashrApp.DEVICE.isKernelDD();
                                }
                                if (!comment.equals("")) {
                                    message +=
                                            "\n\n\n===========COMMENT==========\n"
                                                    + comment +
                                                    "\n=========COMMENT END========\n";
                                }
                                message +=
                                        "\n===========PREFS==========\n"
                                                + activity.getAllPrefs() +
                                                "\n=========PREFS END========\n";
                                files.add(new File(activity.getFilesDir(), Const.Logs));
                                files.add(new File(activity.getFilesDir(), Const.LastLog.getName()));
                                ArrayList<Uri> uris = new ArrayList<>();
                                for (File i : files) {
                                    try {
                                        RashrApp.SHELL.execCommand(Const.Busybox + " chmod 777 " + i);
                                        File tmpFile = new File(Const.PathToTmp, i.getName());
                                        Common.copyFile(i, tmpFile);
                                        RashrApp.SHELL.execCommand(Const.Busybox + " chmod 777 " + tmpFile);
                                        uris.add(Uri.fromFile(tmpFile));
                                    } catch (FailedExecuteCommand e) {
                                        RashrApp.ERRORS.add("Failed setting permissions to Attachment: " + e);
                                    } catch (IOException e) {
                                        RashrApp.ERRORS.add("Failed to copy Attachment to destination: " + e);
                                    }
                                }
                                if (RashrApp.ERRORS.size() > 0) {
                                    message += "ERRORS:\n";
                                    for (String error : RashrApp.ERRORS) {
                                        message += error + "\n";
                                    }
                                }

                                intent.putExtra(Intent.EXTRA_TEXT, message);
                                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                                activity.startActivity(Intent.createChooser(intent, "Send over Gmail"));
                                dismiss();

                            } catch (Exception e) {
                                dismiss();
                                RashrApp.ERRORS.add(Const.RASHR_TAG + " Failed to create attachment: " + e);
                            }
                        }
                    });
                }
            }
        }).start();
    }
}
