package com.pawelcembaluk.armcontroller.ui.devices;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.ListFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pawelcembaluk.armcontroller.bluetooth.BluetoothConnection;
import com.pawelcembaluk.armcontroller.bluetooth.Devices;
import com.pawelcembaluk.armcontroller.R;

import java.util.ArrayList;

public class DevicesFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private BluetoothDeviceArrayAdapter devicesArrayAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() == null) return;
        devicesArrayAdapter = new BluetoothDeviceArrayAdapter(getActivity(), bluetoothDevices);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup parent =
                (ViewGroup) inflater.inflate(R.layout.fragment_devices, container, false);
        parent.addView(view, 0);
        setRefreshListener(parent);
        return parent;
    }

    private void setRefreshListener(ViewGroup parent) {
        if (parent instanceof SwipeRefreshLayout)
            ((SwipeRefreshLayout) parent).setOnRefreshListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        refresh();
    }

    private void refresh() {
        setNoDevicesText();
        refreshDevices();
    }

    private void setNoDevicesText() {
        int stringResourceId = BluetoothConnection.isEnabled() ? R.string.text_no_devices_found :
                               R.string.text_bluetooth_disabled;
        setEmptyText(getString(stringResourceId));
    }


    private void refreshDevices() {
        bluetoothDevices.clear();
        BluetoothConnection.getBondedDevices().stream().filter(Devices::isNotLE)
                           .sorted(Devices::compare).forEach(bluetoothDevices::add);
        devicesArrayAdapter.notifyDataSetChanged();
    }


    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        BluetoothDevice device = bluetoothDevices.get(position-1);
        BluetoothConnection.deviceAddress = device.getAddress();
        Log.d(getClass().getSimpleName(), device.getAddress());
    }

    @Override
    public void onRefresh() {
        refresh();
        if (getView() instanceof SwipeRefreshLayout)
            ((SwipeRefreshLayout) getView()).setRefreshing(false);
    }
}