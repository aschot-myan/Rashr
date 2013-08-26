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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

import java.io.File;

import de.mkrtchyan.utils.Downloader;
import de.mkrtchyan.utils.Notifyer;


public class DeviceHandler {
    /*
     * This class content all device specified information to provide
     * all information for all other classes for example:
     * What kind of partition and where is the recovery partition in the
     * FileSystem
     */

    public String DEVICE_NAME = Build.DEVICE;
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
	private Context mContext;

    public File fflash, fdump, charger, chargermon, ric;


    public DeviceHandler(Context mContext) {

	    this.mContext = mContext;

        String BOARD = Build.BOARD;
        String MODEL = Build.MODEL;

//	Set DEVICE_NAME predefined options
//      Samsung Galaxy S +
        if (DEVICE_NAME.equals("GT-I9001")
                || BOARD.equals("GT-I9001")
                || MODEL.equals("GT-I9001"))
            DEVICE_NAME = "galaxysplus";

//		Kindle Fire HD 7"
        if (DEVICE_NAME.equals("D01E"))
            DEVICE_NAME = "kfhd7";

        if (BOARD.equals("rk29sdk"))
            DEVICE_NAME = "rk29sdk";

//      HTC ONE GSM

        if (BOARD.equals("m7")
                || DEVICE_NAME.equals("m7")
                || DEVICE_NAME.equals("m7ul"))
            DEVICE_NAME = "m7";

        if (DEVICE_NAME.equals("m7spr"))
            DEVICE_NAME = "m7wls";

//      Samsung Galaxy S4 (i9505/jflte)
        if (DEVICE_NAME.equals("jflte"))
            DEVICE_NAME = "jfltexx";

//		Galaxy Note 
        if (DEVICE_NAME.equals("GT-N7000")
                || DEVICE_NAME.equals("n7000")
                || DEVICE_NAME.equals("galaxynote")
                || DEVICE_NAME.equals("N7000")
                || BOARD.equals("GT-N7000")
                || BOARD.equals("n7000")
                || BOARD.equals("galaxynote")
                || BOARD.equals("N7000"))
            DEVICE_NAME = "n7000";

//      Samsung Galaxy Note 10.1
        if (MODEL.equals("GT-N8013")
                || DEVICE_NAME.equals("p4notewifi"))
            DEVICE_NAME = "n8013";

//      Samsung Galaxy Tab 2
        if (BOARD.equals("piranha")
                || MODEL.equals("GT-P3113"))
            DEVICE_NAME = "p3113";

        if (DEVICE_NAME.equals("espressowifi")
                || MODEL.equals("GT-P3110"))
            DEVICE_NAME = "p3110";

//		Galaxy Note 2
        if (DEVICE_NAME.equals("n7100")
                || DEVICE_NAME.equals("n7100")
                || DEVICE_NAME.equals("GT-N7100")
                || BOARD.equals("t03g")
                || BOARD.equals("n7100")
                || BOARD.equals("GT-N7100"))
            DEVICE_NAME = "t03g";

//		Galaxy Note 2 LTE
        if (DEVICE_NAME.equals("t0ltexx")
                || DEVICE_NAME.equals("GT-N7105")
                || DEVICE_NAME.equals("t0ltedv")
                || DEVICE_NAME.equals("GT-N7105T")
                || DEVICE_NAME.equals("t0ltevl")
                || DEVICE_NAME.equals("SGH-I317M")
                || BOARD.equals("t0ltexx")
                || BOARD.equals("GT-N7105")
                || BOARD.equals("t0ltedv")
                || BOARD.equals("GT-N7105T")
                || BOARD.equals("t0ltevl")
                || BOARD.equals("SGH-I317M"))
            DEVICE_NAME = "t0lte";

        if (DEVICE_NAME.equals("SGH-I317")
                || BOARD.equals("t0lteatt")
                || BOARD.equals("SGH-I317"))
            DEVICE_NAME = "t0lteatt";

        if (DEVICE_NAME.equals("SGH-T889")
                || BOARD.equals("t0ltetmo")
                || BOARD.equals("SGH-T889"))
            DEVICE_NAME = "t0ltetmo";

        if (BOARD.equals("t0ltecan"))
            DEVICE_NAME = "t0ltecan";

//		Galaxy S3 (international)
        if (DEVICE_NAME.equals("GT-I9300")
                || DEVICE_NAME.equals("Galaxy S3")
                || DEVICE_NAME.equals("GalaxyS3")
                || DEVICE_NAME.equals("m0")
                || DEVICE_NAME.equals("i9300")
                || BOARD.equals("GT-I9300")
                || BOARD.equals("m0")
                || BOARD.equals("i9300"))
            DEVICE_NAME = "i9300";

//		Galaxy S2
        if (DEVICE_NAME.equals("GT-I9100G")
                || DEVICE_NAME.equals("GT-I9100M")
                || DEVICE_NAME.equals("GT-I9100P")
                || DEVICE_NAME.equals("GT-I9100")
                || DEVICE_NAME.equals("galaxys2")
                || BOARD.equals("GT-I9100G")
                || BOARD.equals("GT-I9100M")
                || BOARD.equals("GT-I9100P")
                || BOARD.equals("GT-I9100")
                || BOARD.equals("galaxys2"))
            DEVICE_NAME = "galaxys2";

//		Galaxy S2 ATT
        if (DEVICE_NAME.equals("SGH-I777")
                || BOARD.equals("SGH-777")
                || BOARD.equals("galaxys2att"))
            DEVICE_NAME = "galaxys2att";

//		Galaxy S2 LTE (skyrocket)
        if (DEVICE_NAME.equals("SGH-I727")
                || BOARD.equals("skyrocket")
                || BOARD.equals("SGH-I727"))
            DEVICE_NAME = "skyrocket";

//      Galaxy S (i9000)
        if (DEVICE_NAME.equals("galaxys")
                || DEVICE_NAME.equals("galaxysmtd")
                || DEVICE_NAME.equals("GT-I9000")
                || DEVICE_NAME.equals("GT-I9000M")
                || DEVICE_NAME.equals("GT-I9000T")
                || BOARD.equals("galaxys")
                || BOARD.equals("galaxysmtd")
                || BOARD.equals("GT-I9000")
                || BOARD.equals("GT-I9000M")
                || BOARD.equals("GT-I9000T")
                || MODEL.equals("GT-I9000T")
                || DEVICE_NAME.equals("SPH-D710BST")
                || MODEL.equals("SPH-D710BST"))
            DEVICE_NAME = "galaxys";

//		GalaxyS Captivate (SGH-I897)
        if (DEVICE_NAME.equals("SGH-I897")) {
            DEVICE_NAME = ("captivate");
        }

        if (BOARD.equals("gee")) {
            DEVICE_NAME = "geeb";
        }

//		Sony Xperia Z (C6603)
        if (DEVICE_NAME.equals("C6603")
                || DEVICE_NAME.equals("yuga")) {
            DEVICE_NAME = "C6603";
            EXT = ".tar";
        }

//		Sony Xperia S
        if (DEVICE_NAME.equals("LT26i"))
            DEVICE_NAME = "nozomi";

//		Sony Xperia T
        if (DEVICE_NAME.equals("LT30p"))
            DEVICE_NAME = "mint";

//      HTC Desire HD
        if (BOARD.equals("ace"))
            DEVICE_NAME = "ace";

//      LG Optimus L9
        if (DEVICE_NAME.equals("u2")
                || BOARD.equals("u2")
                || MODEL.equals("LG-P760"))
            DEVICE_NAME = "p760";
//      Huawei U9508
        if (BOARD.equals("U9508")
                || DEVICE_NAME.equals("hwu9508"))
            DEVICE_NAME = "u9508";

//      Huawei Ascend P1
        if (DEVICE_NAME.equals("hwu9200")
                || BOARD.equals("U9200")
                || MODEL.equals("U9200"))
            DEVICE_NAME = "u9200";

//      Motorola Droid RAZR
        if (DEVICE_NAME.equals("cdma_spyder")
                || BOARD.equals("spyder"))
            DEVICE_NAME = "spyder";

//		MTD Devices
        if (DEVICE_NAME.equals("crespo")
                || DEVICE_NAME.equals("crespo4g")
                || DEVICE_NAME.equals("passion")
                || DEVICE_NAME.equals("saga")
                || DEVICE_NAME.equals("swift")
                || DEVICE_NAME.equals("thunderc")
                || DEVICE_NAME.equals("supersonic")
                || DEVICE_NAME.equals("thunderg")
                || DEVICE_NAME.equals("hero")
                || DEVICE_NAME.equals("heroc")
                || DEVICE_NAME.equals("pecan")
                || DEVICE_NAME.equals("leo")
                || DEVICE_NAME.equals("p760")
                || DEVICE_NAME.equals("bravo")
                || DEVICE_NAME.equals("sholes")
                || DEVICE_NAME.equals("blade")
                || DEVICE_NAME.equals("pico")
                || DEVICE_NAME.equals("sholest")
                || DEVICE_NAME.equals("magnids")
                || DEVICE_NAME.equals("rk29sdk")
                || DEVICE_NAME.equals("spyder"))
            MTD = true;

        if (DEVICE_NAME.equals("droid2")
                || DEVICE_NAME.equals("daytona")
                || DEVICE_NAME.equals("captivate")
                || DEVICE_NAME.equals("galaxys")
                || DEVICE_NAME.equals("galaxys2att")
                || DEVICE_NAME.equals("galaxys2")
                || DEVICE_NAME.equals("n7000"))
            FLASH_OVER_RECOVERY = true;


//		Devices who kernel will be flashed to
        if (DEVICE_NAME.equals("nozomi")
                || DEVICE_NAME.equals("mint"))
            KERNEL_TO = true;

        if (FLASH_OVER_RECOVERY)
            EXT = ".zip";

        DEVICE_NAME = DEVICE_NAME.toLowerCase();

        RecoveryPath = getRecoveryPath();
        getUnsupportedSystems();
        getVersion();
        constructFile();
    }

    private String getRecoveryPath() {

        String tmp = "";

//		Nexus DEVICEs + Same
        if (DEVICE_NAME.equals("maguro")
                || DEVICE_NAME.equals("toro")
                || DEVICE_NAME.equals("toroplus"))
            tmp = "/dev/block/platform/omap/omap_hsmmc.0/by-name/recovery";

        if (DEVICE_NAME.equals("grouper")
                || DEVICE_NAME.equals("tilapia")
                || DEVICE_NAME.equals("p880"))
            tmp = "/dev/block/platform/sdhci-tegra.3/by-name/SOS";

        if (DEVICE_NAME.equals("mako")
                || DEVICE_NAME.equals("geeb")
                || DEVICE_NAME.equals("vanquish")
                || DEVICE_NAME.equals("find5")
                || DEVICE_NAME.equals("jgedlte")
                || DEVICE_NAME.equals("flo"))
            tmp = "/dev/block/platform/msm_sdcc.1/by-name/recovery";

        if (DEVICE_NAME.equals("manta"))
            tmp = "/dev/block/platform/dw_mmc.0/by-name/recovery";

//		Samsung DEVICEs + Same
        if (DEVICE_NAME.equals("d2att")
                || DEVICE_NAME.equals("d2tmo")
                || DEVICE_NAME.equals("d2vzw")
                || DEVICE_NAME.equals("d2spr")
                || DEVICE_NAME.equals("d2usc")
                || DEVICE_NAME.equals("SCH-i929"))
            tmp = "/dev/block/mmcblk0p18";

        if (DEVICE_NAME.equals("i9300")
                || DEVICE_NAME.equals("galaxys2")
                || DEVICE_NAME.equals("n8013")
                || DEVICE_NAME.equals("p3113")
                || DEVICE_NAME.equals("p3110"))
            tmp = "/dev/block/mmcblk0p6";

        if (DEVICE_NAME.equals("t03g")
                || DEVICE_NAME.equals("tf700t")
                || DEVICE_NAME.equals("tf201")
                || DEVICE_NAME.equals("t0lte")
                || DEVICE_NAME.equals("t0ltecan")
                || DEVICE_NAME.equals("t0ltektt")
                || DEVICE_NAME.equals("t0lteskt")
                || DEVICE_NAME.equals("t0ltespr")
                || DEVICE_NAME.equals("t0lteusc")
                || DEVICE_NAME.equals("t0ltevzw")
                || DEVICE_NAME.equals("t0lteatt")
                || DEVICE_NAME.equals("t0ltetmo"))
            tmp = "/dev/block/mmcblk0p9";

        if (DEVICE_NAME.equals("golden")
		        || DEVICE_NAME.equals("villec2")
                || DEVICE_NAME.equals("vivow")
                || DEVICE_NAME.equals("kingdom")
                || DEVICE_NAME.equals("vision")
                || DEVICE_NAME.equals("mystul")
                || DEVICE_NAME.equals("jfltespr")
                || DEVICE_NAME.equals("jflteatt")
                || DEVICE_NAME.equals("jfltevzw")
                || DEVICE_NAME.equals("jfltexx")
                || DEVICE_NAME.equals("jfltecan")
                || DEVICE_NAME.equals("jfltetmo")
                || DEVICE_NAME.equals("jflteusc")
                || DEVICE_NAME.equals("flyer"))
            tmp = "/dev/block/mmcblk0p21";

        if (DEVICE_NAME.equals("jena"))
            tmp = "/dev/block/mmcblk0p12";

        if (DEVICE_NAME.equals("GT-I9103"))
            tmp = "/dev/block/mmcblk0p8";

//		HTC DEVICEs + Same
        if (DEVICE_NAME.equals("m7"))
            tmp = "/dev/block/mmcblk0p34";

        if (DEVICE_NAME.equals("m7wls"))
            tmp = "/dev/block/mmcblk0p36";

        if (DEVICE_NAME.equals("endeavoru")
                || DEVICE_NAME.equals("enrc2b"))
            tmp = "/dev/block/mmcblk0p5";

        if (DEVICE_NAME.equals("ace")
                || DEVICE_NAME.equals("primou"))
            tmp = "/dev/block/platform/msm_sdcc.2/mmcblk0p21";

        if (DEVICE_NAME.equals("pyramid"))
            tmp = "/dev/block/platform/msm_sdcc.1/mmcblk0p21";

        if (DEVICE_NAME.equals("ville")
                || DEVICE_NAME.equals("evita")
                || DEVICE_NAME.equals("skyrocket")
                || DEVICE_NAME.equals("fireball")
                || DEVICE_NAME.equals("jewel")
                || DEVICE_NAME.equals("shooter"))
            tmp = "/dev/block/mmcblk0p22";

        if (DEVICE_NAME.equals("dlxub1")
                || DEVICE_NAME.equals("dlx"))
            tmp = "/dev/block/mmcblk0p20";

//		Motorola DEVICEs + Same

        if (DEVICE_NAME.equals("olympus")
                || DEVICE_NAME.equals("ja3g")
                || DEVICE_NAME.equals("daytona"))
            tmp = "/dev/block/mmcblk0p10";

        if (DEVICE_NAME.equals("stingray")
                || DEVICE_NAME.equals("wingray"))
            tmp = "/dev/block/platform/sdhci-tegra.3/by-name/recovery";

//      Huawei DEVICEs + Same
        if (DEVICE_NAME.equals("u9508"))
            tmp = "/dev/block/platform/hi_mci.1/by-name/recovery";

        if (DEVICE_NAME.equals("u9200")
                || DEVICE_NAME.equals("kfdh7"))
            tmp = "/dev/block/platform/omap/omap_hsmmc.1/by-name/recovery";

//		Sony DEVICEs + Same
        if (DEVICE_NAME.equals("nozomi"))
            tmp = "/dev/block/mmcblk0p3";

        if (DEVICE_NAME.equals("c6603"))
            tmp = "/system/bin/recovery.tar";

//		LG DEVICEs + Same
        if (DEVICE_NAME.equals("p990")
                || DEVICE_NAME.equals("tf300t"))
            tmp = "/dev/block/mmcblk0p7";

        if (DEVICE_NAME.equals("x3"))
            tmp = "/dev/block/mmcblk0p1";

        if (DEVICE_NAME.equals("m3s")
                || DEVICE_NAME.equals("bryce"))
            tmp = "/dev/block/mmcblk0p14";

        if (DEVICE_NAME.equals("p970")
                || DEVICE_NAME.equals("mint"))
            tmp = "/dev/block/mmcblk0p4";

//		ZTE DEVICEs + Same
        if (DEVICE_NAME.equals("warp2")
                || DEVICE_NAME.equals("hwc8813")
                || DEVICE_NAME.equals("galaxysplus"))
            tmp = "/dev/block/mmcblk0p13";

        return tmp;
    }

    public void getUnsupportedSystems() {

        if (DEVICE_NAME.equals("galaxys2")
                || DEVICE_NAME.equals("SGH-I777")
                || DEVICE_NAME.equals("n7000")
                || DEVICE_NAME.equals("x3")
                || DEVICE_NAME.equals("droid2")
                || DEVICE_NAME.equals("kingdom")
                || DEVICE_NAME.equals("SGH-I897")
                || DEVICE_NAME.equals("thunderc")
                || DEVICE_NAME.equals("SCH-i929")
                || DEVICE_NAME.equals("m3s")
                || DEVICE_NAME.equals("SPH-D710")
                || DEVICE_NAME.equals("GT-P6200")
                || DEVICE_NAME.equals("galaxys")
                || DEVICE_NAME.equals("bryce")
                || DEVICE_NAME.equals("vision")
                || DEVICE_NAME.equals("hero")
                || DEVICE_NAME.equals("pecan")
                || DEVICE_NAME.equals("mystul")
                || DEVICE_NAME.equals("u9508")
                || DEVICE_NAME.equals("p880")
                || DEVICE_NAME.equals("p3113")
                || DEVICE_NAME.equals("p3110")
                || DEVICE_NAME.equals("flyer")
                || DEVICE_NAME.equals("daytona")
                || DEVICE_NAME.equals("vanquish")
                || DEVICE_NAME.equals("bravo")
                || DEVICE_NAME.equals("hwc8813")
                || DEVICE_NAME.equals("galaxysplus")
                || DEVICE_NAME.equals("tf300t")
                || DEVICE_NAME.equals("blade")
                || DEVICE_NAME.equals("u9200")
                || DEVICE_NAME.equals("sholest")
                || DEVICE_NAME.equals("magnids")
                || DEVICE_NAME.equals("stingray")
                || DEVICE_NAME.equals("wingray")
                || DEVICE_NAME.equals("rk29sdk")
                || DEVICE_NAME.equals("spyder"))
            TWRP = false;

        if (DEVICE_NAME.equals("nozomi")
                || DEVICE_NAME.equals("mint")
                || DEVICE_NAME.equals("LT30p")
                || DEVICE_NAME.equals("kfhd7")
                || DEVICE_NAME.equals("LT26i")
                || DEVICE_NAME.equals("thunderc")
                || DEVICE_NAME.equals("SCH-i929")
                || DEVICE_NAME.equals("m3s")
                || DEVICE_NAME.equals("SPH-D710")
                || DEVICE_NAME.equals("GT-P6200")
                || DEVICE_NAME.equals("bryce")
                || DEVICE_NAME.equals("pecan")
                || DEVICE_NAME.equals("mystul")
                || DEVICE_NAME.equals("u9508")
                || DEVICE_NAME.equals("enrc2b")
                || DEVICE_NAME.equals("flyer")
                || DEVICE_NAME.equals("vanquish")
                || DEVICE_NAME.equals("hwc8813")
                || DEVICE_NAME.equals("galaxysplus")
                || DEVICE_NAME.equals("tf300t")
                || DEVICE_NAME.equals("jgedlte")
                || DEVICE_NAME.equals("u9200")
                || DEVICE_NAME.equals("sholest")
                || DEVICE_NAME.equals("t0lteusc")
                || DEVICE_NAME.equals("t0ltecan")
                || DEVICE_NAME.equals("t0ltektt")
                || DEVICE_NAME.equals("t0lteskt")
                || DEVICE_NAME.equals("magnids")
                || DEVICE_NAME.equals("flo")
                || DEVICE_NAME.equals("rk29sdk")
                || DEVICE_NAME.equals("spyder"))
            CWM = false;

        if (DEVICE_NAME.equals("")
                || !MTD && RecoveryPath.equals("") && !FLASH_OVER_RECOVERY) {
            TWRP = false;
            CWM = false;
            OTHER = false;
        }
    }

    public void getVersion() {
//		CLOCKWORKMOD touch supported devices
        if (DEVICE_NAME.equals("ace")
                || DEVICE_NAME.equals("crespo")
                || DEVICE_NAME.equals("crespo4g")
                || DEVICE_NAME.equals("d2att")
                || DEVICE_NAME.equals("d2tmo")
                || DEVICE_NAME.equals("endeavoru")
                || DEVICE_NAME.equals("evita")
                || DEVICE_NAME.equals("fireball")
                || DEVICE_NAME.equals("galaxys2")
                || DEVICE_NAME.equals("golden")
                || DEVICE_NAME.equals("grouper")
                || DEVICE_NAME.equals("i9300")
                || DEVICE_NAME.equals("maguro")
                || DEVICE_NAME.equals("mako")
                || DEVICE_NAME.equals("manta")
                || DEVICE_NAME.equals("t03g")
                || DEVICE_NAME.equals("pyramid")
                || DEVICE_NAME.equals("saga")
                || DEVICE_NAME.equals("skyrocket")
                || DEVICE_NAME.equals("tilapia")
                || DEVICE_NAME.equals("toro")
                || DEVICE_NAME.equals("toroplus")
                || DEVICE_NAME.equals("ville")
                || DEVICE_NAME.equals("warp2")
                || DEVICE_NAME.equals("p990")
                || DEVICE_NAME.equals("tf700t")
                || DEVICE_NAME.equals("dlx")
                || DEVICE_NAME.equals("jflte")
                || DEVICE_NAME.equals("d2spr")
                || DEVICE_NAME.equals("supersonic")
                || DEVICE_NAME.equals("olympus")
                || DEVICE_NAME.equals("m7spr")
                || DEVICE_NAME.equals("jfltespr")
                || DEVICE_NAME.equals("jewel")
                || DEVICE_NAME.equals("shooter")
                || DEVICE_NAME.equals("jfltevzw")
                || DEVICE_NAME.equals("p970")
                || DEVICE_NAME.equals("p760")
                || DEVICE_NAME.equals("jfltecan")
                || DEVICE_NAME.equals("jfltexx")
                || DEVICE_NAME.equals("jfltespr")
                || DEVICE_NAME.equals("m7")
                || DEVICE_NAME.equals("m7wls")
                || DEVICE_NAME.equals("jfltevzw")
                || DEVICE_NAME.equals("p880")
                || DEVICE_NAME.equals("n8013")
                || DEVICE_NAME.equals("jfltetmo")
                || DEVICE_NAME.equals("p3113")
                || DEVICE_NAME.equals("d2usc")
                || DEVICE_NAME.equals("bravo")
                || DEVICE_NAME.equals("find5")
                || DEVICE_NAME.equals("jflteatt")
                || DEVICE_NAME.equals("jflteusc")
                || DEVICE_NAME.equals("p3110")
                || DEVICE_NAME.equals("stingray")
                || DEVICE_NAME.equals("wingray"))
            CWM_VERSION = "-touch";

//	    Newest Clockworkmod version for devices
        if (DEVICE_NAME.equals("sholes"))
            CWM_VERSION = CWM_VERSION + "-2.5.0.1";

        if (DEVICE_NAME.equals("heroc"))
            CWM_VERSION = CWM_VERSION + "-2.5.0.7";

        if (DEVICE_NAME.equals("SGH-I897")
                || DEVICE_NAME.equals("galaxys")
                || DEVICE_NAME.equals("captivate"))
            CWM_VERSION = CWM_VERSION + "-2.5.1.2";

        if (DEVICE_NAME.equals("leo"))
            CWM_VERSION = CWM_VERSION + "-3.1.0.0";

        if (DEVICE_NAME.equals("droid2")
                || DEVICE_NAME.equals("vivow")
                || DEVICE_NAME.equals("blade"))
            CWM_VERSION = CWM_VERSION + "-5.0.2.0";

        if (DEVICE_NAME.equals("daytona"))
            CWM_VERSION = CWM_VERSION + "-5.0.2.5";

        if (DEVICE_NAME.equals("thunderg"))
            CWM_VERSION = CWM_VERSION + "-5.0.2.7";

        if (DEVICE_NAME.equals("pico"))
            CWM_VERSION = CWM_VERSION + "-5.0.2.8";

        if (DEVICE_NAME.equals("supersonic"))
            CWM_VERSION = CWM_VERSION + "-5.8.0.1";

        if (DEVICE_NAME.equals("shooter")
                || DEVICE_NAME.equals("bravo"))
            CWM_VERSION = CWM_VERSION + "-5.8.0.2";
        if (DEVICE_NAME.equals("pyramid"))
            CWM_VERSION = CWM_VERSION + "-5.8.0.9";

        if (DEVICE_NAME.equals("ace")
                || DEVICE_NAME.equals("saga")
                || DEVICE_NAME.equals("galaxys2")
                || DEVICE_NAME.equals("olympus"))
            CWM_VERSION = CWM_VERSION + "-5.8.1.5";

        if (DEVICE_NAME.equals("tf201"))
            CWM_VERSION = CWM_VERSION + "-5.8.3.4";

        if (DEVICE_NAME.equals("jewel"))
            CWM_VERSION = CWM_VERSION + "-5.8.3.5";

        if (DEVICE_NAME.equals("endeavoru"))
            CWM_VERSION = CWM_VERSION + "-5.8.4.0";

        if (DEVICE_NAME.equals("primou"))
            CWM_VERSION = CWM_VERSION + "-5.8.4.5";

        if (DEVICE_NAME.equals("n7000"))
            CWM_VERSION = CWM_VERSION + "-6.0.1.2";

        if (DEVICE_NAME.equals("p970"))
            CWM_VERSION = CWM_VERSION + "-6.0.1.4";

        if (DEVICE_NAME.equals("p3113"))
            CWM_VERSION = CWM_VERSION + "-6.0.2.3";

        if (DEVICE_NAME.equals("golden")
                || DEVICE_NAME.equals("warp2")
                || DEVICE_NAME.equals("p3110"))
            CWM_VERSION = CWM_VERSION + "-6.0.2.7";

        if (DEVICE_NAME.equals("t0ltevzw"))
            CWM_VERSION = "-6.0.3.0";

        if (DEVICE_NAME.equals("crespo")
                || DEVICE_NAME.equals("crespo4g")
                || DEVICE_NAME.equals("d2att")
                || DEVICE_NAME.equals("d2tmo")
                || DEVICE_NAME.equals("dlxub1")
                || DEVICE_NAME.equals("evita")
                || DEVICE_NAME.equals("fireball")
                || DEVICE_NAME.equals("grouper")
                || DEVICE_NAME.equals("i9300")
                || DEVICE_NAME.equals("maguro")
                || DEVICE_NAME.equals("mako")
                || DEVICE_NAME.equals("manta")
                || DEVICE_NAME.equals("t03g")
                || DEVICE_NAME.equals("skyrocket")
                || DEVICE_NAME.equals("tilapia")
                || DEVICE_NAME.equals("toro")
                || DEVICE_NAME.equals("toroplus")
                || DEVICE_NAME.equals("ville")
                || DEVICE_NAME.equals("p990")
                || DEVICE_NAME.equals("tf700t")
                || DEVICE_NAME.equals("m7")
                || DEVICE_NAME.equals("dlx")
                || DEVICE_NAME.equals("d2spr")
                || DEVICE_NAME.equals("p760")
                || DEVICE_NAME.equals("p880")
                || DEVICE_NAME.equals("n8013")
                || DEVICE_NAME.equals("d2usc")
                || DEVICE_NAME.equals("find5")
                || DEVICE_NAME.equals("t0ltespr")
                || DEVICE_NAME.equals("t0lteatt")
                || DEVICE_NAME.equals("t0ltetmo")
                || DEVICE_NAME.equals("stingray")
                || DEVICE_NAME.equals("wingray"))
            CWM_VERSION = CWM_VERSION + "-6.0.3.1";

        if (DEVICE_NAME.equals("jfltexx")
                || DEVICE_NAME.equals("jfltespr")
                || DEVICE_NAME.equals("m7wls")
                || DEVICE_NAME.equals("jfltevzw")
                || DEVICE_NAME.equals("t0lte"))
            CWM_VERSION = CWM_VERSION + "-6.0.3.2";

        if (CWM_VERSION.equals(""))
            CWM_OFFICIAL = false;

        if (DEVICE_NAME.equals("thunderg"))
            TWRP_VERSION = "-2.0.0alpha1";

        if (DEVICE_NAME.equals("leo"))
            TWRP_VERSION = "-2.2.0";

        if (DEVICE_NAME.equals("pico"))
            TWRP_VERSION = "-2.4.4.0";

        if (DEVICE_NAME.equals("passion")
                || DEVICE_NAME.equals("crespo")
                || DEVICE_NAME.equals("crespo4g")
                || DEVICE_NAME.equals("maguro")
                || DEVICE_NAME.equals("toro")
                || DEVICE_NAME.equals("toroplus")
                || DEVICE_NAME.equals("grouper")
                || DEVICE_NAME.equals("tilapia")
                || DEVICE_NAME.equals("mako")
                || DEVICE_NAME.equals("manta")
                || DEVICE_NAME.equals("tf700t")
                || DEVICE_NAME.equals("ace")
                || DEVICE_NAME.equals("saga")
                || DEVICE_NAME.equals("pyramid")
                || DEVICE_NAME.equals("fireball")
                || DEVICE_NAME.equals("vivow")
                || DEVICE_NAME.equals("supersonic")
                || DEVICE_NAME.equals("jewel")
                || DEVICE_NAME.equals("primou")
                || DEVICE_NAME.equals("ville")
                || DEVICE_NAME.equals("villec2")
                || DEVICE_NAME.equals("endeavoru")
                || DEVICE_NAME.equals("evita")
                || DEVICE_NAME.equals("dlxub1")
                || DEVICE_NAME.equals("dlx")
                || DEVICE_NAME.equals("m7")
                || DEVICE_NAME.equals("m7wls")
                || DEVICE_NAME.equals("olympus")
                || DEVICE_NAME.equals("find5")
                || DEVICE_NAME.equals("skyrocket")
                || DEVICE_NAME.equals("n8013")
                || DEVICE_NAME.equals("d2att")
                || DEVICE_NAME.equals("d2tmo")
                || DEVICE_NAME.equals("d2vzw")
                || DEVICE_NAME.equals("d2spr")
                || DEVICE_NAME.equals("d2usc")
                || DEVICE_NAME.equals("golden")
                || DEVICE_NAME.equals("geeb")
                || DEVICE_NAME.equals("p990")
                || DEVICE_NAME.equals("p970")
                || DEVICE_NAME.equals("nozomi")
                || DEVICE_NAME.equals("mint")
                || DEVICE_NAME.equals("enrc2b")
                || DEVICE_NAME.equals("heroc")
                || DEVICE_NAME.equals("shooter")
                || DEVICE_NAME.equals("i9300")
                || DEVICE_NAME.equals("jgedlte")
                || DEVICE_NAME.equals("flo"))
            TWRP_VERSION = "-2.6.0.0";

        if (DEVICE_NAME.equals("t03g")
                || DEVICE_NAME.equals("t0lteusc")
                || DEVICE_NAME.equals("t0ltecan")
                || DEVICE_NAME.equals("t0ltektt")
                || DEVICE_NAME.equals("t0lteskt")
                || DEVICE_NAME.equals("t0ltespr")
                || DEVICE_NAME.equals("t0ltevzw"))
            TWRP_VERSION = "-2.6.0.1";

        if (DEVICE_NAME.equals("t0lteatt")
                || DEVICE_NAME.equals("t0ltetmo"))
            TWRP_VERSION = "-2.6.0.3";

        if (DEVICE_NAME.equals("tf201")) {
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
            CWM_IMG = new File(RecoveryTools.PathToRecoveries, "recovery-clockwork" + CWM_VERSION + "-" + DEVICE_NAME + EXT);
        } else {
            CWM_IMG = new File(RecoveryTools.PathToRecoveries, DEVICE_NAME + "-cwm" + EXT);
        }

        if (TWRP_OFFICIAL) {
            TWRP_IMG = new File(RecoveryTools.PathToRecoveries, "openrecovery-twrp" + TWRP_VERSION + "-" + DEVICE_NAME + EXT);
        } else {
            TWRP_IMG = new File(RecoveryTools.PathToRecoveries, DEVICE_NAME + "-twrp" + EXT);
        }
    }

    public boolean downloadUtils() {

        boolean download = false;

        if (DEVICE_NAME.equals("c6603")
                || DEVICE_NAME.equals("montblanc")) {
            charger = new File(RecoveryTools.PathToUtils, "charger");
            chargermon = new File(RecoveryTools.PathToUtils, "chargermon");
            ric = new File(mContext.getFilesDir(), "ric");
            if (!charger.exists() || !chargermon.exists() || !chargermon.exists() || !ric.exists()
                    && DEVICE_NAME.equals("c6603"))
                download = true;
        }

        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);
        mAlertDialog
                .setTitle(R.string.warning)
                .setMessage(R.string.download_utils);
        DialogInterface.OnClickListener onClick = null;

        if (DEVICE_NAME.equals("c6603") || DEVICE_NAME.equals("montblanc")
                && download) {

            onClick = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    new Downloader(mContext, "http://dslnexus.nazuka.net/utils/" + DEVICE_NAME, chargermon.getName(), chargermon, Notifyer.rEmpty).execute();
                    new Downloader(mContext, "http://dslnexus.nazuka.net/utils/" + DEVICE_NAME, charger.getName(), charger, Notifyer.rEmpty).execute();
                    if (DEVICE_NAME.equals("c6603"))
                        new Downloader(mContext, "http://dslnexus.nazuka.net/utils/" + DEVICE_NAME, ric.getName(), ric, Notifyer.rEmpty).execute();
                }
            };
        }
        mAlertDialog.setPositiveButton(R.string.positive, onClick);
        if (download)
            mAlertDialog.show();

        return download;
    }
}