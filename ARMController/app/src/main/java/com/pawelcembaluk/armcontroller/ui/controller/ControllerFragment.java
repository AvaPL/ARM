package com.pawelcembaluk.armcontroller.ui.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.pawelcembaluk.armcontroller.interfaces.ConnectionObserver;
import com.pawelcembaluk.armcontroller.interfaces.DataReceivedObserver;

public class ControllerFragment extends Fragment implements ConnectionObserver, DataReceivedObserver {

    private static final String SHARED_PREFERENCES_ANGLES = "angles";
    private static final String KEY_JOINT_VALUE = "joint_value";
    private static final String KEY_GRAB_VALUE = "grab_value";

    private SharedPreferences angles;
    private SeekBar[] jointSeekBars;
    private SeekBar grabSeekBar;
    private TextView[] jointTextViews;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothConnection.getInstance().addConnectionObserver(this);
        BluetoothConnection.getInstance().addDataReceivedObserver(this);
        BluetoothConnection.getInstance().flushBufferedData();
        if (BluetoothConnection.getInstance().isConnected())
            queryCurrentAnglesState();
    }

    private void queryCurrentAnglesState() {
        BluetoothConnection.getInstance().send("angles"); //Gets current arms state.
    }

    @Override
    public void onDestroy() {
        BluetoothConnection.getInstance().removeConnectionObserver(this);
        BluetoothConnection.getInstance().removeDataReceivedObserver(this);
        super.onDestroy();
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
        setJointListeners();
        setGrabListener();
    }

    private void setJointListeners() {
        for (int i = 0; i < jointSeekBars.length && i < jointTextViews.length; ++i) {
            SeekBar.OnSeekBarChangeListener listener = getTextServoListener(i);
            jointSeekBars[i].setOnSeekBarChangeListener(listener);
        }
    }

    private SeekBar.OnSeekBarChangeListener getTextServoListener(int index) {
        SeekBar.OnSeekBarChangeListener textListener =
                OnSeekBarChangeListenerFactory.getTextViewListener(jointTextViews[index]);
        SeekBar.OnSeekBarChangeListener servoListener =
                OnSeekBarChangeListenerFactory.getServoListener(index);
        return OnSeekBarChangeListenerFactory.getComplexListener(textListener, servoListener);
    }

    private void setGrabListener() {
        int grabServoIndex = jointSeekBars.length;
        SeekBar.OnSeekBarChangeListener listener = OnSeekBarChangeListenerFactory
                .getServoListener(grabServoIndex);
        grabSeekBar.setOnSeekBarChangeListener(listener);
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

    @Override
    public void onConnect() {
        queryCurrentAnglesState();
    }

    @Override
    public void onConnectionFailed() {
    }

    @Override
    public void onDisconnect() {
    }

    @Override
    public void onDataReceived(String command) {
        String[] commandSplit = command.split(" ");
        if (commandSplit[0].equals("angle"))
            setAngle(commandSplit);
        else
            Toast.makeText(getContext(), command.trim(), Toast.LENGTH_SHORT).show();
    }

    private void setAngle(String[] commandSplit) {
        int angle = Integer.parseInt(commandSplit[2]);
        if (!isAngleCorrect(angle)) return;
        if (commandSplit[1].equals("grab")) {
            grabSeekBar.setProgress(angle);
            return;
        }
        int index = Integer.parseInt(commandSplit[1]);
        jointSeekBars[index].setProgress(angle);
    }

    private boolean isAngleCorrect(int angle) {
        return 0 <= angle && angle <= 180;
    }
}