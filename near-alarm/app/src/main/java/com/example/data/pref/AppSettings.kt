package com.example.data.pref

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.DistanceUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("near_alarm_prefs", Context.MODE_PRIVATE)

    private val _distanceUnit = MutableStateFlow(getSavedDistanceUnit())
    val distanceUnit: StateFlow<DistanceUnit> = _distanceUnit.asStateFlow()

    private val _defaultRadius = MutableStateFlow(prefs.getInt(KEY_DEFAULT_RADIUS, 300))
    val defaultRadius: StateFlow<Int> = _defaultRadius.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND_ENABLED, true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION_ENABLED, true))
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    fun setDistanceUnit(unit: DistanceUnit) {
        prefs.edit().putString(KEY_DISTANCE_UNIT, unit.name).apply()
        _distanceUnit.value = unit
    }

    private fun getSavedDistanceUnit(): DistanceUnit {
        val name = prefs.getString(KEY_DISTANCE_UNIT, DistanceUnit.AUTOMATIC.name)
        return try {
            DistanceUnit.valueOf(name ?: DistanceUnit.AUTOMATIC.name)
        } catch (_: Exception) {
            DistanceUnit.AUTOMATIC
        }
    }

    fun setDefaultRadius(radiusMeters: Int) {
        prefs.edit().putInt(KEY_DEFAULT_RADIUS, radiusMeters).apply()
        _defaultRadius.value = radiusMeters
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
        _soundEnabled.value = enabled
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
        _vibrationEnabled.value = enabled
    }

    fun isBgDisclosureSeen(): Boolean {
        return prefs.getBoolean(KEY_BG_DISCLOSURE_SEEN, false)
    }

    fun setBgDisclosureSeen(seen: Boolean) {
        prefs.edit().putBoolean(KEY_BG_DISCLOSURE_SEEN, seen).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
        _distanceUnit.value = DistanceUnit.AUTOMATIC
        _defaultRadius.value = 300
        _soundEnabled.value = true
        _vibrationEnabled.value = true
    }

    companion object {
        private const val KEY_DISTANCE_UNIT = "distance_unit"
        private const val KEY_DEFAULT_RADIUS = "default_radius"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_BG_DISCLOSURE_SEEN = "bg_disclosure_seen"
    }
}
