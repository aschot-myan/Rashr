package de.mkrtchyan.recoverytools.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import butterknife.BindView;
import de.mkrtchyan.recoverytools.App;
import de.mkrtchyan.recoverytools.FlashUtil;
import de.mkrtchyan.recoverytools.R;
import de.mkrtchyan.recoverytools.RashrActivity;

/**
 * Copyright (c) 2017 Aschot Mkrtchyan
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

    @BindView(R.id.optAsKernel)
    AppCompatRadioButton mOptAsKernel;
    @BindView(R.id.optAsRecovery)
    AppCompatRadioButton mOptAsRecovery;
    @BindView(R.id.bFlashAs)
    AppCompatButton mButFlashAs;
    private Context mContext;
    private File mImg;
    private RashrActivity mActivity;

    public FlashAsFragment() {
        // Required empty public constructor
    }

    public static FlashAsFragment newInstance(RashrActivity activity, File img) {
        FlashAsFragment fragment = new FlashAsFragment();
        fragment.setActivity(activity);
        fragment.setImg(img);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_flash_as, container, false);
        AppCompatTextView tvFlashAs = fragment.findViewById(R.id.tvFlashAs);
        tvFlashAs.setText(String.format(getString(R.string.flash_as).replace("%", "%%"), mImg.getName()));
        mOptAsRecovery = fragment.findViewById(R.id.optAsRecovery);
        mOptAsKernel = fragment.findViewById(R.id.optAsKernel);
        mButFlashAs = fragment.findViewById(R.id.bFlashAs);
        final AppCompatButton ButCancel = fragment.findViewById(R.id.bCancel);
        ButCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
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
                    final FlashUtil flashUtil = new FlashUtil(mActivity, mImg, job);
                    flashUtil.setOnFlashListener(new FlashUtil.OnFlashListener() {
                        @Override
                        public void onSuccess() {
                            flashUtil.showRebootDialog();
                        }

                        @Override
                        public void onFail(Exception e) {
                            App.ERRORS.add(e.toString());
                            AlertDialog.Builder d = new AlertDialog.Builder(mContext);
                            d.setTitle(R.string.flash_error);
                            d.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            if (e instanceof FlashUtil.ImageNotValidException) {
                                d.setMessage(getString(R.string.image_not_valid_message));
                                d.setNeutralButton(R.string.settings, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mActivity.switchTo(new SettingsFragment());
                                    }
                                });
                            } else if (e instanceof FlashUtil.ImageToBigException) {
                                FlashUtil.ImageToBigException exception = (FlashUtil.ImageToBigException) e;
                                //Size in MB
                                int sizeOfImage = exception.getCustomSize() / (1024 * 1024);
                                int sizeOfPart = exception.getPartitionSize() / (1024 * 1024);
                                d.setMessage(String.format(getString(R.string.image_to_big_message), sizeOfImage, sizeOfPart));
                                d.setNeutralButton(R.string.settings, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mActivity.switchTo(new SettingsFragment());
                                    }
                                });
                            } else {
                                d.setMessage(e.getMessage());
                            }
                            d.show();
                        }
                    });
                    flashUtil.execute();
                } else {
                    mActivity.finish();
                }
            }
        });
        mOptAsRecovery.setOnClickListener(listener);
        mOptAsKernel.setOnClickListener(listener);
        ViewGroup parent;
        if (!App.Device.isRecoverySupported()) {
            if ((parent = (ViewGroup) mOptAsRecovery.getParent()) != null) {
                parent.removeView(mOptAsRecovery);
                mOptAsKernel.setChecked(true);
            }
        }
        if (!App.Device.isKernelSupported()) {
            if ((parent = (ViewGroup) mOptAsKernel.getParent()) != null) {
                parent.removeView(mOptAsKernel);
            }
        }
        return fragment;
    }

    public void setActivity(RashrActivity activity) {
        mActivity = activity;
        mContext = activity;
    }

    public void setImg(File img) {
        mImg = img;
    }
}
