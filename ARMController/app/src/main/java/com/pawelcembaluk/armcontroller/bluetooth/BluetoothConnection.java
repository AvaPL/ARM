package com.pawelcembaluk.armcontroller.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.Set;
import java.util.stream.Collectors;

public class BluetoothConnection {

    public static boolean isEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public static Set<BluetoothDevice> getBondedDevices() {
        return BluetoothAdapter.getDefaultAdapter().getBondedDevices().stream()
                               .filter(Devices::isNotLE).collect(Collectors.toSet());
    }
}
