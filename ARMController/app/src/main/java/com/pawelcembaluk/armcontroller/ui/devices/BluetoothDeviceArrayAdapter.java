package com.pawelcembaluk.armcontroller.ui.devices;

import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.pawelcembaluk.armcontroller.R;

import java.util.List;

public class BluetoothDeviceArrayAdapter extends ArrayAdapter<BluetoothDevice> {

    private FragmentActivity fragmentActivity;
    private List<BluetoothDevice> bluetoothDevices;

    public BluetoothDeviceArrayAdapter(@NonNull FragmentActivity fragmentActivity,
                                       @NonNull List<BluetoothDevice> bluetoothDevices) {
        super(fragmentActivity, 0, bluetoothDevices);
        this.fragmentActivity = fragmentActivity;
        this.bluetoothDevices = bluetoothDevices;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        BluetoothDevice device = bluetoothDevices.get(position);
        if (convertView == null)
            convertView = inflateDeviceItem(parent);
        initializeItemTextViews(convertView, device);
        return convertView;
    }

    private View inflateDeviceItem(@NonNull ViewGroup parent) {
        View convertView;
        convertView = fragmentActivity.getLayoutInflater()
                                      .inflate(R.layout.device_item, parent, false);
        return convertView;
    }

    private void initializeItemTextViews(@Nullable View convertView,
                                         BluetoothDevice device) {
        TextView deviceName = convertView.findViewById(R.id.text_device_name);
        TextView deviceAddress = convertView.findViewById(R.id.text_device_address);
        deviceName.setText(device.getName());
        deviceAddress.setText(device.getAddress());
    }
}
