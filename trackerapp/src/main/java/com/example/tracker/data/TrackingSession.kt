package com.example.tracker.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

// Mirrors the tracking_sessions/{id} schema written by web/js/dashboard.js and web/js/track.js,
// so links and trips are interchangeable between the web app and this Android app - both are
// just clients of the same Firestore collection (see /firestore.rules).

data class Destination(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val address: String = "",
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf("lat" to lat, "lng" to lng, "address" to address)
}

data class LocationPoint(
    val lat: Double,
    val lng: Double,
    val accuracy: Double? = null,
    val heading: Double? = null,
    val speed: Double? = null,
)

object TrackingStatus {
    const val PENDING = "pending"
    const val ACTIVE = "active"
    const val DECLINED = "declined"
    const val CLOSED = "closed"
    const val ARRIVED = "arrived"
}

data class TrackingSession(
    val id: String,
    val destination: Destination,
    val label: String?,
    val createdAt: Timestamp?,
    val expiresAt: Timestamp?,
    val status: String,
    val consentAt: Timestamp?,
    val location: LocationPointWithTime?,
) {
    val isExpired: Boolean
        get() = expiresAt != null && expiresAt.toDate().before(java.util.Date())

    companion object {
        // Manual parsing (rather than DocumentSnapshot#toObject) keeps this independent of
        // Kotlin/Firestore POJO-mapping reflection quirks and matches the nested-map shape used
        // by the web app exactly.
        fun fromSnapshot(snapshot: DocumentSnapshot): TrackingSession? {
            if (!snapshot.exists()) return null
            val data = snapshot.data ?: return null

            @Suppress("UNCHECKED_CAST")
            val destMap = data["destination"] as? Map<String, Any?> ?: return null
            val destination = Destination(
                lat = (destMap["lat"] as? Number)?.toDouble() ?: return null,
                lng = (destMap["lng"] as? Number)?.toDouble() ?: return null,
                address = destMap["address"] as? String ?: "",
            )

            @Suppress("UNCHECKED_CAST")
            val locMap = data["location"] as? Map<String, Any?>
            val location = locMap?.let {
                val lat = (it["lat"] as? Number)?.toDouble()
                val lng = (it["lng"] as? Number)?.toDouble()
                if (lat == null || lng == null) null
                else LocationPointWithTime(
                    lat = lat,
                    lng = lng,
                    accuracy = (it["accuracy"] as? Number)?.toDouble(),
                    heading = (it["heading"] as? Number)?.toDouble(),
                    speed = (it["speed"] as? Number)?.toDouble(),
                    updatedAt = it["updatedAt"] as? Timestamp,
                )
            }

            return TrackingSession(
                id = snapshot.id,
                destination = destination,
                label = data["label"] as? String,
                createdAt = data["createdAt"] as? Timestamp,
                expiresAt = data["expiresAt"] as? Timestamp,
                status = data["status"] as? String ?: TrackingStatus.PENDING,
                consentAt = data["consentAt"] as? Timestamp,
                location = location,
            )
        }
    }
}

data class LocationPointWithTime(
    val lat: Double,
    val lng: Double,
    val accuracy: Double?,
    val heading: Double?,
    val speed: Double?,
    val updatedAt: Timestamp?,
)
