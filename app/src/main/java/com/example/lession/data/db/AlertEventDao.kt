package com.example.lession.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEvent)

    @Query("SELECT * FROM alert_events ORDER BY timestampMs DESC")
    fun getAllAlerts(): Flow<List<AlertEvent>>

    @Query("SELECT * FROM alert_events WHERE sessionId = :sessionId ORDER BY timestampMs ASC")
    fun getAlertsForSession(sessionId: Long): Flow<List<AlertEvent>>
}
