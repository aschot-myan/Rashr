package de.mkrtchyan.recoverytools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.cardsui.objects.Card;
import com.fima.cardsui.views.CardUI;
import com.fima.cardsui.views.MyCard;
import com.fima.cardsui.views.MyImageCard;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.Toolbox;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Downloader;
import de.mkrtchyan.utils.FileChooserDialog;

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
public class FlashFragment extends Fragment {

    private SwipeRefreshLayout mSwipeUpdater = null;
    private File RecoveryCollectionFile, KernelCollectionFile;

    private Device mDevice;
    private Shell mShell;
    private Toolbox mToolbox;
    private Context mContext;
    private RashrActivity mActivity;

    private OnFragmentInteractionListener mListener;

    public static FlashFragment newInstance(RashrActivity activity) {
        FlashFragment fragment = new FlashFragment();
        fragment.setActivity(activity);
        fragment.setDevice(activity.getDevice());
        fragment.setShell(activity.getShell());
        fragment.setToolbox(activity.getToolbox());
        return fragment;
    }

    public FlashFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecoveryCollectionFile = new File(mContext.getFilesDir(), "recovery_sums");
        KernelCollectionFile = new File(mContext.getFilesDir(), "kernel_sums");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rashr, container, false);
        /** Check if device uses unified builds */
        if (Common.getBooleanPref(mContext, RashrActivity.PREF_NAME, Constants.PREF_KEY_SHOW_UNIFIED)
                && ((mDevice.getName().startsWith("d2lte") || mDevice.getName().startsWith("hlte")
                || mDevice.getName().startsWith("jflte") || mDevice.getName().equals("moto_msm8960"))
                && (!mDevice.isStockRecoverySupported() || !mDevice.isCwmRecoverySupported()
                || !mDevice.isTwrpRecoverySupported() || !mDevice.isPhilzRecoverySupported()))) {
            showUnifiedBuildsDialog();
        }
        optimizeLayout(root);
        return root;
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

    /**
     * Buttons on FlashRecovery and FlashKernel Dialog
     */
    public void FlashSupportedRecovery(Card card) {
        final File path;
        final ArrayList<String> Versions;
        if (!mDevice.downloadUtils(mContext)) {
            /**
             * If there files be needed to flash download it and listing device specified
             * recovery file for example recovery-clockwork-touch-6.0.3.1-grouper.img
             * (read out from RECOVERY_SUMS)
             */
            String SYSTEM = card.getData().toString();
            ArrayAdapter<String> VersionsAdapter = new ArrayAdapter<>(mContext,
                    R.layout.custom_list_item);
            switch (SYSTEM) {
                case "stock":
                    Versions = mDevice.getStockRecoveryVersions();
                    path = Constants.PathToStockRecovery;
                    for (String i : Versions) {
                        try {
                            VersionsAdapter.add("Stock " + i.split("-")[3].replace(mDevice.getRecoveryExt(), ""));
                        } catch (ArrayIndexOutOfBoundsException e) {
                            VersionsAdapter.add(i);
                        }
                    }
                    break;
                case "clockwork":
                    Versions = mDevice.getCwmRecoveryVersions();
                    path = Constants.PathToCWM;
                    for (String i : Versions) {
                        try {
                            if (i.contains("-touch-")) {
                                String device = "(";
                                for (int splitNr = 4; splitNr < i.split("-").length; splitNr++) {
                                    if (!device.equals("(")) device += "-";
                                    device += i.split("-")[splitNr].replace(mDevice.getRecoveryExt(), "");
                                }
                                device += ")";
                                VersionsAdapter.add("ClockworkMod Touch " + i.split("-")[3] + " " + device);
                            } else {
                                String device = "(";
                                for (int splitNr = 3; splitNr < i.split("-").length; splitNr++) {
                                    if (!device.equals("(")) device += "-";
                                    device += i.split("-")[splitNr].replace(mDevice.getRecoveryExt(), "");
                                }
                                device += ")";
                                VersionsAdapter.add("ClockworkMod " + i.split("-")[2] + " " + device);
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            VersionsAdapter.add(i);
                        }
                    }
                    break;
                case "twrp":
                    Versions = mDevice.getTwrpRecoveryVersions();
                    path = Constants.PathToTWRP;
                    for (String i : Versions) {
                        try {
                            if (i.contains("openrecovery")) {
                                String device = "(";
                                for (int splitNr = 3; splitNr < i.split("-").length; splitNr++) {
                                    if (!device.equals("(")) device += "-";
                                    device += i.split("-")[splitNr].replace(mDevice.getRecoveryExt(), "");
                                }
                                device += ")";
                                VersionsAdapter.add("TWRP " + i.split("-")[2] + " " + device);
                            } else {
                                VersionsAdapter.add("TWRP " + i.split("-")[1].replace(mDevice.getRecoveryExt(), "") + ")");
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            VersionsAdapter.add(i);
                        }
                    }
                    break;
                case "philz":
                    Versions = mDevice.getPhilzRecoveryVersions();
                    path = Constants.PathToPhilz;
                    for (String i : Versions) {
                        try {
                            String device = "(";
                            for (int splitNr = 1; splitNr < i.split("-").length; splitNr++) {
                                if (!device.equals("(")) device += "-";
                                device += i.split("-")[splitNr].replace(mDevice.getRecoveryExt(), "");
                            }
                            device += ")";
                            VersionsAdapter.add("PhilZ Touch " + i.split("_")[2].split("-")[0] + " " + device);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            VersionsAdapter.add(i);
                        }
                    }
                    break;
                default:
                    return;
            }
            final Dialog RecoveriesDialog = new Dialog(mContext);
            RecoveriesDialog.setTitle(SYSTEM.toUpperCase());
            ListView VersionList = new ListView(mContext);
            RecoveriesDialog.setContentView(VersionList);

            VersionList.setAdapter(VersionsAdapter);
            RecoveriesDialog.show();
            VersionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    RecoveriesDialog.dismiss();

                    final String fileName = Versions.get(i);
                    final File recovery = new File(path, fileName);
                    if (!recovery.exists()) {
                        Downloader RecoveryDownloader = new Downloader(mContext, Constants.RECOVERY_URL,
                                recovery, new Downloader.OnDownloadListener() {
                            @Override
                            public void success(File file) {
                                flashRecovery(file);
                            }

                            @Override
                            public void failed(Exception e) {

                            }
                        });
                        RecoveryDownloader.setRetry(true);
                        RecoveryDownloader.setAskBeforeDownload(true);
                        RecoveryDownloader.setChecksumFile(RecoveryCollectionFile);
                        RecoveryDownloader.ask();
                    } else {
                        flashRecovery(recovery);
                    }
                }
            });
        }
    }

    /**
     * Flash Recovery from storage (already downloaded)
     */
    public void bFlashOtherRecovery(View view) {
        String AllowedEXT[] = {mDevice.getRecoveryExt()};
        FileChooserDialog chooser = new FileChooserDialog(view.getContext());
        chooser.setAllowedEXT(AllowedEXT);
        chooser.setBrowseUpAllowed(true);
        chooser.setOnFileChooseListener(new FileChooserDialog.OnFileChooseListener() {
                @Override
                public void OnFileChoose(File file) {
                    flashRecovery(file);
                }
            });
        chooser.setStartFolder(Constants.PathToSd);
        chooser.setWarn(true);
        chooser.show();
    }

    public void FlashSupportedKernel(Card card) {
        final File path;
        ArrayList<String> Versions;
        if (!mDevice.downloadUtils(mContext)) {
            /**
             * If there files be needed to flash download it and listing device specified recovery
             * file for example stock-boot-grouper-4.4.img (read out from kernel_sums)
             */
            String SYSTEM = card.getData().toString();
            if (SYSTEM.equals("stock")) {
                Versions = mDevice.getStockKernelVersions();
                path = Constants.PathToStockKernel;
            } else {
                return;
            }

            final Dialog KernelDialog = new Dialog(mContext);
            KernelDialog.setTitle(SYSTEM);
            ListView VersionList = new ListView(mContext);
            KernelDialog.setContentView(VersionList);
            VersionList.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, Versions));
            KernelDialog.show();
            VersionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    KernelDialog.dismiss();
                    final String fileName;
                    if ((fileName = ((TextView) view).getText().toString()) != null) {
                        final File kernel = new File(path, fileName);

                        if (!kernel.exists()) {
                            Downloader KernelDownloader = new Downloader(mContext, Constants.KERNEL_URL,
                                    kernel, new Downloader.OnDownloadListener() {
                                @Override
                                public void success(File file) {
                                    flashKernel(file);
                                }

                                @Override
                                public void failed(Exception e) {

                                }
                            });
                            KernelDownloader.setRetry(true);
                            KernelDownloader.setAskBeforeDownload(true);
                            KernelDownloader.setChecksumFile(KernelCollectionFile);
                            KernelDownloader.ask();
                        } else {
                            flashKernel(kernel);
                        }
                    }
                }
            });
        }
    }

    /**
     * Flash Kernel from storage (separate downloaded)
     */
    public void bFlashOtherKernel(View view) {
        FileChooserDialog chooser = new FileChooserDialog(view.getContext());
        String AllowedEXT[] = {mDevice.getKernelExt()};
        chooser.setOnFileChooseListener(new FileChooserDialog.OnFileChooseListener() {
            @Override
            public void OnFileChoose(File file) {
                flashKernel(file);
            }
        });
        chooser.setStartFolder(Constants.PathToSd);
        chooser.setAllowedEXT(AllowedEXT);
        chooser.setBrowseUpAllowed(true);
        chooser.setWarn(true);
        chooser.show();
    }

    public void showFlashHistory() {
        final ArrayList<File> HistoryFiles = new ArrayList<>();
        final ArrayList<String> HistoryFileNames = new ArrayList<>();
        final Dialog HistoryDialog = new Dialog(mContext);
        HistoryDialog.setTitle(R.string.history);
        ListView HistoryList = new ListView(mContext);
        File tmp;
        for (int i = 0; i < 5; i++) {
            tmp = new File(Common.getStringPref(mContext, RashrActivity.PREF_NAME,
                    Constants.PREF_KEY_HISTORY + String.valueOf(i)));
            if (tmp.exists() && !tmp.isDirectory()) {
                HistoryFiles.add(tmp);
                HistoryFileNames.add(tmp.getName());
            } else {
                Common.setStringPref(mContext, RashrActivity.PREF_NAME,
                        Constants.PREF_KEY_HISTORY + String.valueOf(i), "");
            }
        }
        HistoryList.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1,
                HistoryFileNames));
        HistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                if (HistoryFiles.get(arg2).exists()) {
                    mActivity.getIntent().setData(Uri.fromFile(HistoryFiles.get(arg2)));
                    if (mListener != null)
                        mListener.onFragmentInteraction(Constants.OPEN_FLASH_AS_FRAGMENT);
                    HistoryDialog.dismiss();
                }
            }
        });
        HistoryDialog.setContentView(HistoryList);
        if (HistoryFileNames.toArray().length > 0) {
            HistoryDialog.show();
        } else {
            Toast
                    .makeText(mActivity, R.string.no_history, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void flashRecovery(final File recovery) {
        if (recovery != null) {
            if (recovery.exists() && recovery.getName().endsWith(mDevice.getRecoveryExt())
                    && !recovery.isDirectory()) {
                if (!mDevice.isFOTAFlashed() && !mDevice.isRecoveryOverRecovery()) {
                    /** Flash not need to be handled specially */
                    executeFlash(recovery, FlashUtil.JOB_FLASH_RECOVERY);
                } else {
                    /** Flashing needs to be handled specially (not standard flash method)*/
                    if (mDevice.isFOTAFlashed()) {
                        /** Show warning if FOTAKernel will be flashed */
                        new AlertDialog.Builder(mContext)
                                .setTitle(R.string.warning)
                                .setMessage(R.string.fota)
                                .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        executeFlash(recovery, FlashUtil.JOB_FLASH_RECOVERY);
                                    }
                                })
                                .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    } else {
                        FragmentManager fm = mActivity.getSupportFragmentManager();
                        ScriptManagerFragment fragment = ScriptManagerFragment.newInstance(mActivity,
                                recovery);
                        fm
                                .beginTransaction()
                                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                                .replace(R.id.container, fragment)
								.commitAllowingStateLoss();
                    }
                }
            }
        }
    }

    private void flashKernel(final File kernel) {
        if (kernel != null) {
            if (kernel.exists() && kernel.getName().endsWith(mDevice.getKernelExt())
                    && !kernel.isDirectory()) {
                executeFlash(kernel, FlashUtil.JOB_FLASH_KERNEL);
            }
        }
    }

    private void executeFlash(final File image, final int FlashUtilJOB) {
        final FlashUtil flashUtil = new FlashUtil(mActivity, image, FlashUtilJOB);
        flashUtil.execute();
    }

    public void optimizeLayout(View root) throws NullPointerException {

        if (mDevice.isRecoverySupported() || mDevice.isKernelSupported()) {
            /** If device is supported start setting up layout */
            setupSwipeUpdater(root);

            CardUI RashrCards = (CardUI) root.findViewById(R.id.RashrCards);

            /** Avoid overlapping scroll on CardUI and SwipeRefreshLayout */
            RashrCards.getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    int topRowVerticalPosition = (view == null || view.getChildCount() == 0) ?
                            0 : view.getChildAt(0).getTop();
                    mSwipeUpdater.setEnabled((topRowVerticalPosition >= 0));
                }
            });

            if (mDevice.isRecoverySupported()) {
                addRecoveryCards(RashrCards);
            }

            if (mDevice.isKernelSupported()) {
                addKernelCards(RashrCards);
            }

            final MyImageCard HistoryCard = new MyImageCard(getString(R.string.history),
                    R.drawable.ic_history, getString(R.string.history_description));
            HistoryCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFlashHistory();
                }
            });

            RashrCards.addCard(HistoryCard, true);

            addRebooterCards(RashrCards);
        }
    }

    public boolean showUnifiedBuildsDialog() {

        final Dialog UnifiedBuildsDialog = new Dialog(mContext);
        UnifiedBuildsDialog.setTitle(R.string.make_choice);
        final ArrayList<String> DevName = new ArrayList<>();
        ArrayList<String> DevNamesCarriers = new ArrayList<>();

        UnifiedBuildsDialog.setContentView(R.layout.dialog_unified_build);
        ListView UnifiedList = (ListView) UnifiedBuildsDialog.findViewById(R.id.lvUnifiedList);
        ArrayAdapter<String> UnifiedAdapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_1, DevNamesCarriers);
        UnifiedList.setAdapter(UnifiedAdapter);

        if (mDevice.getManufacture().equals("samsung")) {
            String[] unifiedGalaxyS3 = {"d2lte", "d2att", "d2cri", "d2mtr",
                    "d2spr", "d2tmo", "d2usc", "d2vzw"};
            String[] unifiedGalaxyNote3 = {"hlte", "hltespr", "hltetmo", "hltevzw", "htlexx"};
            String[] unifiedGalaxyS4 = {"jflte", "jflteatt", "jfltecan", "jfltecri", "jfltecsp",
                    "jfltespr", "jfltetmo", "jflteusc", "jfltevzw", "jfltexx", "jgedlte"};
            if (Common.stringEndsWithArray(mDevice.getName(), unifiedGalaxyS3)) {
                DevName.addAll(Arrays.asList(unifiedGalaxyS3));
            } else if (Common.stringEndsWithArray(mDevice.getName(), unifiedGalaxyS3)) {
                DevName.addAll(Arrays.asList(unifiedGalaxyNote3));
            } else if (Common.stringEndsWithArray(mDevice.getName(), unifiedGalaxyS4)) {
                DevName.addAll(Arrays.asList(unifiedGalaxyS4));
            }
        }

        if (mDevice.getManufacture().equals("motorola")) {
            String[] unifiedMsm8960 = {"moto_msm8960"};
            if (mDevice.getBOARD().equals("msm8960")) {
                DevName.addAll(Arrays.asList(unifiedMsm8960));
            }
        }

        for (String i : DevName) {
            if (i.contains("att")) {
                DevNamesCarriers.add(i + " (AT&T Mobility)");
            } else if (i.contains("can")) {
                DevNamesCarriers.add(i + " (Canada)");
            } else if (i.contains("cri")) {
                DevNamesCarriers.add(i + " (Cricket Wireless)");
            } else if (i.contains("csp")) {
                DevNamesCarriers.add(i + " (C Spire Wireless)");
            } else if (i.contains("mtr")) {
                DevNamesCarriers.add(i + " (MetroPCS)");
            } else if (i.contains("spr")) {
                DevNamesCarriers.add(i + " (Sprint Corporation)");
            } else if (i.contains("tmo")) {
                DevNamesCarriers.add(i + " (T-Mobile US)");
            } else if (i.contains("usc")) {
                DevNamesCarriers.add(i + " (U.S. Cellular)");
            } else if (i.contains("vzw")) {
                DevNamesCarriers.add(i + " (Verizon Wireless)");
            } else if (i.contains("xx")) {
                DevNamesCarriers.add(i + " (International)");
            } else if (i.contains("ged")) {
                DevNamesCarriers.add(i + " (Google Play Edition)");
            } else {
                DevNamesCarriers.add(i + " (Unified)");
            }
        }
        UnifiedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                UnifiedBuildsDialog.dismiss();
                final ProgressDialog reloading = new ProgressDialog(mContext);
                reloading.setMessage(mContext.getString(R.string.reloading));
                reloading.setCancelable(false);
                reloading.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Common.setBooleanPref(mContext, RashrActivity.PREF_NAME,
                                Constants.PREF_KEY_SHOW_UNIFIED, false);
                        mDevice.setName(DevName.get(position));
                        mDevice.loadRecoveryList();
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                reloading.dismiss();
                                if (mListener != null)
                                    mListener.onFragmentInteraction(Constants.OPEN_RASHR_FRAGMENT);
                            }
                        });
                    }
                }).start();

            }
        });
        Button KeepCurrent = (Button) UnifiedBuildsDialog.findViewById(R.id.bKeepCurrent);
        KeepCurrent.setText(String.format(getString(R.string.keep_current_name), mDevice.getName()));
        KeepCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.setBooleanPref(mContext, RashrActivity.PREF_NAME, Constants.PREF_KEY_SHOW_UNIFIED, false);
                UnifiedBuildsDialog.dismiss();
            }
        });

        if (DevName.size() > 0) {
            UnifiedBuildsDialog.show();
            UnifiedBuildsDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Common.setBooleanPref(mContext, RashrActivity.PREF_NAME, Constants.PREF_KEY_SHOW_UNIFIED,
                            false);
                }
            });
            return true;
        } else {
            return false;
        }
    }
    public void setupSwipeUpdater(View root) {
        mSwipeUpdater = (SwipeRefreshLayout) root.findViewById(R.id.swipe_updater);
        mSwipeUpdater.setColorSchemeResources(R.color.custom_green,
                R.color.custom_yellow,
                R.color.custom_green,
                android.R.color.darker_gray);
        mSwipeUpdater.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                final int img_count = mDevice.getStockRecoveryVersions().size()
                        + mDevice.getCwmRecoveryVersions().size()
                        + mDevice.getTwrpRecoveryVersions().size()
                        + mDevice.getPhilzRecoveryVersions().size()
                        + mDevice.getStockKernelVersions().size();
                Downloader RecoveryUpdater = new Downloader(mContext, Constants.RECOVERY_SUMS_URL,
                        RecoveryCollectionFile);
                RecoveryUpdater.setOverrideFile(true);
                RecoveryUpdater.setHidden(true);

                Toast
                        .makeText(mActivity, R.string.refresh_list, Toast.LENGTH_SHORT)
                        .show();
                final Downloader KernelUpdater = new Downloader(mContext, Constants.KERNEL_SUMS_URL,
                        KernelCollectionFile);
                KernelUpdater.setOverrideFile(true);
                KernelUpdater.setHidden(true);
                RecoveryUpdater.setOnDownloadListener(new Downloader.OnDownloadListener() {
                    @Override
                    public void success(File file) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mDevice.loadRecoveryList();
                                KernelUpdater.execute();
                            }
                        }).start();
                    }

                    @Override
                    public void failed(Exception e) {
                        Toast
                                .makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });

                KernelUpdater.setOnDownloadListener(new Downloader.OnDownloadListener() {
                    @Override
                    public void success(File file) {
                        mDevice.loadKernelList();
                        mSwipeUpdater.setRefreshing(false);
                        final int new_img_count = (mDevice.getStockRecoveryVersions().size()
                                + mDevice.getCwmRecoveryVersions().size()
                                + mDevice.getTwrpRecoveryVersions().size()
                                + mDevice.getPhilzRecoveryVersions().size()
                                + mDevice.getStockKernelVersions().size()) - img_count;
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast
                                        .makeText(mActivity, String.format(getString(R.string.new_imgs_loaded),
                                                new_img_count), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
                    }

                    @Override
                    public void failed(Exception e) {
                        Toast
                                .makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
                RecoveryUpdater.execute();
            }
        });

    }

    public void addRecoveryCards(CardUI cardUI) {

        if (mDevice.isCwmRecoverySupported()) {
            final MyImageCard CWMCard = new MyImageCard(getString(R.string.sCWM), R.drawable.ic_cwm,
                    getString(R.string.cwm_description));
            CWMCard.setData("clockwork");
            CWMCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FlashSupportedRecovery(CWMCard);
                }
            });
            cardUI.addCard(CWMCard, true);
        }
        if (mDevice.isTwrpRecoverySupported()) {
            final MyImageCard TWRPCard = new MyImageCard(getString(R.string.sTWRP), R.drawable.ic_twrp,
                    getString(R.string.twrp_description));
            TWRPCard.setData("twrp");
            TWRPCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FlashSupportedRecovery(TWRPCard);
                }
            });
            cardUI.addCard(TWRPCard, true);
        }
        if (mDevice.isPhilzRecoverySupported()) {
            final MyCard PHILZCard = new MyCard(getString(R.string.sPhilz),
                    getString(R.string.philz_description));
            PHILZCard.setData("philz");
            PHILZCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FlashSupportedRecovery(PHILZCard);
                }
            });
            cardUI.addCard(PHILZCard, true);
        }
        if (mDevice.isStockRecoverySupported()) {
            final MyImageCard StockCard = new MyImageCard(getString(R.string.stock),
                    R.drawable.ic_update, getString(R.string.stock_recovery_description));
            StockCard.setData("stock");
            StockCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FlashSupportedRecovery(StockCard);
                }
            });
            cardUI.addCard(StockCard, true);
        }

        final MyCard OtherCard = new MyCard(getString(R.string.other_recovery),
                getString(R.string.other_storage_description));
        OtherCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bFlashOtherRecovery(v);
            }
        });
        cardUI.addCard(OtherCard, true);
    }
    public void addKernelCards(CardUI cardUI) {
        if (mDevice.isStockKernelSupported()) {
            final MyImageCard StockCard = new MyImageCard(getString(R.string.stock), R.drawable.ic_stock,
                    getString(R.string.stock_kernel_description));
            StockCard.setData("stock");
            StockCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FlashSupportedKernel(StockCard);
                }
            });
            cardUI.addCard(StockCard, true);
        }

        final MyCard OtherCard = new MyCard(getString(R.string.other_kernel),
                getString(R.string.other_storage_description));
        OtherCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bFlashOtherKernel(v);
            }
        });

        cardUI.addCard(OtherCard, true);
    }
    public void addRebooterCards(CardUI cardUI) {
        MyCard Reboot = new MyCard(getString(R.string.sReboot), getString(R.string.reboot_description));
        Reboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final AlertDialog.Builder ConfirmationDialog = new AlertDialog.Builder(mContext);
                ConfirmationDialog.setTitle(R.string.warning);
                ConfirmationDialog.setMessage(R.string.reboot_confirmation);
                ConfirmationDialog.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            mToolbox.reboot(Toolbox.REBOOT_REBOOT);
                        } catch (Exception e) {
                            e.printStackTrace();
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
        MyCard RebootRecovery = new MyCard(getString(R.string.sRebootRecovery),
                getString(R.string.reboot_recovery_description));
        RebootRecovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder ConfirmationDialog = new AlertDialog.Builder(mContext);
                ConfirmationDialog.setTitle(R.string.warning);
                ConfirmationDialog.setMessage(R.string.reboot_confirmation);
                ConfirmationDialog.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            mToolbox.reboot(Toolbox.REBOOT_RECOVERY);
                        } catch (Exception e) {
                            e.printStackTrace();
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
        MyCard RebootBootloader = new MyCard(getString(R.string.sRebootBootloader),
                getString(R.string.reboot_bootloader_description));
        RebootBootloader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder ConfirmationDialog = new AlertDialog.Builder(mContext);
                ConfirmationDialog.setTitle(R.string.warning);
                ConfirmationDialog.setMessage(R.string.reboot_confirmation);
                ConfirmationDialog.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            mToolbox.reboot(Toolbox.REBOOT_BOOTLOADER);
                        } catch (Exception e) {
                            e.printStackTrace();
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
        MyCard Shutdown = new MyCard(getString(R.string.sRebootShutdown),
                getString(R.string.shutdown_description));
        Shutdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder ConfirmationDialog = new AlertDialog.Builder(mContext);
                ConfirmationDialog.setTitle(R.string.warning);
                ConfirmationDialog.setMessage(R.string.shutdown_confirmation);
                ConfirmationDialog.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            mToolbox.reboot(Toolbox.REBOOT_SHUTDOWN);
                        } catch (Exception e) {
                            e.printStackTrace();
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
        cardUI.addCard(Reboot, true);
        cardUI.addCard(RebootRecovery, true);
        cardUI.addCard(RebootBootloader, true);
        cardUI.addCard(Shutdown, true);
    }

    public void setDevice(Device device) {
        mDevice = device;
    }
    public void setShell(Shell shell) {
        mShell = shell;
    }
    public void setToolbox(Toolbox toolbox) {
        mToolbox = toolbox;
    }
    public void setActivity(RashrActivity activity) {
        mActivity = activity;
        mContext = activity;
    }
}

