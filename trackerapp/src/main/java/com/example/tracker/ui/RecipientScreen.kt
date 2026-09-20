package com.example.tracker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.tracker.data.Destination
import com.example.tracker.data.LocationPoint
import com.example.tracker.data.TrackingRepository
import com.example.tracker.data.TrackingStatus
import com.example.tracker.location.LocationClient
import com.example.tracker.net.RouteResult
import com.example.tracker.net.RoutingService
import com.example.tracker.util.LatLng
import com.example.tracker.util.formatDistance
import com.example.tracker.util.formatDuration
import com.example.tracker.util.formatEta
import com.example.tracker.util.haversineMeters
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private enum class RecipientUiState { LOADING, ERROR, CONSENT, SHARING, DECLINED, ARRIVED, CLOSED }

private const val ARRIVAL_RADIUS_METERS = 150.0
private const val MIN_UPDATE_DISTANCE_METERS = 15.0
private const val MIN_UPDATE_INTERVAL_MS = 8000L

private fun Location.toPoint(): LocationPoint = LocationPoint(
    lat = latitude,
    lng = longitude,
    accuracy = if (hasAccuracy()) accuracy.toDouble() else null,
    heading = if (hasBearing()) bearing.toDouble() else null,
    speed = if (hasSpeed()) speed.toDouble() else null,
)

/** Reached via a deep link (see MainActivity) - mirrors web/track.html + web/js/track.js. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipientScreen(sessionId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { TrackingRepository() }
    val locationClient = remember { LocationClient(context) }

    var uiState by remember { mutableStateOf(RecipientUiState.LOADING) }
    var errorMessage by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf<Destination?>(null) }
    var label by remember { mutableStateOf<String?>(null) }
    var currentLatLng by remember { mutableStateOf<LatLng?>(null) }
    var route by remember { mutableStateOf<RouteResult?>(null) }
    var lastSent by remember { mutableStateOf<Pair<LatLng, Long>?>(null) }
    var watchJob by remember { mutableStateOf<Job?>(null) }

    fun handleFix(location: Location) {
        val dest = destination ?: return
        val here = LatLng(location.latitude, location.longitude)
        val destPoint = LatLng(dest.lat, dest.lng)
        currentLatLng = here

        val last = lastSent
        val movedEnough = last == null || haversineMeters(last.first, here) > MIN_UPDATE_DISTANCE_METERS
        val timeEnough = last == null || System.currentTimeMillis() - last.second > MIN_UPDATE_INTERVAL_MS
        if (movedEnough && timeEnough) {
            lastSent = here to System.currentTimeMillis()
            scope.launch {
                repository.updateLocation(sessionId, location.toPoint())
                route = RoutingService.getRoute(here, destPoint)
            }
        }

        if (haversineMeters(here, destPoint) < ARRIVAL_RADIUS_METERS) {
            watchJob?.cancel()
            scope.launch { repository.updateStatus(sessionId, TrackingStatus.ARRIVED) }
            uiState = RecipientUiState.ARRIVED
        }
    }

    fun startSharing(resuming: Boolean) {
        scope.launch {
            try {
                val loc = locationClient.getCurrentLocation()
                if (!resuming) repository.markActiveWithLocation(sessionId, loc.toPoint())
                uiState = RecipientUiState.SHARING
                handleFix(loc)
                watchJob = launch {
                    locationClient.observeLocationUpdates().collect { update -> handleFix(update) }
                }
            } catch (e: Exception) {
                errorMessage = "Couldn't get your location. Make sure location services are turned on and try again."
                uiState = RecipientUiState.ERROR
            }
        }
    }

    fun decline() {
        scope.launch { repository.updateStatus(sessionId, TrackingStatus.DECLINED) }
        uiState = RecipientUiState.DECLINED
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startSharing(resuming = false) else decline()
    }

    fun requestShare() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) startSharing(resuming = false) else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(sessionId) {
        val session = try {
            repository.getSessionOnce(sessionId)
        } catch (e: Exception) {
            null
        }
        when {
            session == null -> {
                errorMessage = "This tracking link no longer exists."
                uiState = RecipientUiState.ERROR
            }
            session.isExpired -> {
                errorMessage = "This tracking link has expired."
                uiState = RecipientUiState.ERROR
            }
            else -> {
                destination = session.destination
                label = session.label
                when (session.status) {
                    TrackingStatus.DECLINED -> uiState = RecipientUiState.DECLINED
                    TrackingStatus.CLOSED -> uiState = RecipientUiState.CLOSED
                    TrackingStatus.ARRIVED -> uiState = RecipientUiState.ARRIVED
                    TrackingStatus.ACTIVE -> {
                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED
                        if (granted) startSharing(resuming = true) else uiState = RecipientUiState.CONSENT
                    }
                    else -> uiState = RecipientUiState.CONSENT
                }
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(label ?: "Live Tracking") }) }) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = if (uiState == RecipientUiState.SHARING) Alignment.TopCenter else Alignment.Center,
        ) {
            when (uiState) {
                RecipientUiState.LOADING -> CircularProgressIndicator()
                RecipientUiState.ERROR -> MessageCard("⚠️ Link unavailable", errorMessage)
                RecipientUiState.CONSENT -> ConsentCard(
                    label = label,
                    destinationAddress = destination?.address ?: "",
                    onShare = { requestShare() },
                    onDecline = { decline() },
                )
                RecipientUiState.SHARING -> SharingContent(
                    destination = destination,
                    live = currentLatLng,
                    route = route,
                    onStop = {
                        watchJob?.cancel()
                        scope.launch { repository.updateStatus(sessionId, TrackingStatus.CLOSED) }
                        uiState = RecipientUiState.CLOSED
                    },
                )
                RecipientUiState.DECLINED -> MessageCard("🙈 Okay, no location was shared", "You can close this page.")
                RecipientUiState.ARRIVED -> MessageCard("🎉 You've arrived!", "Location sharing has stopped automatically.")
                RecipientUiState.CLOSED -> MessageCard("✅ This trip has ended", "Location sharing is no longer active for this link.")
            }
        }
    }
}

@Composable
private fun ConsentCard(label: String?, destinationAddress: String, onShare: () -> Unit, onDecline: () -> Unit) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("🚚", style = MaterialTheme.typography.displayMedium)
        Text("Someone wants to track this trip", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(
            buildString {
                if (!label.isNullOrBlank()) append("For \"$label\". ")
                append("Destination: $destinationAddress")
            },
            textAlign = TextAlign.Center,
        )
        Text(
            "If you agree, your live location will be shared until you stop sharing, you arrive, or the link expires (24 hours).",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onShare, modifier = Modifier.fillMaxWidth()) { Text("Share my location") }
        OutlinedButton(onClick = onDecline, modifier = Modifier.fillMaxWidth()) { Text("No thanks") }
    }
}

@Composable
private fun SharingContent(destination: Destination?, live: LatLng?, route: RouteResult?, onStop: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("You're sharing your live location.", style = MaterialTheme.typography.titleMedium)
        if (destination != null) {
            TrackingMapView(
                modifier = Modifier.fillMaxWidth().weight(1f),
                initialCenter = LatLng(destination.lat, destination.lng),
                destination = LatLng(destination.lat, destination.lng),
                live = live,
                routePoints = route?.points ?: emptyList(),
            )
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            StatBlock("Distance", formatDistance(route?.distanceMeters))
            StatBlock("ETA", route?.let { "${formatDuration(it.durationSeconds)} (${formatEta(it.durationSeconds)})" } ?: "—")
        }
        Button(onClick = onStop, modifier = Modifier.fillMaxWidth()) { Text("Stop sharing") }
    }
}

@Composable
private fun StatBlock(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun MessageCard(title: String, message: String) {
    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(message, textAlign = TextAlign.Center)
    }
}
