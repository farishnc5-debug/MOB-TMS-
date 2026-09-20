package com.example.tracker.util

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LatLng(val lat: Double, val lng: Double)

/** Straight-line distance in meters - used to decide whether movement is worth a route refetch. */
fun haversineMeters(a: LatLng, b: LatLng): Double {
    val r = 6_371_000.0
    val dLat = Math.toRadians(b.lat - a.lat)
    val dLng = Math.toRadians(b.lng - a.lng)
    val sinDLat = sin(dLat / 2)
    val sinDLng = sin(dLng / 2)
    val h = sinDLat * sinDLat +
        cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) * sinDLng * sinDLng
    return 2 * r * asin(sqrt(h))
}
