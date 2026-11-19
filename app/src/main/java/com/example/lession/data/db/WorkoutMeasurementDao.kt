package com.example.lession.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutMeasurementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurements(measurements: List<WorkoutMeasurement>)

    @Query("SELECT * FROM workout_measurements WHERE sessionId = :sessionId ORDER BY timestampMs ASC")
    fun getMeasurementsForSession(sessionId: Long): Flow<List<WorkoutMeasurement>>

    @Query("SELECT * FROM workout_measurements WHERE sessionId = :sessionId ORDER BY timestampMs ASC")
    suspend fun getMeasurementsForSessionOnce(sessionId: Long): List<WorkoutMeasurement>
}
