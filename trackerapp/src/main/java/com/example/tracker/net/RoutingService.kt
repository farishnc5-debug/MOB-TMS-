package com.example.tracker.net

import com.example.tracker.util.LatLng
import java.util.Locale

data class RouteResult(
    val distanceMeters: Double,
    val durationSeconds: Double,
    val points: List<LatLng>,
)

data class GeocodeResult(val label: String, val lat: Double, val lng: Double)

/** Thin wrapper around the OSRM/Nominatim APIs returning simple domain results, or null/empty
 * on failure - mirrors web/js/utils.js so both clients behave the same way. */
object RoutingService {

    suspend fun getRoute(from: LatLng, to: LatLng): RouteResult? = try {
        val coords = String.format(
            Locale.US, "%f,%f;%f,%f", from.lng, from.lat, to.lng, to.lat
        )
        val response = NetworkModule.osrmApi.getRoute(coords)
        val route = response.routes?.firstOrNull() ?: return null
        RouteResult(
            distanceMeters = route.distance,
            durationSeconds = route.duration,
            points = route.geometry.coordinates.map { LatLng(lat = it[1], lng = it[0]) },
        )
    } catch (e: Exception) {
        null
    }

    suspend fun geocode(query: String): List<GeocodeResult> {
        if (query.trim().length < 3) return emptyList()
        return try {
            NetworkModule.nominatimApi.search(query).mapNotNull { r ->
                val lat = r.lat?.toDoubleOrNull()
                val lng = r.lon?.toDoubleOrNull()
                if (lat == null || lng == null || r.displayName == null) null
                else GeocodeResult(r.displayName, lat, lng)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun reverseGeocode(point: LatLng): String? = try {
        NetworkModule.nominatimApi.reverse(point.lat, point.lng).displayName
    } catch (e: Exception) {
        null
    }
}
