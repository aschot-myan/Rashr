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

    private File StartFolder = File.listRoots()[0];
    final private ListView lvFiles;
    private final Context mContext;
    private File currentPath;
    private boolean showHidden = false;
    private File selectedFile;
    private ArrayList<File> FileList = new ArrayList<File>();
    private ArrayAdapter<String> FilesAdapter = null;
    private OnFileChooseListener onFileChooseListener = null;
    private OnFolderChooseListener onFolderChooseListener = null;
    private String AllowedEXT[] = {""};
    private LinearLayout layout;
    private boolean warn = true;
    private boolean BrowseUpAllowed = false;

    public FileChooserDialog(final Context mContext) {
        super(mContext);
        this.mContext = mContext;
        lvFiles = new FileListView(mContext);
        setup();
    }

    private void setup() {
        this.setTitle(R.string.file_chooser_dialog_title);
        currentPath = StartFolder;
        FilesAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1);

        layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(lvFiles);
        setContentView(layout);

        lvFiles.setAdapter(FilesAdapter);

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
        ArrayList<String> tmp = new ArrayList<String>();
        tmp.clear();
        FileList.clear();
        FilesAdapter.clear();

        if ((!currentPath.equals(StartFolder) || BrowseUpAllowed)
                && currentPath.getParentFile() != null) {
            FileList.add(currentPath.getParentFile());
        }
        File fileList[] = currentPath.listFiles();
        if (fileList != null) {
            for (File i : fileList) {
                if (showHidden || !i.getName().startsWith(".")) {
                    if (!AllowedEXT[0].equals("") || AllowedEXT.length > 1) {
                        if (i.isDirectory()) {
                            FileList.add(i);
                        } else {
	                        if (Common.stringEndsWithArray(i.getName(), AllowedEXT)) {
		                        FileList.add(i);
	                        }
                        }
                    } else {
                        FileList.add(i);
                    }
                }
            }
            Collections.sort(FileList);
	        lvFiles.post(new Runnable() {
		        @Override
		        public void run() {
			        lvFiles.smoothScrollToPosition(0);
		        }
	        });
        }

        boolean parent = currentPath.getParentFile() != null;

        for (File i : FileList) {
            if (i.isDirectory()) {
                if (parent) {
                    if (!i.toString().equals("/")) {
                        tmp.add(i + "/");
                    } else {
                        tmp.add(i.getAbsolutePath());
                    }
                    parent = false;
                } else {
                    tmp.add(i.getName() + "/");
                }
            } else {
                tmp.add(i.getName());
            }
        }
        for (String i : tmp) {
            FilesAdapter.add(i);
        }

    }

    private void fileSelected() {
        if (onFileChooseListener != null) {
            if (warn) {
                AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);
                mAlertDialog
                        .setTitle(R.string.warning)
                        .setMessage(String.format(mContext.getString(R.string.choose_message), selectedFile.getName()))
                        .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onFileChooseListener.OnFileChoose(selectedFile);
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
                onFileChooseListener.OnFileChoose(selectedFile);
                this.dismiss();
            }
        }
    }

    public void show() {
        super.show();
        currentPath = StartFolder;
        reload();
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

    public void setBrowseUpAllowed(boolean BrowseUpAllowed) {
        this.BrowseUpAllowed = BrowseUpAllowed;
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

    public ListView getListView() {
        return lvFiles;
    }

    public void setOnFileChooseListener(OnFileChooseListener listener) {
        onFileChooseListener = listener;
    }

    public boolean setStartFolder(File StartFolder) {
        if (StartFolder.isDirectory()) {
            this.StartFolder = StartFolder;
        }
        return StartFolder.isDirectory();
    }

    public abstract interface OnFileChooseListener {
        void OnFileChoose(File file);
    }

    public abstract interface OnFolderChooseListener {
        void OnFolderChoose(File folder);
    }
}