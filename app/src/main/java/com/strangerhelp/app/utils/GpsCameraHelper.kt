package com.strangerhelp.app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume

class GpsCameraHelper(private val context: Context) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun requireLocation(): Location? {
        if (!hasLocationPermission()) {
            return null
        }

        val location = getCurrentLocation()
        if (location == null) {
            return null
        }

        return location
    }

    private suspend fun getCurrentLocation(): Location? {
        return suspendCancellableCoroutine { cont ->
            try {
                fusedClient.lastLocation
                    .addOnSuccessListener { location ->
                        cont.resume(location)
                    }
                    .addOnFailureListener {
                        cont.resume(null)
                    }
            } catch (e: SecurityException) {
                cont.resume(null)
            }
        }
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getPlaceName(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lng")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "StrangerHelp Android")

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            val json = JSONObject(response.toString())
            json.getString("display_name").split(",").take(3).joinToString(",").trim()
        } catch (e: Exception) {
            "${"%.6f".format(lat)}, ${"%.6f".format(lng)}"
        }
    }
}
