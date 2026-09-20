package com.example.tracker.net

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

/** OpenStreetMap Nominatim - free, no API key, rate-limited. See trackerapp/README.md. */
interface NominatimApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "jsonv2",
        @Query("limit") limit: Int = 5,
    ): List<NominatimResult>

    @GET("reverse")
    suspend fun reverse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "jsonv2",
    ): NominatimResult
}

@JsonClass(generateAdapter = true)
data class NominatimResult(
    @Json(name = "display_name") val displayName: String?,
    val lat: String?,
    val lon: String?,
)
