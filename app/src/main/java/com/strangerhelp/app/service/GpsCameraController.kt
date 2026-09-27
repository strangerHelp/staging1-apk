package com.strangerhelp.app.service

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

class GpsCameraController(private val context: Context) {

    private val fused = LocationServices.getFusedLocationProviderClient(context)

    suspend fun getCurrentLocation(): Location? = withContext(Dispatchers.IO) {
        withTimeoutOrNull(3000L) {
            suspendCancellableCoroutine { cont ->
                try {
                    // Try lastLocation first
                    fused.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null && cont.isActive) {
                            cont.resume(lastLoc)
                        } else {
                            // Request current location
                            fused.getCurrentLocation(
                                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                                null
                            ).addOnSuccessListener { loc ->
                                if (cont.isActive) cont.resume(loc)
                            }.addOnFailureListener { err ->
                                Log.e(TAG, "getCurrentLocation failed", err)
                                if (cont.isActive) cont.resume(null)
                            }
                        }
                    }.addOnFailureListener {
                        // On failure, request current location directly
                        fused.getCurrentLocation(
                            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                            null
                        ).addOnSuccessListener { loc ->
                            if (cont.isActive) cont.resume(loc)
                        }.addOnFailureListener { err ->
                            Log.e(TAG, "getCurrentLocation failed", err)
                            if (cont.isActive) cont.resume(null)
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException fetching location", e)
                    if (cont.isActive) cont.resume(null)
                } catch (e: Exception) {
                    Log.e(TAG, "Exception fetching location", e)
                    if (cont.isActive) cont.resume(null)
                }
            }
        }
    }

    suspend fun reverseGeocode(lat: Double, lng: Double): String =
        withContext(Dispatchers.IO) {
            val result = withTimeoutOrNull(2500L) {
                try {
                    @Suppress("DEPRECATION")
                    val addresses = Geocoder(context, Locale.getDefault())
                        .getFromLocation(lat, lng, 1)
                    val a = addresses?.firstOrNull()
                    if (a != null) {
                        listOfNotNull(a.locality, a.subAdminArea, a.adminArea)
                            .joinToString(", ")
                            .ifBlank { "%.6f, %.6f".format(lat, lng) }
                    } else "%.6f, %.6f".format(lat, lng)
                } catch (e: Exception) {
                    Log.e(TAG, "Reverse geocode failed", e)
                    "%.6f, %.6f".format(lat, lng)
                }
            }
            result ?: "%.6f, %.6f".format(lat, lng)
        }

    companion object { private const val TAG = "GpsCameraController" }
}
