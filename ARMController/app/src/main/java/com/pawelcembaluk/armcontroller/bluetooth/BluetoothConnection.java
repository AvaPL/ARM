package com.pawelcembaluk.armcontroller.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.widget.Toast;

import com.pawelcembaluk.armcontroller.interfaces.ConnectionObserver;
import com.pawelcembaluk.armcontroller.interfaces.DataReceivedObserver;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BluetoothConnection implements SerialListener {

    private enum Connected {False, Pending, True}

    private static BluetoothConnection instance;

    private Connected connected = Connected.False;
    private SerialService service;
    private SerialSocket socket;
    private String deviceAddress;
    private Set<ConnectionObserver> connectionObservers = new HashSet<>();
    private Set<DataReceivedObserver> dataReceivedObservers = new HashSet<>();

    public static BluetoothConnection getInstance() {
        if (instance == null)
            instance = new BluetoothConnection();
        return instance;
    }

    public boolean isEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public Set<BluetoothDevice> getBondedDevices() {
        return BluetoothAdapter.getDefaultAdapter().getBondedDevices().stream()
                               .filter(Devices::isNotLE).collect(Collectors.toSet());
    }

    public boolean isConnected() {
        return connected == Connected.True;
    }

    public void addConnectionObserver(ConnectionObserver connectionObserver) {
        connectionObservers.add(connectionObserver);
    }

    public void removeConnectionObserver(ConnectionObserver connectionObserver) {
        connectionObservers.remove(connectionObserver);
    }

    public void connect(Context context) {
        if (deviceAddress == null) {
            Toast.makeText(context, "No device selected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (connected != Connected.False) return;
        Log.d(getClass().getSimpleName(), "connect");
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            String deviceName = device.getName() != null ? device.getName() : device.getAddress();
            connected = Connected.Pending;
            Toast.makeText(context, "Connecting...", Toast.LENGTH_SHORT).show();
            socket = new SerialSocket();
            service.connect(this, "Connected to " + deviceName);
            socket.connect(context, service, device);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    public void startService(Activity activity) {
        Log.d(getClass().getSimpleName(), "startService");
        if (service != null) {
            Log.d(getClass().getSimpleName(), "attaching service");
            service.attach(this);
        } else {
            Log.d(getClass().getSimpleName(), "starting service");
            activity.startService(new Intent(activity, SerialService.class));
        }
    }

    public void bindService(Activity activity, ServiceConnection serviceConnection) {
        Log.d(getClass().getSimpleName(), "bindService");
        activity.bindService(new Intent(activity, SerialService.class), serviceConnection,
                             Context.BIND_AUTO_CREATE);
    }

    public void unbindService(Activity activity, ServiceConnection serviceConnection) {
        Log.d(getClass().getSimpleName(), "unbindService");
        try {
            activity.unbindService(serviceConnection);
        } catch (Exception ignored) {
        }
    }

    public void detachService(Activity activity) {
        Log.d(getClass().getSimpleName(), "detachService");
        if (service != null && !activity.isChangingConfigurations())
            service.detach();
    }

    @Override
    public void onSerialConnect() {
        Log.d(getClass().getSimpleName(), "connected (yay!)");
        connected = Connected.True;
        notifyObservers(connectionObservers, ConnectionObserver::onConnect);
    }

    private <T> void notifyObservers(Set<? extends T> observers, Consumer<T> method) {
        for (T observer : observers)
            method.accept(observer);
    }

    @Override
    public void onSerialConnectError(Exception e) {
        Log.d(getClass().getSimpleName(), "connection error");
        cleanConnection();
        notifyObservers(connectionObservers, ConnectionObserver::onConnectionFailed);
    }

    private void cleanConnection() {
        connected = Connected.False;
        if (service != null) service.disconnect();
        if (socket != null) socket.disconnect();
        socket = null;
    }

    @Override
    public void onSerialRead(byte[] data) {
        Log.d(getClass().getSimpleName(), "Data received: " + new String(data));
    }

    @Override
    public void onSerialIoError(Exception e) {
        Log.d(getClass().getSimpleName(), "io error (disconnect)");
        cleanConnection();
        notifyObservers(connectionObservers, ConnectionObserver::onDisconnect);
    }

    public void disconnect() {
        Log.d(getClass().getSimpleName(), "disconnect");
        cleanConnection();
        notifyObservers(connectionObservers, ConnectionObserver::onDisconnect);
    }

    public SerialService getService() {
        return service;
    }

    public void setService(SerialService service) {
        this.service = service;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    private BluetoothConnection() {
    }
}
