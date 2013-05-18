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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.sbstrm.appirater.Appirater;

public class MainActivity extends Activity {
	
//	Get path to external storage
	private static final File PathToSd = Environment.getExternalStorageDirectory();
//	Declaring needed files and folders
	private static final File PathToRecoveryTools = new File(PathToSd , "Recovery-Tools");
	private static final File PathToRecoveries = new File(PathToRecoveryTools ,"recoveries");
	
	private static File fRECOVERY;
//	Declaring Views
	private static TextView tvInfo;
	private static CheckBox cbUseBinary;
	private static MenuItem iLog, iShowLogs;
//	Declaring other vars
	private static boolean firstrun = true;
	Context mContext = this;
//	Declaring needed objects
	NotificationUtil nu = new NotificationUtil(mContext);
	CommonUtil cu = new CommonUtil(mContext);
	FlashUtil fu = new FlashUtil(mContext);
	Support s = new Support();
	
	FileChooser fcFlashOther;

	
//	"Methods" need a input from user (AlertDialog) or at the end of AsyncTask
	Runnable rFlash = new Runnable() {

		@Override
		public void run() {
			fu.flash(fRECOVERY);
			nu.createDialog(R.string.info, R.string.flashed, true, true);
		}
		
	};
	Runnable rFlasher = new Runnable(){
		@Override
		public void run() {
			try{
				if (fcFlashOther.use){
					fRECOVERY = fcFlashOther.selectedFile;
				}
			} catch (Exception e) {/* Continue with TWRP and CWM Button */}
			
			if (fRECOVERY.exists()){
				if (!s.KERNEL_TO 
						&& !s.FLASH_OVER_RECOVERY) {
					rFlash.run();
				} else {
					if (s.KERNEL_TO)
						nu.createAlertDialog(R.string.warning, R.string.kernel_to, rFlash);
					if (s.FLASH_OVER_RECOVERY) {
						final AlertDialog.Builder abuilder = new AlertDialog.Builder(mContext);
						abuilder
							.setTitle(R.string.info)
							.setMessage(R.string.flash_over_recovery)
							.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									cu.executeShell("reboot recovery", true);
								}
							})
							.setNeutralButton(R.string.instructions, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Dialog d = new Dialog(mContext);
									d.setTitle(R.string.instructions);
									TextView tv = new TextView(mContext);
									tv.setTextSize(20);
									tv.setText(R.string.instruction);
									d.setContentView(tv);
									d.setOnCancelListener(new DialogInterface.OnCancelListener() {
										
										@Override
										public void onCancel(DialogInterface dialog) {
											abuilder.show();
											
										}
									});
									d.show();
								}
							})
							.show();
					}
				}
					
			} else {
				nu.createAlertDialog(R.string.info, R.string.getdownload, rDownload);
			}
			
		}
	};
	
	Runnable rDownload = new Runnable(){
		@Override
		public void run() {
			downloadFile(s.HOST_URL + "/" + fRECOVERY.getName().toString(), fRECOVERY);
		}};
	Runnable runOnTrue = new Runnable() {
		@Override
		public void run() {report(null);}
	};
	Runnable runOnNegative = new Runnable() {
		@Override
		public void run() {
			createNotification(R.drawable.ic_launcher, R.string.warning, R.string.notsupportded, 28);
			finish();
			System.exit(0);
		}
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getSupport();
		
		cu.checkFolder(PathToRecoveryTools);
		cu.checkFolder(PathToRecoveries);
		
		tvInfo = (TextView) findViewById (R.id.tvInfo);
		
		tvInfo.setText("\nModel: " + android.os.Build.MODEL + "\nName: " + s.DEVICE);
		
		if (firstrun){
			
			if (!cu.suRecognition()) {
				createNotification(R.drawable.ic_launcher, R.string.warning, R.string.noroot, 28);
				finish();
				System.exit(0);
			}
			
			firstrun = false;
		}
		
		cbUseBinary = (CheckBox) findViewById(R.id.cbUseBinary);
		cbUseBinary.setChecked(true);
		if (s.RecoveryPath.equals("")
				&& !s.MTD) {
			nu.createAlertDialog(R.string.warning, R.string.notsupportded, true, runOnTrue, false, new Runnable(){public void run() {}}, true, runOnNegative);
		} else if (!s.RecoveryPath.equals("")){
			cbUseBinary.setText(String.format(mContext.getString(R.string.using_dd), "\n" + s.RecoveryPath));
		} else if (s.MTD){
			cbUseBinary.setText(R.string.using_mtd);
		}
		
	}

//	Button Methods (onClick)
	public void Go(View view){
		s.getVersion(view.getTag().toString());
		
		fRECOVERY = s.constructFile(PathToRecoveries);
		rFlasher.run();
		Appirater.appLaunched(mContext);
	}
	public void bFlashOther(View view){
		fcFlashOther = new FileChooser(mContext, PathToSd.getAbsolutePath(), rFlasher);
	}
	public void bBackupMgr(View view) {
		startActivity(new Intent(this, BackupManagerActivity.class));
	}
	public void bCleareCache(View view) {
		cu.deleteFolder(PathToRecoveries, false);
	}
	public void bRebooter(View view) {
		new Rebooter(mContext).run();
	}
//	Called from Button Methods, created to redundancy
	public void downloadFile(String URL, File outputFile) {
		new DownloadUtil(mContext, URL, outputFile, rFlasher).execute();
	}

	public void getSupport() {
	
//	TWRP unsupported devices
		if (!s.TWRP) {
			Button bTWRP = (Button) findViewById(R.id.bTWRP);
			bTWRP.setText(R.string.notwrp);
			bTWRP.setClickable(false);
		}

//	CWM unsupported devices
		if (!s.CWM) {
			Button bCWM = (Button) findViewById(R.id.bCWM);
			bCWM.setText(R.string.nocwm);
			bCWM.setClickable(false);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void createNotification(int Icon, int Title, int Message, int nid) {
		Intent intent = new Intent();
		PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification n = new Notification(Icon, getString(Message), System.currentTimeMillis());
		n.setLatestEventInfo(this, getString(Title), getString(Message), pi);
		n.flags = Notification.FLAG_AUTO_CANCEL;
		nm.notify(nid, n);	
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    super.onPrepareOptionsMenu(menu);
	    iShowLogs = menu.findItem(R.id.iShowLogs);
	    iShowLogs.setVisible(cu.getBooleanPerf("common_util", "log"));
	    iLog = menu.findItem(R.id.iLog);
	    iLog.setChecked(cu.getBooleanPerf("common_util", "log"));
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.iProfile:
	            cu.xdaProfile();
	            return true;
	        case R.id.iExit:
	        	finish();
	    		System.exit(0);
	    		return true;
	        case R.id.iLog:
	        	if (cu.getBooleanPerf("common_util", "log")){
	        		iLog.setChecked(false);
	        		cu.setBooleanPerf("common_util", "log", false);
	        	} else {
	        		iLog.setChecked(true);
	        		cu.setBooleanPerf("common_util", "log", true);
	        	}
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	public void report(MenuItem Item) {
		final Dialog reportDialog = nu.createDialog(R.string.commentar, R.layout.dialog_comment, false, true);
		Button ok = (Button) reportDialog.findViewById(R.id.bGo);
		ok.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
					EditText text = (EditText) reportDialog.findViewById(R.id.etCommentar);
					String comment = text.getText().toString();

					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_EMAIL, new String[] {mContext.getString(R.string.REPORT_to_EMAIL)});
					intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.EMAIL_SUBJECT));
					intent.putExtra(Intent.EXTRA_TEXT, "Package Infos:" +
							"\n\nName: " + pInfo.packageName +
							"\nVersionName: " + pInfo.versionName +
							"\nVersionCode: " + pInfo.versionCode +
							"\n\n\nProduct Info: " + 
							"\n\nManufacture: " + android.os.Build.MANUFACTURER + 
							"\nDevice: " + android.os.Build.DEVICE + 
							"\nBoard: " + android.os.Build.BOARD + 
							"\nBrand: " + android.os.Build.BRAND +
							"\nModel: " + android.os.Build.MODEL +
							"\n\n\n===========Comment==========\n" + comment +
							"\n===========Comment==========");
					startActivity(Intent.createChooser(intent, "Send as EMAIL"));
					reportDialog.dismiss();
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
		
		reportDialog.show();
	}
	
	public void showLogs(MenuItem item) {
		
		Dialog dialog = nu.createDialog(R.string.su_logs_title, R.layout.dialog_su_logs, false, true);
		TextView Log = (TextView) dialog.findViewById(R.id.tvSuLogs);
		
		Log.setText(fu.cu.SuLog);
	}
}
