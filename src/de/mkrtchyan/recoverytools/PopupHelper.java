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

import android.content.Context;
import android.view.MenuItem;
import android.widget.PopupMenu;

import org.sufficientlysecure.rootcommands.util.RootAccessDeniedException;

import de.mkrtchyan.utils.Common;

@SuppressWarnings("NewApi")
public class PopupHelper implements PopupMenu.OnMenuItemClickListener {
    Common mCommon = new Common();
    Context mContext;

    public PopupHelper(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.iReboot:
                try {
                    mCommon.executeSuShell(mContext, "reboot");
                } catch (RootAccessDeniedException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.iRebootRecovery:
                try {
                    mCommon.executeSuShell(mContext, "reboot recovery");
                } catch (RootAccessDeniedException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.iRebootBootloader:
                try {
                    mCommon.executeSuShell(mContext, "reboot bootloader");
                } catch (RootAccessDeniedException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.iCreateBackup:
                new BackupHandler(mContext).backup();
                return true;
            case R.id.iRestoreBackup:
                new BackupHandler(mContext).restore();
                return true;
            case R.id.iDeleteBackup:
                new BackupHandler(mContext).deleteBackup();
                return true;
            default:
                return false;
        }
    }
}
