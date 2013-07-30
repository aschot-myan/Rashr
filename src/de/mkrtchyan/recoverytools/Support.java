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
     * This class content all device specified information to provide
     * all information for all other classes for example:
     * What kind of partition and where is the recovery partition in the
     * FileSystem
     */
    private final Common mCommon = new Common();
    public String DEVICE = Build.DEVICE;
    public String RecoveryPath;

    public File CWM_IMG;
    public File TWRP_IMG;

    private String CWM_VERSION = "";
    private String TWRP_VERSION = "";
    public String EXT = ".img";
    final public String CWM_URL = "http://dslnexus.nazuka.net/recoveries";
    final public String TWRP_URL = "http://dslnexus.nazuka.net/recoveries";
    public boolean KERNEL_TO = false;
    public boolean FLASH_OVER_RECOVERY = false;
    public boolean MTD = false;
    public boolean TWRP = true;
    private boolean TWRP_OFFICIAL = true;
    public boolean CWM = true;
    private boolean CWM_OFFICIAL = true;
    public boolean OTHER = true;


    public Support() {

        String BOARD = Build.BOARD;
        String MODEL = Build.MODEL;

//	Set DEVICE predefined options
//      Samsung Galaxy S +
        if (DEVICE.equals("GT-I9001")
                || BOARD.equals("GT-I9001")
                || MODEL.equals("GT-I9001"))
            DEVICE = "galaxysplus";

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

//      Samsung Galaxy Note 10.1
        if (MODEL.equals("GT-N8013")
                || DEVICE.equals("p4notewifi"))
            DEVICE = "n8013";

//      Samsung Galaxy Tab 2
        if (BOARD.equals("piranha")
                || MODEL.equals("GT-P3113"))
            DEVICE = "p3113";

        if (DEVICE.equals("espressowifi")
                || MODEL.equals("GT-P3110"))
            DEVICE = "p3110";

//		Galaxy Note 2
        if (DEVICE.equals("n7100")
                || DEVICE.equals("n7100")
                || DEVICE.equals("GT-N7100")
                || BOARD.equals("t03g")
                || BOARD.equals("n7100")
                || BOARD.equals("GT-N7100"))
            DEVICE = "t03g";

//		Galaxy Note 2 LTE
        if (DEVICE.equals("t0ltexx")
                || DEVICE.equals("GT-N7105")
                || DEVICE.equals("t0ltedv")
                || DEVICE.equals("GT-N7105T")
                || DEVICE.equals("t0ltevl")
                || DEVICE.equals("SGH-I317M")
                || BOARD.equals("t0ltexx")
                || BOARD.equals("GT-N7105")
                || BOARD.equals("t0ltedv")
                || BOARD.equals("GT-N7105T")
                || BOARD.equals("t0ltevl")
                || BOARD.equals("SGH-I317M"))
            DEVICE = "t0lte";

        if (DEVICE.equals("SGH-I317")
                || BOARD.equals("t0lteatt")
                || BOARD.equals("SGH-I317"))
            DEVICE = "t0lteatt";

        if (DEVICE.equals("SGH-T889")
                || BOARD.equals("t0ltetmo")
                || BOARD.equals("SGH-T889"))
            DEVICE = "t0ltetmo";

        if (BOARD.equals("t0ltecan"))
            DEVICE = "t0ltecan";

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
                || BOARD.equals("galaxys2"))
            DEVICE = "galaxys2";

//		Galaxy S2 ATT
        if (DEVICE.equals("SGH-I777")
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
                || BOARD.equals("GT-I9000T")
                || MODEL.equals("GT-I9000T")
                || DEVICE.equals("SPH-D710BST")
                || MODEL.equals("SPH-D710BST")) {
            DEVICE = "galaxys";
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
                || DEVICE.equals("p760")
                || DEVICE.equals("bravo")
                || DEVICE.equals("sholes")
                || DEVICE.equals("blade"))
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
        getVersion();
        constructFile();
    }

    private String getRecoveryPath() {

        String tmp = "";

//		Nexus DEVICEs + Same
        if (DEVICE.equals("maguro")
                || DEVICE.equals("toro")
                || DEVICE.equals("toroplus"))
            tmp = "/dev/block/platform/omap/omap_hsmmc.0/by-name/recovery";

        if (DEVICE.equals("grouper")
                || DEVICE.equals("tilapia")
                || DEVICE.equals("p880"))
            tmp = "/dev/block/platform/sdhci-tegra.3/by-name/SOS";

        if (DEVICE.equals("mako")
                || DEVICE.equals("geeb")
                || DEVICE.equals("vanquish")
                || DEVICE.equals("find5"))
            tmp = "/dev/block/platform/msm_sdcc.1/by-name/recovery";

        if (DEVICE.equals("manta"))
            tmp = "/dev/block/platform/dw_mmc.0/by-name/recovery";

//		Samsung DEVICEs + Same
        if (DEVICE.equals("d2att")
                || DEVICE.equals("d2tmo")
                || DEVICE.equals("d2vzw")
                || DEVICE.equals("d2spr")
                || DEVICE.equals("d2usc")
                || DEVICE.equals("SCH-i929"))
            tmp = "/dev/block/mmcblk0p18";

        if (DEVICE.equals("i9300")
                || DEVICE.equals("galaxys2")
                || DEVICE.equals("n8013")
                || DEVICE.equals("p3113")
                || DEVICE.equals("p3110"))
            tmp = "/dev/block/mmcblk0p6";

        if (DEVICE.equals("t03g")
                || DEVICE.equals("tf700t")
                || DEVICE.equals("t0lte")
                || DEVICE.equals("t0ltevzw")
                || DEVICE.equals("tf201")
                || DEVICE.equals("t0ltespr"))
            tmp = "/dev/block/mmcblk0p9";

        if (DEVICE.equals("golden")
                || DEVICE.equals("villec2")
                || DEVICE.equals("vivow")
                || DEVICE.equals("kingdom")
                || DEVICE.equals("vision")
                || DEVICE.equals("mystul")
                || DEVICE.equals("jfltespr")
                || DEVICE.equals("jflteatt")
                || DEVICE.equals("jfltevzw")
                || DEVICE.equals("jfltexx")
                || DEVICE.equals("jfltecan")
                || DEVICE.equals("jfltetmo")
                || DEVICE.equals("jflteusc")
                || DEVICE.equals("flyer"))
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

        if (DEVICE.equals("endeavoru")
                || DEVICE.equals("enrc2b"))
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
            FLASH_OVER_RECOVERY = true;
        }

        if (DEVICE.equals("olympus")
                || DEVICE.equals("ja3g")
                || DEVICE.equals("daytona"))
            tmp = "/dev/block/mmcblk0p10";

        if (DEVICE.equals("daytona"))
            FLASH_OVER_RECOVERY = true;

//      Huawei DEVICEs + Same
        if (DEVICE.equals("u9508"))
            tmp = "/dev/block/platform/hi_mci.1/by-name/recovery";

//		Sony DEVICEs + Same
        if (DEVICE.equals("nozomi"))
            tmp = "/dev/block/mmcblk0p3";

//		LG DEVICEs + Same
        if (DEVICE.equals("p990")
                || DEVICE.equals("tf300t"))
            tmp = "/dev/block/mmcblk0p7";

        if (DEVICE.equals("x3"))
            tmp = "/dev/block/mmcblk0p1";

        if (DEVICE.equals("m3s")
                || DEVICE.equals("bryce"))
            tmp = "/dev/block/mmcblk0p14";

        if (DEVICE.equals("p970")
                || DEVICE.equals("mint"))
            tmp = "/dev/block/mmcblk0p4";

//		ZTE DEVICEs + Same
        if (DEVICE.equals("warp2")
                || DEVICE.equals("hwc8813")
                || DEVICE.equals("galaxysplus"))
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
                || DEVICE.equals("u9508")
                || DEVICE.equals("p880")
                || DEVICE.equals("p3113")
                || DEVICE.equals("p3110")
                || DEVICE.equals("flyer")
                || DEVICE.equals("daytona")
                || DEVICE.equals("vanquish")
                || DEVICE.equals("bravo")
                || DEVICE.equals("hwc8813")
                || DEVICE.equals("galaxysplus")
                || DEVICE.equals("tf300t")
                || DEVICE.equals("blade"))
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
                || DEVICE.equals("u9508")
                || DEVICE.equals("enrc2b")
                || DEVICE.equals("flyer")
                || DEVICE.equals("vanquish")
                || DEVICE.equals("t0ltespr")
                || DEVICE.equals("hwc8813")
                || DEVICE.equals("galaxysplus")
                || DEVICE.equals("tf300t"))
            CWM = false;

        if (DEVICE.equals("")
                || !MTD && RecoveryPath.equals("") && !FLASH_OVER_RECOVERY) {
            TWRP = false;
            CWM = false;
            OTHER = false;
        }
    }

    public void getVersion() {
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
                || DEVICE.equals("t03g")
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
                || DEVICE.equals("dlx")
                || DEVICE.equals("jflte")
                || DEVICE.equals("d2spr")
                || DEVICE.equals("supersonic")
                || DEVICE.equals("olympus")
                || DEVICE.equals("m7spr")
                || DEVICE.equals("jfltespr")
                || DEVICE.equals("jewel")
                || DEVICE.equals("shooter")
                || DEVICE.equals("jfltevzw")
                || DEVICE.equals("p970")
                || DEVICE.equals("p760")
                || DEVICE.equals("jfltecan")
                || DEVICE.equals("jfltexx")
                || DEVICE.equals("jfltespr")
                || DEVICE.equals("m7")
                || DEVICE.equals("m7wls")
                || DEVICE.equals("jfltevzw")
                || DEVICE.equals("p880")
                || DEVICE.equals("n8013")
                || DEVICE.equals("jfltetmo")
                || DEVICE.equals("p3113")
                || DEVICE.equals("d2usc")
                || DEVICE.equals("bravo")
                || DEVICE.equals("find5")
                || DEVICE.equals("jflteatt")
                || DEVICE.equals("jflteusc")
                || DEVICE.equals("p3110"))
            CWM_VERSION = "-touch";

//	    Newest Clockworkmod version for devices
        if (DEVICE.equals("sholes"))
            CWM_VERSION = CWM_VERSION + "-2.5.0.1";

        if (DEVICE.equals("heroc"))
            CWM_VERSION = CWM_VERSION + "-2.5.0.7";

        if (DEVICE.equals("SGH-I897")
                || DEVICE.equals("galaxys")
                || DEVICE.equals("captivate"))
            CWM_VERSION = CWM_VERSION + "-2.5.1.2";

        if (DEVICE.equals("leo"))
            CWM_VERSION = CWM_VERSION + "-3.1.0.0";

        if (DEVICE.equals("droid2")
                || DEVICE.equals("vivow")
                || DEVICE.equals("blade"))
            CWM_VERSION = CWM_VERSION + "-5.0.2.0";

        if (DEVICE.equals("daytona"))
            CWM_VERSION = CWM_VERSION + "-5.0.2.5";

        if (DEVICE.equals("thunderg"))
            CWM_VERSION = CWM_VERSION + "-5.0.2.7";

        if (DEVICE.equals("supersonic"))
            CWM_VERSION = CWM_VERSION + "-5.8.0.1";

        if (DEVICE.equals("shooter")
                || DEVICE.equals("bravo"))
            CWM_VERSION = CWM_VERSION + "-5.8.0.2";
        if (DEVICE.equals("pyramid"))
            CWM_VERSION = CWM_VERSION + "-5.8.0.9";

        if (DEVICE.equals("ace")
                || DEVICE.equals("saga")
                || DEVICE.equals("galaxys2")
                || DEVICE.equals("olympus"))
            CWM_VERSION = CWM_VERSION + "-5.8.1.5";

        if (DEVICE.equals("tf201"))
            CWM_VERSION = CWM_VERSION + "-5.8.3.4";

        if (DEVICE.equals("jewel"))
            CWM_VERSION = CWM_VERSION + "-5.8.3.5";

        if (DEVICE.equals("endeavoru"))
            CWM_VERSION = CWM_VERSION + "-5.8.4.0";

        if (DEVICE.equals("primou"))
            CWM_VERSION = CWM_VERSION + "-5.8.4.5";

        if (DEVICE.equals("n7000"))
            CWM_VERSION = CWM_VERSION + "-6.0.1.2";

        if (DEVICE.equals("p970"))
            CWM_VERSION = CWM_VERSION + "-6.0.1.4";

        if (DEVICE.equals("p3113"))
            CWM_VERSION = CWM_VERSION + "-6.0.2.3";

        if (DEVICE.equals("golden")
                || DEVICE.equals("warp2")
                || DEVICE.equals("p3110"))
            CWM_VERSION = CWM_VERSION + "-6.0.2.7";

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
                || DEVICE.equals("t03g")
                || DEVICE.equals("skyrocket")
                || DEVICE.equals("tilapia")
                || DEVICE.equals("toro")
                || DEVICE.equals("toroplus")
                || DEVICE.equals("ville")
                || DEVICE.equals("p990")
                || DEVICE.equals("tf700t")
                || DEVICE.equals("m7")
                || DEVICE.equals("dlx")
                || DEVICE.equals("d2spr")
                || DEVICE.equals("p760")
                || DEVICE.equals("p880")
                || DEVICE.equals("n8013")
                || DEVICE.equals("d2usc")
                || DEVICE.equals("find5"))
            CWM_VERSION = CWM_VERSION + "-6.0.3.1";

        if (DEVICE.equals("jfltexx")
                || DEVICE.equals("jfltespr")
                || DEVICE.equals("m7wls")
                || DEVICE.equals("jfltevzw")
                || DEVICE.equals("t0lte"))
            CWM_VERSION = CWM_VERSION + "-6.0.3.2";

        if (CWM_VERSION.equals(""))
            CWM_OFFICIAL = false;

        if (DEVICE.equals("thunderg"))
            TWRP_VERSION = "-2.0.0alpha1";

        if (DEVICE.equals("leo"))
            TWRP_VERSION = "-2.2.0";

        if (DEVICE.equals("passion")
                || DEVICE.equals("crespo")
                || DEVICE.equals("crespo4g")
                || DEVICE.equals("maguro")
                || DEVICE.equals("toro")
                || DEVICE.equals("toroplus")
                || DEVICE.equals("grouper")
                || DEVICE.equals("tilapia")
                || DEVICE.equals("mako")
                || DEVICE.equals("manta")
                || DEVICE.equals("tf700t")
                || DEVICE.equals("ace")
                || DEVICE.equals("saga")
                || DEVICE.equals("pyramid")
                || DEVICE.equals("fireball")
                || DEVICE.equals("vivow")
                || DEVICE.equals("supersonic")
                || DEVICE.equals("jewel")
                || DEVICE.equals("primou")
                || DEVICE.equals("ville")
                || DEVICE.equals("villec2")
                || DEVICE.equals("endeavoru")
                || DEVICE.equals("evita")
                || DEVICE.equals("dlxub1")
                || DEVICE.equals("dlx")
                || DEVICE.equals("m7")
                || DEVICE.equals("m7wls")
                || DEVICE.equals("olympus")
                || DEVICE.equals("find5")
                || DEVICE.equals("jfltexx")
                || DEVICE.equals("jfltespr")
                || DEVICE.equals("jfltevzw")
                || DEVICE.equals("jfltetmo")
                || DEVICE.equals("jfltecan")
                || DEVICE.equals("jflteatt")
                || DEVICE.equals("jflteusc")
                || DEVICE.equals("skyrocket")
                || DEVICE.equals("n8013")
                || DEVICE.equals("d2att")
                || DEVICE.equals("d2tmo")
                || DEVICE.equals("d2vzw")
                || DEVICE.equals("d2spr")
                || DEVICE.equals("d2usc")
                || DEVICE.equals("golden")
                || DEVICE.equals("geeb")
                || DEVICE.equals("p990")
                || DEVICE.equals("p970")
                || DEVICE.equals("nozomi")
                || DEVICE.equals("mint")
                || DEVICE.equals("enrc2b")
                || DEVICE.equals("heroc")
                || DEVICE.equals("shooter")
                || DEVICE.equals("i9300"))
            TWRP_VERSION = "-2.6.0.0";

        if (DEVICE.equals("t0lte")
                || DEVICE.equals("t0lteatt")
                || DEVICE.equals("t0ltetmo")
                || DEVICE.equals("t0ltecan")
                || DEVICE.equals("t0ltevzw")
                || DEVICE.equals("t0ltespr")
                || DEVICE.equals("t03g"))
            TWRP_VERSION = "-2.6.0.1";

        if (DEVICE.equals("tf201")) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH
                    || Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                TWRP_VERSION = TWRP_VERSION + "-ICS";
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                TWRP_VERSION = TWRP_VERSION + "-JB";
            }
        }
        if (TWRP_VERSION.equals(""))
            TWRP_OFFICIAL = false;
    }

    public void constructFile() {
        if (CWM_OFFICIAL) {
            CWM_IMG = new File(RecoveryTools.PathToRecoveries, "recovery-clockwork" + CWM_VERSION + "-" + DEVICE + EXT);
        } else {
            CWM_IMG = new File(RecoveryTools.PathToRecoveries, DEVICE + "-cwm" + EXT);
        }

        if (TWRP_OFFICIAL) {
            TWRP_IMG = new File(RecoveryTools.PathToRecoveries, "openrecovery-twrp" + TWRP_VERSION + "-" + DEVICE + EXT);
        } else {
            TWRP_IMG = new File(RecoveryTools.PathToRecoveries, DEVICE + "-twrp" + EXT);
        }
    }

    public void installZipOverTWRP(File ZipFile) {
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