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
import android.widget.TextView;
import android.widget.Toast;

public class NotificationUtil {
	
	Context context;
	TextView tv;
	Dialog dialog;
	AlertDialog.Builder abuilder;
	
	public NotificationUtil(Context context) {
		this.context = context;
	}	
	public void createDialog(int Title, String Message , boolean isCancelable) {
		tv = new TextView(context);
		tv.setTextSize(20);
		tv.setText(Message);
		dialog = new Dialog(context);
		dialog.setTitle(Title);
		dialog.setContentView(tv);
		dialog.setCancelable(isCancelable);
		dialog.show();
	}
	public void createDialog(int Title, int Content, boolean isMessage, boolean isCancelable) {
		dialog = new Dialog(context);
		dialog.setTitle(Title);
		if (isMessage) {
			tv = new TextView(context);
			dialog.setContentView(tv);
			tv.setTextSize(20);
			tv.setText(Content);
		} else {
			dialog.setContentView(Content);
		}
		dialog.setCancelable(isCancelable);
		dialog.show();
	}
	public void createToast(int Message) {
		Toast
			.makeText(context, Message, Toast.LENGTH_LONG)
			.show();
	}
	public void createToast(String Message) {
		Toast
			.makeText(context, Message, Toast.LENGTH_LONG)
			.show();
	}
	public void createAlertDialog(int Title, int Message, final Runnable runOnTrue) {
		abuilder = new AlertDialog.Builder(context);
		abuilder
			.setTitle(Title)
			.setMessage(Message)
			.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				runOnTrue.run();
			}
		})
			.setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		})
			.show();
	}
	
	public void createAlertDialog(int Title, int Message, boolean PositiveButton, final Runnable runOnTrue, boolean NeutralButton, final Runnable runOnNeutral, boolean NegativeButton , final Runnable runOnNegative) {
		abuilder = new AlertDialog.Builder(context);
		abuilder
			.setTitle(Title)
			.setMessage(Message);
		
		if (PositiveButton){
			abuilder.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
			
				@Override
				public void onClick(DialogInterface dialog, int which) {
				
					runOnTrue.run();
				}
			}); 
		}
		
		if (NegativeButton){
			abuilder.setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
			
				@Override
					public void onClick(DialogInterface dialog, int which) {
					runOnNegative.run();
				}
			});
		}
		
		if (NeutralButton) {
			abuilder.setNeutralButton(R.string.neutral, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					runOnNeutral.run();
				}
			});
		}
		
		abuilder.show();
	}
}