package com.example.map

import com.example.data.model.DistanceUnit
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MapHelper {
    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Internal Haversine distance calculation in meters without external server calls.
     */
    fun calculateDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    private fun isImperialLocale(): Boolean {
        val country = Locale.getDefault().country.uppercase(Locale.ROOT)
        return country == "US" || country == "GB" || country == "LR" || country == "MM"
    }

    /**
     * Formats distance according to user preference (Automatic, Metric, Imperial).
     */
    fun formatDistance(
        distanceMeters: Double,
        unitPreference: DistanceUnit,
        withAwaySuffix: Boolean = true
    ): String {
        val useImperial = when (unitPreference) {
            DistanceUnit.IMPERIAL -> true
            DistanceUnit.METRIC -> false
            DistanceUnit.AUTOMATIC -> isImperialLocale()
        }

        val suffix = if (withAwaySuffix) " away" else ""

        return if (!useImperial) {
            // Metric
            if (distanceMeters < 1000) {
                "${distanceMeters.toInt()} m$suffix"
            } else {
                val km = distanceMeters / 1000.0
                String.format(Locale.getDefault(), "%.1f km%s", km, suffix)
            }
        } else {
            // Imperial (1 meter = 3.28084 feet, 1 mile = 1609.344 meters)
            val feet = distanceMeters * 3.28084
            if (feet < 1000) {
                "${feet.toInt()} ft$suffix"
            } else {
                val miles = distanceMeters / 1609.344
                String.format(Locale.getDefault(), "%.1f mi%s", miles, suffix)
            }
        }
    }

    fun formatRadius(radiusMeters: Int, unitPreference: DistanceUnit): String {
        return formatDistance(radiusMeters.toDouble(), unitPreference, withAwaySuffix = false)
    }
}
