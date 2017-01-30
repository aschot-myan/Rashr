package de.mkrtchyan.recoverytools;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;
import org.sufficientlysecure.rootcommands.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.DownloadDialog;
import de.mkrtchyan.utils.Downloader;

public class RecoverySystemFragment extends Fragment {

    private String mTitle, mDesc, mDev, mScreenshotURL, mImagePath;
    private int mLogo;
    private ArrayList<String> mVersions;
    private Context mContext;
    private RashrActivity mActivity;
    private RecoverySystemFragment mView = this;

    public final static String PARAM_TITLE = "title";
    public final static String PARAM_DESC = "desc";
    public final static String PARAM_DEV = "dev";
    public final static String PARAM_SCREENSHOT_URL = "screenshot_url";
    public final static String PARAM_IMG_PATH = "img_path";
    public final static String PARAM_LOGO = "logo";
    public final static String PARAM_VERSIONS = "versions";


    public RecoverySystemFragment() {
        // Required empty public constructor
    }

    private void setTitle(String title) {
        mTitle = title;
    }

    private void setDescription(String desc) {
        mDesc = desc;
    }

    private void setDeveloper(String dev) {
        mDev = dev;
    }

    private void setLogo(int logo) {
        mLogo = logo;
    }

    private void setVersions(ArrayList<String> versions) {
        mVersions = versions;
    }

    private void setScreenshotURL(String url) {
        mScreenshotURL = url;
    }

    private void setPath(String path) {
        mImagePath = path;
    }

    public static RecoverySystemFragment newInstance(Bundle bundle) {
        RecoverySystemFragment fragment = new RecoverySystemFragment();
        fragment.setTitle(bundle.getString(PARAM_TITLE));
        fragment.setDescription((bundle.getString(PARAM_DESC)));
        fragment.setDeveloper(bundle.getString(PARAM_DEV));
        fragment.setVersions(bundle.getStringArrayList(PARAM_VERSIONS));
        fragment.setLogo(bundle.getInt(PARAM_LOGO));
        fragment.setPath(bundle.getString(PARAM_IMG_PATH));
        fragment.setScreenshotURL(bundle.getString(PARAM_SCREENSHOT_URL));
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mActivity = (RashrActivity) getActivity();
        final ScrollView root =
                (ScrollView) inflater.inflate(R.layout.fragment_recovery_system, container, false);
        mContext = root.getContext();
        final AppCompatTextView tvTitle = (AppCompatTextView) root.findViewById(R.id.tvSysName);
        tvTitle.setText(mTitle.toUpperCase());
        final AppCompatTextView tvDesc = (AppCompatTextView) root.findViewById(R.id.tvRecSysDesc);
        tvDesc.setText(mDesc);
        final AppCompatSpinner spVersions = (AppCompatSpinner) root.findViewById(R.id.spVersions);
        ArrayList<String> formatedVersions = new ArrayList<>();
        for (String versionLinks : mVersions) {
            formatedVersions.add(formatName(versionLinks, mTitle));
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(root.getContext(), android.R.layout.simple_list_item_1, formatedVersions);
        spVersions.setAdapter(adapter);
        spVersions.setSelection(0);
        final AppCompatTextView tvDev = (AppCompatTextView) root.findViewById(R.id.tvDevName);
        tvDev.setText(mDev);
        final AppCompatImageView imLogo = (AppCompatImageView) root.findViewById(R.id.ivRecLogo);
        if (mLogo == 0) {
            root.removeView(imLogo);
        } else {
            imLogo.setImageResource(mLogo);
        }
        final AppCompatButton bFlash = (AppCompatButton) root.findViewById(R.id.bFlashRecovery);
        bFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flashSupportedRecovery(mTitle, mVersions.get(spVersions.getSelectedItemPosition()));
            }
        });
        final LinearLayout ScreenshotLayout = (LinearLayout) root.findViewById(R.id.ScreenshotLayout);
        if (mScreenshotURL == null) {
            Log.d(Const.RASHR_TAG, "No screenshots");
            ((ViewGroup)ScreenshotLayout.getParent()).removeView(ScreenshotLayout);
        } else {
            try {
                Downloader jsonDownloader = new Downloader(new URL(mScreenshotURL + "/getScreenshots.php"),
                        new File(mContext.getExternalCacheDir(), "screenhots.json"));
                jsonDownloader.setOverrideFile(true);
                jsonDownloader.setOnDownloadListener(new Downloader.OnDownloadListener() {
                    @Override
                    public void onSuccess(File file) {
                        try {
                            JSONArray arr = new JSONArray(Common.fileContent(file));
                            for (int i = 0; i < arr.length(); i++) {
                                final String name = arr.get(i).toString();
                                if (name.equals(".") || name.equals("..") || name.equals("getScreenshots.php"))
                                    continue;
                                Downloader imageDownloader = new Downloader(
                                        new URL(mScreenshotURL + "/" + name),
                                        new File(file.getParentFile(), name));
                                imageDownloader.setOverrideFile(false); //Do not redownload predownloaded images
                                imageDownloader.setOnDownloadListener(new Downloader.OnDownloadListener() {
                                    @Override
                                    public void onSuccess(File file) {
                                        AppCompatImageView iv = (AppCompatImageView)
                                                inflater.inflate(R.layout.recovery_screenshot, null);
                                        Bitmap screenshot = BitmapFactory.decodeFile(file.toString());
                                        iv.setImageBitmap(screenshot);
                                        ScreenshotLayout.addView(iv);
                                    }

                                    @Override
                                    public void onFail(Exception e) {

                                    }
                                });
                                imageDownloader.download();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            onFail(e);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        e.printStackTrace();
                    }
                });
                jsonDownloader.download();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return root;
    }

    /**
     * formatName formats the fileName to a better one for the User (NO FILE WILL BE TOUCHED)
     * you need to work with the real name to flash like @param fileName
     *
     * @param fileName for example recovery-clockwork-touch-rndversion-rnddevice.image
     * @param system   Supported recovery Systems twrp, cwm, philz, xzdual, cm, stock
     * @return Formatted Filename like ClockworkMod Touch 5.8.x.x (grouper) // Nexus 7 2012
     */
    private String formatName(final String fileName, final String system) {
        try {
            switch (system) {
                case Device.REC_SYS_XZDUAL:
                    /*
                     * Finding better name for example:
                     *      Z3-lockeddualrecovery2.8.21.zip -> Z3 XZDualRecovery 2.8.21
                     */
                    String split[] = fileName.split("lockeddualrecovery");
                    return RashrApp.DEVICE.getXZDualName().toUpperCase() + " XZDualRecovery " + split[split.length - 1].replace(".zip", "");
                case Device.REC_SYS_TWRP:
                    /*
                     * Finding better name for example:
                     *      twrp-3.0.0-1-zeroflte.img -> TWRP 3.0.0-1 (zeroflte)
                     */
                    String tokens[] = fileName.split("-");
                    if (tokens.length == 3) {
                        return "TWRP " + tokens[1] + " (" + tokens[tokens.length - 1].substring(0, tokens[tokens.length - 1].length()) + ")";
                    }
                    // Contains revision number like 3.0.0-1
                    // Example twrp-3.0.0-1-zeroflte.img
                    return "TWRP " + tokens[1] + "-" + tokens[2] + " (" + tokens[tokens.length - 1].replace(RashrApp.DEVICE.getRecoveryExt(), "") + ")";
                case Device.REC_SYS_CWM:
                    /*
                     * Finding better name for example:
                     *      recovery-clockwork-touch-6.0.4.7-mako.img -> ClockworkMod Touch 6.0.4.7 (mako)
                     */
                    int startIndex;
                    String cversion = "";
                    if (fileName.contains("-touch-")) {
                        startIndex = 4;
                        cversion = "Touch ";
                    } else {
                        startIndex = 3;
                    }
                    cversion += fileName.split("-")[startIndex - 1];
                    String device = "(";
                    for (int splitNr = startIndex; splitNr < fileName.split("-").length; splitNr++) {
                        if (!device.equals("(")) device += "-";
                        device += fileName.split("-")[splitNr].replace(RashrApp.DEVICE.getRecoveryExt(), "");
                    }
                    device += ")";
                    return "ClockworkMod " + cversion + " " + device;
                case Device.REC_SYS_PHILZ:
                    /*
                     * Finding better name for example:
                     *      philz_touch_6.57.9-mako.img -> PhilZ Touch 6.57.9 (mako)
                     */
                    String pdevice = "(";
                    for (int splitNr = 1; splitNr < fileName.split("-").length; splitNr++) {
                        if (!pdevice.equals("(")) pdevice += "-";
                        pdevice += fileName.split("-")[splitNr].replace(RashrApp.DEVICE.getRecoveryExt(), "");
                    }
                    pdevice += ")";
                    String philzVersion = fileName.split("_")[2].split("-")[0];
                    return "PhilZ Touch " + philzVersion + " " + pdevice;
                case Device.REC_SYS_STOCK:
                    /*
                     * Finding better name for example:
                     *      stock-recovery-hammerhead-6.0.1.img -> Stock Recovery 6.0.1 (hammerhead)
                     */
                    String sversion = fileName.split("-")[3].replace(RashrApp.DEVICE.getRecoveryExt(), "");
                    String deviceName = fileName.split("-")[2];
                    return "Stock Recovery " + sversion + " (" + deviceName + ")";
                case Device.REC_SYS_CM:
                    /*
                     * Finding better name for example:
                     *      cm-13-20160224-NIGHTLY-hammerhead ->
                     *              CyanogenMod Recovery 13 nightly 20160224 (hammerhead)
                     */
                    String cmVersion = fileName.split("-")[1];
                    String date = fileName.split("-")[2];
                    String build = fileName.split("-")[3];
                    deviceName = fileName.split("-")[4];
                    return "CyanogenMod Recovery " + cmVersion + " " + build + " " + date + " (" + deviceName + ")";
            }
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        return fileName;
    }

    /**
     * Flash a Recovery provided by Rashr, like ClockworkMod, TWRP, PhilZ, CM, Stock
     *
     * @param system String containing the Recovery-System type for example:
     *             clockwork, cm, twrp, philz, stock....
     * @param fileUrl File that will be flashed
     */
    public void flashSupportedRecovery(final String system, String fileUrl) {
        /**
         * If there files be needed to flash download it and listing device specified
         * recovery file for example recovery-clockwork-touch-6.0.3.1-grouper.img
         * (read out from RECOVERY_SUMS)
         */
        if (system.equals(Device.REC_SYS_XZDUAL)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setTitle(R.string.warning);
            if (RashrApp.DEVICE.isXZDualInstalled()) {
                alert.setMessage(R.string.xzdual_uninstall_alert);
                alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder abuilder = new AlertDialog.Builder(mContext);
                        abuilder.setTitle(R.string.info);
                        abuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        try {
                            FlashUtil.uninstallXZDual();
                            abuilder.setMessage(R.string.xzdual_uninstall_successfull);
                        } catch (FailedExecuteCommand failedExecuteCommand) {
                            abuilder.setMessage(getString(R.string.xzdual_uninstall_failed) + "\n" +
                                    failedExecuteCommand.toString());
                            failedExecuteCommand.printStackTrace();
                            RashrApp.ERRORS.add(failedExecuteCommand.toString() + " Error uninstalling XZDual");
                        }
                        abuilder.show();
                    }
                });
                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                return;
            }
        }
        String fileName = "";
        if (system.equals(Device.REC_SYS_CM) || system.equals(Device.REC_SYS_TWRP)) {
            fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        }
        final File recovery = new File(mImagePath, fileName);
        if (!recovery.exists()) {
            try {
                URL url = new URL(fileUrl);
                final Downloader downloader = new Downloader(url, recovery);
                final DownloadDialog RecoveryDownloader = new DownloadDialog(mContext, downloader);
                if (system.equals(Device.REC_SYS_TWRP)) {
                    downloader.setReferrer(fileUrl);
                }
                RecoveryDownloader.setOnDownloadListener(new DownloadDialog.OnDownloadListener() {
                    @Override
                    public void onSuccess(File file) {
                        if (system.equals(Device.REC_SYS_XZDUAL)) {
                            FlashUtil flasher = new FlashUtil(getActivity(), file, FlashUtil.JOB_INSTALL_XZDUAL);
                            flasher.execute();
                        } else {
                            flashRecovery(file);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        if (e != null) {
                            RashrApp.ERRORS.add(e.toString());
                            Snackbar.make(mView.getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                        RecoveryDownloader.retry();
                    }
                });
                RecoveryDownloader.setAskBeforeDownload(true);
                downloader.setChecksumFile(Const.RecoveryCollectionFile);
                RecoveryDownloader.ask();
            } catch (MalformedURLException ignored) {
            }
        } else {
            flashRecovery(recovery);
        }
    }

    /**
     * Flash recovery using FlashUtil
     *
     * @param recovery recovery image (appropriated for this device)
     */
    private void flashRecovery(final File recovery) {
        if (recovery != null) {
            if (recovery.exists() && recovery.getName().endsWith(RashrApp.DEVICE.getRecoveryExt())
                    && !recovery.isDirectory()) {
                if (!RashrApp.DEVICE.isFOTAFlashed() && !RashrApp.DEVICE.isRecoveryOverRecovery()) {
                    /** Flash not need to be handled specially */
                    final FlashUtil flashUtil = new FlashUtil(mActivity, recovery, FlashUtil.JOB_FLASH_RECOVERY);
                    flashUtil.setOnTaskDoneListener(new FlashUtil.OnTaskDoneListener() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFail(Exception e) {
                            RashrApp.ERRORS.add(e.toString());
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
                                        mActivity.switchTo(SettingsFragment.newInstance());
                                    }
                                });
                            } else if (e instanceof FlashUtil.ImageToBigException) {
                                d.setMessage(String.format(getString(R.string.image_to_big_message), ((FlashUtil.ImageToBigException) e).getCustomSize() / (1024 * 1024), ((FlashUtil.ImageToBigException) e).getPartitionSize() / (1024 * 1024)));
                                d.setNeutralButton(R.string.settings, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mActivity.switchTo(SettingsFragment.newInstance());
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
                    /** Flashing needs to be handled specially (not standard flash method)*/
                    if (RashrApp.DEVICE.isFOTAFlashed()) {
                        /** Show warning if FOTAKernel will be flashed */
                        new AlertDialog.Builder(mContext)
                                .setTitle(R.string.warning)
                                .setMessage(R.string.fota)
                                .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final FlashUtil flashUtil = new FlashUtil(mActivity, recovery, FlashUtil.JOB_FLASH_RECOVERY);
                                        flashUtil.execute();
                                    }
                                })
                                .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    } else if (RashrApp.DEVICE.isRecoveryOverRecovery()) {
                        mActivity.switchTo(ScriptManagerFragment.newInstance(mActivity, recovery));
                    }
                }
            }
        }
    }

}
