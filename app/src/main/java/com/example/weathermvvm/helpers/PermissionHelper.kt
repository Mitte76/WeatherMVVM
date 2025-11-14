package com.example.weathermvvm.helpers

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.collections.any


object PermissionHelper {

    fun checkAndRequestLocationPermission(activity: Activity): PermissionState {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val isGranted = permissions.any {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
        if (isGranted) return PermissionState.GRANTED

        val shouldShowRationale = permissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
        if (shouldShowRationale) return PermissionState.SHOW_RATIONALE

        val wasRequestedBefore = permissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it) ||
                    ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!wasRequestedBefore) return PermissionState.REQUEST

        return PermissionState.PERMANENTLY_DENIED
    }

}

enum class PermissionState {
    GRANTED,
    SHOW_RATIONALE,
    REQUEST,
    PERMANENTLY_DENIED
}