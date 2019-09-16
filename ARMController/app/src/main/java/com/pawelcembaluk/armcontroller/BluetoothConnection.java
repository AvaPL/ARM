package com.pawelcembaluk.armcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BluetoothConnection {

    public static boolean isEnabled(){
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public static Set<BluetoothDevice> getBondedDevices() {
        Predicate<BluetoothDevice> isNotLe = bluetoothDevice -> bluetoothDevice.getType() !=
                                                                BluetoothDevice.DEVICE_TYPE_LE;
        return BluetoothAdapter.getDefaultAdapter().getBondedDevices().stream().filter(isNotLe)
                               .collect(Collectors.toSet());
    }

    public static boolean isNotLE(BluetoothDevice bluetoothDevice){
        return bluetoothDevice.getType() != BluetoothDevice.DEVICE_TYPE_LE;
    }
}
