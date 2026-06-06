package com.mapdir.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Manages routing/navigation intents to open maps application.
 * Supports both geo: URI scheme and Google Maps URL.
 */
object RoutingManager {
    /**
     * Open route to destination in maps application.
     * Uses geo: URI scheme which works with any maps app.
     *
     * @param context Android context
     * @param latitude Destination latitude
     * @param longitude Destination longitude
     * @param label Optional label for the destination (shown in map)
     */
    fun openRoute(
        context: Context,
        latitude: Double,
        longitude: Double,
        label: String? = null
    ) {
        try {
            val uri = if (label != null) {
                Uri.parse("geo:$latitude,$longitude?q=$label")
            } else {
                Uri.parse("geo:$latitude,$longitude")
            }

            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }

            // Try Google Maps first
            try {
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                // Google Maps not installed, fall back to any maps app
            }

            // Fall back to any maps application
            val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(fallbackIntent)
        } catch (e: Exception) {
            // No maps app available - in real app, show error message
        }
    }

    /**
     * Open route with turn-by-turn navigation (Google Maps specific).
     * Requires Google Maps to be installed.
     *
     * @param context Android context
     * @param destLatitude Destination latitude
     * @param destLongitude Destination longitude
     * @param destLabel Label for destination
     */
    fun openNavigationGoogleMaps(
        context: Context,
        destLatitude: Double,
        destLongitude: Double,
        destLabel: String
    ) {
        try {
            val url = "https://www.google.com/maps/dir/?api=1&destination=$destLatitude,$destLongitude&destination_place_id=$destLabel"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to geo: scheme
            openRoute(context, destLatitude, destLongitude, destLabel)
        }
    }
}
