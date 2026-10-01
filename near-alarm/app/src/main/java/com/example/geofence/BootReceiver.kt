package com.example.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            Log.d(TAG, "Device booted or app updated. Restoring active geofence alarms...")
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val activeAlarms = db.alarmDao().getActiveAlarmsList()
                    if (activeAlarms.isNotEmpty()) {
                        val geofenceManager = GeofenceManager(context)
                        geofenceManager.syncGeofences(activeAlarms)
                        Log.d(TAG, "Restored ${activeAlarms.size} active location alarms.")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to restore geofences after boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
