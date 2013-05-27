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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.rootcommands.util.RootAccessDeniedException;

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
import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Downloader;
import de.mkrtchyan.utils.FileChooser;
import de.mkrtchyan.utils.Notifyer;

public class RecoveryTools extends Activity {
	
	Context mContext = this;
//	Get path to external storage
	public static final File PathToSd = Environment.getExternalStorageDirectory();
//	Declaring needed files and folders
	public static final File PathToRecoveryTools = new File(PathToSd , "Recovery-Tools");
	public static final File PathToRecoveries = new File(PathToRecoveryTools ,"recoveries");
	public static final File PathToUtils = new File(PathToRecoveryTools, "utils");
	private File fRECOVERY, fflash, fdump, charger, chargermon, ric;
//	Declaring Views
	private TextView tvInfo;
	private CheckBox cbUseBinary;
	private MenuItem iLog, iShowLogs;
//	Declaring other vars
	private boolean firstrun = true;
	private boolean download = false;
//	Declaring needed objects
	Notifyer notifyer = new Notifyer(mContext);
	Common common = new Common();
	Support s = new Support();
	FileChooser fcFlashOther;
	
//	"Methods" need a input from user (AlertDialog) or at the end of AsyncTask
	Runnable rFlash = new Runnable() {
		@Override
		public void run() {
			new FlashUtil(mContext, fRECOVERY, 1).execute();
		}
	};
	Runnable rFlasher = new Runnable(){
		@Override
		public void run() {
			if (fcFlashOther != null) {
				if (fcFlashOther.use){
					if (fcFlashOther.selectedFile.getName().endsWith(s.EXT)) {
						fRECOVERY = fcFlashOther.selectedFile;
					} else {
						fRECOVERY = null;
						notifyer.createDialog(R.string.warning, String.format(mContext.getString(R.string.wrong_format), s.EXT), true);
					}
				}
			}
			
			if (fRECOVERY != (null)) {
				if (fRECOVERY.exists() 
						&& fRECOVERY.getAbsolutePath().endsWith(s.EXT)){
	//				If the flashing don't be handle specially flash it
					if (!s.KERNEL_TO 
							&& !s.FLASH_OVER_RECOVERY) {
						rFlash.run();
					} else {
	//					Get user input if Kernel will be modified 
						if (s.KERNEL_TO)
							notifyer.createAlertDialog(R.string.warning, R.string.kernel_to, rFlash);
	//					Get user input if user want to install over recovery now
						if (s.FLASH_OVER_RECOVERY) {
	//						Create coustom AlertDialog
							final AlertDialog.Builder abuilder = new AlertDialog.Builder(mContext);
							abuilder
								.setTitle(R.string.info)
								.setMessage(R.string.flash_over_recovery)
								.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										try {
											common.executeSuShell("reboot recovery");
										} catch (RootAccessDeniedException e) {
											e.printStackTrace();
										}
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
	//				If Recovery File don't exist ask if you want to download it now.
					notifyer.createAlertDialog(R.string.info, R.string.getdownload, rDownload);
				}
			}
		}
	};
	
	Runnable rDownload = new Runnable(){
		@Override
		public void run() {
//			Download file from URL s.HOST_URL + "/" + fRECOVERY.getName().toString() and write it to fRECOVERY
			new Downloader(mContext, s.HOST_URL, fRECOVERY.getName(), fRECOVERY, rFlasher).execute();	
		}};
	
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recovery_tools);
		
		fflash = new File("/system/bin", "flash_image");
		fdump = new File("/system/bin", "dump_image");
//		Checking if flash and dump image is implemented in ROM
		if (!fflash.exists())
			fflash = new File(mContext.getFilesDir(), fflash.getName());
		if(!fdump.exists())
			fdump = new File(mContext.getFilesDir(), fdump.getName());
		if (!fflash.exists()
				|| !fdump.exists()
				&& s.MTD)
			download = true;
		charger = new File(PathToUtils, "charger");
		chargermon = new File(PathToUtils, "chargermon");
		ric = new File(PathToUtils, "ric");
		if (!charger.exists()
				|| !chargermon.exists()
				|| !ric.exists()
				&& s.DEVICE.equals("C6603"))
			download = true;
		
//		Do on every First-Run
		if (firstrun){
//			If device is not supported, you can report it now or close the App
			if (s.RecoveryPath.equals("")
					&& !s.MTD 
					&& !s.FLASH_OVER_RECOVERY) {
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
				notifyer.createAlertDialog(R.string.warning, R.string.notsupportded, runOnTrue, null, runOnNegative);
			}
//			Check if Su-Access is given if not the app will be closed
			if (!common.suRecognition()) {
//				Show a new notification with Info
				createNotification(R.drawable.ic_launcher, R.string.warning, R.string.noroot, 28);
				finish();
				System.exit(0);
			}
			firstrun = false;
		}
//		Setting up Buttons (CWM and TWRP support)
		getSupport();
//		Create needed Folder
		common.checkFolder(PathToRecoveryTools);
		common.checkFolder(PathToRecoveries);
		common.checkFolder(PathToUtils);
		
//		Print information of the Device
		tvInfo = (TextView) findViewById (R.id.tvInfo);
		tvInfo.setText("\nModel: " + android.os.Build.MODEL + "\nName: " + s.DEVICE);
//		Show Advanced information how device will be Flashed
		cbUseBinary = (CheckBox) findViewById(R.id.cbUseBinary);
		cbUseBinary.setChecked(true);
		if (!s.RecoveryPath.equals("")) {
			cbUseBinary.setText(String.format(mContext.getString(R.string.using_dd), "\n" + s.RecoveryPath));
		} else if (s.MTD) {
			cbUseBinary.setText(R.string.using_mtd);
		} else if (s.FLASH_OVER_RECOVERY) {
			cbUseBinary.setText(R.string.over_recovery);
		}
		
		downloadUtils();
	}

//	Button Methods (onClick)
	public void Go(View view){
		if (s.MTD 
				&& fflash.exists()
				&& fdump.exists()
				|| s.DEVICE.equals("C6603")
				&& ric.exists()
				&& charger.exists()
				&& chargermon.exists()
				|| !s.MTD
				&& !s.DEVICE.equals("C6603")) {
//			Get newest version No. of recovery (view.getTag().toString() returns clockwork or twrp)
			s.getVersion(view.getTag().toString());
//			Get device specificed recovery file for example recovery-clockwork-touch-6.0.3.1-grouper.img
			fRECOVERY = s.constructFile(PathToRecoveries);
			rFlasher.run();
		} else {
			downloadUtils();
		}
	}
	public void bFlashOther(View view){
		fcFlashOther = new FileChooser(mContext, PathToSd.getAbsolutePath(), rFlasher);
	}
	public void bBackupMgr(View view) {
		startActivity(new Intent(this, BackupManagerActivity.class));
	}
	public void bCleareCache(View view) {
		common.deleteFolder(PathToRecoveries, false);
	}
	public void bRebooter(View view) {
		new Rebooter(mContext).run();
	}
//	Called from Button Methods, created to redundancy
	public void getSupport() {
//	Toggle TWRP and CWM install button if they aren't supported
//		Settings for TWRP unsupported devices
		if (!s.TWRP) {
			Button bTWRP = (Button) findViewById(R.id.bTWRP);
			bTWRP.setText(R.string.notwrp);
			bTWRP.setClickable(false);
		}

//		Settings for CWM unsupported devices
		if (!s.CWM) {
			Button bCWM = (Button) findViewById(R.id.bCWM);
			bCWM.setText(R.string.nocwm);
			bCWM.setClickable(false);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void createNotification(int Icon, int Title, int Message, int nid) {
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification n = new Notification(Icon, getString(Message), System.currentTimeMillis());
		n.setLatestEventInfo(this, getString(Title), getString(Message), PendingIntent.getActivity(this, 0, new Intent(), 0));
		n.flags = Notification.FLAG_AUTO_CANCEL;
		nm.notify(nid, n);	
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.recovery_tools_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    super.onPrepareOptionsMenu(menu);
	    iShowLogs = menu.findItem(R.id.iShowLogs);
	    iShowLogs.setVisible(common.getBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands"));
	    iLog = menu.findItem(R.id.iLog);
	    iLog.setChecked(common.getBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands"));
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.iProfile:
	        	common.xdaProfile(mContext);
	            return true;
	        case R.id.iExit:
	        	finish();
	    		System.exit(0);
	    		return true;
	        case R.id.iLog:
	        	if (common.getBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands")){
	        		iLog.setChecked(false);
	        		common.setBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands", false);
	        	} else {
	        		iLog.setChecked(true);
	        		common.setBooleanPerf(mContext, "mkrtchyan_utils_common", "log-commands", true);
	        	}
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	public void report(MenuItem Item) {
//		Creates a report Email with Commentar
		final Dialog reportDialog = notifyer.createDialog(R.string.commentar, R.layout.dialog_comment, false, true);
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
		
		Dialog dialog = notifyer.createDialog(R.string.su_logs_title, R.layout.dialog_command_logs, false, true);
		final TextView tvLog = (TextView) dialog.findViewById(R.id.tvSuLogs);
		final Button bClearLog = (Button) dialog.findViewById(R.id.bClearLog);
		bClearLog.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				new File(mContext.getFilesDir(), "command-logs.log").delete();
				tvLog.setText("");
			}
		});
		String sLog = "";
		
		try {
			
			String line = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput("command-logs.log")));
			while ((line = br.readLine()) != null) {
				sLog = sLog + line + "\n";
			}
			br.close();
			tvLog.setText(sLog);
		} catch (FileNotFoundException e) {
			tvLog.setText("No log found!");
			e.printStackTrace();
		} catch (IOException e) {
			tvLog.setText(tvLog.getText() + "\n" + e.getMessage());
			e.printStackTrace();
		}
		
//		Log.setText(sLog);
	}
	
	public void downloadUtils() {
		AlertDialog.Builder abuilder = new AlertDialog.Builder(mContext);
		abuilder
			.setTitle(R.string.warning)
			.setMessage(R.string.download_utils);
		DialogInterface.OnClickListener onClick = null;

		if (s.MTD 
				&& download) {
				
			onClick = new DialogInterface.OnClickListener() {
				
			@Override
			public void onClick(DialogInterface dialog, int which) {
				new Downloader(mContext, "http://dslnexus.nazuka.net/utils", fflash.getName(), fflash, Notifyer.rEmpty).execute();
				new Downloader(mContext, "http://dslnexus.nazuka.net/utils", fdump.getName(), fdump, Notifyer.rEmpty).execute();
			}
			};
		} else if (s.DEVICE.equals("C6603")
				&& download) {
			
			onClick = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
						new Downloader(mContext, "http://dslnexus.nazuka.net/utils/" + s.DEVICE, chargermon.getName(), chargermon, Notifyer.rEmpty).execute();
						new Downloader(mContext, "http://dslnexus.nazuka.net/utils/" + s.DEVICE, charger.getName(), charger, Notifyer.rEmpty).execute();
						new Downloader(mContext, "http://dslnexus.nazuka.net/utils/" + s.DEVICE, ric.getName(), ric, Notifyer.rEmpty).execute();
					;
				}
			};
		}
		abuilder.setPositiveButton(R.string.positive, onClick);
		if (download
				&& s.MTD
				|| s.DEVICE.equals("C6603")) 
			abuilder.show();
	}
}
