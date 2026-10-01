package com.example.data.model

enum class DistanceUnit {
    AUTOMATIC,
    METRIC,
    IMPERIAL
}

data class SearchResultPlace(
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val type: String = ""
)
