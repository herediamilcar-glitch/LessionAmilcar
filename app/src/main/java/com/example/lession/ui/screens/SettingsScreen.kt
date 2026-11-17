package com.example.lession.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lession.ui.components.LessionTopBar
import com.example.lession.ui.theme.LessionBackgroundDark
import com.example.lession.ui.theme.LessionCardBorder
import com.example.lession.ui.theme.LessionCardDark
import com.example.lession.ui.theme.LessionLimeAccent
import com.example.lession.ui.theme.LessionTextMuted
import com.example.lession.ui.theme.LessionTextWhite

@Composable
fun SettingsScreen(
    autoReconnect: Boolean,
    onAutoReconnectChanged: (Boolean) -> Unit,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LessionBackgroundDark),
    ) {
        LessionTopBar(title = "Configuración LESSION", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "PREFERENCIAS DE CONEXIÓN BLE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = LessionTextMuted,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LessionCardDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LessionCardBorder),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(imageVector = Icons.Default.Bluetooth, contentDescription = null, tint = LessionLimeAccent)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Reconexión Automática", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LessionTextWhite)
                        Text(text = "Intentar reconectar automáticamente al perder señal con ESP32", fontSize = 11.sp, color = LessionTextMuted)
                    }
                    Switch(
                        checked = autoReconnect,
                        onCheckedChange = onAutoReconnectChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = LessionLimeAccent,
                            checkedTrackColor = LessionLimeAccent.copy(alpha = 0.5f),
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "INFORMACIÓN DEL DISPOSITIVO Y HARDWARE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = LessionTextMuted,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LessionCardDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LessionCardBorder),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingInfoRow("Placa Controladora", "ESP32-C3 RISC-V")
                    SettingInfoRow("Protocolo BLE", "GATT Service (6E400001)")
                    SettingInfoRow("Característica TX", "Notify (6E400003)")
                    SettingInfoRow("Característica RX", "Write (6E400002)")
                    SettingInfoRow("Frecuencia de Muestreo", "50 Hz (Muestra c/20ms)")
                    SettingInfoRow("Formato de Transmisión", "ASCII (t_ms,adc\\n)")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "ACERCA DEL PROYECTO LESSION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = LessionTextMuted,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LessionCardDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LessionCardBorder),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = LessionLimeAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "LESSION v1.0.0", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LessionTextWhite)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sistema de monitoreo de actividad muscular en tiempo real para deportistas de alto rendimiento mediante banda inteligente EMG.",
                        fontSize = 12.sp,
                        color = LessionTextMuted,
                        lineHeight = 18.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun SettingInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, fontSize = 12.sp, color = LessionTextMuted)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LessionTextWhite)
    }
}
