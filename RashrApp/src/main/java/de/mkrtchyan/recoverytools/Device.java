package de.mkrtchyan.recoverytools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.util.FailedExecuteCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.mkrtchyan.utils.Downloader;
import de.mkrtchyan.utils.Unzipper;

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
public class Device {

    public static final int PARTITION_TYPE_NOT_SUPPORTED = 0;
    /**
     * This class content all device specified information to provide
     * all information for all other classes for example:
     * What kind of partition and where is the recovery partition in the
     * FileSystem
     */
    private int mRECOVERY_TYPE = PARTITION_TYPE_NOT_SUPPORTED;
    private int mKERNEL_TYPE = PARTITION_TYPE_NOT_SUPPORTED;
    public static String EXT_IMG = ".img";
    public static String EXT_TAR = ".tar";
    public static String EXT_ZIP = ".zip";
    public static final int PARTITION_TYPE_DD = 1;
    public static final int PARTITION_TYPE_MTD = 2;
    public static final int PARTITION_TYPE_RECOVERY = 3;
    public static final int PARTITION_TYPE_SONY = 4;
    /** Collection of known Recovery Partitions on some devices */
    private final File[] RecoveryList = {
            new File("/dev/block/platform/omap/omap_hsmmc.0/by-name/recovery"),
            new File("/dev/block/platform/omap/omap_hsmmc.1/by-name/recovery"),
            new File("/dev/block/platform/sdhci-tegra.3/by-name/recovery"),
            new File("/dev/block/platform/sdhci-pxav3.2/by-name/RECOVERY"),
            new File("/dev/block/platform/comip-mmc.1/by-name/recovery"),
            new File("/dev/block/platform/msm_sdcc.1/by-name/FOTAKernel"),
            new File("/dev/block/platform/msm_sdcc.1/by-name/recovery"),
            new File("/dev/block/platform/sprd-sdhci.3/by-name/KERNEL"),
            new File("/dev/block/platform/sdhci-tegra.3/by-name/SOS"),
            new File("/dev/block/platform/sdhci-tegra.3/by-name/USP"),
            new File("/dev/block/platform/dw_mmc.0/by-name/recovery"),
            new File("/dev/block/platform/dw_mmc.0/by-name/RECOVERY"),
            new File("/dev/block/platform/hi_mci.1/by-name/recovery"),
            new File("/dev/block/platform/sdhci-tegra.3/by-name/UP"),
            new File("/dev/block/platform/sdhci-tegra.3/by-name/SS"),
            new File("/dev/block/platform/sdhci.1/by-name/RECOVERY"),
            new File("/dev/block/platform/sdhci.1/by-name/recovery"),
            new File("/dev/block/platform/dw_mmc/by-name/recovery"),
            new File("/dev/block/platform/dw_mmc/by-name/RECOVERY"),
            new File("/system/bin/recovery.tar"),
            new File("/dev/block/recovery"),
            new File("/dev/block/nandg"),
            new File("/dev/block/acta"),
            new File("/dev/recovery")
    };
    /** Collection of known Kernel Partitions on some devices */
    private final File[] KernelList = {
            new File("/dev/block/platform/omap/omap_hsmmc.0/by-name/boot"),
            new File("/dev/block/platform/sprd-sdhci.3/by-name/KERNEL"),
            new File("/dev/block/platform/sdhci-tegra.3/by-name/LNX"),
            new File("/dev/block/platform/msm_sdcc.1/by-name/Kernel"),
            new File("/dev/block/platform/msm_sdcc.1/by-name/boot"),
            new File("/dev/block/platform/sdhci.1/by-name/KERNEL"),
            new File("/dev/block/platform/sdhci.1/by-name/boot"),
            new File("/dev/block/nandc"),
            new File("/dev/boot")
    };
    private String mName = Build.DEVICE.toLowerCase();
    private String mManufacture = Build.MANUFACTURER.toLowerCase();
    private String mBoard = Build.BOARD.toLowerCase();
    private String mRecoveryPath = "";
    private String mRecoveryVersion = "Not recognized Recovery-Version";
    private String mKernelVersion = "Linux " + System.getProperty("os.version");
    private String mKernelPath = "";
    private String mRECOVERY_EXT = EXT_IMG;
    private String mKERNEL_EXT = EXT_IMG;
    private ArrayList<String> mStockRecoveries = new ArrayList<>();
    private ArrayList<String> mTwrpRecoveries = new ArrayList<>();
    private ArrayList<String> mCwmRecoveries = new ArrayList<>();
    private ArrayList<String> mPhilzRecoveries = new ArrayList<>();
    private ArrayList<String> mStockKernel = new ArrayList<>();

    private File flash_image = new File("/system/bin", "flash_image");
    private File dump_image = new File("/system/bin", "dump_image");

    private RashrActivity mActivity;
    private Context mContext;
    private Shell mShell;

    public Device(RashrActivity activity) {
        mActivity = activity;
        mContext = activity;
        mShell = activity.getShell();
        setPredefinedOptions();
        loadRecoveryList();
        loadKernelList();
    }

    private void setPredefinedOptions() {

        String MODEL = Build.MODEL.toLowerCase();

        /** Set Name and predefined options */
//      Unified Motorola CM Build
        if (mManufacture.equals("motorola") && mBoard.equals("msm8960")) mName = "moto_msm8960";

//      LG Optimus L7
        if (MODEL.equals("lg-p710") || mName.equals("vee7e")) mName = "p710";

//      Acer Iconia Tab A500
        if (mName.equals("a500")) mName = "picasso";

//      Motorola DROID RAZR M
        if (mName.equals("xt907")) mName = "scorpion_mini";

//      ASUS PadFone
        if (mName.equals("padfone")) mName = "a66";

//      HTC Fireball
        if (mName.equals("valentewx")) mName = "fireball";

//      LG Optimus GX2
        if (mBoard.equals("p990")) mName = "p990";

//      Motorola Photon Q 4G LTE
        if (mName.equals("xt897c") || mBoard.equals("xt897")) mName = "xt897";

//      Motorola Atrix HD
        if (mName.equals("mb886") || MODEL.equals("mb886"))
            mName = "qinara";

//      LG Optimus G International
        if (mBoard.equals("geehrc")) mName = "e975";

//      LG Optimus G
        if (mBoard.equals("geefhd")) mName = "e988";

//      Motorola DROID4
        if (mName.equals("cdma_maserati") || mBoard.equals("maserati")) mName = "maserati";

//      LG Spectrum 4G (vs920)
        if (mName.equals("d1lv") || mBoard.equals("d1lv")) mName = "vs930";

//      Motorola Droid 2 WE
        if (mName.equals("cdma_droid2we")) mName = "droid2we";

//      OPPO Find 5
        if (mName.equals("x909") || mName.equals("x909t")) mName = "find5";

//      Samsung Galaxy S +
        if (mName.equals("gt-i9001") || mBoard.equals("gt-i9001") || MODEL.equals("gt-i9001"))
            mName = "galaxysplus";

//      Samsung Galaxy Tab 7 Plus
        if (mName.equals("gt-p6200")) mName = "p6200";

//      Samsung Galaxy Note 8.0
        if (MODEL.equals("gt-n5110")) mName = "konawifi";

//		Kindle Fire HD 7"
        if (mName.equals("d01e")) mName = "kfhd7";

        if (mBoard.equals("rk29sdk")) mName = "rk29sdk";

//      HTC ONE GSM
        if (mBoard.equals("m7") || mName.equals("m7") || mName.equals("m7ul")) mName = "m7";

        if (mName.equals("m7spr"))
            mName = "m7wls";

//		Galaxy Note
        if (mName.equals("gt-n7000") || mName.equals("n7000") || mName.equals("galaxynote")
                || mName.equals("n7000") || mBoard.equals("gt-n7000") || mBoard.equals("n7000")
                || mBoard.equals("galaxynote") || mBoard.equals("N7000"))
            mName = "n7000";

        if (mName.equals("p4noterf") || MODEL.equals("gt-n8000")) mName = "n8000";

//      Samsung Galaxy Note 10.1
        if (MODEL.equals("gt-n8013") || mName.equals("p4notewifi")) mName = "n8013";

//      Samsung Galaxy Tab 2
        if (mBoard.equals("piranha") || MODEL.equals("gt-p3110")) mName = "p3110";

        if (mName.equals("espressowifi") || MODEL.equals("gt-p3113")) mName = "p3113";

//		Galaxy Note 2
        if (mName.equals("n7100") || mName.equals("n7100") || mName.equals("gt-n7100")
                || MODEL.equals("gt-n7100") || mBoard.equals("t03g") || mBoard.equals("n7100")
                || mBoard.equals("gt-n7100"))
            mName = "t03g";

//		Galaxy Note 2 LTE
        if (mName.equals("t0ltexx") || mName.equals("gt-n7105") || mName.equals("t0ltedv")
                || mName.equals("gt-n7105T") || mName.equals("t0ltevl") || mName.equals("sgh-I317m")
                || mBoard.equals("t0ltexx") || mBoard.equals("gt-n7105") || mBoard.equals("t0ltedv")
                || mBoard.equals("gt-n7105T") || mBoard.equals("t0ltevl") || mBoard.equals("sgh-i317m"))
            mName = "t0lte";

        if (mName.equals("sgh-i317") || mBoard.equals("t0lteatt") || mBoard.equals("sgh-i317"))
            mName = "t0lteatt";

        if (mName.equals("sgh-t889") || mBoard.equals("t0ltetmo") || mBoard.equals("sgh-t889"))
            mName = "t0ltetmo";

        if (mBoard.equals("t0ltecan")) mName = "t0ltecan";

//		Galaxy S3 (international)
        if (mName.equals("gt-i9300") || mName.equals("galaxy s3") || mName.equals("galaxys3")
                || mName.equals("m0") || mName.equals("i9300") || mBoard.equals("gt-i9300")
                || mBoard.equals("m0") || mBoard.equals("i9300"))
            mName = "i9300";

//		Galaxy S2
        if (mName.equals("gt-i9100g") || mName.equals("gt-i9100m") || mName.equals("gt-i9100p")
                || mName.equals("gt-i9100") || mName.equals("galaxys2") || mBoard.equals("gt-i9100g")
                || mBoard.equals("gt-i9100m") || mBoard.equals("gt-i9100p")
                || mBoard.equals("gt-i9100") || mBoard.equals("galaxys2"))
            mName = "galaxys2";

//		Galaxy S2 ATT
        if (mName.equals("sgh-i777") || mBoard.equals("sgh-i777") || mBoard.equals("galaxys2att"))
            mName = "galaxys2att";

//		Galaxy S2 LTE (skyrocket)
        if (mName.equals("sgh-i727") || mBoard.equals("skyrocket") || mBoard.equals("sgh-i727"))
            mName = "skyrocket";

//      Galaxy S3 (International/i9300)
        if (mName.equals("m3") && mManufacture.equals("samsung")) mName = "i9305";

//      Galaxy S (i9000)
        if (mName.equals("galaxys") || mName.equals("galaxysmtd") || mName.equals("gt-i9000")
                || mName.equals("gt-i9000m") || mName.equals("gt-i9000t") || mBoard.equals("galaxys")
                || mBoard.equals("galaxysmtd") || mBoard.equals("gt-i9000") || mBoard.equals("gt-i9000m")
                || mBoard.equals("gt-i9000t") || MODEL.equals("gt-i9000t") || mName.equals("sph-d710")
                || mName.equals("sph-d710bst") || MODEL.equals("sph-d710bst"))
            mName = "galaxys";

//      Samsung Galaxy Note
        if (mName.equals("gt-n7000b")) mName = "n7000";

//		GalaxyS Captivate (SGH-I897)
        if (mName.equals("sgh-i897")) mName = ("captivate");

        if (mBoard.equals("gee") && mManufacture.equals("lge")) mName = "geeb";

//		Sony Xperia Z (C6603)
        if (mName.equals("c6603")) mName = "yuga";

        if (mName.equals("c6603") || mName.equals("c6602")) mRECOVERY_EXT = EXT_TAR;

//      HTC Desire HD
        if (mBoard.equals("ace")) mName = "ace";

//      Motorola Droid X
        if (mName.equals("cdma_shadow") || mBoard.equals("shadow") || MODEL.equals("droidx"))
            mName = "shadow";

//      LG Optimus L9
        if (mName.equals("u2") || mBoard.equals("u2") || MODEL.equals("lg-p760")) mName = "p760";

//      LG Optimus L5
        if (mName.equals("m4") || MODEL.equals("lg-e610")) mName = "e610";

//      Huawei U9508
        if (mBoard.equals("u9508") || mName.equals("hwu9508")) mName = "u9508";

//      Huawei Ascend P1
        if (mName.equals("hwu9200") || mBoard.equals("u9200") || MODEL.equals("u9200"))
            mName = "u9200";

//      Motorola RAZR
        if (mName.equals("cdma_yangtze") || mBoard.equals("yangtze")) mName = "yangtze";

//      Motorola Droid RAZR
        if (mName.equals("cdma_spyder") || mBoard.equals("spyder")) mName = "spyder";

//      Huawei M835
        if (mName.equals("hwm835") || mBoard.equals("m835")) mName = "m835";

//      LG Optimus Black
        if (mName.equals("bproj_cis-xxx") || mBoard.equals("bproj") || MODEL.equals("lg-p970"))
            mName = "p970";

//      LG Optimus X2
        if (mName.equals("star")) mName = "p990";

        if (mName.equals("droid2") || mName.equals("daytona") || mName.equals("captivate")
                || mName.equals("galaxys") || mName.equals("droid2we")) {
            mRECOVERY_TYPE = PARTITION_TYPE_RECOVERY;
            mRECOVERY_EXT = EXT_ZIP;
        }
        readDeviceInfos();
        if (!mRecoveryPath.equals("") && !isRecoveryOverRecovery())
            mRECOVERY_TYPE = PARTITION_TYPE_DD;

//		Devices who kernel will be flashed to
        if (mName.equals("c6602") || mName.equals("yuga")) mRECOVERY_TYPE = PARTITION_TYPE_SONY;

        if (new File("/dev/mtd/").exists()) {
            if (!isRecoveryDD()) {
                mRECOVERY_TYPE = PARTITION_TYPE_MTD;
            }
            if (!isKernelDD()) {
                mKERNEL_TYPE = PARTITION_TYPE_MTD;
            }
        }
        if (!mRecoveryPath.equals("")) {
            if (mRecoveryPath.contains("mtd")) {
                mRECOVERY_TYPE = PARTITION_TYPE_MTD;
            } else {
                mRECOVERY_TYPE = PARTITION_TYPE_DD;
            }
        }

        if (!mKernelPath.equals("")) {
            if (mKernelPath.contains("mtd")) {
                mKERNEL_TYPE = PARTITION_TYPE_MTD;
            } else {
                mKERNEL_TYPE = PARTITION_TYPE_DD;
            }
        }
    }

    public void loadRecoveryList() {

        ArrayList<String> CWMList = new ArrayList<>(), TWRPList = new ArrayList<>(),
                PHILZList = new ArrayList<>(), StockList = new ArrayList<>();

        try {
            String Line;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    new File(mContext.getFilesDir(), "recovery_sums"))));
            while ((Line = br.readLine()) != null) {
                String lowLine = Line.toLowerCase();
                final int NameStartAt = Line.lastIndexOf("/") + 1;
                if (lowLine.endsWith(mRECOVERY_EXT)) {
                    if (lowLine.contains(mName.toLowerCase()) || lowLine.contains(Build.DEVICE.toLowerCase())) {
                        if (lowLine.contains("stock")) {
                            StockList.add(Line.substring(NameStartAt));
                        } else if (lowLine.contains("clockwork") || lowLine.contains("cwm")) {
                            CWMList.add(Line.substring(NameStartAt));
                        } else if (lowLine.contains("twrp")) {
                            TWRPList.add(Line.substring(NameStartAt));
                        } else if (lowLine.contains("philz")) {
                            PHILZList.add(Line.substring(NameStartAt));
                        }
                    }
                }
            }
            br.close();

            Collections.sort(StockList);
            Collections.sort(CWMList);
            Collections.sort(TWRPList);
            Collections.sort(PHILZList);

            /**
             * First clear list before adding items (to avoid double entry on reload by update)
             */
            mStockRecoveries.clear();
            mCwmRecoveries.clear();
            mTwrpRecoveries.clear();
            mPhilzRecoveries.clear();

            /** Sort newest version to first place */
            for (Object i : StockList) {
                mStockRecoveries.add(0, i.toString());
            }
            for (Object i : CWMList) {
                mCwmRecoveries.add(0, i.toString());
            }
            for (Object i : TWRPList) {
                mTwrpRecoveries.add(0, i.toString());
            }
            for (Object i : PHILZList) {
                mPhilzRecoveries.add(0, i.toString());
            }

        } catch (Exception e) {
            mActivity.addError(Constants.DEVICE_TAG, e, false);
        }
    }

    public void loadKernelList() {
        ArrayList<String> StockKernel = new ArrayList<>();

        try {
            String Line;
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(mContext.getFilesDir(), "kernel_sums"))));
            while ((Line = br.readLine()) != null) {
                String lowLine = Line.toLowerCase();
                final int NameStartAt = Line.lastIndexOf("/") + 1;
                if ((lowLine.contains(mName) || lowLine.contains(Build.DEVICE.toLowerCase()))
                        && lowLine.endsWith(mKERNEL_EXT)) {
                    if (lowLine.contains("stock")) {
                        StockKernel.add(Line.substring(NameStartAt));
                    }
                }
            }
            br.close();

            Collections.sort(StockKernel);

            /**
             * First clear list before adding items (to avoid double entry on reload by update)
             */
            mStockKernel.clear();

            /** Sort newest version to first place */
            for (Object i : StockKernel) {
                mStockKernel.add(0, i.toString());
            }

        } catch (Exception e) {
            mActivity.addError(Constants.DEVICE_TAG, e, false);
        }
    }

    public boolean downloadUtils(final Context mContext) {

        final File archive = new File(Constants.PathToUtils, mName + EXT_ZIP);

        final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);
        mAlertDialog
                .setTitle(R.string.warning)
                .setMessage(R.string.download_utils);
        if (mName.equals("montblanc") || mName.equals("c6602") || mName.equals("yuga")) {
            if (!archive.exists()) {
                mAlertDialog.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        new Downloader(mContext, "http://dslnexus.de/Android/utils/", archive.getName(), archive, new Downloader.OnDownloadListener() {
                            @Override
                            public void success(File file) {
                                Unzipper.unzip(archive, new File(Constants.PathToUtils, mName));
                            }

                            @Override
                            public void failed(Exception e) {

                            }
                        }).execute();

                    }
                });
                mAlertDialog.show();
                return true;
            } else {
                Unzipper.unzip(archive, new File(Constants.PathToUtils, mName));
                return false;
            }
        }
        return false;
    }

    private void readDeviceInfos() {

        for (File i : KernelList) {
            if (mKernelPath.equals("")) {
                try {
                    mShell.execCommand("ls " + i.getAbsolutePath());
                    mKernelPath = i.getAbsolutePath();
                    break;
                } catch (FailedExecuteCommand e) {
                    e.printStackTrace();
                    /**
                     * Partition doesn't exist LOLLIPOP Workaround
                     * File.exists() returns always false if file is in hidden FS
                     * Lollipop marks /dev/.... as hidden
                     */
                }
            }
        }
        for (File i : RecoveryList) {
            if (mRecoveryPath.equals("")) {
                try {
                    mShell.execCommand("ls " + i.getAbsolutePath());
                    mRecoveryPath = i.getAbsolutePath();
                    if (mRecoveryPath.endsWith(EXT_TAR)) {
                        mRECOVERY_EXT = EXT_TAR;
                        mRECOVERY_TYPE = PARTITION_TYPE_SONY;
                    }
                    break;
                } catch (FailedExecuteCommand e) {
                    e.printStackTrace();
                    /**
                     * Partition doesn't exist LOLLIPOP Workaround
                     * File.exists() returns always false if file is in hidden FS
                     * Lollipop marks /dev/.... as hidden
                     */
                }
            }
        }

        try {
            String line;
            File LogCopy = new File(mContext.getFilesDir(), Constants.LastLog.getName() + ".txt");
            mShell.execCommand("chmod 644 " + LogCopy.getAbsolutePath());
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(LogCopy)));
            while ((line = br.readLine()) != null) {
                line = line.replace("\"", "");
                line = line.replace("\'", "");
                if (mRecoveryVersion.equals("Not recognized Recovery-Version")) {
                    if (line.contains("ClockworkMod Recovery") || line.contains("CWM")) {
                        mRecoveryVersion = line;
                    } else if (line.contains("TWRP")) {
                        line = line.replace("Starting ", "");
                        line = line.split(" on")[0];
                        mRecoveryVersion = line;
                    } else if (line.contains("PhilZ")) {
                        mRecoveryVersion = line;
                    } else if (line.contains("4EXT")) {
                        line = line.split("4EXT")[1];
                        mRecoveryVersion = line;
                    }
                } else if (!mKernelPath.equals("") && !mRecoveryPath.equals("")) {
                    break;
                }

                if (mKernelPath.equals("")) {
                    if (line.contains("/boot") && !line.contains("/bootloader")) {
                        if (line.contains("mtd")) {
                            mKERNEL_TYPE = PARTITION_TYPE_MTD;
                        } else if (line.contains("/dev/")) {
                            for (String split : line.split(" ")) {

                                if (split.startsWith("/dev") || split.startsWith("/system")) {
                                    try {
                                        mShell.execCommand("ls " + split);
                                        mKernelPath = split;
                                        break;
                                    } catch (FailedExecuteCommand e) {
                                        e.printStackTrace();
                                        /**
                                         * Partition doesn't exist LOLLIPOP Workaround
                                         * File.exists() returns always false if file is in hidden FS
                                         * Lollipop marks /dev/.... as hidden
                                         */
                                    }
                                }

                            }
                        }
                    }
                }

                if (mRecoveryPath.equals("")) {
                    if (line.contains("/recovery")) {
                        if (line.contains("mtd")) {
                            mRECOVERY_TYPE = PARTITION_TYPE_MTD;
                        } else if (line.contains("/dev/")) {
                            for (String split : line.split(" ")) {
                                if (split.startsWith("/dev") || split.startsWith("/system")) {
                                    try {
                                        mShell.execCommand("ls " + split);
                                        mRecoveryPath = split;
                                        break;
                                    } catch (FailedExecuteCommand e) {
                                        e.printStackTrace();
                                        /**
                                         * Partition doesn't exist LOLLIPOP Workaround
                                         * File.exists() returns always false if file is in hidden FS
                                         * Lollipop marks /dev/.... as hidden
                                         */
                                    }
                                }
                            }
                        }
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            mActivity.addError(Constants.DEVICE_TAG, e, false);
        }

        if (mRecoveryPath.equals("")) {
//      ASUS DEVICEs + Same
            if (mName.equals("a66") || mName.equals("c5133") || mName.equals("c5170")
                    || mName.equals("raybst"))
                mRecoveryPath = "/dev/block/mmcblk0p15";

//	    Samsung DEVICEs + Same
            if (mName.equals("d2att") || mName.equals("d2tmo") || mName.equals("d2mtr")
                    || mName.equals("d2vzw") || mName.equals("d2spr") || mName.equals("d2usc")
                    || mName.equals("d2can") || mName.equals("d2cri") || mName.equals("d2vmu")
                    || mName.equals("sch-i929") || mName.equals("e6710") || mName.equals("expresslte")
                    || mName.equals("goghcri") || mName.equals("p710") || mName.equals("im-a810s")
                    || mName.equals("hmh") || mName.equals("ef65l") || mName.equals("pantechp9070"))
                mRecoveryPath = "/dev/block/mmcblk0p18";

            if (mName.equals("i9300") || mName.equals("galaxys2") || mName.equals("n8013")
                    || mName.equals("p3113") || mName.equals("p3110") || mName.equals("p6200")
                    || mName.equals("n8000") || mName.equals("sph-d710vmub") || mName.equals("p920")
                    || mName.equals("konawifi") || mName.equals("t03gctc") || mName.equals("cosmopolitan")
                    || mName.equals("s2vep") || mName.equals("gt-p6810") || mName.equals("baffin")
                    || mName.equals("ivoryss") || mName.equals("crater") || mName.equals("kyletdcmcc"))
                mRecoveryPath = "/dev/block/mmcblk0p6";

            if (mName.equals("t03g") || mName.equals("tf700t") || mName.equals("t0lte")
                    || mName.equals("t0lteatt") || mName.equals("t0ltecan") || mName.equals("t0ltektt")
                    || mName.equals("t0lteskt") || mName.equals("t0ltespr") || mName.equals("t0lteusc")
                    || mName.equals("t0ltevzw") || mName.equals("t0lteatt") || mName.equals("t0ltetmo")
                    || mName.equals("m3") || mName.equals("otter2") || mName.equals("p4notelte"))
                mRecoveryPath = "/dev/block/mmcblk0p9";

            if (mName.equals("golden") || mName.equals("villec2") || mName.equals("vivo")
                    || mName.equals("vivow") || mName.equals("kingdom") || mName.equals("vision")
                    || mName.equals("mystul") || mName.equals("jflteatt") || mName.equals("jfltespi")
                    || mName.equals("jfltecan") || mName.equals("jfltecri") || mName.equals("jfltexx")
                    || mName.equals("jfltespr") || mName.equals("jfltetmo") || mName.equals("jflteusc")
                    || mName.equals("jfltevzw") || mName.equals("i9500") || mName.equals("flyer")
                    || mName.equals("saga") || mName.equals("shooteru") || mName.equals("golfu")
                    || mName.equals("glacier") || mName.equals("runnymede") || mName.equals("protou")
                    || mName.equals("codinametropcs") || mName.equals("codinatmo")
                    || mName.equals("skomer") || mName.equals("magnids"))
                mRecoveryPath = "/dev/block/mmcblk0p21";

            if (mName.equals("jena") || mName.equals("kylessopen") || mName.equals("kyleopen"))
                mRecoveryPath = "/dev/block/mmcblk0p12";

            if (mName.equals("GT-I9103") || mName.equals("mevlana"))
                mRecoveryPath = "/dev/block/mmcblk0p8";

//      LG DEVICEs + Same
            if (mName.equals("e610") || mName.equals("fx3") || mName.equals("hws7300u")
                    || mName.equals("vee3e") || mName.equals("victor") || mName.equals("ef34k")
                    || mName.equals("aviva"))
                mRecoveryPath = "/dev/block/mmcblk0p17";

            if (mName.equals("vs930") || mName.equals("l0") || mName.equals("ca201l")
                    || mName.equals("ef49k") || mName.equals("ot-930") || mName.equals("fx1")
                    || mName.equals("ef47s") || mName.equals("ef46l") || mName.equals("l1v"))
                mRecoveryPath = "/dev/block/mmcblk0p19";

//	    HTC DEVICEs + Same
            if (mName.equals("t6wl")) mRecoveryPath = "/dev/block/mmcblk0p38";

            if (mName.equals("holiday") || mName.equals("vigor") || mName.equals("a68"))
                mRecoveryPath = "/dev/block/mmcblk0p23";

            if (mName.equals("m7") || mName.equals("obakem") || mName.equals("obake")
                    || mName.equals("ovation"))
                mRecoveryPath = "/dev/block/mmcblk0p34";

            if (mName.equals("m7wls")) mRecoveryPath = "/dev/block/mmcblk0p36";

            if (mName.equals("endeavoru") || mName.equals("enrc2b") || mName.equals("p999")
                    || mName.equals("us9230e1") || mName.equals("evitareul") || mName.equals("otter")
                    || mName.equals("e2001_v89_gq2008s"))
                mRecoveryPath = "/dev/block/mmcblk0p5";

            if (mName.equals("ace") || mName.equals("primou"))
                mRecoveryPath = "/dev/block/platform/msm_sdcc.2/mmcblk0p21";

            if (mName.equals("pyramid")) mRecoveryPath = "/dev/block/platform/msm_sdcc.1/mmcblk0p21";

            if (mName.equals("ville") || mName.equals("evita") || mName.equals("skyrocket")
                    || mName.equals("fireball") || mName.equals("jewel") || mName.equals("shooter"))
                mRecoveryPath = "/dev/block/mmcblk0p22";

            if (mName.equals("dlxub1") || mName.equals("dlx") || mName.equals("dlxj")
                    || mName.equals("im-a840sp") || mName.equals("im-a840s") || mName.equals("taurus"))
                mRecoveryPath = "/dev/block/mmcblk0p20";

//	    Motorola DEVICEs + Same
            if (mName.equals("qinara") || mName.equals("f02e") || mName.equals("vanquish_u")
                    || mName.equals("xt897") || mName.equals("solstice") || mName.equals("smq_u"))
                mRecoveryPath = "/dev/block/mmcblk0p32";

            if (mName.equals("pasteur")) mRecoveryPath = "/dev/block/mmcblk1p12";

            if (mName.equals("dinara_td")) mRecoveryPath = "/dev/block/mmcblk1p14";

            if (mName.equals("e975") || mName.equals("e988")) mRecoveryPath = "/dev/block/mmcblk0p28";

            if (mName.equals("shadow") || mName.equals("edison") || mName.equals("venus2"))
                mRecoveryPath = "/dev/block/mmcblk1p16";

            if (mName.equals("spyder") || mName.equals("maserati"))
                mRecoveryPath = "/dev/block/mmcblk1p15";

            if (mName.equals("olympus") || mName.equals("ja3g") || mName.equals("ja3gchnduos")
                    || mName.equals("daytona") || mName.equals("konalteatt") || mName.equals("lc1810")
                    || mName.equals("lt02wifi") || mName.equals("lt013g"))
                mRecoveryPath = "/dev/block/mmcblk0p10";

//	    Sony DEVICEs + Same
            if (mName.equals("nozomi"))
                mRecoveryPath = "/dev/block/mmcblk0p3";

//	    LG DEVICEs + Same
            if (mName.equals("p990") || mName.equals("tf300t"))
                mRecoveryPath = "/dev/block/mmcblk0p7";

            if (mName.equals("x3") || mName.equals("picasso") || mName.equals("picasso_m")
                    || mName.equals("enterprise_ru"))
                mRecoveryPath = "/dev/block/mmcblk0p1";

            if (mName.equals("m3s") || mName.equals("bryce") || mName.equals("melius3g")
                    || mName.equals("meliuslte") || mName.equals("serranolte"))
                mRecoveryPath = "/dev/block/mmcblk0p14";

            if (mName.equals("p970") || mName.equals("u2") || mName.equals("p760") || mName.equals("p768"))
                mRecoveryPath = "/dev/block/mmcblk0p4";

//	    ZTE DEVICEs + Same
            if (mName.equals("warp2") || mName.equals("hwc8813") || mName.equals("galaxysplus")
                    || mName.equals("cayman") || mName.equals("ancora_tmo") || mName.equals("c8812e")
                    || mName.equals("batman_skt") || mName.equals("u8833") || mName.equals("i_vzw")
                    || mName.equals("armani_row") || mName.equals("hwu8825-1") || mName.equals("ad685g")
                    || mName.equals("audi") || mName.equals("a111") || mName.equals("ancora")
                    || mName.equals("arubaslim"))
                mRecoveryPath = "/dev/block/mmcblk0p13";

            if (mName.equals("elden") || mName.equals("hayes") || mName.equals("quantum")
                    || mName.equals("coeus") || mName.equals("c_4"))
                mRecoveryPath = "/dev/block/mmcblk0p16";
        }

        if (!isRecoverySupported()) {
            if (mRecoveryPath.contains("/dev/block")) {
                mRECOVERY_TYPE = PARTITION_TYPE_DD;
            }
        }

        if (!isKernelSupported()) {
            if (mKernelPath.contains("/dev/block")) {
                mKERNEL_TYPE = PARTITION_TYPE_DD;
            }
        }

        if (!isRecoverySupported() || !isKernelSupported()) {
            File PartLayout = new File(mContext.getFilesDir(), Build.DEVICE);
            if (!PartLayout.exists()) {
                try {
                    ZipFile PartLayoutsZip = new ZipFile(new File(mContext.getFilesDir(), "partlayouts.zip"));
                    for (Enumeration e = PartLayoutsZip.entries(); e.hasMoreElements(); ) {
                        ZipEntry entry = (ZipEntry) e.nextElement();
                        if (entry.getName().equals(Build.DEVICE)) {
                            Unzipper.unzipEntry(PartLayoutsZip, entry, mContext.getFilesDir());
                            if (new File(mContext.getFilesDir(), entry.getName()).renameTo(PartLayout)) {
                                throw new IOException("Failed rename File into " + PartLayout);
                            }
                        }
                    }
                } catch (IOException e) {
                    mActivity.addError(Constants.DEVICE_TAG, e, false);
                }
            }
            if (PartLayout.exists()) {
                try {
                    String Line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(PartLayout)));
                    while ((Line = br.readLine()) != null) {
                        Line = Line.replace('"', ' ').replace(':', ' ');
                        File partition = new File("/dev/block/", Line.split(" ")[0]);
                        if (partition.exists()) {
                            if (!isRecoverySupported() && Line.contains("recovery")) {
                                mRecoveryPath = partition.getAbsolutePath();
                                mRECOVERY_TYPE = PARTITION_TYPE_DD;
                            } else if (!isKernelSupported() && Line.contains("boot")
                                    && !Line.contains("bootloader")) {
                                mKernelPath = partition.getAbsolutePath();
                                mKERNEL_TYPE = PARTITION_TYPE_DD;
                            }
                        }
                    }
                } catch (IOException e) {
                    mActivity.addError(Constants.DEVICE_TAG, e, false);
                }
            }
        }
    }

    public File getFlash_image() {
        if (!flash_image.exists()) {
            flash_image = new File(mContext.getFilesDir(), flash_image.getName());
        }
        return flash_image;
    }

    public File getDump_image() {
        if (!dump_image.exists()) {
            dump_image = new File(mContext.getFilesDir(), dump_image.getName());
        }
        return dump_image;
    }

    public boolean isStockRecoverySupported() {
        return mStockRecoveries.size() > 0 && isRecoverySupported();
    }

    public boolean isTwrpRecoverySupported() {
        return mTwrpRecoveries.size() > 0 && isRecoverySupported();
    }

    public boolean isCwmRecoverySupported() {
        return mCwmRecoveries.size() > 0 && isRecoverySupported();
    }

    public boolean isPhilzRecoverySupported() {
        return mPhilzRecoveries.size() > 0 && isRecoverySupported();
    }

    public boolean isStockKernelSupported() {
        return mStockKernel.size() > 0 && isRecoverySupported();
    }

    public boolean isRecoverySupported() {
        return mRECOVERY_TYPE != PARTITION_TYPE_NOT_SUPPORTED;
    }

    public int getRecoveryType() {
        return mRECOVERY_TYPE;
    }

    public boolean isRecoveryMTD() {
        return mRECOVERY_TYPE == PARTITION_TYPE_MTD;
    }

    public boolean isRecoveryDD() {
        return mRECOVERY_TYPE == PARTITION_TYPE_DD;
    }

    public boolean isRecoveryOverRecovery() {
        return mRECOVERY_TYPE == PARTITION_TYPE_RECOVERY;
    }

    public boolean isFOTAFlashed() {
        return mRecoveryPath.toLowerCase().contains("fota");
    }

    public boolean isKernelSupported() {
        return mKERNEL_TYPE != PARTITION_TYPE_NOT_SUPPORTED;
    }

    public boolean isKernelDD() {
        return mKERNEL_TYPE == PARTITION_TYPE_DD;
    }

    public boolean isKernelMTD() {
        return mKERNEL_TYPE == PARTITION_TYPE_MTD;
    }

    public int getKernelType() {
        return mKERNEL_TYPE;
    }

    public String getRecoveryExt() {
        return mRECOVERY_EXT;
    }

    public String getKernelExt() {
        return mKERNEL_EXT;
    }

    public ArrayList<String> getStockRecoveryVersions() {
        return mStockRecoveries;
    }

    public ArrayList<String> getCwmRecoveryVersions() {
        return mCwmRecoveries;
    }

    public ArrayList<String> getTwrpRecoveryVersions() {
        return mTwrpRecoveries;
    }

    public ArrayList<String> getPhilzRecoveryVersions() {
        return mPhilzRecoveries;
    }

    public ArrayList<String> getStockKernelVersions() {
        return mStockKernel;
    }

    public String getRecoveryVersion() {
        return mRecoveryVersion;
    }

    public String getKernelVersion() {
        return mKernelVersion;
    }

    public String getName() {
        return mName;
    }

    public void setName(String Name) {
        this.mName = Name;
    }

    public String getRecoveryPath() {
        return mRecoveryPath;
    }

    public String getKernelPath() {
        return mKernelPath;
    }

    public String getManufacture() {
        return mManufacture;
    }

    public String getBOARD() {
        return mBoard;
    }


}