package com.example.map

/**
 * Clean architectural abstraction for Map and Tile Providers.
 * Easily switchable to any custom tile server (MapLibre style, Stadia, Thunderforest, self-hosted OSM tiles)
 * without touching UI code.
 */
object MapConfig {
    const val TILE_SOURCE_NAME = "Mapnik"
    const val TILE_SERVER_BASE_URL = "https://tile.openstreetmap.org/"
    const val DEFAULT_USER_AGENT = "NearAlarm-AndroidApp/1.0"
    const val ATTRIBUTION_TEXT = "© OpenStreetMap contributors"
    const val ATTRIBUTION_URL = "https://www.openstreetmap.org/copyright"

    // Default map camera start point (Seoul / Global fallback)
    const val DEFAULT_LATITUDE = 37.5665
    const val DEFAULT_LONGITUDE = 126.9780
    const val DEFAULT_ZOOM = 15.0
    const val MIN_ZOOM = 3.0
    const val MAX_ZOOM = 20.0
}
