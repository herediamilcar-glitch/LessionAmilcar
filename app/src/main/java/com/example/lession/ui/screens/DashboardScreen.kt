package com.example.lession.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lession.ble.BleConnectionState
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
import com.example.lession.ui.viewmodels.WorkoutStats

@Composable
fun DashboardScreen(
    connectionState: BleConnectionState,
    workoutStats: WorkoutStats,
    activeAlert: String?,
    onNavigate: (String) -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LessionBackgroundDark)
            .padding(16.dp),
    ) {
        // App Bar / Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "LESSION",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = LessionLimeAccent,
                letterSpacing = 2.sp,
            )

            // Connection Badge
            val statusColor = when (connectionState) {
                BleConnectionState.RECEIVING_DATA, BleConnectionState.CONNECTED -> LessionStatusGreen
                BleConnectionState.CONNECTING, BleConnectionState.SCANNING -> LessionWarningOrange
                BleConnectionState.CONNECTION_LOST, BleConnectionState.ERROR -> LessionAlertRed
                BleConnectionState.DISCONNECTED -> LessionTextMuted
            }

            val statusText = when (connectionState) {
                BleConnectionState.RECEIVING_DATA -> "Recibiendo Datos"
                BleConnectionState.CONNECTED -> "Conectado"
                BleConnectionState.CONNECTING -> "Conectando..."
                BleConnectionState.SCANNING -> "Buscando..."
                BleConnectionState.CONNECTION_LOST -> "Reconectando..."
                BleConnectionState.ERROR -> "Error BLE"
                BleConnectionState.DISCONNECTED -> "Desconectado"
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(LessionCardDark)
                    .border(1.dp, statusColor, RoundedCornerShape(12.dp))
                    .clickable { onNavigate("device_scan") }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LessionTextWhite,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Active Alert Banner if triggered
        if (activeAlert != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = LessionAlertRed),
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Alerta",
                        tint = LessionTextWhite,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = activeAlert,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LessionTextWhite,
                    )
                }
            }
        }

        // Live Metric Summary Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LessionCardDark),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, LessionPurplePrimary),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "ACTIVIDAD MUSCULAR EMG",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LessionTextMuted,
                        letterSpacing = 1.sp,
                    )

                    Text(
                        text = "${workoutStats.sampleRateHz} Hz",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LessionLimeAccent,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "${workoutStats.currentAdc}",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = LessionLimeAccent,
                        )
                        Text(
                            text = "Unidades ADC (0 - 4095)",
                            fontSize = 11.sp,
                            color = LessionTextMuted,
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Máx: ${workoutStats.maxAdc}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LessionTextWhite,
                        )
                        Text(
                            text = "Prom: ${workoutStats.avgAdc}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LessionTextWhite,
                        )
                        Text(
                            text = "Muestras: ${workoutStats.sampleCount}",
                            fontSize = 12.sp,
                            color = LessionTextMuted,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Record / Stop Session Button
                if (!workoutStats.isRecording) {
                    Button(
                        onClick = onStartRecording,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LessionLimeAccent, contentColor = LessionBackgroundDark),
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "INICIAR SESIÓN DE ENTRENAMIENTO", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onStopRecording,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LessionAlertRed, contentColor = LessionTextWhite),
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "DETENER Y GUARDAR ENTRENAMIENTO", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "MENÚ PRINCIPAL",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = LessionTextMuted,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // 2x3 Quick Action Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                DashboardCard(
                    title = "Conectar Banda",
                    subtitle = "ESP32 BLE",
                    icon = if (connectionState == BleConnectionState.RECEIVING_DATA) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                    iconColor = LessionLimeAccent,
                    onClick = { onNavigate("device_scan") },
                )
            }
            item {
                DashboardCard(
                    title = "Mediciones",
                    subtitle = "Valores en Vivo",
                    icon = Icons.Default.Speed,
                    iconColor = LessionLimeAccent,
                    onClick = { onNavigate("metrics") },
                )
            }
            item {
                DashboardCard(
                    title = "Gráficos",
                    subtitle = "Onda Temporal",
                    icon = Icons.Default.ShowChart,
                    iconColor = LessionLimeAccent,
                    onClick = { onNavigate("charts") },
                )
            }
            item {
                DashboardCard(
                    title = "Alertas",
                    subtitle = "Parámetros Fatiga",
                    icon = Icons.Default.NotificationsActive,
                    iconColor = LessionWarningOrange,
                    onClick = { onNavigate("alerts") },
                )
            }
            item {
                DashboardCard(
                    title = "Historial",
                    subtitle = "Sesiones SQLite",
                    icon = Icons.Default.History,
                    iconColor = LessionLimeAccent,
                    onClick = { onNavigate("history") },
                )
            }
            item {
                DashboardCard(
                    title = "Configuración",
                    subtitle = "Ajustes de App",
                    icon = Icons.Default.Settings,
                    iconColor = LessionTextMuted,
                    onClick = { onNavigate("settings") },
                )
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = LessionCardDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LessionCardBorder),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(28.dp),
            )
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = LessionTextWhite,
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = LessionTextMuted,
                )
            }
        }
    }
}
