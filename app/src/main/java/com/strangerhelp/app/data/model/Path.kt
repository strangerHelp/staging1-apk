package com.strangerhelp.app.data.model

import com.google.gson.annotations.SerializedName

data class Path(
    val id: String,
    @SerializedName("from_location") val fromLocation: String,
    @SerializedName("from_lat") val fromLat: Double,
    @SerializedName("from_lng") val fromLng: Double,
    @SerializedName("to_location") val toLocation: String,
    @SerializedName("to_lat") val toLat: Double,
    @SerializedName("to_lng") val toLng: Double,
    @SerializedName("radius_km") val radiusKm: Double,
    val recurring: Int,
    val active: Int,
    @SerializedName("expires_at") val expiresAt: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class PathTask(
    @SerializedName("_id") val id: String,
    val title: String,
    val category: String,
    val budget: Int,
    val location: String,
    val lat: Double,
    val lng: Double,
    val urgent: Int,
    val status: String,
    @SerializedName("poster_name") val posterName: String?,
    val offPath: Double,        // km off the route
    val distFromStart: Double   // km along the route
)

data class PathResponse(
    val path: Path?,
    val tasks: List<PathTask>
)

data class PathSetResponse(
    val ok: Boolean,
    val id: String?,
    @SerializedName("matchedTasks") val matchedTasks: Int?
)

data class PlaceResult(
    val display_name: String,
    val lat: String,
    val lon: String
)
