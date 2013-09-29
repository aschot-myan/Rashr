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

import org.rootcommands.util.RootAccessDeniedException;

import java.io.File;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Notifyer;

public class FlashUtil extends AsyncTask<Void, Void, Boolean> {

	public static final int JOB_FLASH = 1;
	public static final int JOB_BACKUP = 2;
	private Context mContext;
	private static final String TAG = "FlashUtil";
	private ProgressDialog pDialog;
	final private Common mCommon = new Common();
	private Notifyer mNotifyer;
	private final DeviceHandler mDeviceHandler;
	private File file;
	private int JOB;

	public FlashUtil(Context mContext, File file, int JOB) {
		this.mContext = mContext;
		this.file = file;
		this.JOB = JOB;
		mNotifyer = new Notifyer(mContext);
		mDeviceHandler = new DeviceHandler(mContext);
	}

	protected void onPreExecute() {

		Log.d(TAG, "Preparing to flash");
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
						if (mDeviceHandler.isMTD()) {
							File fflash = new File(mContext.getFilesDir(), "flash_image");
							mCommon.chmod(fflash, "741");
							mCommon.executeSuShell(mContext, fflash.getAbsolutePath() + " recovery " + file.getAbsolutePath());

						} else if (!mDeviceHandler.getRecoveryPath().equals(""))
							mCommon.executeSuShell(mContext, "dd if=" + file.getAbsolutePath() + " of=" + mDeviceHandler.getRecoveryPath());
						if (mDeviceHandler.DEVICE_NAME.equals("c6603")
								|| mDeviceHandler.DEVICE_NAME.equals("montblanc")) {
							if (mCommon.getBooleanPerf(mContext, "flash-util", "first-flash")) {
								mCommon.mountDir(new File(mDeviceHandler.getRecoveryPath()), "RW");
								mCommon.executeSuShell(mContext, "cat " + mDeviceHandler.charger.getAbsolutePath() + " >> /system/bin/" + mDeviceHandler.charger.getName());
								mCommon.executeSuShell(mContext, "cat " + mDeviceHandler.chargermon.getAbsolutePath() + " >> /system/bin/" + mDeviceHandler.chargermon.getName());
								if (mDeviceHandler.DEVICE_NAME.equals("c6603")) {
									mCommon.executeSuShell(mContext, "cat " + mDeviceHandler.ric.getAbsolutePath() + " >> /system/bin/" + mDeviceHandler.ric.getName());
									mCommon.chmod(mDeviceHandler.ric, "755");
								}
								mCommon.chmod(mDeviceHandler.charger, "755");
								mCommon.chmod(mDeviceHandler.chargermon, "755");
							}
							mCommon.chmod(file, "644");
							mCommon.mountDir(new File(mDeviceHandler.getRecoveryPath()), "RO");
						}
					}
					break;

				case 2:
					if (mDeviceHandler.isMTD()) {
						File fdump = new File(mContext.getFilesDir(), "dump_image");
						mCommon.chmod(mDeviceHandler.fdump, "741");
						mCommon.executeSuShell(mContext, fdump.getAbsolutePath() + " recovery " + file.getAbsolutePath());
					} else if (!mDeviceHandler.getRecoveryPath().equals(""))
						mCommon.executeSuShell(mContext, "dd if=" + mDeviceHandler.getRecoveryPath() + " of=" + file.getAbsolutePath());
					break;
			}
		} catch (RootAccessDeniedException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void onPostExecute(Boolean succes) {

		Log.i(TAG, "Flashing finished");

		pDialog.dismiss();
		if (JOB == 1) {
			if (!mCommon.getBooleanPerf(mContext, "recovery-tools", "never_show")) {
				AlertDialog.Builder abuilder = new AlertDialog.Builder(mContext);
				abuilder.setTitle(R.string.tsk_end)
						.setMessage(mContext.getString(R.string.flashed) + " " + mContext.getString(R.string.reboot_recovery_now))
						.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								try {
									mCommon.executeSuShell(mContext, "reboot recovery");
								} catch (RootAccessDeniedException e) {
									e.printStackTrace();
								}
							}
						})
						.setNeutralButton(R.string.neutral, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {

							}
						})
						.setNegativeButton(R.string.never_again, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								mCommon.setBooleanPerf(mContext, "recovery-tools", "never_show", true);
							}
						})
						.show();
			}
			switch (mCommon.getIntegerPerf(mContext, "recovery-tools", "last_counter")) {
				case 0:
					mCommon.setStringPerf(mContext, "recovery-tools", "last_history_" + String.valueOf(mCommon.getIntegerPerf(mContext, "recovery-tools", "last_counter")), file.getAbsolutePath());
					mCommon.setIntegerPerf(mContext, "recovery-tools", "last_counter", 1);
					return;
				default:
					mCommon.setStringPerf(mContext, "recovery-tools", "last_history_" + String.valueOf(mCommon.getIntegerPerf(mContext, "recovery-tools", "last_counter")), file.getAbsolutePath());
					mCommon.setIntegerPerf(mContext, "recovery-tools", "last_counter", mCommon.getIntegerPerf(mContext, "recovery-tools", "last_counter") + 1);
					if (mCommon.getIntegerPerf(mContext, "recovery-tools", "last_counter") == 5)
						mCommon.setIntegerPerf(mContext, "recovery-tools", "last_counter", 0);
			}
		} else {
			mNotifyer.showToast(R.string.bak_done, AppMsg.STYLE_INFO);
		}

		Appirater.appLaunched(mContext);
		mCommon.setBooleanPerf(mContext, "flash-util", "first-flash", false);
	}
}