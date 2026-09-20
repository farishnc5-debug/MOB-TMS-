package com.example.tracker

import android.app.Application
import org.osmdroid.config.Configuration
import java.io.File

class TrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().apply {
            userAgentValue = packageName
            // App-private cache dir - avoids requesting storage permissions for tile caching.
            osmdroidBasePath = File(cacheDir, "osmdroid").apply { mkdirs() }
            osmdroidTileCache = File(osmdroidBasePath, "tiles").apply { mkdirs() }
        }
    }
}
