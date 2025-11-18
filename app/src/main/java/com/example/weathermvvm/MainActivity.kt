package com.example.weathermvvm

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.compose.animation.core.copy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.SampleData
import com.example.weathermvvm.helpers.NetworkHelper
import com.example.weathermvvm.models.LocationData
import com.example.weathermvvm.ui.theme.WeatherMVVMTheme
import com.example.weathermvvm.helpers.PermissionHelper
import com.example.weathermvvm.helpers.PermissionState
import com.example.weathermvvm.models.Condition
import com.example.weathermvvm.models.Current
import com.example.weathermvvm.models.Forecast
import com.example.weathermvvm.models.Location
import com.example.weathermvvm.models.LocationBase
import com.example.weathermvvm.models.WeatherData
import com.example.weathermvvm.models.WeatherResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<WeatherViewModel>()
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private var requestedPermissions by mutableStateOf(false)
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkHelper.clearCache(this)
        enableEdgeToEdge()
        setContent {
            WeatherMVVMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherView(
                        viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onResume() {
        super.onResume()
        if (!requestedPermissions) {
            checkAndRequestLocationPermission()
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.locationUpdatesRunning) {
            viewModel.locationUpdatesRunning = false
            stopLocationUpdates()
        }
    }

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val isGranted = permissions.any { it.value }
            viewModel.onPermissionResult(isGranted)

            if (!isGranted) {
                handlePermissionDenied()
            }
        }

    private fun handlePermissionDenied() {
        val permanentlyDenied = permissions.all {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, it)
        }

        if (permanentlyDenied) {
            AlertDialog.Builder(this)
                .setTitle("Permission permanently denied")
                .setPositiveButton("Open Settings") { _, _ ->
                    requestedPermissions = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", packageName, null)
                    startActivity(intent)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    requestedPermissions = false
                }
                .show()
        } else {
            viewModel.onPermissionResult(false)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun checkAndRequestLocationPermission() {
        requestedPermissions = true
        val permission = PermissionHelper.checkAndRequestLocationPermission(this)
        when (permission) {
            PermissionState.GRANTED -> {
                requestedPermissions = false
                viewModel.onPermissionResult(true)
                println("Should work!")
                setupLocationUpdates(this)
                startLocationUpdates()
            }

            else -> {
                showRationaleDialog()
            }
        }
    }

    fun setupLocationUpdates(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    viewModel.fetchWeatherForLocation(
                        LocationData(
                            latitude = location.latitude,
                            longitude = location.longitude,
                        ), context
                        ,true
                    )
                    println("Location: ${location.latitude}, ${location.longitude}")
                }
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, Constants.LOCATION_INTERVAL
        ).setMinUpdateIntervalMillis(2000L).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        ).addOnSuccessListener {
            viewModel.locationUpdatesRunning = true
        }.addOnFailureListener {
            viewModel.locationUpdatesRunning = false
        }
    }

    private fun showRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location permission required")
            .setMessage("This app needs location access to function properly.")
            .setPositiveButton("Grant") { _, _ ->
                requestedPermissions = true
                locationPermissionRequest.launch(permissions)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                requestedPermissions = false
                dialog.dismiss()
            }
            .show()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}

@Preview(showBackground = true) // Use showBackground = true for better visibility
@Composable
private fun WeatherViewPreview() {

    val fakeLondonData = WeatherData(
        base = LocationBase.CITY,
        weatherResponse = SampleData.sampleWeatherResponse
    )
    val dummyViewModel = FakeWeatherViewModel(initialData = listOf(fakeLondonData)) // Or use a fake Application if needed

    WeatherMVVMTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            WeatherView(
                viewModel = dummyViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

class FakeWeatherViewModel(
    // It starts with a predefined list of weather data
    initialData: List<WeatherData> = listOf()
) : IWeatherViewModel {
    // It just holds the data and does nothing else.
    override val weatherData: List<WeatherData> = initialData
    override fun fetchWeatherForLocation(
        location: LocationData,
        context: Context,
        forceReload: Boolean
    ) {
        TODO("Not yet implemented")
    }
}