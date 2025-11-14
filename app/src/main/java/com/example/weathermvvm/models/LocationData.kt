package com.example.weathermvvm.models

import kotlinx.serialization.Serializable

@Serializable
data class LocationData(
    var basedOn: LocationBase = LocationBase.LAT_LONG,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var city: String = "",

    ) {
    /**
     * Returns the appropriate string for a weather API query,
     * based on whether the location is city-based or coordinate-based.
     */
    val apiQuery: String
        get() = if (basedOn == LocationBase.CITY && city.isNotBlank()) {
            city
        } else {
            "$latitude,$longitude"
        }
}
enum class LocationBase {
    CITY,
    LAT_LONG
}