package com.example.tracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tracker.data.TrackingRepository
import com.example.tracker.data.TrackingStatus
import com.example.tracker.net.RouteResult
import com.example.tracker.net.RoutingService
import com.example.tracker.util.LatLng
import com.example.tracker.util.formatDistance
import com.example.tracker.util.formatDuration
import com.example.tracker.util.formatEta
import com.example.tracker.util.haversineMeters
import kotlinx.coroutines.launch

private val statusMessage = mapOf(
    TrackingStatus.PENDING to "Waiting for the recipient to open the link and agree to share their location.",
    TrackingStatus.ACTIVE to "Recipient is sharing their live location.",
    TrackingStatus.DECLINED to "Recipient declined to share their location.",
    TrackingStatus.CLOSED to "This trip was closed.",
    TrackingStatus.ARRIVED to "Recipient has arrived at the destination.",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(sessionId: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val repository = remember { TrackingRepository() }
    val session by repository.observeSession(sessionId).collectAsStateWithLifecycle(initialValue = null)

    var route by remember { mutableStateOf<RouteResult?>(null) }
    var lastRouteFetch by remember { mutableStateOf<Pair<LatLng, Long>?>(null) }

    LaunchedEffect(session?.location?.lat, session?.location?.lng) {
        val loc = session?.location ?: return@LaunchedEffect
        val dest = session?.destination ?: return@LaunchedEffect
        val here = LatLng(loc.lat, loc.lng)
        val destPoint = LatLng(dest.lat, dest.lng)
        val last = lastRouteFetch
        val moved = last == null || haversineMeters(last.first, here) > 40
        val stale = last == null || System.currentTimeMillis() - last.second > 25_000
        if (moved || stale) {
            lastRouteFetch = here to System.currentTimeMillis()
            route = RoutingService.getRoute(here, destPoint)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.label ?: "Tracking") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { padding ->
        val current = session
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (current == null) {
                Text("This tracking link has expired or was removed.")
                return@Column
            }

            statusMessage[current.status]?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }

            TrackingMapView(
                modifier = Modifier.fillMaxWidth().weight(1f),
                initialCenter = LatLng(current.destination.lat, current.destination.lng),
                destination = LatLng(current.destination.lat, current.destination.lng),
                live = current.location?.let { LatLng(it.lat, it.lng) },
                routePoints = route?.points ?: emptyList(),
            )

            Card {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatLabel("Distance", formatDistance(route?.distanceMeters))
                    StatLabel("ETA", route?.let { "${formatDuration(it.durationSeconds)} (${formatEta(it.durationSeconds)})" } ?: "—")
                }
            }

            if (current.status == TrackingStatus.PENDING || current.status == TrackingStatus.ACTIVE) {
                Button(
                    onClick = { scope.launch { repository.updateStatus(sessionId, TrackingStatus.CLOSED) } },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Close this trip")
                }
            }
        }
    }
}

@Composable
private fun StatLabel(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
