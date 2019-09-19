package com.pawelcembaluk.armcontroller.ui.controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pawelcembaluk.armcontroller.R;
import com.pawelcembaluk.armcontroller.bluetooth.BluetoothConnection;
import com.pawelcembaluk.armcontroller.bluetooth.SerialListener;
import com.pawelcembaluk.armcontroller.bluetooth.SerialService;
import com.pawelcembaluk.armcontroller.bluetooth.SerialSocket;

/**
 * MIT License
 * <p>
 * Copyright (c) 2019 Paweł Cembaluk, Kai Morich
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

//TODO: Rearrange methods order.
public class ControllerFragment extends Fragment implements ServiceConnection, SerialListener {

    private enum Connected {False, Pending, True}

    private static final String SHARED_PREFERENCES_ANGLES = "angles";
    private static final String KEY_JOINT_VALUE = "joint_value";
    private static final String KEY_GRAB_VALUE = "grab_value";

    private SharedPreferences angles;
    private SeekBar[] jointSeekBars;
    private SeekBar grabSeekBar;
    private TextView[] jointTextViews;

    private String newline = "\r\n"; //TODO: Remove or replace with constant.
    private SerialSocket socket;
    private SerialService service;
    private boolean initialStart = true;
    private Connected connected = Connected.False;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroy() {
        Log.d(getClass().getSimpleName(), "onDestroy");
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
        socket.disconnect();
        socket = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(getClass().getSimpleName(), "onStart");
        if (service != null){
            service.attach(this);
            Log.d(getClass().getSimpleName(), "Attaching this");
        }
        else {
            getActivity().startService(new Intent(getActivity(),
                                                  SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
            Log.d(getClass().getSimpleName(), "Starting service");
        }
    }

    @Override
    public void onStop() {
        Log.d(getClass().getSimpleName(), "onStop");
        if (service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation")
    // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this,
                                  Context.BIND_AUTO_CREATE);
        Log.d(getClass().getSimpleName(), "Binding service");
    }

    @Override
    public void onDetach() {
        Log.d(getClass().getSimpleName(), "onDetach");
        try {
            getActivity().unbindService(this);
        } catch (Exception ignored) {
        }
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getClass().getSimpleName(), "onResume");
        if (initialStart && service != null) {
            Log.d(getClass().getSimpleName(), "onResume success");
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    private void connect() {
        Log.d(getClass().getSimpleName(), "connect");
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device =
                    bluetoothAdapter.getRemoteDevice(BluetoothConnection.deviceAddress);
            String deviceName = device.getName() != null ? device.getName() : device.getAddress();
            status("connecting...");
            connected = Connected.Pending;
            socket = new SerialSocket();
            service.connect(this, "Connected to " + deviceName);
            socket.connect(getContext(), service, device);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void status(String str) { //TODO: Replace with making toasts.
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorText)), 0,
                    spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        makePlaceholderToast(spn.toString());
        //        receiveText.append(spn);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        Log.d(getClass().getSimpleName(), "onServiceConnected");
        if (initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_controller, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initializeSeekBars();
        initializeJointTextViews();
        initializeAnglesSharedPreferences();
        loadState();
        setTexts();
        setListeners();
    }

    private void initializeSeekBars() {
        if (getView() == null) return;
        jointSeekBars = new SeekBar[3];
        jointSeekBars[0] = getView().findViewById(R.id.seek_bar_joint_0);
        jointSeekBars[1] = getView().findViewById(R.id.seek_bar_joint_1);
        jointSeekBars[2] = getView().findViewById(R.id.seek_bar_joint_2);
        grabSeekBar = getView().findViewById(R.id.seek_bar_grab);
    }

    private void initializeJointTextViews() {
        if (getView() == null) return;
        jointTextViews = new TextView[3];
        jointTextViews[0] = getView().findViewById(R.id.text_joint_0);
        jointTextViews[1] = getView().findViewById(R.id.text_joint_1);
        jointTextViews[2] = getView().findViewById(R.id.text_joint_2);
    }

    private void initializeAnglesSharedPreferences() {
        if (getContext() == null) return;
        this.angles =
                getContext().getSharedPreferences(SHARED_PREFERENCES_ANGLES, Context.MODE_PRIVATE);
    }

    private void loadState() {
        if (angles == null) return;
        for (int i = 0; i < jointSeekBars.length; ++i) {
            int jointValue = angles.getInt(KEY_JOINT_VALUE + "_" + i, 0);
            jointSeekBars[i].setProgress(jointValue);
        }
        int grabValue = angles.getInt(KEY_GRAB_VALUE, 0);
        grabSeekBar.setProgress(grabValue);
    }

    private void setTexts() {
        for (int i = 0; i < jointSeekBars.length && i < jointTextViews.length; ++i)
            jointTextViews[i].setText(String.valueOf(jointSeekBars[i].getProgress()));
    }

    private void setListeners() {
        for (int i = 0; i < jointSeekBars.length && i < jointTextViews.length; ++i) {
            SeekBar.OnSeekBarChangeListener listener =
                    OnSeekBarChangeListenerFactory.getTextViewListener(jointTextViews[i]);
            jointSeekBars[i].setOnSeekBarChangeListener(listener);
        }
    }

    @Override
    public void onPause() {
        if (angles == null) return;
        SharedPreferences.Editor anglesEditor = angles.edit();
        for (int i = 0; i < jointSeekBars.length; ++i)
            anglesEditor.putInt(KEY_JOINT_VALUE + "_" + i, jointSeekBars[i].getProgress());
        anglesEditor.putInt(KEY_GRAB_VALUE, grabSeekBar.getProgress());
        anglesEditor.apply();
        super.onPause();
    }

    private void send(String str) {
        if (connected != Connected.True) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorText)), 0,
                        spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            makePlaceholderToast(spn.toString());
            //            receiveText.append(spn);
            byte[] data = (str + newline).getBytes();
            socket.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    @Override
    public void onSerialConnect() {
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    private void receive(byte[] data) {
        makePlaceholderToast(new String(data));
//        receiveText.append(new String(data));
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        disconnect();
    }

    private void makePlaceholderToast(String message) { //TODO: Remove.
        Log.d(getClass().getSimpleName(), "Displaying toast");
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}