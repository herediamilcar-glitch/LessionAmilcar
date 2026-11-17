package com.example.lession.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
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
import com.example.lession.ui.components.LessionTopBar
import com.example.lession.ui.theme.LessionBackgroundDark
import com.example.lession.ui.theme.LessionCardBorder
import com.example.lession.ui.theme.LessionCardDark
import com.example.lession.ui.theme.LessionLimeAccent
import com.example.lession.ui.theme.LessionPurplePrimary
import com.example.lession.ui.theme.LessionTextMuted
import com.example.lession.ui.theme.LessionTextWhite
import com.example.lession.ui.viewmodels.WorkoutStats

@Composable
fun MetricsScreen(
    workoutStats: WorkoutStats,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LessionBackgroundDark),
    ) {
        LessionTopBar(title = "Mediciones Físicas EMG", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Main Live EMG Value Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LessionCardDark),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, LessionLimeAccent),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = LessionLimeAccent,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SEÑAL ELECTROMIOGRÁFICA (EMG)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LessionLimeAccent,
                            letterSpacing = 1.sp,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (workoutStats.hasReceivedData) "${workoutStats.currentAdc}" else "--",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                        color = LessionTextWhite,
                    )

                    Text(
                        text = if (workoutStats.hasReceivedData) "Lectura ADC actual (0 a 4095)" else "Esperando paquetes del sensor ESP32...",
                        fontSize = 12.sp,
                        color = LessionTextMuted,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LessionBackgroundDark)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        MetricMiniItem("MÁXIMO", if (workoutStats.hasReceivedData) "${workoutStats.maxAdc}" else "--")
                        MetricMiniItem("MÍNIMO", if (workoutStats.hasReceivedData) "${workoutStats.minAdc}" else "--")
                        MetricMiniItem("PROMEDIO", if (workoutStats.hasReceivedData) "${workoutStats.avgAdc}" else "--")
                        MetricMiniItem("MUESTRAS", if (workoutStats.hasReceivedData) "${workoutStats.sampleRateHz} Hz" else "0 Hz")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Clarification Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LessionCardDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LessionCardBorder),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = LessionLimeAccent,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Nota Técnica: La señal representa la envolvente rectificada y suavizada del esfuerzo muscular enviada desde el ESP32-C3 a 50 Muestras/seg. Las cuentas ADC digitales corresponden al rango analógico de 0 a 4095.",
                        fontSize = 11.sp,
                        color = LessionTextMuted,
                        lineHeight = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "MÓDULOS DE SENSORES FUTUROS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = LessionTextMuted,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            // Future Expansion Placeholders
            FutureSensorCard("Frecuencia Cardíaca", "Próxima integración PPG", Icons.Default.FavoriteBorder)
            Spacer(modifier = Modifier.height(10.dp))
            FutureSensorCard("Temperatura Corporal", "Sensor Térmico NTC/DS18B20", Icons.Default.Thermostat)
            Spacer(modifier = Modifier.height(10.dp))
            FutureSensorCard("Acelerómetro / IMU", "Análisis de Movimiento 6-Ejes", Icons.Default.Speed)
        }
    }
}

@Composable
fun MetricMiniItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = LessionTextMuted, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 15.sp, color = LessionLimeAccent, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FutureSensorCard(title: String, subtitle: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LessionCardDark.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LessionCardBorder),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(LessionPurplePrimary.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = LessionTextMuted, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LessionTextWhite)
                Text(text = subtitle, fontSize = 11.sp, color = LessionTextMuted)
            }
        }
    }
}
