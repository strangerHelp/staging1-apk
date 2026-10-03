package com.strangerhelp.app.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * One-shot location fetcher for the task feed.
 * Does NOT start a foreground service.
 */
class LocationHelper(private val context: Context) {

    private val fused = LocationServices.getFusedLocationProviderClient(context)

    fun hasPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        if (!hasPermission()) return null
        return suspendCancellableCoroutine { cont ->
            try {
                fused.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    null
                ).addOnSuccessListener { loc ->
                    if (cont.isActive) cont.resume(loc)
                }.addOnFailureListener { e ->
                    Log.w(TAG, "Location fetch failed", e)
                    if (cont.isActive) cont.resume(null)
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException", e)
                if (cont.isActive) cont.resume(null)
            }
        }
    }

    /** Fallback when GPS is off: use last known location. */
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): Location? {
        if (!hasPermission()) return null
        return suspendCancellableCoroutine { cont ->
            fused.lastLocation
                .addOnSuccessListener { if (cont.isActive) cont.resume(it) }
                .addOnFailureListener { if (cont.isActive) cont.resume(null) }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(callback: (Location) -> Unit) {
        if (!hasPermission()) return
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30000)
            .setMinUpdateIntervalMillis(15000)
            .build()
        
        fused.requestLocationUpdates(request, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { callback(it) }
            }
        }, Looper.getMainLooper())
    }

    companion object { private const val TAG = "LocationHelper" }
}
