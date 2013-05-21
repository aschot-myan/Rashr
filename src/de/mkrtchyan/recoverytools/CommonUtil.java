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
import java.io.FileNotFoundException;
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
import org.rootcommands.util.BrokenBusyboxException;
import org.rootcommands.util.RootAccessDeniedException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;


public class CommonUtil {

		
	public CommonUtil() {}

	public static Runnable rEmpty = new Runnable() {
		@Override 
		public void run() {
			
		}};
	public File pushFileFromRAW(Context mContext, File outputfile, int RAW) {
	    if (!outputfile.exists()){
		    try {
		        InputStream is = mContext.getResources().openRawResource(RAW);
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
		
		Shell shell;
		try{
			if (su){
				try {
					shell = Shell.startRootShell();
				} catch (RootAccessDeniedException e) {
					e.printStackTrace();
					shell = Shell.startShell();
				}
			} else {
				shell = Shell.startShell();
			}
			Toolbox tb = new Toolbox(shell);
			if (!tb.getFilePermissions(file.getAbsolutePath()).equals(mod))
					tb.setFilePermissions(file.getAbsolutePath(), mod);
		} catch (IOException e) {e.printStackTrace();} catch (TimeoutException e) {e.printStackTrace();}
	}
	
	public void xdaProfile(Context mContext){
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/member.php?u=5116786"));
		mContext.startActivity(browserIntent);
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
	
	public void mountDir(File Dir, String mode) throws RootAccessDeniedException {
		try {
			Shell shell = Shell.startRootShell();
			Toolbox tb = new Toolbox(shell);
			tb.remount(Dir.getAbsolutePath(), mode);
		} catch (IOException e) {e.printStackTrace();}
	}
	
	public void copy(File Source, File Destination, boolean Mount) throws RootAccessDeniedException {
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
		executeShell("busybox mv -f " + Source.getAbsolutePath() + " " + Destination.getAbsolutePath());
		if (Mount)
			mountDir(Destination, "RO");
	}
	
	public String executeShell(String Command){
		SimpleCommand command = new SimpleCommand(Command);
		String output = "";
			
		try {
			Shell shell = Shell.startShell();
			shell.add(command).waitForFinish();
			output = command.getOutput();
		} catch (BrokenBusyboxException e) {e.printStackTrace();
		} catch (TimeoutException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();
		}
		return output;
	}
	
	public String executeShell(Context mContext, String Command){
		SimpleCommand command = new SimpleCommand(Command);
		try {
			Shell shell = Shell.startShell();
			shell.add(command).waitForFinish();
		} catch (IOException e) {e.printStackTrace();} catch (TimeoutException e) {e.printStackTrace();}
		String output = command.getOutput();
		if (getBooleanPerf(mContext, "common_util", "log")){
			
			String SuLog = "\nCommand:\n" + Command + 
					"\n\nOutput:\n" +  output;
			
			FileOutputStream fo;
			try {
				fo = mContext.openFileOutput("su-logs.log", Context.MODE_APPEND);
				fo.write(SuLog.getBytes());
			} catch (FileNotFoundException e) {e.printStackTrace();} catch (IOException e) {e.printStackTrace();}
		}
		return output;
	}
	
	public String executeSuShell(String Command) throws RootAccessDeniedException {
		String output = "";
		try {
			SimpleCommand command = new SimpleCommand(Command);
			Shell shell = Shell.startRootShell();
			shell.add(command).waitForFinish();
			output = command.getOutput();
		} catch (BrokenBusyboxException e) {e.printStackTrace();
		} catch (TimeoutException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();
		}
		return output;
	}
	
	public String executeSuShell(Context mContext, String Command) throws RootAccessDeniedException{
		String output = "";
		try {
			SimpleCommand command = new SimpleCommand(Command);
			Shell shell = Shell.startRootShell();
			shell.add(command).waitForFinish();
			output = command.getOutput();
			if (getBooleanPerf(mContext, "common_util", "log")){
				
				String SuLog = "\nCommand:\n" + Command + 
						"\n\nOutput:\n" +  output;
				
				FileOutputStream fo;
				try {
					fo = mContext.openFileOutput("su-logs.log", Context.MODE_APPEND);
					fo.write(SuLog.getBytes());
				} catch (FileNotFoundException e) {e.printStackTrace();}
			}
		} catch (BrokenBusyboxException ex) {ex.printStackTrace();
		} catch (TimeoutException ex) {ex.printStackTrace();
		} catch (IOException ex) {ex.printStackTrace();}
		return output;
	}

	public boolean getBooleanPerf(Context mContext, String PrefName, String key){
		SharedPreferences prefs = mContext.getSharedPreferences(mContext.getPackageName() + "." + PrefName, Context.MODE_PRIVATE);
		boolean pref = prefs.getBoolean(key, false);
		return pref;
	}
	
	public void setBooleanPerf(Context mContext, String PrefName, String key, Boolean value) {
		SharedPreferences.Editor editor = mContext.getSharedPreferences(mContext.getPackageName() + "." + PrefName, Context.MODE_PRIVATE).edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
}
