package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LocationAlarm
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM location_alarms ORDER BY createdAt DESC")
    fun getAllAlarms(): Flow<List<LocationAlarm>>

    @Query("SELECT * FROM location_alarms WHERE isEnabled = 1 ORDER BY createdAt DESC")
    fun getActiveAlarms(): Flow<List<LocationAlarm>>

    @Query("SELECT * FROM location_alarms WHERE isEnabled = 1")
    suspend fun getActiveAlarmsList(): List<LocationAlarm>

    @Query("SELECT * FROM location_alarms WHERE id = :id LIMIT 1")
    suspend fun getAlarmById(id: Long): LocationAlarm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: LocationAlarm): Long

    @Update
    suspend fun updateAlarm(alarm: LocationAlarm)

    @Query("UPDATE location_alarms SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setAlarmEnabled(id: Long, isEnabled: Boolean)

    @Query("DELETE FROM location_alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: Long)

    @Query("DELETE FROM location_alarms")
    suspend fun deleteAllAlarms()
}
