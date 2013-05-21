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

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

public class DownloadUtil extends AsyncTask<Void, Integer, Boolean> {
	
	Context context;
	String exString = "";
	ProgressDialog downloadDialog;
	ProgressDialog connectingDialog;
	boolean first_start = true;
	Runnable AfterDownload;
	String URL;
	String FileName;
	File outputFile;
	NotificationUtil nu;
	
	
	public DownloadUtil(Context context, String URL, String FileName, File outputFile, Runnable AfterDownload) {
		this.context = context;
		this.URL = URL;
		this.FileName = FileName;
		this.outputFile = outputFile;
		this.AfterDownload = AfterDownload;
		nu = new NotificationUtil(context);
	}
	
	protected void onPreExecute(){
		connectingDialog = new ProgressDialog(context);
		connectingDialog.setTitle("Connecting to..");
		connectingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		connectingDialog.setCancelable(false);
		connectingDialog.setMessage(URL);
		connectingDialog.show();
		downloadDialog = new ProgressDialog(context);
		downloadDialog.setTitle(R.string.Downloading);
		downloadDialog.setMessage(URL + "/" + FileName);
		downloadDialog.setCancelable(false);
	}
	
	protected Boolean doInBackground(Void... params) throws NullPointerException {
		
		if (!outputFile.exists()) {
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		
			if (networkInfo != null 
					&& networkInfo.isConnected()) {
				try {
					
					URLConnection ucon =  new URL(URL + "/" + FileName).openConnection();
		
					ucon.setDoOutput(true);
					ucon.connect();
		
					FileOutputStream fileOutput = new FileOutputStream(outputFile);
					
							
					InputStream inputStream = ucon.getInputStream();
					
					byte[] buffer = new byte[1024];
					int fullLenght = ucon.getContentLength();
						
					int bufferLength = 0;
					int downloaded = 0;
							
					while ((bufferLength = inputStream.read(buffer)) > 0 ) {
						fileOutput.write(buffer, 0, bufferLength);
						downloaded += bufferLength;
						publishProgress(downloaded, fullLenght);
					}
					
					fileOutput.close();
		
				} catch (MalformedURLException e) {
					e.printStackTrace();
					exString = "MalformedURLException:\n\n" + e.getMessage() + "\n";
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					exString = exString + "IOException:\n\n" + e.getMessage();
					return false;
				}
			} else { 
				return false;
			}
		}
		return true;
	}
	
    @Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		if (first_start) {
			connectingDialog.dismiss();
			if (progress[1] >= 0) {
				downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			} else {
				downloadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			}
			downloadDialog.show();
			downloadDialog.setMax(progress[1]);
			first_start = false;
		}
		downloadDialog.setProgress(progress[0]);
	}
	
	protected void onPostExecute(Boolean success) {
		if (connectingDialog.isShowing())
			connectingDialog.dismiss();
		if(downloadDialog.isShowing())
			downloadDialog.dismiss();
		
		if (success) {
			AfterDownload.run();
		} else {
			outputFile.delete();
			if (!exString.equals("")) {
				nu.createDialog(R.string.error, String.format(exString, R.string.failed_download), true);
			} else {
				nu.createDialog(R.string.warning, R.string.noconnection, true, true);
			}
		}
	 }
	
//	public void addFile(String URL, String FileName, File outputFile) {
//		this.URL[0] = URL;
//		this.FileName[0] = FileName;
//		this.outputFile[0] = outputFile;
//	}
//	
//	public void addFiles(String[] URL, String[] FileName, File[] outputFile) {
//		
//	}
}
