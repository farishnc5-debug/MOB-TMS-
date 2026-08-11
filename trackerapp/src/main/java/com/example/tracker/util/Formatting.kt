package com.example.tracker.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

fun formatDistance(meters: Double?): String {
    if (meters == null || meters.isNaN()) return "—"
    return if (meters < 1000) "${meters.roundToInt()} m"
    else String.format(Locale.US, "%.1f km", meters / 1000)
}

fun formatDuration(seconds: Double?): String {
    if (seconds == null || seconds.isNaN()) return "—"
    val mins = (seconds / 60).roundToInt()
    if (mins < 1) return "under 1 min"
    if (mins < 60) return "$mins min"
    val hrs = mins / 60
    val rem = mins % 60
    return if (rem == 0) "$hrs hr" else "$hrs hr $rem min"
}

fun formatEta(seconds: Double?): String {
    if (seconds == null || seconds.isNaN()) return "—"
    val arrival = Date(System.currentTimeMillis() + (seconds * 1000).toLong())
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(arrival)
}

fun timeAgo(timestamp: Timestamp?): String {
    if (timestamp == null) return "never"
    val secs = (System.currentTimeMillis() - timestamp.toDate().time) / 1000
    return when {
        secs < 10 -> "just now"
        secs < 60 -> "${secs}s ago"
        secs < 3600 -> "${secs / 60}m ago"
        else -> "${secs / 3600}h ago"
    }
}
