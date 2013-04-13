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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.rootcommands.Shell;
import org.rootcommands.command.SimpleCommand;
import org.rootcommands.util.RootAccessDeniedException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
public class RebooterActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rebooter);
	}
	
	public void bReboot(View view) {
		executeShell(new SimpleCommand("reboot"));
	}
	
	public void bRebootRecovery(View view) {
		executeShell(new SimpleCommand("reboot recovery"));
	}
	
	public void bRebootBootloader(View view) {
		executeShell(new SimpleCommand("reboot bootloader"));
	}
	
	public void bRebooterBack(View view) {
		finish();
		System.exit(0);
	}
	
	public String executeShell(SimpleCommand Command){
		try {
			Shell shell = Shell.startRootShell();
			shell.add(Command).waitForFinish();
			shell.close();
		} catch (RootAccessDeniedException e) {} catch (IOException e) {} catch (TimeoutException e) {}
		
		return Command.getOutput();
	}
}
