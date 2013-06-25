package de.mkrtchyan.recoverytools;

/*
 * Copyright (c) 2013 Ashot Mkrtchyan
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

import android.os.Build;
import android.os.Environment;

import org.rootcommands.util.RootAccessDeniedException;

import java.io.File;

import de.mkrtchyan.utils.Common;


public class Support {
    /*
     * This class content all device specified informations to provide
     * all informations for all other classes for example:
     * What kind of partition and where is the recovery partition in the
     * FileSystem
     */
    private final Common mCommon = new Common();
    public String DEVICE = Build.DEVICE;
    public String RecoveryPath;
    private String SYSTEM;
    private String VERSION = "";
    public String EXT = ".img";
    public String HOST_URL = "http://dslnexus.nazuka.net/recoveries";
    public boolean KERNEL_TO = false;
    public boolean FLASH_OVER_RECOVERY = false;
    public boolean MTD = false;
    public boolean TWRP = true;
    //	public boolean TWRP_INSTALLED = false;
    private boolean TWRP_OFFICIAL = true;
    public boolean CWM = true;
    //	public boolean CWM_INSTALLED = false;
    private boolean CWM_OFFICIAL = true;
    public boolean OTHER = true;


    public Support() {

        String BOARD = Build.BOARD;
        String MODEL = Build.MODEL;

//	Set DEVICE predefined options

//		Kindle Fire HD 7"
        if (DEVICE.equals("D01E"))
            DEVICE = "kfhd7";

//      HTC ONE GSM

        if (BOARD.equals("m7")
                || DEVICE.equals("m7")
                || DEVICE.equals("m7ul"))
            DEVICE = "m7";

        if (DEVICE.equals("m7spr"))
            DEVICE = "m7wls";

//      Samsung Galaxy S4 (i9505/jflte)
        if (DEVICE.equals("jflte"))
            DEVICE = "jfltexx";

//		Galaxy Note 
        if (DEVICE.equals("GT-N7000")
                || DEVICE.equals("n7000")
                || DEVICE.equals("galaxynote")
                || DEVICE.equals("N7000")
                || BOARD.equals("GT-N7000")
                || BOARD.equals("n7000")
                || BOARD.equals("galaxynote")
                || BOARD.equals("N7000")) {
            DEVICE = "n7000";
            FLASH_OVER_RECOVERY = true;
        }

//		Galaxy Note 2
        if (DEVICE.equals("t03g")
                || DEVICE.equals("n7100")
                || DEVICE.equals("GT-N7100")
                || BOARD.equals("t03g")
                || BOARD.equals("n7100")
                || BOARD.equals("GT-N7100"))
            DEVICE = "n7100";

//		Galaxy Note 2 LTE
        if (DEVICE.equals("t0ltexx")
                || DEVICE.equals("GT-N7105")
                || DEVICE.equals("t0ltedv")
                || DEVICE.equals("GT-N7105T")
                || DEVICE.equals("t0lteatt")
                || DEVICE.equals("SGH-I317")
                || DEVICE.equals("t0ltetmo")
                || DEVICE.equals("SGH-T889")
                || DEVICE.equals("t0ltecan")
                || DEVICE.equals("t0ltevl")
                || DEVICE.equals("SGH-I317M")
                || BOARD.equals("t0ltexx")
                || BOARD.equals("GT-N7105")
                || BOARD.equals("t0ltedv")
                || BOARD.equals("GT-N7105T")
                || BOARD.equals("t0lteatt")
                || BOARD.equals("SGH-I317")
                || BOARD.equals("t0ltetmo")
                || BOARD.equals("SGH-T889")
                || BOARD.equals("t0ltecan")
                || BOARD.equals("t0ltevl")
                || BOARD.equals("SGH-I317M"))
            DEVICE = "t0lte";

//		Galaxy S3 (international)
        if (DEVICE.equals("GT-I9300")
                || DEVICE.equals("Galaxy S3")
                || DEVICE.equals("GalaxyS3")
                || DEVICE.equals("m0")
                || DEVICE.equals("i9300")
                || BOARD.equals("GT-I9300")
                || BOARD.equals("m0")
                || BOARD.equals("i9300"))
            DEVICE = "i9300";

//		Galaxy S2
        if (DEVICE.equals("GT-I9100G")
                || DEVICE.equals("GT-I9100M")
                || DEVICE.equals("GT-I9100P")
                || DEVICE.equals("GT-I9100")
                || DEVICE.equals("galaxys2")
                || BOARD.equals("GT-I9100G")
                || BOARD.equals("GT-I9100M")
                || BOARD.equals("GT-I9100P")
                || BOARD.equals("GT-I9100")
                || BOARD.equals("galaxys2")) {
            FLASH_OVER_RECOVERY = true;
            DEVICE = "galaxys2";
        }

//		Galaxy S2 ATT
        if (DEVICE.equals("SGH-I777")
                || DEVICE.equals("galaxys2att")
                || BOARD.equals("SGH-777")
                || BOARD.equals("galaxys2att")) {
            DEVICE = "galaxys2att";
            FLASH_OVER_RECOVERY = true;
        }

//		Galaxy S2 LTE (skyrocket)
        if (DEVICE.equals("SGH-I727")
                || BOARD.equals("skyrocket")
                || BOARD.equals("SGH-I727"))
            DEVICE = "skyrocket";

//      Galaxy S (i9000)
        if (DEVICE.equals("galaxys")
                || DEVICE.equals("galaxysmtd")
                || DEVICE.equals("GT-I9000")
                || DEVICE.equals("GT-I9000M")
                || DEVICE.equals("GT-I9000T")
                || BOARD.equals("galaxys")
                || BOARD.equals("galaxysmtd")
                || BOARD.equals("GT-I9000")
                || BOARD.equals("GT-I9000M")
                || BOARD.equals("GT-I9000T")) {
            DEVICE = "galaxys";
            EXT = ".zip";
            FLASH_OVER_RECOVERY = true;
        }

//		GalaxyS Captivate (SGH-I897)
        if (DEVICE.equals("SGH-I897")
                || DEVICE.equals("captivate")) {
            DEVICE = ("captivate");
            FLASH_OVER_RECOVERY = true;
        }

        if (BOARD.equals("gee")) {
            DEVICE = "geeb";
        }

//		Sony Xperia Z (C6603)

        if (DEVICE.equals("C6603")
                || DEVICE.equals("yuga")) {
            DEVICE = "C6603";
            EXT = ".tar";
        }

//		Sony Xperia S

        if (DEVICE.equals("LT26i"))
            DEVICE = "nozomi";

//		Sony Xperia T

        if (DEVICE.equals("LT30p"))
            DEVICE = "mint";

//      HTC Desire HD
        if (BOARD.equals("ace"))
            DEVICE = "ace";

//      LG Optimus L9
        if (DEVICE.equals("u2")
                || BOARD.equals("u2")
                || MODEL.equals("LG-P760"))
            DEVICE = "p760";
//      Huawei U9508
        if (BOARD.equals("U9508")
            || DEVICE.equals("hwu9508"))
            DEVICE = "u9508";


//		MTD Devices
        if (DEVICE.equals("crespo")
                || DEVICE.equals("crespo4g")
                || DEVICE.equals("passion")
                || DEVICE.equals("saga")
                || DEVICE.equals("swift")
                || DEVICE.equals("thunderc")
                || DEVICE.equals("supersonic")
                || DEVICE.equals("thunderg")
                || DEVICE.equals("hero")
                || DEVICE.equals("heroc")
                || DEVICE.equals("pecan")
                || DEVICE.equals("leo")
                || DEVICE.equals("p760"))
            MTD = true;

//		Devices who kernel will be flashed to
        if (DEVICE.equals("nozomi")
                || DEVICE.equals("mint"))
            KERNEL_TO = true;

        if (FLASH_OVER_RECOVERY)
            EXT = ".zip";

        DEVICE = DEVICE.toLowerCase();

        RecoveryPath = getRecoveryPath();
        getUnsupportedSystems();
    }

    private String getRecoveryPath() {

        String tmp = "";

//		Nexus DEVICEs + Same

        if (DEVICE.equals("maguro")
                || DEVICE.equals("toro")
                || DEVICE.equals("toroplus"))
            tmp = "/dev/block/platform/omap/omap_hsmmc.0/by-name/recovery";

        if (DEVICE.equals("grouper")
                || DEVICE.equals("tilapia"))
            tmp = "/dev/block/platform/sdhci-tegra.3/by-name/SOS";

        if (DEVICE.equals("mako")
                || DEVICE.equals("geeb"))
            tmp = "/dev/block/platform/msm_sdcc.1/by-name/recovery";

        if (DEVICE.equals("manta"))
            tmp = "/dev/block/platform/dw_mmc.0/by-name/recovery";

//		Samsung DEVICEs + Same

        if (DEVICE.equals("d2att")
                || DEVICE.equals("d2tmo")
                || DEVICE.equals("d2vzw")
                || DEVICE.equals("d2spr")
                || DEVICE.equals("SCH-i929"))
            tmp = "/dev/block/mmcblk0p18";

        if (DEVICE.equals("i9300")
                || DEVICE.equals("galaxys2"))
            tmp = "/dev/block/mmcblk0p6";

        if (DEVICE.equals("n7100")
                || DEVICE.equals("tf700t")
                || DEVICE.equals("t0lte")
                || DEVICE.equals("t0ltevzw")
                || DEVICE.equals("tf201"))
//				|| DEVICE.equals("m3"))
            tmp = "/dev/block/mmcblk0p9";

        if (DEVICE.equals("golden")
                || DEVICE.equals("villec2")
                || DEVICE.equals("vivow")
                || DEVICE.equals("kingdom")
                || DEVICE.equals("vision")
                || DEVICE.equals("mystul")
                || DEVICE.equals("jfltespr")
                || DEVICE.equals("jfltevzw")
                || DEVICE.equals("jfltexx"))
            tmp = "/dev/block/mmcblk0p21";

        if (DEVICE.equals("jena"))
            tmp = "/dev/block/mmcblk0p12";

        if (DEVICE.equals("GT-I9103"))
            tmp = "/dev/block/mmcblk0p8";

//		HTC DEVICEs + Same

        if (DEVICE.equals("m7"))
            tmp = "/dev/block/mmcblk0p34";

        if (DEVICE.equals("m7wls"))
            tmp = "/dev/block/mmcblk0p36";

        if (DEVICE.equals("endeavoru"))
            tmp = "/dev/block/mmcblk0p5";

        if (DEVICE.equals("ace")
                || DEVICE.equals("primou"))
            tmp = "/dev/block/platform/msm_sdcc.2/mmcblk0p21";

        if (DEVICE.equals("pyramid"))
            tmp = "/dev/block/platform/msm_sdcc.1/mmcblk0p21";

        if (DEVICE.equals("ville")
                || DEVICE.equals("evita")
                || DEVICE.equals("skyrocket")
                || DEVICE.equals("fireball")
                || DEVICE.equals("jewel")
                || DEVICE.equals("shooter"))
            tmp = "/dev/block/mmcblk0p22";

        if (DEVICE.equals("dlxub1")
                || DEVICE.equals("dlx"))
            tmp = "/dev/block/mmcblk0p20";

//		Motorola DEVICEs + Same

        if (DEVICE.equals("droid2")) {
            tmp = "/dev/block/mmcblk1p16";
            FLASH_OVER_RECOVERY = true;
        }

        if (DEVICE.equals("olympus")
                || DEVICE.equals("ja3g"))
            tmp = "/dev/block/mmcblk0p10";

//      Huawei

        if (DEVICE.equals("u9508"))
            tmp = "/dev/block/platform/hi_mci.1/by-name/recovery";

//		Sony DEVICEs + Same

        if (DEVICE.equals("nozomi")
                || DEVICE.equals("LT26i")
                || DEVICE.equals("mint")
                || DEVICE.equals("LT30p"))
            tmp = "/dev/block/mmcblk0p11";

        if (DEVICE.equals("c6603"))
            tmp = "/system/bin/recovery.tar";

//		LG DEVICEs + Same

        if (DEVICE.equals("p990"))
            tmp = "/dev/block/mmcblk0p7";

        if (DEVICE.equals("x3"))
            tmp = "/dev/block/mmcblk0p1";

        if (DEVICE.equals("m3s")
                || DEVICE.equals("bryce"))
            tmp = "/dev/block/mmcblk0p14";

        if (DEVICE.equals("p970"))
            tmp = "/dev/block/mmcblk0p4";

//		ZTE DEVICEs + Same

        if (DEVICE.equals("warp2"))
            tmp = "/dev/block/mmcblk0p13";

        return tmp;
    }

    public void getUnsupportedSystems() {

        if (DEVICE.equals("galaxys2")
                || DEVICE.equals("SGH-I777")
                || DEVICE.equals("n7000")
                || DEVICE.equals("x3")
                || DEVICE.equals("droid2")
                || DEVICE.equals("kingdom")
                || DEVICE.equals("SGH-I897")
                || DEVICE.equals("thunderc")
                || DEVICE.equals("SCH-i929")
                || DEVICE.equals("m3s")
                || DEVICE.equals("SPH-D710")
                || DEVICE.equals("GT-P6200")
                || DEVICE.equals("galaxys")
                || DEVICE.equals("bryce")
                || DEVICE.equals("vision")
                || DEVICE.equals("hero")
                || DEVICE.equals("pecan")
                || DEVICE.equals("mystul")
                || DEVICE.equals("u9508"))
            TWRP = false;

        if (DEVICE.equals("nozomi")
                || DEVICE.equals("mint")
                || DEVICE.equals("LT30p")
                || DEVICE.equals("kfhd7")
                || DEVICE.equals("LT26i")
                || DEVICE.equals("thunderc")
                || DEVICE.equals("SCH-i929")
                || DEVICE.equals("m3s")
                || DEVICE.equals("SPH-D710")
                || DEVICE.equals("GT-P6200")
                || DEVICE.equals("bryce")
                || DEVICE.equals("pecan")
                || DEVICE.equals("mystul")
                || DEVICE.equals("u9508"))
            CWM = false;

        if (DEVICE.equals("")
                || !MTD
                && RecoveryPath.equals("")) {
            TWRP = false;
            CWM = false;
            OTHER = false;
        }

    }

    public void getVersion(String SYSTEM) {
        this.SYSTEM = SYSTEM;
        if (SYSTEM.equals("clockwork")) {

//		CLOCKWORKMOD touch supported devices
            if (DEVICE.equals("ace")
                    || DEVICE.equals("crespo")
                    || DEVICE.equals("crespo4g")
                    || DEVICE.equals("d2att")
                    || DEVICE.equals("d2tmo")
                    || DEVICE.equals("endeavoru")
                    || DEVICE.equals("evita")
                    || DEVICE.equals("fireball")
                    || DEVICE.equals("galaxys2")
                    || DEVICE.equals("golden")
                    || DEVICE.equals("grouper")
                    || DEVICE.equals("i9300")
                    || DEVICE.equals("maguro")
                    || DEVICE.equals("mako")
                    || DEVICE.equals("manta")
                    || DEVICE.equals("n7100")
                    || DEVICE.equals("pyramid")
                    || DEVICE.equals("saga")
                    || DEVICE.equals("skyrocket")
                    || DEVICE.equals("t0lte")
                    || DEVICE.equals("tilapia")
                    || DEVICE.equals("toro")
                    || DEVICE.equals("toroplus")
                    || DEVICE.equals("ville")
                    || DEVICE.equals("warp2")
                    || DEVICE.equals("p990")
                    || DEVICE.equals("tf700t")
                    || DEVICE.equals("m7")
                    || DEVICE.equals("dlx")
                    || DEVICE.equals("jflte")
                    || DEVICE.equals("d2spr")
                    || DEVICE.equals("supersonic")
                    || DEVICE.equals("olympus")
                    || DEVICE.equals("m7spr")
                    || DEVICE.equals("p990")
                    || DEVICE.equals("jfltespr")
                    || DEVICE.equals("jewel")
                    || DEVICE.equals("shooter")
                    || DEVICE.equals("jfltevzw")
                    || DEVICE.equals("p970")
                    || DEVICE.equals("p760"))
                VERSION = "-touch";

//			Newest Clockworkmod version for devices

            if (DEVICE.equals("heroc"))
                VERSION = VERSION + "-2.5.0.7";

            if (DEVICE.equals("SGH-I897")
                    || DEVICE.equals("galaxys")
                    || DEVICE.equals("captivate"))
                VERSION = VERSION + "-2.5.1.2";

            if (DEVICE.equals("leo"))
                VERSION = VERSION + "-3.1.0.0";

            if (DEVICE.equals("droid2")
                    || DEVICE.equals("vivow"))
                VERSION = VERSION + "-5.0.2.0";

            if (DEVICE.equals("thunderg"))
                VERSION = VERSION + "-5.0.2.7";

            if (DEVICE.equals("supersonic"))
                VERSION = VERSION + "-5.8.0.1";

            if (DEVICE.equals("shooter"))
                VERSION = VERSION + "-5.8.0.2";
            if (DEVICE.equals("pyramid"))
                VERSION = VERSION + "-5.8.0.9";

            if (DEVICE.equals("ace")
                    || DEVICE.equals("saga")
                    || DEVICE.equals("galaxys2")
                    || DEVICE.equals("olympus"))
            VERSION = VERSION + "-5.8.1.5";

            if (DEVICE.equals("tf201"))
                VERSION = VERSION + "-5.8.3.4";

            if (DEVICE.equals("jewel"))
                VERSION = VERSION + "-5.8.3.5";

            if (DEVICE.equals("endeavoru"))
                VERSION = VERSION + "-5.8.4.0";

            if (DEVICE.equals("primou"))
                VERSION = VERSION + "-5.8.4.5";

            if (DEVICE.equals("n7000"))
                VERSION = VERSION + "-6.0.1.2";

            if (DEVICE.equals("p970"))
                VERSION = VERSION + "-6.0.1.4";

            if (DEVICE.equals("golden")
                    || DEVICE.equals("warp2"))
                VERSION = VERSION + "-6.0.2.7";

            if (DEVICE.equals("crespo")
                    || DEVICE.equals("crespo4g")
                    || DEVICE.equals("d2att")
                    || DEVICE.equals("d2tmo")
                    || DEVICE.equals("dlxub1")
                    || DEVICE.equals("evita")
                    || DEVICE.equals("fireball")
                    || DEVICE.equals("grouper")
                    || DEVICE.equals("i9300")
                    || DEVICE.equals("maguro")
                    || DEVICE.equals("mako")
                    || DEVICE.equals("manta")
                    || DEVICE.equals("n7100")
                    || DEVICE.equals("skyrocket")
                    || DEVICE.equals("t0lte")
                    || DEVICE.equals("tilapia")
                    || DEVICE.equals("toro")
                    || DEVICE.equals("toroplus")
                    || DEVICE.equals("ville")
                    || DEVICE.equals("p990")
                    || DEVICE.equals("tf700t")
                    || DEVICE.equals("m7")
                    || DEVICE.equals("dlx")
                    || DEVICE.equals("d2spr")
                    || DEVICE.equals("p990")
                    || DEVICE.equals("p760"))
                VERSION = VERSION + "-6.0.3.1";

            if (DEVICE.equals("jfltexx")
                    || DEVICE.equals("jfltespr")
                    || DEVICE.equals("m7wls")
                    || DEVICE.equals("jfltevzw"))
                VERSION = "-6.0.3.2";

            if (VERSION.equals(""))
                CWM_OFFICIAL = false;

            HOST_URL = "http://dslnexus.nazuka.net/recoveries";
        }

        if (SYSTEM.equals("twrp")) {

            if (DEVICE.equals("thunderg"))
                VERSION = "-2.0.0alpha1";

            if (DEVICE.equals("leo"))
                VERSION = "-2.1.1";

            if (DEVICE.equals("ace")
                    || DEVICE.equals("crespo")
                    || DEVICE.equals("crespo4g")
                    || DEVICE.equals("d2att")
                    || DEVICE.equals("d2tmo")
                    || DEVICE.equals("d2vzw")
                    || DEVICE.equals("endeavoru")
                    || DEVICE.equals("evita")
                    || DEVICE.equals("fireball")
                    || DEVICE.equals("geeb")
                    || DEVICE.equals("golden")
                    || DEVICE.equals("grouper")
                    || DEVICE.equals("i9300")
                    || DEVICE.equals("maguro")
                    || DEVICE.equals("mako")
                    || DEVICE.equals("manta")
                    || DEVICE.equals("mint")
                    || DEVICE.equals("nozomi")
                    || DEVICE.equals("passion")
                    || DEVICE.equals("primou")
                    || DEVICE.equals("pyramid")
                    || DEVICE.equals("skyrocket")
                    || DEVICE.equals("t0lte")
                    || DEVICE.equals("t0lteatt")
                    || DEVICE.equals("t0ltetmo")
                    || DEVICE.equals("t0ltevzw")
                    || DEVICE.equals("tilapia")
                    || DEVICE.equals("toro")
                    || DEVICE.equals("toroplus")
                    || DEVICE.equals("ville")
                    || DEVICE.equals("villec2")
                    || DEVICE.equals("p990")
                    || DEVICE.equals("tf700t")
                    || DEVICE.equals("m7")
                    || DEVICE.equals("dlx")
                    || DEVICE.equals("d2spr")
                    || DEVICE.equals("supersonic")
                    || DEVICE.equals("vivow")
                    || DEVICE.equals("olympus")
                    || DEVICE.equals("m7wls")
                    || DEVICE.equals("p990")
                    || DEVICE.equals("heroc")
                    || DEVICE.equals("jewel")
                    || DEVICE.equals("shooter")
                    || DEVICE.equals("p970"))
                VERSION = "-2.5.0.0";

            if (DEVICE.equals("dlxub1")
                    || DEVICE.equals("n7100")
                    || DEVICE.equals("saga"))
                VERSION = "-2.5.0.1";

            if (DEVICE.equals("jfltexx")
                    || DEVICE.equals("jfltespr")
                    || DEVICE.equals("jfltevzw"))
                VERSION = "-2.5.0.2";

            if (DEVICE.equals("tf201")){
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH
                        || Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    VERSION = VERSION + "-ICS";
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH
                        || Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    VERSION = VERSION + "-JB";
                }
            }

            if (VERSION.equals(""))
                TWRP_OFFICIAL = false;

            if (TWRP_OFFICIAL
                    && !DEVICE.equals("tf700t")) {
                HOST_URL = "http://jrummy16.com/jrummy/goodies/recoveries/twrp/" + DEVICE;
                //HOST_URL = "http://dslnexus.nazuka.net/recoveries";
            } else {
                HOST_URL = "http://dslnexus.nazuka.net/recoveries";
            }
//			HOST_URL = "http://techerrata.com/get/twrp2/" + DEVICE;
        }
    }

    public File constructFile() {

        File file = null;
        if (SYSTEM.equals("clockwork"))
            if (CWM_OFFICIAL) {
                file = new File(RecoveryTools.PathToRecoveries, "recovery-" + SYSTEM + VERSION + "-" + DEVICE + EXT);
            } else {
                file = new File(RecoveryTools.PathToRecoveries, DEVICE + "-cwm" + EXT);
            }

        if (SYSTEM.equals("twrp"))
            if (TWRP_OFFICIAL) {
                file = new File(RecoveryTools.PathToRecoveries, "openrecovery-" + SYSTEM + VERSION + "-" + DEVICE + EXT);
            } else {
                file = new File(RecoveryTools.PathToRecoveries, DEVICE + "-twrp" + EXT);
            }

        return file;
    }

    public void installZip(File ZipFile) {
        try {
            if (!ZipFile.getPath().endsWith(Environment.getExternalStorageDirectory().getAbsolutePath()))
                mCommon.executeSuShell("cat " + ZipFile.getAbsolutePath() + " >> " + new File(Environment.getExternalStorageDirectory(), ZipFile.getName()).getAbsolutePath());
            File script = new File("/cache/recovery", "openrecoveryscript");
            mCommon.executeSuShell("echo install /sdcard/" + ZipFile.getName() + " >> " + script.getAbsolutePath());
            mCommon.executeSuShell("reboot recovery");
        } catch (RootAccessDeniedException e) {
            e.printStackTrace();
        }
    }
}
