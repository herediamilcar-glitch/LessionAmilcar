package com.example.lession.ble

import android.bluetooth.BluetoothDevice

data class BleDeviceItem(
    val device: BluetoothDevice,
    val name: String,
    val address: String,
    val rssi: Int,
)
