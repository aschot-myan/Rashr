package de.mkrtchyan.recoverytools.fragment;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.View;

import java.util.ArrayList;

import de.mkrtchyan.recoverytools.App;
import de.mkrtchyan.recoverytools.Device;
import de.mkrtchyan.recoverytools.InformationItem;
import de.mkrtchyan.recoverytools.R;
import de.mkrtchyan.utils.Common;

public class InformationFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceClickListener {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InformationFragment() {
    }

    public static InformationFragment newInstance() {
        return new InformationFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getContext());

        ArrayList<InformationItem> items = new ArrayList<>();
        items.add(new InformationItem("Device Name", App.Device.getName()));
        items.add(new InformationItem("Manufacture", App.Device.getManufacture()));
        String recoveryType = "";
        boolean recoveryWithPath = false;
        boolean recoveryWithBS = false;
        switch (App.Device.getRecoveryType()) {
            case Device.PARTITION_TYPE_DD:
                recoveryType = "DD";
                recoveryWithPath = true;
                recoveryWithBS = App.Device.getRecoveryBlocksize() > 0;
                break;
            case Device.PARTITION_TYPE_MTD:
                recoveryType = "MTD";
                break;
            case Device.PARTITION_TYPE_RECOVERY:
                recoveryType = "Flash over current Recovery";
                break;
            case Device.PARTITION_TYPE_NOT_SUPPORTED:
                recoveryType = "Not supported";
                break;
        }

        String kernelType = "";
        boolean kernelWithPath = false;
        boolean kernelWithBS = false;
        switch (App.Device.getKernelType()) {
            case Device.PARTITION_TYPE_DD:
                kernelType = "DD";
                kernelWithPath = true;
                kernelWithBS = App.Device.getKernelBlocksize() > 0;
                break;
            case Device.PARTITION_TYPE_MTD:
                kernelType = "MTD";
                break;
            case Device.PARTITION_TYPE_RECOVERY:
                kernelType = "Flash over current Recovery";
                break;
            case Device.PARTITION_TYPE_NOT_SUPPORTED:
                kernelType = "Not supported";
        }

        items.add(new InformationItem(getString(R.string.recovery_type), recoveryType));
        if (recoveryWithPath)
            items.add(new InformationItem("Recovery Path", App.Device.getRecoveryPath()));
        if (recoveryWithBS)
            items.add(new InformationItem("Recovery Blocksize", String.valueOf(App.Device.getRecoveryBlocksize())));

        items.add(new InformationItem("Kernel Partition Type", kernelType));
        if (kernelWithPath)
            items.add(new InformationItem("Kernel Path", App.Device.getKernelPath()));
        if (kernelWithBS)
            items.add(new InformationItem("Kernel Blocksize", String.valueOf(App.Device.getKernelBlocksize())));

        int total = 0;
        if (App.Device.isCwmRecoverySupported()) {
            int tmp = App.Device.getCwmRecoveryVersions().size();
            items.add(new InformationItem(getString(R.string.sCWM), String.valueOf(tmp)));
            total += tmp;
        }
        if (App.Device.isTwrpRecoverySupported()) {
            int tmp = App.Device.getTwrpRecoveryVersions().size();
            items.add(new InformationItem(getString(R.string.sTWRP), String.valueOf(tmp)));
            total += tmp;
        }
        if (App.Device.isPhilzRecoverySupported()) {
            int tmp = App.Device.getPhilzRecoveryVersions().size();
            items.add(new InformationItem(getString(R.string.sPhilz), String.valueOf(tmp)));
            total += tmp;
        }
        if (App.Device.isXZDualRecoverySupported()) {
            int tmp = App.Device.getXZDualRecoveryVersions().size();
            items.add(new InformationItem(getString(R.string.xzdualrecovery), String.valueOf(tmp)));
            total += tmp;
        }
        if (App.Device.isStockRecoverySupported()) {
            int tmp = App.Device.getStockRecoveryVersions().size();
            items.add(new InformationItem(getString(R.string.stock_recovery), String.valueOf(tmp)));
            total += tmp;
        }
        if (App.Device.isStockKernelSupported()) {
            int tmp = App.Device.getStockKernelVersions().size();
            items.add(new InformationItem(getString(R.string.stock_kernel), String.valueOf(tmp)));
            total += tmp;
        }

        items.add(new InformationItem(getString(R.string.total_images), String.valueOf(total)));

        for (String err : App.ERRORS) {
            items.add(new InformationItem("Error:", err));
        }
        for (InformationItem item : items) {
            Preference pref = new Preference(getContext());
            pref.setTitle(item.id);
            pref.setSummary(item.content);
            pref.setOnPreferenceClickListener(this);
            screen.addPreference(pref);
        }
        setPreferenceScreen(screen);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Common.copyToClipboard(getContext(), preference.getSummary().toString());
        View v = getView();
        if (v == null) return false;
        Snackbar
                .make(getView(), R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT)
                .show();
        return false;
    }
}
