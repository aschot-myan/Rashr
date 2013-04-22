package de.mkrtchyan.recoverytools;


public class Support {
	
	public String Device = android.os.Build.DEVICE;
	public String RecoveryPath = "";
	public boolean MTD = false;
//	public boolean BLM = false;
	public boolean TWRP = true;
	public boolean CWM = true;
	public String EXT = ".img";

	public Support(){
		
		if (Device.equals("GT-N7000"))
			Device = "n7000";
		
		if (Device.equals("GT-I9300")
				|| Device.equals("GalaxyS3"))
			Device = "i9300";
		
		if (Device.equals("GT-S5369"))
			Device = "totoro";
		
		if (Device.equals("GT-I9100G")
				|| Device.equals("GT-I9100")) 
			Device = "galaxys2";
		
		if (Device.equals("crespo")
				|| Device.equals("crespo4g")
				|| Device.equals("passion")
				|| Device.equals("saga")
				|| Device.equals("swift"))
			MTD = true;
		
//		if (Device.equals("tass")
//				|| Device.equals("totoro")
//				|| Device.equals("grouper")){
//			BLM = true;
//			EXT = ".zip";
//		}
		
		RecoveryPath = getRecoveryPath();
	}
	
	private String getRecoveryPath(){
		
		String tmp = "";
		
//		Nexus Devices + Same
		
		if (Device.equals("maguro")
				|| Device.equals("toro")
				|| Device.equals("toroplus"))
			tmp = "/dev/block/platform/omap/omap_hsmmc.0/by-name/recovery";
		
		if (Device.equals("grouper") 
				|| Device.equals("endeavoru") 
				|| Device.equals("tilapia")) 
			tmp = "/dev/block/platform/sdhci-tegra.3/by-name/SOS";
		
		if (Device.equals("mako")
				|| Device.equals("geeb"))
			tmp = "/dev/block/platform/msm_sdcc.1/by-name/recovery";
		
		if (Device.equals("manta"))
			tmp = "/dev/block/platform/dw_mmc.0/by-name/recovery";
		
//		Samsung Devices + Same
		
		if (Device.equals("d2att")
			|| Device.equals("d2tmo")
			|| Device.equals("d2vzw"))
			tmp = "/dev/block/mmcblk0p18";
		
		if (Device.equals("i9300")
				|| Device.equals("galaxys2"))
			tmp = "/dev/block/mmcblk0p6";
		
		if (Device.equals("n7100")
				|| Device.equals("t0ltevzw"))
			tmp = "/dev/block/mmcblk0p9";
		
		if (Device.equals("golden") 
				|| Device.equals("villec2")) 
			tmp = "/dev/block/mmcblk0p21";
		
		if (Device.equals("n7000"))
			tmp = "/dev/block/platform/dw_mmc/by-name/RECOVERY";
		
		if (Device.equals("jena"))
			tmp = "/dev/block/mmcblk0p12";
		
//		HTC Devices + Same
		
		if (Device.equals("ace") 
				|| Device.equals("primou")) 
			tmp = "/dev/block/platform/msm_sdcc.2/mmcblk0p21";
		
		if (Device.equals("pyramid"))
			tmp = "/dev/block/platform/msm_sdcc.1/mmcblk0p21";
		
		if (Device.equals("ville"))
			tmp = "/dev/block/mmcblk0p22";
		
//		Motorola Devices + Same
		
		if (Device.equals("droid2"))
			tmp = "/dev/block/mmcblk1p16";
		
		return tmp;
	}
	
	public void getSupportedSystems(){
		if (Device.equals("galaxys2") 
				|| Device.equals("n7000")
				|| Device.equals("droid2")) 
			TWRP = false;
		if (Device.equals("")
				|| !MTD 
//				&& !BLM 
				&& RecoveryPath.equals("")) {
			TWRP = false;
			CWM = false;
		}
	}
}
