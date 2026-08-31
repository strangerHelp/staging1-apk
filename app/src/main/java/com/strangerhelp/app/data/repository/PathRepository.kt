package com.strangerhelp.app.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.strangerhelp.app.data.api.StrangerHelpApi
import com.strangerhelp.app.data.model.PathResponse
import com.strangerhelp.app.data.model.PlaceResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class PathRepository(
    private val api: StrangerHelpApi
) {
    private val okHttpClient = OkHttpClient()
    private val gson = Gson()

    suspend fun setPath(
        fromLocation: String,
        fromLat: Double,
        fromLng: Double,
        toLocation: String,
        toLat: Double,
        toLng: Double,
        radiusKm: Double,
        recurring: Boolean
    ): Response<com.strangerhelp.app.data.model.PathSetResponse> {
        val body = mapOf(
            "from_location" to fromLocation,
            "from_lat" to fromLat,
            "from_lng" to fromLng,
            "to_location" to toLocation,
            "to_lat" to toLat,
            "to_lng" to toLng,
            "radius_km" to radiusKm,
            "recurring" to recurring
        )
        val json = gson.toJson(body)
        
        // Wait, StrangerHelpApi has setPath returning JsonObject. Let me fix the api first.
        // Actually, I can just use Retrofit and update StrangerHelpApi
        return api.setPath(body)
    }

    suspend fun getPath(): Response<PathResponse> {
        return api.getPath()
    }

    suspend fun deactivatePath(): Response<JsonObject> {
        return api.clearPath()
    }

    suspend fun searchPlaces(query: String): List<PlaceResult> = withContext(Dispatchers.IO) {
        try {
            val url = "https://nominatim.openstreetmap.org/search?q=${android.net.Uri.encode(query)}&format=json&limit=5&countrycodes=in"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "StrangerHelpApp/1.0")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val listType = object : TypeToken<List<PlaceResult>>() {}.type
                    return@withContext gson.fromJson(responseBody, listType)
                }
            }
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
