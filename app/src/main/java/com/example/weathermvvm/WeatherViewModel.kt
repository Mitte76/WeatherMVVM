package com.example.weathermvvm

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathermvvm.helpers.NetworkHelper
import com.example.weathermvvm.models.LocationData
import com.example.weathermvvm.models.LocationBase
import com.example.weathermvvm.models.WeatherData
import com.example.weathermvvm.models.WeatherResponse
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class WeatherViewModel : ViewModel() {

    private val _weatherData = mutableStateListOf<WeatherData>()
    val weatherData: List<WeatherData> = _weatherData
    var permissionGranted by mutableStateOf(false)
    var locationUpdatesRunning by mutableStateOf(false)

    fun onPermissionResult(isGranted: Boolean) {
        permissionGranted = isGranted
    }

    fun fetchWeatherForLocation(
        location: LocationData,
        context: Context,
        forceReload: Boolean = false,
    ) {

        val filename = if (location.basedOn == LocationBase.CITY) {
            "${location.city}.json"
        } else {
            "${location.latitude}_${location.longitude}.json"
        }

        val cacheFile = NetworkHelper.loadWeatherFromCache(context, filename)

        val shouldReload = forceReload || cacheFile == null

        val url =
            Constants.URL_FORECAST + "?key=" + BuildConfig.API_KEY + "&q=" + location.apiQuery + "&aqi=no&days=4"
        if (shouldReload) {
            viewModelScope.launch {
                val response = NetworkHelper.httpGet(url)
                response.onSuccess { data ->
                    println("Data fetch success")
                    NetworkHelper.writeFileToCache(context, filename, data)
                    val json = Json { ignoreUnknownKeys = true }
                    val weatherResponse = json.decodeFromString<WeatherResponse>(data)
                    handleWeatherData(WeatherData(location.basedOn, weatherResponse))
                }.onFailure { error ->
                    // Show an error message to the user
                }
                println("Fetching weather data for $url")

            }
        } else {
            println("using cached data!")
            handleWeatherData(WeatherData(location.basedOn, cacheFile))
        }
    }

    private fun handleWeatherData(updated: WeatherData) {
        val index = weatherData.indexOfFirst {
            if (updated.base == LocationBase.LAT_LONG) {
                it.base == LocationBase.LAT_LONG
            } else {
                it.weatherResponse.location.name == updated.weatherResponse.location.name
            }
        }
        if (index >= 0) {
            _weatherData[index] = updated
        } else {
            _weatherData.add(updated)
        }

        println("Weather data updated ${weatherData.size}")
    }
}

