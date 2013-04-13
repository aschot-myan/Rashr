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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;

public class DownloadUtil extends AsyncTask<Void, Integer, Boolean> {
	
	Context context;
	Dialog dialog;
	Runnable AfterDownload;
	private static String URL;
	private static File outputFile;
	public static boolean output;
	NotificationUtil nu = new NotificationUtil(context);
	
	
	public DownloadUtil(Context context, String URLAdress, File outputfile, Runnable AfterDownload) {
		this.context = context;
		URL = URLAdress;
		outputFile = outputfile;
		this.AfterDownload = AfterDownload;
	}
	
	protected void onPreExecute(){
		dialog = new Dialog(context);
		dialog.setContentView(R.layout.activity_downloading);
		dialog.setTitle(R.string.app_name);
		dialog.setCancelable(false);
		dialog.show();
	}
	
	protected Boolean doInBackground(Void... params) throws NullPointerException {
		
		try {
			URL url = new URL(URL);
								
			URLConnection ucon = url.openConnection();

			ucon.setDoOutput(true);
			ucon.connect();

			FileOutputStream fileOutput = new FileOutputStream(outputFile);
					
			InputStream inputStream = ucon.getInputStream();
			
			byte[] buffer = new byte[1024];
			int bufferLength = 0;
					
			while ((bufferLength = inputStream.read(buffer)) > 0 ) {
				fileOutput.write(buffer, 0, bufferLength);
			}
			
			fileOutput.close();
							
			output = true;

		} catch (MalformedURLException e) {
			
			System.out.println(e.toString());
			e.printStackTrace();
			output = false;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.toString());
			output = false;
		}
		return output;
	}
	
    @Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
	}
	
	protected void onPostExecute(Boolean success) {
		AfterDownload.run();
		dialog.dismiss();
	 }
}
