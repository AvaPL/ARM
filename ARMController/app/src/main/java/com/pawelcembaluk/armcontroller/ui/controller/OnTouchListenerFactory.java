package com.pawelcembaluk.armcontroller.ui.controller;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.pawelcembaluk.armcontroller.bluetooth.BluetoothConnection;

import java.util.function.IntUnaryOperator;

public class OnTouchListenerFactory {

    private static class RepeatListener implements View.OnTouchListener, Runnable {

        private final Handler handler = new Handler();
        private final View.OnClickListener clickListener;
        private final int intervalMillis;
        private View touchedView;

        public RepeatListener(View.OnClickListener clickListener, int intervalMillis) {
            this.clickListener = clickListener;
            this.intervalMillis = intervalMillis;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return onPress(view);
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    return onRelease();
            }
            return false;
        }

        private boolean onPress(View view) {
            handler.removeCallbacks(this);
            handler.postDelayed(this, intervalMillis);
            touchedView = view;
            touchedView.setPressed(true);
            clickListener.onClick(view);
            return true;
        }

        private boolean onRelease() {
            stop();
            return true;
        }

        private void stop() {
            handler.removeCallbacks(this);
            touchedView.setPressed(false);
            touchedView = null;
        }

        @Override
        public void run() {
            if (touchedView.isEnabled())
                runAndScheduleNext();
            else
                stop();
        }

        private void runAndScheduleNext() {
            handler.postDelayed(this, intervalMillis);
            clickListener.onClick(touchedView);
        }
    }

    public static View.OnTouchListener getRepeatCommandListener(String command, int delayMillis) {
        View.OnClickListener onClick = view -> BluetoothConnection.getInstance().send(command);
        return new RepeatListener(onClick, delayMillis);
    }

    public static View.OnTouchListener getIncrementValueListener(TextView valueText,
                                                                 int delayMillis) {
        View.OnClickListener onClick = getModifyValueOnClickListener(valueText, i -> ++i);
        return new RepeatListener(onClick, delayMillis);
    }

    public static View.OnTouchListener getDecrementValueListener(TextView valueText,
                                                                 int delayMillis) {
        View.OnClickListener onClick = getModifyValueOnClickListener(valueText, i -> --i);
        return new RepeatListener(onClick, delayMillis);
    }

    private static View.OnClickListener getModifyValueOnClickListener(TextView valueText,
                                                                      IntUnaryOperator operation) {
        return view -> {
            int value = Integer.parseInt(valueText.getText().toString());
            String decrementedValueString = Integer.toString(operation.applyAsInt(value));
            valueText.setText(decrementedValueString);
        };
    }


}
