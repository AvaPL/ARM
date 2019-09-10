package com.pawelcembaluk.armcontroller.ui.settings;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import com.pawelcembaluk.armcontroller.R;
import com.pawelcembaluk.armcontroller.interfaces.DrawerEnabler;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        setDrawerEnabled(false);
        initializeContinuousCommandsDelay();
    }

    private void initializeContinuousCommandsDelay() {
        String continuousCommandsDelayKey =
                getString(R.string.preference_key_continuous_commands_delay);
        SeekBarPreference continuousCommandsDelay = findPreference(continuousCommandsDelayKey);
        if (continuousCommandsDelay == null) return;
        continuousCommandsDelay
                .setSummary(floorToMultipleOf10(continuousCommandsDelay.getValue()) + " ms");
        continuousCommandsDelay.setOnPreferenceChangeListener(
                (preference, newValue) -> {
                    int flooredValue = floorToMultipleOf10((int) newValue);
                    continuousCommandsDelay.setValue(flooredValue);
                    continuousCommandsDelay.setSummary(flooredValue + " ms");
                    return true;
                });
    }

    private int floorToMultipleOf10(int value) {
        value /= 10;
        value *= 10;
        return value;
    }

//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        View root = inflater.inflate(R.layout.fragment_settings, container, false);
//        TextView textView = root.findViewById(R.id.text_settings);
//        SettingsViewModel settingsViewModel =
//                ViewModelProviders.of(this).get(SettingsViewModel.class);
//        settingsViewModel.getText().observe(this, textView::setText);
//        return root;
//    }

    private void setDrawerEnabled(boolean isEnabled) {
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity instanceof DrawerEnabler)
            ((DrawerEnabler) fragmentActivity).setDrawerEnabled(isEnabled);
    }

    @Override
    public void onDestroyView() {
        setDrawerEnabled(true);
        super.onDestroyView();
    }
}