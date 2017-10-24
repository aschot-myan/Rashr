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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.mkrtchyan.recoverytools.view.SlidingTabLayout;

public class BackupActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.vpBackupRestore)
    ViewPager mPager;
    @BindView(R.id.fabCreateBackup)
    FloatingActionButton mFab;
    @BindView(R.id.stlBackupRestore)
    SlidingTabLayout mSlidingTabLayout;
    private BackupActivity mActivity = this;
    private Context mContext = this;
    private BackupRestorePagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        mAdapter = new BackupRestorePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
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
        mSlidingTabLayout.setViewPager(mPager);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isRecovery = mPager.getCurrentItem() == 0;
                createBackup(isRecovery);
            }
        });
        mFab.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add_white));
        mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    if (App.Device.isRecoveryBackupPossible()) {
                        mFab.setVisibility(View.VISIBLE);
                    } else {
                        mFab.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (App.Device.isKernelBackupPossible()) {
                        mFab.setVisibility(View.VISIBLE);
                    } else {
                        mFab.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void showPopup(final View v, final File file) {
        PopupMenu popup = new PopupMenu(mActivity, v);
        popup.getMenuInflater().inflate(R.menu.bakmgr_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem menuItem) {

                final AppCompatDialog dialog = new AppCompatDialog(mActivity);
                dialog.setTitle(R.string.setname);
                dialog.setContentView(R.layout.dialog_input);
                final AppCompatButton bGo = dialog.findViewById(R.id.bGoBackup);
                final AppCompatEditText etFileName = dialog.findViewById(R.id.etFileName);
                if (bGo == null || etFileName == null) return false;
                //If current item is 0 (first item) a recovery backup will be edited or created
                final File path = mPager.getCurrentItem() == 0 ?
                        App.PathToRecoveryBackups : App.PathToKernelBackups;

                try {
                    switch (menuItem.getItemId()) {
                        case R.id.iRestore:
                            FlashUtil RestoreUtil = new FlashUtil(mActivity, file,
                                    mPager.getCurrentItem() == 0 ?
                                            FlashUtil.JOB_RESTORE_RECOVERY : FlashUtil.JOB_RESTORE_KERNEL);
                            RestoreUtil.execute();
                            return true;
                        case R.id.iRename:
                            etFileName.setHint(file.getName());
                            bGo.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {

                                    String Name;
                                    if (etFileName.getText() != null && etFileName.isEnabled()
                                            && !etFileName.getText().toString().equals("")) {
                                        //User has defined a name for the backup. Use it.
                                        Name = etFileName.getText().toString();
                                    } else {
                                        //Use hint as backup name. Normally the correct version
                                        //and Recovery System (if Rashr could read it out of
                                        //"/cache/recovery/last_log"
                                        Name = String.valueOf(etFileName.getHint());
                                    }

                                    if (!Name.endsWith(App.Device.getRecoveryExt())) {
                                        //Append extension
                                        Name += App.Device.getRecoveryExt();
                                    }

                                    File renamedBackup = new File(path, Name);

                                    if (renamedBackup.exists()) {
                                        Toast
                                                .makeText(mActivity, R.string.backupalready, Toast.LENGTH_SHORT)
                                                .show();
                                        // if backup already exists, let the user chose a new name
                                        onMenuItemClick(menuItem);
                                    } else {
                                        if (file.renameTo(renamedBackup)) {
                                            mAdapter.loadBackups();
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
                            if (file.delete()) {
                                Toast.makeText(mActivity, mContext.getString(R.string.bak_deleted),
                                        Toast.LENGTH_SHORT).show();
                            }
                            mAdapter.loadBackups();
                            return true;
                        default:
                            return false;
                    }
                } catch (Exception e) {
                    if (e.getMessage().contains("EINVAL") && file.getName().contains(":")) {
                        AlertDialog.Builder adialog = new AlertDialog.Builder(mContext);
                        adialog.setMessage(R.string.check_name);
                        adialog.setMessage(R.string.ok);
                        adialog.show();
                    }
                    App.ERRORS.add(App.TAG + " " + e);
                    return false;
                }
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
            EXT = App.Device.getRecoveryExt();
            CurrentName = App.Device.getRecoveryVersion();
        } else {
            prefix = "kernel";
            EXT = App.Device.getKernelExt();
            CurrentName = App.Device.getKernelVersion();
        }
        final AppCompatDialog dialog = new AppCompatDialog(mContext);
        dialog.setTitle(R.string.setname);
        dialog.setContentView(R.layout.dialog_input);
        final AppCompatButton bGoBackup = dialog.findViewById(R.id.bGoBackup);
        final AppCompatEditText etFileName = dialog.findViewById(R.id.etFileName);
        final AppCompatCheckBox optName = dialog.findViewById(R.id.cbOptInput);
        if (bGoBackup == null || etFileName == null || optName == null) return;
        final String NameHint = prefix + "-from-" + Calendar.getInstance().get(Calendar.DATE)
                + "-" + Calendar.getInstance().get(Calendar.MONTH)
                + "-" + Calendar.getInstance().get(Calendar.YEAR)
                + "-" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
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
                    EXT = App.Device.getRecoveryExt();
                    Path = App.PathToRecoveryBackups;
                    JOB = FlashUtil.JOB_BACKUP_RECOVERY;
                } else {
                    EXT = App.Device.getKernelExt();
                    Path = App.PathToKernelBackups;
                    JOB = FlashUtil.JOB_BACKUP_KERNEL;
                }

                String Name;
                if (optName.isChecked()) {
                    //Using preset name as filename
                    Name = optName.getText() + EXT;
                } else {
                    if (etFileName.getText() != null && !etFileName.getText().toString().equals("")) {
                        //Use edittext as name
                        Name = etFileName.getText().toString();
                    } else {
                        //Use hint as name
                        Name = String.valueOf(etFileName.getHint());
                    }

                }

                //Adding extension to chosen name
                if (!Name.endsWith(EXT)) {
                    Name = Name + EXT;
                }


                final File fBACKUP = new File(Path, Name);
                if (fBACKUP.exists()) {
                    //Backup exists already do not create new.
                    Toast
                            .makeText(mActivity, R.string.backupalready, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    final FlashUtil BackupCreator = new FlashUtil(mActivity, fBACKUP, JOB);
                    BackupCreator.setOnFlashListener(new FlashUtil.OnFlashListener() {
                        @Override
                        public void onSuccess() {
                            mAdapter.loadBackups();
                        }

                        @Override
                        public void onFail(Exception e) {
                            String msg;
                            if (e != null) {
                                msg = e.toString();
                            } else {
                                msg = getString(R.string.bak_error);
                            }
                            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                    BackupCreator.execute();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static class FileListFragment extends Fragment {

        private RecyclerView mRecyclerView;

        public FileListFragment() {
        }

        public static FileListFragment newInstance(Context context) {
            FileListFragment fragment = new FileListFragment();
            fragment.mRecyclerView = new RecyclerView(context);
            fragment.mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            return fragment;
        }

        public void setAdapter(BackupRestorePagerAdapter.FileAdapter adapter) {
            mRecyclerView.setAdapter(adapter);
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            return mRecyclerView;
        }


        public RecyclerView getRecycler() {
            return mRecyclerView;
        }

    }

    public class BackupRestorePagerAdapter extends FragmentPagerAdapter {

        private FileListFragment mRecoveryBackupFragment = null;
        private FileListFragment mKernelBackupFragment = null;

        public BackupRestorePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (getCount() == 1) {
                        return App.Device.isRecoveryBackupPossible()
                                ? getRecoveryBackupFragment() :
                                getKernelBackupFragment();
                    }
                    return getRecoveryBackupFragment();
                default:
                    return getKernelBackupFragment();
            }
        }

        @Override
        public int getCount() {
            int count = 0;
            if (App.Device.isRecoveryBackupPossible()) {
                count++;
            }
            if (App.Device.isKernelBackupPossible()) {
                count++;
            }
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    if (getCount() == 1) {
                        return App.Device.isRecoveryBackupPossible()
                                ? getString(R.string.sRecoveryBackup) :
                                getString(R.string.sKernelBackup);
                    }
                    return getString(R.string.sRecoveryBackup);
                case 1:
                    return getString(R.string.sKernelBackup);
            }
            return getString(R.string.app_name);
        }

        public FileListFragment getRecoveryBackupFragment() {
            if (mRecoveryBackupFragment != null) {
                return mRecoveryBackupFragment;
            }
            mRecoveryBackupFragment = FileListFragment.newInstance(mContext);
            ArrayList<File> files = new ArrayList<>();
            Collections.addAll(files, App.PathToRecoveryBackups.listFiles());
            mRecoveryBackupFragment.setAdapter(new BackupRestorePagerAdapter.FileAdapter(files));
            loadBackups();
            return mRecoveryBackupFragment;
        }

        public FileListFragment getKernelBackupFragment() {
            if (mKernelBackupFragment != null) {
                return mKernelBackupFragment;
            }
            mKernelBackupFragment = FileListFragment.newInstance(mContext);
            loadBackups();
            return mKernelBackupFragment;
        }

        private void loadBackups() {
            if (mRecoveryBackupFragment != null) {
                List<File> files = new ArrayList<>();
                if (App.Device.isRecoveryDD() || App.Device.isRecoveryMTD()) {
                    File path = App.PathToRecoveryBackups;
                    if (path.listFiles() != null) {
                        File FileList[] = path.listFiles();
                        for (File backup : FileList) {
                            if (!backup.isDirectory()) {
                                files.add(backup);
                            }
                        }
                    }
                }
                mRecoveryBackupFragment.getRecycler().setAdapter(new BackupRestorePagerAdapter.FileAdapter(files));
            }
            if (mKernelBackupFragment != null) {
                List<File> files = new ArrayList<>();
                if (App.Device.isKernelBackupPossible()) {
                    File path = App.PathToKernelBackups;
                    if (path.listFiles() != null) {
                        File FileList[] = path.listFiles();
                        for (File backup : FileList) {
                            if (!backup.isDirectory()) {
                                files.add(backup);
                            }
                        }
                    }
                    mKernelBackupFragment.getRecycler().setAdapter(new BackupRestorePagerAdapter.FileAdapter(files));
                }
            }

        }

        public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

            private List<File> files;

            public FileAdapter(List<File> files) {
                this.files = files;
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.backup_list_item, parent, false);
                return new ViewHolder(v);
            }

            @Override
            public void onBindViewHolder(FileAdapter.ViewHolder holder, int position) {
                System.out.println(position);
                holder.fileName.setText(files.get(position).getName());
                holder.fileSize.setText(((files.get(position).length() / 1024) / 1024) + " MB");
                holder.fileDate.setText(DateFormat.getDateFormat(mContext)
                        .format(files.get(position).lastModified()));
                File parent = files.get(position).getParentFile();
                if (parent != null) {
                    holder.fileParent.setText(parent.getName() + "/");
                }
                final File backup = files.get(position);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopup(v, backup);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return files.size();
            }

            class ViewHolder extends RecyclerView.ViewHolder {

                AppCompatTextView fileName;
                AppCompatTextView fileSize;
                AppCompatTextView fileDate;
                AppCompatTextView fileParent;

                public ViewHolder(View itemView) {
                    super(itemView);
                    fileName = itemView.findViewById(R.id.file_name);
                    fileSize = itemView.findViewById(R.id.file_size);
                    fileDate = itemView.findViewById(R.id.last_edit);
                    fileParent = itemView.findViewById(R.id.file_parent);
                }
            }
        }
    }
}
