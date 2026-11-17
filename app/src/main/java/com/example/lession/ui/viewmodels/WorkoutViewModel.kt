package com.example.lession.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lession.ble.BleManager
import com.example.lession.ble.EmgReading
import com.example.lession.data.db.AlertEvent
import com.example.lession.data.db.WorkoutDatabase
import com.example.lession.data.db.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorkoutStats(
    val currentAdc: Int = 0,
    val maxAdc: Int = 0,
    val minAdc: Int = 0,
    val avgAdc: Int = 0,
    val sampleCount: Int = 0,
    val sampleRateHz: Int = 0,
    val isRecording: Boolean = false,
    val startTimeMs: Long = 0,
    val alertCount: Int = 0,
)

class WorkoutViewModel(
    application: Application,
    private val bleManager: BleManager,
) : AndroidViewModel(application) {

    private val db = WorkoutDatabase.getInstance(application)
    private val sessionDao = db.workoutSessionDao()
    private val alertDao = db.alertEventDao()

    private val _workoutStats = MutableStateFlow(WorkoutStats())
    val workoutStats: StateFlow<WorkoutStats> = _workoutStats.asStateFlow()

    private val _chartPoints = MutableStateFlow<List<EmgReading>>(emptyList())
    val chartPoints: StateFlow<List<EmgReading>> = _chartPoints.asStateFlow()

    private val _alertThreshold = MutableStateFlow(3200)
    val alertThreshold: StateFlow<Int> = _alertThreshold.asStateFlow()

    private val _activeAlert = MutableStateFlow<String?>(null)
    val activeAlert: StateFlow<String?> = _activeAlert.asStateFlow()

    private var totalSum: Long = 0
    private var samplesInLastSecond = 0
    private var lastHzCalcTime = System.currentTimeMillis()

    init {
        viewModelScope.launch {
            bleManager.readingsFlow.collect { reading ->
                processNewReading(reading)
            }
        }
    }

    fun setAlertThreshold(threshold: Int) {
        _alertThreshold.value = threshold
    }

    fun clearActiveAlert() {
        _activeAlert.value = null
    }

    fun startRecording() {
        val now = System.currentTimeMillis()
        totalSum = 0
        samplesInLastSecond = 0
        lastHzCalcTime = now

        _workoutStats.value = WorkoutStats(
            isRecording = true,
            startTimeMs = now,
        )
        _chartPoints.value = emptyList()
        bleManager.sendCommand("START")
    }

    fun stopRecording() {
        val current = _workoutStats.value
        if (!current.isRecording) return

        val endTime = System.currentTimeMillis()
        val durationSec = ((endTime - current.startTimeMs) / 1000).coerceAtLeast(1)

        val sessionToSave = WorkoutSession(
            title = "Entrenamiento LESSION",
            startTimeMs = current.startTimeMs,
            endTimeMs = endTime,
            durationSeconds = durationSec,
            maxAdc = current.maxAdc,
            minAdc = current.minAdc,
            avgAdc = current.avgAdc,
            sampleCount = current.sampleCount,
            alertCount = current.alertCount,
        )

        viewModelScope.launch {
            sessionDao.insertSession(sessionToSave)
        }

        bleManager.sendCommand("STOP")
        _workoutStats.value = current.copy(isRecording = false)
    }

    private fun processNewReading(reading: EmgReading) {
        val current = _workoutStats.value
        val adc = reading.adcValue

        val newMax = if (current.sampleCount == 0) adc else maxOf(current.maxAdc, adc)
        val newMin = if (current.sampleCount == 0) adc else minOf(current.minAdc, adc)
        val newCount = current.sampleCount + 1
        totalSum += adc
        val newAvg = (totalSum / newCount).toInt()

        samplesInLastSecond++
        val now = System.currentTimeMillis()
        var newHz = current.sampleRateHz
        if (now - lastHzCalcTime >= 1000) {
            newHz = samplesInLastSecond
            samplesInLastSecond = 0
            lastHzCalcTime = now
        }

        var newAlertCount = current.alertCount
        val threshold = _alertThreshold.value

        if (adc >= threshold) {
            newAlertCount++
            val msg = "¡Atención! Esfuerzo elevado detectado: $adc ADC (Umbral: $threshold)"
            _activeAlert.value = msg

            if (current.isRecording) {
                viewModelScope.launch {
                    alertDao.insertAlert(
                        AlertEvent(
                            timestampMs = reading.timestampMs,
                            adcValue = adc,
                            thresholdValue = threshold,
                            message = msg,
                        )
                    )
                }
            }
        }

        _workoutStats.value = current.copy(
            currentAdc = adc,
            maxAdc = newMax,
            minAdc = newMin,
            avgAdc = newAvg,
            sampleCount = newCount,
            sampleRateHz = newHz,
            alertCount = newAlertCount,
        )

        val points = _chartPoints.value.toMutableList()
        points.add(reading)
        if (points.size > 150) {
            points.removeAt(0)
        }
        _chartPoints.value = points
    }
}
