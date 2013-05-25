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
