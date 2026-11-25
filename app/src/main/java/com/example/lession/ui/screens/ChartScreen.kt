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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
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
            // Live Stats Header Card
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
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "Muestras 120s: ${readings.size}",
                            fontSize = 12.sp,
                            color = LessionTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (readings.isNotEmpty()) "ADC: ${readings.last().adcValue}" else "Esperando sensor ESP32...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LessionLimeAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
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
                            fontWeight = FontWeight.Bold,
                            color = LessionTextMuted,
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Line Chart Box with labeled X and Y axes
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(390.dp),
                colors = CardDefaults.cardColors(containerColor = LessionCardDark),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LessionPurplePrimary),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
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
                text = "Gráfico dinámico en vivo del sensor ESP32. Eje Y: Amplitud ADC (0 a 4095). Eje X: Ventana temporal de 0 a 120 segundos (50 Hz).",
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
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val fullWidth = size.width
        val fullHeight = size.height

        // Axis paddings
        val paddingLeft = 46.dp.toPx()
        val paddingBottom = 26.dp.toPx()
        val paddingTop = 12.dp.toPx()
        val paddingRight = 12.dp.toPx()

        val chartWidth = fullWidth - paddingLeft - paddingRight
        val chartHeight = fullHeight - paddingTop - paddingBottom

        val axisTextStyle = TextStyle(
            color = LessionTextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )

        // 1. Draw Y-Axis Gridlines & Labels (0, 1000, 2000, 3000, 4095 ADC)
        val yGridLevels = listOf(0, 1000, 2000, 3000, 4095)
        yGridLevels.forEach { level ->
            val normY = level / 4095f
            val y = paddingTop + chartHeight * (1f - normY)

            // Horizontal Gridline
            drawLine(
                color = LessionCardBorder,
                start = Offset(paddingLeft, y),
                end = Offset(paddingLeft + chartWidth, y),
                strokeWidth = 1.dp.toPx(),
            )

            // Y-Axis Text Label
            val labelText = "$level"
            val textLayout = textMeasurer.measure(labelText, style = axisTextStyle)
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(paddingLeft - textLayout.size.width - 6.dp.toPx(), y - textLayout.size.height / 2f)
            )
        }

        // 2. Draw X-Axis Labels (0s, 30s, 60s, 90s, 120s)
        val xTimeTicks = listOf("0s", "30s", "60s", "90s", "120s")
        val tickCount = xTimeTicks.size
        xTimeTicks.forEachIndexed { index, tickText ->
            val fraction = index / (tickCount - 1).toFloat()
            val x = paddingLeft + chartWidth * fraction

            // Vertical Gridline
            drawLine(
                color = LessionCardBorder.copy(alpha = 0.5f),
                start = Offset(x, paddingTop),
                end = Offset(x, paddingTop + chartHeight),
                strokeWidth = 1.dp.toPx(),
            )

            // X-Axis Text Label
            val textLayout = textMeasurer.measure(tickText, style = axisTextStyle)
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(x - textLayout.size.width / 2f, paddingTop + chartHeight + 6.dp.toPx())
            )
        }

        // Draw Threshold Line (Alert Red)
        val thresholdNormY = alertThreshold / 4095f
        val thresholdY = paddingTop + chartHeight * (1f - thresholdNormY)
        drawLine(
            color = LessionAlertRed,
            start = Offset(paddingLeft, thresholdY),
            end = Offset(paddingLeft + chartWidth, thresholdY),
            strokeWidth = 2.dp.toPx(),
        )

        if (readings.size < 2) return@Canvas

        // 3. Map Waveform Points across the 120s Window
        val path = Path()
        val fillPath = Path()

        val firstTimestamp = readings.first().timestampMs

        readings.forEachIndexed { index, reading ->
            val elapsedSec = ((reading.timestampMs - firstTimestamp) / 1000f).coerceIn(0f, 120f)
            val fractionX = if (readings.size == 1) 0f else (elapsedSec / 120f).coerceIn(0f, 1f)

            // Also fallback to index-based distribution if timestamp range is short
            val indexFractionX = index / (readings.size - 1).toFloat()
            val finalFractionX = if (readings.last().timestampMs - firstTimestamp < 1000) indexFractionX else fractionX

            val x = paddingLeft + chartWidth * finalFractionX
            val normY = (reading.adcValue / 4095f).coerceIn(0f, 1f)
            val y = paddingTop + chartHeight * (1f - normY)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, paddingTop + chartHeight)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == readings.size - 1) {
                fillPath.lineTo(x, paddingTop + chartHeight)
                fillPath.close()
            }

            // Highlight points exceeding alert threshold
            if (reading.adcValue >= alertThreshold) {
                drawCircle(
                    color = LessionAlertRed,
                    radius = 3.5.dp.toPx(),
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
                ),
                startY = paddingTop,
                endY = paddingTop + chartHeight,
            ),
        )

        // Draw Waveform Line
        drawPath(
            path = path,
            color = LessionLimeAccent,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
            ),
        )

        // Draw Live Point Marker at the tip of the waveform
        val lastReading = readings.last()
        val lastElapsedSec = ((lastReading.timestampMs - firstTimestamp) / 1000f).coerceIn(0f, 120f)
        val lastFractionX = if (readings.last().timestampMs - firstTimestamp < 1000) 1f else (lastElapsedSec / 120f).coerceIn(0f, 1f)
        val lastX = paddingLeft + chartWidth * lastFractionX
        val lastY = paddingTop + chartHeight * (1f - (lastReading.adcValue / 4095f).coerceIn(0f, 1f))

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
