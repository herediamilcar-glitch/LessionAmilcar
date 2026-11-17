package com.example.lession.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationSeconds: Long,
    val maxAdc: Int,
    val minAdc: Int,
    val avgAdc: Int,
    val sampleCount: Int,
    val alertCount: Int,
)
