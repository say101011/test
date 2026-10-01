package com.example.geofence

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.example.data.db.AppDatabase
import com.example.data.model.TriggerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notifId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, -1)
        val alarmId = intent.getLongExtra(NotificationHelper.EXTRA_ALARM_ID, -1L)

        when (intent.action) {
            NotificationHelper.ACTION_DISMISS -> {
                if (notifId != -1) {
                    notificationManager.cancel(notifId)
                }
            }
            NotificationHelper.ACTION_SNOOZE -> {
                if (notifId != -1) {
                    notificationManager.cancel(notifId)
                }
                if (alarmId != -1L) {
                    scheduleSnoozeReminder(context, alarmId)
                }
            }
            ACTION_SNOOZE_FIRE -> {
                if (alarmId != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val alarm = AppDatabase.getInstance(context).alarmDao().getAlarmById(alarmId)
                        if (alarm != null && alarm.isEnabled) {
                            NotificationHelper.showProximityNotification(context, alarm, alarm.triggerType)
                        }
                    }
                }
            }
        }
    }

    private fun scheduleSnoozeReminder(context: Context, alarmId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE_FIRE
            putExtra(NotificationHelper.EXTRA_ALARM_ID, alarmId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId * 100 + 42).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // 10 minutes from now (600,000 ms)
        val triggerAt = SystemClock.elapsedRealtime() + 10 * 60 * 1000L
        try {
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerAt,
                pendingIntent
            )
        } catch (_: SecurityException) {
            // Fallback gracefully if exact alarm not granted
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME,
                triggerAt,
                pendingIntent
            )
        }
    }

    companion object {
        const val ACTION_SNOOZE_FIRE = "com.example.nearalarm.ACTION_SNOOZE_FIRE"
    }
}
