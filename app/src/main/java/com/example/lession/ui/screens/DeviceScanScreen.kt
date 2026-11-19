package com.example.lession.ui.screens

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lession.ble.BleConnectionState
import com.example.lession.ble.BleDeviceItem
import com.example.lession.ui.components.LessionTopBar
import com.example.lession.ui.theme.LessionAlertRed
import com.example.lession.ui.theme.LessionBackgroundDark
import com.example.lession.ui.theme.LessionCardBorder
import com.example.lession.ui.theme.LessionCardDark
import com.example.lession.ui.theme.LessionLimeAccent
import com.example.lession.ui.theme.LessionPurplePrimary
import com.example.lession.ui.theme.LessionStatusGreen
import com.example.lession.ui.theme.LessionTextMuted
import com.example.lession.ui.theme.LessionTextWhite
import com.example.lession.ui.theme.LessionWarningOrange

@Composable
fun DeviceScanScreen(
    connectionState: BleConnectionState,
    discoveredDevices: List<BleDeviceItem>,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDevice: (BluetoothDevice) -> Unit,
    onDisconnectDevice: () -> Unit,
    onBackClick: () -> Unit,
) {
    LaunchedEffect(Unit) {
        if (connectionState == BleConnectionState.DISCONNECTED) {
            onStartScan()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LessionBackgroundDark),
    ) {
        LessionTopBar(title = "Conectar Banda LESSION", onBackClick = onBackClick)

        Column(modifier = Modifier.padding(16.dp)) {

            // Connection Status Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LessionCardDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LessionPurplePrimary),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val (statusColor, statusTitle) = when (connectionState) {
                        BleConnectionState.RECEIVING_DATA -> LessionStatusGreen to "Conectado y Recibiendo Datos"
                        BleConnectionState.CONNECTED -> LessionStatusGreen to "Conectado a ESP32 LESSION"
                        BleConnectionState.CONNECTING -> LessionWarningOrange to "Estableciendo Conexión..."
                        BleConnectionState.SCANNING -> LessionLimeAccent to "Buscando Dispositivos BLE..."
                        BleConnectionState.CONNECTION_LOST -> LessionAlertRed to "Conexión Interrumpida - Reconectando..."
                        BleConnectionState.ERROR -> LessionAlertRed to "Error de conexión BLE"
                        BleConnectionState.DISCONNECTED -> LessionTextMuted to "Desconectado"
                    }

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = statusTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LessionTextWhite,
                        )
                        Text(
                            text = "UUID: 6E400001-B5A3-F393-E0A9-E50E24DCCA9E",
                            fontSize = 10.sp,
                            color = LessionTextMuted,
                        )
                    }

                    if (connectionState == BleConnectionState.CONNECTED || connectionState == BleConnectionState.RECEIVING_DATA) {
                        Button(
                            onClick = onDisconnectDevice,
                            colors = ButtonDefaults.buttonColors(containerColor = LessionAlertRed),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(text = "Desconectar", fontSize = 11.sp, color = LessionTextWhite)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action button (Scan / Stop Scan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "DISPOSITIVOS DETECTADOS (${discoveredDevices.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LessionTextMuted,
                )

                if (connectionState == BleConnectionState.SCANNING) {
                    Button(
                        onClick = onStopScan,
                        colors = ButtonDefaults.buttonColors(containerColor = LessionAlertRed),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text(text = "Detener", fontSize = 12.sp, color = LessionTextWhite)
                    }
                } else {
                    Button(
                        onClick = onStartScan,
                        colors = ButtonDefaults.buttonColors(containerColor = LessionLimeAccent, contentColor = LessionBackgroundDark),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Escanear BLE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (connectionState == BleConnectionState.SCANNING && discoveredDevices.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator(color = LessionLimeAccent)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Buscando 'ESP32 LESSION'...",
                        color = LessionTextMuted,
                        fontSize = 14.sp,
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(discoveredDevices) { item ->
                    val isTargetDevice = item.name.contains("ESP32", ignoreCase = true) || item.name.contains("LESSION", ignoreCase = true)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConnectDevice(item.device) },
                        colors = CardDefaults.cardColors(containerColor = LessionCardDark),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isTargetDevice) LessionLimeAccent else LessionCardBorder
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isTargetDevice) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = if (isTargetDevice) LessionLimeAccent else LessionTextMuted,
                                    modifier = Modifier.size(28.dp),
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTargetDevice) LessionLimeAccent else LessionTextWhite,
                                    )
                                    Text(
                                        text = item.address,
                                        fontSize = 11.sp,
                                        color = LessionTextMuted,
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${item.rssi} dBm",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LessionLimeAccent,
                                )
                                Text(
                                    text = if (isTargetDevice) "OFICIAL" else "BLE",
                                    fontSize = 10.sp,
                                    color = LessionTextMuted,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
