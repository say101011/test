package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TriggerType {
    ENTER,
    EXIT
}

enum class AlertType {
    NOTIFICATION,
    NOTIFICATION_VIBRATION,
    ALARM_SOUND
}

enum class RepeatType {
    ONCE,
    EVERY_TIME
}

@Entity(tableName = "location_alarms")
data class LocationAlarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int,
    val triggerType: TriggerType = TriggerType.ENTER,
    val alertType: AlertType = AlertType.NOTIFICATION_VIBRATION,
    val repeatType: RepeatType = RepeatType.EVERY_TIME,
    val memo: String = "",
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
