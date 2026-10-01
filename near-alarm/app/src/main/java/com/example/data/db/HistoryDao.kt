package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AlarmHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM alarm_history ORDER BY triggeredAt DESC")
    fun getAllHistory(): Flow<List<AlarmHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: AlarmHistory): Long

    @Query("DELETE FROM alarm_history")
    suspend fun deleteAllHistory()
}
