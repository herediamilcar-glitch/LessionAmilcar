package com.example.lession.ble

data class EmgReading(
    val timestampMs: Long,
    val adcValue: Int,
    val receivedAtMs: Long = System.currentTimeMillis(),
)
