package com.pawelcembaluk.armcontroller.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.pawelcembaluk.armcontroller.MainActivity;
import com.pawelcembaluk.armcontroller.R;
import com.pawelcembaluk.armcontroller.interfaces.DrawerEnabler;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setDrawerEnabled(false);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        TextView textView = root.findViewById(R.id.text_settings);
        SettingsViewModel settingsViewModel =
                ViewModelProviders.of(this).get(SettingsViewModel.class);
        settingsViewModel.getText().observe(this, textView::setText);
        return root;
    }

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