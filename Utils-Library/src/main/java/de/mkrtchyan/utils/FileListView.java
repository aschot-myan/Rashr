package de.mkrtchyan.utils;

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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class FileListView extends ListView {

    final private FileListView thisFileList = this;
    private final Context mContext;
    private File currentPath;
    private ArrayList<File> FileList = new ArrayList<File>();

    /** Settings */
    /**
     * Standard starts in android root
     */
    private File StartFolder = new File("/");
    /**
     * AllowedEXT contains all allowed file extensions (.mp3 or .img ), "" = All extensions
     */
    private String AllowedEXT[] = {""};
    /**
     * Show warning after file or folder picked
     */
    private boolean warnAtChoose = false;
    /**
     * Show hidden directories and files
     */
    private boolean showHidden = false;
    /**
     * Enables the option to navigate over the start folder.
     * EXAMPLE:
     * <p/>
     * If /sdcard/ is the start folder you can navigate up to /
     */
    private boolean BrowseUpEnabled = false;
    private OnFileClickListener mFileClickListener = null;
    private OnFolderClickListener mFolderClickListener = null;

    public FileListView(Context mContext) {
        super(mContext);
        this.mContext = mContext;
        this.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                if (FileList.get(arg2).isDirectory()) {
                    /**
                     * If picked item is a directory browse into the directory and reload the
                     * ListView
                     */
                    currentPath = FileList.get(arg2);
                    reload();
                } else {
                    fileSelected(FileList.get(arg2));
                }
            }
        });
    }

    public void reload() {
        FileList.clear();

        if (!currentPath.equals(StartFolder) || BrowseUpEnabled) {
            FileList.add(currentPath.getParentFile());
        }
        try {
            for (File i : currentPath.listFiles()) {
                if (showHidden || !i.getName().startsWith("."))
                    if (i.isDirectory()) {
                        FileList.add(i);
                    } else {
                        if (!AllowedEXT[0].equals("")) {
                            for (String EXT : AllowedEXT) {
                                if (i.getName().endsWith(EXT)) {
                                    FileList.add(i);
                                }
                            }
                        } else {
                            FileList.add(i);
                        }
                    }
            }
            Collections.sort(FileList);
            String[] tmp = new String[FileList.toArray(new File[FileList.size()]).length];
            for (int i = 0; i < tmp.length; i++) {

                if (i == 0 && BrowseUpEnabled || i == 0 && currentPath != StartFolder) {
                    tmp[0] = "/..  " + currentPath.getParentFile().getName() + "/";
                } else {
                    if (FileList.get(i).isDirectory()) {
                        tmp[i] = FileList.get(i).getName() + "/";
                    } else {
                        tmp[i] = FileList.get(i).getName();
                    }
                }
            }
            if (BrowseUpEnabled || currentPath != StartFolder) {
                tmp[0] = "/..  " + currentPath.getParentFile().getName() + "/";
            }

            int i = 1;
            for (File file : FileList) {
                if (FileList.get(i).isDirectory()) {
                    tmp[i] = file.getName() + "/";
                } else {
                    tmp[i] = file.getName();
                }
                i++;
            }
            this.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, tmp));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void fileSelected(final File file) {
        if (warnAtChoose) {
            final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);
            mAlertDialog
                    .setTitle(R.string.warning)
                    .setMessage(String.format(mContext.getString(R.string.choose_message), file.getName()))
                    .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (mFileClickListener != null) {
                                mFileClickListener.OnFileClick(thisFileList, file);
                            }
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        } else {
            if (mFileClickListener != null) {
                mFileClickListener.OnFileClick(thisFileList, file);
            }
        }
    }

    public void setAllowedEXT(String[] AllowedEXT) {
        int i = 0;
        for (String EXT : AllowedEXT) {
            if (!EXT.startsWith(".")) {
                AllowedEXT[i] = "." + EXT;
            }
            i++;
        }
        reload();
    }

    public void setWarnAtChoose(boolean warnAtChoose) {
        this.warnAtChoose = warnAtChoose;
    }

    public void setBrowseUpEnabled(boolean BrowseUpEnabled) {
        this.BrowseUpEnabled = BrowseUpEnabled;
    }

    public void showHidden(boolean showHidden) {
        this.showHidden = showHidden;
        reload();
    }

    public void setOnFileClickListener(OnFileClickListener listener) {
        this.mFileClickListener = listener;
    }

    public void setOnFolderClickListener(OnFolderClickListener listener) {
        this.mFolderClickListener = listener;
    }

    public void setStartFolder(File StartFolder) {
        this.StartFolder = StartFolder;
    }

    /**
     * Handles file picking event
     */
    public interface OnFileClickListener {
        void OnFileClick(FileListView fileListView, File selectedFile);
    }

    public interface OnFolderClickListener {
        void OnFolderClick(FileListView fileListView, File selectedFolder);
    }


}