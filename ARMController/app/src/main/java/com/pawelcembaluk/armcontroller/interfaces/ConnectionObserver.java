package com.pawelcembaluk.armcontroller.interfaces;

public interface ConnectionObserver {
    void onConnect();

    void onConnectionFailed();

    void onDisconnect();
}
