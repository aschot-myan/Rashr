package de.mkrtchyan.recoverytools;

public class Support {
	
	String Device = android.os.Build.DEVICE;
	String RecoveryPath = getRecoveryPath();
	public boolean MTD = false;

	public Support(){
		
		if (Device.equals("GT-N7000"))
			Device = "n7000";
		
		if (Device.equals("GT-I9300"))
			Device = "i9300";
		
		if (Device.equals("GT-I9100G")
				|| Device.equals("GT-I9100")) 
			Device = "galaxys2";
		
		if (Device.equals("crespo")
				|| Device.equals("crespo4g")
				|| Device.equals("passion"))
			MTD = true;
	}
	
	public String getRecoveryPath(){
		
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
			|| Device.equals("d2tmo"))
			tmp = "/dev/block/mmcblk0p18";
		
		if (Device.equals("i9300")
				|| Device.equals("galaxys2"))
			tmp = "/dev/block/mmcblk0p6";
		
		if (Device.equals("n7100"))
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
		
//		Motorola Devices + Same
		
		if (Device.equals("droid2"))
			tmp = "/dev/block/mmcblk1p16";
		
		return tmp;
	}
}
