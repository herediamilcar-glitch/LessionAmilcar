package com.example.lession.ui.viewmodels

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lession.data.db.WorkoutDatabase
import com.example.lession.data.db.WorkoutMeasurement
import com.example.lession.data.db.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WorkoutDatabase.getInstance(application)
    private val sessionDao = db.workoutSessionDao()
    private val measurementDao = db.workoutMeasurementDao()
    private val selectedSessionId = MutableStateFlow<Long?>(null)

    val sessions: StateFlow<List<WorkoutSession>> = sessionDao.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedMeasurements: StateFlow<List<WorkoutMeasurement>> = selectedSessionId
        .flatMapLatest { sessionId ->
            if (sessionId == null) flowOf(emptyList())
            else measurementDao.getMeasurementsForSession(sessionId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    fun selectSession(sessionId: Long) {
        selectedSessionId.value = sessionId
    }

    fun deleteSession(session: WorkoutSession) {
        viewModelScope.launch {
            sessionDao.deleteSession(session)
        }
    }

    fun exportSession(session: WorkoutSession) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                val measurements = measurementDao.getMeasurementsForSessionOnce(session.id)

                val fileName = "LESSION_Entrenamiento_${dateFormat.format(Date(session.startTimeMs))}.csv"

                val csvContent = buildString {
                    appendLine("REPORTE DE ENTRENAMIENTO LESSION EMG")
                    appendLine("Fecha,Hora Inicio,Hora Fin,Duracion (seg),Maximo ADC,Minimo ADC,Promedio ADC,Total Muestras,Alertas")
                    appendLine(
                        "\"${displayFormat.format(Date(session.startTimeMs))}\",\"${displayFormat.format(Date(session.startTimeMs))}\",\"${displayFormat.format(Date(session.endTimeMs))}\",${session.durationSeconds},${session.maxAdc},${session.minAdc},${session.avgAdc},${session.sampleCount},${session.alertCount}"
                    )
                    appendLine()
                    appendLine("Marca de tiempo (ms),Lectura EMG (ADC)")
                    measurements.forEach { measurement ->
                        appendLine("${measurement.timestampMs},${measurement.adcValue}")
                    }
                }

                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                val file = File(downloadsDir, fileName)
                file.writeText(csvContent, Charsets.UTF_8)

                Toast.makeText(context, "Sesión descargada en Descargas:\n$fileName", Toast.LENGTH_LONG).show()

                val fileUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Compartir reporte LESSION").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(getApplication(), "Descargado en Descargas", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
