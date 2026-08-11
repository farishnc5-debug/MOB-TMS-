package com.example.tracker.util

import com.example.tracker.BuildConfig

fun buildTrackingUrl(sessionId: String): String =
    "https://${BuildConfig.TRACKING_WEB_HOST}/track.html?id=$sessionId"

fun buildShareMessage(label: String?, destinationAddress: String, url: String): String =
    "Hi! ${if (label != null) "For \"$label\", " else ""}please share your live location so I can track your trip to $destinationAddress and see your estimated arrival time. Your location is only shared after you agree: $url"
