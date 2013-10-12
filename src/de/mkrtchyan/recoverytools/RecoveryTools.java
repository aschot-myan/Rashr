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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.devspark.appmsg.AppMsg;

import java.io.File;
import java.io.IOException;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Downloader;
import de.mkrtchyan.utils.FileChooser;
import de.mkrtchyan.utils.Notifyer;

public class RecoveryTools extends ActionBarActivity {

    public static final String TAG = "Recovery-Tools";
//  Declaring setting names
	public static final String PREF_NAME = "recovery_tools";
	public static final String PREF_ADS = "show_ads";
	public static final String PREF_CUR_VER = "current_version";
	public static final String PREF_CUSTOM_DEVICE = "custom_device_name";
	public static final String PREF_USE_CUSTOM = "use_custom_device_name";
	public static final String PREF_FIRST_RUN = "first_run";
	public static final String PREF_HISTORY = "last_history_";

    private final Context mContext = this;
//  Used paths and files
    private static final File PathToSd = Environment.getExternalStorageDirectory();
    private static final File PathToRecoveryTools = new File(PathToSd, "Recovery-Tools");
    private static final File PathToRecoveries = new File(PathToRecoveryTools, "recoveries");
    public static final File PathToCWM = new File(PathToRecoveries, "clockworkmod");
    public static final File PathToTWRP = new File(PathToRecoveries, "twrp");
    public static final File PathToBackups = new File(PathToRecoveryTools, "backups");
    public static final File PathToUtils = new File(PathToRecoveryTools, "utils");
    private File fRECOVERY;
    private String SYSTEM;
	private final String CWM_SYSTEM = "clockwork";
	private final String TWRP_SYSTEM = "twrp";

    //	Declaring needed objects
    private final Notifyer mNotifyer = new Notifyer(mContext);
    private final Common mCommon = new Common();
    private DeviceHandler mDeviceHandler = new DeviceHandler(mContext);
    private DrawerLayout mDrawerLayout = null;
    private FileChooser fcFlashOther = null;
	private boolean keepAppOpen = true;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    if(getIntent().getData() != null) {
		    keepAppOpen = false;
		    fRECOVERY = new File(getIntent().getData().getPath());
		    getIntent().setData(null);
            showFlashAlertDialog();
	    } else {
        	setContentView(R.layout.recovery_tools);
        	setupDrawerList();
        	if (BuildConfig.DEBUG) {
        	    showFakeDialog();
        	}
//      	If device is not supported, you can report it now or close the App
        	if (!mDeviceHandler.isOtherSupported()) {
        	   showDeviceNotSupportedDialog();
        	} else {
        	    if (!mCommon.getBooleanPerf(mContext, PREF_NAME, PREF_FIRST_RUN)) {
        	        showFlashWarning();
        	        mCommon.setBooleanPerf(mContext, PREF_NAME, PREF_ADS, true);
        	    }
//			    Create needed Folder
        	    checkFolder();
        	}

		    try {
			    optimizeLayout();
		    } catch (Exception e) {
			    e.printStackTrace();
		    }
		    mDeviceHandler.extractFiles(mContext);
		    mDeviceHandler.downloadUtils();
	    	showChangelog();
	    }
    }

	public void bDonate(View view) {
        startActivity(new Intent(mContext, DonationsActivity.class));
    }

    public void bXDA(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/showthread.php?t=2334554")));
    }

    public void bExit(View view) {
        finish();
        System.exit(0);
    }

    public void cbLog(View view) {
        mCommon.setBooleanPerf(mContext, Common.PREF_NAME, Common.PREF_LOG, !mCommon.getBooleanPerf(mContext, Common.PREF_NAME, Common.PREF_LOG));
        ((CheckBox) view).setChecked(mCommon.getBooleanPerf(mContext, Common.PREF_NAME, Common.PREF_LOG));
        if (((CheckBox) view).isChecked()) {
            findViewById(R.id.bShowLogs).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.bShowLogs).setVisibility(View.INVISIBLE);
        }
    }

    public void bReport(View view) {
        report(true);
    }

    public void bShowLogs(View view) {
        mCommon.showLogs(mContext);
    }

    public void cbShowAds(View view) {
        mCommon.setBooleanPerf(mContext, PREF_NAME, PREF_ADS, !mCommon.getBooleanPerf(mContext, PREF_NAME, PREF_ADS));
        ((CheckBox) view).setChecked(mCommon.getBooleanPerf(mContext, PREF_NAME, PREF_ADS));
        mNotifyer.showToast(R.string.please_restart, AppMsg.STYLE_INFO);
    }

    //	Button Methods (onClick)
    public void Go(View view) {

	    boolean updateable = true;
        fRECOVERY = null;
        if (!mDeviceHandler.downloadUtils()) {
//			Get device specified recovery file for example recovery-clockwork-touch-6.0.3.1-grouper.img
            SYSTEM = view.getTag().toString();
	        if (SYSTEM.equals(CWM_SYSTEM)) {
		        fRECOVERY = mDeviceHandler.getCWM_IMG();
	        } else if (SYSTEM.equals(TWRP_SYSTEM)) {
		        fRECOVERY = mDeviceHandler.getTWRP_IMG();
	        } else {
		        return;
	        }
	        for (File i : fRECOVERY.getParentFile().listFiles()) {
		        if (i.compareTo(fRECOVERY) == 0) {
			        updateable = false;
		        }
	        }
		    if (fRECOVERY.getParentFile().listFiles().length == 1 && !updateable) {
			    rFlasher.run();
		    } else if (updateable && fRECOVERY.getParentFile().listFiles().length > 0
			     || fRECOVERY.getParentFile().listFiles().length > 1)  {
			    final FileChooser AllIMGS = new FileChooser(mContext, fRECOVERY.getParentFile(), rFlasher);
			    AllIMGS.setEXT(mDeviceHandler.getEXT());
			    if (updateable) {
				    LinearLayout chooserLayout = AllIMGS.getLayout();
				    Button Update = new Button(mContext);
				    Update.setText(R.string.update);
				    Update.setOnClickListener(new View.OnClickListener() {

					    @Override
					    public void onClick(View v) {
						    AllIMGS.dismiss();
						    rFlasher.run();
					    }
				    });
				    chooserLayout.addView(Update);
			    }
			    AllIMGS.setTitle(SYSTEM);
			    AllIMGS.show();
		    } else {
			    rFlasher.run();
		    }
        }
    }

    public void bFlashOther(View view) {
        fRECOVERY = null;
	    try {
            fcFlashOther = new FileChooser(mContext, PathToSd, rFlasher);
	        fcFlashOther.setEXT(mDeviceHandler.getEXT());
            fcFlashOther.show();
	    } catch (NullPointerException e) {
		    e.printStackTrace();
	    }
    }

    public void bShowHistory(View view) {
	    File tmpFile[] = {null, null, null, null, null};
	    String tmpFileNames[] = {"", "", "", "", ""};
        final Dialog d = new Dialog(mContext);
        d.setTitle(R.string.sHistory);
        ListView list = new ListView(mContext);

        for (int i = 0; i < 5; i++) {
            tmpFile[i] = new File(mCommon.getStringPerf(mContext, PREF_NAME, PREF_HISTORY + String.valueOf(i)));
            if (!tmpFile[i].exists())
                mCommon.setStringPerf(mContext, PREF_NAME, PREF_HISTORY + String.valueOf(i), "");
            else {
                tmpFileNames[i] = tmpFile[i].getName();
            }
        }
        final File file[] = tmpFile;
        list.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, tmpFileNames));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                if (!arg0.getItemAtPosition(arg2).toString().equals("")) {
                    fRECOVERY = file[arg2];
                    rFlasher.run();
                } else {
                    d.dismiss();
                    mNotifyer.showToast(R.string.no_choosed, AppMsg.STYLE_CONFIRM);
                }
            }
        });
        d.setContentView(list);
        d.show();
    }

    public void bBackupMgr(View view) {
        showPopup(R.menu.bakmgr_menu, view);
    }

    public void bCleareCache(View view) {
        try {
            mCommon.deleteFolder(PathToCWM, false);
            mCommon.deleteFolder(PathToTWRP, false);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void bRebooter(View view) {
        showPopup(R.menu.rebooter_menu, view);
    }

    public void report(boolean isCancelable) {
//		Creates a report Email including a Comment and important device infos
        final Dialog reportDialog = mNotifyer.createDialog(R.string.commentar, R.layout.dialog_comment, false, true);
        if (!isCancelable)
            reportDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    System.exit(0);
                    finish();
                }
            });
        final Button bGo = (Button) reportDialog.findViewById(R.id.bGo);
        bGo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!mCommon.getBooleanPerf(mContext, PREF_NAME, PREF_ADS))
                    mNotifyer.showToast(R.string.please_ads, AppMsg.STYLE_ALERT);
				mNotifyer.showToast(R.string.donate_to_support, AppMsg.STYLE_ALERT);
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    EditText text = (EditText) reportDialog.findViewById(R.id.etCommentar);
                    String comment = text.getText().toString();
                    String message = "Package Infos:" +
                            "\n\nName: " + pInfo.packageName +
                            "\nVersionName: " + pInfo.versionName +
                            "\nVersionCode: " + pInfo.versionCode +
                            "\n\n\nProduct Info: " +
                            "\n\nManufacture: " + android.os.Build.MANUFACTURER +
                            "\nDevice: " + Build.DEVICE + " (" + mDeviceHandler.DEV_NAME + ")" +
                            "\nBoard: " + Build.BOARD +
                            "\nBrand: " + Build.BRAND +
                            "\nModel: " + Build.MODEL +
                            "\nFingerprint: " + Build.FINGERPRINT +
                            "\nAndroid SDK Level: " + Build.VERSION.CODENAME + " (" + Build.VERSION.SDK_INT + ")" +
                            "\n\n\n===========Comment==========\n" + comment +
                            "\n===========Comment==========\n" +
                            "\nMTD Testresult:\n" +
                            mCommon.executeSuShell("cat /proc/mtd") + "\n" +
                            "\nDevice Tree:\n" +
                            "\n" + mCommon.executeSuShell("ls -lR /dev/block");
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ashotmkrtchyan1995@gmail.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Recovery-Tools report");
                    intent.putExtra(Intent.EXTRA_TEXT, message);
                    startActivity(Intent.createChooser(intent, "Send over Gmail"));
                    reportDialog.dismiss();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
					e.printStackTrace();
                }
            }
        });
        reportDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            if (mDrawerLayout.isDrawerOpen(Gravity.LEFT))
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            else
                mDrawerLayout.openDrawer(Gravity.LEFT);
        return super.onOptionsItemSelected(item);
    }

    public void showPopup(int Menu, View v) {
        PopupMenu popup = new PopupMenu(mContext, v);
        popup.getMenuInflater().inflate(Menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {
                    switch (menuItem.getItemId()) {
                        case R.id.iReboot:
                            mCommon.executeSuShell(mContext, "reboot");
                            return true;
                        case R.id.iRebootRecovery:
                            mCommon.executeSuShell(mContext, "reboot recovery");
                            return true;
                        case R.id.iRebootBootloader:
                            new Common().executeSuShell(mContext, "reboot bootloader");
                            return true;
                        case R.id.iCreateBackup:
                            new BackupHandler(mContext).backup();
                            return true;
                        case R.id.iRestoreBackup:
                            new BackupHandler(mContext).restore();
                            return true;
                        case R.id.iDeleteBackup:
                            new BackupHandler(mContext).deleteBackup();
                            return true;
                        default:
                            return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        });
        popup.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (!mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            } else {
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showFakeDialog() {
        // Fake other devices
        final Dialog FakerDialog = new Dialog(mContext);
        FakerDialog.setTitle("Set your preferred device name");
        final LinearLayout FakerLayout = new LinearLayout(mContext);
        FakerLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText etFakeDevName = new EditText(mContext);
        if (!mCommon.getStringPerf(mContext, PREF_NAME, PREF_CUSTOM_DEVICE).equals("")) {
            etFakeDevName.setHint(mCommon.getStringPerf(mContext, PREF_NAME, PREF_CUSTOM_DEVICE));
            mDeviceHandler = new DeviceHandler(mContext, mCommon.getStringPerf(mContext, PREF_NAME, PREF_CUSTOM_DEVICE));
        } else {
            etFakeDevName.setHint(Build.DEVICE);
        }
        final Button reset = new Button(mContext);
        reset.setText(R.string.go);
        reset.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
		        mCommon.setStringPerf(mContext, PREF_NAME, PREF_CUSTOM_DEVICE, etFakeDevName.getText().toString());
		        FakerDialog.dismiss();
		        mNotifyer.showToast(R.string.please_restart, AppMsg.STYLE_INFO);
	        }
        });
        final Button setDefault = new Button(mContext);
        setDefault.setText("Reset to Default");
        setDefault.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
		        mCommon.setStringPerf(mContext, PREF_NAME, PREF_CUSTOM_DEVICE, Build.DEVICE);
		        mCommon.setBooleanPerf(mContext, PREF_NAME, PREF_USE_CUSTOM, false);
		        etFakeDevName.setHint(Build.DEVICE);
		        FakerDialog.dismiss();
		        mNotifyer.showToast(R.string.please_restart, AppMsg.STYLE_INFO);

	        }
        });
        FakerLayout.addView(etFakeDevName);
        FakerLayout.addView(reset);
        FakerLayout.addView(setDefault);
        FakerDialog.setContentView(FakerLayout);
        FakerDialog.show();
    }

	public void optimizeLayout() throws Exception{
//	    Show Advanced information how device will be Flashed
		final CheckBox cbMethod = (CheckBox) findViewById(R.id.cbMethod);
		if (mDeviceHandler.isOverRecovery()) {
			cbMethod.setText(R.string.over_recovery);
		} else if (mDeviceHandler.isDD()) {
			cbMethod.setText(R.string.using_dd);
		} else if (mDeviceHandler.isMTD()) {
			cbMethod.setText(R.string.using_mtd);
		}
		final LinearLayout mDrawerLinear = (LinearLayout) findViewById(R.id.left_drawer);
		CheckBox cbShowAds = (CheckBox) mDrawerLinear.findViewById(R.id.cbShowAds);
		CheckBox cbLog = (CheckBox) mDrawerLinear.findViewById(R.id.cbLog);
		cbShowAds.setChecked(mCommon.getBooleanPerf(mContext, PREF_NAME, PREF_ADS));
		cbLog.setChecked(mCommon.getBooleanPerf(mContext, Common.PREF_NAME, Common.PREF_LOG));
		if (cbLog.isChecked()) {
			findViewById(R.id.bShowLogs).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.bShowLogs).setVisibility(View.INVISIBLE);
		}
		((CheckBox) mDrawerLayout.findViewById(R.id.cbShowAds)).setChecked(mCommon.getBooleanPerf(mContext, PREF_NAME, PREF_ADS));
		try {
			if (!mCommon.getBooleanPerf(mContext, PREF_NAME, PREF_ADS)) {
				((ViewGroup) findViewById(R.id.adView).getParent()).removeView(findViewById(R.id.adView));
			}
			if (!mDeviceHandler.isCwmSupported()) {
				((ViewGroup) findViewById(R.id.bCWM).getParent()).removeView(findViewById(R.id.bCWM));
			}
			if (!mDeviceHandler.isTwrpSupported()) {
				((ViewGroup) findViewById(R.id.bTWRP).getParent()).removeView(findViewById(R.id.bTWRP));
			}
			if (mDeviceHandler.isOverRecovery()) {
				((ViewGroup) findViewById(R.id.bBAK_MGR).getParent()).removeView(findViewById(R.id.bBAK_MGR));
				((ViewGroup) findViewById(R.id.bHistory).getParent()).removeView(findViewById(R.id.bHistory));
			}
			if (!mDeviceHandler.isOtherSupported())
				((ViewGroup) findViewById(R.id.bFlashOther).getParent()).removeAllViews();
		} catch (NullPointerException e) {
			throw new Exception("Error while setting up Layout");
		}

	}

	private void checkFolder() {
		mCommon.checkFolder(PathToRecoveryTools);
		mCommon.checkFolder(PathToRecoveries);
		mCommon.checkFolder(PathToCWM);
		mCommon.checkFolder(PathToTWRP);
		mCommon.checkFolder(PathToBackups);
		mCommon.checkFolder(PathToUtils);
		mCommon.checkFolder(new File(PathToUtils, mDeviceHandler.DEV_NAME));
	}

	public void showOverRecoveryInstructions() {
		final AlertDialog.Builder abuilder = new AlertDialog.Builder(mContext);
		abuilder
				.setTitle(R.string.info)
				.setMessage(R.string.flash_over_recovery)
				.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						bRebooter(findViewById(R.id.bRebooter));
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

	public void showChangelog() {
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			final int previous_version = mCommon.getIntegerPerf(mContext, PREF_NAME, PREF_CUR_VER);
			final int current_version = pInfo.versionCode;
			if (current_version > previous_version) {
				mNotifyer.createDialog(R.string.version, R.string.changes, true, true).show();
				mCommon.setIntegerPerf(mContext, PREF_NAME, PREF_CUR_VER, current_version);
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	public void showFlashAlertDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
		alert
				.setTitle(R.string.warning)
				.setMessage(String.format(mContext.getString(R.string.choose_message), fRECOVERY.getName()))
				.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						rFlasher.run();
					}
				})
				.setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						finish();
						System.exit(0);
					}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialogInterface) {

					}
				})
				.show();
	}

	private void showDeviceNotSupportedDialog() {
		AlertDialog.Builder DeviceNotSupported = mNotifyer.createAlertDialog(R.string.warning, R.string.notsupportded,
				new Runnable() {
					@Override
					public void run() {
						report(false);
					}
				}, null,
				new Runnable() {
					@Override
					public void run() {
						finish();
						System.exit(0);
					}
				}
		);
		DeviceNotSupported.setCancelable(BuildConfig.DEBUG);
		DeviceNotSupported.show();
	}

	public void setupDrawerList() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.settings, R.string.app_name);
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		mDrawerToggle.syncState();
		mDrawerToggle.setDrawerIndicatorEnabled(true);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	public void showFlashWarning() {
		if (mDeviceHandler.getDevType() != DeviceHandler.DEV_TYPE_RECOVERY && mCommon.suRecognition()) {
			final AlertDialog.Builder WarningDialog = new AlertDialog.Builder(mContext);
			WarningDialog.setTitle(R.string.warning);
			WarningDialog.setMessage(String.format(getString(R.string.bak_warning), PathToBackups.getAbsolutePath()));
			WarningDialog.setPositiveButton(R.string.sBackup, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					new BackupHandler(mContext).backup();
					mCommon.setBooleanPerf(mContext, PREF_NAME, PREF_FIRST_RUN, true);
				}
			});
			WarningDialog.setNegativeButton(R.string.risk, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					mCommon.setBooleanPerf(mContext, PREF_NAME, PREF_FIRST_RUN, true);
				}
			});
			WarningDialog.setCancelable(false);
			WarningDialog.show();
		}
	}

	//	"Methods" need a input from user (AlertDialog) or at the end of AsyncTasks
	private final Runnable rFlash = new Runnable() {
		@Override
		public void run() {
			FlashUtil flashUtil = new FlashUtil(mContext, fRECOVERY, FlashUtil.JOB_FLASH);
			flashUtil.setKeepAppOpen(keepAppOpen);
			flashUtil.execute();
		}
	};
	private final Runnable rFlasher = new Runnable() {
		@Override
		public void run() {

			if (!mCommon.suRecognition()) {
				mNotifyer.showRootDeniedDialog();
			} else {
				if (fcFlashOther != null) {
					if (fcFlashOther.isChoosed()) {
						fRECOVERY = fcFlashOther.getSelectedFile();
					}
				}
				if (fRECOVERY != (null)) {
					if (fRECOVERY.exists()) {
						if (fRECOVERY.getName().endsWith(mDeviceHandler.getEXT())){
//				            If the flashing don't be handle specially flash it
							if (!mDeviceHandler.isKernelFlashed() && !mDeviceHandler.isOverRecovery()) {
								rFlash.run();
							} else {
//		        	            Get user input if Kernel will be modified
								if (mDeviceHandler.isKernelFlashed())
									mNotifyer.createAlertDialog(R.string.warning, R.string.kernel_to, rFlash).show();
//					            Get user input if user want to install over recovery now
								if (mDeviceHandler.isOverRecovery()) {
									showOverRecoveryInstructions();
								}
							}
						}
					} else {
//  				    If Recovery File don't exist ask if you want to download it now.
						mNotifyer.createAlertDialog(R.string.info, R.string.img_not_found, rDownload).show();
					}
				}
			}
		}
	};

	private final Runnable rDownload = new Runnable() {
		@Override
		public void run() {
			Downloader RecoveryDownloader;
			String url;
//			Download file from URL mDeviceHandler."SYSTEM"_URL + "/" + fRECOVERY.getName().toString() and write it to fRECOVERY
			if (SYSTEM.equals(CWM_SYSTEM)) {
				url = mDeviceHandler.getCWM_URL();
			} else if (SYSTEM.equals(TWRP_SYSTEM)) {
				url = mDeviceHandler.getTWRP_URL();
			} else {
				return;
			}
			RecoveryDownloader = new Downloader(mContext, url, fRECOVERY.getName(), fRECOVERY, rFlasher);
			RecoveryDownloader.setRetry(true);
			RecoveryDownloader.execute();
		}
	};
}