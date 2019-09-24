package com.pawelcembaluk.armcontroller.interfaces;

public interface SerialListener {
    void onConnect();

    void onConnectionFailed(Exception e);

    void onDataReceived(byte[] data);

    void onDisconnect(Exception e);
}
