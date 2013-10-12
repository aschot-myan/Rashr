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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.devspark.appmsg.AppMsg;
import com.sbstrm.appirater.Appirater;

import java.io.File;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Notifyer;

public class FlashUtil extends AsyncTask<Void, Void, Boolean> {

	public static final String PREF_NAME = "FlashUtil";
	public static final String PREF_HIDE_REBOOT = "hide_reboot";
	public static final String PREF_FLASH_COUNTER = "last_counter";
	public static final int JOB_FLASH = 1;
	public static final int JOB_BACKUP = 2;

	private final Context mContext;
	private static final String TAG = "FlashUtil";
	private ProgressDialog pDialog;
	final private Common mCommon = new Common();
	private final Notifyer mNotifyer;
	private final DeviceHandler mDeviceHandler;
	private final File file;
	private final int JOB;
	private boolean keepAppOpen = true;

	public FlashUtil(Context mContext, File file, int JOB) {
		this.mContext = mContext;
		this.file = file;
		this.JOB = JOB;
		mNotifyer = new Notifyer(mContext);
		mDeviceHandler = new DeviceHandler(mContext);
	}

	protected void onPreExecute() {

		Log.i(TAG, "Preparing to flash");
		pDialog = new ProgressDialog(mContext);

		int Title = 0;
		if (JOB == JOB_FLASH) {
			Title = R.string.flashing;
		} else if (JOB == JOB_BACKUP) {
			Title = R.string.creating_bak;
		}
		if (Title != 0)
			pDialog.setTitle(Title);
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		pDialog.setMessage(file.getName());
		pDialog.setCancelable(false);
		pDialog.show();

	}

	@Override
	protected Boolean doInBackground(Void... params) {

		Log.i(TAG, "Flashing...");

		try {
			switch (JOB) {
				case 1:
					if (file.exists()) {
						switch (mDeviceHandler.getDevType()) {
							case DeviceHandler.DEV_TYPE_MTD:
								File flash_image = mDeviceHandler.getFlash_image();
								mCommon.chmod(mDeviceHandler.getFlash_image(), "741");
								mCommon.executeSuShell(mContext, flash_image.getAbsolutePath() + " recovery " + file.getAbsolutePath());
								break;
							case DeviceHandler.DEV_TYPE_DD:
								mCommon.executeSuShell(mContext, "dd if=" + file.getAbsolutePath() + " of=" + mDeviceHandler.getRecoveryPath());
								break;
							case DeviceHandler.DEV_TYPE_CUSTOM:
								if (mDeviceHandler.DEV_NAME.equals("c6603")
										|| mDeviceHandler.DEV_NAME.equals("c6602")
										|| mDeviceHandler.DEV_NAME.equals("montblanc")) {
									mCommon.mountDir(new File(mDeviceHandler.getRecoveryPath()), "RW");
									mCommon.executeSuShell(mContext, "cat " + mDeviceHandler.getCharger().getAbsolutePath() + " >> /system/bin/" + mDeviceHandler.getCharger().getName());
									mCommon.executeSuShell(mContext, "cat " + mDeviceHandler.getChargermon().getAbsolutePath() + " >> /system/bin/" + mDeviceHandler.getChargermon().getName());
									if (mDeviceHandler.DEV_NAME.equals("c6603")
											|| mDeviceHandler.DEV_NAME.equals("c6602")) {
										mCommon.executeSuShell(mContext, "cat " + mDeviceHandler.getRic().getAbsolutePath() + " >> /system/bin/" + mDeviceHandler.getRic().getName());
										mCommon.chmod(mDeviceHandler.getRic(), "755");
									}
									mCommon.chmod(mDeviceHandler.getCharger(), "755");
									mCommon.chmod(mDeviceHandler.getChargermon(), "755");
									mCommon.executeSuShell(mContext, "cat " + file.getAbsolutePath() + " >> " + mDeviceHandler.getRecoveryPath());
									mCommon.chmod(file, "644");
									mCommon.mountDir(new File(mDeviceHandler.getRecoveryPath()), "RO");
								}
								break;
						}
					}
					break;

				case 2:
					switch (mDeviceHandler.getDevType()) {
						case DeviceHandler.DEV_TYPE_DD:
							mCommon.executeSuShell(mContext, "dd if=" + mDeviceHandler.getRecoveryPath() + " of=" + file.getAbsolutePath());
							break;
						case DeviceHandler.DEV_TYPE_MTD:
							File dump_image = mDeviceHandler.getDump_image();
							mCommon.chmod(dump_image, "741");
							mCommon.executeSuShell(mContext, dump_image.getAbsolutePath() + " recovery " + file.getAbsolutePath());
							break;
						case DeviceHandler.DEV_TYPE_CUSTOM:
							if (mDeviceHandler.DEV_NAME.equals("c6603")
									|| mDeviceHandler.DEV_NAME.equals("c6602")
									|| mDeviceHandler.DEV_NAME.equals("montblanc"))
								mCommon.executeSuShell(mContext, "cat " + mDeviceHandler.getRecoveryPath() + " >> " + file.getAbsolutePath());
							break;
					}
					break;
			}
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	protected void onPostExecute(Boolean succes) {

		Log.i(TAG, "Flashing finished");

		pDialog.dismiss();

		saveHistory();

		if (JOB == 1) {
			if (!mCommon.getBooleanPerf(mContext, PREF_NAME, PREF_HIDE_REBOOT)) {
				showRebootDialog();
			} else {
				if (!keepAppOpen) {
					System.exit(0);
				}
			}
		} else {
			mNotifyer.showToast(R.string.bak_done, AppMsg.STYLE_INFO);
		}

		Appirater.appLaunched(mContext);
	}

	public void showRebootDialog() {
		AlertDialog.Builder abuilder = new AlertDialog.Builder(mContext);
		abuilder.setTitle(R.string.tsk_end)
				.setMessage(mContext.getString(R.string.flashed) + " " + mContext.getString(R.string.reboot_recovery_now))
				.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						try {
							mCommon.executeSuShell(mContext, "reboot recovery");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				})
				.setNeutralButton(R.string.neutral, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						if (!keepAppOpen) {
							System.exit(0);
						}
					}
				})
				.setNegativeButton(R.string.never_again, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						mCommon.setBooleanPerf(mContext, PREF_NAME, PREF_HIDE_REBOOT, true);
						if (!keepAppOpen) {
							System.exit(0);
						}
					}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialogInterface) {
						if (!keepAppOpen) {
							System.exit(0);
						}
					}
				})
				.setCancelable(keepAppOpen)
				.show();
	}

	public void saveHistory() {
		switch (mCommon.getIntegerPerf(mContext, RecoveryTools.PREF_NAME, PREF_FLASH_COUNTER)) {
			case 0:
				mCommon.setStringPerf(mContext, RecoveryTools.PREF_NAME, RecoveryTools.PREF_HISTORY + String.valueOf(mCommon.getIntegerPerf(mContext, RecoveryTools.PREF_NAME, PREF_FLASH_COUNTER)), file.getAbsolutePath());
				mCommon.setIntegerPerf(mContext, RecoveryTools.PREF_NAME, PREF_FLASH_COUNTER, 1);
				return;
			default:
				mCommon.setStringPerf(mContext, RecoveryTools.PREF_NAME, RecoveryTools.PREF_HISTORY + String.valueOf(mCommon.getIntegerPerf(mContext, RecoveryTools.PREF_NAME, PREF_FLASH_COUNTER)), file.getAbsolutePath());
				mCommon.setIntegerPerf(mContext, RecoveryTools.PREF_NAME, PREF_FLASH_COUNTER, mCommon.getIntegerPerf(mContext, RecoveryTools.PREF_NAME, PREF_FLASH_COUNTER) + 1);
				if (mCommon.getIntegerPerf(mContext, RecoveryTools.PREF_NAME, PREF_FLASH_COUNTER) == 5)
					mCommon.setIntegerPerf(mContext, RecoveryTools.PREF_NAME, PREF_FLASH_COUNTER, 0);
		}
	}

	public void setKeepAppOpen(boolean keepAppOpen) {
		this.keepAppOpen = keepAppOpen;
	}
}