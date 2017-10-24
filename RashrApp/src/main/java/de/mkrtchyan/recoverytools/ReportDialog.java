package de.mkrtchyan.recoverytools;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

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
public class ReportDialog extends AppCompatDialog {

    @BindView(R.id.etComment)
    AppCompatEditText mText;
    @BindView(R.id.bGo)
    AppCompatButton mGo;

    public ReportDialog(final Context context, @Nullable String message) {
        super(context);
        setTitle(R.string.comment);
        setContentView(R.layout.dialog_comment);
        ButterKnife.bind(this);
        if (message != null)
            mText.setText(message);
        new Thread(new Runnable() {
            @Override
            public void run() {
                /* Creates a report Email including a Comment and important device infos */
                mGo.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (!App.Preferences.getBoolean(App.PREF_KEY_ADS, false)) {
                            Toast
                                    .makeText(getContext(), R.string.please_ads, Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast
                                    .makeText(getContext(), R.string.donate_to_support, Toast.LENGTH_SHORT)
                                    .show();
                        }

                        ArrayList<File> files = new ArrayList<>();
                        File TestResults = new File(App.FilesDir, "results.txt");
                        try {
                            if (TestResults.exists()) {
                                if (TestResults.delete()) {
                                    FileOutputStream fos = context.openFileOutput(
                                            TestResults.getName(), Context.MODE_PRIVATE);
                                    fos.write(("Rashr:\n\n" + App.Shell
                                            .execCommand("ls -lR " + App.PathToRashr) +
                                            "\nCache Tree:\n" + App.Shell
                                            .execCommand("ls -lR /cache") + "\n" +
                                            "\nMTD result:\n" + App.Shell
                                            .execCommand("cat /proc/mtd") + "\n" +
                                            "\nDevice Tree:\n\n" + App.Shell
                                            .execCommand("ls -lR /dev")).getBytes());
                                }
                                files.add(TestResults);
                            }
                        } catch (Exception e) {
                            App.ERRORS.add(App.TAG + " Failed to list files: " + e);
                        }

                        String comment = mText.getText().toString();
                        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{App.DEV_EMAIL});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Rashr " + BuildConfig.VERSION_CODE + " report");
                        String message = "Package Infos:" +
                                "\n\nName: " + BuildConfig.APPLICATION_ID +
                                "\nVersion Name: " + BuildConfig.VERSION_NAME;
                        message += "\n\n\nManufacture: " + Build.MANUFACTURER + " (" + App.Device.getManufacture() + ") " +
                                "\nDevice: " + Build.DEVICE + " (" + App.Device.getName() + ")" +
                                "\nBoard: " + Build.BOARD +
                                "\nBrand: " + Build.BRAND +
                                "\nModel: " + Build.MODEL +
                                "\nFingerprint: " + Build.FINGERPRINT +
                                "\nAndroid SDK Level: " + Build.VERSION.CODENAME + " (" + Build.VERSION.SDK_INT + ")\n\n" +
                                new Gson().toJson(App.Device)
                                        //formatting text
                                        .replace("\",\"", "\",\n\"").replace("{\"", "\n\"")
                                        .replace("\"}", "\"\n").replace(",\"", ",\n\"")
                                        .replace("\n,\n", "").replace("\":\n", "\":")
                                        .replace("\":\"", ": ").replace("\"\"", "\n")
                                        .replace("\"", "").replace(" path: ", " ");

                        if (!comment.equals("")) {
                            message += "\n\n\n===========COMMENT==========\n"
                                    + comment +
                                    "\n=========COMMENT END========\n";
                        }
                        message += "\n===========PREFS==========\n"
                                + getAllPrefs() +
                                "\n=========PREFS END========\n";
                        files.add(new File(context.getFilesDir(), App.AppLogs));
                        files.add(new File(context.getFilesDir(), App.LastLog.getName() + ".txt"));
                        ArrayList<Uri> uris = new ArrayList<>();
                        for (File file : files) {
                            if (file.exists()) {
                                try {
                                    App.Shell.execCommand(App.Busybox + " chmod 777 " + file);
                                    File tmpFile = new File(App.PathToTmp, file.getName());
                                    App.Shell.execCommand(App.Busybox + " cp " + file + " " + tmpFile);
                                    App.Shell.execCommand(App.Busybox + " chmod 777 " + tmpFile);
                                    App.Shell.execCommand(App.Busybox + " chown root:root " + tmpFile);
                                    uris.add(Uri.fromFile(tmpFile));
                                } catch (FailedExecuteCommand e) {
                                    App.ERRORS.add("Failed setting permissions to Attachment: " + e);
                                    e.printStackTrace();
                                }
                            } else {
                                App.ERRORS.add("Attachment dosn't exists");
                            }
                        }
                        if (App.ERRORS.size() > 0) {
                            message += "ERRORS:\n";
                            for (String error : App.ERRORS) {
                                message += error + "\n";
                            }
                        }

                        intent.putExtra(Intent.EXTRA_TEXT, message);
                        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                        context.startActivity(Intent.createChooser(intent, "Send over Gmail"));
                        dismiss();
                    }
                });
            }
        }).start();
    }

    /**
     * @return All Preferences as String
     */
    public String getAllPrefs() {
        String Prefs = "";
        Map<String, ?> prefsMap = App.Preferences.getAll();
        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            /*
             * Skip following Prefs (PREF_KEY_HISTORY, ...)
             */
            if (!entry.getKey().contains(App.PREF_KEY_HISTORY)
                    && !entry.getKey().contains(App.PREF_KEY_FLASH_COUNTER)) {
                Prefs += entry.getKey() + ": " + entry.getValue().toString() + "\n";
            }
        }

        return Prefs;
    }
}
