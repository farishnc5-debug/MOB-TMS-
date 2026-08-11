package com.example.tracker.data

import com.example.tracker.util.awaitResult
import com.example.tracker.util.generateSessionId
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Date

private const val COLLECTION = "tracking_sessions"
private const val SESSION_LIFETIME_MS = 24L * 60 * 60 * 1000

class TrackingRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    /** Creates a new trip and returns its session id (the same id used in the shareable link). */
    suspend fun createSession(destination: Destination, label: String?): String {
        val id = generateSessionId()
        val expiresAt = Timestamp(Date(System.currentTimeMillis() + SESSION_LIFETIME_MS))
        val data: Map<String, Any?> = mapOf(
            "destination" to destination.toFirestoreMap(),
            "label" to label,
            "createdAt" to FieldValue.serverTimestamp(),
            "expiresAt" to expiresAt,
            "status" to TrackingStatus.PENDING,
            "consentAt" to null,
            "location" to null,
        )
        db.collection(COLLECTION).document(id).set(data).awaitResult()
        return id
    }

    suspend fun getSessionOnce(id: String): TrackingSession? {
        val snap = db.collection(COLLECTION).document(id).get().awaitResult()
        return TrackingSession.fromSnapshot(snap)
    }

    /** Live updates for one session; emits null once the doc is missing/expired-and-deleted. */
    fun observeSession(id: String): Flow<TrackingSession?> = callbackFlow {
        val registration = db.collection(COLLECTION).document(id).addSnapshotListener { snap, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snap?.let { TrackingSession.fromSnapshot(it) })
        }
        awaitClose { registration.remove() }
    }

    suspend fun updateStatus(id: String, status: String) {
        db.collection(COLLECTION).document(id).update("status", status).awaitResult()
    }

    /** Recipient's first accepted fix: flips the trip to active and records consent + location. */
    suspend fun markActiveWithLocation(id: String, location: LocationPoint) {
        db.collection(COLLECTION).document(id).update(
            mapOf(
                "status" to TrackingStatus.ACTIVE,
                "consentAt" to FieldValue.serverTimestamp(),
                "location" to locationMap(location),
            )
        ).awaitResult()
    }

    suspend fun updateLocation(id: String, location: LocationPoint) {
        db.collection(COLLECTION).document(id).update("location", locationMap(location)).awaitResult()
    }

    private fun locationMap(l: LocationPoint): Map<String, Any?> = mapOf(
        "lat" to l.lat,
        "lng" to l.lng,
        "accuracy" to l.accuracy,
        "heading" to l.heading,
        "speed" to l.speed,
        "updatedAt" to FieldValue.serverTimestamp(),
    )
}
