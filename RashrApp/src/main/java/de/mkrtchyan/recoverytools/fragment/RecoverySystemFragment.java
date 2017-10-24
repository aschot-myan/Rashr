package de.mkrtchyan.recoverytools.fragment;

import android.content.Context;
import android.content.DialogInterface;
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

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.mkrtchyan.recoverytools.App;
import de.mkrtchyan.recoverytools.BuildConfig;
import de.mkrtchyan.recoverytools.Device;
import de.mkrtchyan.recoverytools.FlashUtil;
import de.mkrtchyan.recoverytools.R;
import de.mkrtchyan.recoverytools.RashrActivity;
import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.DownloadDialog;
import de.mkrtchyan.utils.Downloader;

public class RecoverySystemFragment extends Fragment {

    public final static String PARAM_TITLE = "title";
    public final static String PARAM_DESC = "desc";
    public final static String PARAM_DEV = "dev";
    public final static String PARAM_SCREENSHOT_URL = "screenshot_url";
    public final static String PARAM_IMG_PATH = "img_path";
    public final static String PARAM_LOGO = "logo";
    public final static String PARAM_VERSIONS = "versions";
    @BindView(R.id.tvSysName)
    AppCompatTextView tvTitle;
    @BindView(R.id.tvRecSysDesc)
    AppCompatTextView tvDesc;
    @BindView(R.id.spVersions)
    AppCompatSpinner spVersions;
    @BindView(R.id.ScreenshotLayout)
    LinearLayout ScreenshotLayout;
    @BindView(R.id.bFlashRecovery)
    AppCompatButton bFlash;
    @BindView(R.id.ivRecLogo)
    AppCompatImageView imLogo;
    @BindView(R.id.tvDevName)
    AppCompatTextView tvDev;
    private String mTitle, mDesc, mDev, mScreenshotURL, mImagePath;
    private int mLogo;
    private ArrayList<String> mVersions;
    private Context mContext;
    private RashrActivity mActivity;


    public RecoverySystemFragment() {
        // Required empty public constructor
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

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mActivity = (RashrActivity) getActivity();
        final ScrollView root =
                (ScrollView) inflater.inflate(R.layout.fragment_recovery_system, container, false);
        ButterKnife.bind(this, root);
        mContext = root.getContext();
        tvTitle.setText(mTitle.toUpperCase());
        tvDesc.setText(mDesc);
        ArrayList<String> formatedVersions = new ArrayList<>();
        for (String versionLinks : mVersions) {
            formatedVersions.add(formatName(versionLinks, mTitle));
        }
        final ArrayAdapter<String> adapter =new ArrayAdapter<>(root.getContext(),
                android.R.layout.simple_list_item_1, formatedVersions);
        spVersions.setAdapter(adapter);
        spVersions.setSelection(0);
        tvDev.setText(mDev);
        if (mLogo == 0) {
            imLogo.setVisibility(View.GONE);
        } else {
            imLogo.setImageResource(mLogo);
        }
        bFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flashSupportedRecovery(mTitle, mVersions.get(spVersions.getSelectedItemPosition()));
            }
        });
        if (mScreenshotURL == null) {
            ScreenshotLayout.setVisibility(View.GONE);
        } else {
            try {
                Downloader links = new Downloader(new URL(mScreenshotURL),
                        new File(mContext.getExternalCacheDir(), "screenshots.json"));
                links.setOverrideFile(true);
                links.setOnDownloadListener(new Downloader.OnDownloadListener() {
                    @Override
                    public void onSuccess(File file) {
                        try {
                            String res = Common.fileContent(file);
                            JSONArray arr = new JSONArray(res);
                            for (int i = 0; i < arr.length(); i++) {
                                final String name = arr.get(i).toString();
                                //Skip file that lists content of url
                                if (name.equals("getScreenshots.php"))
                                    continue;
                                if (name.equals("index.php"))
                                    continue;

                                //All others are filenames of screenshots
                                AppCompatImageView iv = (AppCompatImageView)
                                        inflater.inflate(R.layout.recovery_screenshot, ScreenshotLayout, false);
                                ScreenshotLayout.addView(iv);
                                Picasso picasso = Picasso.with(mContext);
                                picasso.setLoggingEnabled(BuildConfig.DEBUG);
                                picasso.setIndicatorsEnabled(BuildConfig.DEBUG);
                                picasso.load(mScreenshotURL + "/" + name)
                                        .placeholder(R.drawable.ic_launcher_web)
                                        .into(iv);

                            }
                        } catch (JSONException | IOException e) {
                            onFail(e);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        e.printStackTrace();
                    }
                });
                links.download();
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
                    return App.Device.getXZDualName().toUpperCase() + " XZDualRecovery " + split[split.length - 1].replace(".zip", "");
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
                    return "TWRP " + tokens[1] + "-" + tokens[2] + " (" + tokens[tokens.length - 1].replace(App.Device.getRecoveryExt(), "") + ")";
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
                        device += fileName.split("-")[splitNr].replace(App.Device.getRecoveryExt(), "");
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
                        pdevice += fileName.split("-")[splitNr].replace(App.Device.getRecoveryExt(), "");
                    }
                    pdevice += ")";
                    String philzVersion = fileName.split("_")[2].split("-")[0];
                    return "PhilZ Touch " + philzVersion + " " + pdevice;
                case Device.REC_SYS_STOCK:
                    /*
                     * Finding better name for example:
                     *      stock-recovery-angler-7.0.0_nov2016_nbd91k.img -> Stock Recovery 7.0.0 NOV2016 NBD91K (angler)
                     */
                    String sversion = fileName.split("-")[3].replace(App.Device.getRecoveryExt(), "").replace("_", " ").toUpperCase();
                    String deviceName = fileName.split("-")[2];
                    return "Stock Recovery " + sversion + " (" + deviceName + ")";
            }
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        return fileName;
    }

    /**
     * Flash a Recovery provided by Rashr, like ClockworkMod, TWRP, PhilZ, CM, Stock
     *
     * @param system  String containing the Recovery-System type for example:
     *                clockwork, cm, twrp, philz, stock....
     * @param fileUrl File that will be flashed
     */
    public void flashSupportedRecovery(final String system, String fileUrl) {
        /*
         * If there files be needed to flash download it and listing device specified
         * recovery file for example recovery-clockwork-touch-6.0.3.1-grouper.img
         * (read out from RECOVERY_SUMS)
         */
        if (system.equals(Device.REC_SYS_XZDUAL)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle(R.string.warning);
            if (App.Device.isXZDualInstalled()) {
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
                            App.ERRORS.add(failedExecuteCommand.toString() + " Error uninstalling XZDual");
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
        if (system.equals(Device.REC_SYS_TWRP)) {
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
                            mActivity.onBackPressed();
                        } else {
                            flashRecovery(file);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        if (e != null) {
                            App.ERRORS.add(e.toString());
                            View v = getView();
                            if (v == null) return;
                            Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                        RecoveryDownloader.retry();
                    }
                });
                RecoveryDownloader.setAskBeforeDownload(true);
                downloader.setChecksumUrl(fileUrl + ".md5");
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
            if (recovery.exists() && recovery.getName().endsWith(App.Device.getRecoveryExt())
                    && !recovery.isDirectory()) {
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
                    } else if (App.Device.isRecoveryOverRecovery()) {
                        mActivity.switchTo(ScriptManagerFragment.newInstance(recovery));
                    }
                }
            }
        }
    }

}
