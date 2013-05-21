package de.mkrtchyan.recoverytools;

import org.rootcommands.util.RootAccessDeniedException;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Notifyer;

public class Rebooter {
	
	Notifyer nu;
	Common c;
	Context context;
	
	public Rebooter(Context context) {
		
		nu = new Notifyer(context);
		c = new Common();
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
				try {
					c.executeSuShell("reboot");
				} catch (RootAccessDeniedException e) {
					e.printStackTrace();
				}
			}
		});
		
		bRebootRecovery.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					c.executeSuShell("reboot recovery");
				} catch (RootAccessDeniedException e) {
					e.printStackTrace();
				}
			}
		});
		
		bRebootBootloader.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					c.executeSuShell("reboot bootloader");
				} catch (RootAccessDeniedException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
