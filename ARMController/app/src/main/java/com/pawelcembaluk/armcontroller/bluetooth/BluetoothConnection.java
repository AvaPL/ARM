package com.pawelcembaluk.armcontroller.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.widget.Toast;

import com.pawelcembaluk.armcontroller.R;
import com.pawelcembaluk.armcontroller.interfaces.ConnectionObserver;
import com.pawelcembaluk.armcontroller.interfaces.DataReceivedObserver;
import com.pawelcembaluk.armcontroller.interfaces.SerialListener;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BluetoothConnection implements SerialListener {

    private enum Connected {False, Pending, True}

    private static final String NEW_LINE = "\n";

    private static BluetoothConnection instance;

    private final Set<ConnectionObserver> connectionObservers = new HashSet<>();
    private final Set<DataReceivedObserver> dataReceivedObservers = new HashSet<>();
    private final BufferedData bufferedData = new BufferedData();

    private Connected connected = Connected.False;
    private SerialService service;
    private SerialSocket socket;
    private String deviceAddress;

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
        if (connected != Connected.False) return;
        if (!isEnabled()) {
            Toast.makeText(context, R.string.text_bluetooth_disabled, Toast.LENGTH_SHORT).show();
            return;
        }
        if (deviceAddress == null) {
            Toast.makeText(context, R.string.text_no_device_selected, Toast.LENGTH_SHORT).show();
            return;
        }
        connectToDevice(context);
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

    private void connectToDevice(Context context) {
        try {
            safelyConnectToDevice(context);
        } catch (Exception e) {
            onConnectionFailed(e);
        }
    }

    private void safelyConnectToDevice(Context context) throws IOException {
        BluetoothDevice device = getBluetoothDevice();
        connected = Connected.Pending;
        Toast.makeText(context, R.string.text_connecting, Toast.LENGTH_SHORT).show();
        socket = new SerialSocket();
        service.connect(this);
        socket.connect(context, service, device);
    }

    private BluetoothDevice getBluetoothDevice() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.getRemoteDevice(deviceAddress);
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
        bufferedData.append(new String(data));
        if (!dataReceivedObservers.isEmpty())
            flushBufferedData();
    }

    public void flushBufferedData() {
        if (dataReceivedObservers.isEmpty()) return;
        String[] dataStrings = bufferedData.flush();
        for (String string : dataStrings)
            notifyDataReceivedObservers(string);
    }

    private void notifyDataReceivedObservers(String data) {
        for (DataReceivedObserver observer : dataReceivedObservers)
            observer.onDataReceived(data);
    }

    @Override
    public void onDisconnect(Exception e) {
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
