package de.mkrtchyan.recoverytools;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

public class Rebooter {
	
	NotificationUtil nu;
	CommonUtil cu;
	Context context;
	
	public Rebooter(Context context) {
		
		nu = new NotificationUtil(context);
		cu = new CommonUtil(context);
		this.context = context;
	
	}
	
	public void run(){
		Dialog dialog = nu.createDialog(R.string.sRebooter, R.layout.dialog_rebooter, false, true);
		Button bReboot = (Button) dialog.findViewById(R.id.bReboot);
		Button bRebootRecovery = (Button) dialog.findViewById(R.id.bRebootRecovery);
		Button bRebootBootloader = (Button) dialog.findViewById(R.id.bRebootBootloader);
		
		bReboot.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				cu.executeShell("reboot", true);
				
			}
		});
		
		bRebootRecovery.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				cu.executeShell("reboot recovery", true);
			}
		});
		
		bRebootBootloader.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				cu.executeShell("reboot bootloader", true);
			}
		});
	}

}
