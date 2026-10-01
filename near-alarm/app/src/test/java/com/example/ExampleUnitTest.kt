package com.example

import com.example.data.model.DistanceUnit
import com.example.map.MapHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testHaversineDistance() {
        // Distance between Seoul City Hall (37.5665, 126.9780) and Gwanghwamun (37.5759, 126.9768) is ~1 km
        val distance = MapHelper.calculateDistanceMeters(
            37.5665, 126.9780,
            37.5759, 126.9768
        )
        assertTrue(distance > 900 && distance < 1200)
    }

    @Test
    fun testFormatDistanceMetric() {
        val formattedMeters = MapHelper.formatDistance(350.0, DistanceUnit.METRIC)
        assertEquals("350 m away", formattedMeters)

        val formattedKm = MapHelper.formatDistance(2400.0, DistanceUnit.METRIC)
        assertEquals("2.4 km away", formattedKm)
    }

    @Test
    fun testFormatDistanceImperial() {
        val formattedFeet = MapHelper.formatDistance(100.0, DistanceUnit.IMPERIAL)
        assertEquals("328 ft away", formattedFeet)
    }
}
