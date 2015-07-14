package de.mkrtchyan.recoverytools;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BackupRestoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BackupRestoreFragment extends Fragment {

    private RashrActivity mActivity;
    private Context mContext;
    private Device mDevice;

    private ArrayAdapter<String> RecoveryBakAdapter;
    private ArrayAdapter<String> KernelBakAdapter;

    public static BackupRestoreFragment newInstance(RashrActivity activity) {
        BackupRestoreFragment fragment = new BackupRestoreFragment();
        fragment.setActivity(activity);
        return fragment;
    }

    public BackupRestoreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_backup_restore, container, false);
        mActivity = (RashrActivity) getActivity();
        mContext = root.getContext();
        mDevice = mActivity.getDevice();
        View createKernelBackup = root.findViewById(R.id.bCreateKernelBackup);
        createKernelBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBackup(false);
            }
        });
        ListView lvKernelBackups = (ListView) root.findViewById(R.id.lvKernelBackups);
        View createRecoveryBackup = root.findViewById(R.id.bCreateRecoveryBackup);
        createRecoveryBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBackup(true);
            }
        });
        ListView lvRecoveryBackups = (ListView) root.findViewById(R.id.lvRecoveryBackups);
        if (!mDevice.isKernelSupported()) {
            /** If Kernel flashing is not supported remove backup views */
            ViewGroup parent;
            if ((parent = (ViewGroup) createKernelBackup.getParent()) != null) {
                parent.removeView(createKernelBackup);
            }
            if ((parent = (ViewGroup) lvKernelBackups.getParent()) != null) {
                parent.removeView(lvKernelBackups);
            }
        } else {
            KernelBakAdapter = new ArrayAdapter<>(mContext,
                    R.layout.custom_list_item);
            lvKernelBackups.setAdapter(KernelBakAdapter);
            lvKernelBackups.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                        long arg3) {
                    showBackupPopup(arg1, false);
                }
            });
        }
        if (!mDevice.isRecoverySupported()) {
            /** If Recovery flashing is not supported remove backup views */
            ViewGroup parent;
            if ((parent = (ViewGroup) createRecoveryBackup.getParent()) != null) {
                parent.removeView(createRecoveryBackup);
            }
            if ((parent = (ViewGroup) lvRecoveryBackups.getParent()) != null) {
                parent.removeView(lvRecoveryBackups);
            }
        } else {
            RecoveryBakAdapter = new ArrayAdapter<>(mContext,
                    R.layout.custom_list_item);
            lvRecoveryBackups.setAdapter(RecoveryBakAdapter);
            lvRecoveryBackups.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                        long arg3) {
                    showBackupPopup(arg1, true);
                }
            });
        }
        loadBackups();
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.backup_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void showBackupPopup(final View v, final boolean isRecovery) {
        PopupMenu popup = new PopupMenu(mContext, v);
        popup.getMenuInflater().inflate(R.menu.bakmgr_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                final CharSequence text = ((AppCompatTextView) v).getText();
                try {
                    if (text != null) {
                        final String FileName = text.toString();

                        final AppCompatDialog dialog = new AppCompatDialog(mContext);
                        dialog.setTitle(R.string.setname);
                        dialog.setContentView(R.layout.dialog_input);
                        final AppCompatButton bGo = (AppCompatButton) dialog.findViewById(R.id.bGoBackup);
                        final AppCompatEditText etFileName = (AppCompatEditText) dialog.findViewById(R.id.etFileName);
                        final File path = isRecovery ?
                                Const.PathToRecoveryBackups : Const.PathToKernelBackups;
                        switch (menuItem.getItemId()) {
                            case R.id.iRestore:
                                FlashUtil RestoreUtil = new FlashUtil(mActivity, new File(path, FileName),
                                        isRecovery ? FlashUtil.JOB_RESTORE_RECOVERY : FlashUtil.JOB_RESTORE_KERNEL);
                                RestoreUtil.execute();
                                return true;
                            case R.id.iRename:
                                etFileName.setHint(FileName);
                                bGo.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {

                                        String Name;
                                        if (etFileName.getText() != null && etFileName.isEnabled()
                                                && !etFileName.getText().toString().equals("")) {
                                            Name = etFileName.getText().toString();
                                        } else {
                                            Name = String.valueOf(etFileName.getHint());
                                        }

                                        if (!Name.endsWith(mDevice.getRecoveryExt())) {
                                            Name = Name + mDevice.getRecoveryExt();
                                        }

                                        File renamedBackup = new File(path, Name);

                                        if (renamedBackup.exists()) {
                                            Toast
                                                    .makeText(mActivity, R.string.backupalready, Toast.LENGTH_SHORT)
                                                    .show();
                                        } else {
                                            File Backup = new File(path, FileName);
                                            if (Backup.renameTo(renamedBackup)) {
                                                loadBackups();
                                            } else {
                                                Toast
                                                        .makeText(mActivity, R.string.rename_failed, Toast.LENGTH_SHORT)
                                                        .show();
                                            }

                                        }
                                        dialog.dismiss();
                                    }
                                });
                                dialog.show();
                                return true;
                            case R.id.iDeleteBackup:
                                if (new File(path, text.toString()).delete()) {
                                    Toast.makeText(mActivity, mContext.getString(R.string.bak_deleted),
                                            Toast.LENGTH_SHORT).show();
                                    loadBackups();
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                } catch (Exception e) {
                    if (e.getMessage().contains("EINVAL") && text.toString().contains(":")) {
                        AlertDialog.Builder adialog = new AlertDialog.Builder(mContext);
                        adialog.setMessage(R.string.check_name);
                        adialog.setMessage(R.string.ok);
                        adialog.show();
                    }
                    mActivity.addError(Const.RASHR_TAG, e, false);
                    return false;
                }
                return false;
            }
        });
        popup.show();
    }

    public void createBackup(final boolean RecoveryBackup) {
        String prefix;
        String CurrentName;
        String EXT;
        if (RecoveryBackup) {
            prefix = "recovery";
            EXT = mDevice.getRecoveryExt();
            CurrentName = mDevice.getRecoveryVersion();
        } else {
            prefix = "kernel";
            EXT = mDevice.getKernelExt();
            CurrentName = mDevice.getKernelVersion();
        }
        final AppCompatDialog dialog = new AppCompatDialog(mContext);
        dialog.setTitle(R.string.setname);
        dialog.setContentView(R.layout.dialog_input);
        final AppCompatButton bGoBackup = (AppCompatButton) dialog.findViewById(R.id.bGoBackup);
        final AppCompatEditText etFileName = (AppCompatEditText) dialog.findViewById(R.id.etFileName);
        final AppCompatCheckBox optName = (AppCompatCheckBox) dialog.findViewById(R.id.cbOptInput);
        final String NameHint = prefix + "-from-" + Calendar.getInstance().get(Calendar.DATE)
                + "-" + Calendar.getInstance().get(Calendar.MONTH)
                + "-" + Calendar.getInstance().get(Calendar.YEAR)
                + "-" + Calendar.getInstance().get(Calendar.HOUR)
                + "-" + Calendar.getInstance().get(Calendar.MINUTE) + EXT;
        optName.setText(CurrentName);
        optName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etFileName.setEnabled(!optName.isChecked());
            }
        });

        etFileName.setHint(NameHint);
        bGoBackup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String EXT;
                File Path;
                final int JOB;
                if (RecoveryBackup) {
                    EXT = mDevice.getRecoveryExt();
                    Path = Const.PathToRecoveryBackups;
                    JOB = FlashUtil.JOB_BACKUP_RECOVERY;
                } else {
                    EXT = mDevice.getKernelExt();
                    Path = Const.PathToKernelBackups;
                    JOB = FlashUtil.JOB_BACKUP_KERNEL;
                }

                CharSequence Name = "";
                if (optName.isChecked()) {
                    Name = optName.getText() + EXT;
                } else {
                    if (etFileName.getText() != null && !etFileName.getText().toString().equals("")) {
                        Name = etFileName.getText().toString();
                    }

                    if (Name.equals("")) {
                        Name = String.valueOf(etFileName.getHint());
                    }

                    if (!Name.toString().endsWith(EXT)) {
                        Name = Name + EXT;
                    }
                }

                final File fBACKUP = new File(Path, Name.toString());
                if (fBACKUP.exists()) {
                    Toast
                            .makeText(mActivity, R.string.backupalready, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    final FlashUtil BackupCreator = new FlashUtil(mActivity, fBACKUP, JOB);
                    BackupCreator.setRunAtEnd(new Runnable() {
                        @Override
                        public void run() {
                            loadBackups();
                        }
                    });
                    BackupCreator.execute();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void loadBackups() {

        if (RecoveryBakAdapter != null) {
            if (mDevice.isRecoveryDD() || mDevice.isRecoveryMTD()) {
                if (Const.PathToRecoveryBackups.listFiles() != null) {
                    ArrayList<File> RecoveryBakFiles = new ArrayList<>();
                    File FileList[] = Const.PathToRecoveryBackups.listFiles();
                    if (FileList != null) {
                        RecoveryBakFiles.addAll(Arrays.asList(FileList));
                    }
                    RecoveryBakAdapter.clear();
                    for (File backup : RecoveryBakFiles) {
                        if (!backup.isDirectory()) RecoveryBakAdapter.add(backup.getName());
                    }
                }
            }
        }

        if (KernelBakAdapter != null) {
            if (mDevice.isKernelDD() || mDevice.isKernelMTD()) {
                if (Const.PathToKernelBackups.listFiles() != null) {
                    ArrayList<File> KernelBakList = new ArrayList<>();
                    File FileList[] = Const.PathToKernelBackups.listFiles();
                    if (FileList != null) {
                        KernelBakList.addAll(Arrays.asList(FileList));
                    }
                    KernelBakAdapter.clear();
                    for (File backup : KernelBakList) {
                        if (!backup.isDirectory()) KernelBakAdapter.add(backup.getName());
                    }
                }
            }
        }
    }

    public void setActivity(RashrActivity activity) {
        mActivity = activity;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.FlashItem:
                mActivity.switchTo(FlashFragment.newInstance(mActivity));
                break;
        }
        return false;
    }
}
