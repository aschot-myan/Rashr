package de.mkrtchyan.recoverytools;


public class Support {
	
	public String DEVICE = android.os.Build.DEVICE;
	public String BOARD = android.os.Build.BOARD;
	public String RecoveryPath;
	public boolean KERNEL_TO = false;
	public boolean FLASH_OVER_RECOVERY = false;
	public boolean MTD = false;
	public boolean BLM = false;
	public boolean TWRP = true;
	public boolean CWM = true;
	public String EXT = ".img";

	public Support(){
		
//	Set DEVICE predefined options
		
//		Galaxy Note 
		if (DEVICE.equals("GT-N7000")
				|| DEVICE.equals("n7000")
				|| DEVICE.equals("galaxynote")
				|| DEVICE.equals("N7000")
				|| BOARD.equals("GT-N7000")
				|| BOARD.equals("n7000")
				|| BOARD.equals("galaxynote")
				|| BOARD.equals("N7000")){
			DEVICE = "n7000";
			FLASH_OVER_RECOVERY = true;
			EXT = ".zip";
		}
		
//		Galaxy Note 2
		if (DEVICE.equals("t03g")
				|| DEVICE.equals("n7100")
				|| DEVICE.equals("GT-N7100")
				|| BOARD.equals("t03g")
				|| BOARD.equals("n7100")
				|| BOARD.equals("GT-N7100"))
			DEVICE = "n7100";
			
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
		
//		if (DEVICE.equals("GT-S5369"))
//			DEVICE = "totoro";
		
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
				|| BOARD.equals("galaxys2")){
			FLASH_OVER_RECOVERY = true;
			EXT = ".zip";
			DEVICE = "galaxys2";
		}
		
//		Devices who kernel will be flashed to
		if (DEVICE.equals("nozomi")
				|| DEVICE.equals("mint")
				|| DEVICE.equals("LT30p")
				|| DEVICE.equals("LT26i"))
			KERNEL_TO = true;
		
//		Sony Xperia Z C6603
		
		if (DEVICE.equals("C6603"))
			EXT = ".tar";
			
		
		if (DEVICE.equals("crespo")
				|| DEVICE.equals("crespo4g")
				|| DEVICE.equals("passion")
				|| DEVICE.equals("saga")
				|| DEVICE.equals("swift")
				|| DEVICE.equals("geeb"))
			MTD = true;
		
		if (BOARD.equals("ace"))
			DEVICE = "ace";
		
//		if (DEVICE.equals("tass")
//				|| DEVICE.equals("totoro")){
//			BLM = true;
//			EXT = ".zip";
//		}
		
		RecoveryPath = getRecoveryPath();
		getSupportedSystems();
	}
	
	private String getRecoveryPath(){
		
		String tmp = "";
		
//		Nexus DEVICEs + Same
		
		if (DEVICE.equals("maguro")
				|| DEVICE.equals("toro")
				|| DEVICE.equals("toroplus"))
			tmp = "/dev/block/platform/omap/omap_hsmmc.0/by-name/recovery";
		
		if (DEVICE.equals("grouper") 
				|| DEVICE.equals("endeavoru") 
				|| DEVICE.equals("tilapia")) 
			tmp = "/dev/block/platform/sdhci-tegra.3/by-name/SOS";
		
		if (DEVICE.equals("mako"))
			tmp = "/dev/block/platform/msm_sdcc.1/by-name/recovery";
		
		if (DEVICE.equals("manta"))
			tmp = "/dev/block/platform/dw_mmc.0/by-name/recovery";
		
//		Samsung DEVICEs + Same
		
		if (DEVICE.equals("d2att")
			|| DEVICE.equals("d2tmo")
			|| DEVICE.equals("d2vzw"))
			tmp = "/dev/block/mmcblk0p18";
		
		if (DEVICE.equals("i9300")
				|| DEVICE.equals("galaxys2"))
			tmp = "/dev/block/mmcblk0p6";
		
		if (DEVICE.equals("n7100")
				|| DEVICE.equals("t0ltevzw"))
//				|| DEVICE.equals("m3"))
			tmp = "/dev/block/mmcblk0p9";
		
		if (DEVICE.equals("golden") 
				|| DEVICE.equals("villec2")) 
			tmp = "/dev/block/mmcblk0p21";
		
		if (DEVICE.equals("n7000"))
			tmp = "/dev/block/platform/dw_mmc/by-name/RECOVERY";
		
		if (DEVICE.equals("jena"))
			tmp = "/dev/block/mmcblk0p12";
		
//		HTC DEVICEs + Same
		
		if (DEVICE.equals("ace") 
				|| DEVICE.equals("primou")) 
			tmp = "/dev/block/platform/msm_sdcc.2/mmcblk0p21";
		
		if (DEVICE.equals("pyramid"))
			tmp = "/dev/block/platform/msm_sdcc.1/mmcblk0p21";
		
		if (DEVICE.equals("kingdom"))
			tmp = "/dev/block/mmcblk0p21";
		
		if (DEVICE.equals("ville"))
			tmp = "/dev/block/mmcblk0p22";
		
//		Motorola DEVICEs + Same
		
		if (DEVICE.equals("droid2"))
			tmp = "/dev/block/mmcblk1p16";
		
//		Sony DEVICEs + Same
		
		if (DEVICE.equals("nozomi")
				|| DEVICE.equals("LT26i")
				|| DEVICE.equals("mint")
				|| DEVICE.equals("LT30p"))
			tmp = "/dev/block/mmcblk0p11";
		
		if (DEVICE.equals("C6603"))
			tmp = "/system/bin/recovery.tar";
		
//		LG DEVICEs + Same
		
		if (DEVICE.equals("x3"))
			tmp = "/dev/block/mmcblk0p1";
	
//		ZTE DEVICEs + Same
		
		if (DEVICE.equals("warp2"))
			tmp = "/dev/block/mmcblk0p13";
			
		
		return tmp;
	}
	
	public void getSupportedSystems(){
		
		if (DEVICE.equals("galaxys2") 
				|| DEVICE.equals("n7000")
				|| DEVICE.equals("x3")
				|| DEVICE.equals("droid2")
				|| DEVICE.equals("kingdom")) 
			TWRP = false;
		
		if (DEVICE.equals("nozomi")
				|| DEVICE.equals("mint")
				|| DEVICE.equals("LT30p")
				|| DEVICE.equals("LT26i"))
			CWM = false;
			
		if (DEVICE.equals("")
				|| !MTD 
				&& !BLM 
				&& RecoveryPath.equals("")) {
			TWRP = false;
			CWM = false;
		}
	}
}
