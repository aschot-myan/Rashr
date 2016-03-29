package de.mkrtchyan.recoverytools;


import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.PopupMenu;
import android.util.TypedValue;
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
import java.util.Calendar;

import de.mkrtchyan.recoverytools.view.SlidingTabLayout;


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
public class BackupRestoreFragment extends Fragment {

    private RashrActivity mActivity;
    private Context mContext;
    private ViewPager mPager;
    private BackupRestorePagerAdapter mAdapter;

    public BackupRestoreFragment() {
    }

    public static BackupRestoreFragment newInstance(RashrActivity activity) {
        BackupRestoreFragment fragment = new BackupRestoreFragment();
        fragment.mActivity = activity;
        return fragment;
    }

    public void showPopup(final View v) {
        PopupMenu popup = new PopupMenu(mActivity, v);
        popup.getMenuInflater().inflate(R.menu.bakmgr_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem menuItem) {

                final CharSequence text = ((AppCompatTextView) v).getText();

                final String FileName = text.toString();

                final AppCompatDialog dialog = new AppCompatDialog(mActivity);
                dialog.setTitle(R.string.setname);
                dialog.setContentView(R.layout.dialog_input);
                final AppCompatButton bGo = (AppCompatButton) dialog.findViewById(R.id.bGoBackup);
                final AppCompatEditText etFileName = (AppCompatEditText) dialog.findViewById(R.id.etFileName);
                if (bGo == null || etFileName == null) return false;
                //If current item is 0 (first item) a recovery backup will be edited or created
                final File path = mPager.getCurrentItem() == 0 ?
                        Const.PathToRecoveryBackups : Const.PathToKernelBackups;

                try {
                    switch (menuItem.getItemId()) {
                        case R.id.iRestore:
                            File backup = new File(path, FileName);
                            FlashUtil RestoreUtil = new FlashUtil(mActivity, backup,
                                    mPager.getCurrentItem() == 0 ?
                                            FlashUtil.JOB_RESTORE_RECOVERY : FlashUtil.JOB_RESTORE_KERNEL);
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

                                        if (!Name.endsWith(RashrApp.DEVICE.getRecoveryExt())) {
                                        //Append extension
                                        Name += RashrApp.DEVICE.getRecoveryExt();
                                    }

                                    File renamedBackup = new File(path, Name);

                                    if (renamedBackup.exists()) {
                                        Toast
                                                .makeText(mActivity, R.string.backupalready, Toast.LENGTH_SHORT)
                                                .show();
                                        // if backup already exists, let the user chose a new name
                                        onMenuItemClick(menuItem);
                                    } else {
                                        File Backup = new File(path, FileName);
                                        if (Backup.renameTo(renamedBackup)) {
                                            mAdapter.reload();
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
                            backup = new File(path, text.toString());
                            if (backup.delete()) {
                                Toast.makeText(mActivity, mContext.getString(R.string.bak_deleted),
                                        Toast.LENGTH_SHORT).show();
                            }
                            mAdapter.reload();
                            return true;
                        default:
                            return false;
                    }
                } catch (Exception e) {
                    if (e.getMessage().contains("EINVAL") && text.toString().contains(":")) {
                        AlertDialog.Builder adialog = new AlertDialog.Builder(mContext);
                        adialog.setMessage(R.string.check_name);
                        adialog.setMessage(R.string.ok);
                        adialog.show();
                    }
                    RashrApp.ERRORS.add(Const.RASHR_TAG + " " + e);
                    return false;
                }
            }
        });
        popup.show();
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
        mPager = (ViewPager) root.findViewById(R.id.vpBackupRestore);
        mAdapter = new BackupRestorePagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mAdapter);
        SlidingTabLayout slidingTabLayout =
                (SlidingTabLayout) root.findViewById(R.id.stlBackupRestore);
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = mContext.getTheme();
                theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
                return typedValue.data;
            }

            @Override
            public int getDividerColor(int position) {
                return 0;
            }
        });
        slidingTabLayout.setViewPager(mPager);
        final FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.fabCreateBackup);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isRecovery = mPager.getCurrentItem() == 0;
                createBackup(isRecovery);
            }
        });
        fab.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add_white));
        slidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    if (RashrApp.DEVICE.isRecoveryDD() || RashrApp.DEVICE.isRecoveryMTD()) {
                        fab.setVisibility(View.VISIBLE);
                    } else {
                        fab.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (RashrApp.DEVICE.isKernelDD() || RashrApp.DEVICE.isKernelMTD()) {
                        fab.setVisibility(View.VISIBLE);
                    } else {
                        fab.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mAdapter.getRecoveryBackupFragment().getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!(RashrApp.DEVICE.isRecoveryDD() || RashrApp.DEVICE.isRecoveryMTD())) {
                    Toast.makeText(mContext, "Operation not supported", Toast.LENGTH_SHORT).show();
                } else {
                    showPopup(view);
                }
            }
        });
        mAdapter.getKernelBackupFragment().getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!(RashrApp.DEVICE.isKernelDD() || RashrApp.DEVICE.isKernelMTD())) {
                    Toast.makeText(mContext, "Operation not supported", Toast.LENGTH_SHORT).show();
                } else {
                    showPopup(view);
                }
            }
        });
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.backup_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void createBackup(final boolean RecoveryBackup) {
        String prefix;
        String CurrentName;
        String EXT;
        if (RecoveryBackup) {
            prefix = "recovery";
            EXT = RashrApp.DEVICE.getRecoveryExt();
            CurrentName = RashrApp.DEVICE.getRecoveryVersion();
        } else {
            prefix = "kernel";
            EXT = RashrApp.DEVICE.getKernelExt();
            CurrentName = RashrApp.DEVICE.getKernelVersion();
        }
        final AppCompatDialog dialog = new AppCompatDialog(mContext);
        dialog.setTitle(R.string.setname);
        dialog.setContentView(R.layout.dialog_input);
        final AppCompatButton bGoBackup = (AppCompatButton) dialog.findViewById(R.id.bGoBackup);
        final AppCompatEditText etFileName = (AppCompatEditText) dialog.findViewById(R.id.etFileName);
        final AppCompatCheckBox optName = (AppCompatCheckBox) dialog.findViewById(R.id.cbOptInput);
        if (bGoBackup == null || etFileName == null || optName == null) return;
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
                    EXT = RashrApp.DEVICE.getRecoveryExt();
                    Path = Const.PathToRecoveryBackups;
                    JOB = FlashUtil.JOB_BACKUP_RECOVERY;
                } else {
                    EXT = RashrApp.DEVICE.getKernelExt();
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
                    BackupCreator.setOnTaskDoneListener(new FlashUtil.OnTaskDoneListener() {
                        @Override
                        public void onSuccess() {
                            mAdapter.reload();
                        }

                        @Override
                        public void onFail(Exception e) {
                            Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    BackupCreator.execute();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
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

    public static class ListFragment extends Fragment {

        private ArrayAdapter<String> mAdapter;
        private ListView mListView;

        public ListFragment() {
        }

        public static ListFragment newInstance(RashrActivity activity) {
            ListFragment fragment = new ListFragment();
            fragment.mListView = new ListView(activity);
            fragment.mAdapter = new ArrayAdapter<>(activity, R.layout.drawer_list_item);
            fragment.mListView.setAdapter(fragment.getAdapter());
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            return mListView;
        }

        public ArrayAdapter<String> getAdapter() {
            return mAdapter;
        }

        public ListView getListView() {
            return mListView;
        }

    }

    public class BackupRestorePagerAdapter extends FragmentPagerAdapter {

        private ListFragment mRecoveryBackupFragment = null;
        private ListFragment mKernelBackupFragment = null;

        public BackupRestorePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return getRecoveryBackupFragment();
                default:
                    return getKernelBackupFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Recovery Backups";
                case 1:
                    return "Kernel Backups";
            }
            return "Rashr";
        }

        public ListFragment getRecoveryBackupFragment() {
            if (mRecoveryBackupFragment != null) {
                return mRecoveryBackupFragment;
            }
            mRecoveryBackupFragment = ListFragment.newInstance(mActivity);
            loadBackups();
            return mRecoveryBackupFragment;
        }

        public ListFragment getKernelBackupFragment() {
            if (mKernelBackupFragment != null) {
                return mKernelBackupFragment;
            }
            mKernelBackupFragment = ListFragment.newInstance(mActivity);
            loadBackups();
            return mKernelBackupFragment;
        }

        public void reload() {
            loadBackups();
        }

        private void loadBackups() {
            if (mRecoveryBackupFragment != null) {
                mRecoveryBackupFragment.getAdapter().clear();
                if (!(RashrApp.DEVICE.isRecoveryDD() || RashrApp.DEVICE.isRecoveryMTD())) {
                    mRecoveryBackupFragment.getAdapter().add(getString(R.string.op_not_supported));
                } else {
                    File path = Const.PathToRecoveryBackups;
                    if (path.listFiles() != null) {
                        File FileList[] = path.listFiles();
                        for (File backup : FileList) {
                            if (!backup.isDirectory()) {
                                mRecoveryBackupFragment.getAdapter().add(backup.getName());
                            }
                        }
                    }
                }
            }
            if (mKernelBackupFragment != null) {
                mKernelBackupFragment.getAdapter().clear();
                if (!(RashrApp.DEVICE.isKernelDD() || RashrApp.DEVICE.isKernelMTD())) {
                    mKernelBackupFragment.getAdapter().add(getString(R.string.op_not_supported));
                } else {
                    File path = Const.PathToKernelBackups;
                    if (path.listFiles() != null) {
                        File FileList[] = path.listFiles();
                        for (File backup : FileList) {
                            if (!backup.isDirectory()) {
                                mKernelBackupFragment.getAdapter().add(backup.getName());
                            }
                        }
                    }
                }
            }

        }
    }
}
