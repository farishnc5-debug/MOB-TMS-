package com.example.tracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.tracker.ui.HomeScreen
import com.example.tracker.ui.RecipientScreen
import com.example.tracker.ui.Route
import com.example.tracker.ui.SessionDetailScreen
import com.example.tracker.ui.theme.TrackerTheme

/**
 * Single-activity app. A tracking link (https://<host>/track.html?id=... or the
 * yallamuvtracker://track?id=... test scheme - see AndroidManifest.xml) opens straight into
 * RecipientScreen; launching normally from the home screen opens the sender's HomeScreen.
 */
class MainActivity : ComponentActivity() {
    private var route by mutableStateOf<Route>(Route.Home)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        route = routeFromIntent(intent) ?: Route.Home

        setContent {
            TrackerTheme {
                when (val current = route) {
                    is Route.Home -> HomeScreen(onOpenSession = { id -> route = Route.SessionDetail(id) })
                    is Route.SessionDetail -> SessionDetailScreen(sessionId = current.id, onBack = { route = Route.Home })
                    is Route.Recipient -> RecipientScreen(sessionId = current.id)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        routeFromIntent(intent)?.let { route = it }
    }

    private fun routeFromIntent(intent: Intent?): Route? {
        val data: Uri = intent?.data ?: return null
        val id = data.getQueryParameter("id") ?: return null
        return Route.Recipient(id)
    }
}
