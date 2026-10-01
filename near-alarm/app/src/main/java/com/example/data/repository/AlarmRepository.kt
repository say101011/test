package com.example.data.repository

import com.example.data.db.AlarmDao
import com.example.data.db.HistoryDao
import com.example.data.model.AlarmHistory
import com.example.data.model.LocationAlarm
import com.example.data.pref.AppSettings
import kotlinx.coroutines.flow.Flow

class AlarmRepository(
    private val alarmDao: AlarmDao,
    private val historyDao: HistoryDao,
    val settings: AppSettings
) {
    val allAlarms: Flow<List<LocationAlarm>> = alarmDao.getAllAlarms()
    val activeAlarms: Flow<List<LocationAlarm>> = alarmDao.getActiveAlarms()
    val allHistory: Flow<List<AlarmHistory>> = historyDao.getAllHistory()

    suspend fun getActiveAlarmsList(): List<LocationAlarm> = alarmDao.getActiveAlarmsList()

    suspend fun getAlarmById(id: Long): LocationAlarm? = alarmDao.getAlarmById(id)

    suspend fun saveAlarm(alarm: LocationAlarm): Long {
        return if (alarm.id == 0L) {
            alarmDao.insertAlarm(alarm)
        } else {
            alarmDao.updateAlarm(alarm)
            alarm.id
        }
    }

    suspend fun toggleAlarm(id: Long, isEnabled: Boolean) {
        alarmDao.setAlarmEnabled(id, isEnabled)
    }

    suspend fun deleteAlarm(id: Long) {
        alarmDao.deleteAlarmById(id)
    }

    suspend fun recordHistory(history: AlarmHistory) {
        historyDao.insertHistory(history)
    }

    suspend fun clearHistory() {
        historyDao.deleteAllHistory()
    }

    suspend fun deleteAllAlarms() {
        alarmDao.deleteAllAlarms()
    }

    suspend fun deleteAllAppData() {
        alarmDao.deleteAllAlarms()
        historyDao.deleteAllHistory()
        settings.clearAll()
    }
}
