package de.mkrtchyan.recoverytools;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import java.io.File;

/**
 * Copyright (c) 2015 Aschot Mkrtchyan
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class FlashAsFragment extends Fragment {

    private Device mDevice;
    private Context mContext;
    private File mImg;
    private RashrActivity mActivity;
    private RadioButton mOptAsKernel, mOptAsRecovery;
    private AppCompatButton mButFlashAs;
    private boolean mCloseApp;

    public FlashAsFragment() {
        // Required empty public constructor
    }

    public static FlashAsFragment newInstance(RashrActivity activity, File img, boolean CloseApp) {
        FlashAsFragment fragment = new FlashAsFragment();
        fragment.setActivity(activity);
        fragment.setDevice(activity.getDevice());
        fragment.setImg(img);
        fragment.setCloseApp(CloseApp);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_flash_as, container, false);
        AppCompatTextView tvFlashAs = (AppCompatTextView) fragment.findViewById(R.id.tvFlashAs);
        tvFlashAs.setText(String.format(getString(R.string.flash_as), mImg.getName()));
        mOptAsRecovery = (RadioButton) fragment.findViewById(R.id.optAsRecovery);
        mOptAsKernel = (RadioButton) fragment.findViewById(R.id.optAsKernel);
        mButFlashAs = (AppCompatButton) fragment.findViewById(R.id.bFlashAs);
        final AppCompatButton ButCancel = (AppCompatButton) fragment.findViewById(R.id.bCancel);
        ButCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCloseApp) {
                    new AlertDialog.Builder(mContext)
                            .setTitle(R.string.exit_app)
                            .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mActivity.finish();
                                }
                            })
                            .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mActivity.switchTo(FlashFragment.newInstance(mActivity));
                                }
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    mActivity.finish();
                }
            }
        });
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag().toString().equals("recovery")) {
                    mOptAsKernel.setChecked(false);
                } else {
                    mOptAsRecovery.setChecked(false);
                }
                mButFlashAs.setEnabled(true);
            }
        };
        mButFlashAs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mImg.exists()) {
                    int job = mOptAsRecovery.isChecked() ?
                            FlashUtil.JOB_FLASH_RECOVERY : FlashUtil.JOB_FLASH_KERNEL;
                    FlashUtil flashUtil = new FlashUtil(mActivity, mImg, job);
                    flashUtil.setKeepAppOpen(false);
                    flashUtil.execute();
                } else {
                    mActivity.finish();
                }
            }
        });
        mOptAsRecovery.setOnClickListener(listener);
        mOptAsKernel.setOnClickListener(listener);
        ViewGroup parent;
        if (!mDevice.isRecoverySupported()) {
            if ((parent = (ViewGroup) mOptAsRecovery.getParent()) != null) {
                parent.removeView(mOptAsRecovery);
                mOptAsKernel.setChecked(true);
            }
        }
        if (!mDevice.isKernelSupported()) {
            if ((parent = (ViewGroup) mOptAsKernel.getParent()) != null) {
                parent.removeView((mOptAsKernel));
            }
        }
        return fragment;
    }

    public void setDevice(Device device) {
        mDevice = device;
    }

    public void setActivity(RashrActivity activity) {
        mActivity = activity;
        mContext = activity;
    }

    public void setImg(File img) {
        mImg = img;
    }

    public void setCloseApp(boolean closeApp) {
        mCloseApp = closeApp;
    }
}
