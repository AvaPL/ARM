package com.pawelcembaluk.armcontroller.ui.controller;

import android.widget.SeekBar;
import android.widget.TextView;

public class OnSeekBarChangeListenerFactory {

    private static class TextViewSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        private TextView textViewToUpdate;

        public TextViewSeekBarChangeListener(TextView textViewToUpdate) {
            this.textViewToUpdate = textViewToUpdate;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            textViewToUpdate.setText(Integer.toString(i));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    public static SeekBar.OnSeekBarChangeListener getTextViewListener(TextView textViewToUpdate) {
        return new TextViewSeekBarChangeListener(textViewToUpdate);
    }
}