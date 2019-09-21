package com.pawelcembaluk.armcontroller.ui.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pawelcembaluk.armcontroller.R;

public class ControllerFragment extends Fragment {

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
        setRetainInstance(true);
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
}