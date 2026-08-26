package com.strangerhelp.app.data.model

import com.google.gson.annotations.SerializedName

data class Path(
    @SerializedName("from_location") val fromLocation: String,
    @SerializedName("from_lat") val fromLat: Double,
    @SerializedName("from_lng") val fromLng: Double,
    @SerializedName("to_location") val toLocation: String,
    @SerializedName("to_lat") val toLat: Double,
    @SerializedName("to_lng") val toLng: Double,
    @SerializedName("radius_km") val radiusKm: Double,
    val recurring: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("expires_at") val expiresAt: String
)

data class PathTask(
    @SerializedName("_id") val id: String,
    val title: String,
    val location: String,
    val budget: Int,
    val category: String,
    @SerializedName("offPath") val offPath: Double,
    @SerializedName("distFromStart") val distFromStart: Double,
    val lat: Double,
    val lng: Double,
    val status: String
)

data class PathResponse(
    val path: Path?,
    val tasks: List<PathTask>
)
