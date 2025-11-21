package com.example.weathermvvm

import android.Manifest


object Constants {
    const val LOCATION_INTERVAL = (60 * 30 * 1000).toLong()
//    const val LOCATION_INTERVAL = (60 * 1 * 1000).toLong()
    const val BASE_URL = "https://api.weatherapi.com/v1/"
    const val URL_FORECAST = BASE_URL + "forecast.json"

    const val CACHEFILE_LIFETIME = (60 * 20 * 1000).toLong()
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    const val HOUR_CARD_HEIGHT = 124
    const val HOURS_ROW_HEIGHT = HOUR_CARD_HEIGHT + 16
    const val DAY_ITEM_HEIGHT = HOUR_CARD_HEIGHT + 76
}