package com.strangerhelp.app.data.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Headers

data class NominatimResult(
    val lat: String,
    val lon: String,
    val display_name: String
)

interface NominatimApi {
    @Headers("User-Agent: StrangerHelp/1.0")
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5,
        @Query("countrycodes") countries: String = "in"
    ): Response<List<NominatimResult>>
    
    @Headers("User-Agent: StrangerHelp/1.0")
    @GET("reverse")
    suspend fun reverse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json"
    ): Response<NominatimResult>
}

object NominatimClient {
    val api: NominatimApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApi::class.java)
    }
}
