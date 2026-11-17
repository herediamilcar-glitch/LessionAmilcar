package com.example.lession.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alert_events")
data class AlertEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long = 0,
    val timestampMs: Long,
    val adcValue: Int,
    val thresholdValue: Int,
    val message: String,
)
