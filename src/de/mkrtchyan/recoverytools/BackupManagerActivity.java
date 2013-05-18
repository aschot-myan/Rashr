package de.mkrtchyan.recoverytools;

import java.io.File;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class BackupManagerActivity extends Activity {

	private static final File PathToSd = Environment.getExternalStorageDirectory();
	private static final File PathToRecoveryTools = new File(PathToSd , "Recovery-Tools");
	private static final File PathToBackups = new File(PathToRecoveryTools, "backups");
	private File fBACKUP;
	
	Context mContext = this;
	NotificationUtil nu = new NotificationUtil(mContext);
	CommonUtil cu = new CommonUtil(mContext);
	Support s = new Support();
	FlashUtil fu = new FlashUtil(mContext);
	FileChooser fcRestore;
	FileChooser fcDelete;

	
	
	Runnable rBackup = new Runnable(){
		@Override
		public void run() {
			if (s.FLASH_OVER_RECOVERY 
					|| s.BLM){
				nu.createDialog(R.string.warning, R.string.no_function, true, true);	
			} else {
				fu.backup(fBACKUP);
				nu.createDialog(R.string.info, R.string.bakreport, true, true);
			}
		}
	};
	Runnable rRestore = new Runnable(){
		@Override
		public void run() {
			if (s.FLASH_OVER_RECOVERY 
					|| s.BLM){
				nu.createDialog(R.string.warning, R.string.no_function, true, true);	
			} else {
				if (fcRestore.use)
					fBACKUP = fcRestore.selectedFile;
				fu.flash(fBACKUP);
				nu.createDialog(R.string.info, R.string.resreport, true, true);
			}
		}
	};
	
	Runnable rDelete = new Runnable(){
		@Override
		public void run() {
			fcDelete.selectedFile.delete();
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup_manager);
		
		cu.checkFolder(PathToBackups);
	}
	
	public void bBackup(View view) {
		
		final Dialog dialog = new Dialog(mContext);
		dialog.setTitle(R.string.setname);
		dialog.setContentView(R.layout.dialog_renamer);
		Button dobackup = (Button) dialog.findViewById(R.id.bGoBackup);
		final EditText etFileName = (EditText) dialog.findViewById(R.id.etFileName);
		dobackup.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String Name = "";
				if (!etFileName.getText().toString().equals("")) {
					Name = etFileName.getText().toString();
				} else {
					Calendar c = Calendar.getInstance();
					Name = Calendar.DATE
							+ "-" + c.get(Calendar.MONTH)
							+ "-" + c.get(Calendar.YEAR)
							+ "-" + c.get(Calendar.HOUR)
							+ ":" + c.get(Calendar.MINUTE)
							+ "-" + c.get(Calendar.AM_PM);
				}
				
				Name = Name + s.EXT;
				
				fBACKUP = new File(PathToBackups, Name);
					
				if (fBACKUP.exists()) {
					nu.createAlertDialog(R.string.warning, R.string.backupalready, rBackup);
				} else {
					rBackup.run();
				}
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	public void bRestore(View view) {
		if (PathToBackups.list().length < 1) {
			nu.createAlertDialog(R.string.warning, R.string.nobackup, rBackup);
		} else {
			fcRestore = new FileChooser(mContext, PathToBackups.getAbsolutePath(), rRestore);
		}
	}
	public void bDeleteBackup(View view) {
		if (PathToBackups.list().length < 1) {
			nu.createAlertDialog(R.string.warning, R.string.nobackup, rBackup);
		} else {
			fcDelete = new FileChooser(mContext, PathToBackups.getAbsolutePath(), rDelete);
		}
	}
}
