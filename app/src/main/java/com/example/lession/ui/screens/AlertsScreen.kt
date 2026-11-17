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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lession.data.db.AlertEvent
import com.example.lession.ui.components.LessionTopBar
import com.example.lession.ui.theme.LessionAlertRed
import com.example.lession.ui.theme.LessionBackgroundDark
import com.example.lession.ui.theme.LessionCardBorder
import com.example.lession.ui.theme.LessionCardDark
import com.example.lession.ui.theme.LessionLimeAccent
import com.example.lession.ui.theme.LessionPurplePrimary
import com.example.lession.ui.theme.LessionTextMuted
import com.example.lession.ui.theme.LessionTextWhite
import com.example.lession.ui.theme.LessionWarningOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlertsScreen(
    currentThreshold: Int,
    activeAlert: String?,
    alertHistory: List<AlertEvent>,
    onThresholdChanged: (Int) -> Unit,
    onClearAlert: () -> Unit,
    onBackClick: () -> Unit,
) {
    var sliderValue by remember(currentThreshold) { mutableFloatStateOf(currentThreshold.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LessionBackgroundDark),
    ) {
        LessionTopBar(title = "Sistema de Alertas", onBackClick = onBackClick)

        Column(modifier = Modifier.padding(16.dp)) {

            // Threshold Configurator Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LessionCardDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LessionPurplePrimary),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "CONFIGURAR UMBRAL DE ALERTA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LessionTextMuted,
                        )
                        Text(
                            text = "${sliderValue.toInt()} ADC",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = LessionLimeAccent,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            sliderValue = it
                            onThresholdChanged(it.toInt())
                        },
                        valueRange = 500f..4000f,
                        steps = 35,
                        colors = SliderDefaults.colors(
                            thumbColor = LessionLimeAccent,
                            activeTrackColor = LessionLimeAccent,
                            inactiveTrackColor = LessionCardBorder,
                        ),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = "500 ADC (Bajo)", fontSize = 10.sp, color = LessionTextMuted)
                        Text(text = "4000 ADC (Alto)", fontSize = 10.sp, color = LessionTextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Alert Display Box
            if (activeAlert != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LessionAlertRed),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = LessionTextWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¡ALERTA DE ESFUERZO DETECTADA!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LessionTextWhite,
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = activeAlert, fontSize = 12.sp, color = LessionTextWhite)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onClearAlert,
                            colors = ButtonDefaults.buttonColors(containerColor = LessionBackgroundDark),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(text = "Entendido / Descartar", fontSize = 11.sp, color = LessionTextWhite)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Info Disclaimer
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LessionCardDark),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LessionCardBorder),
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = LessionLimeAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Aviso: Las alertas identifican picos elevados de la señal EMG configurados por el usuario. No constituyen diagnóstico médico de fatiga.",
                        fontSize = 11.sp,
                        color = LessionTextMuted,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "REGISTRO DE EVENTOS OCURRIDOS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = LessionTextMuted,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(alertHistory) { alert ->
                    val dateFormat = SimpleDateFormat("HH:mm:ss - dd/MM/yyyy", Locale.getDefault())
                    val dateStr = dateFormat.format(Date(alert.timestampMs))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LessionCardDark),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LessionWarningOrange),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = LessionWarningOrange,
                                modifier = Modifier.size(24.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${alert.adcValue} ADC (Umbral: ${alert.thresholdValue})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LessionTextWhite,
                                )
                                Text(
                                    text = dateStr,
                                    fontSize = 11.sp,
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
