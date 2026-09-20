package com.example.tracker.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.tracker.util.LatLng
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
private fun rememberMapViewWithLifecycle(context: Context): MapView {
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }
    return mapView
}

private fun dotDrawable(context: Context, colorHex: String, sizeDp: Int = 22): Drawable {
    val density = context.resources.displayMetrics.density
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(Color.parseColor(colorHex))
        setStroke((2 * density).toInt(), Color.WHITE)
        setSize((sizeDp * density).toInt(), (sizeDp * density).toInt())
    }
}

/**
 * A minimal osmdroid map: an optional destination pin, an optional live position dot, an
 * optional route polyline between them, and an optional tap callback used only by the
 * destination picker (ui/HomeScreen.kt). No API key - OpenStreetMap tiles + OSRM routing.
 */
@Composable
fun TrackingMapView(
    modifier: Modifier = Modifier,
    initialCenter: LatLng,
    initialZoom: Double = 12.0,
    destination: LatLng? = null,
    live: LatLng? = null,
    routePoints: List<LatLng> = emptyList(),
    onMapTap: ((LatLng) -> Unit)? = null,
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle(context)

    val destMarker = remember { Marker(mapView).apply { setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) } }
    val liveMarker = remember {
        Marker(mapView).apply {
            icon = dotDrawable(context, "#22C55E")
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
    }
    val polyline = remember {
        Polyline().apply {
            outlinePaint.color = Color.parseColor("#4F8CFF")
            outlinePaint.strokeWidth = 8f
        }
    }
    var hasAutoFitted by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                controller.setZoom(initialZoom)
                controller.setCenter(GeoPoint(initialCenter.lat, initialCenter.lng))
                overlays.add(polyline)
                overlays.add(destMarker)
                overlays.add(liveMarker)
                if (onMapTap != null) {
                    val receiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            onMapTap(LatLng(p.latitude, p.longitude))
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint) = false
                    }
                    overlays.add(0, MapEventsOverlay(receiver))
                }
            }
        },
        update = { view ->
            destMarker.isEnabled = destination != null
            destMarker.setVisible(destination != null)
            destination?.let { destMarker.position = GeoPoint(it.lat, it.lng) }

            liveMarker.isEnabled = live != null
            liveMarker.setVisible(live != null)
            live?.let { liveMarker.position = GeoPoint(it.lat, it.lng) }

            polyline.setPoints(routePoints.map { GeoPoint(it.lat, it.lng) })

            if (!hasAutoFitted && destination != null && live != null) {
                hasAutoFitted = true
                val box = BoundingBox.fromGeoPoints(
                    listOf(GeoPoint(destination.lat, destination.lng), GeoPoint(live.lat, live.lng))
                )
                view.post { view.zoomToBoundingBox(box, true, 120) }
            } else if (destination != null && live == null) {
                view.controller.animateTo(GeoPoint(destination.lat, destination.lng))
            }

            view.invalidate()
        }
    )
}
