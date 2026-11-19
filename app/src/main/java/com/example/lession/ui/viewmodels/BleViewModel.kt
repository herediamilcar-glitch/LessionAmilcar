package com.example.lession.ui.viewmodels

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import com.example.lession.ble.BleConnectionState
import com.example.lession.ble.BleDeviceItem
import com.example.lession.ble.BleManager
import kotlinx.coroutines.flow.StateFlow

class BleViewModel(application: Application) : AndroidViewModel(application) {

    val bleManager = BleManager(application)

    val connectionState: StateFlow<BleConnectionState> = bleManager.connectionState
    val discoveredDevices: StateFlow<List<BleDeviceItem>> = bleManager.discoveredDevices

    fun startScan() {
        bleManager.startScan()
    }

    fun stopScan() {
        bleManager.stopScan()
    }

    fun connectToDevice(device: BluetoothDevice) {
        bleManager.connectToDevice(device)
    }

    fun disconnect() {
        bleManager.disconnect()
    }

    fun sendStartCommand() {
        bleManager.sendCommand("START")
    }

    fun sendStopCommand() {
        bleManager.sendCommand("STOP")
    }

    override fun onCleared() {
        bleManager.release()
        super.onCleared()
    }
}
