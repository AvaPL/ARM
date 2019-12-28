package com.pawelcembaluk.armcontroller.ui.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.pawelcembaluk.armcontroller.R;
import com.pawelcembaluk.armcontroller.bluetooth.BluetoothConnection;
import com.pawelcembaluk.armcontroller.interfaces.ConnectionObserver;
import com.pawelcembaluk.armcontroller.interfaces.DataReceivedObserver;
import com.pawelcembaluk.armcontroller.ui.controller.enums.Mode;
import com.pawelcembaluk.armcontroller.ui.controller.listeners.OnSeekBarChangeListenerFactory;
import com.pawelcembaluk.armcontroller.ui.controller.listeners.OnTouchListenerFactory;
import com.pawelcembaluk.armcontroller.ui.settings.SettingsFragment;

public class ControllerFragment extends Fragment implements ConnectionObserver, DataReceivedObserver {

    private static final String SHARED_PREFERENCES_ANGLES = "angles";
    private static final String KEY_JOINT_VALUE = "joint_value";
    private static final String KEY_GRAB_VALUE = "grab_value";
    private static final String SHARED_PREFERENCES_KINEMATICS = "kinematics";
    private static final String KEY_POSITION_X = "position_x";
    private static final String KEY_POSITION_Y = "position_y";
    private static final String KEY_ANGLE_PHI = "angle_phi";

    private Mode mode;
    private int currentLayout;

    private SharedPreferences angles;
    private SeekBar[] jointSeekBars;
    private SeekBar grabSeekBar;
    private TextView[] jointTextViews;

    private SharedPreferences kinematics;
    private TextView xPositionTextView;
    private TextView yPositionTextView;
    private TextView phiAngleTextView;
    private ImageButton xPositionUp;
    private ImageButton xPositionDown;
    private ImageButton yPositionUp;
    private ImageButton yPositionDown;
    private ImageButton phiAngleUp;
    private ImageButton phiAngleDown;


    private ImageButton up;
    private ImageButton down;
    private ImageButton left;
    private ImageButton right;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadMode();
        initializeBluetoothConnection();
    }

    private void loadMode() {
        if (getContext() == null)
            mode = Mode.ANGLES;
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        mode = sharedPreferences.getBoolean(SettingsFragment.KEY_DH_MODE, false) ? Mode.DH :
               Mode.ANGLES;
    }

    private void initializeBluetoothConnection() {
        BluetoothConnection.getInstance().addConnectionObserver(this);
        BluetoothConnection.getInstance().addDataReceivedObserver(this);
        BluetoothConnection.getInstance().flushBufferedData();
        if (BluetoothConnection.getInstance().isConnected())
            queryCurrentArmState();
    }

    private void queryCurrentArmState() {
        if (mode == Mode.ANGLES)
            BluetoothConnection.getInstance().send("angles");
        else
            BluetoothConnection.getInstance().send("kinematics");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        currentLayout = mode == Mode.ANGLES ? R.layout.fragment_controller :
                        R.layout.fragment_controller_dh;
        return inflater.inflate(currentLayout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (mode == Mode.ANGLES)
            initializeAnglesView();
        else
            initializeDhView();
        initializeMovementButtons();
    }

    private void initializeAnglesView() {
        initializeSeekBars();
        initializeJointTextViews();
        initializeAnglesSharedPreferences();
        loadAnglesState();
        setJointsTexts();
        setAnglesListeners();
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
        angles = getContext().getSharedPreferences(SHARED_PREFERENCES_ANGLES, Context.MODE_PRIVATE);
    }

    private void loadAnglesState() {
        if (angles == null) return;
        for (int i = 0; i < jointSeekBars.length; ++i) {
            int jointValue = angles.getInt(KEY_JOINT_VALUE + "_" + i, 0);
            jointSeekBars[i].setProgress(jointValue);
        }
        int grabValue = angles.getInt(KEY_GRAB_VALUE, 0);
        grabSeekBar.setProgress(grabValue);
    }

    private void setJointsTexts() {
        for (int i = 0; i < jointSeekBars.length && i < jointTextViews.length; ++i)
            jointTextViews[i].setText(String.valueOf(jointSeekBars[i].getProgress()));
    }

    private void setAnglesListeners() {
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

    private void initializeDhView() {
        initializeKinematicsTextViews();
        initializeKinematicsButtons();
        initializeKinematicsSharedPreferences();
        loadKinematicsState();
    }

    private void initializeKinematicsTextViews() {
        if (getView() == null) return;
        xPositionTextView = getView().findViewById(R.id.text_position_x_value);
        yPositionTextView = getView().findViewById(R.id.text_position_y_value);
        phiAngleTextView = getView().findViewById(R.id.text_angle_phi_value);
    }

    private void initializeKinematicsButtons() {
        if (getView() == null) return;
        xPositionUp = getView().findViewById(R.id.button_dh_up_x);
        xPositionDown = getView().findViewById(R.id.button_dh_down_x);
        yPositionUp = getView().findViewById(R.id.button_dh_up_y);
        yPositionDown = getView().findViewById(R.id.button_dh_down_y);
        phiAngleUp = getView().findViewById(R.id.button_dh_up_phi);
        phiAngleDown = getView().findViewById(R.id.button_dh_down_phi);
        setKinematicsOnTouchListeners();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setKinematicsOnTouchListeners() {
        int delay = getDelay();
        xPositionUp.setOnTouchListener(OnTouchListenerFactory.getIncrementCoordinateListener("x",
                                                                                             xPositionTextView,
                                                                                             delay));
        xPositionDown.setOnTouchListener(OnTouchListenerFactory.getDecrementCoordinateListener("x",
                                                                                               xPositionTextView,
                                                                                               delay));
        yPositionUp.setOnTouchListener(OnTouchListenerFactory.getIncrementCoordinateListener("y",
                                                                                             yPositionTextView,
                                                                                             delay));
        yPositionDown.setOnTouchListener(OnTouchListenerFactory.getDecrementCoordinateListener("y",
                                                                                               yPositionTextView,
                                                                                               delay));
        phiAngleUp.setOnTouchListener(OnTouchListenerFactory.getIncrementCoordinateListener("phi",
                                                                                            phiAngleTextView,
                                                                                            delay));
        phiAngleDown.setOnTouchListener(OnTouchListenerFactory.getDecrementCoordinateListener("phi",
                                                                                              phiAngleTextView,
                                                                                              delay));
    }

    private void initializeKinematicsSharedPreferences() {
        if (getContext() == null) return;
        kinematics = getContext()
                .getSharedPreferences(SHARED_PREFERENCES_KINEMATICS, Context.MODE_PRIVATE);
    }

    private void loadKinematicsState() {
        if (kinematics == null) return;
        xPositionTextView.setText(
                kinematics.getString(KEY_POSITION_X, getString(R.string.text_default_int_value)));
        yPositionTextView.setText(
                kinematics.getString(KEY_POSITION_Y, getString(R.string.text_default_int_value)));
        phiAngleTextView.setText(
                kinematics.getString(KEY_ANGLE_PHI, getString(R.string.text_default_int_value)));
    }

    private void initializeMovementButtons() {
        if (getView() == null) return;
        up = getView().findViewById(R.id.button_up);
        down = getView().findViewById(R.id.button_down);
        left = getView().findViewById(R.id.button_left);
        right = getView().findViewById(R.id.button_right);
        setOnTouchListeners();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnTouchListeners() {
        int delay = getDelay();
        up.setOnTouchListener(OnTouchListenerFactory.getRepeatCommandListener("forward", delay));
        down.setOnTouchListener(OnTouchListenerFactory.getRepeatCommandListener("back", delay));
        left.setOnTouchListener(OnTouchListenerFactory.getRepeatCommandListener("left", delay));
        right.setOnTouchListener(OnTouchListenerFactory.getRepeatCommandListener("right", delay));
    }

    private int getDelay() {
        if (getContext() == null) return SettingsFragment.DEFAULT_CONTINUOUS_COMMANDS_DELAY;
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getInt(SettingsFragment.KEY_CONTINUOUS_COMMANDS_DELAY,
                                        SettingsFragment.DEFAULT_CONTINUOUS_COMMANDS_DELAY);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMode();
        if (!isLayoutCorrect())
            refreshLayout();
    }

    private boolean isLayoutCorrect() {
        boolean isAnglesLayout =
                mode == Mode.ANGLES && currentLayout == R.layout.fragment_controller;
        boolean isDhLayout = mode == Mode.DH && currentLayout == R.layout.fragment_controller_dh;
        return isAnglesLayout || isDhLayout;
    }

    private void refreshLayout() {
        if (getFragmentManager() == null) return;
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    @Override
    public void onPause() {
        if (mode == Mode.ANGLES)
            anglesOnPause();
        else
            kinematicsOnPause();
        super.onPause();
    }

    private void anglesOnPause() {
        if (angles == null) return;
        SharedPreferences.Editor anglesEditor = angles.edit();
        for (int i = 0; i < jointSeekBars.length; ++i)
            anglesEditor.putInt(KEY_JOINT_VALUE + "_" + i, jointSeekBars[i].getProgress());
        anglesEditor.putInt(KEY_GRAB_VALUE, grabSeekBar.getProgress());
        anglesEditor.apply();
    }

    private void kinematicsOnPause() {
        if (kinematics == null) return;
        SharedPreferences.Editor kinematicsEditor = kinematics.edit();
        kinematicsEditor.putString(KEY_POSITION_X, xPositionTextView.getText().toString());
        kinematicsEditor.putString(KEY_POSITION_Y, yPositionTextView.getText().toString());
        kinematicsEditor.putString(KEY_ANGLE_PHI, phiAngleTextView.getText().toString());
        kinematicsEditor.apply();
    }

    @Override
    public void onDestroy() {
        BluetoothConnection.getInstance().removeConnectionObserver(this);
        BluetoothConnection.getInstance().removeDataReceivedObserver(this);
        super.onDestroy();
    }

    @Override
    public void onConnect() {
        queryCurrentArmState();
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
        if (commandSplit[0].equals("angle") && mode == Mode.ANGLES)
            setAngle(commandSplit);
        else if (commandSplit[0].equals("coordinate") && mode == Mode.DH)
            setCoordinate(commandSplit);
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

    private void setCoordinate(String[] commandSplit) {
        int value = Integer.parseInt(commandSplit[2]);
        if (commandSplit[1].equals("x"))
            xPositionTextView.setText(value);
        else if (commandSplit[1].equals("y"))
            yPositionTextView.setText(value);
        else
            phiAngleTextView.setText(value % 360);
    }
}