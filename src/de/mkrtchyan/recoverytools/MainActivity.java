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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;


public class MainActivity extends Activity {
	
//	Get path to external storage
	private static final String PathToSd = Environment.getExternalStorageDirectory().getPath();
//	Setting root URL to RecoveryURL
	private static String RecoveryURL = "http://dslnexus.nazuka.net/recoveries/";
//	Declaring needed files and folders
	private static final File PathToRecoveryTools = new File(PathToSd , "RecoveryTools");
	private static final File PathToRecoveries = new File(PathToRecoveryTools ,"recoveries");
	private static final File PathToBackup = new File(PathToRecoveryTools, "backup");
	private static final File fBACKUP = new File(PathToBackup, "backup.img");
	private static File fflash;
	private static File fdump;
	private static File fIMG;
// Get device info and other
	private static String Device = android.os.Build.DEVICE;
	private final String RecoveryPath = getRecoveryPath();
	private static String filename;
	private static CheckBox cbUseBinary;
	private static boolean MTD = false;
	private static boolean firstrun;
	
	Context context = this;
	NotificationUtil nu = new NotificationUtil(context);
	CommonUtil cu = new CommonUtil(context);
//	"Methods" need a input from user (AlertDialog) or at the end of AsyncTask
	Runnable rFlash = new Runnable(){

		@Override
		public void run() {
			if (fIMG.exists()) {
				if (!MTD){
					cu.executeShell("dd if=" + fIMG.getAbsolutePath() + " of=" + RecoveryPath);
					nu.createDialog(R.string.info, R.string.flashed, true, true);
				} else {
					cu.executeShell(fflash.getAbsolutePath() + " recovery " + fIMG.getAbsolutePath());
				}
			}
		}
	};
	Runnable rBackup = new Runnable(){
	
		@Override
		public void run() {
			if (!MTD){
				nu.createDialog(R.string.bakreport, cu.executeShell("dd if=" + RecoveryPath + " of=" + PathToBackup.getAbsolutePath() + "/backup.img"), true);
			} else {
				nu.createDialog(R.string.bakreport, cu.executeShell(fdump.getAbsolutePath() + " recovery " + fBACKUP.getAbsolutePath()), true);
			}
		}
	};
	Runnable rRestore = new Runnable(){
		@Override
		public void run() {
			if (!MTD){
				nu.createDialog(R.string.resreport, cu.executeShell("dd if=" + PathToBackup.getAbsolutePath() + "/backup.img of=" + RecoveryPath), true);
			} else {
				nu.createDialog(R.string.resreport, cu.executeShell(fflash.getAbsolutePath() + " recovery " + fBACKUP.getAbsolutePath()), true);
			}
		}
	};
	Runnable rDownload = new Runnable(){
		@Override
		public void run() {
			if (fIMG.exists()) {
				rFlash.run();
			} else {
				ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			
				if (networkInfo != null 
						&& networkInfo.isConnected()) {
					downloadFile(RecoveryURL + Device + "-" + filename, fIMG);
				} else {
					nu.createDialog(R.string.warning, R.string.noconnection, true, true);
				}
			}
		}};
	Runnable runOnTrue = new Runnable() {

		@Override
		public void run() {
//			report();
		}
		
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
		
		cu.checkFolder(PathToRecoveryTools);
		cu.checkFolder(PathToRecoveries);
		cu.checkFolder(PathToBackup);
		
		TextView tvInfo = (TextView) findViewById (R.id.tvInfo);
		
		tvInfo.setText("\nModel: " + android.os.Build.MODEL + "\nName: " + Device);
		
		if (!firstrun){
			
			if (!cu.suRecognition()) {
				createNotification(R.drawable.ic_launcher, R.string.warning, R.string.noroot, 28);
				finish();
				System.exit(0);
			} else if (MTD) {
				cbUseBinary = (CheckBox) findViewById(R.id.cbUseBinary);
				cbUseBinary.setVisibility(View.VISIBLE);
				cbUseBinary.setChecked(true);
				fflash = new File(context.getFilesDir(), "flash_image");
				fdump = new File(context.getFilesDir(), "dump_image");
				cu.pushFileFromRAW(fflash, R.raw.flash_image);
				cu.pushFileFromRAW(fdump, R.raw.dump_image);
				cu.chmod("641", fflash);
				cu.chmod("641", fdump);
				MTD = true;
			} else if (RecoveryPath.equals("")) {
				runOnNegative.run();
//				nu.createAlertDialog(R.string.warning, R.string.report, true, runOnTrue, false, new Runnable(){public void run() {}}, true, runOnNegative);
			}
			firstrun = true;
		}
		getSupport();
	}

//	Button Methods (onClick)
	public void Go(View view){
		filename = view.getTag().toString() + ".img";
		fIMG = new File(PathToRecoveries, "/" + Device + "-" + filename);
		if (fIMG.exists()){
			rFlash.run();
		} else {
			nu.createAlertDialog(R.string.info, R.string.getdownload, rDownload);
		}
	}
	public void bBackup(View view) {
		
		if (fBACKUP.exists()) {
			nu.createAlertDialog(R.string.warning, R.string.backupalready, rBackup);
		} else {
			rBackup.run();
		}
	}
	public void bRestore(View view) {
		if (!fBACKUP.exists()) {
			nu.createAlertDialog(R.string.warning, R.string.nobackup, rBackup);
		} else {
			rRestore.run();
		}
	}
	public void bCleareCache(View view) {
		if(PathToRecoveries.exists()) {
			File[] files = PathToRecoveries.listFiles();
			for(int i=0; i<files.length; i++) {
				files[i].delete();
			}
		}
	}
	public void bRebooter(View view) {
		Intent intent = new Intent(this, RebooterActivity.class);
		startActivity(intent);
	}
	public void bExit(View view) {
		finish();
		System.exit(0);
	}
//	Called from Button Methods, created to redundancy
	public void downloadFile(String URL, File outputFile) {
		DownloadUtil du = new DownloadUtil(context, URL, outputFile, rFlash);
		du.execute();
	}

	public String getRecoveryPath() {
		
//		Nexus Devices + Same
		
		if (Device.equals("crespo")
				|| Device.equals("crespo4g")
				|| Device.equals("passion"))
			MTD = true;
		
		if (Device.equals("maguro")
				|| Device.equals("toro")
				|| Device.equals("toroplus"))
			return "/dev/block/platform/omap/omap_hsmmc.0/by-name/recovery";
		
		if (Device.equals("grouper") 
				|| Device.equals("endeavoru") 
				|| Device.equals("tilapia")) 
			return "/dev/block/platform/sdhci-tegra.3/by-name/SOS";
		
		if (Device.equals("mako"))
			return "/dev/block/platform/msm_sdcc.1/by-name/recovery";
		
		if (Device.equals("manta"))
			return "/dev/block/platform/dw_mmc.0/by-name/recovery";
		
//		Samsung Devices + Same
	 
		if (Device.equals("GT-I9100G")
				|| Device.equals("GT-I9100")) 
			Device = "galaxys2";
		
		if (Device.equals("d2att"))
			return "/dev/block/mmcblk0p18";
		
		if (Device.equals("i9300")
				|| Device.equals("GT-I9100")
				|| Device.equals("GT-I9100G"))
			return "/dev/block/mmcblk0p6";
		
		if (Device.equals("n7100"))
			return "/dev/block/mmcblk0p9";
		
		if (Device.equals("golden") 
				|| Device.equals("villec2")) 
			return "/dev/block/mmcblk0p21";
		
		if (Device.equals("n7000"))
			return "/dev/block/platform/dw_mmc/by-name/RECOVERY";
		
		if (Device.equals("jena"))
			return "/dev/block/mmcblk0p12";
		
//		HTC Devices + Same
		
		if (Device.equals("ace") 
				|| Device.equals("primou")) 
			return "/dev/block/platform/msm_sdcc.2/mmcblk0p21";
		
		if (Device.equals("pyramid"))
			return "/dev/block/platform/msm_sdcc.1/mmcblk0p21";
		
		return "";
	}
	public void getSupport() {
		Button bCWM = (Button) findViewById(R.id.bCWM);
		Button bTWRP = (Button) findViewById(R.id.bTWRP);
		
		if (Device.equals("galaxys2") 
				|| Device.equals("n7000")) {
			bTWRP.setText(R.string.notwrp);
			bTWRP.setClickable(false);
		}
		if (Device.equals("")) {
			bCWM.setText(R.string.nocwm);
			bCWM.setClickable(false);
			bTWRP.setText(R.string.notwrp);
			bTWRP.setClickable(false);
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
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.iProfile:
	            cu.xdaProfile();
	            return true;
	        case R.id.iReport:
	        	report();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void report() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"ashotmkrtchyan1995@gmail.com"});
		intent.putExtra(Intent.EXTRA_SUBJECT, "Recovery-Tools report to support new Device");
		intent.putExtra(Intent.EXTRA_TEXT,"Manufacture: " + android.os.Build.MANUFACTURER+ "\nDevice: " + Device + "\nBoard: " + android.os.Build.BOARD + "\nBrand: " + android.os.Build.BRAND);
		startActivity(Intent.createChooser(intent, "Send as EMAIL"));
	}

}
