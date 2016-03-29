package de.mkrtchyan.recoverytools;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class InformationFragment extends Fragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InformationFragment() {
    }

    @SuppressWarnings("unused")
    public static InformationFragment newInstance() {
        return new InformationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_information_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            final Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            ArrayList<InformationItem> items = new ArrayList<>();
            items.add(new InformationItem("Device Name", RashrApp.DEVICE.getName()));
            items.add(new InformationItem("Manufacture", RashrApp.DEVICE.getManufacture()));
            String recoveryType = "";
            boolean recoveryWithPath = false;
            boolean recoveryWithBS = false;
            switch (RashrApp.DEVICE.getRecoveryType()) {
                case Device.PARTITION_TYPE_DD:
                    recoveryType = "DD";
                    recoveryWithPath = true;
                    recoveryWithBS = RashrApp.DEVICE.getRecoveryBlocksize() > 0;
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
            switch (RashrApp.DEVICE.getKernelType()) {
                case Device.PARTITION_TYPE_DD:
                    kernelType = "DD";
                    kernelWithPath = true;
                    kernelWithBS = RashrApp.DEVICE.getKernelBlocksize() > 0;
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
                items.add(new InformationItem("Recovery Path", RashrApp.DEVICE.getRecoveryPath()));
            if (recoveryWithBS)
                items.add(new InformationItem("Recovery Blocksize", String.valueOf(RashrApp.DEVICE.getRecoveryBlocksize())));

            items.add(new InformationItem("Kernel Partition Type", kernelType));
            if (kernelWithPath)
                items.add(new InformationItem("Kernel Path", RashrApp.DEVICE.getKernelPath()));
            if (kernelWithBS)
                items.add(new InformationItem("Kernel Blocksize", String.valueOf(RashrApp.DEVICE.getKernelBlocksize())));

            int total = 0;
            if (RashrApp.DEVICE.isCwmRecoverySupported()) {
                int tmp = RashrApp.DEVICE.getCwmRecoveryVersions().size();
                items.add(new InformationItem(getString(R.string.sCWM), String.valueOf(tmp)));
                total += tmp;
            }
            if (RashrApp.DEVICE.isTwrpRecoverySupported()) {
                int tmp = RashrApp.DEVICE.getTwrpRecoveryVersions().size();
                items.add(new InformationItem("TWRP Images", String.valueOf(tmp)));
                total += tmp;
            }
            if (RashrApp.DEVICE.isPhilzRecoverySupported()){
                int tmp = RashrApp.DEVICE.getPhilzRecoveryVersions().size();
                items.add(new InformationItem("Philz Images", String.valueOf(tmp)));
                total += tmp;
            }
            if (RashrApp.DEVICE.isXZDualRecoverySupported()) {
                int tmp = RashrApp.DEVICE.getXZDualRecoveryVersions().size();
                items.add(new InformationItem("XZDual Images", String.valueOf(tmp)));
                total += tmp;
            }
            if (RashrApp.DEVICE.isStockRecoverySupported()){
                int tmp = RashrApp.DEVICE.getStockRecoveryVersions().size();
                items.add(new InformationItem(getString(R.string.stock_recovery), String.valueOf(tmp)));
                total += tmp;
            }
            if (RashrApp.DEVICE.isStockKernelSupported()){
                int tmp = RashrApp.DEVICE.getStockKernelVersions().size();
                items.add(new InformationItem(getString(R.string.stock_kernel), String.valueOf(tmp)));
                total += tmp;
            }

            items.add(new InformationItem(getString(R.string.total_images), String.valueOf(total)));

            for (String err : RashrApp.ERRORS) {
                items.add(new InformationItem("Error:", err));
            }

            recyclerView.setAdapter(new InformationRecyclerViewAdapter(items));
        }
        return view;
    }
}
