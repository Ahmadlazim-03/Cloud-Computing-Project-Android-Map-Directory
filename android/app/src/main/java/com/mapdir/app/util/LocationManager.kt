package com.mapdir.app.util

import android.content.Context
import android.location.Location
import android.location.LocationManager as AndroidLocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import android.Manifest
import kotlin.coroutines.resume

/**
 * Manages location retrieval using FusedLocationProviderClient.
 * Handles permissions check and GPS availability detection.
 */
class LocationManager(context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val context = context

    /**
     * Check if location permissions are granted.
     */
    fun hasLocationPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    /**
     * Check if GPS is enabled on device.
     */
    fun isGpsEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
        return locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER)
    }

    /**
     * Get last known location asynchronously.
     * Returns null if permissions not granted or no location available.
     */
    suspend fun getLastKnownLocation(): Location? = withContext(Dispatchers.Default) {
        if (!hasLocationPermissions()) return@withContext null

        suspendCancellableCoroutine { continuation ->
            try {
                @Suppress("MissingPermission")
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    continuation.resume(location)
                }.addOnFailureListener {
                    continuation.resume(null)
                }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }

    /**
     * Get current location with high accuracy.
     * Returns null if permissions not granted.
     */
    suspend fun getCurrentLocation(): Location? = withContext(Dispatchers.Default) {
        if (!hasLocationPermissions()) return@withContext null

        suspendCancellableCoroutine { continuation ->
            try {
                @Suppress("MissingPermission")
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { location ->
                    continuation.resume(location)
                }.addOnFailureListener {
                    continuation.resume(null)
                }
            } catch (e: Exception) {
                continuation.resume(null)
            }
        }
    }
}
