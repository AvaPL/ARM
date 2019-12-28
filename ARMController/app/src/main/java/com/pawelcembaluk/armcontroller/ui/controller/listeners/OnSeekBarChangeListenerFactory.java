package com.pawelcembaluk.armcontroller.ui.controller.listeners;

import android.widget.SeekBar;
import android.widget.TextView;

import com.pawelcembaluk.armcontroller.bluetooth.BluetoothConnection;

import java.util.List;

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

    private static class ServoSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        private int servoIndex;

        public ServoSeekBarChangeListener(int servoIndex) {
            this.servoIndex = servoIndex;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            BluetoothConnection.getInstance().send("angle " + servoIndex + " " + i);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    private static class ComplexSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        private SeekBar.OnSeekBarChangeListener[] listeners;

        public ComplexSeekBarChangeListener(SeekBar.OnSeekBarChangeListener[] listeners) {
            this.listeners = listeners;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            for (SeekBar.OnSeekBarChangeListener listener : listeners)
                listener.onProgressChanged(seekBar, i, b);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            for (SeekBar.OnSeekBarChangeListener listener : listeners)
                listener.onStartTrackingTouch(seekBar);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            for (SeekBar.OnSeekBarChangeListener listener : listeners)
                listener.onStopTrackingTouch(seekBar);
        }
    }

    public static SeekBar.OnSeekBarChangeListener getTextViewListener(TextView textViewToUpdate) {
        return new TextViewSeekBarChangeListener(textViewToUpdate);
    }

    public static SeekBar.OnSeekBarChangeListener getServoListener(int servoIndex) {
        return new ServoSeekBarChangeListener(servoIndex);
    }

    public static SeekBar.OnSeekBarChangeListener getComplexListener(
            SeekBar.OnSeekBarChangeListener... listeners) {
        return new ComplexSeekBarChangeListener(listeners);
    }
}