package com.example.tracker.net

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Public OSRM demo server - free, no API key, rate-limited. See trackerapp/README.md. */
interface OsrmApi {
    @GET("route/v1/driving/{coordinates}")
    suspend fun getRoute(
        @Path("coordinates", encoded = true) coordinates: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson",
    ): OsrmResponse
}

@JsonClass(generateAdapter = true)
data class OsrmResponse(val routes: List<OsrmRoute>?)

@JsonClass(generateAdapter = true)
data class OsrmRoute(
    val distance: Double,
    val duration: Double,
    val geometry: OsrmGeometry,
)

@JsonClass(generateAdapter = true)
data class OsrmGeometry(
    @Json(name = "coordinates") val coordinates: List<List<Double>>,
)
