package com.example.lession.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter
    }

    private var bluetoothGatt: BluetoothGatt? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null
    private var rxCharacteristic: BluetoothGattCharacteristic? = null
    private var selectedDevice: BluetoothDevice? = null

    private val _connectionState = MutableStateFlow(BleConnectionState.DISCONNECTED)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BleDeviceItem>>(emptyList())
    val discoveredDevices: StateFlow<List<BleDeviceItem>> = _discoveredDevices.asStateFlow()

    private val _latestReading = MutableStateFlow<EmgReading?>(null)
    val latestReading: StateFlow<EmgReading?> = _latestReading.asStateFlow()

    private val _readingsFlow = MutableSharedFlow<EmgReading>(extraBufferCapacity = 100)
    val readingsFlow: SharedFlow<EmgReading> = _readingsFlow.asSharedFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.Default)

    private val streamParser = BleStreamParser { reading ->
        _latestReading.value = reading
        scope.launch {
            _readingsFlow.emit(reading)
        }
        if (_connectionState.value == BleConnectionState.CONNECTED) {
            _connectionState.value = BleConnectionState.RECEIVING_DATA
        }
    }

    private var autoReconnectEnabled = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                val name = device.name ?: result.scanRecord?.deviceName ?: "Desconocido"
                val address = device.address
                val rssi = result.rssi

                val currentList = _discoveredDevices.value.toMutableList()
                val existingIndex = currentList.indexOfFirst { it.address == address }

                val newItem = BleDeviceItem(device, name, address, rssi)
                if (existingIndex >= 0) {
                    currentList[existingIndex] = newItem
                } else {
                    currentList.add(newItem)
                }

                _discoveredDevices.value = currentList.sortedByDescending { it.rssi }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BleManager", "Scan failed with error: $errorCode")
            _connectionState.value = BleConnectionState.ERROR
        }
    }

    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    fun startScan() {
        if (!isBluetoothEnabled()) {
            _connectionState.value = BleConnectionState.ERROR
            return
        }

        _discoveredDevices.value = emptyList()
        _connectionState.value = BleConnectionState.SCANNING

        val scanner = bluetoothAdapter?.bluetoothLeScanner
        scanner?.startScan(scanCallback)

        mainHandler.postDelayed({
            if (_connectionState.value == BleConnectionState.SCANNING) {
                stopScan()
            }
        }, 10000)
    }

    fun stopScan() {
        if (_connectionState.value == BleConnectionState.SCANNING) {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            _connectionState.value = BleConnectionState.DISCONNECTED
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        stopScan()
        selectedDevice = device
        autoReconnectEnabled = true
        _connectionState.value = BleConnectionState.CONNECTING
        streamParser.clear()

        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()

        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    fun disconnect() {
        autoReconnectEnabled = false
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        txCharacteristic = null
        rxCharacteristic = null
        _connectionState.value = BleConnectionState.DISCONNECTED
    }

    fun sendCommand(command: String) {
        val gatt = bluetoothGatt ?: return
        val rx = rxCharacteristic ?: return

        rx.value = command.toByteArray(Charsets.UTF_8)
        gatt.writeCharacteristic(rx)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i("BleManager", "GATT Connected. Discovering services...")
                    _connectionState.value = BleConnectionState.CONNECTED
                    gatt?.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.w("BleManager", "GATT Disconnected")
                    txCharacteristic = null
                    rxCharacteristic = null

                    if (autoReconnectEnabled && selectedDevice != null) {
                        _connectionState.value = BleConnectionState.CONNECTION_LOST
                        mainHandler.postDelayed({
                            if (autoReconnectEnabled && selectedDevice != null) {
                                connectToDevice(selectedDevice!!)
                            }
                        }, 3000)
                    } else {
                        _connectionState.value = BleConnectionState.DISCONNECTED
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                val service = gatt.getService(BleProtocol.SERVICE_UUID)
                if (service != null) {
                    txCharacteristic = service.getCharacteristic(BleProtocol.TX_CHARACTERISTIC_UUID)
                    rxCharacteristic = service.getCharacteristic(BleProtocol.RX_CHARACTERISTIC_UUID)

                    txCharacteristic?.let { tx ->
                        gatt.setCharacteristicNotification(tx, true)
                        val descriptor = tx.getDescriptor(BleProtocol.CLIENT_CHARACTERISTIC_CONFIG)
                        if (descriptor != null) {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }
                    }
                    Log.i("BleManager", "BLE Service & Characteristics Discovered Successfully")
                } else {
                    Log.e("BleManager", "LESSION Service not found on device")
                }
            }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            if (characteristic?.uuid == BleProtocol.TX_CHARACTERISTIC_UUID) {
                val bytes = characteristic.value
                if (bytes != null && bytes.isNotEmpty()) {
                    streamParser.parseChunk(bytes)
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid == BleProtocol.TX_CHARACTERISTIC_UUID) {
                streamParser.parseChunk(value)
            }
        }
    }
}
