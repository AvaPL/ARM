package com.pawelcembaluk.armcontroller.ui.devices;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.ListFragment;

import com.pawelcembaluk.armcontroller.BluetoothConnection;
import com.pawelcembaluk.armcontroller.R;

import java.util.ArrayList;

public class DevicesFragment extends ListFragment {

    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private BluetoothDeviceArrayAdapter devicesArrayAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() == null) return;
        devicesArrayAdapter = new BluetoothDeviceArrayAdapter(getActivity(), bluetoothDevices);
    }

//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_devices, container, false);
//    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        setListAdapter(null); //TODO: Is this line needed?
        if (getActivity() == null) return;
        View header = inflateListHeader(getActivity());
        getListView().addHeaderView(header, null, false);
        setEmptyText(getString(R.string.text_initializing));
        setListAdapter(devicesArrayAdapter);
    }

    @SuppressLint("InflateParams")
    private View inflateListHeader(FragmentActivity fragmentActivity) {
        return fragmentActivity.getLayoutInflater()
                               .inflate(R.layout.list_header, null, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setNoDevicesText();
        refresh();
    }

    private void setNoDevicesText() {
        int stringResourceId = BluetoothConnection.isEnabled() ? R.string.text_no_devices_found :
                               R.string.text_bluetooth_disabled;
        setEmptyText(getString(stringResourceId));
    }

    private void refresh() {
        bluetoothDevices.clear();
        for (BluetoothDevice device : BluetoothConnection.getBondedDevices())
            if (device.getType() != BluetoothDevice.DEVICE_TYPE_LE)
                bluetoothDevices.add(device);
//        Collections.sort(bluetoothDevices, DevicesFragment::compareTo); TODO: Add sorting method.
        devicesArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        Toast.makeText(getContext(), "Item selected", Toast.LENGTH_SHORT).show();
    }
}