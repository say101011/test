package com.example.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.AlertType
import com.example.data.model.LocationAlarm
import com.example.data.model.TriggerType
import com.example.map.MapHelper

object NotificationHelper {
    const val CHANNEL_PROXIMITY_ID = "channel_proximity_alerts"
    const val CHANNEL_SOUND_ID = "channel_sound_alerts"

    const val ACTION_DISMISS = "com.example.nearalarm.ACTION_DISMISS"
    const val ACTION_SNOOZE = "com.example.nearalarm.ACTION_SNOOZE"
    const val EXTRA_NOTIFICATION_ID = "extra_notif_id"
    const val EXTRA_ALARM_ID = "extra_alarm_id"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Standard proximity channel with vibration
            val proximityChannel = NotificationChannel(
                CHANNEL_PROXIMITY_ID,
                "Proximity Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies you when approaching or leaving your saved location"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
            }

            // High Priority Sound channel with alarm audio
            val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val soundChannel = NotificationChannel(
                CHANNEL_SOUND_ID,
                "High Priority Sound Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Plays high volume sound when reaching destination"
                setSound(alarmSoundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 600, 300, 600, 300, 600)
            }

            notificationManager.createNotificationChannel(proximityChannel)
            notificationManager.createNotificationChannel(soundChannel)
        }
    }

    fun showProximityNotification(
        context: Context,
        alarm: LocationAlarm,
        triggerType: TriggerType
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notifId = (alarm.id and 0x7FFFFFFF).toInt()

        // Choose channel based on alertType
        val channelId = if (alarm.alertType == AlertType.ALARM_SOUND) {
            CHANNEL_SOUND_ID
        } else {
            CHANNEL_PROXIMITY_ID
        }

        // Tap content -> Open Map in MainActivity
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_ALARM_ID", alarm.id)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss action
        val dismissIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_DISMISS
            putExtra(EXTRA_NOTIFICATION_ID, notifId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notifId * 10 + 1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_NOTIFICATION_ID, notifId)
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notifId * 10 + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (triggerType == TriggerType.ENTER) {
            "You're near your destination"
        } else {
            "You left your location"
        }

        val distanceText = "${alarm.radiusMeters} m"
        val body = if (triggerType == TriggerType.ENTER) {
            "You're within $distanceText of \"${alarm.title}\"${if (alarm.memo.isNotBlank()) " — ${alarm.memo}" else ""}."
        } else {
            "You have moved away from \"${alarm.title}\"."
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze (10m)", snoozePendingIntent)

        if (alarm.alertType == AlertType.NOTIFICATION_VIBRATION || alarm.alertType == AlertType.ALARM_SOUND) {
            builder.setVibrate(longArrayOf(0, 500, 250, 500))
        }

        notificationManager.notify(notifId, builder.build())
    }
}
