package com.example.geofence

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.model.LocationAlarm
import com.example.data.model.TriggerType
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceManager(private val context: Context) {
    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_GEOFENCE_EVENT
        }
        PendingIntent.getBroadcast(
            context,
            GEOFENCE_PENDING_INTENT_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun syncGeofences(activeAlarms: List<LocationAlarm>, onResult: ((Boolean, String?) -> Unit)? = null) {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Cannot register geofences: Location permission not granted")
            onResult?.invoke(false, "Location permission not granted")
            return
        }

        // If no active alarms, remove all geofences to save battery
        if (activeAlarms.isEmpty()) {
            geofencingClient.removeGeofences(geofencePendingIntent)
                .addOnSuccessListener {
                    Log.d(TAG, "Removed all geofences because 0 alarms are active (saving battery)")
                    onResult?.invoke(true, null)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to remove geofences: ${e.message}")
                    onResult?.invoke(false, e.message)
                }
            return
        }

        val geofenceList = activeAlarms.map { alarm ->
            val transitionType = when (alarm.triggerType) {
                TriggerType.ENTER -> Geofence.GEOFENCE_TRANSITION_ENTER
                TriggerType.EXIT -> Geofence.GEOFENCE_TRANSITION_EXIT
            }

            Geofence.Builder()
                .setRequestId(alarm.id.toString())
                .setCircularRegion(
                    alarm.latitude,
                    alarm.longitude,
                    alarm.radiusMeters.toFloat()
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(transitionType)
                .setNotificationResponsiveness(5000) // 5 seconds responsiveness
                .build()
        }

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceList)
            .build()

        // First remove existing to avoid duplicate triggers, then add updated list
        geofencingClient.removeGeofences(geofencePendingIntent).addOnCompleteListener {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully registered ${geofenceList.size} geofences")
                    onResult?.invoke(true, null)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to add geofences: ${e.message}")
                    onResult?.invoke(false, e.message)
                }
        }
    }

    companion object {
        private const val TAG = "GeofenceManager"
        const val ACTION_GEOFENCE_EVENT = "com.example.nearalarm.ACTION_GEOFENCE_EVENT"
        private const val GEOFENCE_PENDING_INTENT_ID = 9001
    }
}
