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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import org.rootcommands.util.RootAccessDeniedException;

import de.mkrtchyan.utils.Common;

public class Rebooter extends Dialog {

    public Rebooter(Context mContext) {
        super(mContext);
        final Context context = mContext;
        final Common mCommon = new Common();
        setTitle(R.string.sRebooter);
        setContentView(R.layout.dialog_rebooter);
        Button bReboot = (Button) findViewById(R.id.bReboot);
        Button bRebootRecovery = (Button) findViewById(R.id.bRebootRecovery);
        Button bRebootBootloader = (Button) findViewById(R.id.bRebootBootloader);

        bReboot.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    mCommon.executeSuShell(context, "reboot");
                } catch (RootAccessDeniedException e) {
                    e.printStackTrace();
                }
            }
        });

        bRebootRecovery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    mCommon.executeSuShell("reboot recovery");
                } catch (RootAccessDeniedException e) {
                    e.printStackTrace();
                }
            }
        });

        bRebootBootloader.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    mCommon.executeSuShell(context, "reboot bootloader");
                } catch (RootAccessDeniedException e) {
                    e.printStackTrace();
                }
            }
        });
        show();
    }
}
