package com.pawelcembaluk.armcontroller.ui.devices;

import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.pawelcembaluk.armcontroller.R;
import com.pawelcembaluk.armcontroller.bluetooth.BluetoothConnection;

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
        initializeItem(convertView, device);
        return convertView;
    }

    private View inflateDeviceItem(@NonNull ViewGroup parent) {
        return fragmentActivity.getLayoutInflater().inflate(R.layout.device_item, parent, false);
    }

    private void initializeItem(@Nullable View convertView, BluetoothDevice device) {
        if (convertView == null) return;
        TextView deviceName = convertView.findViewById(R.id.text_device_name);
        TextView deviceAddress = convertView.findViewById(R.id.text_device_address);
        ImageView status = convertView.findViewById(R.id.status_bar);
        deviceName.setText(device.getName());
        deviceAddress.setText(device.getAddress());
        initializeStatusColor(status, device.getAddress());
    }

    private void initializeStatusColor(ImageView status, String deviceAddress) {
        int color = isSelectedDevice(deviceAddress) ? R.color.colorStatusActive :
                    R.color.colorStatusInactive;
        status.setBackgroundColor(ContextCompat.getColor(getContext(), color));
    }

    private boolean isSelectedDevice(String deviceAddress) {
        return deviceAddress.equals(BluetoothConnection.getInstance().getDeviceAddress());
    }
}
