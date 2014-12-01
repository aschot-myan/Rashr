package de.mkrtchyan.recoverytools;

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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.Toolbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Notifyer;
import donations.DonationsFragment;

public class RashrActivity extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        FlashFragment.OnFragmentInteractionListener,
        ScriptManagerFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        FlashAsFragment.OnFragmentInteractionListener {

    public static final String PREF_NAME = "rashr";

    private final File Folder[] = {
            Constants.PathToRashr, Constants.PathToRecoveries, Constants.PathToKernel,
            Constants.PathToStockRecovery, Constants.PathToCWM, Constants.PathToTWRP,
            Constants.PathToPhilz, Constants.PathToStockKernel, Constants.PathToRecoveryBackups,
            Constants.PathToKernelBackups, Constants.PathToUtils
    };
    private final RashrActivity mActivity = this;
    private final Context mContext = this;
    /**
     * Declaring needed objects
     */
    private final ArrayList<String> mERRORS = new ArrayList<>();
    private Shell mShell;
    private Toolbox mToolbox;
    private Device mDevice;

    private ArrayAdapter<String> RecoveryBakAdapter;
    private ArrayAdapter<String> KernelBakAdapter;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private boolean mVersionChanged = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.loading_layout);

        final TextView tvLoading = (TextView) findViewById(R.id.tvLoading);

        final Thread StartThread = new Thread(new Runnable() {
            @Override
            public void run() {
                /** Try to get root access */
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvLoading.setText(R.string.getting_root);
                    }
                });

	            try {
		            mShell = Shell.startRootShell(mContext);
		            mToolbox = new Toolbox(mShell);
                } catch (IOException e) {
                    mActivity.addError(Constants.RASHR_TAG, e, false);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.pbLoading).setVisibility(View.INVISIBLE);
                            tvLoading.setTextColor(Color.RED);
                            tvLoading.setText(R.string.no_root);
                        }
                    });
                    return;
                }

                try {
                    unpackFiles();
                } catch (IOException e) {
                    mActivity.addError(Constants.RASHR_TAG, e, true);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvLoading.setText(R.string.failed_unpack_files);
                            tvLoading.setTextColor(Color.RED);
                        }
                    });
                    return;
                }

                try {
                    File LogCopy = new File(mContext.getFilesDir(), Constants.LastLog.getName() + ".txt");
                    mToolbox.setFilePermissions(Constants.LastLog, "666");
                    mToolbox.copyFile(Constants.LastLog, LogCopy, false, false);
                } catch (Exception e) {
                    mActivity.addError(Constants.RASHR_TAG, e, false);
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvLoading.setText(R.string.reading_device);
                    }
                });
	            if (mDevice == null)
		            mDevice = new Device(mActivity);
                /** Creating needed folder and unpacking files */
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvLoading.setText(R.string.loading_data);
                    }
                });
                for (File i : Folder) {
                    if (!i.exists()) {
                        if (i.mkdir()) {
                            mActivity.addError(Constants.RASHR_TAG,
                                    new IOException(i.getAbsolutePath() + " can't be created!"), true);
                        }
                    }
                }

                /** If device is not supported, you can report it now or close the App */
                if (!mDevice.isRecoverySupported() && !mDevice.isKernelSupported()) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showDeviceNotSupportedDialog();
                        }
                    });
                } else {
                    Common.setBooleanPref(mContext, PREF_NAME, Constants.PREF_KEY_SHOW_UNIFIED, true);
                    if (!Common.getBooleanPref(mContext, PREF_NAME, Constants.PREF_KEY_FIRST_RUN)) {
                        /** Setting first start configuration */
                        Common.setBooleanPref(mContext, PREF_NAME, Constants.PREF_KEY_ADS, true);
                        Common.setBooleanPref(mContext, Shell.PREF_NAME, Shell.PREF_LOG, true);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showUsageWarning();
                            }
                        });
                    } else {
                        /** Checking if version has changed */
                        try {
                            PackageInfo pInfo;
                            String pName;
                            PackageManager pManager;
                            if ((pName = getPackageName()) != null
                                    && (pManager = getPackageManager()) != null
                                    && (pInfo = pManager.getPackageInfo(pName, 0)) != null) {
                                final int previous_version = Common.getIntegerPref(mContext, PREF_NAME,
                                        Constants.PREF_KEY_CUR_VER);
                                final int current_version = pInfo.versionCode;
                                mVersionChanged = current_version > previous_version;
                                Common.setIntegerPref(mContext, PREF_NAME, Constants.PREF_KEY_CUR_VER,
                                        current_version);
                            } else {
                                mVersionChanged = true;
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            mActivity.addError(Constants.RASHR_TAG, e, false);
                            mVersionChanged = true;
                        }
                        if (mVersionChanged) {
                            Common.setBooleanPref(mContext, PREF_NAME, Constants.PREF_KEY_ADS, true);
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!Common.getBooleanPref(mContext, PREF_NAME,
                                            Constants.PREF_KEY_HIDE_RATER)) {
                                        Notifyer.showAppRateDialog(mContext, PREF_NAME,
                                                Constants.PREF_KEY_HIDE_RATER);
                                    }
                                    if (mVersionChanged) {
                                        showChangelog();
                                    }
                                }
                            });
                        }
                    }
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            setContentView(R.layout.activity_rashr);
                            mDevice.downloadUtils(mContext);
                            mNavigationDrawerFragment = (NavigationDrawerFragment)
                                    getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
                            mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                                    (DrawerLayout) findViewById(R.id.RashrLayout));
                            setupBackupDrawer();

                            RelativeLayout containerLayout =
                                    (RelativeLayout) findViewById(R.id.containerLayout);
                            AdView ads = (AdView) containerLayout.findViewById(R.id.ads);
                            if (ads != null) {
                                if (Common.getBooleanPref(mContext, RashrActivity.PREF_NAME,
                                        Constants.PREF_KEY_ADS)) {
                                    ads.loadAd(new AdRequest.Builder()
                                            .addTestDevice("53B35F6E356EB90AD09B357DF092BC8F")
                                            .build());
                                } else {
                                    containerLayout.removeView(ads);
                                }
                            }
                            onNavigationDrawerItemSelected(0);
                        } catch (NullPointerException e) {
                            mActivity.addError(Constants.RASHR_TAG, e, true);
                            try {
                                tvLoading.setText(R.string.failed_setup_layout);
                                tvLoading.setTextColor(Color.RED);
                            } catch (RuntimeException ex) {
                                mActivity.addError(Constants.RASHR_TAG, e, true);
                                ex.printStackTrace();

                            }
                        }
                    }
                });


            }
        });
        StartThread.start();
    }

    /**
     * Buttons on BackupDrawer
     */
    public void bCreateBackup(View view) {
        createBackup(view.getTag().equals("recovery"));
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
        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle(R.string.setname);
        dialog.setContentView(R.layout.dialog_input);
        final Button bGoBackup = (Button) dialog.findViewById(R.id.bGoBackup);
        final EditText etFileName = (EditText) dialog.findViewById(R.id.etFileName);
        final CheckBox optName = (CheckBox) dialog.findViewById(R.id.cbOptInput);
        final String NameHint = prefix + "-from-" + Calendar.getInstance().get(Calendar.DATE)
                + "-" + Calendar.getInstance().get(Calendar.MONTH)
                + "-" + Calendar.getInstance().get(Calendar.YEAR)
                + "-" + Calendar.getInstance().get(Calendar.HOUR)
                + ":" + Calendar.getInstance().get(Calendar.MINUTE) + EXT;
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
                    Path = Constants.PathToRecoveryBackups;
                    JOB = FlashUtil.JOB_BACKUP_RECOVERY;
                } else {
                    EXT = mDevice.getKernelExt();
                    Path = Constants.PathToKernelBackups;
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

    public void report(final boolean isCancelable, final String message) {
        final Dialog reportDialog = new Dialog(mContext);
        reportDialog.setTitle(R.string.commentar);
        reportDialog.setContentView(R.layout.dialog_comment);
        final EditText text = (EditText) reportDialog.findViewById(R.id.etComment);
        if (!message.equals("")) text.setText(message);
        reportDialog.setCancelable(isCancelable);
        new Thread(new Runnable() {
            @Override
            public void run() {
                /** Creates a report Email including a Comment and important device infos */
                final Button bGo = (Button) reportDialog.findViewById(R.id.bGo);
                bGo.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (!Common.getBooleanPref(mContext, PREF_NAME, Constants.PREF_KEY_ADS))
                            Toast
                                    .makeText(mActivity, R.string.please_ads, Toast.LENGTH_SHORT)
                                    .show();
                        Toast
                                .makeText(mActivity, R.string.donate_to_support, Toast.LENGTH_SHORT)
                                .show();
                        try {
                            ArrayList<File> files = new ArrayList<>();
                            File TestResults = new File(mContext.getFilesDir(), "results.txt");
                            try {
                                if (TestResults.exists()) {
                                    if (TestResults.delete()) {
                                        FileOutputStream fos = openFileOutput(TestResults.getName(), Context.MODE_PRIVATE);
                                        fos.write(("Rashr:\n\n" + mShell.execCommand("ls -lR " + Constants.PathToRashr.getAbsolutePath()) +
                                                "\nCache Tree:\n" + mShell.execCommand("ls -lR /cache") + "\n" +
                                                "\nMTD result:\n" + mShell.execCommand("cat /proc/mtd") + "\n" +
                                                "\nDevice Tree:\n\n" + mShell.execCommand("ls -lR /dev")).getBytes());
                                    }
                                    files.add(TestResults);
                                }
                            } catch (Exception e) {
                                mActivity.addError(Constants.RASHR_TAG, e, false);
                            }
                            if (getPackageManager() != null) {
                                PackageInfo pInfo = mActivity.getPackageManager()
                                        .getPackageInfo(mActivity.getPackageName(), 0);

                                String comment = "";
                                if (text.getText() != null) comment = text.getText().toString();
                                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ashotmkrtchyan1995@gmail.com"});
                                intent.putExtra(Intent.EXTRA_SUBJECT, "Rashr " + pInfo.versionName + " report");
                                String message = "Package Infos:" +
                                        "\n\nName: " + pInfo.packageName +
                                        "\nVersion Code: " + pInfo.versionCode;
                                message +=
                                        "\n\n\nProduct Info: " +
                                                "\n\nManufacture: " + Build.MANUFACTURER + " (" + mDevice.getManufacture() + ") " +
                                                "\nDevice: " + Build.DEVICE + " (" + mDevice.getName() + ")" +
                                                "\nBoard: " + Build.BOARD +
                                                "\nBrand: " + Build.BRAND +
                                                "\nModel: " + Build.MODEL +
                                                "\nFingerprint: " + Build.FINGERPRINT +
                                                "\nAndroid SDK Level: " + Build.VERSION.CODENAME + " (" + Build.VERSION.SDK_INT + ")";

                                if (mDevice.isRecoverySupported()) {
                                    message += "\n\nRecovery Path: " + mDevice.getRecoveryPath() +
                                            "\nRecovery Version: " + mDevice.getRecoveryVersion() +
                                            "\nRecovery MTD: " + mDevice.isRecoveryMTD() +
                                            "\nRecovery DD: " + mDevice.isRecoveryDD() +
                                            "\nStock: " + mDevice.isStockRecoverySupported() +
                                            "\nCWM: " + mDevice.isCwmRecoverySupported() +
                                            "\nTWRP: " + mDevice.isTwrpRecoverySupported() +
                                            "\nPHILZ: " + mDevice.isPhilzRecoverySupported();
                                }
                                if (mDevice.isKernelSupported()) {
                                    message += "\n\nKernel Path: " + mDevice.getKernelPath() +
                                            "\nKernel Version: " + mDevice.getKernelVersion() +
                                            "\nKernel MTD: " + mDevice.isKernelMTD() +
                                            "\nKernel DD: " + mDevice.isKernelDD();
                                }
                                if (!comment.equals("")) {
                                    message +=
                                            "\n\n\n===========COMMENT==========\n"
                                                    + comment +
                                                    "\n=========COMMENT END========\n";
                                }
                                message +=
                                        "\n===========PREFS==========\n"
                                                + getAllPrefs() +
                                                "\n=========PREFS END========\n";

                                intent.putExtra(Intent.EXTRA_TEXT, message);
                                files.add(new File(mContext.getFilesDir(), Shell.Logs));
                                files.add(new File(mContext.getFilesDir(), "last_log.txt"));
                                ArrayList<Uri> uris = new ArrayList<>();
                                for (File i : files) {
                                    try {
                                        mToolbox.setFilePermissions(i, "666");
                                        uris.add(Uri.fromFile(i));
                                    } catch (Exception e) {
                                        mActivity.addError(Constants.RASHR_TAG, e, false);
                                    }
                                }
                                if (mERRORS.size() > 0) {
                                    message += "ERRORS:\n";
                                    for (String error : mERRORS) {
                                        message += error + "\n";
                                    }
                                }

                                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                                startActivity(Intent.createChooser(intent, "Send over Gmail"));
                                reportDialog.dismiss();
                            }
                        } catch (Exception e) {
                            reportDialog.dismiss();
                            mActivity.addError(Constants.RASHR_TAG, e, false);
                        }
                    }
                });
            }
        }).start();
        reportDialog.setCancelable(isCancelable);
        reportDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mNavigationDrawerFragment.isDrawerOpen()) {
                    mNavigationDrawerFragment.closeDrawer();
                } else {
                    mNavigationDrawerFragment.openDrawer();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChangelog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(R.string.changelog);
        WebView changes = new WebView(mContext);
        changes.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        changes.setWebViewClient(new WebViewClient());
        changes.loadUrl("https://raw.githubusercontent.com/DsLNeXuS/Rashr/master/CHANGELOG.md");
        changes.clearCache(true);
        dialog.setView(changes);
        dialog.show();
    }

    private void showUsageWarning() {
        if (mDevice.isRecoverySupported() || mDevice.isKernelSupported()) {
            final AlertDialog.Builder WarningDialog = new AlertDialog.Builder(mContext);
            WarningDialog.setTitle(R.string.warning);
            WarningDialog.setMessage(String.format(getString(R.string.bak_warning),
                    Constants.PathToRecoveryBackups + " & " + Constants.PathToKernelBackups));
            WarningDialog.setPositiveButton(R.string.backup, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    createBackup(mDevice.isRecoveryDD() || mDevice.isRecoveryMTD());
                    createBackup(!mDevice.isKernelDD() && !mDevice.isKernelMTD());
                    Common.setBooleanPref(mContext, PREF_NAME, Constants.PREF_KEY_FIRST_RUN, true);
                }
            });
            WarningDialog.setNegativeButton(R.string.risk, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Common.setBooleanPref(mContext, PREF_NAME, Constants.PREF_KEY_FIRST_RUN, true);
                }
            });
            WarningDialog.setCancelable(false);
            WarningDialog.show();
        }
    }

    public void showPopup(int Menu, final View v) {
        PopupMenu popup = new PopupMenu(mContext, v);
        popup.getMenuInflater().inflate(Menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {
                    CharSequence text = ((TextView) v).getText();
                    if (text != null) {
                        final String FileName = text.toString();

                        final Dialog dialog = new Dialog(mContext);
                        dialog.setTitle(R.string.setname);
                        dialog.setContentView(R.layout.dialog_input);
                        final Button bGo = (Button) dialog.findViewById(R.id.bGoBackup);
                        final EditText etFileName = (EditText) dialog.findViewById(R.id.etFileName);

                        switch (menuItem.getItemId()) {
                            case R.id.iRestoreRecovery:
                                final FlashUtil RestoreRecoveryUtil = new FlashUtil(mActivity, new File(Constants.PathToRecoveryBackups, FileName),
                                        FlashUtil.JOB_RESTORE_RECOVERY);
                                RestoreRecoveryUtil.execute();
                                return true;
                            case R.id.iRenameRecovery:
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

                                        File renamedBackup =
                                                new File(Constants.PathToRecoveryBackups, Name);

                                        if (renamedBackup.exists()) {
                                            Toast
                                                    .makeText(mActivity, R.string.backupalready, Toast.LENGTH_SHORT)
                                                    .show();
                                        } else {
                                            File Backup =
                                                    new File(Constants.PathToRecoveryBackups, FileName);
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
                            case R.id.iDeleteRecoveryBackup:
                                if (new File(Constants.PathToRecoveryBackups, text.toString()).delete()) {
                                    Toast.makeText(mActivity, mContext.getString(R.string.bak_deleted),
                                            Toast.LENGTH_SHORT).show();
                                    loadBackups();
                                }
                                return true;
                            case R.id.iRestoreKernel:
                                final FlashUtil RestoreKernelUtil = new FlashUtil(mActivity,
                                        new File(Constants.PathToKernelBackups, text.toString()), FlashUtil.JOB_RESTORE_KERNEL);
                                RestoreKernelUtil.execute();
                                return true;
                            case R.id.iRenameKernel:
                                etFileName.setHint(text);
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

                                        if (!Name.endsWith(mDevice.getKernelExt())) {
                                            Name = Name + mDevice.getKernelExt();
                                        }

                                        final File renamedBackup = new File(Constants.PathToKernelBackups, Name);

                                        if (renamedBackup.exists()) {
                                            Toast
                                                    .makeText(mActivity, R.string.backupalready, Toast.LENGTH_SHORT)
                                                    .show();
                                        } else {
                                            File Backup = new File(Constants.PathToKernelBackups, FileName);
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
                            case R.id.iDeleteKernelBackup:

                                if (new File(Constants.PathToKernelBackups, text.toString()).delete()) {
                                    Toast
                                            .makeText(mActivity, mContext.getString(R.string.bak_deleted),
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    loadBackups();
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                } catch (Exception e) {
                    mActivity.addError(Constants.RASHR_TAG, e, true);
                    return false;
                }
                return false;
            }
        });
        popup.show();
    }

    private void showDeviceNotSupportedDialog() {
        AlertDialog.Builder DeviceNotSupported = new AlertDialog.Builder(mContext);
        DeviceNotSupported.setTitle(R.string.warning);
        DeviceNotSupported.setMessage(R.string.not_supportded);
        DeviceNotSupported.setPositiveButton(R.string.report, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                report(false, "");
            }
        });
        DeviceNotSupported.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                exit();
            }
        });
        if (!Constants.LastLog.exists()) {
            DeviceNotSupported.setNeutralButton(R.string.sReboot, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        mToolbox.reboot(Toolbox.REBOOT_RECOVERY);
                    } catch (Exception e) {
                        mActivity.addError(Constants.RASHR_TAG, e, false);
                    }
                }
            });
        }
        DeviceNotSupported.setCancelable(BuildConfig.DEBUG);
        DeviceNotSupported.show();
    }

    private void unpackFiles() throws IOException {
        File RecoveryCollectionFile = new File(mContext.getFilesDir(), "recovery_sums");
        File KernelCollectionFile = new File(mContext.getFilesDir(), "kernel_sums");
        File flash_image = new File(getFilesDir(), "flash_image");
        Common.pushFileFromRAW(mContext, flash_image, R.raw.flash_image, mVersionChanged);
        File dump_image = new File(getFilesDir(), "dump_image");
        Common.pushFileFromRAW(mContext, dump_image, R.raw.dump_image, mVersionChanged);
        File busybox = new File(mContext.getFilesDir(), "busybox");
        Common.pushFileFromRAW(mContext, busybox, R.raw.busybox, mVersionChanged);
        Common.pushFileFromRAW(mContext, RecoveryCollectionFile, R.raw.recovery_sums, mVersionChanged);
        Common.pushFileFromRAW(mContext, KernelCollectionFile, R.raw.kernel_sums, mVersionChanged);
        File PartLayoutsZip = new File(mContext.getFilesDir(), "partlayouts.zip");
        Common.pushFileFromRAW(mContext, PartLayoutsZip, R.raw.partlayouts, mVersionChanged);
        File loki_patch = new File(mContext.getFilesDir(), "loki_patch");
        Common.pushFileFromRAW(mContext, loki_patch, R.raw.loki_patch, mVersionChanged);
        File loki_flash = new File(mContext.getFilesDir(), "loki_flash");
        Common.pushFileFromRAW(mContext, loki_flash, R.raw.loki_flash, mVersionChanged);
    }

    public void loadBackups() {

        if (RecoveryBakAdapter != null) {
            if (mDevice.isRecoveryDD() || mDevice.isRecoveryMTD()) {
                if (Constants.PathToRecoveryBackups.listFiles() != null) {
                    ArrayList<File> RecoveryBakFiles = new ArrayList<>();
                    File FileList[] = Constants.PathToRecoveryBackups.listFiles();
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
                if (Constants.PathToKernelBackups.listFiles() != null) {
                    ArrayList<File> KernelBakList = new ArrayList<>();
                    File FileList[] = Constants.PathToKernelBackups.listFiles();
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

    public void setupBackupDrawer() throws NullPointerException {
        LayoutInflater inflater = getLayoutInflater();
        DrawerLayout mRashrLayout = (DrawerLayout) findViewById(R.id.RashrLayout);
        DrawerLayout mBackupDrawer =
                (DrawerLayout) inflater.inflate(R.layout.backup_drawer, mRashrLayout, true);
        if (mBackupDrawer != null) {
            ViewGroup BackupDrawerParent = (ViewGroup) mBackupDrawer.getParent();
            if (mDevice.isRecoveryOverRecovery()) {
                if (BackupDrawerParent != null) BackupDrawerParent.removeView(mBackupDrawer);
            } else {
                View createKernelBackup = findViewById(R.id.bCreateKernelBackup);
                View kernelBackups = findViewById(R.id.lvKernelBackups);
                View createRecoveryBackup = findViewById(R.id.bCreateRecoveryBackup);
                View recoveryBackups = findViewById(R.id.lvRecoveryBackups);
                if (!mDevice.isKernelSupported()) {
                    /** If Kernel flashing is not supported remove backup views */
                    ViewGroup parent;
                    if ((parent = (ViewGroup) createKernelBackup.getParent()) != null) {
                        parent.removeView(createKernelBackup);
                    }
                    if ((parent = (ViewGroup) kernelBackups.getParent()) != null) {
                        parent.removeView(kernelBackups);
                    }
                } else {
                    KernelBakAdapter = new ArrayAdapter<>(mContext,
                            R.layout.custom_list_item);
                    final ListView lvKernelBackups = (ListView) mActivity.findViewById(R.id.lvKernelBackups);
                    lvKernelBackups.setAdapter(KernelBakAdapter);
                    lvKernelBackups.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                                long arg3) {
                            showPopup(R.menu.bakmgr_kernel_menu, arg1);
                        }
                    });
                }
                if (!mDevice.isRecoverySupported()) {
                    /** If Recovery flashing is not supported remove backup views */
                    ViewGroup parent;
                    if ((parent = (ViewGroup) createRecoveryBackup.getParent()) != null) {
                        parent.removeView(createRecoveryBackup);
                    }
                    if ((parent = (ViewGroup) recoveryBackups.getParent()) != null) {
                        parent.removeView(recoveryBackups);
                    }
                } else {
                    RecoveryBakAdapter = new ArrayAdapter<>(mContext,
                            R.layout.custom_list_item);
                    final ListView lvRecoveryBackups = (ListView) mActivity.findViewById(R.id.lvRecoveryBackups);
                    lvRecoveryBackups.setAdapter(RecoveryBakAdapter);
                    lvRecoveryBackups.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                                long arg3) {
                            showPopup(R.menu.bakmgr_recovery_menu, arg1);
                        }
                    });
                }
            }
            loadBackups();
        }
    }

    public void exit() {
        finish();
        System.exit(0);
    }

    /**
     * @return All Preferences as String
     */
    public String getAllPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String Prefs = "";
        Map<String, ?> prefsMap = prefs.getAll();
        try {
            for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
                /**
                 * Skip following Prefs (PREF_KEY_KERNEL_HISTORY, PREF_KEY_RECOVERY_HISTORY ...)
                 */
                try {
                    if (!entry.getKey().contains(Constants.PREF_KEY_HISTORY)
                            && !entry.getKey().contains(FlashUtil.PREF_KEY_FLASH_COUNTER)) {
                        Prefs += entry.getKey() + ": " + entry.getValue().toString() + "\n";
                    }
                } catch (NullPointerException e) {
                    mActivity.addError(Constants.RASHR_TAG, e, false);
                }
            }
        } catch (NullPointerException e) {
            mActivity.addError(Constants.RASHR_TAG, e, false);
        }

        return Prefs;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = null;
        position++;
        String action;
        if ((action = mActivity.getIntent().getAction()) != null && action.equals(Intent.ACTION_VIEW)) {
            if (mActivity.getIntent().getData().toString().endsWith(".zip")) {
                File zip = new File(getIntent().getData().getPath());
                if (zip.exists()) fragment = ScriptManagerFragment.newInstance(this, zip);
            } else {
                File img = new File(getIntent().getData().getPath());
                if (img.exists()) fragment = FlashAsFragment.newInstance(this, img, true);
            }
        } else {
            switch (position) {
                case 1:
                    fragment = FlashFragment.newInstance(this);
                    break;
                case 2:
                    fragment = ScriptManagerFragment.newInstance(this, null);
                    break;
                case 3:
                    fragment = DonationsFragment.newInstance(BuildConfig.DEBUG, Constants.GOOGLE_PLAY,
                            Constants.GOOGLE_PUBKEY, Constants.GOOGLE_CATALOG,
                            getResources().getStringArray(R.array.donation_google_catalog_values),
                            Constants.FLATTR, Constants.FLATTR_PROJECT_URL, Constants.FLATTR_URL);
                    break;
                case 4:
                    fragment = SettingsFragment.newInstance();
                    break;
                case 5:
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://forum.xda-developers.com/showthread.php?t=2334554")));
                    break;
            }
        }
        if (fragment != null) {
            ft
                    .setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out)
                    .replace(R.id.container, fragment)
					.commitAllowingStateLoss();
        }
    }

    @Override
    public void onFragmentInteraction(int id) {
        if (id == R.id.bReport) {
            report(true, "");
        } else if (id == Constants.OPEN_RASHR_FRAGMENT) {
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = FlashFragment.newInstance(this);
            fm.beginTransaction()
                    .setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out)
                    .replace(R.id.container, fragment)
					.commitAllowingStateLoss();
        } else if (id == Constants.OPEN_FLASH_AS_FRAGMENT) {
            File img = new File(getIntent().getData().getPath());
            if (img.exists()) {
                FragmentManager fm = getSupportFragmentManager();
                Fragment fragment = FlashAsFragment.newInstance(this, img, false);
                fm.beginTransaction()
                        .setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out)
                        .replace(R.id.container, fragment)
						.commitAllowingStateLoss();
            }
        }
    }

    public Device getDevice() {
        return mDevice;
    }

    /**
     * Share instances with root access instance with all other Classes
     */
    public Shell getShell() {
        return mShell;
    }
    public Toolbox getToolbox() {
        return mToolbox;
    }

    public void addError(String TAG, final Exception e, boolean serious) {
        mERRORS.add(TAG + ": " + (e != null ? e.toString() : ""));
        if (e != null) {
            if (serious) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        report(true, e.toString());
                        Notifyer.showExceptionToast(mContext, e);
                    }
                });
            }
        }
    }
}