/*************************************************************************************
 * Copyright (C) 2012-2014 Kristian Lauszus, TKJ Electronics. All rights reserved.
 *
 * This software may be distributed and modified under the terms of the GNU
 * General Public License version 2 (GPL2) as published by the Free Software
 * Foundation and appearing in the file GPL2.TXT included in the packaging of
 * this file. Please note that GPL2 Section 2[b] requires that all works based
 * on this software must also be made publicly available under the terms of
 * the GPL2 ("Copyleft").
 *
 * Contact information
 * -------------------
 *
 * Kristian Lauszus, TKJ Electronics
 * Web      :  http://www.tkjelectronics.com
 * e-mail   :  kristianl@tkjelectronics.com
 *
 ************************************************************************************/

package com.tkjelectronics.balanduino;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.SeekBar;

import com.actionbarsherlock.app.SherlockFragment;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class JoystickFragment extends SherlockFragment implements JoystickView.OnJoystickChangeListener {
    private DecimalFormat d = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
    private DecimalFormat d2 = new DecimalFormat("#.##");
    private JoystickView mJoystick;
    private TextView mText1, mText2;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;

    private double xValue, yValue;
    private double old_xValue, old_yValue;

    private boolean joystickReleased;

    private SeekBar mheighbar;
    private int mheighbar_value;
    private boolean mheighbar_flag = false;
    private boolean mJoystick_flag = false;

    private double filterx []= {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
    private int x_idx = 0;
    private double filtery []= {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
    private int y_idx = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.joystick, container, false);

        mJoystick = (JoystickView) v.findViewById(R.id.joystick);
        mJoystick.setOnJoystickChangeListener(this);

        mText1 = (TextView) v.findViewById(R.id.textView1);
        mText1.setText(R.string.defaultJoystickValue);
        mheighbar = (SeekBar) v.findViewById(R.id.heighbar);
        mText2 = (TextView) v.findViewById(R.id.textView2);
        mText2.setText(String.valueOf(mheighbar.getProgress()));


        mheighbar.setMax(100);
        mheighbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
//                int newKpValue = (int) progress; // Since the SeekBar can only handle integers, so this is needed
                mText2.setText(String.valueOf(progress));
                mheighbar_value = progress;
                mheighbar_flag = true;//set flag here to send its changed value in the interupt service routine below - check bottom of this page
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
//        mheighbar.setProgress(mheighbar.getMax() / 2);//for initial stage
        mheighbar.setProgress(0);//for initial stage



        BalanduinoActivity.joystickReleased = true;

        return v;
    }

    private void newData(double xValue, double yValue, boolean joystickReleased) {
        if (xValue == 0 && yValue == 0)
            joystickReleased = true;

        CustomViewPager.setPagingEnabled(joystickReleased);
        BalanduinoActivity.joystickReleased = joystickReleased;
        this.joystickReleased = joystickReleased;

        if(joystickReleased){
            //Log.d("newData", "newData");
            mJoystick_flag = true;
            this.xValue = 0;
            this.yValue = 0;
//            int idx_runx = 0;
//            while(idx_runx < filterx.length){
//                filterx[idx_runx] = 0.0;
//                filtery[idx_runx] = 0.0;
//                idx_runx++;
//            }
        }else{
            if(xValue<0.1 && xValue> -0.1) xValue = 0.0;
            if(yValue<0.1 && yValue> -0.1) yValue = 0.0;

//
//            if(x_idx >= filterx.length) x_idx = 0;
//            filterx[x_idx] = xValue;
//            x_idx++;
//
//            int idx_runx = 0;
//            double sum = 0;
//            while(idx_runx < filterx.length){
//                sum+=filterx[idx_runx];
//                idx_runx++;
//            }
//            xValue = (double) (sum / filterx.length);
//            //Log.d("xValue", Double.toString(Double.valueOf(d2.format(xValue))));
//
//            if(y_idx >= filtery.length) y_idx = 0;
//            filtery[y_idx] = yValue;
//            y_idx++;
//
//            int idx_runy = 0;
//            sum = 0;
//            while(idx_runy < filtery.length){
//                sum+=filtery[idx_runy];
//                idx_runy++;
//            }
//            yValue = (double) (sum / filtery.length);


            this.xValue = xValue;
            this.yValue = yValue;
            mText1.setText("x: " + d.format(xValue) + " y: " + d.format(yValue));


            if(old_yValue != Double.valueOf(d2.format(yValue)) || old_xValue != Double.valueOf(d2.format(xValue))) {
//            Log.d("New Data", Double.toString(old_yValue));
//            Log.d("New Data", Double.toString(Double.valueOf(d2.format(yValue))));
//            Log.d("New Data","-------------------");


                mJoystick_flag = true;
                old_yValue = Double.valueOf(d2.format(yValue));
                old_xValue = Double.valueOf(d2.format(xValue));
            }
        }
    }

    @Override
    public void setOnTouchListener(double xValue, double yValue) {
        newData(xValue, yValue, false);

    }

    @Override
    public void setOnMovedListener(double xValue, double yValue) {
        newData(xValue, yValue, false);

    }

    @Override
    public void setOnReleaseListener(double xValue, double yValue) {
       // Log.d("setOnReleaseListener", "call setOnReleaseListener");
        newData(xValue, yValue, true);

    }

    @Override
    public void onStart() {
        super.onStart();
        mJoystick.invalidate();
    }

    @Override//this is interupt service routine, that will execute every 150ms
    public void onResume() {
        super.onResume();
        mJoystick.invalidate();
        BalanduinoActivity.joystickReleased = true;

        mRunnable = new Runnable() {//this is a interupt service that run every 150ms
            @Override
            public void run() {
                mHandler.postDelayed(this, 150); // Send data every 200ms
                if (BalanduinoActivity.mChatService == null)
                    return;
                if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED && BalanduinoActivity.checkTab(ViewPagerAdapter.JOYSTICK_FRAGMENT)) {
                    if (!getResources().getBoolean(R.bool.isTablet)){// || !BalanduinoActivity.buttonState) { // Don't send stop if the button in the IMU fragment is pressed
                        //this is for sending the value of joystick

                        if(mJoystick_flag) {
//                            Log.d("newData", "come here2");
                            mJoystick_flag = false;
                            if (joystickReleased || (xValue == 0 && yValue == 0)) {
                                BalanduinoActivity.mChatService.write(BalanduinoActivity.sendStop);
                                Log.d("newData", "sendStop");
                            }else {
                                Log.d("newData-map-xvaluexx", Double.toString(Double.valueOf(d2.format(xValue))));
                                Log.d("newData-map-yvalueyyyyy", Double.toString(Double.valueOf(d2.format(yValue))));
                               // String message = BalanduinoActivity.sendJoystickValues + d.format(xValue) + ',' + d.format(yValue) + ";";
                                String message = BalanduinoActivity.sendJoystickValues + Double.valueOf(d2.format(xValue)) + ',' + Double.valueOf(d2.format(yValue)) + ";";
                                BalanduinoActivity.mChatService.write(message);

                            }
                        }

                        //this is for sending the value of heigh bar if its value change or flag is set
                        if(mheighbar_flag){
                            mheighbar_flag = false;
                            String message = BalanduinoActivity.setHeight + ','  + d.format(mheighbar_value) + ";";

                            BalanduinoActivity.mChatService.write(message);
                        }

                    }
                }
            }
        };
        mHandler.postDelayed(mRunnable, 150); // Send data every 150ms
    }

    @Override
    public void onPause() {
        super.onPause();
        mJoystick.invalidate();
        BalanduinoActivity.joystickReleased = true;
        CustomViewPager.setPagingEnabled(true);
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mJoystick.invalidate();
        BalanduinoActivity.joystickReleased = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        mJoystick.invalidate();
        BalanduinoActivity.joystickReleased = true;
        CustomViewPager.setPagingEnabled(true);
    }
}