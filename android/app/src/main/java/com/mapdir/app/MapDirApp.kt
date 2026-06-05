package com.mapdir.app

import android.app.Application

/**
 * Custom [Application] class for the Map Directory app.
 *
 * Configured in AndroidManifest.xml (`android:name=".MapDirApp"`).
 * Can be used for initializing dependency injection frameworks (like Hilt),
 * analytics, or local caching databases later.
 */
class MapDirApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialization code here
    }
}
