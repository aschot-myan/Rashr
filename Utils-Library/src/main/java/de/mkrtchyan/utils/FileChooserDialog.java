package de.mkrtchyan.utils;

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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class FileChooserDialog extends Dialog {

    private final File StartFolder;
    final private ListView lvFiles;
    private final Context mContext;
    private File currentPath;
    private boolean showHidden = false;
    private File selectedFile;
    private ArrayList<File> FileList = new ArrayList<File>();
    private Runnable runAtChoose;
    private String AllowedEXT[] = {""};
    private LinearLayout layout;
    private boolean warn = true;
    private boolean BrowseUpEnabled = false;

    public FileChooserDialog(final Context mContext, final File StartFolder, Runnable runAtChoose) throws NullPointerException {
        super(mContext);

        this.StartFolder = StartFolder;
        this.mContext = mContext;
        currentPath = StartFolder;
        this.runAtChoose = runAtChoose;

        layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);

        lvFiles = new FileListView(mContext);
        layout.addView(lvFiles);
        setContentView(layout);

        lvFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                selectedFile = FileList.get(arg2);
                if (selectedFile.isDirectory()) {
                    currentPath = selectedFile;
                    reload();
                } else {
                    fileSelected();
                }
            }
        });
    }

    private void reload() {
        FileList.clear();

        if ((!currentPath.equals(StartFolder) || BrowseUpEnabled)
                && currentPath.getParentFile()!= null) {
            FileList.add(currentPath.getParentFile());
        }
        try {
            for (File i : currentPath.listFiles()) {
                if (showHidden || !i.getName().startsWith(".")) {
                    if (!AllowedEXT[0].equals("") || AllowedEXT.length > 1) {
                        if (i.isDirectory()) {
                            FileList.add(i);
                        } else {
                            for (String EXT : AllowedEXT) {
                                if (i.getName().endsWith(EXT)) {
                                    FileList.add(i);
                                }
                            }
                        }
                    } else {
                        FileList.add(i);
                    }
                }
            }
        } catch (NullPointerException e) {
            if (isShowing()) {
                dismiss();
            }
        }

        try {
            Collections.sort(FileList);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        String[] tmp = new String[FileList.toArray(new File[FileList.size()]).length];
        for (int i = 0; i < tmp.length; i++) {

            if (i == 0 && (BrowseUpEnabled || !currentPath.equals(StartFolder))
                    && currentPath.getParentFile() != null) {
                if (!currentPath.getParentFile().getAbsolutePath().equals("/")) {
                    tmp[0] = currentPath.getParentFile().getAbsolutePath() + "/";
                } else {
                    tmp[0] = currentPath.getParentFile().getAbsolutePath();
                }
            } else {
                if (FileList.get(i).isDirectory()) {
                    tmp[i] = FileList.get(i).getName() + "/";
                } else {
                    tmp[i] = FileList.get(i).getName();
                }
            }
        }
        lvFiles.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, tmp));
    }

    private void fileSelected() {
        if (warn) {
            AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);
            mAlertDialog
                    .setTitle(R.string.warning)
                    .setMessage(String.format(mContext.getString(R.string.choose_message), selectedFile.getName()))
                    .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            runAtChoose.run();
                            dismiss();
                        }
                    })
                    .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        } else {
            runAtChoose.run();
            this.dismiss();
        }
    }

    public void show() {
        super.show();
        reload();
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setAllowedEXT(String AllowedEXT[]) {
        for (int i = 0; i > AllowedEXT.length; i++) {
            if (!AllowedEXT[i].startsWith(".")) {
                AllowedEXT[i] = "." + AllowedEXT[i];
            }
        }
        this.AllowedEXT = AllowedEXT;
        if (isShowing()) {
            reload();
        }
    }

    public LinearLayout getLayout() {
        return layout;
    }

    public void setWarn(boolean warn) {
        this.warn = warn;
    }

    public void setBrowseUpEnabled(boolean BrowseUpEnabled) {
        this.BrowseUpEnabled = BrowseUpEnabled;
        if (isShowing()) {
            reload();
        }
    }

    public void showHiddenFiles(boolean showHidden) {
        this.showHidden = showHidden;
        if (isShowing()) {
            reload();
        }
    }

    public ListView getList() {
        return lvFiles;
    }
}