package com.mapdir.app.ui.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.mapdir.app.data.repository.PlaceRepository
import com.mapdir.app.util.LocationManager
import android.content.pm.PackageManager

/**
 * Map Screen - displays Google Maps with markers for all places.
 * Handles location permissions, user location, and routing.
 */
@Composable
fun MapScreen(
    placeRepository: PlaceRepository,
    viewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(placeRepository, LocalContext.current)
    ),
    onOpenRoute: ((latitude: Double, longitude: Double) -> Unit)? = null
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val cameraPositionState = rememberCameraPositionState()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.retryLocationPermissions()
        }
    }

    LaunchedEffect(Unit) {
        if (!state.hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.updateUserLocation()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Google Map
        if (state.places.isNotEmpty()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapType = MapType.NORMAL)
            ) {
                // Render user location marker
                state.userLocation?.let { location ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(location.latitude, location.longitude)
                        ),
                        title = "Lokasi Saya",
                        snippet = "Posisi anda saat ini"
                    )
                }

                // Render place markers
                state.placesWithDistance.forEach { placeWithDistance ->
                    val place = placeWithDistance.place
                    if (place.latitude != null && place.longitude != null) {
                        Marker(
                            state = MarkerState(
                                position = LatLng(place.latitude, place.longitude)
                            ),
                            title = place.name,
                            snippet = buildString {
                                append(place.category?.name ?: "N/A")
                                if (placeWithDistance.distanceM != null) {
                                    append(" • ")
                                    append(placeWithDistance.distanceFormatted)
                                }
                            },
                            onClick = {
                                // Move camera to marker
                                val cameraUpdate = CameraUpdateFactory.newLatLng(
                                    LatLng(place.latitude, place.longitude)
                                )
                                cameraPositionState.move(cameraUpdate)
                                true
                            }
                        )
                    }
                }
            }

            // Initial camera position
            LaunchedEffect(state.userLocation) {
                if (state.userLocation != null) {
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        LatLng(state.userLocation.latitude, state.userLocation.longitude),
                        15f
                    )
                    cameraPositionState.move(cameraUpdate)
                }
            }
        } else if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text("Memuat peta...")
            }
        }

        // Error Message
        if (state.errorMessage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // GPS Status & Buttons
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // GPS Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (state.isGpsEnabled) Color.Green else Color.Red,
                        modifier = Modifier.padding(4.dp)
                    )
                    Text(
                        text = if (state.isGpsEnabled) "GPS: Aktif" else "GPS: Tidak Aktif",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Permission Status
                Text(
                    text = if (state.hasLocationPermission) "Izin Lokasi: ✓" else "Izin Lokasi: ✗",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.hasLocationPermission) Color.Green else Color.Red
                )
            }
        }

        // My Location Button
        IconButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50.dp)
                ),
            onClick = {
                viewModel.updateUserLocation()
            }
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Lokasi Saya",
                tint = Color.White
            )
        }

        // Places Info Panel (if any place selected, or show list)
        if (state.placesWithDistance.isNotEmpty()) {
            PlacesListPanel(
                places = state.placesWithDistance,
                onPlaceClick = { place ->
                    if (place.latitude != null && place.longitude != null) {
                        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                            LatLng(place.latitude, place.longitude),
                            17f
                        )
                        cameraPositionState.move(cameraUpdate)
                    }
                },
                onRouteClick = { place ->
                    if (place.latitude != null && place.longitude != null) {
                        onOpenRoute?.invoke(place.latitude, place.longitude)
                            ?: viewModel.openRoute(
                                place.latitude,
                                place.longitude,
                                place.name
                            )
                    }
                }
            )
        }
    }
}

/**
 * Panel showing list of places at bottom of screen.
 */
@Composable
private fun PlacesListPanel(
    places: List<PlaceWithDistance>,
    onPlaceClick: (Place) -> Unit,
    onRouteClick: (Place) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
            )
            .align(Alignment.TopStart)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Tempat di Sekitar (${places.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            places.take(3).forEach { placeWithDistance ->
                PlaceRowItem(
                    place = placeWithDistance.place,
                    distance = placeWithDistance.distanceFormatted,
                    onPlaceClick = { onPlaceClick(placeWithDistance.place) },
                    onRouteClick = { onRouteClick(placeWithDistance.place) }
                )
            }

            if (places.size > 3) {
                Text(
                    text = "+ ${places.size - 3} lainnya",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Single place row in the list.
 */
@Composable
private fun PlaceRowItem(
    place: Place,
    distance: String,
    onPlaceClick: () -> Unit,
    onRouteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onPlaceClick)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "${place.category?.name} • $distance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        IconButton(
            onClick = onRouteClick,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = "Buka Rute",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
