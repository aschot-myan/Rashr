package de.mkrtchyan.recoverytools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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

    private OnFragmentInteractionListener mListener;

    private Context mContext;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View root = inflater.inflate(R.layout.fragment_settings, container, false);
        CheckBox cbShowAds = (CheckBox) root.findViewById(R.id.cbShowAds);
        CheckBox cbLog = (CheckBox) root.findViewById(R.id.cbLog);
        Button bShowLogs = (Button) root.findViewById(R.id.bShowLogs);
        Button bReport = (Button) root.findViewById(R.id.bReport);


        bReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonPressed(view);
            }
        });
        cbShowAds.setChecked(Common.getBooleanPref(root.getContext(), RashrActivity.PREF_NAME, Constants.PREF_KEY_ADS));
        cbLog.setChecked(Common.getBooleanPref(root.getContext(), Shell.PREF_NAME, Shell.PREF_LOG));
        cbLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox cbLog = (CheckBox) view;
                Common.setBooleanPref(mContext, Shell.PREF_NAME, Shell.PREF_LOG,
                        !Common.getBooleanPref(mContext, Shell.PREF_NAME, Shell.PREF_LOG));
                cbLog.setChecked(Common.getBooleanPref(mContext, Shell.PREF_NAME, Shell.PREF_LOG));
                if (cbLog.isChecked()) {
                    root.findViewById(R.id.bShowLogs).setVisibility(View.VISIBLE);
                } else {
                    root.findViewById(R.id.bShowLogs).setVisibility(View.INVISIBLE);
                }
            }
        });
        bShowLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showLogs(view.getContext());
            }
        });
        cbShowAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.setBooleanPref(view.getContext(), RashrActivity.PREF_NAME, Constants.PREF_KEY_ADS,
                        !Common.getBooleanPref(view.getContext(), RashrActivity.PREF_NAME, Constants.PREF_KEY_ADS));
                ((CheckBox) view).setChecked(
                        Common.getBooleanPref(view.getContext(), RashrActivity.PREF_NAME, Constants.PREF_KEY_ADS));
            }
        });
        bShowLogs.setVisibility(cbLog.isChecked() ? View.VISIBLE : View.INVISIBLE);
        cbShowAds.setChecked(Common.getBooleanPref(root.getContext(), RashrActivity.PREF_NAME, Constants.PREF_KEY_ADS));

        Button ResetButton = (Button) root.findViewById(R.id.bReset);
        ResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                SharedPreferences.Editor editor = activity.getSharedPreferences(
                        RashrActivity.PREF_NAME, Context.MODE_PRIVATE).edit();
                editor.clear().apply();
                editor = activity.getSharedPreferences(FlashUtil.PREF_NAME,
                        Context.MODE_PRIVATE).edit();
                editor.clear().apply();
                editor = activity.getSharedPreferences(Shell.PREF_NAME,
                        Context.MODE_PRIVATE).edit();
                editor.clear().apply();
            }
        });

        Button ClearCache = (Button) root.findViewById(R.id.bClearCache);
        ClearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Activity activity = getActivity();
                final AlertDialog.Builder ConfirmationDialog = new AlertDialog.Builder(mContext);
                ConfirmationDialog.setTitle(R.string.warning);
                ConfirmationDialog.setMessage(R.string.delete_confirmation);
                ConfirmationDialog.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!Common.deleteFolder(Constants.PathToCWM, false)
                                || !Common.deleteFolder(Constants.PathToTWRP, false)
                                || !Common.deleteFolder(Constants.PathToPhilz, false)
                                || !Common.deleteFolder(Constants.PathToStockRecovery, false)
                                || !Common.deleteFolder(Constants.PathToStockKernel,false)) {
                            Toast
                                    .makeText(activity, R.string.delete_failed, Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast
                                    .makeText(activity, R.string.files_deleted, Toast.LENGTH_SHORT)
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
        mContext = root.getContext();
        return root;
    }

    public void onButtonPressed(View view) {
        if (mListener != null) {
            mListener.onFragmentInteraction(view.getId());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(int id);
    }

}
