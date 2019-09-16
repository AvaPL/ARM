package com.pawelcembaluk.armcontroller.bluetooth;

import android.bluetooth.BluetoothDevice;

import java.util.Objects;

public final class Devices {

    public static boolean isNotLE(BluetoothDevice bluetoothDevice) {
        return bluetoothDevice.getType() != BluetoothDevice.DEVICE_TYPE_LE;
    }

    public static int compare(BluetoothDevice device1, BluetoothDevice device2) {
        if (Objects.equals(device1.getName(), device2.getName()))
            return device1.getAddress().compareTo(device2.getAddress());
        return device1.getName().compareTo(device2.getName());
    }

    private Devices(){
    }
}
