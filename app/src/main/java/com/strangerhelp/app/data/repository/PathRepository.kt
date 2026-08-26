package com.strangerhelp.app.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.strangerhelp.app.data.api.StrangerHelpApi
import com.strangerhelp.app.data.model.PathResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class PathRepository(
    private val api: StrangerHelpApi
) {
    suspend fun setPath(
        fromLocation: String,
        fromLat: Double,
        fromLng: Double,
        toLocation: String,
        toLat: Double,
        toLng: Double,
        radiusKm: Double,
        recurring: Boolean
    ): Response<JsonObject> {
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
        val json = Gson().toJson(body)
        return api.setPath(json.toRequestBody("application/json".toMediaType()))
    }

    suspend fun getPath(): Response<PathResponse> {
        return api.getPath()
    }

    suspend fun clearPath(): Response<JsonObject> {
        return api.clearPath()
    }
}
