package com.example.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.model.AlarmHistory
import com.example.data.model.RepeatType
import com.example.data.model.TriggerType
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "Geofencing error code: ${geofencingEvent.errorCode} - $errorMessage")
            return
        }

        val transition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return

        val triggerType = when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> TriggerType.ENTER
            Geofence.GEOFENCE_TRANSITION_EXIT -> TriggerType.EXIT
            else -> return
        }

        val database = AppDatabase.getInstance(context)
        val alarmDao = database.alarmDao()
        val historyDao = database.historyDao()
        val geofenceManager = GeofenceManager(context)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (geofence in triggeringGeofences) {
                    val alarmId = geofence.requestId.toLongOrNull() ?: continue
                    val alarm = alarmDao.getAlarmById(alarmId) ?: continue

                    if (!alarm.isEnabled) continue

                    // Show user-configured alert/notification
                    NotificationHelper.showProximityNotification(context, alarm, triggerType)

                    // Record history entry
                    historyDao.insertHistory(
                        AlarmHistory(
                            alarmId = alarm.id,
                            alarmTitle = alarm.title,
                            triggerType = triggerType,
                            triggeredAt = System.currentTimeMillis(),
                            distanceMeters = alarm.radiusMeters.toDouble()
                        )
                    )

                    // If configured as ONCE, disable alarm after trigger
                    if (alarm.repeatType == RepeatType.ONCE) {
                        alarmDao.setAlarmEnabled(alarm.id, false)
                        val remainingActive = alarmDao.getActiveAlarmsList()
                        geofenceManager.syncGeofences(remainingActive)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling geofence transition", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
    }
}
