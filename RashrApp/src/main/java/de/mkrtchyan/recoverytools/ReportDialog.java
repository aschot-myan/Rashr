package de.mkrtchyan.recoverytools;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.Toolbox;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

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
public class ReportDialog extends Dialog {

    public ReportDialog(final RashrActivity activity, String message) {
        super(activity);
        final Shell shell = activity.getShell();
        final Device device = activity.getDevice();
        final Toolbox toolbox = activity.getToolbox();
        final ArrayList<String> errors = activity.getErrors();
        setTitle(R.string.comment);
        setContentView(R.layout.dialog_comment);
        final EditText text = (EditText) findViewById(R.id.etComment);
        if (!message.equals("")) text.setText(message);
        new Thread(new Runnable() {
            @Override
            public void run() {
                /** Creates a report Email including a Comment and important device infos */
                final Button bGo = (Button) findViewById(R.id.bGo);
                bGo.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (!Common.getBooleanPref(activity, Constants.PREF_NAME,
                                Constants.PREF_KEY_ADS))
                            Toast
                                    .makeText(activity, R.string.please_ads, Toast.LENGTH_SHORT)
                                    .show();
                        Toast
                                .makeText(activity, R.string.donate_to_support, Toast.LENGTH_SHORT)
                                .show();
                        try {
                            ArrayList<File> files = new ArrayList<>();
                            File TestResults = new File(activity.getFilesDir(), "results.txt");
                            try {
                                if (TestResults.exists()) {
                                    if (TestResults.delete()) {
                                        FileOutputStream fos = activity.openFileOutput(
                                                TestResults.getName(), Context.MODE_PRIVATE);
                                        fos.write(("Rashr:\n\n" + shell
                                                .execCommand("ls -lR " + Constants.PathToRashr.getAbsolutePath()) +
                                                "\nCache Tree:\n" + shell
                                                .execCommand("ls -lR /cache") + "\n" +
                                                "\nMTD result:\n" + shell
                                                .execCommand("cat /proc/mtd") + "\n" +
                                                "\nDevice Tree:\n\n" + shell
                                                .execCommand("ls -lR /dev")).getBytes());
                                    }
                                    files.add(TestResults);
                                }
                            } catch (Exception e) {
                                activity.addError(Constants.RASHR_TAG, e, false);
                            }
                            if (activity.getPackageManager() != null) {
                                PackageInfo pInfo = activity.getPackageManager()
                                        .getPackageInfo(activity.getPackageName(), 0);

                                String comment = "";
                                if (text.getText() != null) comment = text.getText().toString();
                                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ashotmkrtchyan1995@gmail.com"});
                                intent.putExtra(Intent.EXTRA_SUBJECT, "Rashr " + pInfo.versionCode + " report");
                                String message = "Package Infos:" +
                                        "\n\nName: " + pInfo.packageName +
                                        "\nVersion Name: " + pInfo.versionName;
                                message +=
                                        "\n\n\nProduct Info: " +
                                                "\n\nManufacture: " + Build.MANUFACTURER + " (" + device.getManufacture() + ") " +
                                                "\nDevice: " + Build.DEVICE + " (" + device.getName() + ")" +
                                                "\nBoard: " + Build.BOARD +
                                                "\nBrand: " + Build.BRAND +
                                                "\nModel: " + Build.MODEL +
                                                "\nFingerprint: " + Build.FINGERPRINT +
                                                "\nAndroid SDK Level: " + Build.VERSION.CODENAME + " (" + Build.VERSION.SDK_INT + ")";

                                if (device.isRecoverySupported()) {
                                    message += "\n\nRecovery Path: " + device.getRecoveryPath() +
                                            "\nRecovery Version: " + device.getRecoveryVersion() +
                                            "\nRecovery MTD: " + device.isRecoveryMTD() +
                                            "\nRecovery DD: " + device.isRecoveryDD() +
                                            "\nStock: " + device.isStockRecoverySupported() +
                                            "\nCWM: " + device.isCwmRecoverySupported() +
                                            "\nTWRP: " + device.isTwrpRecoverySupported() +
                                            "\nPHILZ: " + device.isPhilzRecoverySupported();
                                }
                                if (device.isKernelSupported()) {
                                    message += "\n\nKernel Path: " + device.getKernelPath() +
                                            "\nKernel Version: " + device.getKernelVersion() +
                                            "\nKernel MTD: " + device.isKernelMTD() +
                                            "\nKernel DD: " + device.isKernelDD();
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
                                files.add(new File(activity.getFilesDir(), Shell.Logs));
                                files.add(new File(activity.getFilesDir(), "last_log.txt"));
                                ArrayList<Uri> uris = new ArrayList<>();
                                File tmpFolder = new File(activity.getFilesDir(), "tmp");
                                if (tmpFolder.mkdir()) tmpFolder.deleteOnExit();
                                for (File i : files) {
                                    try {
                                        File tmp = new File(tmpFolder, i.getName());
                                        toolbox.setFilePermissions(tmp, "777");
                                        toolbox.copyFile(i, tmp, true, false);
                                        toolbox.setFilePermissions(tmp, "777");
                                        uris.add(Uri.fromFile(tmp));
                                    } catch (Exception e) {
                                        activity.addError(Constants.RASHR_TAG, e, false);
                                    }
                                }
                                if (errors.size() > 0) {
                                    message += "ERRORS:\n";
                                    for (String error : errors) {
                                        message += error + "\n";
                                    }
                                }

                                intent.putExtra(Intent.EXTRA_TEXT, message);
                                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                                activity.startActivity(Intent.createChooser(intent, "Send over Gmail"));
                                dismiss();
                            }
                        } catch (Exception e) {
                            dismiss();
                            activity.addError(Constants.RASHR_TAG, e, false);
                        }
                    }
                });
            }
        }).start();
    }
}
