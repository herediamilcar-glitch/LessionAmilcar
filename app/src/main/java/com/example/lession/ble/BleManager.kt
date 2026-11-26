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
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
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
    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                if (state == BluetoothAdapter.STATE_TURNING_OFF || state == BluetoothAdapter.STATE_OFF) {
                    handleBluetoothDisabled()
                }
            }
        }
    }

    init {
        ContextCompat.registerReceiver(
            context.applicationContext,
            bluetoothStateReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

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
                val rawName = device.name ?: result.scanRecord?.deviceName ?: ""
                val isLessionDevice = rawName.contains("LESSION", ignoreCase = true) || rawName.contains("ESP32", ignoreCase = true)

                // STRICT FILTER: Only show official LESSION / ESP32 devices
                if (!isLessionDevice) return@let

                val name = if (rawName.isBlank()) "ESP32 LESSION" else rawName
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
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner?.startScan(null, settings, scanCallback)

        mainHandler.postDelayed({
            if (_connectionState.value == BleConnectionState.SCANNING) {
                stopScan()
            }
        }, 12000)
    }

    fun stopScan() {
        if (_connectionState.value == BleConnectionState.SCANNING) {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            _connectionState.value = BleConnectionState.DISCONNECTED
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        stopScan()
        if (!isBluetoothEnabled()) {
            _connectionState.value = BleConnectionState.ERROR
            return
        }

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
        selectedDevice = null
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        txCharacteristic = null
        rxCharacteristic = null
        _connectionState.value = BleConnectionState.DISCONNECTED
    }

    fun release() {
        disconnect()
        context.applicationContext.unregisterReceiver(bluetoothStateReceiver)
    }

    fun sendCommand(command: String) {
        val gatt = bluetoothGatt ?: return
        val rx = rxCharacteristic ?: return

        rx.value = command.toByteArray(Charsets.UTF_8)
        gatt.writeCharacteristic(rx)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (gatt == null || gatt !== bluetoothGatt) return

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    if (status != BluetoothGatt.GATT_SUCCESS || !isBluetoothEnabled()) {
                        failConnection(gatt, "GATT connection failed with status $status")
                        return
                    }

                    Log.i("BleManager", "GATT connected. Discovering services...")
                    if (!gatt.discoverServices()) {
                        failConnection(gatt, "Unable to discover GATT services")
                    }
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.w("BleManager", "GATT Disconnected")
                    txCharacteristic = null
                    rxCharacteristic = null
                    bluetoothGatt = null
                    gatt.close()

                    if (!isBluetoothEnabled()) {
                        autoReconnectEnabled = false
                        selectedDevice = null
                        _connectionState.value = BleConnectionState.ERROR
                    } else if (autoReconnectEnabled && selectedDevice != null) {
                        _connectionState.value = BleConnectionState.CONNECTION_LOST
                        mainHandler.postDelayed({
                            if (autoReconnectEnabled && selectedDevice != null && isBluetoothEnabled()) {
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
            if (gatt == null || gatt !== bluetoothGatt) return
            if (status != BluetoothGatt.GATT_SUCCESS) {
                failConnection(gatt, "GATT service discovery failed with status $status")
                return
            }

            val service = gatt.getService(BleProtocol.SERVICE_UUID)
            val tx = service?.getCharacteristic(BleProtocol.TX_CHARACTERISTIC_UUID)
            val rx = service?.getCharacteristic(BleProtocol.RX_CHARACTERISTIC_UUID)
            if (service == null || tx == null || rx == null) {
                failConnection(gatt, "Required LESSION BLE service or characteristics not found")
                return
            }

            txCharacteristic = tx
            rxCharacteristic = rx
            gatt.setCharacteristicNotification(tx, true)
            val descriptor = tx.getDescriptor(BleProtocol.CLIENT_CHARACTERISTIC_CONFIG)
            if (descriptor != null) {
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            }
            Log.i("BleManager", "BLE service and characteristics discovered successfully")
            _connectionState.value = BleConnectionState.CONNECTED
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

    private fun handleBluetoothDisabled() {
        autoReconnectEnabled = false
        selectedDevice = null
        val gatt = bluetoothGatt
        bluetoothGatt = null
        txCharacteristic = null
        rxCharacteristic = null
        gatt?.disconnect()
        gatt?.close()
        _connectionState.value = BleConnectionState.ERROR
    }

    private fun failConnection(gatt: BluetoothGatt, message: String) {
        Log.e("BleManager", message)
        if (gatt !== bluetoothGatt) return

        autoReconnectEnabled = false
        selectedDevice = null
        bluetoothGatt = null
        txCharacteristic = null
        rxCharacteristic = null
        gatt.disconnect()
        gatt.close()
        _connectionState.value = BleConnectionState.ERROR
    }
}
