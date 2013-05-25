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
import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.FileChooser;
import de.mkrtchyan.utils.Notifyer;

public class BackupManagerActivity extends Activity {

	private static final File PathToSd = Environment.getExternalStorageDirectory();
	private static final File PathToRecoveryTools = new File(PathToSd , "Recovery-Tools");
	private static final File PathToBackups = new File(PathToRecoveryTools, "backups");
	private File fBACKUP;
	
	Context mContext = this;
	Notifyer nu = new Notifyer(mContext);
	Common c = new Common();
	Support s = new Support();
	FlashUtil fu;
	FileChooser fcRestore;
	FileChooser fcDelete;
	
	Runnable rBackup = new Runnable(){
		@Override
		public void run() {
			if (s.FLASH_OVER_RECOVERY 
					|| s.BLM){
				nu.createDialog(R.string.warning, R.string.no_function, true, true);	
			} else {
				fu = new FlashUtil(mContext, fBACKUP, 2);
				fu.execute();
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
				fu = new FlashUtil(mContext, fBACKUP, 1);
				fu.execute();
			}
		}
	};
	
	Runnable rDelete = new Runnable(){
		@Override
		public void run() {
			fcDelete.selectedFile.delete();
			fcDelete = new FileChooser(mContext, PathToBackups.getAbsolutePath(), rDelete);
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup_manager);
		
		c.checkFolder(PathToBackups);
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
