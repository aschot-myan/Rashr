package de.mkrtchyan.recoverytools.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;

import de.mkrtchyan.recoverytools.App;
import de.mkrtchyan.recoverytools.R;
import de.mkrtchyan.recoverytools.ReportDialog;
import de.mkrtchyan.utils.Common;
import de.psdev.licensesdialog.LicensesDialog;

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
public class SettingsFragment extends PreferenceFragmentCompat {

    public static void showChangelog(Context AppContext) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(AppContext);
        dialog.setTitle(R.string.changelog);
        WebView changes = new WebView(AppContext);
        changes.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        changes.setWebViewClient(new WebViewClient());
        changes.loadUrl(App.CHANGELOG_URL);
        changes.clearCache(true);
        dialog.setView(changes);
        dialog.show();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        EditTextPreference recovery_path = (EditTextPreference) findPreference(App.PREF_KEY_RECOVERY_PATH);
        recovery_path.setText(App.Preferences.getString(App.PREF_KEY_RECOVERY_PATH, null));
        EditTextPreference kernel_path = (EditTextPreference) findPreference(App.PREF_KEY_KERNEL_PATH);
        kernel_path.setText(App.Preferences.getString(App.PREF_KEY_KERNEL_PATH, null));
        EditTextPreference device_name = (EditTextPreference) findPreference(App.PREF_KEY_DEVICE_NAME);
        device_name.setText(App.Device.getName());
        device_name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                View v = getView();
                if (v == null) return true;
                if (!newValue.toString().equals("")) {
                    Snackbar.make(v, R.string.please_restart, Snackbar.LENGTH_INDEFINITE).show();
                }
                return true;
            }
        });
        Preference changelog = findPreference(App.PREF_KEY_CHANGELOG);
        changelog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showChangelog(getContext());
                return false;
            }
        });
        Preference showlogs = findPreference(App.PREF_KEY_SHOW_LOGS);
        showlogs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showLogs();
                return false;
            }
        });
        Preference report = findPreference(App.PREF_KEY_REPORT);
        report.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new ReportDialog(getActivity(), "").show();
                return false;
            }
        });
        Preference licenses = findPreference(App.PREF_KEY_LICENSES);
        licenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new LicensesDialog.Builder(getContext())
                        .setNotices(R.raw.licenses_notice)
                        .setIncludeOwnLicense(true)
                        .build()
                        .show();
                return false;
            }
        });
        Preference reset = findPreference(App.PREF_KEY_RESET_APP);
        reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, true);
                return true;
            }
        });
        Preference clearCache = findPreference(App.PREF_KEY_CLEAR_CACHE);
        clearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final AlertDialog.Builder ConfirmationDialog = new AlertDialog.Builder(getContext());
                ConfirmationDialog.setTitle(R.string.warning);
                ConfirmationDialog.setMessage(R.string.delete_confirmation);
                ConfirmationDialog.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!Common.deleteFolder(App.PathToCWM, false)
                                || !Common.deleteFolder(App.PathToTWRP, false)
                                || !Common.deleteFolder(App.PathToPhilz, false)
                                || !Common.deleteFolder(App.PathToCM, false)
                                || !Common.deleteFolder(App.PathToPhilz, false)
                                || !Common.deleteFolder(App.PathToStockRecovery, false)
                                || !Common.deleteFolder(App.PathToStockKernel, false)) {
                            Toast
                                    .makeText(getActivity(), R.string.delete_failed, Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast
                                    .makeText(getActivity(), R.string.files_deleted, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
                ConfirmationDialog.setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                ConfirmationDialog.show();
                return false;
            }
        });
    }

    private void showLogs() {
        final Context context = getActivity();
        final AlertDialog.Builder LogDialog = new AlertDialog.Builder(context);
        LogDialog.setTitle(R.string.logs);
        final File logs = new File(context.getFilesDir(), App.AppLogs);
        try {
            final String message = Common.fileContent(logs);
            LogDialog.setMessage(message);
            LogDialog.setNeutralButton(R.string.copy, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Common.copyToClipboard(context, message);
                }
            });
        } catch (Exception ignore) {
            Toast.makeText(context, R.string.no_logs, Toast.LENGTH_SHORT).show();
        }
        LogDialog.setNegativeButton(R.string.clear_logs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logs.delete();
            }
        });
        LogDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        LogDialog.show();
    }

}
