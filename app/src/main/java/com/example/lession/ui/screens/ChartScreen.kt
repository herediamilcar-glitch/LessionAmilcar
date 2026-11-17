package com.example.lession.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lession.ble.EmgReading
import com.example.lession.ui.components.LessionTopBar
import com.example.lession.ui.theme.LessionAlertRed
import com.example.lession.ui.theme.LessionBackgroundDark
import com.example.lession.ui.theme.LessionCardBorder
import com.example.lession.ui.theme.LessionCardDark
import com.example.lession.ui.theme.LessionLimeAccent
import com.example.lession.ui.theme.LessionPurplePrimary
import com.example.lession.ui.theme.LessionTextMuted
import com.example.lession.ui.theme.LessionTextWhite

@Composable
fun ChartScreen(
    readings: List<EmgReading>,
    alertThreshold: Int,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LessionBackgroundDark),
    ) {
        LessionTopBar(title = "Gráfico Temporal EMG", onBackClick = onBackClick)

        Column(modifier = Modifier.padding(16.dp)) {
            // Live Stats Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LessionCardDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LessionCardBorder),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Muestras en Pantalla: ${readings.size}",
                            fontSize = 12.sp,
                            color = LessionTextMuted,
                        )
                        Text(
                            text = if (readings.isNotEmpty()) "Lectura Sensor: ${readings.last().adcValue} ADC" else "Esperando señal del sensor...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = LessionLimeAccent,
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(2.dp)
                                .background(LessionAlertRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Umbral ($alertThreshold)",
                            fontSize = 11.sp,
                            color = LessionTextMuted,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Line Chart Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                colors = CardDefaults.cardColors(containerColor = LessionCardDark),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LessionPurplePrimary),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    EmgLineCanvasChart(
                        readings = readings,
                        alertThreshold = alertThreshold,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Guidance
            Text(
                text = "Gráfico continuo de la onda muscular EMG transmitida por el sensor a 50 Hz. Rango analógico digital: 0 - 4095 ADC.",
                fontSize = 12.sp,
                color = LessionTextMuted,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
fun EmgLineCanvasChart(
    readings: List<EmgReading>,
    alertThreshold: Int,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Draw Background Gridlines (0, 1000, 2000, 3000, 4000)
        val gridLevels = listOf(0, 1000, 2000, 3000, 4000)
        gridLevels.forEach { level ->
            val y = height - (level / 4095f) * height
            drawLine(
                color = LessionCardBorder,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        // Draw Threshold Line (Alert Red)
        val thresholdY = height - (alertThreshold / 4095f) * height
        drawLine(
            color = LessionAlertRed,
            start = Offset(0f, thresholdY),
            end = Offset(width, thresholdY),
            strokeWidth = 2.dp.toPx(),
        )

        if (readings.size < 2) return@Canvas

        val stepX = width / (readings.size - 1).coerceAtLeast(1)
        val path = Path()
        val fillPath = Path()

        readings.forEachIndexed { index, reading ->
            val x = index * stepX
            val normalizedY = (reading.adcValue / 4095f).coerceIn(0f, 1f)
            val y = height - (normalizedY * height)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == readings.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }

            // Highlight points exceeding threshold
            if (reading.adcValue >= alertThreshold) {
                drawCircle(
                    color = LessionAlertRed,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y),
                )
            }
        }

        // Draw Gradient Fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    LessionLimeAccent.copy(alpha = 0.25f),
                    Color.Transparent,
                )
            ),
        )

        // Draw Waveform Line
        drawPath(
            path = path,
            color = LessionLimeAccent,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
            ),
        )

        // Draw pulse dot at current (latest) point
        val lastReading = readings.last()
        val lastX = (readings.size - 1) * stepX
        val lastY = height - ((lastReading.adcValue / 4095f).coerceIn(0f, 1f) * height)

        drawCircle(
            color = LessionLimeAccent,
            radius = 6.dp.toPx(),
            center = Offset(lastX, lastY),
        )
        drawCircle(
            color = LessionBackgroundDark,
            radius = 3.dp.toPx(),
            center = Offset(lastX, lastY),
        )
    }
}
