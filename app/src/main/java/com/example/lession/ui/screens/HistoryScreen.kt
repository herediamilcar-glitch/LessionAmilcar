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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lession.data.db.WorkoutSession
import com.example.lession.ui.components.LessionTopBar
import com.example.lession.ui.theme.LessionAlertRed
import com.example.lession.ui.theme.LessionBackgroundDark
import com.example.lession.ui.theme.LessionCardBorder
import com.example.lession.ui.theme.LessionCardDark
import com.example.lession.ui.theme.LessionLimeAccent
import com.example.lession.ui.theme.LessionTextMuted
import com.example.lession.ui.theme.LessionTextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    sessions: List<WorkoutSession>,
    onDeleteSession: (WorkoutSession) -> Unit,
    onExportSession: (WorkoutSession) -> Unit,
    onBackClick: () -> Unit,
) {
    var sessionToDelete by remember { mutableStateOf<WorkoutSession?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LessionBackgroundDark),
    ) {
        LessionTopBar(title = "Historial de Entrenamientos", onBackClick = onBackClick)

        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "SESIONES GUARDADAS EN SQLITE (${sessions.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = LessionTextMuted,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            if (sessions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = LessionTextMuted,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aún no hay entrenamientos registrados.",
                        color = LessionTextMuted,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = "Inicia un entrenamiento desde el Panel Principal para guardar datos.",
                        color = LessionTextMuted,
                        fontSize = 12.sp,
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(sessions) { session ->
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())
                    val dateStr = dateFormat.format(Date(session.startTimeMs))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LessionCardDark),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LessionCardBorder),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = LessionLimeAccent,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = session.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LessionTextWhite,
                                    )
                                }

                                Row {
                                    IconButton(onClick = { onExportSession(session) }) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "Descargar Sesión",
                                            tint = LessionLimeAccent,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                    IconButton(onClick = { sessionToDelete = session }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = LessionAlertRed,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
                            }

                            Text(
                                text = dateStr,
                                fontSize = 12.sp,
                                color = LessionTextMuted,
                                modifier = Modifier.padding(bottom = 12.dp),
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                HistoryStatItem("Duración", "${session.durationSeconds}s")
                                HistoryStatItem("Máx ADC", "${session.maxAdc}")
                                HistoryStatItem("Prom ADC", "${session.avgAdc}")
                                HistoryStatItem("Alertas", "${session.alertCount}")
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Alert Dialog when deleting
    if (sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = {
                Text(
                    text = "Confirmar eliminación",
                    color = LessionTextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            },
            text = {
                Text(
                    text = "¿quiere borrar esta sesion?",
                    color = LessionTextWhite,
                    fontSize = 15.sp,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        sessionToDelete?.let { onDeleteSession(it) }
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LessionAlertRed),
                ) {
                    Text(text = "Sí", color = LessionTextWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { sessionToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = LessionCardDark),
                ) {
                    Text(text = "No", color = LessionTextWhite)
                }
            },
            containerColor = LessionCardDark,
            shape = RoundedCornerShape(16.dp),
        )
    }
}

@Composable
fun HistoryStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = LessionTextMuted)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LessionLimeAccent)
    }
}
