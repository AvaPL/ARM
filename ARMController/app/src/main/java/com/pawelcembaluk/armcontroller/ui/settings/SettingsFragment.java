package com.pawelcembaluk.armcontroller.ui.settings;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import com.pawelcembaluk.armcontroller.R;
import com.pawelcembaluk.armcontroller.interfaces.DrawerEnabler;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String KEY_DH_MODE = "dh_mode";
    public static final String KEY_CONTINUOUS_COMMANDS_DELAY = "continuous_commands_delay";
    public static final int DEFAULT_CONTINUOUS_COMMANDS_DELAY = 50;

    private SeekBarPreference continuousCommandsDelay;

    @Override
    public void onDestroyView() {
        continuousCommandsDelay.setValue(floorToMultipleOf10(continuousCommandsDelay.getValue()));
        setDrawerEnabled(true);
        super.onDestroyView();
    }

    private void setDrawerEnabled(boolean isEnabled) {
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity instanceof DrawerEnabler)
            ((DrawerEnabler) fragmentActivity).setDrawerEnabled(isEnabled);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        continuousCommandsDelay = findPreference(KEY_CONTINUOUS_COMMANDS_DELAY);
        initializeContinuousCommandsDelay();
        setDrawerEnabled(false);
    }

    private void initializeContinuousCommandsDelay() {
        String summary = getSummary(continuousCommandsDelay.getValue());
        continuousCommandsDelay.setSummary(summary);
        Preference.OnPreferenceChangeListener continuousCommandsDelayListener =
                getContinuousCommandsDelayListener();
        continuousCommandsDelay.setOnPreferenceChangeListener(continuousCommandsDelayListener);
    }

    private String getSummary(int value) {
        int flooredValue = floorToMultipleOf10(value);
        return flooredValue + " ms";
    }

    private int floorToMultipleOf10(int value) {
        value /= 10;
        value *= 10;
        return value;
    }

    private Preference.OnPreferenceChangeListener getContinuousCommandsDelayListener() {
        return (preference, newValue) -> {
            String summary = getSummary((int) newValue);
            continuousCommandsDelay.setSummary(summary);
            return true;
        };
    }
}