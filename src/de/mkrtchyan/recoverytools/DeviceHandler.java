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
import java.io.IOException;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Downloader;
import de.mkrtchyan.utils.Unzipper;


public class DeviceHandler {

    public static final int DEV_TYPE_DD = 1;
    public static final int DEV_TYPE_MTD = 2;
    public static final int DEV_TYPE_RECOVERY = 3;
    public static final int DEV_TYPE_SONY = 4;

    public static final String HOST_URL = "http://dslnexus.org/Android/recoveries";

    private int DEV_TYPE = 0;

    /*
     * This class content all device specified information to provide
     * all information for all other classes for example:
     * What kind of partition and where is the recovery partition in the
     * FileSystem
     */

    public String DEV_NAME = Build.DEVICE.toLowerCase();
    private String RecoveryPath = "";
    private static final File[] RecoveryList = {
            new File("/dev/block/platform/omap/omap_hsmmc.0/by-name/recovery"),
            new File("/dev/block/platform/omap/omap_hsmmc.1/by-name/recovery"),
            new File("/dev/block/platform/sdhci-tegra.3/by-name/recovery"),
            new File("/dev/block/platform/sdhci-pxav3.2/by-name/RECOVERY"),
            new File("/dev/block/platform/msm_sdcc.1/by-name/recovery"),
            new File("/dev/block/platform/sdhci-tegra.3/by-name/SOS"),
            new File("/dev/block/platform/sdhci-tegra.3/by-name/USP"),
            new File("/dev/block/platform/sdhci-tegra.3/by-name/UP"),
            new File("/dev/block/platform/sdhci-tegra.3/by-name/SS"),
            new File("/dev/block/platform/sdhci.1/by-name/RECOVERY"),
            new File("/dev/block/platform/dw_mmc.0/by-name/recovery"),
            new File("/dev/block/platform/dw_mmc.0/by-name/RECOVERY"),
            new File("/dev/block/platform/hi_mci.1/by-name/recovery"),
            new File("/dev/block/platform/dw_mmc/by-name/recovery"),
            new File("/dev/block/platform/dw_mmc/by-name/RECOVERY"),
            new File("/dev/block/recovery"),
            new File("/dev/block/nandg")
    };

    private File CWM_IMG = null;
    private File TWRP_IMG = null;
    private String CWM_VERSION = "";
    private String TWRP_VERSION = "";
    private String EXT = ".img";
    private boolean KERNEL_TO = false;
    private boolean TWRP = false;
    private boolean OTHER = false;
    private boolean CWM = false;
    private boolean TWRP_OFFICIAL = false;
    private boolean CWM_OFFICIAL = false;

    private File flash_image = new File("/system/bin", "flash_image");
    private File dump_image = new File("/system/bin", "dump_image");

    public DeviceHandler() {
        setPredefinedOptions();
    }

    public DeviceHandler(String CustomDevice) {
        if (BuildConfig.DEBUG && !CustomDevice.equals(""))
            DEV_NAME = CustomDevice;
        setPredefinedOptions();
    }

    private void setPredefinedOptions() {

        String BOARD = Build.BOARD.toLowerCase();
        String MODEL = Build.MODEL.toLowerCase();

//	Set DEV_NAME predefined options

//      LG Optimus L7
        if (MODEL.equals("lg-p710")
                || DEV_NAME.equals("vee7e"))
            DEV_NAME = "p710";

//      Acer Iconia Tab A500
        if (DEV_NAME.equals("a500"))
            DEV_NAME = "picasso";

//      Motorola DROID RAZR M
        if (DEV_NAME.equals("xt907"))
            DEV_NAME = "scorpion_mini";

//      ASUS PadFone
        if (DEV_NAME.equals("padfone"))
            DEV_NAME = "a66";

//      HTC Fireball
        if (DEV_NAME.equals("valentewx"))
            DEV_NAME = "fireball";

//      LG Optimus GX2
        if (BOARD.equals("p990"))
            DEV_NAME = "p990";

//      Motorola Photon Q 4G LTE
        if (DEV_NAME.equals("xt897c")
                || BOARD.equals("xt897"))
            DEV_NAME = "xt897";

//      Motorola Atrix HD
        if (DEV_NAME.equals("mb886")
                || MODEL.equals("mb886"))
            DEV_NAME = "qinara";

//      LG Optimus G International
        if (BOARD.equals("geehrc"))
            DEV_NAME = "e975";

//      LG Optimus G
        if (BOARD.equals("geefhd"))
            DEV_NAME = "e988";

//      Motorola DROID4
        if (DEV_NAME.equals("cdma_maserati")
                || BOARD.equals("maserati"))
            DEV_NAME = "maserati";

//      LG Spectrum 4G (vs920)
        if (DEV_NAME.equals("d1lv")
                || BOARD.equals("d1lv"))
            DEV_NAME = "vs930";

//      Motorola Droid 2 WE
        if (DEV_NAME.equals("cdma_droid2we"))
            DEV_NAME = "droid2we";

//      OPPO Find 5
        if (DEV_NAME.equals("x909")
                || DEV_NAME.equals("x909t"))
            DEV_NAME = "find5";

//      Samsung Galaxy S +
        if (DEV_NAME.equals("gt-i9001")
                || BOARD.equals("gt-i9001")
                || MODEL.equals("gt-i9001"))
            DEV_NAME = "galaxysplus";

//      Samsung Galaxy Tab 7 Plus
        if (DEV_NAME.equals("gt-p6200"))
            DEV_NAME = "p6200";

//      Samsung Galaxy Note 8.0
        if (MODEL.equals("gt-n5110"))
            DEV_NAME = "konawifi";

//		Kindle Fire HD 7"
        if (DEV_NAME.equals("d01e"))
            DEV_NAME = "kfhd7";

        if (BOARD.equals("rk29sdk"))
            DEV_NAME = "rk29sdk";

//      HTC ONE GSM
        if (BOARD.equals("m7")
                || DEV_NAME.equals("m7")
                || DEV_NAME.equals("m7ul"))
            DEV_NAME = "m7";

        if (DEV_NAME.equals("m7spr"))
            DEV_NAME = "m7wls";

//      Samsung Galaxy S4 (i9505/jflte)
        if (DEV_NAME.equals("jflte"))
            DEV_NAME = "jfltexx";

//		Galaxy Note
        if (DEV_NAME.equals("gt-n7000")
                || DEV_NAME.equals("n7000")
                || DEV_NAME.equals("galaxynote")
                || DEV_NAME.equals("n7000")
                || BOARD.equals("gt-n7000")
                || BOARD.equals("n7000")
                || BOARD.equals("galaxynote")
                || BOARD.equals("N7000"))
            DEV_NAME = "n7000";

        if (DEV_NAME.equals("p4noterf")
                || MODEL.equals("gt-n8000"))
            DEV_NAME = "n8000";

//      Samsung Galaxy Note 10.1
        if (MODEL.equals("gt-n8013")
                || DEV_NAME.equals("p4notewifi"))
            DEV_NAME = "n8013";

//      Samsung Galaxy Tab 2
        if (BOARD.equals("piranha")
                || MODEL.equals("gt-p3113"))
            DEV_NAME = "p3113";

        if (DEV_NAME.equals("espressowifi")
                || MODEL.equals("gt-p3110"))
            DEV_NAME = "p3110";

//		Galaxy Note 2
        if (DEV_NAME.equals("n7100")
                || DEV_NAME.equals("n7100")
                || DEV_NAME.equals("gt-n7100")
                || MODEL.equals("gt-n7100")
                || BOARD.equals("t03g")
                || BOARD.equals("n7100")
                || BOARD.equals("gt-n7100"))
            DEV_NAME = "t03g";

//		Galaxy Note 2 LTE
        if (DEV_NAME.equals("t0ltexx")
                || DEV_NAME.equals("gt-n7105")
                || DEV_NAME.equals("t0ltedv")
                || DEV_NAME.equals("gt-n7105T")
                || DEV_NAME.equals("t0ltevl")
                || DEV_NAME.equals("sgh-I317m")
                || BOARD.equals("t0ltexx")
                || BOARD.equals("gt-n7105")
                || BOARD.equals("t0ltedv")
                || BOARD.equals("gt-n7105T")
                || BOARD.equals("t0ltevl")
                || BOARD.equals("sgh-i317m"))
            DEV_NAME = "t0lte";

        if (DEV_NAME.equals("sgh-i317")
                || BOARD.equals("t0lteatt")
                || BOARD.equals("sgh-i317"))
            DEV_NAME = "t0lteatt";

        if (DEV_NAME.equals("sgh-t889")
                || BOARD.equals("t0ltetmo")
                || BOARD.equals("sgh-t889"))
            DEV_NAME = "t0ltetmo";

        if (BOARD.equals("t0ltecan"))
            DEV_NAME = "t0ltecan";

//		Galaxy S3 (international)
        if (DEV_NAME.equals("gt-i9300")
                || DEV_NAME.equals("galaxy s3")
                || DEV_NAME.equals("galaxys3")
                || DEV_NAME.equals("m0")
                || DEV_NAME.equals("i9300")
                || BOARD.equals("gt-i9300")
                || BOARD.equals("m0")
                || BOARD.equals("i9300"))
            DEV_NAME = "i9300";

//		Galaxy S2
        if (DEV_NAME.equals("gt-i9100g")
                || DEV_NAME.equals("gt-i9100m")
                || DEV_NAME.equals("gt-i9100p")
                || DEV_NAME.equals("gt-i9100")
                || DEV_NAME.equals("galaxys2")
                || BOARD.equals("gt-i9100g")
                || BOARD.equals("gt-i9100m")
                || BOARD.equals("gt-I9100p")
                || BOARD.equals("gt-i9100")
                || BOARD.equals("galaxys2"))
            DEV_NAME = "galaxys2";

//		Galaxy S2 ATT
        if (DEV_NAME.equals("sgh-i777")
                || BOARD.equals("sgh-i777")
                || BOARD.equals("galaxys2att"))
            DEV_NAME = "galaxys2att";

//		Galaxy S2 LTE (skyrocket)
        if (DEV_NAME.equals("sgh-i727")
                || BOARD.equals("skyrocket")
                || BOARD.equals("sgh-i727"))
            DEV_NAME = "skyrocket";

//      Galaxy S (i9000)
        if (DEV_NAME.equals("galaxys")
                || DEV_NAME.equals("galaxysmtd")
                || DEV_NAME.equals("gt-i9000")
                || DEV_NAME.equals("gt-i9000m")
                || DEV_NAME.equals("gt-i9000t")
                || BOARD.equals("galaxys")
                || BOARD.equals("galaxysmtd")
                || BOARD.equals("gt-i9000")
                || BOARD.equals("gt-i9000m")
                || BOARD.equals("gt-i9000t")
                || MODEL.equals("gt-i9000t")
                || DEV_NAME.equals("sph-d710")
                || DEV_NAME.equals("sph-d710bst")
                || MODEL.equals("sph-d710bst"))
            DEV_NAME = "galaxys";

//      Samsung Galaxy Note
        if (DEV_NAME.equals("gt-n7000b"))
            DEV_NAME = "n7000";

//		GalaxyS Captivate (SGH-I897)
        if (DEV_NAME.equals("sgh-i897")) {
            DEV_NAME = ("captivate");
        }

        if (BOARD.equals("gee")) {
            DEV_NAME = "geeb";
        }

//		Sony Xperia Z (C6603)
        if (DEV_NAME.equals("c6603")
                || DEV_NAME.equals("yuga")) {
            DEV_NAME = "c6603";
        }

        if (DEV_NAME.equals("c6603")
                || DEV_NAME.equals("c6602"))
            EXT = ".tar";

//		Sony Xperia S
        if (DEV_NAME.equals("lt26i"))
            DEV_NAME = "nozomi";

//		Sony Xperia T
        if (DEV_NAME.equals("lt30p"))
            DEV_NAME = "mint";

//      HTC Desire HD
        if (BOARD.equals("ace"))
            DEV_NAME = "ace";

//      Motorola Droid X
        if (DEV_NAME.equals("cdma_shadow")
                || BOARD.equals("shadow")
                || MODEL.equals("droidx"))
            DEV_NAME = "shadow";

//      LG Optimus L9
        if (DEV_NAME.equals("u2")
                || BOARD.equals("u2")
                || MODEL.equals("lg-p760"))
            DEV_NAME = "p760";

//      LG Optimus L5
        if (DEV_NAME.equals("m4")
                || MODEL.equals("lg-e610"))
            DEV_NAME = "e610";

//      Huawei U9508
        if (BOARD.equals("u9508")
                || DEV_NAME.equals("hwu9508"))
            DEV_NAME = "u9508";

//      Huawei Ascend P1
        if (DEV_NAME.equals("hwu9200")
                || BOARD.equals("u9200")
                || MODEL.equals("u9200"))
            DEV_NAME = "u9200";

//      Motorola RAZR
        if (DEV_NAME.equals("cdma_yangtze")
                || BOARD.equals("yangtze"))
            DEV_NAME = "yangtze";

//      Motorola Droid RAZR
        if (DEV_NAME.equals("cdma_spyder")
                || BOARD.equals("spyder"))
            DEV_NAME = "spyder";

//      Huawei M835
        if (DEV_NAME.equals("hwm835")
                || BOARD.equals("m835"))
            DEV_NAME = "m835";

//      LG Optimus Black
        if (DEV_NAME.equals("bproj_cis-xxx")
                || BOARD.equals("bproj")
                || MODEL.equals("lg-p970"))
            DEV_NAME = "p970";

//      LG Optimus X2
        if (DEV_NAME.equals("star"))
            DEV_NAME = "p990";

        if (DEV_NAME.equals("droid2")
                || DEV_NAME.equals("daytona")
                || DEV_NAME.equals("captivate")
                || DEV_NAME.equals("galaxys")
                || DEV_NAME.equals("galaxys2att")
                || DEV_NAME.equals("galaxys2")
                || DEV_NAME.equals("n7000")
                || DEV_NAME.equals("droid2we")) {
            DEV_TYPE = DEV_TYPE_RECOVERY;
            EXT = ".zip";
        }

        if (!getRecoveryPath().equals(""))
            DEV_TYPE = DEV_TYPE_DD;

//		Devices who kernel will be flashed to
        if (DEV_NAME.equals("nozomi")
                || DEV_NAME.equals("mint")) {
            KERNEL_TO = true;
            DEV_TYPE = DEV_TYPE_SONY;
        }

        if (new File("/dev/mtd/").exists() && DEV_TYPE != DEV_TYPE_DD)
            DEV_TYPE = DEV_TYPE_MTD;
    }

    public void getSupportedSystems() {

        if (CWM_VERSION.equals(""))
            getCWMVersion();
        if (TWRP_VERSION.equals(""))
            getTWRPVersion();
        if (RecoveryPath.equals(""))
            getRecoveryPath();
        if (CWM_OFFICIAL
                || DEV_NAME.equals("c6602")
                || DEV_NAME.equals("c6603")
                || DEV_NAME.equals("jena"))
            CWM = true;

        if (TWRP_OFFICIAL
                || DEV_NAME.equals("c6603")
                || DEV_NAME.equals("jena")
                || DEV_NAME.equals("kfhd7"))
            TWRP = true;

        if (getDevType() == DEV_TYPE_DD
                || isOverRecovery()
                || isMTD()
                || CWM
                || TWRP)
            OTHER = true;
    }

    public String getCWMVersion() {
        if (CWM_VERSION.equals("")) {
//		    CLOCKWORKMOD touch supported devices
            if (DEV_NAME.equals("ace")
                    || DEV_NAME.equals("crespo")
                    || DEV_NAME.equals("crespo4g")
                    || DEV_NAME.equals("d2att")
                    || DEV_NAME.equals("d2tmo")
                    || DEV_NAME.equals("endeavoru")
                    || DEV_NAME.equals("evita")
                    || DEV_NAME.equals("fireball")
                    || DEV_NAME.equals("galaxys2")
                    || DEV_NAME.equals("golden")
                    || DEV_NAME.equals("grouper")
                    || DEV_NAME.equals("i9300")
                    || DEV_NAME.equals("maguro")
                    || DEV_NAME.equals("mako")
                    || DEV_NAME.equals("manta")
                    || DEV_NAME.equals("t03g")
                    || DEV_NAME.equals("pyramid")
                    || DEV_NAME.equals("saga")
                    || DEV_NAME.equals("skyrocket")
                    || DEV_NAME.equals("tilapia")
                    || DEV_NAME.equals("toro")
                    || DEV_NAME.equals("toroplus")
                    || DEV_NAME.equals("ville")
                    || DEV_NAME.equals("warp2")
                    || DEV_NAME.equals("p990")
                    || DEV_NAME.equals("tf700t")
                    || DEV_NAME.equals("dlx")
                    || DEV_NAME.equals("jflte")
                    || DEV_NAME.equals("d2spr")
                    || DEV_NAME.equals("supersonic")
                    || DEV_NAME.equals("olympus")
                    || DEV_NAME.equals("m7spr")
                    || DEV_NAME.equals("jfltespr")
                    || DEV_NAME.equals("jewel")
                    || DEV_NAME.equals("shooter")
                    || DEV_NAME.equals("jfltevzw")
                    || DEV_NAME.equals("p970")
                    || DEV_NAME.equals("p760")
                    || DEV_NAME.equals("jfltecan")
                    || DEV_NAME.equals("jfltexx")
                    || DEV_NAME.equals("jfltespr")
                    || DEV_NAME.equals("m7")
                    || DEV_NAME.equals("m7wls")
                    || DEV_NAME.equals("jfltevzw")
                    || DEV_NAME.equals("p880")
                    || DEV_NAME.equals("n8013")
                    || DEV_NAME.equals("jfltetmo")
                    || DEV_NAME.equals("p3113")
                    || DEV_NAME.equals("d2usc")
                    || DEV_NAME.equals("bravo")
                    || DEV_NAME.equals("find5")
                    || DEV_NAME.equals("jflteatt")
                    || DEV_NAME.equals("jflteusc")
                    || DEV_NAME.equals("p3110")
                    || DEV_NAME.equals("stingray")
                    || DEV_NAME.equals("wingray")
                    || DEV_NAME.equals("dlxj")
                    || DEV_NAME.equals("n8000")
                    || DEV_NAME.equals("p920")
                    || DEV_NAME.equals("t0lte")
                    || DEV_NAME.equals("shooteru")
                    || DEV_NAME.equals("e975")
                    || DEV_NAME.equals("glacier")
                    || DEV_NAME.equals("vigor")
                    || DEV_NAME.equals("runnymede")
                    || DEV_NAME.equals("flo")
                    || DEV_NAME.equals("protou")
                    || DEV_NAME.equals("tf101"))
                CWM_VERSION = "-touch";

//	        Newest Clockworkmod version for devices
            if (DEV_NAME.equals("sholes"))
                CWM_VERSION = "-2.5.0.1";

            if (DEV_NAME.equals("heroc"))
                CWM_VERSION = "-2.5.0.7";

            if (DEV_NAME.equals("sgh-i897")
                    || DEV_NAME.equals("galaxys")
                    || DEV_NAME.equals("captivate"))
                CWM_VERSION = "-2.5.1.2";

            if (DEV_NAME.equals("leo"))
                CWM_VERSION = "-3.1.0.0";

            if (DEV_NAME.equals("droid2")
                    || DEV_NAME.equals("vivo")
                    || DEV_NAME.equals("vivow")
                    || DEV_NAME.equals("blade")
                    || DEV_NAME.equals("shadow")
                    || DEV_NAME.equals("p999")
                    || DEV_NAME.equals("thunderg"))
                CWM_VERSION = "-5.0.2.0";

            if (DEV_NAME.equals("droid2we"))
                CWM_VERSION = "-5.0.2.3";

            if (DEV_NAME.equals("daytona"))
                CWM_VERSION = "-5.0.2.5";

            if (DEV_NAME.equals("holiday"))
                CWM_VERSION = "-5.0.2.7";

            if (DEV_NAME.equals("pico"))
                CWM_VERSION = "-5.0.2.8";

            if (DEV_NAME.equals("supersonic"))
                CWM_VERSION = CWM_VERSION + "-5.8.0.1";

            if (DEV_NAME.equals("shooter")
                    || DEV_NAME.equals("bravo")
                    || DEV_NAME.equals("shooteru"))
                CWM_VERSION = CWM_VERSION + "-5.8.0.2";

            if (DEV_NAME.equals("pyramid"))
                CWM_VERSION = CWM_VERSION + "-5.8.0.9";

            if (DEV_NAME.equals("glacier"))
                CWM_VERSION = CWM_VERSION + "-5.8.1.0";

            if (DEV_NAME.equals("ace")
                    || DEV_NAME.equals("saga")
                    || DEV_NAME.equals("galaxys2")
                    || DEV_NAME.equals("olympus"))
                CWM_VERSION = CWM_VERSION + "-5.8.1.5";

            if (DEV_NAME.equals("tf201")
                    || DEV_NAME.equals("tf101"))
                CWM_VERSION = CWM_VERSION + "-5.8.3.4";

            if (DEV_NAME.equals("jewel"))
                CWM_VERSION = CWM_VERSION + "-5.8.3.5";

            if (DEV_NAME.equals("endeavoru"))
                CWM_VERSION = CWM_VERSION + "-5.8.4.0";

            if (DEV_NAME.equals("primou"))
                CWM_VERSION = CWM_VERSION + "-5.8.4.5";

            if (DEV_NAME.equals("dummy"))
                CWM_VERSION = CWM_VERSION + "-6.0.1.1";

            if (DEV_NAME.equals("p970"))
                CWM_VERSION = CWM_VERSION + "-6.0.1.4";

            if (DEV_NAME.equals("p920"))
                CWM_VERSION = CWM_VERSION + "-6.0.1.9";

            if (DEV_NAME.equals("p3113"))
                CWM_VERSION = CWM_VERSION + "-6.0.2.3";

            if (DEV_NAME.equals("golden")
                    || DEV_NAME.equals("warp2")
                    || DEV_NAME.equals("p3110")
                    || DEV_NAME.equals("runnymede"))
                CWM_VERSION = CWM_VERSION + "-6.0.2.7";

            if (DEV_NAME.equals("e610"))
                CWM_VERSION = CWM_VERSION + "-6.0.2.8";

            if (DEV_NAME.equals("e975"))
                CWM_VERSION = "-6.0.3.0";

            if (DEV_NAME.equals("dlxub1")
                    || DEV_NAME.equals("evita")
                    || DEV_NAME.equals("skyrocket")
                    || DEV_NAME.equals("ville")
                    || DEV_NAME.equals("p990")
                    || DEV_NAME.equals("tf700t")
                    || DEV_NAME.equals("m7")
                    || DEV_NAME.equals("dlx")
                    || DEV_NAME.equals("p760")
                    || DEV_NAME.equals("p880")
                    || DEV_NAME.equals("t0ltespr")
                    || DEV_NAME.equals("stingray")
                    || DEV_NAME.equals("wingray")
                    || DEV_NAME.equals("dlxj")
                    || DEV_NAME.equals("vigor"))
                CWM_VERSION = CWM_VERSION + "-6.0.3.1";

            if (DEV_NAME.equals("m7wls")
                    || DEV_NAME.equals("protou"))
                CWM_VERSION = CWM_VERSION + "-6.0.3.2";

            if (DEV_NAME.equals("fireball"))
                CWM_VERSION = CWM_VERSION + "-6.0.3.3";

            if (DEV_NAME.equals("n8000")
                    || DEV_NAME.equals("n8013")
                    || DEV_NAME.equals("i9300"))
                CWM_VERSION = CWM_VERSION + "-6.0.3.6";

            if (DEV_NAME.equals("find5"))
                CWM_VERSION = CWM_VERSION + "-6.0.3.7";

            if (DEV_NAME.equals("t0lte"))
                CWM_VERSION = CWM_VERSION + "-6.0.4.1";

            if (DEV_NAME.equals("n7000")
                    || DEV_NAME.equals("t0ltevzw")
                    || DEV_NAME.equals("t0ltetmo")
                    || DEV_NAME.equals("jfltevzw")
                    || DEV_NAME.equals("jfltexx")
                    || DEV_NAME.equals("jfltespr")
                    || DEV_NAME.equals("crespo")
                    || DEV_NAME.equals("crespo4g")
                    || DEV_NAME.equals("grouper")
                    || DEV_NAME.equals("maguro")
                    || DEV_NAME.equals("mako")
                    || DEV_NAME.equals("manta")
                    || DEV_NAME.equals("tilapia")
                    || DEV_NAME.equals("flo")
                    || DEV_NAME.equals("toro")
                    || DEV_NAME.equals("toroplus")
                    || DEV_NAME.equals("d2spr")
                    || DEV_NAME.equals("d2usc")
                    || DEV_NAME.equals("t03g"))
                CWM_VERSION = CWM_VERSION + "-6.0.4.3";

        }
        if (!CWM_VERSION.equals(""))
            CWM_OFFICIAL = true;
        else
            return "-cwm";
        return CWM_VERSION;
    }

    public String getTWRPVersion() {
        if (TWRP_VERSION.equals("")) {
            if (DEV_NAME.equals("thunderg"))
                TWRP_VERSION = "-2.0.0alpha1";

            if (DEV_NAME.equals("leo"))
                TWRP_VERSION = "-2.2.0";

            if (DEV_NAME.equals("tf101"))
                TWRP_VERSION = "-2.3.2.3";

            if (DEV_NAME.equals("pico"))
                TWRP_VERSION = "-2.4.4.0";

            if (DEV_NAME.equals("passion")
                    || DEV_NAME.equals("geeb")
                    || DEV_NAME.equals("nozomi")
                    || DEV_NAME.equals("mint")
                    || DEV_NAME.equals("enrc2b"))
                TWRP_VERSION = "-2.6.0.0";

            if (DEV_NAME.equals("n8000")
                    || DEV_NAME.equals("heroc")
                    || DEV_NAME.equals("p970"))
                TWRP_VERSION = "-2.6.1.0";

            if (DEV_NAME.equals("picasso")
                    || DEV_NAME.equals("crespo")
                    || DEV_NAME.equals("crespo4g")
                    || DEV_NAME.equals("maguro")
                    || DEV_NAME.equals("toro")
                    || DEV_NAME.equals("toroplus")
                    || DEV_NAME.equals("mako")
                    || DEV_NAME.equals("manta")
                    || DEV_NAME.equals("grouper")
                    || DEV_NAME.equals("tilapia")
                    || DEV_NAME.equals("flo")
                    || DEV_NAME.equals("deb")
                    || DEV_NAME.equals("tf700t")
                    || DEV_NAME.equals("p990")
                    || DEV_NAME.equals("runnymede")
                    || DEV_NAME.equals("a66")
                    || DEV_NAME.equals("m7")
                    || DEV_NAME.equals("m7wls")
                    || DEV_NAME.equals("dlxub1")
                    || DEV_NAME.equals("dlx")
                    || DEV_NAME.equals("ville")
                    || DEV_NAME.equals("villec2")
                    || DEV_NAME.equals("endeavoru")
                    || DEV_NAME.equals("evita")
                    || DEV_NAME.equals("shooteru")
                    || DEV_NAME.equals("shooter")
                    || DEV_NAME.equals("holiday")
                    || DEV_NAME.equals("ace")
                    || DEV_NAME.equals("saga")
                    || DEV_NAME.equals("pyramid")
                    || DEV_NAME.equals("fireball")
                    || DEV_NAME.equals("vivo")
                    || DEV_NAME.equals("vivow")
                    || DEV_NAME.equals("supersonic")
                    || DEV_NAME.equals("jewel")
                    || DEV_NAME.equals("primou")
                    || DEV_NAME.equals("olympus")
                    || DEV_NAME.equals("find5")
                    || DEV_NAME.equals("jflteatt")
                    || DEV_NAME.equals("jfltespi")
                    || DEV_NAME.equals("jfltecan")
                    || DEV_NAME.equals("jfltecri")
                    || DEV_NAME.equals("jfltexx")
                    || DEV_NAME.equals("jfltespr")
                    || DEV_NAME.equals("jfltetmo")
                    || DEV_NAME.equals("jflteusc")
                    || DEV_NAME.equals("jfltevzw")
                    || DEV_NAME.equals("i9500")
                    || DEV_NAME.equals("skyrocket")
                    || DEV_NAME.equals("n8013")
                    || DEV_NAME.equals("t0lte")
                    || DEV_NAME.equals("t0lteatt")
                    || DEV_NAME.equals("t0ltecan")
                    || DEV_NAME.equals("t0ltektt")
                    || DEV_NAME.equals("t0lteskt")
                    || DEV_NAME.equals("t0ltespr")
                    || DEV_NAME.equals("t0lteusc")
                    || DEV_NAME.equals("t0ltevzw")
                    || DEV_NAME.equals("t0lteatt")
                    || DEV_NAME.equals("t0ltetmo")
                    || DEV_NAME.equals("d2att")
                    || DEV_NAME.equals("d2tmo")
                    || DEV_NAME.equals("d2mtr")
                    || DEV_NAME.equals("d2vzw")
                    || DEV_NAME.equals("d2spr")
                    || DEV_NAME.equals("d2usc")
                    || DEV_NAME.equals("d2can")
                    || DEV_NAME.equals("d2cri")
                    || DEV_NAME.equals("i9300")
                    || DEV_NAME.equals("golden")
                    || DEV_NAME.equals("p999")
                    || DEV_NAME.equals("a68")
                    || DEV_NAME.equals("golfu")
                    || DEV_NAME.equals("jgedlte")
                    || DEV_NAME.equals("protou")
                    || DEV_NAME.equals("evitareul")
                    || DEV_NAME.equals("crater")
                    || DEV_NAME.equals("tf201")
                    || DEV_NAME.equals("t03g"))
                TWRP_VERSION = "-2.6.3.0";

            if (DEV_NAME.equals("hammerhead"))
                TWRP_VERSION = "-2.6.3.2";
        }
        if (!TWRP_VERSION.equals(""))
            TWRP_OFFICIAL = true;
        else
            return "-twrp";
        return TWRP_VERSION;
    }

    public boolean downloadUtils(final Context mContext) {

        final File archive = new File(RecoveryTools.PathToUtils, DEV_NAME + ".zip");

        final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(mContext);
        mAlertDialog
                .setTitle(R.string.warning)
                .setMessage(R.string.download_utils);
        if (DEV_NAME.equals("montblanc") || DEV_NAME.equals("c6602") || DEV_NAME.equals("c6603") && !archive.exists()) {
            mAlertDialog.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    new Downloader(mContext, "http://dslnexus.org/Android/utils/", archive.getName(), archive, new Runnable() {
                        @Override
                        public void run() {
                            new Unzipper(archive, new File(RecoveryTools.PathToUtils, DEV_NAME)).unzip();
                        }
                    }).execute();

                }
            });
            mAlertDialog.show();
            return true;
        }
        if (archive.exists())
            new Unzipper(archive, new File(RecoveryTools.PathToUtils, DEV_NAME)).unzip();
        return false;
    }

    public File getFlash_image(Context mContext) {
        if (!flash_image.exists()) {
            flash_image = new File(mContext.getFilesDir(), flash_image.getName());
        }
        return flash_image;
    }

    public File getDump_image(Context mContext) {
        if (!dump_image.exists()) {
            dump_image = new File(mContext.getFilesDir(), dump_image.getName());
        }
        return dump_image;
    }

    public boolean isTwrpSupported() {
        getSupportedSystems();
        return TWRP;
    }

    public boolean isCwmSupported() {
        getSupportedSystems();
        return CWM;
    }

    public boolean isOtherSupported() {
        getSupportedSystems();
        return OTHER;
    }

    public int getDevType() {
        return DEV_TYPE;
    }

    public boolean isMTD() {
        return getDevType() == DEV_TYPE_MTD;
    }

    public boolean isDD() {
        return getDevType() == DEV_TYPE_DD;
    }

    public boolean isOverRecovery() {
        return getDevType() == DEV_TYPE_RECOVERY;
    }

    public boolean isKernelFlashed() {
        return KERNEL_TO;
    }

    public File getCWM_IMG() {
        if (CWM_IMG == null)
            if (CWM_OFFICIAL) {
                CWM_IMG = new File(RecoveryTools.PathToCWM, "recovery-clockwork" + getCWMVersion() + "-" + DEV_NAME + EXT);
            } else {
                CWM_IMG = new File(RecoveryTools.PathToCWM, DEV_NAME + getCWMVersion() + EXT);
            }
        return CWM_IMG;
    }

    public File getTWRP_IMG() {
        if (TWRP_IMG == null)
            if (TWRP_OFFICIAL) {
                String IMG_NAME = "openrecovery-twrp" + getTWRPVersion() + "-" + DEV_NAME + EXT;
                if (DEV_NAME.equals("tf201")) {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH
                            || Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                        IMG_NAME = IMG_NAME + "-ICS";
                    } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                        IMG_NAME = IMG_NAME + "-JB";
                    }
                }
                TWRP_IMG = new File(RecoveryTools.PathToTWRP, IMG_NAME);
            } else {
                TWRP_IMG = new File(RecoveryTools.PathToTWRP, DEV_NAME + getTWRPVersion() + EXT);
            }
        return TWRP_IMG;
    }

    public String getRecoveryPath() {
        if (RecoveryPath.equals("")) {
            for (File i : RecoveryList) {
                if (i.exists()) {
                    return i.getAbsolutePath();
                }
            }

//          ASUS DEVICEs + Same
            if (DEV_NAME.equals("a66"))
                return "/dev/block/mmcblk0p15";

//		    Samsung DEVICEs + Same
            if (DEV_NAME.equals("d2att")
                    || DEV_NAME.equals("d2tmo")
                    || DEV_NAME.equals("d2mtr")
                    || DEV_NAME.equals("d2vzw")
                    || DEV_NAME.equals("d2spr")
                    || DEV_NAME.equals("d2usc")
                    || DEV_NAME.equals("d2can")
                    || DEV_NAME.equals("d2cri")
                    || DEV_NAME.equals("d2vmu")
                    || DEV_NAME.equals("sch-i929")
                    || DEV_NAME.equals("e6710")
                    || DEV_NAME.equals("expresslte")
                    || DEV_NAME.equals("goghcri")
                    || DEV_NAME.equals("p710"))
                return "/dev/block/mmcblk0p18";

            if (DEV_NAME.equals("i9300")
                    || DEV_NAME.equals("galaxys2")
                    || DEV_NAME.equals("n8013")
                    || DEV_NAME.equals("p3113")
                    || DEV_NAME.equals("p3110")
                    || DEV_NAME.equals("p6200")
                    || DEV_NAME.equals("n8000")
                    || DEV_NAME.equals("sph-d710vmub")
                    || DEV_NAME.equals("p920")
                    || DEV_NAME.equals("konawifi")
                    || DEV_NAME.equals("t03gctc")
                    || DEV_NAME.equals("cosmopolitan")
                    || DEV_NAME.equals("s2vep")
                    || DEV_NAME.equals("gt-p6810")
                    || DEV_NAME.equals("baffin")
                    || DEV_NAME.equals("ivoryss")
                    || DEV_NAME.equals("crater"))
                return "/dev/block/mmcblk0p6";

            if (DEV_NAME.equals("t03g")
                    || DEV_NAME.equals("tf700t")
                    || DEV_NAME.equals("t0lte")
                    || DEV_NAME.equals("t0lteatt")
                    || DEV_NAME.equals("t0ltecan")
                    || DEV_NAME.equals("t0ltektt")
                    || DEV_NAME.equals("t0lteskt")
                    || DEV_NAME.equals("t0ltespr")
                    || DEV_NAME.equals("t0lteusc")
                    || DEV_NAME.equals("t0ltevzw")
                    || DEV_NAME.equals("t0lteatt")
                    || DEV_NAME.equals("t0ltetmo")
                    || DEV_NAME.equals("m3")
                    || DEV_NAME.equals("otter2")
                    || DEV_NAME.equals("p4notelte"))
                return "/dev/block/mmcblk0p9";

            if (DEV_NAME.equals("golden")
                    || DEV_NAME.equals("villec2")
                    || DEV_NAME.equals("vivo")
                    || DEV_NAME.equals("vivow")
                    || DEV_NAME.equals("kingdom")
                    || DEV_NAME.equals("vision")
                    || DEV_NAME.equals("mystul")
                    || DEV_NAME.equals("jflteatt")
                    || DEV_NAME.equals("jfltespi")
                    || DEV_NAME.equals("jfltecan")
                    || DEV_NAME.equals("jfltecri")
                    || DEV_NAME.equals("jfltexx")
                    || DEV_NAME.equals("jfltespr")
                    || DEV_NAME.equals("jfltetmo")
                    || DEV_NAME.equals("jflteusc")
                    || DEV_NAME.equals("jfltevzw")
                    || DEV_NAME.equals("i9500")
                    || DEV_NAME.equals("flyer")
                    || DEV_NAME.equals("saga")
                    || DEV_NAME.equals("shooteru")
                    || DEV_NAME.equals("golfu")
                    || DEV_NAME.equals("glacier")
                    || DEV_NAME.equals("runnymede")
                    || DEV_NAME.equals("protou"))
                return "/dev/block/mmcblk0p21";

            if (DEV_NAME.equals("jena"))
                return "/dev/block/mmcblk0p12";

            if (DEV_NAME.equals("GT-I9103")
                    || DEV_NAME.equals("mevlana"))
                return "/dev/block/mmcblk0p8";

//          LG DEVICEs + Same
            if (DEV_NAME.equals("e610")
                    || DEV_NAME.equals("fx3"))
                return "/dev/block/mmcblk0p17";

            if (DEV_NAME.equals("vs930")
                    || DEV_NAME.equals("l0")
                    || DEV_NAME.equals("ca201l")
                    || DEV_NAME.equals("ef49k")
                    || DEV_NAME.equals("ot-930")
                    || DEV_NAME.equals("fx1")
                    || DEV_NAME.equals("ef47s")
                    || DEV_NAME.equals("ef46l")
                    || DEV_NAME.equals("l1v"))
                return "/dev/block/mmcblk0p19";

//		    HTC DEVICEs + Same
            if (DEV_NAME.equals("holiday")
                    || DEV_NAME.equals("vigor")
                    || DEV_NAME.equals("a68"))
                return "/dev/block/mmcblk0p23";

            if (DEV_NAME.equals("m7")
                    || DEV_NAME.equals("obakem")
                    || DEV_NAME.equals("obake")
                    || DEV_NAME.equals("ovation"))
                return "/dev/block/mmcblk0p34";

            if (DEV_NAME.equals("m7wls"))
                return "/dev/block/mmcblk0p36";

            if (DEV_NAME.equals("endeavoru")
                    || DEV_NAME.equals("enrc2b")
                    || DEV_NAME.equals("p999")
                    || DEV_NAME.equals("us9230e1")
                    || DEV_NAME.equals("evitareul")
                    || DEV_NAME.equals("otter"))
                return "/dev/block/mmcblk0p5";

            if (DEV_NAME.equals("ace")
                    || DEV_NAME.equals("primou"))
                return "/dev/block/platform/msm_sdcc.2/mmcblk0p21";

            if (DEV_NAME.equals("pyramid"))
                return "/dev/block/platform/msm_sdcc.1/mmcblk0p21";

            if (DEV_NAME.equals("ville")
                    || DEV_NAME.equals("evita")
                    || DEV_NAME.equals("skyrocket")
                    || DEV_NAME.equals("fireball")
                    || DEV_NAME.equals("jewel")
                    || DEV_NAME.equals("shooter"))
                return "/dev/block/mmcblk0p22";

            if (DEV_NAME.equals("dlxub1")
                    || DEV_NAME.equals("dlx")
                    || DEV_NAME.equals("dlxj")
                    || DEV_NAME.equals("im-a840sp")
                    || DEV_NAME.equals("im-a840s")
                    || DEV_NAME.equals("taurus"))
                return "/dev/block/mmcblk0p20";

//		    Motorola DEVICEs + Same
            if (DEV_NAME.equals("qinara")
                    || DEV_NAME.equals("f02e")
                    || DEV_NAME.equals("vanquish_u")
                    || DEV_NAME.equals("xt897")
                    || DEV_NAME.equals("solstice")
                    || DEV_NAME.equals("smq_u"))
                return "/dev/block/mmcblk0p32";

            if (DEV_NAME.equals("pasteur"))
                return "/dev/block/mmcblk1p12";

            if (DEV_NAME.equals("dinara_td"))
                return "/dev/block/mmcblk1p14";

            if (DEV_NAME.equals("e975")
                    || DEV_NAME.equals("e988"))
                return "/dev/block/mmcblk0p28";

            if (DEV_NAME.equals("shadow")
                    || DEV_NAME.equals("edison")
                    || DEV_NAME.equals("venus2"))
                return "/dev/block/mmcblk1p16";

            if (DEV_NAME.equals("spyder")
                    || DEV_NAME.equals("maserati"))
                return "/dev/block/mmcblk1p15";

            if (DEV_NAME.equals("olympus")
                    || DEV_NAME.equals("ja3g")
                    || DEV_NAME.equals("ja3gchnduos")
                    || DEV_NAME.equals("daytona")
                    || DEV_NAME.equals("konalteatt")
                    || DEV_NAME.equals("lc1810")
                    || DEV_NAME.equals("lt02wifi")
                    || DEV_NAME.equals("lt013g"))
                return "/dev/block/mmcblk0p10";

//		    Sony DEVICEs + Same
            if (DEV_NAME.equals("nozomi"))
                return "/dev/block/mmcblk0p3";

            if (DEV_NAME.equals("c6603")
                    || DEV_NAME.equals("c6602"))
                return "/system/bin/recovery.tar";

//		    LG DEVICEs + Same
            if (DEV_NAME.equals("p990")
                    || DEV_NAME.equals("tf300t"))
                return "/dev/block/mmcblk0p7";

            if (DEV_NAME.equals("x3")
                    || DEV_NAME.equals("picasso")
                    || DEV_NAME.equals("picasso_m")
                    || DEV_NAME.equals("enterprise_ru"))
                return "/dev/block/mmcblk0p1";

            if (DEV_NAME.equals("m3s")
                    || DEV_NAME.equals("bryce")
                    || DEV_NAME.equals("melius3g")
                    || DEV_NAME.equals("meliuslte")
                    || DEV_NAME.equals("serranolte"))
                return "/dev/block/mmcblk0p14";

            if (DEV_NAME.equals("p970")
                    || DEV_NAME.equals("mint")
                    || DEV_NAME.equals("u2")
                    || DEV_NAME.equals("p760")
                    || DEV_NAME.equals("p768"))
                return "/dev/block/mmcblk0p4";

//		    ZTE DEVICEs + Same
            if (DEV_NAME.equals("warp2")
                    || DEV_NAME.equals("hwc8813")
                    || DEV_NAME.equals("galaxysplus")
                    || DEV_NAME.equals("cayman")
                    || DEV_NAME.equals("ancora_tmo")
                    || DEV_NAME.equals("c8812e")
                    || DEV_NAME.equals("batman_skt"))
                return "/dev/block/mmcblk0p13";

            if (DEV_NAME.equals("elden")
                    || DEV_NAME.equals("hayes")
                    || DEV_NAME.equals("quantum")
                    || DEV_NAME.equals("coeus"))
                return "/dev/block/mmcblk0p16";

            RecoveryPath = "";
        }
        return RecoveryPath;
    }

    public String getEXT() {
        return EXT;
    }

    public void extractFiles(Context mContext) {
        if (isMTD()) {
            try {
                File flash_image = getFlash_image(mContext);
                if (!flash_image.exists())
                    Common.pushFileFromRAW(mContext, flash_image, R.raw.flash_image);
                File dump_image = getDump_image(mContext);
                if (!dump_image.exists())
                    Common.pushFileFromRAW(mContext, dump_image, R.raw.dump_image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public File getRic() {
        return new File(RecoveryTools.PathToUtils, DEV_NAME + "/ric");
    }

    public File getCharger() {
        return new File(RecoveryTools.PathToUtils, DEV_NAME + "/charger");
    }

    public File getChargermon() {
        return new File(RecoveryTools.PathToUtils, DEV_NAME + "/chargermon");
    }
}