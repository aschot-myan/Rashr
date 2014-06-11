package de.mkrtchyan.recoverytools;

/**
 * Copyright (c) 2014 Ashot Mkrtchyan
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

import org.sufficientlysecure.rootcommands.Shell;

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

public class Device {

    public static final int PARTITION_TYPE_NOT_SUPPORTED = 0;
    /**
     * This class content all device specified information to provide
     * all information for all other classes for example:
     * What kind of partition and where is the recovery partition in the
     * FileSystem
     */
    private int RECOVERY_TYPE = PARTITION_TYPE_NOT_SUPPORTED;
    private int KERNEL_TYPE = PARTITION_TYPE_NOT_SUPPORTED;
    public static final int PARTITION_TYPE_DD = 1;
    public static final int PARTITION_TYPE_MTD = 2;
    public static final int PARTITION_TYPE_RECOVERY = 3;
    public static final int PARTITION_TYPE_SONY = 4;
    private static final File[] RecoveryList = {
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
    private static final File[] KernelList = {
            new File("/dev/block/platform/omap/omap_hsmmc.0/by-name/boot"),
            new File("/dev/block/platform/sprd-sdhci.3/by-name/KERNEL"),
            new File("/dev/block/platform/sdhci-tegra.3/by-name/LNX"),
            new File("/dev/block/platform/msm_sdcc.1/by-name/Kernel"),
            new File("/dev/block/platform/msm_sdcc.1/by-name/boot"),
            new File("/dev/block/nandc"),
            new File("/dev/boot")
    };
    private String Name = Build.DEVICE.toLowerCase();
    private String MANUFACTURE = Build.MANUFACTURER.toLowerCase();
    private String RecoveryPath = "";
    private String RecoveryVersion = "Not recognized Recovery-Version";
    private String KernelVersion = "Linux " + System.getProperty("os.version");
    private String KernelPath = "";
    private String RECOVERY_EXT = ".img";
    private String KERNEL_EXT = ".img";
    private ArrayList<String> StockRecoveryVersions = new ArrayList<String>();
    private ArrayList<String> TwrpRecoveryVersions = new ArrayList<String>();
    private ArrayList<String> CwmRecoveryVersions = new ArrayList<String>();
    private ArrayList<String> PhilzRecoveryVersions = new ArrayList<String>();
    private ArrayList<String> StockKernelVersions = new ArrayList<String>();

    private File flash_image = new File("/system/bin", "flash_image");
    private File dump_image = new File("/system/bin", "dump_image");

    private Context mContext;

    private ArrayList<String> ERRORS = new ArrayList<String>();

    public Device(Context mContext) {
        this.mContext = mContext;
        setPredefinedOptions();
        loadRecoveryList();
        loadKernelList();
    }

    private void setPredefinedOptions() {

        String BOARD = Build.BOARD.toLowerCase();
        String MODEL = Build.MODEL.toLowerCase();

        /** Set Name and predefined options */
//      Unified Motorola CM Build
        if (MANUFACTURE.equals("motorola") && BOARD.equals("msm8960")) Name = "moto_msm8960";

//      LG Optimus L7
        if (MODEL.equals("lg-p710") || Name.equals("vee7e")) Name = "p710";

//      Acer Iconia Tab A500
        if (Name.equals("a500")) Name = "picasso";

//      Motorola DROID RAZR M
        if (Name.equals("xt907")) Name = "scorpion_mini";

//      ASUS PadFone
        if (Name.equals("padfone")) Name = "a66";

//      HTC Fireball
        if (Name.equals("valentewx")) Name = "fireball";

//      LG Optimus GX2
        if (BOARD.equals("p990")) Name = "p990";

//      Motorola Photon Q 4G LTE
        if (Name.equals("xt897c") || BOARD.equals("xt897")) Name = "xt897";

//      Motorola Atrix HD
        if (Name.equals("mb886") || MODEL.equals("mb886"))
            Name = "qinara";

//      LG Optimus G International
        if (BOARD.equals("geehrc")) Name = "e975";

//      LG Optimus G
        if (BOARD.equals("geefhd")) Name = "e988";

//      Motorola DROID4
        if (Name.equals("cdma_maserati") || BOARD.equals("maserati")) Name = "maserati";

//      LG Spectrum 4G (vs920)
        if (Name.equals("d1lv") || BOARD.equals("d1lv")) Name = "vs930";

//      Motorola Droid 2 WE
        if (Name.equals("cdma_droid2we")) Name = "droid2we";

//      OPPO Find 5
        if (Name.equals("x909") || Name.equals("x909t")) Name = "find5";

//      Samsung Galaxy S +
        if (Name.equals("gt-i9001") || BOARD.equals("gt-i9001") || MODEL.equals("gt-i9001"))
            Name = "galaxysplus";

//      Samsung Galaxy Tab 7 Plus
        if (Name.equals("gt-p6200")) Name = "p6200";

//      Samsung Galaxy Note 8.0
        if (MODEL.equals("gt-n5110")) Name = "konawifi";

//		Kindle Fire HD 7"
        if (Name.equals("d01e")) Name = "kfhd7";

        if (BOARD.equals("rk29sdk")) Name = "rk29sdk";

//      HTC ONE GSM
        if (BOARD.equals("m7") || Name.equals("m7") || Name.equals("m7ul")) Name = "m7";

        if (Name.equals("m7spr"))
            Name = "m7wls";

//		Galaxy Note
        if (Name.equals("gt-n7000") || Name.equals("n7000") || Name.equals("galaxynote")
                || Name.equals("n7000") || BOARD.equals("gt-n7000") || BOARD.equals("n7000")
                || BOARD.equals("galaxynote") || BOARD.equals("N7000"))
            Name = "n7000";

        if (Name.equals("p4noterf") || MODEL.equals("gt-n8000")) Name = "n8000";

//      Samsung Galaxy Note 10.1
        if (MODEL.equals("gt-n8013") || Name.equals("p4notewifi")) Name = "n8013";

//      Samsung Galaxy Tab 2
        if (BOARD.equals("piranha") || MODEL.equals("gt-p3110")) Name = "p3110";

        if (Name.equals("espressowifi") || MODEL.equals("gt-p3113")) Name = "p3113";

//		Galaxy Note 2
        if (Name.equals("n7100") || Name.equals("n7100") || Name.equals("gt-n7100")
                || MODEL.equals("gt-n7100") || BOARD.equals("t03g") || BOARD.equals("n7100")
                || BOARD.equals("gt-n7100"))
            Name = "t03g";

//		Galaxy Note 2 LTE
        if (Name.equals("t0ltexx") || Name.equals("gt-n7105") || Name.equals("t0ltedv")
                || Name.equals("gt-n7105T") || Name.equals("t0ltevl") || Name.equals("sgh-I317m")
                || BOARD.equals("t0ltexx") || BOARD.equals("gt-n7105") || BOARD.equals("t0ltedv")
                || BOARD.equals("gt-n7105T") || BOARD.equals("t0ltevl") || BOARD.equals("sgh-i317m"))
            Name = "t0lte";

        if (Name.equals("sgh-i317") || BOARD.equals("t0lteatt") || BOARD.equals("sgh-i317"))
            Name = "t0lteatt";

        if (Name.equals("sgh-t889") || BOARD.equals("t0ltetmo") || BOARD.equals("sgh-t889"))
            Name = "t0ltetmo";

        if (BOARD.equals("t0ltecan")) Name = "t0ltecan";

//		Galaxy S3 (international)
        if (Name.equals("gt-i9300") || Name.equals("galaxy s3") || Name.equals("galaxys3")
                || Name.equals("m0") || Name.equals("i9300") || BOARD.equals("gt-i9300")
                || BOARD.equals("m0") || BOARD.equals("i9300"))
            Name = "i9300";

//		Galaxy S2
        if (Name.equals("gt-i9100g") || Name.equals("gt-i9100m") || Name.equals("gt-i9100p")
                || Name.equals("gt-i9100") || Name.equals("galaxys2") || BOARD.equals("gt-i9100g")
                || BOARD.equals("gt-i9100m") || BOARD.equals("gt-i9100p")
                || BOARD.equals("gt-i9100") || BOARD.equals("galaxys2"))
            Name = "galaxys2";

//		Galaxy S2 ATT
        if (Name.equals("sgh-i777") || BOARD.equals("sgh-i777") || BOARD.equals("galaxys2att"))
            Name = "galaxys2att";

//		Galaxy S2 LTE (skyrocket)
        if (Name.equals("sgh-i727") || BOARD.equals("skyrocket") || BOARD.equals("sgh-i727"))
            Name = "skyrocket";

//      Galaxy S3 (International/i9300)
        if (Name.equals("m3") && MANUFACTURE.equals("samsung")) Name = "i9300";

//      Galaxy S (i9000)
        if (Name.equals("galaxys") || Name.equals("galaxysmtd") || Name.equals("gt-i9000")
                || Name.equals("gt-i9000m") || Name.equals("gt-i9000t") || BOARD.equals("galaxys")
                || BOARD.equals("galaxysmtd") || BOARD.equals("gt-i9000") || BOARD.equals("gt-i9000m")
                || BOARD.equals("gt-i9000t") || MODEL.equals("gt-i9000t") || Name.equals("sph-d710")
                || Name.equals("sph-d710bst") || MODEL.equals("sph-d710bst"))
            Name = "galaxys";

//      Samsung Galaxy Note
        if (Name.equals("gt-n7000b")) Name = "n7000";

//		GalaxyS Captivate (SGH-I897)
        if (Name.equals("sgh-i897")) Name = ("captivate");

        if (BOARD.equals("gee") && MANUFACTURE.equals("lge")) Name = "geeb";

//		Sony Xperia Z (C6603)
        if (Name.equals("c6603")) Name = "yuga";

        if (Name.equals("c6603") || Name.equals("c6602")) RECOVERY_EXT = ".tar";

//      HTC Desire HD
        if (BOARD.equals("ace")) Name = "ace";

//      Motorola Droid X
        if (Name.equals("cdma_shadow") || BOARD.equals("shadow") || MODEL.equals("droidx"))
            Name = "shadow";

//      LG Optimus L9
        if (Name.equals("u2") || BOARD.equals("u2") || MODEL.equals("lg-p760")) Name = "p760";

//      LG Optimus L5
        if (Name.equals("m4") || MODEL.equals("lg-e610")) Name = "e610";

//      Huawei U9508
        if (BOARD.equals("u9508") || Name.equals("hwu9508")) Name = "u9508";

//      Huawei Ascend P1
        if (Name.equals("hwu9200") || BOARD.equals("u9200") || MODEL.equals("u9200"))
            Name = "u9200";

//      Motorola RAZR
        if (Name.equals("cdma_yangtze") || BOARD.equals("yangtze")) Name = "yangtze";

//      Motorola Droid RAZR
        if (Name.equals("cdma_spyder") || BOARD.equals("spyder")) Name = "spyder";

//      Huawei M835
        if (Name.equals("hwm835") || BOARD.equals("m835")) Name = "m835";

//      LG Optimus Black
        if (Name.equals("bproj_cis-xxx") || BOARD.equals("bproj") || MODEL.equals("lg-p970"))
            Name = "p970";

//      LG Optimus X2
        if (Name.equals("star")) Name = "p990";

        if (Name.equals("droid2") || Name.equals("daytona") || Name.equals("captivate")
                || Name.equals("galaxys") || Name.equals("droid2we")) {
            RECOVERY_TYPE = PARTITION_TYPE_RECOVERY;
            RECOVERY_EXT = ".zip";
        }
        readDeviceInfos();
        if (!RecoveryPath.equals("") && !isRecoveryOverRecovery())
            RECOVERY_TYPE = PARTITION_TYPE_DD;

//		Devices who kernel will be flashed to
        if (Name.equals("c6602") || Name.equals("yuga")) RECOVERY_TYPE = PARTITION_TYPE_SONY;

        if (new File("/dev/mtd/").exists()) {
            if (!isRecoveryDD()) {
                RECOVERY_TYPE = PARTITION_TYPE_MTD;
            }
            if (!isKernelDD()) {
                KERNEL_TYPE = PARTITION_TYPE_MTD;
            }
        }
        if (!RecoveryPath.equals("")) {
            if (RecoveryPath.contains("mtd")) {
                RECOVERY_TYPE = PARTITION_TYPE_MTD;
            } else {
                RECOVERY_TYPE = PARTITION_TYPE_DD;
            }
        }

        if (!KernelPath.equals("")) {
            if (KernelPath.contains("mtd")) {
                KERNEL_TYPE = PARTITION_TYPE_MTD;
            } else {
                KERNEL_TYPE = PARTITION_TYPE_DD;
            }
        }
    }

    public void loadRecoveryList() {

        ArrayList<String> CWMList = new ArrayList<String>(), TWRPList = new ArrayList<String>(),
                PHILZList = new ArrayList<String>(), StockList = new ArrayList<String>();

        try {
            String Line;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(mContext.getFilesDir(), "recovery_sums"))));
            while ((Line = br.readLine()) != null) {
                String lowLine = Line.toLowerCase();
                final int NameStartAt = Line.lastIndexOf("/") + 1;
                if (lowLine.endsWith(RECOVERY_EXT)) {
                    if (lowLine.contains(Name.toLowerCase()) || lowLine.contains(Build.DEVICE.toLowerCase())) {
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
            StockRecoveryVersions.clear();
            CwmRecoveryVersions.clear();
            TwrpRecoveryVersions.clear();
            PhilzRecoveryVersions.clear();

            /** Sort newest version to first place */
            for (Object i : StockList) {
                StockRecoveryVersions.add(0, i.toString());
            }
            for (Object i : CWMList) {
                CwmRecoveryVersions.add(0, i.toString());
            }
            for (Object i : TWRPList) {
                TwrpRecoveryVersions.add(0, i.toString());
            }
            for (Object i : PHILZList) {
                PhilzRecoveryVersions.add(0, i.toString());
            }

        } catch (Exception e) {
            ERRORS.add(e.toString());
        }
    }

    public void loadKernelList() {
        ArrayList<String> StockKernel = new ArrayList<String>();

        try {
            String Line;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(mContext.getFilesDir(), "kernel_sums"))));
            while ((Line = br.readLine()) != null) {
                String lowLine = Line.toLowerCase();
                final int NameStartAt = Line.lastIndexOf("/") + 1;
                if ((lowLine.contains(Name) || lowLine.contains(Build.DEVICE.toLowerCase()))
                        && lowLine.endsWith(KERNEL_EXT)) {
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
            StockKernelVersions.clear();

            /** Sort newest version to first place */
            for (Object i : StockKernel) {
                StockKernelVersions.add(0, i.toString());
            }

        } catch (Exception e) {
            ERRORS.add(e.toString());
        }
    }

    public boolean downloadUtils(final Context mContext) {

        final File archive = new File(Rashr.PathToUtils, Name + ".zip");

        final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);
        mAlertDialog
                .setTitle(R.string.warning)
                .setMessage(R.string.download_utils);
        if (Name.equals("montblanc") || Name.equals("c6602") || Name.equals("yuga")) {
            if (!archive.exists()) {
                mAlertDialog.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        new Downloader(mContext, "http://dslnexus.de/Android/utils/", archive.getName(), archive, new Runnable() {
                            @Override
                            public void run() {
                                Unzipper.unzip(archive, new File(Rashr.PathToUtils, Name));
                            }
                        }).execute();

                    }
                });
                mAlertDialog.show();
                return true;
            } else {
                Unzipper.unzip(archive, new File(Rashr.PathToUtils, Name));
                return false;
            }
        }
        return false;
    }

    private void readDeviceInfos() {

        for (File i : KernelList) {
            if (i.exists() && KernelPath.equals("")) {
                KernelPath = i.getAbsolutePath();
            }
        }
        for (File i : RecoveryList) {
            if (i.exists() && RecoveryPath.equals("")) {
                RecoveryPath = i.getAbsolutePath();
                if (RecoveryPath.endsWith(".tar")) {
                    RECOVERY_EXT = ".tar";
                    RECOVERY_TYPE = PARTITION_TYPE_SONY;
                }
            }
        }

        Shell mShell;
        try {
            mShell = Shell.startRootShell();
            String line;
            File LogCopy = new File(mContext.getFilesDir(), Rashr.LastLog.getName() + ".txt");
            mShell.execCommand("chmod 644 " + LogCopy.getAbsolutePath());
            mShell.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(LogCopy)));
            while ((line = br.readLine()) != null) {
                line = line.replace("\"", "");
                line = line.replace("\'", "");
                if (RecoveryVersion.equals("Not recognized Recovery-Version")) {
                    if (line.contains("ClockworkMod Recovery") || line.contains("CWM")) {
                        RecoveryVersion = line;
                    } else if (line.contains("TWRP")) {
                        line = line.replace("Starting ", "");
                        line = line.split(" on")[0];
                        RecoveryVersion = line;
                    } else if (line.contains("PhilZ")) {
                        RecoveryVersion = line;
                    } else if (line.contains("4EXT")) {
                        line = line.split("4EXT")[1];
                        RecoveryVersion = line;
                    }
                }

                if (KernelPath.equals("")) {
                    if (line.contains("/boot") && !line.contains("/bootloader")) {
                        if (line.contains("mtd")) {
                            KERNEL_TYPE = PARTITION_TYPE_MTD;
                        } else if (line.contains("/dev/")) {
                            for (String split : line.split(" ")) {
                                if (new File(split).exists()) {
                                    KernelPath = split;
                                }
                            }
                        }
                    }
                }

                if (RecoveryPath.equals("")) {
                    if (line.contains("/recovery")) {
                        if (line.contains("mtd")) {
                            RECOVERY_TYPE = PARTITION_TYPE_MTD;
                        } else if (line.contains("/dev/")) {
                            for (String split : line.split(" ")) {
                                if (new File(split).exists()) {
                                    RecoveryPath = split;
                                }
                            }
                        }
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            ERRORS.add(e.toString());
        }

        if (RecoveryPath.equals("")) {
//      ASUS DEVICEs + Same
            if (Name.equals("a66") || Name.equals("c5133") || Name.equals("c5170")
                    || Name.equals("raybst"))
                RecoveryPath = "/dev/block/mmcblk0p15";

//	    Samsung DEVICEs + Same
            if (Name.equals("d2att") || Name.equals("d2tmo") || Name.equals("d2mtr")
                    || Name.equals("d2vzw") || Name.equals("d2spr") || Name.equals("d2usc")
                    || Name.equals("d2can") || Name.equals("d2cri") || Name.equals("d2vmu")
                    || Name.equals("sch-i929") || Name.equals("e6710") || Name.equals("expresslte")
                    || Name.equals("goghcri") || Name.equals("p710") || Name.equals("im-a810s")
                    || Name.equals("hmh") || Name.equals("ef65l") || Name.equals("pantechp9070"))
                RecoveryPath = "/dev/block/mmcblk0p18";

            if (Name.equals("i9300") || Name.equals("galaxys2") || Name.equals("n8013")
                    || Name.equals("p3113") || Name.equals("p3110") || Name.equals("p6200")
                    || Name.equals("n8000") || Name.equals("sph-d710vmub") || Name.equals("p920")
                    || Name.equals("konawifi") || Name.equals("t03gctc") || Name.equals("cosmopolitan")
                    || Name.equals("s2vep") || Name.equals("gt-p6810") || Name.equals("baffin")
                    || Name.equals("ivoryss") || Name.equals("crater") || Name.equals("kyletdcmcc"))
                RecoveryPath = "/dev/block/mmcblk0p6";

            if (Name.equals("t03g") || Name.equals("tf700t") || Name.equals("t0lte")
                    || Name.equals("t0lteatt") || Name.equals("t0ltecan") || Name.equals("t0ltektt")
                    || Name.equals("t0lteskt") || Name.equals("t0ltespr") || Name.equals("t0lteusc")
                    || Name.equals("t0ltevzw") || Name.equals("t0lteatt") || Name.equals("t0ltetmo")
                    || Name.equals("m3") || Name.equals("otter2") || Name.equals("p4notelte"))
                RecoveryPath = "/dev/block/mmcblk0p9";

            if (Name.equals("golden") || Name.equals("villec2") || Name.equals("vivo")
                    || Name.equals("vivow") || Name.equals("kingdom") || Name.equals("vision")
                    || Name.equals("mystul") || Name.equals("jflteatt") || Name.equals("jfltespi")
                    || Name.equals("jfltecan") || Name.equals("jfltecri") || Name.equals("jfltexx")
                    || Name.equals("jfltespr") || Name.equals("jfltetmo") || Name.equals("jflteusc")
                    || Name.equals("jfltevzw") || Name.equals("i9500") || Name.equals("flyer")
                    || Name.equals("saga") || Name.equals("shooteru") || Name.equals("golfu")
                    || Name.equals("glacier") || Name.equals("runnymede") || Name.equals("protou")
                    || Name.equals("codinametropcs") || Name.equals("codinatmo")
                    || Name.equals("skomer") || Name.equals("magnids"))
                RecoveryPath = "/dev/block/mmcblk0p21";

            if (Name.equals("jena") || Name.equals("kylessopen") || Name.equals("kyleopen"))
                RecoveryPath = "/dev/block/mmcblk0p12";

            if (Name.equals("GT-I9103") || Name.equals("mevlana"))
                RecoveryPath = "/dev/block/mmcblk0p8";

//      LG DEVICEs + Same
            if (Name.equals("e610") || Name.equals("fx3") || Name.equals("hws7300u")
                    || Name.equals("vee3e") || Name.equals("victor") || Name.equals("ef34k")
                    || Name.equals("aviva"))
                RecoveryPath = "/dev/block/mmcblk0p17";

            if (Name.equals("vs930") || Name.equals("l0") || Name.equals("ca201l")
                    || Name.equals("ef49k") || Name.equals("ot-930") || Name.equals("fx1")
                    || Name.equals("ef47s") || Name.equals("ef46l") || Name.equals("l1v"))
                RecoveryPath = "/dev/block/mmcblk0p19";

//	    HTC DEVICEs + Same
            if (Name.equals("t6wl")) RecoveryPath = "/dev/block/mmcblk0p38";

            if (Name.equals("holiday") || Name.equals("vigor") || Name.equals("a68"))
                RecoveryPath = "/dev/block/mmcblk0p23";

            if (Name.equals("m7") || Name.equals("obakem") || Name.equals("obake")
                    || Name.equals("ovation"))
                RecoveryPath = "/dev/block/mmcblk0p34";

            if (Name.equals("m7wls")) RecoveryPath = "/dev/block/mmcblk0p36";

            if (Name.equals("endeavoru") || Name.equals("enrc2b") || Name.equals("p999")
                    || Name.equals("us9230e1") || Name.equals("evitareul") || Name.equals("otter")
                    || Name.equals("e2001_v89_gq2008s"))
                RecoveryPath = "/dev/block/mmcblk0p5";

            if (Name.equals("ace") || Name.equals("primou"))
                RecoveryPath = "/dev/block/platform/msm_sdcc.2/mmcblk0p21";

            if (Name.equals("pyramid")) RecoveryPath = "/dev/block/platform/msm_sdcc.1/mmcblk0p21";

            if (Name.equals("ville") || Name.equals("evita") || Name.equals("skyrocket")
                    || Name.equals("fireball") || Name.equals("jewel") || Name.equals("shooter"))
                RecoveryPath = "/dev/block/mmcblk0p22";

            if (Name.equals("dlxub1") || Name.equals("dlx") || Name.equals("dlxj")
                    || Name.equals("im-a840sp") || Name.equals("im-a840s") || Name.equals("taurus"))
                RecoveryPath = "/dev/block/mmcblk0p20";

//	    Motorola DEVICEs + Same
            if (Name.equals("qinara") || Name.equals("f02e") || Name.equals("vanquish_u")
                    || Name.equals("xt897") || Name.equals("solstice") || Name.equals("smq_u"))
                RecoveryPath = "/dev/block/mmcblk0p32";

            if (Name.equals("pasteur")) RecoveryPath = "/dev/block/mmcblk1p12";

            if (Name.equals("dinara_td")) RecoveryPath = "/dev/block/mmcblk1p14";

            if (Name.equals("e975") || Name.equals("e988")) RecoveryPath = "/dev/block/mmcblk0p28";

            if (Name.equals("shadow") || Name.equals("edison") || Name.equals("venus2"))
                RecoveryPath = "/dev/block/mmcblk1p16";

            if (Name.equals("spyder") || Name.equals("maserati"))
                RecoveryPath = "/dev/block/mmcblk1p15";

            if (Name.equals("olympus") || Name.equals("ja3g") || Name.equals("ja3gchnduos")
                    || Name.equals("daytona") || Name.equals("konalteatt") || Name.equals("lc1810")
                    || Name.equals("lt02wifi") || Name.equals("lt013g"))
                RecoveryPath = "/dev/block/mmcblk0p10";

//	    Sony DEVICEs + Same
            if (Name.equals("nozomi"))
                RecoveryPath = "/dev/block/mmcblk0p3";

//	    LG DEVICEs + Same
            if (Name.equals("p990") || Name.equals("tf300t"))
                RecoveryPath = "/dev/block/mmcblk0p7";

            if (Name.equals("x3") || Name.equals("picasso") || Name.equals("picasso_m")
                    || Name.equals("enterprise_ru"))
                RecoveryPath = "/dev/block/mmcblk0p1";

            if (Name.equals("m3s") || Name.equals("bryce") || Name.equals("melius3g")
                    || Name.equals("meliuslte") || Name.equals("serranolte"))
                RecoveryPath = "/dev/block/mmcblk0p14";

            if (Name.equals("p970") || Name.equals("u2") || Name.equals("p760") || Name.equals("p768"))
                RecoveryPath = "/dev/block/mmcblk0p4";

//	    ZTE DEVICEs + Same
            if (Name.equals("warp2") || Name.equals("hwc8813") || Name.equals("galaxysplus")
                    || Name.equals("cayman") || Name.equals("ancora_tmo") || Name.equals("c8812e")
                    || Name.equals("batman_skt") || Name.equals("u8833") || Name.equals("i_vzw")
                    || Name.equals("armani_row") || Name.equals("hwu8825-1") || Name.equals("ad685g")
                    || Name.equals("audi") || Name.equals("a111") || Name.equals("ancora")
                    || Name.equals("arubaslim"))
                RecoveryPath = "/dev/block/mmcblk0p13";

            if (Name.equals("elden") || Name.equals("hayes") || Name.equals("quantum")
                    || Name.equals("coeus") || Name.equals("c_4"))
                RecoveryPath = "/dev/block/mmcblk0p16";
        }

        if (!isRecoverySupported()) {
            if (RecoveryPath.contains("/dev/block")) {
                RECOVERY_TYPE = PARTITION_TYPE_DD;
            }
        }

        if (!isKernelSupported()) {
            if (KernelPath.contains("/dev/block")) {
                KERNEL_TYPE = PARTITION_TYPE_DD;
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
                    ERRORS.add(e.toString());
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
                                RecoveryPath = partition.getAbsolutePath();
                                RECOVERY_TYPE = PARTITION_TYPE_DD;
                            } else if (!isKernelSupported() && Line.contains("boot")
                                    && !Line.contains("bootloader")) {
                                KernelPath = partition.getAbsolutePath();
                                KERNEL_TYPE = PARTITION_TYPE_DD;
                            }
                        }
                    }
                } catch (IOException e) {
                    ERRORS.add(e.toString());
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
        return StockRecoveryVersions.size() > 0 && isRecoverySupported();
    }

    public boolean isTwrpRecoverySupported() {
        return TwrpRecoveryVersions.size() > 0 && isRecoverySupported();
    }

    public boolean isCwmRecoverySupported() {
        return CwmRecoveryVersions.size() > 0 && isRecoverySupported();
    }

    public boolean isPhilzRecoverySupported() {
        return PhilzRecoveryVersions.size() > 0 && isRecoverySupported();
    }

    public boolean isStockKernelSupported() {
        return StockKernelVersions.size() > 0 && isRecoverySupported();
    }

    public boolean isRecoverySupported() {
        return RECOVERY_TYPE != PARTITION_TYPE_NOT_SUPPORTED;
    }

    public int getRecoveryType() {
        return RECOVERY_TYPE;
    }

    public boolean isRecoveryMTD() {
        return RECOVERY_TYPE == PARTITION_TYPE_MTD;
    }

    public boolean isRecoveryDD() {
        return RECOVERY_TYPE == PARTITION_TYPE_DD;
    }

    public boolean isRecoveryOverRecovery() {
        return RECOVERY_TYPE == PARTITION_TYPE_RECOVERY;
    }

    public boolean isFOTAFlashed() {
        return RecoveryPath.toLowerCase().contains("fota");
    }

    public boolean isKernelSupported() {
        return KERNEL_TYPE != PARTITION_TYPE_NOT_SUPPORTED;
    }

    public boolean isKernelDD() {
        return KERNEL_TYPE == PARTITION_TYPE_DD;
    }

    public boolean isKernelMTD() {
        return KERNEL_TYPE == PARTITION_TYPE_MTD;
    }

    public int getKernelType() {
        return KERNEL_TYPE;
    }

    public String getRecoveryExt() {
        return RECOVERY_EXT;
    }

    public String getKernelExt() {
        return KERNEL_EXT;
    }

    public ArrayList<String> getStockRecoveryVersions() {
        return StockRecoveryVersions;
    }

    public ArrayList<String> getCwmRecoveryVersions() {
        return CwmRecoveryVersions;
    }

    public ArrayList<String> getTwrpRecoveryVersions() {
        return TwrpRecoveryVersions;
    }

    public ArrayList<String> getPhilzRecoveryVersions() {
        return PhilzRecoveryVersions;
    }

    public ArrayList<String> getStockKernelVersions() {
        return StockKernelVersions;
    }

    public String getRecoveryVersion() {
        return RecoveryVersion;
    }

    public String getKernelVersion() {
        return KernelVersion;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getRecoveryPath() {
        return RecoveryPath;
    }

    public String getKernelPath() {
        return KernelPath;
    }

    public ArrayList<String> getERRORS() {
        return ERRORS;
    }

    public String getManufacture() {
        return MANUFACTURE;
    }

}