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
import com.pawelcembaluk.armcontroller.interfaces.SerialListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BluetoothConnection implements SerialListener {

    private enum Connected {False, Pending, True}

    private static final String NEW_LINE = "\n";

    private static BluetoothConnection instance;

    private Connected connected = Connected.False;
    private SerialService service;
    private SerialSocket socket;
    private String deviceAddress;
    private Set<ConnectionObserver> connectionObservers = new HashSet<>();
    private Set<DataReceivedObserver> dataReceivedObservers = new HashSet<>();
    private ArrayList<byte[]> unreceivedData = new ArrayList<>();

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

    public void addConnectionObserver(ConnectionObserver observer) {
        connectionObservers.add(observer);
    }

    public void removeConnectionObserver(ConnectionObserver observer) {
        connectionObservers.remove(observer);
    }

    public void addDataReceivedObserver(DataReceivedObserver observer) {
        dataReceivedObservers.add(observer);
    }

    public void removeDataReceivedObserver(DataReceivedObserver observer) {
        dataReceivedObservers.remove(observer);
    }

    public void connect(Context context) {
        if (deviceAddress == null) {
            Toast.makeText(context, "No device selected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (connected != Connected.False) return;
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            connected = Connected.Pending;
            Toast.makeText(context, "Connecting...", Toast.LENGTH_SHORT).show();
            socket = new SerialSocket();
            service.connect(this);
            socket.connect(context, service, device);
        } catch (Exception e) {
            onConnectionFailed(e);
        }
    }

    public void startService(Activity activity) {
        if (service != null)
            service.attach(this);
        else
            activity.startService(new Intent(activity, SerialService.class));
    }

    public void bindService(Activity activity, ServiceConnection serviceConnection) {
        activity.bindService(new Intent(activity, SerialService.class), serviceConnection,
                             Context.BIND_AUTO_CREATE);
    }

    public void unbindService(Activity activity, ServiceConnection serviceConnection) {
        try {
            activity.unbindService(serviceConnection);
        } catch (Exception ignored) {
        }
    }

    public void detachService(Activity activity) {
        if (service != null && !activity.isChangingConfigurations())
            service.detach();
    }

    @Override
    public void onConnect() {
        connected = Connected.True;
        notifyObservers(connectionObservers, ConnectionObserver::onConnect);
    }

    private <T> void notifyObservers(Set<? extends T> observers, Consumer<T> method) {
        for (T observer : observers)
            method.accept(observer);
    }

    @Override
    public void onConnectionFailed(Exception e) {
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
    public void onDataReceived(byte[] data) {
        Log.d(getClass().getSimpleName(), "Data received: " + new String(data));
        if (dataReceivedObservers.isEmpty())
            unreceivedData.add(data);
        else
            notifyDataReceivedObservers(data);
    }

    private void notifyDataReceivedObservers(byte[] data) {
        for (DataReceivedObserver observer : dataReceivedObservers)
            observer.onDataReceived(data);
    }

    public void flushUnreceivedData() {
        if (dataReceivedObservers.isEmpty()) return;
        for (byte[] data : unreceivedData)
            notifyDataReceivedObservers(data);
        unreceivedData.clear();
    }

    @Override
    public void onDisconnect(Exception e) {
        cleanConnection();
        notifyObservers(connectionObservers, ConnectionObserver::onDisconnect);
    }

    public void disconnect() {
        cleanConnection();
        notifyObservers(connectionObservers, ConnectionObserver::onDisconnect);
    }

    public void send(String string) {
        if (connected != Connected.True) return;
        try {
            byte[] data = (string + NEW_LINE).getBytes();
            socket.write(data);
        } catch (Exception e) {
            onDisconnect(e);
        }
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
