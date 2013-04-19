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
import org.rootcommands.util.RootAccessDeniedException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class CommonUtil {

	Context context;
		
	public CommonUtil(Context context) {
		this.context = context;
	}

	public void pushFileFromRAW(File outputfile, int RAW) {
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
		if (!Folder.exists()) {
			Folder.mkdir();
		}
	}
	
	public String executeShell(String Command){
		SimpleCommand command = new SimpleCommand(Command);
		try {
			Shell shell = Shell.startRootShell();
			shell.add(command).waitForFinish();
		} catch (RootAccessDeniedException e) {} catch (IOException e) {} catch (TimeoutException e) {}
		
		return command.getOutput();
	}
	
	public void chmod(String mod, File file) {
		try {
			Shell shell = Shell.startRootShell();
			Toolbox tb = new Toolbox(shell);
			tb.setFilePermissions(file.getAbsolutePath(), mod);
		} catch (RootAccessDeniedException e) {} catch (IOException e) {} catch (TimeoutException e) {}
	}
	
	public void xdaProfile(){
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/member.php?u=5116786"));
		context.startActivity(browserIntent);
	}
	
	public void unzip(File ZipFile, File OutputFolder) { 
		if (!OutputFolder.isDirectory()
				|| !OutputFolder.exists())
			OutputFolder.mkdir();
		
		try {
			FileInputStream fin;
			fin = new FileInputStream(ZipFile);
		
			ZipInputStream zin = new ZipInputStream(fin); 
			ZipEntry ze = null; 
			while ((ze = zin.getNextEntry()) != null) { 
				if(ze.isDirectory()) { 
					File f = new File(ze.getName()); 
					if(!f.isDirectory()) { 
						f.mkdirs(); 
					}
				} else { 
					FileOutputStream fout = new FileOutputStream(OutputFolder.getAbsolutePath() + "/" + ze.getName()); 
					for (int c = zin.read(); c != -1; c = zin.read()) { 
						fout.write(c); 
					} 
					zin.closeEntry(); 
					fout.close(); 
				}
			}
	
			zin.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
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
		} catch (RootAccessDeniedException e) {e.printStackTrace();} catch (IOException e) {e.printStackTrace();}
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
		executeShell("busybox mv -f " + Source.getAbsolutePath() + " " + Destination.getAbsolutePath());
		if (Mount)
			mountDir(Destination, "RO");
	}
}
