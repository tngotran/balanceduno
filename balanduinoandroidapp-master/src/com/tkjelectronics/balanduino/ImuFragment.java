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

import android.annotation.TargetApi;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import java.text.DecimalFormat;

public class ImuFragment extends SherlockFragment {
    private Button mButton;
    private Button mButton_machine_gun;
    private Button mButton_Yahoo, mButton_eat_cake, mButton_japan, mButton_Disagree, mButton_hate;
    public TextView mPitchView;
    public TextView mRollView;
    public TextView mCoefficient;
    private TableRow mTableRow;

    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private int counter = 0;
    private boolean buttonState;
    private boolean mstop_flag = false;
    private double lst_roll=0.0, lst_pitch=0.0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.imu, container, false);

        mPitchView = (TextView) v.findViewById(R.id.textView1);
        mRollView = (TextView) v.findViewById(R.id.textView2);
        mCoefficient = (TextView) v.findViewById(R.id.textView3);
        mTableRow = (TableRow) v.findViewById(R.id.tableRowCoefficient);

        mButton = (Button) v.findViewById(R.id.button);

        mButton_Yahoo = (Button) v.findViewById(R.id.button_yahoo);
        mButton_Yahoo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                send_message(BalanduinoActivity.sendYahoo);
            }
        });

        mButton_machine_gun = (Button) v.findViewById(R.id.button_machine_gun);
        mButton_machine_gun.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                send_message(BalanduinoActivity.sendMachineGun);
            }
        });

        mButton_eat_cake = (Button) v.findViewById(R.id.button_eat_cake);
        mButton_eat_cake.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                send_message(BalanduinoActivity.sendEatCake);
            }
        });

        mButton_japan = (Button) v.findViewById(R.id.button_japanshow);
        mButton_japan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                send_message(BalanduinoActivity.sendJapan);
            }
        });

        mButton_Disagree = (Button) v.findViewById(R.id.button_disagree);
        mButton_Disagree.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                send_message(BalanduinoActivity.sendDisagree);
            }
        });

        mButton_hate = (Button) v.findViewById(R.id.button_hate);
        mButton_hate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                send_message(BalanduinoActivity.sendHate);
            }
        });

        mHandler.postDelayed(new Runnable() { // Hide the menu icon and tablerow if there is no build in gyroscope in the device
            @Override
            public void run() {
                if (SensorFusion.IMUOutputSelection == -1)
                    mHandler.postDelayed(this, 100); // Run this again if it hasn't initialized the sensors yet
                else if (SensorFusion.IMUOutputSelection != 2) // Check if a gyro is supported
                    mTableRow.setVisibility(View.GONE); // If not then hide the tablerow
            }
        }, 100); // Wait 100ms before running the code

        BalanduinoActivity.buttonState = false;

        return v;
    }

    double roundTwoDecimals(double d)
    {
        DecimalFormat twoDForm = new DecimalFormat("#.#");
        return Double.valueOf(twoDForm.format(d));
    }
    //send message to slaver
    void send_message(String out){
        if (BalanduinoActivity.mChatService == null)
            return;
        if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED && BalanduinoActivity.currentTabSelected == ViewPagerAdapter.IMU_FRAGMENT) {
            BalanduinoActivity.mChatService.write(out);
        }

    }
    @Override
    public void onResume() {
        super.onResume();

        mRunnable = new Runnable() {
            @Override
            public void run() {
                mHandler.postDelayed(this, 50); // Update IMU data every 50ms
                if (BalanduinoActivity.mSensorFusion == null)
                    return;
                mPitchView.setText(BalanduinoActivity.mSensorFusion.pitch);
                mRollView.setText(BalanduinoActivity.mSensorFusion.roll);
                mCoefficient.setText(BalanduinoActivity.mSensorFusion.coefficient);

                counter++;
                if (counter > 2) { // Only send data every 150ms time
                    counter = 0;
                    if (BalanduinoActivity.mChatService == null)
                        return;
                    if (BalanduinoActivity.mChatService.getState() == BluetoothChatService.STATE_CONNECTED && BalanduinoActivity.currentTabSelected == ViewPagerAdapter.IMU_FRAGMENT) {
                        buttonState = mButton.isPressed();
                        BalanduinoActivity.buttonState = buttonState;

                        if (BalanduinoActivity.joystickReleased || getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) // Check if joystick is released or we are not in landscape mode
                            CustomViewPager.setPagingEnabled(!buttonState); // Set the ViewPager according to the button
                        else
                            CustomViewPager.setPagingEnabled(false);

                        if (BalanduinoActivity.joystickReleased) {
                            if (buttonState) {
                                lockRotation();

                                try {
                                    double pitch_tr = Double.parseDouble(BalanduinoActivity.mSensorFusion.pitch); // Make use of autoboxing.  It's also easier to read.
                                    double pitch_tr_str = roundTwoDecimals(pitch_tr);


                                    double roll_tr = Double.parseDouble(BalanduinoActivity.mSensorFusion.roll); // Make use of autoboxing.  It's also easier to read.
                                    double roll_tr_str = roundTwoDecimals(roll_tr);

                                    if(lst_pitch != pitch_tr_str || lst_roll != roll_tr_str) {
                                        BalanduinoActivity.mChatService.write(BalanduinoActivity.sendIMUValues + Double.toString(pitch_tr_str) + ',' + Double.toString(roll_tr_str) + ";");
                                        lst_pitch = pitch_tr_str;
                                        lst_roll = roll_tr_str;
                                    }
                                } catch (NumberFormatException e) {

                                }
                                mButton.setText(R.string.sendingData);
                                mstop_flag = false;
                            } else if(mstop_flag == false) {
                                unlockRotation();
                                BalanduinoActivity.mChatService.write(BalanduinoActivity.sendStop);
                                mButton.setText(R.string.notSendingData);
                                mstop_flag = true;
                            }
                        }
                    } else {
                        mButton.setText(R.string.button);
                        if (BalanduinoActivity.currentTabSelected == ViewPagerAdapter.IMU_FRAGMENT && BalanduinoActivity.joystickReleased)
                            CustomViewPager.setPagingEnabled(true);
                    }
                }
            }
        };
        mHandler.postDelayed(mRunnable, 50); // Update IMU data every 50ms
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void lockRotation() {
        if (getResources().getBoolean(R.bool.isTablet)) { // Check if the layout can rotate
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED); // Lock screen orientation so it doesn't rotate
            else { // Lock rotation manually - source: http://blogs.captechconsulting.com/blog/eric-miles/programmatically-locking-android-screen-orientation
                int rotation = BalanduinoActivity.getRotation();
                int lock;

                if (rotation == Surface.ROTATION_90) // Landscape
                    lock = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                else if (rotation == Surface.ROTATION_180) // Reverse Portrait
                    lock = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                else if (rotation == Surface.ROTATION_270) // Reverse Landscape
                    lock = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                else
                    lock = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                getActivity().setRequestedOrientation(lock);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void unlockRotation() {
        if (getResources().getBoolean(R.bool.isTablet)) { // Check if the layout can rotate
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER); // Unlock screen orientation
            else
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR); // Unlock screen orientation
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunnable);
        unlockRotation();
    }
}