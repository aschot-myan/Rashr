package de.mkrtchyan.recoverytools;

import java.io.File;

import org.rootcommands.util.RootAccessDeniedException;

import com.sbstrm.appirater.Appirater;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class FlashUtil extends AsyncTask<Void, Void, Boolean>{
	
	Context mContext;
	ProgressDialog pDialog;
	CommonUtil cu;
	NotificationUtil nu;
	Support s = new Support();
	File charger;
	File chargermon;
	File ric;
	private static File fflash;
	private static File fdump;
	File file;
	int JOB;
	
	public FlashUtil(Context context, File file, int JOB){
		mContext = context;
		this.file = file;
		this.JOB = JOB;
		nu = new NotificationUtil(mContext);
		cu = new CommonUtil();
		if (s.MTD){
			fflash = new File(mContext.getFilesDir(), "flash_image");
			fdump = new File(mContext.getFilesDir(), "dump_image");
			
			cu.chmod(fflash, "641", true);
			cu.chmod(fdump, "641", true);
		}
		if (s.DEVICE.equals("C6603")) {
			charger = new File(mContext.getFilesDir(), "charger");
			chargermon = new File(mContext.getFilesDir(), "chargermon");
			ric = new File(mContext.getFilesDir(), "ric");
			
			cu.chmod(charger, "755", true);
			cu.chmod(chargermon, "755", true);
			cu.chmod(ric, "755", true);
		}
	}
	
	
	protected void onPreExecute() {
		
		
		pDialog = new ProgressDialog(mContext);
		int Title;
		if (JOB == 1) {
			Title = R.string.flashing;
		} else {
			Title = R.string.creating_bak;
		}
		pDialog.setTitle(Title);
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
		pDialog.setMessage(file.getAbsolutePath());
		pDialog.setCancelable(false);
		pDialog.show();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		
		try {
			if (s.DEVICE.equals("C6603")) {
				cu.executeSuShell(mContext, "cat " + charger.getAbsolutePath() + " >> /system/bin" + charger.getName());
				cu.executeSuShell(mContext, "cat " + chargermon.getAbsolutePath() + " >> /system/bin" + chargermon.getName());
				cu.executeSuShell(mContext, "cat " + ric.getAbsolutePath() + " >> /system/bin" + ric.getName());
				cu.chmod(ric, "755", true);
				cu.chmod(charger, "755", true);
				cu.chmod(chargermon, "755", true);
			}
			
			switch (JOB) {
			
			case 1:
				if (file.exists()) {
					
					if (s.MTD)
						cu.executeSuShell(mContext, fflash.getAbsolutePath() + " recovery " + file.getAbsolutePath());
					
					if (!s.MTD
							&& !s.BLM
							&& !s.RecoveryPath.equals(""))
						cu.executeSuShell(mContext, "dd if=" + file.getAbsolutePath() + " of=" + s.RecoveryPath);
					if (s.DEVICE.equals("C6603"))
						cu.chmod(file, "644", true);
				}
				break;
			case 2:
				if (s.MTD)
					cu.executeSuShell(mContext, fdump.getAbsolutePath() + " recovery " + file.getAbsolutePath());
				
				if (!s.MTD
						&& !s.BLM
						&& !s.RecoveryPath.equals(""))
					cu.executeSuShell(mContext, "dd if=" + s.RecoveryPath + " of=" + file.getAbsolutePath());
				break;
			}
		} catch (RootAccessDeniedException e) {e.printStackTrace();}
		return null;
	}
	
	protected void onPostExecute(Boolean succes) {
		
		pDialog.dismiss();
		if (JOB == 1) {
			nu.createAlertDialog(R.string.tsk_end, mContext.getString(R.string.flashed) + " " + mContext.getString(R.string.reboot_recovery_now), new Runnable() {
				@Override
				public void run() {try {
					cu.executeSuShell("reboot recovery");
				} catch (RootAccessDeniedException e) {e.printStackTrace();}}
			});
		} else {
			nu.createDialog(R.string.tsk_end, R.string.bakreport, true, true);
		}
		
		Appirater.appLaunched(mContext);
		
		cu.setBooleanPerf(mContext, "flash-util", "first-flash", false);
	}
}
