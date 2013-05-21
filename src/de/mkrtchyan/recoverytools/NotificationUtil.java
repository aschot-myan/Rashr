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
	
	Context mContext;
	
	public NotificationUtil(Context mContext) {
		this.mContext = mContext;
	}	
	public Dialog createDialog(int Title, String Message , boolean isCancelable) {
		TextView tv = new TextView(mContext);
		tv.setTextSize(20);
		tv.setText(Message);
		Dialog dialog = new Dialog(mContext);
		dialog.setTitle(Title);
		dialog.setContentView(tv);
		dialog.setCancelable(isCancelable);
		dialog.show();
		return dialog;
	}
	public Dialog createDialog(int Title, int Content, boolean isMessage, boolean isCancelable) {
		Dialog dialog = new Dialog(mContext);
		dialog.setTitle(Title);
		if (isMessage) {
			TextView tv = new TextView(mContext);
			dialog.setContentView(tv);
			tv.setTextSize(20);
			tv.setText(Content);
		} else {
			dialog.setContentView(Content);
		}
		dialog.setCancelable(isCancelable);
		dialog.show();
		return dialog;
	}
	public void createToast(int Message) {
		Toast
			.makeText(mContext, Message, Toast.LENGTH_LONG)
			.show();
	}
	public void createToast(String Message) {
		Toast
			.makeText(mContext, Message, Toast.LENGTH_LONG)
			.show();
	}
	public AlertDialog.Builder createAlertDialog(int Title, int Message, final Runnable runOnTrue) {
		AlertDialog.Builder abuilder = new AlertDialog.Builder(mContext);
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
		return abuilder;
	}
	
	public AlertDialog.Builder createAlertDialog(int Title, String Message, final Runnable runOnTrue) {
		AlertDialog.Builder abuilder = new AlertDialog.Builder(mContext);
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
		return abuilder;
	}
	
	public AlertDialog.Builder createAlertDialog(int Title, int Message, final Runnable runOnTrue, final Runnable runOnNeutral, final Runnable runOnNegative) {
		AlertDialog.Builder abuilder = new AlertDialog.Builder(mContext);
		abuilder
			.setTitle(Title)
			.setMessage(Message);
		
		if (!runOnTrue.equals(null)){
			abuilder.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
			
				@Override
				public void onClick(DialogInterface dialog, int which) {
				
					runOnTrue.run();
				}
			}); 
		}
		
		if (!runOnNegative.equals(null)){
			abuilder.setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
			
				@Override
					public void onClick(DialogInterface dialog, int which) {
					runOnNegative.run();
				}
			});
		}
		
		if (!runOnNeutral.equals(null)) {
			abuilder.setNeutralButton(R.string.neutral, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					runOnNeutral.run();
				}
			});
		}
		
		abuilder.show();
		
		return abuilder;
	}
}