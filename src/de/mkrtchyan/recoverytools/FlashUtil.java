package de.mkrtchyan.recoverytools;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class FlashUtil {
	
	Context context;
	CommonUtil cu;
	Support s = new Support();
	private static File fflash;
	private static File fdump;
	
	private static final File PathToSd = new File(Environment.getExternalStorageDirectory().getPath());
	private static final File PathToRecoveryTools = new File(PathToSd , "RecoveryTools");
	private static final File PathToBackup = new File(PathToRecoveryTools, "backup");
	private static final File fBACKUP = new File(PathToBackup, "backup.img");
	
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
			
//			if (s.MTD){
//				cu.executeShell(fflash.getAbsolutePath() + " recovery " + file.getAbsolutePath(), true);
//			}
//			
//			if (s.BLM){
				File tmp = new File(PathToRecoveryTools, "tmp");
				cu.checkFolder(tmp);
				
				cu.unzip(file, tmp);
				
				File meta = new File(tmp, "META-INF");
				if (meta.exists())
					cu.deleteFolder(meta, true);
				
				cu.copy(tmp, new File(s.RecoveryPath), true);
//			}
//			
//			if (!s.MTD
//					&& !s.BLM
//					&& !s.RecoveryPath.equals("")){
//				cu.executeShell("dd if=" + file.getAbsolutePath() + " of=" + s.RecoveryPath, true);
//			}
		}
	}
	
	public void backup() {
		if (!s.MTD){
			cu.executeShell("dd if=" + s.RecoveryPath + " of=" + PathToBackup.getAbsolutePath() + "/backup.img", true);
		} else {
			cu.executeShell(fdump.getAbsolutePath() + " recovery " + fBACKUP.getAbsolutePath(), true);
		}
	}
	
	public void restore() {
		if (!s.MTD){
			cu.executeShell("dd if=" + PathToBackup.getAbsolutePath() + "/backup.img of=" + s.RecoveryPath, true);
		} else {
			cu.executeShell(fflash.getAbsolutePath() + " recovery " + fBACKUP.getAbsolutePath(), true);
		}
	}
	
}
