package de.mkrtchyan.recoverytools;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.Toast;

import org.sufficientlysecure.rootcommands.Shell;

import de.mkrtchyan.utils.Common;

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
public class SettingsFragment extends Fragment {

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    public SettingsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_settings, container, false);
        final SwitchCompat swShowAds = (SwitchCompat) root.findViewById(R.id.cbShowAds);
        final SwitchCompat swLog = (SwitchCompat) root.findViewById(R.id.cbLog);
        final SwitchCompat swDarkUI = (SwitchCompat) root.findViewById(R.id.cbDarkUI);
        final SwitchCompat swCheckUpdates = (SwitchCompat) root.findViewById(R.id.cbCheckUpdates);
        final AppCompatButton bShowLogs = (AppCompatButton) root.findViewById(R.id.bShowLogs);
        final AppCompatButton bReport = (AppCompatButton) root.findViewById(R.id.bReport);
        final AppCompatButton bShowChangelog = (AppCompatButton) root.findViewById(R.id.bShowChangelog);
        final AppCompatButton bReset = (AppCompatButton) root.findViewById(R.id.bReset);
        final AppCompatButton bClearCache = (AppCompatButton) root.findViewById(R.id.bClearCache);

        swDarkUI.setChecked(Common.getBooleanPref(root.getContext(), Constants.PREF_NAME,
                Constants.PREF_KEY_DARK_UI));
        swShowAds.setChecked(Common.getBooleanPref(root.getContext(), Constants.PREF_NAME,
                Constants.PREF_KEY_ADS));
        swLog.setChecked(Common.getBooleanPref(root.getContext(), Shell.PREF_NAME, Shell.PREF_LOG));
        swCheckUpdates.setChecked(Common.getBooleanPref(root.getContext(), Constants.PREF_NAME,
                Constants.PREF_KEY_CHECK_UPDATES));
        swShowAds.setChecked(Common.getBooleanPref(root.getContext(), Constants.PREF_NAME,
                Constants.PREF_KEY_ADS));

        swDarkUI.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                Common.setBooleanPref(view.getContext(), Constants.PREF_NAME,
                        Constants.PREF_KEY_DARK_UI, isChecked);
                RashrActivity.isDark = isChecked;
            }
        });
        swLog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                Common.setBooleanPref(view.getContext(), Shell.PREF_NAME, Shell.PREF_LOG, isChecked);
                root.findViewById(R.id.bShowLogs)
                        .setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
            }
        });
        swCheckUpdates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                Common.setBooleanPref(view.getContext(), Constants.PREF_NAME,
                        Constants.PREF_KEY_CHECK_UPDATES, isChecked);
                swCheckUpdates.setChecked(isChecked);
            }
        });
        swShowAds.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                Common.setBooleanPref(view.getContext(), Constants.PREF_NAME, Constants.PREF_KEY_ADS,
                        isChecked);
                swShowAds.setChecked(isChecked);
            }
        });

        final RashrActivity activity = (RashrActivity) getActivity();

        bReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReportDialog dialog = new ReportDialog(activity, "");
                dialog.show();
            }
        });

        bShowLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showLogs(view.getContext());
            }
        });

        bShowLogs.setVisibility(swLog.isChecked() ? View.VISIBLE : View.INVISIBLE);

        bReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity)getActivity();
                SharedPreferences.Editor editor = activity.getSharedPreferences(Constants.PREF_NAME,
                        Context.MODE_PRIVATE).edit();
                editor.clear().commit();
                editor = activity.getSharedPreferences(FlashUtil.PREF_NAME,
                        Context.MODE_PRIVATE).edit();
                editor.clear().commit();
                editor = activity.getSharedPreferences(Shell.PREF_NAME,
                        Context.MODE_PRIVATE).edit();
                editor.clear().commit();
            }
        });

        bClearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder ConfirmationDialog = new AlertDialog.Builder(v.getContext());
                ConfirmationDialog.setTitle(R.string.warning);
                ConfirmationDialog.setMessage(R.string.delete_confirmation);
                ConfirmationDialog.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!Common.deleteFolder(Constants.PathToCWM, false)
                                || !Common.deleteFolder(Constants.PathToTWRP, false)
                                || !Common.deleteFolder(Constants.PathToPhilz, false)
                                || !Common.deleteFolder(Constants.PathToStockRecovery, false)
                                || !Common.deleteFolder(Constants.PathToStockKernel, false)) {
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
            }
        });
        bShowChangelog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsFragment.showChangelog(v.getContext());
            }
        });
        return root;
    }

    public static void showChangelog(Context AppContext) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(AppContext);
        dialog.setTitle(R.string.changelog);
        WebView changes = new WebView(AppContext);
        changes.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        changes.setWebViewClient(new WebViewClient());
        changes.loadUrl(Constants.CHANGELOG_URL);
        changes.clearCache(true);
        dialog.setView(changes);
        dialog.show();
    }

}
