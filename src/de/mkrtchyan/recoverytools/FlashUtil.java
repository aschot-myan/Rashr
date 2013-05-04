package de.mkrtchyan.recoverytools;

import java.io.File;

import android.content.Context;

public class FlashUtil {
	
	Context context;
	CommonUtil cu;
	Support s = new Support();
	private static File fflash;
	private static File fdump;
	
	public FlashUtil(Context context){
		context = this.context;
		cu = new CommonUtil(this.context);
		if (s.MTD){
			fflash = new File(context.getFilesDir(), "flash_image");
			fdump = new File(context.getFilesDir(), "dump_image");
			cu.pushFileFromRAW(fflash, R.raw.flash_image);
			cu.pushFileFromRAW(fdump, R.raw.dump_image);
			cu.chmod(fflash, "641");
			cu.chmod(fdump, "641");
		}
	}
	
	public void flash(File file) {
			
		if (file.exists()) {
			
			if (s.MTD)
				cu.executeShell(fflash.getAbsolutePath() + " recovery " + file.getAbsolutePath(), true);
			
			if (!s.MTD
					&& !s.BLM
					&& !s.RecoveryPath.equals(""))
				cu.executeShell("dd if=" + file.getAbsolutePath() + " of=" + s.RecoveryPath, true);
			
		}
	}
	
	public void backup(File outputFile) {
		
		if (s.MTD)
			cu.executeShell(fdump.getAbsolutePath() + " recovery " + outputFile.getAbsolutePath() + s.EXT, true);
		
		if (!s.MTD
				&& !s.BLM
				&& !s.RecoveryPath.equals(""))
			cu.executeShell("dd if=" + s.RecoveryPath + " of=" + outputFile.getAbsolutePath() + s.EXT, true);
	}
}
