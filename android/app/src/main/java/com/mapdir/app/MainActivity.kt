package com.mapdir.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mapdir.app.data.repository.PlaceRepository
import com.mapdir.app.ui.navigation.AppNavigation
import com.mapdir.app.ui.theme.MapDirTheme

/**
 * Single activity entry point for the application.
 *
 * Configured with edge-to-edge system bars and displays [AppNavigation].
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val placeRepository = PlaceRepository()

        setContent {
            MapDirTheme {
                AppNavigation(
                    onOpenRoute = { latitude, longitude ->
                        handleOpenRoute(latitude, longitude)
                    },
                    placeRepository = placeRepository
                )
            }
        }
    }

    /**
     * Fallback callback implementation for opening a route.
     *
     * Launches an implicit Intent to Google Maps or any compatible mapping app.
     * The Maps/GPS teammate can replace this logic or integrate their in-app route map.
     */
    private fun handleOpenRoute(latitude: Double, longitude: Double) {
        val uri = "google.navigation:q=$latitude,$longitude"
        val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            setPackage("com.google.android.apps.maps")
        }

        try {
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // If official Google Maps isn't installed, try a generic geo intent
                val genericGeoUri = "geo:$latitude,$longitude?q=$latitude,$longitude"
                val genericIntent = Intent(Intent.ACTION_VIEW, Uri.parse(genericGeoUri))
                startActivity(genericIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Tidak ada aplikasi peta yang mendukung rute ini: Lat $latitude, Lng $longitude",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
