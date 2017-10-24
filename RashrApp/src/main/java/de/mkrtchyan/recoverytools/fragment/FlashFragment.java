package de.mkrtchyan.recoverytools.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.sufficientlysecure.rootcommands.Toolbox;
import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.mkrtchyan.recoverytools.App;
import de.mkrtchyan.recoverytools.BuildConfig;
import de.mkrtchyan.recoverytools.Device;
import de.mkrtchyan.recoverytools.FlashUtil;
import de.mkrtchyan.recoverytools.R;
import de.mkrtchyan.recoverytools.RashrActivity;
import de.mkrtchyan.recoverytools.view.Card;
import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.DownloadDialog;
import de.mkrtchyan.utils.Downloader;
import de.mkrtchyan.utils.FileChooserDialog;

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
public class FlashFragment extends Fragment {

    @BindView(R.id.philz_card)
    Card mPHILZCard;
    @BindView(R.id.xzdual_card)
    Card mXZDualCard;
    @BindView(R.id.twrp_card)
    Card mTWRPCard;
    @BindView(R.id.cwm_card)
    Card mCWMCard;
    @BindView(R.id.stock_recovery_card)
    Card mStockRecoveryCard;
    @BindView(R.id.other_recovery_card)
    Card mOtherRecoveryCard;
    @BindView(R.id.stock_kernel_card)
    Card mStockKernelCard;
    @BindView(R.id.other_kernel_card)
    Card mOtherKernelCard;
    @BindView(R.id.reboot_card)
    Card mRebootCard;
    @BindView(R.id.reboot_bootloader_card)
    Card mRebootBootloaderCard;
    @BindView(R.id.reboot_recovery_card)
    Card mRebootRecoveryCard;
    @BindView(R.id.shutdown_card)
    Card mShutdownCard;
    @BindView(R.id.history_card)
    Card mHistoryCard;
    @BindView(R.id.RashrCards)
    LinearLayoutCompat mRashrCards;
    private Context mContext;
    private RashrActivity mActivity;
    private boolean isRecoveryListUpToDate = true;
    private boolean isKernelListUpToDate = true;
    private SwipeRefreshLayout mSwipeUpdater; //Root element

    public FlashFragment() {
    }

    public static FlashFragment newInstance(RashrActivity activity) {
        FlashFragment fragment = new FlashFragment();
        fragment.setActivity(activity);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mSwipeUpdater = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_rashr, container, false);
        ButterKnife.bind(this, mSwipeUpdater);
        /* Check if device uses unified builds */
        if (App.Preferences.getBoolean(App.PREF_KEY_SHOW_UNIFIED, true)
                && App.Device.isUnified()) {
            showUnifiedBuildsDialog();
        }
        mSwipeUpdater.setColorSchemeResources(
                R.color.custom_blue,
                R.color.golden,
                R.color.custom_blue,
                android.R.color.darker_gray);
        mSwipeUpdater.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                catchUpdates();
            }
        });

        setupCards();

        if (App.Preferences.getBoolean(App.PREF_KEY_CHECK_UPDATES, true)
                && RashrActivity.FirstSession) {
            catchUpdates();
            RashrActivity.FirstSession = false;
        }
        return mSwipeUpdater;
    }

    /**
     * Flash Recovery from storage (already downloaded)
     */
    public void bFlashOtherRecovery(View view) {
        String AllowedEXT[] = {App.Device.getRecoveryExt()};
        FileChooserDialog chooser = new FileChooserDialog(view.getContext());
        chooser.setAllowedEXT(AllowedEXT);
        chooser.setBrowseUpAllowed(true);
        chooser.setOnFileChooseListener(new FileChooserDialog.OnFileChooseListener() {
            @Override
            public void OnFileChoose(File file) {
                flashRecovery(file);
            }
        });
        chooser.setStartFolder(App.PathToSd);
        chooser.setWarning(getString(R.string.choose_message));
        chooser.show();
    }

    /**
     * Flash Kernels provided by Rashr like stock kernels for Nexus Devices
     *
     * @param card Card.getData() contains the Kernel type should be flashed for example: stock,
     *             bricked...
     */
    private void FlashSupportedKernel(Card card) {
        final File path;
        final ArrayList<String> Versions;
        ArrayAdapter<String> VersionsAdapter = new ArrayAdapter<>(mContext, R.layout.custom_list_item);
        /*
         * If there files be needed to flash download it and listing device specified recovery
         * file for example stock-boot-grouper-4.4.img (read out from kernel_sums)
         */
        String SYSTEM = card.getData();
        if (SYSTEM.equals(Device.KER_SYS_STOCK)) {
            Versions = App.Device.getStockKernelVersions();
            path = App.PathToStockKernel;
            for (String i : Versions) {
                try {
                    String version = i.split("-")[3].replace(App.Device.getRecoveryExt(), "").replace("_", " ").toUpperCase();
                    String deviceName = i.split("-")[2];
                    /* Readable name for user */
                    VersionsAdapter.add("Stock Kernel " + version + " (" + deviceName + ")");
                } catch (ArrayIndexOutOfBoundsException e) {
                    /* Add the normal filename if something went wrong */
                    VersionsAdapter.add(i);
                }
            }
        } else {
            //Easter Egg?
            Toast.makeText(getContext(), "Only stock kernel is supported at the moment, why are you here?\n" +
                    "Something went wrong better return :)", Toast.LENGTH_SHORT).show();
            /* Only stock kernel is supported at the moment, why are you here?
             * Something went wrong better return :)
             */
            return;
        }

        final AlertDialog.Builder KernelDialog = new AlertDialog.Builder(mContext);
        KernelDialog.setTitle(SYSTEM);
        KernelDialog.setAdapter(VersionsAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final File kernel = new File(path, Versions.get(which));

                if (!kernel.exists()) {
                    try {
                        URL url = new URL(App.KERNEL_URL + "/" + kernel.getName());
                        Downloader downloader = new Downloader(url, kernel);
                        final DownloadDialog KernelDownloader = new DownloadDialog(mContext, downloader);
                        KernelDownloader.setOnDownloadListener(new DownloadDialog.OnDownloadListener() {
                            @Override
                            public void onSuccess(File file) {
                                flashKernel(file);
                            }

                            @Override
                            public void onFail(Exception e) {
                                KernelDownloader.retry();
                            }
                        });
                        KernelDownloader.setAskBeforeDownload(true);
                        downloader.setChecksumFile(App.KernelCollectionFile);
                        KernelDownloader.ask();
                    } catch (MalformedURLException ignored) {}
                } else {
                    flashKernel(kernel);
                }
            }
        });
        KernelDialog.show();
        //}
    }

    /**
     * Flash Kernel from storage (separate downloaded)
     */
    public void bFlashOtherKernel(View view) {
        FileChooserDialog chooser = new FileChooserDialog(view.getContext());
        String AllowedEXT[] = {App.Device.getKernelExt()};
        chooser.setOnFileChooseListener(new FileChooserDialog.OnFileChooseListener() {
            @Override
            public void OnFileChoose(File file) {
                flashKernel(file);
            }
        });
        chooser.setStartFolder(App.PathToSd);
        chooser.setAllowedEXT(AllowedEXT);
        chooser.setBrowseUpAllowed(true);
        chooser.setWarning(getString(R.string.choose_message));
        chooser.show();
    }

    /**
     * Lists the last 5 flashed images and shows a dialog for a re-flash
     */
    public void showFlashHistory() {
        final ArrayList<File> HistoryFiles = getHistoryFiles();
        final ArrayList<String> HistoryFileNames = new ArrayList<>();
        final AlertDialog.Builder HistoryDialog = new AlertDialog.Builder(mContext);
        HistoryDialog.setTitle(R.string.history);

        for (File i : HistoryFiles) {
            HistoryFileNames.add(i.getName());
        }

        HistoryDialog.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1,
                HistoryFileNames), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (HistoryFiles.get(which).exists()) {
                    mActivity.switchTo(FlashAsFragment.newInstance(mActivity,
                            HistoryFiles.get(which)), true);
                }
            }
        });
        if (HistoryFileNames.toArray().length > 0) {
            HistoryDialog.show();
        } else {
            Toast
                    .makeText(getContext(), R.string.no_history, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * Flash recovery using FlashUtil
     *
     * @param recovery recovery image (appropriated for this device)
     */
    private void flashRecovery(@NonNull final File recovery) {
        /* recovery needs to be a file and ends with the allowed extension */
        if (recovery.exists() && recovery.getName().endsWith(App.Device.getRecoveryExt())
                && recovery.isFile()) {
            if (!App.Device.isFOTAFlashed() && !App.Device.isRecoveryOverRecovery()) {
                /* Flash not need to be handled specially */
                final FlashUtil flashUtil = new FlashUtil(mActivity, recovery, FlashUtil.JOB_FLASH_RECOVERY);
                flashUtil.setOnFlashListener(new FlashUtil.OnFlashListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFail(Exception e) {
                        App.ERRORS.add(e.toString());
                        AlertDialog.Builder d = new AlertDialog.Builder(mContext);
                        d.setTitle(R.string.flash_error);
                        d.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        if (e instanceof FlashUtil.ImageNotValidException) {
                            d.setMessage(getString(R.string.image_not_valid_message));
                            d.setNeutralButton(R.string.settings, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mActivity.switchTo(new SettingsFragment());
                                }
                            });
                        } else if (e instanceof FlashUtil.ImageToBigException) {
                            //Size in MB
                            int sizeOfImage = ((FlashUtil.ImageToBigException) e).getCustomSize() / (1024 * 1024);
                            int sizeOfPart = ((FlashUtil.ImageToBigException) e).getPartitionSize() / (1024 * 1024);
                            d.setMessage(String.format(getString(R.string.image_to_big_message), sizeOfImage, sizeOfPart));
                            d.setNeutralButton(R.string.settings, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mActivity.switchTo(new SettingsFragment());
                                }
                            });
                        } else {
                            d.setMessage(e.getMessage());
                        }
                        d.show();
                    }
                });
                flashUtil.execute();
            } else {
                    /* Flashing needs to be handled specially (not standard flash method)*/
                if (App.Device.isFOTAFlashed()) {
                    /* Show warning if FOTAKernel will be flashed */
                    new AlertDialog.Builder(mContext)
                            .setTitle(R.string.warning)
                            .setMessage(R.string.fota)
                            .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final FlashUtil flashUtil = new FlashUtil(getContext(), recovery, FlashUtil.JOB_FLASH_RECOVERY);
                                    flashUtil.execute();
                                }
                            })
                            .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                } else if (App.Device.isRecoveryOverRecovery()) {
                    mActivity.switchTo(ScriptManagerFragment.newInstance(recovery));
                }
            }
        }
    }

    /**
     * Flashing kernel using FlashUtil
     *
     * @param kernel kernel image (appropriated for this device)
     */
    private void flashKernel(final File kernel) {
        if (kernel != null) {
            if (kernel.exists() && kernel.getName().endsWith(App.Device.getKernelExt())
                    && !kernel.isDirectory()) {
                final FlashUtil flashUtil = new FlashUtil(getContext(), kernel, FlashUtil.JOB_FLASH_KERNEL);
                flashUtil.execute();
            }
        }
    }

    /**
     * Check if Device uses a Unified base like some Galaxy S4: htle, htltespr htltexx uses the same
     * sources so they can use the unified kernels and recoveries. Let the User choice which one is
     * the correct for him. PLEASE BE CAREFUL!
     */
    public void showUnifiedBuildsDialog() {

        final AppCompatDialog UnifiedBuildsDialog = new AppCompatDialog(mContext);
        UnifiedBuildsDialog.setTitle(R.string.make_choice);
        final ArrayList<String> DevName = new ArrayList<>();
        ArrayList<String> DevNamesCarriers = new ArrayList<>();

        UnifiedBuildsDialog.setContentView(R.layout.dialog_unified_build);
        ListView UnifiedList = UnifiedBuildsDialog.findViewById(R.id.lvUnifiedList);
        AppCompatButton KeepCurrent = UnifiedBuildsDialog.findViewById(R.id.bKeepCurrent);
        ArrayAdapter<String> UnifiedAdapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_1, DevNamesCarriers);
        if (UnifiedList != null) {
            UnifiedList.setAdapter(UnifiedAdapter);

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
                            App.Preferences.edit().putBoolean(App.PREF_KEY_SHOW_UNIFIED, false).apply();
                            App.Device.setName(DevName.get(position));
                            App.Device.loadRecoveryList();
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    reloading.dismiss();
                                    mActivity.switchTo(FlashFragment.newInstance(mActivity));
                                }
                            });
                        }
                    }).start();

                }
            });
        }

        //Some Samsung devices are unified
        if (App.Device.getManufacture().equals("samsung")) {
            String[] unifiedGalaxyS3 = {"d2lte", "d2att", "d2cri", "d2mtr",
                    "d2spr", "d2tmo", "d2usc", "d2vzw"};
            String[] unifiedGalaxyNote3 = {"hlte", "hltespr", "hltetmo", "hltevzw", "htlexx"};
            String[] unifiedGalaxyS4 = {"jflte", "jflteatt", "jfltecan", "jfltecri", "jfltecsp",
                    "jfltespr", "jfltetmo", "jflteusc", "jfltevzw", "jfltexx", "jgedlte"};
            String[] unifiedGalaxyNote4 = {"trlte", "trltecan", "trltedt", "trltexx", "trltespr",
                    "trltetmo", "trltevzw", "trlteusc"};
            String[] unifiedGalaxyS7 = {"herolte", "heroltexx"};
            if (Common.stringEndsWithArray(App.Device.getName(), unifiedGalaxyS3)) {
                DevName.addAll(Arrays.asList(unifiedGalaxyS3));
            } else if (Common.stringEndsWithArray(App.Device.getName(), unifiedGalaxyS3)) {
                DevName.addAll(Arrays.asList(unifiedGalaxyNote3));
            } else if (Common.stringEndsWithArray(App.Device.getName(), unifiedGalaxyS4)) {
                DevName.addAll(Arrays.asList(unifiedGalaxyS4));
            } else if (Common.stringEndsWithArray(App.Device.getName(), unifiedGalaxyNote4)) {
                DevName.addAll(Arrays.asList(unifiedGalaxyNote4));
            } else if (Common.stringEndsWithArray(App.Device.getName(), unifiedGalaxyS7)) {
                DevName.addAll(Arrays.asList(unifiedGalaxyS7));
            }
        }

        //Some Motorola devices are unified
        if (App.Device.getManufacture().equals("motorola")) {
            String[] unifiedMsm8960 = {"moto_msm8960"};
            if (App.Device.getBOARD().equals("msm8960")) {
                DevName.addAll(Arrays.asList(unifiedMsm8960));
            }
        }

        for (String device : DevName) {
            if (device.contains("att")) {
                DevNamesCarriers.add(device + " (AT&T Mobility)");
            } else if (device.contains("can")) {
                DevNamesCarriers.add(device + " (Canada)");
            } else if (device.contains("cri")) {
                DevNamesCarriers.add(device + " (Cricket Wireless)");
            } else if (device.contains("csp")) {
                DevNamesCarriers.add(device + " (C Spire Wireless)");
            } else if (device.contains("mtr")) {
                DevNamesCarriers.add(device + " (MetroPCS)");
            } else if (device.contains("spr")) {
                DevNamesCarriers.add(device + " (Sprint Corporation)");
            } else if (device.contains("tmo")) {
                DevNamesCarriers.add(device + " (T-Mobile US)");
            } else if (device.contains("usc")) {
                DevNamesCarriers.add(device + " (U.S. Cellular)");
            } else if (device.contains("vzw")) {
                DevNamesCarriers.add(device + " (Verizon Wireless)");
            } else if (device.contains("xx")) {
                DevNamesCarriers.add(device + " (International)");
            } else if (device.contains("ged")) {
                DevNamesCarriers.add(device + " (Google Play Edition)");
            } else if (device.contains("dt")) {
                DevNamesCarriers.add(device + " (India)");
            } else {
                DevNamesCarriers.add(device + " (Unified)");
            }
        }
        if (DevName.size() > 0) {
            if (KeepCurrent != null) {
                KeepCurrent.setText(
                        String.format(getString(R.string.keep_current_name), App.Device.getName()));
            }
            UnifiedBuildsDialog.show();
            UnifiedBuildsDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    App.Preferences.edit().putBoolean(App.PREF_KEY_SHOW_UNIFIED, false).apply();
                }
            });
        }
    }

    /**
     * setupCards checks which recovery systems are supported by your device for example:
     * Galaxy S6 (SM-G920F) supports TWRP but isn't supported by CWM so addRecoveryCards will add
     * TWRP Card and Recovery From Storage card.
     */
    public void setupCards() {
        if (!App.Device.isXZDualRecoverySupported() && !BuildConfig.DEBUG) {
            mRashrCards.removeView(mXZDualCard);
        } else {
            mXZDualCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString(RecoverySystemFragment.PARAM_TITLE, mXZDualCard.getData());
                    bundle.putString(RecoverySystemFragment.PARAM_DEV, getString(R.string.dev_name_xzdual));
                    bundle.putString(RecoverySystemFragment.PARAM_DESC, getString(R.string.xzdual_describtion));
                    bundle.putStringArrayList(RecoverySystemFragment.PARAM_VERSIONS, App.Device.getXZDualRecoveryVersions());
                    bundle.putInt(RecoverySystemFragment.PARAM_LOGO, R.drawable.ic_xzdual);
                    bundle.putString(RecoverySystemFragment.PARAM_IMG_PATH, App.PathToXZDual.toString());
                    mActivity.switchTo(RecoverySystemFragment.newInstance(bundle), true);
                }
            });
        }
        if (!App.Device.isCwmRecoverySupported() && !BuildConfig.DEBUG) {
            mRashrCards.removeView(mCWMCard);
        } else {
            mCWMCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString(RecoverySystemFragment.PARAM_TITLE, mCWMCard.getData());
                    bundle.putString(RecoverySystemFragment.PARAM_DEV, getString(R.string.dev_name_cwm));
                    bundle.putString(RecoverySystemFragment.PARAM_DESC, getString(R.string.cwm_description));
                    bundle.putStringArrayList(RecoverySystemFragment.PARAM_VERSIONS, App.Device.getCwmRecoveryVersions());
                    bundle.putString(RecoverySystemFragment.PARAM_SCREENSHOT_URL, App.CWM_SCREENSHOT_URL);
                    bundle.putInt(RecoverySystemFragment.PARAM_LOGO, R.drawable.ic_cwm);
                    bundle.putString(RecoverySystemFragment.PARAM_IMG_PATH, App.PathToCWM.toString());
                    mActivity.switchTo(RecoverySystemFragment.newInstance(bundle), true);
                }
            });
        }
        if (!App.Device.isTwrpRecoverySupported() && !BuildConfig.DEBUG) {
            mRashrCards.removeView(mTWRPCard);
        } else {
            mTWRPCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString(RecoverySystemFragment.PARAM_TITLE, mTWRPCard.getData());
                    bundle.putString(RecoverySystemFragment.PARAM_DEV, getString(R.string.dev_name_twrp));
                    bundle.putString(RecoverySystemFragment.PARAM_DESC, getString(R.string.twrp_description));
                    bundle.putStringArrayList(RecoverySystemFragment.PARAM_VERSIONS, App.Device.getTwrpRecoveryVersions());
                    bundle.putString(RecoverySystemFragment.PARAM_SCREENSHOT_URL, App.TWRP_SCREENSHOT_URL);
                    bundle.putInt(RecoverySystemFragment.PARAM_LOGO, R.drawable.ic_twrp);
                    bundle.putString(RecoverySystemFragment.PARAM_IMG_PATH, App.PathToTWRP.toString());
                    mActivity.switchTo(RecoverySystemFragment.newInstance(bundle), true);
                }
            });
        }
        if (!App.Device.isPhilzRecoverySupported() && !BuildConfig.DEBUG) {
            mRashrCards.removeView(mPHILZCard);
        } else {
            mPHILZCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString(RecoverySystemFragment.PARAM_TITLE, mPHILZCard.getData());
                    bundle.putString(RecoverySystemFragment.PARAM_DEV, getString(R.string.dev_name_philz));
                    bundle.putString(RecoverySystemFragment.PARAM_DESC, getString(R.string.philz_description));
                    bundle.putStringArrayList(RecoverySystemFragment.PARAM_VERSIONS, App.Device.getPhilzRecoveryVersions());
                    bundle.putString(RecoverySystemFragment.PARAM_IMG_PATH, App.PathToPhilz.toString());
                    mActivity.switchTo(RecoverySystemFragment.newInstance(bundle), true);
                }
            });
        }
        if (!App.Device.isStockRecoverySupported() && !BuildConfig.DEBUG) {
            mRashrCards.removeView(mStockRecoveryCard);
        } else {
            mStockRecoveryCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString(RecoverySystemFragment.PARAM_TITLE, mStockRecoveryCard.getData());
                    bundle.putString(RecoverySystemFragment.PARAM_DEV, App.Device.getManufacture());
                    bundle.putString(RecoverySystemFragment.PARAM_DESC, getString(R.string.stock_recovery_description));
                    bundle.putStringArrayList(RecoverySystemFragment.PARAM_VERSIONS, App.Device.getStockRecoveryVersions());
                    bundle.putInt(RecoverySystemFragment.PARAM_LOGO, R.drawable.ic_update);
                    bundle.putString(RecoverySystemFragment.PARAM_IMG_PATH, App.PathToStockRecovery.toString());
                    mActivity.switchTo(RecoverySystemFragment.newInstance(bundle), true);
                }
            });
        }

        if (!App.Device.isRecoverySupported() && BuildConfig.DEBUG) {
            mRashrCards.removeView(mOtherKernelCard);
        } else {
            mOtherRecoveryCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bFlashOtherRecovery(v);
                }
            });
        }

        if (!App.Device.isStockKernelSupported() && !BuildConfig.DEBUG) {
            mRashrCards.removeView(mStockKernelCard);
        } else {
            mStockKernelCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FlashSupportedKernel(mStockKernelCard);
                }
            });
        }

        if (!App.Device.isKernelSupported() && BuildConfig.DEBUG) {
            mRashrCards.removeView(mOtherKernelCard);
        } else {
            mOtherKernelCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bFlashOtherKernel(v);
                }
            });
        }

        if (getHistoryFiles().size() <= 0) {
            mRashrCards.removeView(mHistoryCard);
        } else {
            mHistoryCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFlashHistory();
                }
            });
        }

        mRebootCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    App.Toolbox.reboot(Toolbox.REBOOT_REBOOT);
                } catch (FailedExecuteCommand ignore) {
                }
            }
        });
        mRebootRecoveryCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    App.Toolbox.reboot(Toolbox.REBOOT_RECOVERY);
                } catch (FailedExecuteCommand ignore) {
                }
            }
        });
        mRebootBootloaderCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    App.Toolbox.reboot(Toolbox.REBOOT_BOOTLOADER);
                } catch (FailedExecuteCommand ignore) {
                }
            }
        });
        mShutdownCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    App.Toolbox.reboot(Toolbox.REBOOT_SHUTDOWN);
                } catch (FailedExecuteCommand ignore) {
                }
            }
        });
    }

    public void setActivity(RashrActivity activity) {
        mActivity = activity;
        mContext = activity;
    }

    /**
     * Checking if there are new Kernel and Recovery images to download.
     * Download new list of recoveries and kernels if user want and reload interface.
     * The lists are placed in  dslnexus.de/Android/recovery_links ( App.RECOVERY_SUMS_URL )
     * dslnexus.de/Android/kernel_sums ( App.KERNEL_SUMS_URL )
     */
    public void catchUpdates() {
        mSwipeUpdater.setRefreshing(true);
        final Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /* Check changes on server */
                    final URL recoveryUrl = new URL(App.RECOVERY_SUMS_URL);
                    URLConnection recoveryCon = recoveryUrl.openConnection();
                    long recoveryListSize = recoveryCon.getContentLength();         //returns size of file on server
                    long recoveryListLocalSize = App.RecoveryCollectionFile.length();   //returns size of local file
                    if (recoveryListSize > 0) {
                        //Asuming if sizes are equal nothing has changed
                        isRecoveryListUpToDate = recoveryListLocalSize == recoveryListSize;
                    }
                    final URL kernelUrl = new URL(App.KERNEL_SUMS_URL);
                    URLConnection kernelCon = kernelUrl.openConnection();
                    long kernelListSize = kernelCon.getContentLength();
                    long kernelListLocalSize = App.KernelCollectionFile.length();
                    if (kernelListSize > 0) {
                        //Asuming if sizes are equal nothing has changed
                        isKernelListUpToDate = kernelListLocalSize == kernelListSize;
                    }
                } catch (IOException e) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar
                                    .make(mSwipeUpdater, R.string.check_connection, Snackbar.LENGTH_SHORT)
                                    .setAction(R.string.retry, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            catchUpdates();
                                        }
                                    })
                                    .show();
                        }
                    });
                    if (e.toString() != null) {
                        App.ERRORS.add(App.TAG + " Error while checking updates: " + e);
                    } else {
                        App.ERRORS.add(App.TAG + " Error while checking updates");
                    }
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /* Something on server changed */
                        if (!isRecoveryListUpToDate || !isKernelListUpToDate) {
                            /* Counting current images before update */
                            final int img_count = App.Device.getStockRecoveryVersions().size()
                                    + App.Device.getCwmRecoveryVersions().size()
                                    + App.Device.getTwrpRecoveryVersions().size()
                                    + App.Device.getPhilzRecoveryVersions().size()
                                    + App.Device.getStockKernelVersions().size();
                            final URL recoveryURL;
                            final URL kernelURL;
                            try {
                                recoveryURL = new URL(App.RECOVERY_SUMS_URL);
                                kernelURL = new URL(App.KERNEL_SUMS_URL);
                            } catch (MalformedURLException e) {
                                App.ERRORS.add(e.toString());
                                return;
                            }
                            /* Download the new lists */
                            final Downloader rDownloader = new Downloader(recoveryURL, App.RecoveryCollectionFile);
                            rDownloader.setOverrideFile(true);
                            rDownloader.setOnDownloadListener(new Downloader.OnDownloadListener() {
                                @Override
                                public void onSuccess(File file) {
                                    App.Device.loadRecoveryList();
                                    isRecoveryListUpToDate = true;
                                    final Downloader kDownloader = new Downloader(kernelURL, App.KernelCollectionFile);
                                    kDownloader.setOverrideFile(true);
                                    kDownloader.setOnDownloadListener(new Downloader.OnDownloadListener() {
                                        @Override
                                        public void onSuccess(File file) {
                                            App.Device.loadKernelList();
                                            isKernelListUpToDate = true;
                                            /* Counting added images (after update) */
                                            final int new_img_count = (App.Device.getStockRecoveryVersions().size()
                                                    + App.Device.getCwmRecoveryVersions().size()
                                                    + App.Device.getTwrpRecoveryVersions().size()
                                                    + App.Device.getPhilzRecoveryVersions().size()
                                                    + App.Device.getStockKernelVersions().size())
                                                    - img_count;
                                            mActivity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (isAdded()) {
                                                        Toast
                                                                .makeText(getContext(), String.format(getString(R.string.new_imgs_loaded),
                                                                        String.valueOf(new_img_count)), Toast.LENGTH_SHORT)
                                                                .show();
                                                    }
                                                    mSwipeUpdater.setRefreshing(false);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFail(final Exception e) {
                                            String msg;
                                            if (e != null) {
                                                msg = e.getMessage();
                                            } else {
                                                msg = "Error occurred while loading new Kernel Lists";
                                            }
                                            Snackbar
                                                    .make(mSwipeUpdater, msg, Snackbar.LENGTH_SHORT)
                                                    .show();
                                            mSwipeUpdater.setRefreshing(false);
                                        }
                                    });
                                    kDownloader.download();
                                }

                                @Override
                                public void onFail(final Exception e) {
                                    String msg;
                                    if (e != null) {
                                        msg = e.getMessage();
                                    } else {
                                        msg = "Error occurred while loading new Recovery Lists";
                                    }
                                    Snackbar
                                            .make(mSwipeUpdater, msg, Snackbar.LENGTH_SHORT)
                                            .show();
                                    mSwipeUpdater.setRefreshing(false);
                                }
                            });
                            rDownloader.download();
                        } else {
                            /* Lists are up to date */
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!App.Preferences.getBoolean(App.PREF_KEY_HIDE_UPDATE_HINTS, false)) {
                                        Snackbar.make(mRashrCards, R.string.uptodate, Snackbar.LENGTH_SHORT).show();
                                    }
                                    mSwipeUpdater.setRefreshing(false);
                                }
                            });
                        }
                    }
                });
            }
        });
        updateThread.start();
    }

    /**
     * @return A ArrayList<File> with the last existing images that the user has Flashed
     */
    //TODO: Reimplement with SQLite
    private ArrayList<File> getHistoryFiles() {
        ArrayList<File> history = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            File tmp = new File(App.Preferences.getString(App.PREF_KEY_HISTORY + String.valueOf(i), ""));
            if (tmp.exists() && !tmp.isDirectory()) {
                /* Add file to list */
                history.add(tmp);
            } else {
                /* File has been deleted, clear information */
                App.Preferences.edit().putString(App.PREF_KEY_HISTORY + String.valueOf(i), "").apply();
            }
        }

        return history;
    }
}

