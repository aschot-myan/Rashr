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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.rootcommands.Shell;
import org.rootcommands.Toolbox;
import org.rootcommands.command.SimpleCommand;
import org.rootcommands.util.RootAccessDeniedException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;


public class CommonUtil {

	Context context;
	Shell shell;
	public String SuLog = "";
		
	public CommonUtil(Context context) {
		this.context = context;
	}

	public File pushFileFromRAW(File outputfile, int RAW) {
	    if (!outputfile.exists()){
		    try {
		        InputStream is = context.getResources().openRawResource(RAW);
		        OutputStream os = new FileOutputStream(outputfile);
		        byte[] data = new byte[is.available()];
		        is.read(data);
		        os.write(data);
		        is.close();
		        os.close();
		    } catch (IOException e) {}
	    }
	    return outputfile;
	}
	
	public boolean suRecognition() {
		try {
			Shell shell = Shell.startRootShell();
	        Toolbox tb = new Toolbox(shell);
	        return tb.isRootAccessGiven();
		} catch (Exception e){
			return false;
		}
	}
	
	public void checkFolder(File Folder) {
		if (!Folder.exists()
				|| !Folder.isDirectory()) {
			Folder.mkdir();
		}
	}
	
	public void chmod(File file, String mod, boolean su) {
		
		try{
			if (su){
				try {
					shell = Shell.startRootShell();
				} catch (RootAccessDeniedException e) {}
			} else {
				shell = Shell.startShell();
		}
		Toolbox tb = new Toolbox(shell);
			if (!tb.getFilePermissions(file.getAbsolutePath()).equals(mod))
					tb.setFilePermissions(file.getAbsolutePath(), mod);
		} catch (IOException e) {} catch (TimeoutException e) {}
	}
	
	public void xdaProfile(){
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/member.php?u=5116786"));
		context.startActivity(browserIntent);
	}
	
	public void unzip(File ZipFile, File OutputFolder) { 
		
		FileInputStream fin;
		ZipInputStream zin;
		ZipEntry ze = null; 
		
		try { 
			fin = new FileInputStream(ZipFile); 
			zin = new ZipInputStream(fin); 
			
			while ((ze = zin.getNextEntry()) != null) { 
	
				if(ze.isDirectory()) { 
					checkFolder(new File(OutputFolder, ze.getName())); 
				} else {
					File file = new File(OutputFolder.getAbsolutePath(), ze.getName());
					FileOutputStream fout = new FileOutputStream(file); 
					for (int c = zin.read(); c != -1; c = zin.read()) { 
						fout.write(c); 
					} 
					zin.closeEntry(); 
					fout.close(); 
				} 
			} 
			zin.close(); 
		} catch(Exception e) {} 
	
	}
	
	public void deleteFolder(File Folder, boolean AndFolder){
		if(Folder.exists()
				&& Folder.isDirectory()) {
			File[] files = Folder.listFiles();
			for(int i = 0; i < files.length; i++) {
				files[i].delete();
			}
			if (AndFolder)
				Folder.delete();
		}
	}
	
	public void mountDir(File Dir, String mode){
		try {
			Shell shell = Shell.startRootShell();
			Toolbox tb = new Toolbox(shell);
			tb.remount(Dir.getAbsolutePath(), mode);
		} catch (RootAccessDeniedException e) {} catch (IOException e) {}
	}
	
	public void copy(File Source, File Destination, boolean Mount){
		if (Mount)
			mountDir(Destination, "RW");
		File[] files = Source.listFiles();
		for(int i = 0; i < files.length; i++) {
			if (files[i].isDirectory() 
					&& files[i].exists()){
				if (Mount)
					mountDir(new File("/" + files[i].getName().toString()), "RW");
			}
		}
		executeShell("busybox mv -f " + Source.getAbsolutePath() + " " + Destination.getAbsolutePath(), true);
		if (Mount)
			mountDir(Destination, "RO");
	}
	
	public String executeShell(String Command, boolean su){
		SimpleCommand command = new SimpleCommand(Command);
		try {
			Shell shell;
			if (su){
				shell = Shell.startRootShell();
			} else {
				shell = Shell.startShell();
			}
			shell.add(command).waitForFinish();
		} catch (RootAccessDeniedException e) {} catch (IOException e) {} catch (TimeoutException e) {}
		String output = command.getOutput();
		if (getBooleanPerf("common_util", "log")){
			if (!SuLog.equals("")) {
				SuLog = SuLog + 
					"\n\nCommand:\n" + Command + 
					"\n\nOutput:\n" +  output;
			} else {
				SuLog = "Command:\n" + Command + 
						"\n\nOutput:\n" +  output;
			}
		}
			
		return output;
	}

	public boolean getBooleanPerf(String PrefName, String key){
		SharedPreferences prefs = context.getSharedPreferences(context.getPackageName() + "." + PrefName, Context.MODE_PRIVATE);
		boolean pref = prefs.getBoolean(key, false);
		return pref;
	}
	
	public void setBooleanPerf(String PrefName, String key, Boolean value) {
		SharedPreferences.Editor editor = context.getSharedPreferences(context.getPackageName() + "." + PrefName, Context.MODE_PRIVATE).edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
}
