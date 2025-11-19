package com.example.weathermvvm

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathermvvm.helpers.NetworkHelper
import com.example.weathermvvm.models.LocationBase
import com.example.weathermvvm.models.LocationData
import com.example.weathermvvm.models.WeatherData
import com.example.weathermvvm.models.WeatherResponse
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

open class DefaultWeatherViewModel(application: Application) : AndroidViewModel(application),
    WeatherViewModel {

    private val _weatherData = MutableStateFlow<PersistentList<WeatherData>>(persistentListOf())
    override val weatherData: StateFlow<ImmutableList<WeatherData>> = _weatherData

    var permissionGranted by mutableStateOf(false)
    var locationUpdatesRunning by mutableStateOf(false)

    fun onPermissionResult(isGranted: Boolean) {
        permissionGranted = isGranted
    }

    override fun fetchWeatherForLocation(
        location: LocationData,
        forceReload: Boolean,
    ) {
        val context = getApplication<Application>().applicationContext
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
                }.onFailure {
                    TODO("Handle error stuff")
                }
                println("Fetching weather data for $url")
            }
        } else {
            handleWeatherData(WeatherData(location.basedOn, cacheFile))
        }
    }

    fun handleWeatherData(updated: WeatherData) {
        val currentList = _weatherData.value

        val index = currentList.indexOfFirst {
            if (updated.base == LocationBase.LAT_LONG) {
                it.base == LocationBase.LAT_LONG
            } else {
                it.weatherResponse.location.name == updated.weatherResponse.location.name
            }
        }

        val newList = if (index >= 0) {
            currentList.set(index, updated)
        } else {
            currentList.add(updated)        }

        _weatherData.value = newList

        println("Weather data updated. Count: ${newList.size}")
    }
}

interface WeatherViewModel {
    val weatherData: StateFlow<ImmutableList<WeatherData>>
    fun fetchWeatherForLocation(
        location: LocationData,
        forceReload: Boolean = false,
    )
}



