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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.Toolbox;
import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.FileChooserDialog;

public class RecoveryScriptManager extends ActionBarActivity {

    final Context mContext = this;
    static private final String CMD_END = ";";
    ArrayList<File> mFileList;
    ArrayAdapter<String> mFileNameAdapter;
    ListView Queue;
    FileChooserDialog fileChooser;
	String[] AllowedEXT = {".zip"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Common.getIntegerPref(mContext, Rashr.PREF_NAME, Rashr.PREF_STYLE) != 0) {
            /** Using predefined theme */
            int Theme = Common.getIntegerPref(mContext, Rashr.PREF_NAME, Rashr.PREF_STYLE);
            setTheme(Theme);
        }

        setContentView(R.layout.recovery_script_manager);

        mFileNameAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1);
        mFileList = new ArrayList<File>();
        Queue = (ListView) findViewById(R.id.lvQueue);
        Queue.setAdapter(mFileNameAdapter);
        Queue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mFileList.remove(position);
                mFileNameAdapter.clear();
                for (File i : mFileList) {
                    mFileNameAdapter.add(i.getName());
                }
            }
        });

        Uri path;
        if ((path = getIntent().getData()) != null) {
            File zip = new File(path.getPath());
	        if (Common.stringEndsWithArray(zip.getName(), AllowedEXT)) {
		        addFileToQueue(zip);
	        } else {
		        Toast.makeText(mContext, R.string.wrong_format, Toast.LENGTH_SHORT).show();
	        }
        }
    }

    public void addZip(View view) {
        fileChooser = new FileChooserDialog(mContext);
        fileChooser.setOnFileChooseListener(new FileChooserDialog.OnFileChooseListener() {
            @Override
            public void OnFileChoose(File file) {
                addFileToQueue(file);
            }
        });
        String EXT[] = {"zip"};
        fileChooser.setAllowedEXT(EXT);
        fileChooser.setBrowseUpAllowed(true);
        fileChooser.setWarn(false);
        fileChooser.show();
    }

    public void addFileToQueue(File file) {
        if (file.exists()) {
            mFileList.add(file);
            mFileNameAdapter.add(file.getName());
        }
    }

    public void bFlashZip(View view) {
        CheckBox cbBakSystem = (CheckBox) findViewById(R.id.cbBackupSystem);
        CheckBox cbBakData = (CheckBox) findViewById(R.id.cbBackupData);
        CheckBox cbBakCache = (CheckBox) findViewById(R.id.cbBackupCache);
        CheckBox cbBakRecovery = (CheckBox) findViewById(R.id.cbBackupRecovery);
        CheckBox cbBakBoot = (CheckBox) findViewById(R.id.cbBackupBoot);
        EditText etBakName = (EditText) findViewById(R.id.etBackupName);
        CheckBox cbWipeCache = (CheckBox) findViewById(R.id.cbWipeCache);
        CheckBox cbWipeDalvik = (CheckBox) findViewById(R.id.cbWipeDalvik);
        CheckBox cbWipeData = (CheckBox) findViewById(R.id.cbWipeData);
        final StringBuilder command = new StringBuilder();
        command.append("echo #####Script created by Rashr#####;");
        if (cbBakBoot.isChecked() || cbBakCache.isChecked() || cbBakData.isChecked()
                || cbBakRecovery.isChecked() || cbBakSystem.isChecked()) {
            command.append("backup ");
            if (cbBakBoot.isChecked()) command.append("B");
            if (cbBakCache.isChecked()) command.append("C");
            if (cbBakData.isChecked()) command.append("D");
            if (cbBakRecovery.isChecked()) command.append("R");
            if (cbBakSystem.isChecked()) command.append("S");

            CharSequence BackupName = etBakName.getText();
            if (BackupName != null && !BackupName.equals("")) {
                command.append(" ");
                command.append(BackupName);
            }
            command.append(CMD_END);

        }

        if (cbWipeCache.isChecked()) command.append("wipe cache;");
        if (cbWipeDalvik.isChecked()) command.append("wipe dalvik;");
        if (cbWipeData.isChecked()) command.append("wipe data;");

        for (File i : mFileList) {
            command.append("install ");
            command.append(i.getAbsolutePath());
            command.append(CMD_END);
        }

        if (!command.toString().equals("")) {
            String commands = "";
            int index = 0;
            for (String i : command.toString().split(CMD_END)) {
                if (!i.equals("")) {
                    if (index > 0) {
                        commands += index++ + ". " + i + "\n";
                    } else {
                        index++;
                    }
                }
            }
            final AlertDialog.Builder CommandsPreview = new AlertDialog.Builder(mContext);
            CommandsPreview.setTitle(R.string.recovery_script_review);
            CommandsPreview.setPositiveButton(R.string.run, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        Shell mShell = Shell.startRootShell();
                        for (String split : command.toString().split(";")) {
                            if (!split.equals("")) {
                                mShell.execCommand("echo " + split + " >> /cache/recovery/openrecoveryscript");
                            }
                        }
                        new Toolbox(mShell).reboot(Toolbox.REBOOT_RECOVERY);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (FailedExecuteCommand failedExecuteCommand) {
                        failedExecuteCommand.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                }
            });
            CommandsPreview.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            CommandsPreview.setMessage(commands);
            CommandsPreview.show();
        } else {
            Toast.makeText(mContext, "No job to do :)", Toast.LENGTH_LONG).show();
        }

    }
}
