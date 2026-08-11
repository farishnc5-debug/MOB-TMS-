package com.example.tracker.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.tracker.data.Destination
import com.example.tracker.data.LocalSessionEntry
import com.example.tracker.data.LocalSessionsStore
import com.example.tracker.data.TrackingRepository
import com.example.tracker.net.GeocodeResult
import com.example.tracker.net.RoutingService
import com.example.tracker.util.LatLng
import com.example.tracker.util.buildShareMessage
import com.example.tracker.util.buildTrackingUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val JEDDAH = LatLng(21.4858, 39.1925)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onOpenSession: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { TrackingRepository() }
    val localStore = remember { LocalSessionsStore(context) }
    val clipboard = LocalClipboardManager.current

    var localEntries by remember { mutableStateOf(localStore.list()) }
    val sessionStatuses = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(localEntries) {
        localEntries.forEach { entry ->
            launch {
                repository.observeSession(entry.id).collect { session ->
                    sessionStatuses[entry.id] = session?.status ?: "expired"
                }
            }
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<GeocodeResult>>(emptyList()) }
    var chosenDestination by remember { mutableStateOf<Destination?>(null) }
    var tripLabel by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedLink by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().length < 3) {
            searchResults = emptyList()
            return@LaunchedEffect
        }
        delay(400)
        searchResults = RoutingService.geocode(searchQuery)
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Live Tracking") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Set the destination", style = MaterialTheme.typography.labelLarge)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search for an address") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    searchResults.forEach { result ->
                        ListItem(
                            headlineContent = { Text(result.label, maxLines = 2) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    chosenDestination = Destination(result.lat, result.lng, result.label)
                                    searchQuery = ""
                                    searchResults = emptyList()
                                },
                        )
                    }
                    Text(
                        "Or tap the map to drop a pin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TrackingMapView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        initialCenter = chosenDestination?.let { LatLng(it.lat, it.lng) } ?: JEDDAH,
                        destination = chosenDestination?.let { LatLng(it.lat, it.lng) },
                        onMapTap = { tapped ->
                            chosenDestination = Destination(tapped.lat, tapped.lng, "Looking up address…")
                            scope.launch {
                                val address = RoutingService.reverseGeocode(tapped)
                                    ?: "${tapped.lat.format5()}, ${tapped.lng.format5()}"
                                chosenDestination = Destination(tapped.lat, tapped.lng, address)
                            }
                        },
                    )
                    chosenDestination?.let {
                        Text(it.address, style = MaterialTheme.typography.bodyMedium)
                    }

                    OutlinedTextField(
                        value = tripLabel,
                        onValueChange = { tripLabel = it },
                        label = { Text("Trip label (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Recipient's WhatsApp number (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Button(
                        onClick = {
                            val dest = chosenDestination ?: return@Button
                            isGenerating = true
                            scope.launch {
                                val id = repository.createSession(dest, tripLabel.ifBlank { null })
                                localStore.add(
                                    LocalSessionEntry(
                                        id = id,
                                        label = tripLabel.ifBlank { dest.address },
                                        createdAtMillis = System.currentTimeMillis(),
                                    )
                                )
                                localEntries = localStore.list()
                                val url = buildTrackingUrl(id)
                                generatedLink = url
                                isGenerating = false

                                val message = buildShareMessage(tripLabel.ifBlank { null }, dest.address, url)
                                shareViaWhatsApp(context, message, phone)
                            }
                        },
                        enabled = chosenDestination != null && !isGenerating,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (isGenerating) "Generating…" else "Generate & share via WhatsApp")
                    }

                    generatedLink?.let { link ->
                        Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(link, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                OutlinedButton(onClick = { clipboard.setText(AnnotatedString(link)) }) {
                                    Text("Copy")
                                }
                            }
                        }
                    }
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Your tracking links", style = MaterialTheme.typography.labelLarge)
                    if (localEntries.isEmpty()) {
                        Text(
                            "No tracking links yet — generate one above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        LazyColumn(modifier = Modifier.height((localEntries.size * 72).coerceAtMost(400).dp)) {
                            items(localEntries, key = { it.id }) { entry ->
                                ListItem(
                                    headlineContent = { Text(entry.label) },
                                    supportingContent = { Text(sessionStatuses[entry.id] ?: "…") },
                                    trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenSession(entry.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Double.format5(): String = "%.5f".format(this)

private fun shareViaWhatsApp(context: Context, message: String, phone: String) {
    val digits = phone.filter { it.isDigit() }
    if (digits.isNotBlank()) {
        val uri = Uri.parse("https://wa.me/$digits?text=${Uri.encode(message)}")
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            return
        } catch (e: ActivityNotFoundException) {
            // fall through to a generic share sheet below
        }
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
        setPackage("com.whatsapp")
    }
    try {
        context.startActivity(sendIntent)
    } catch (e: ActivityNotFoundException) {
        val chooser = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(chooser, null))
    }
}
