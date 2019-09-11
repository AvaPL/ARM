package com.pawelcembaluk.armcontroller.ui.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.pawelcembaluk.armcontroller.R;

public class ControllerFragment extends Fragment {

    private ControllerViewModel controllerViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        controllerViewModel =
                ViewModelProviders.of(this).get(ControllerViewModel.class);
        View root = inflater.inflate(R.layout.fragment_controller, container, false);
        final TextView textView = root.findViewById(R.id.text_controller);
        controllerViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}