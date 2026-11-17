package com.example.lession.ble

enum class BleConnectionState {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    CONNECTED,
    RECEIVING_DATA,
    CONNECTION_LOST,
    ERROR,
}
