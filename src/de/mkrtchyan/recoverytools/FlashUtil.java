package de.mkrtchyan.recoverytools;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class FlashUtil {
	
	Context context;
	CommonUtil cu;
	NotificationUtil nu;
	Support s;
	private static File fflash;
	private static File fdump;
	
	private static final File PathToSd = new File(Environment.getExternalStorageDirectory().getPath());
	private static final File PathToRecoveryTools = new File(PathToSd , "RecoveryTools");
	private static final File PathToBackup = new File(PathToRecoveryTools, "backup");
	private static final File fBACKUP = new File(PathToBackup, "backup.img");
	
	public FlashUtil(Context context){
		context = this.context;
		cu = new CommonUtil(this.context);
		nu = new NotificationUtil(this.context);
		s = new Support();
		if (s.MTD){
			fflash = new File(context.getFilesDir(), "flash_image");
			fdump = new File(context.getFilesDir(), "dump_image");
			cu.pushFileFromRAW(fflash, R.raw.flash_image);
			cu.pushFileFromRAW(fdump, R.raw.dump_image);
			cu.chmod("641", fflash);
			cu.chmod("641", fdump);
		}
	}
	
	public void flash(File file) {
		if (file.exists()) {
			if (!s.MTD){
				cu.executeShell("dd if=" + file.getAbsolutePath() + " of=" + s.RecoveryPath);
				nu.createDialog(R.string.info, R.string.flashed, true, true);
			} else {
				cu.executeShell(fflash.getAbsolutePath() + " recovery " + file.getAbsolutePath());
			}
		}
	}
	
	public void backup() {
		if (!s.MTD){
			nu.createDialog(R.string.bakreport, cu.executeShell("dd if=" + s.RecoveryPath + " of=" + PathToBackup.getAbsolutePath() + "/backup.img"), true);
		} else {
			nu.createDialog(R.string.bakreport, cu.executeShell(fdump.getAbsolutePath() + " recovery " + fBACKUP.getAbsolutePath()), true);
		}
	}
	
	public void restore() {
		if (!s.MTD){
			nu.createDialog(R.string.resreport, cu.executeShell("dd if=" + PathToBackup.getAbsolutePath() + "/backup.img of=" + s.RecoveryPath), true);
		} else {
			nu.createDialog(R.string.resreport, cu.executeShell(fflash.getAbsolutePath() + " recovery " + fBACKUP.getAbsolutePath()), true);
		}
	}
	
}
