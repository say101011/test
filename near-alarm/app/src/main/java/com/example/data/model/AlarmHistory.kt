package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_history")
data class AlarmHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val alarmId: Long,
    val alarmTitle: String,
    val triggerType: TriggerType,
    val triggeredAt: Long = System.currentTimeMillis(),
    val distanceMeters: Double = 0.0
)
