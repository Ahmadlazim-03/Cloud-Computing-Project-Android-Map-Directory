package com.mapdir.app.ui.map

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapdir.app.data.model.Place
import com.mapdir.app.data.repository.PlaceRepository
import com.mapdir.app.util.DistanceCalculator
import com.mapdir.app.util.LocationManager
import com.mapdir.app.util.RoutingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State for the Map screen.
 */
data class MapScreenState(
    val places: List<Place> = emptyList(),
    val userLocation: Location? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasLocationPermission: Boolean = false,
    val isGpsEnabled: Boolean = false,
    val placesWithDistance: List<PlaceWithDistance> = emptyList()
)

/**
 * Place model with calculated distance from user.
 */
data class PlaceWithDistance(
    val place: Place,
    val distanceM: Double? = null
) {
    val distanceFormatted: String
        get() = DistanceCalculator.formatDistance(distanceM)
}

/**
 * ViewModel for Map Screen.
 * Manages places, user location, permissions, and distance calculations.
 */
class MapViewModel(
    private val placeRepository: PlaceRepository,
    private val locationManager: LocationManager,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(MapScreenState())
    val state: StateFlow<MapScreenState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            loadPlaces()
            checkLocationPermissions()
        }
    }

    /**
     * Load places from repository.
     */
    private suspend fun loadPlaces() {
        _state.value = _state.value.copy(isLoading = true)
        try {
            val result = placeRepository.getPlaces()
            result.onSuccess { (places, _) ->
                _state.value = _state.value.copy(places = places, isLoading = false)
            }.onFailure { exception ->
                _state.value = _state.value.copy(
                    errorMessage = exception.message ?: "Failed to load places",
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                errorMessage = e.message ?: "Failed to load places",
                isLoading = false
            )
        }
    }

    /**
     * Check location permissions and GPS status.
     */
    private fun checkLocationPermissions() {
        val hasPermission = locationManager.hasLocationPermissions()
        val isGpsEnabled = locationManager.isGpsEnabled()
        _state.value = _state.value.copy(
            hasLocationPermission = hasPermission,
            isGpsEnabled = isGpsEnabled
        )
    }

    /**
     * Fetch user location and update places with distance.
     */
    fun updateUserLocation() {
        if (!_state.value.hasLocationPermission) {
            _state.value = _state.value.copy(
                errorMessage = "Location permission required"
            )
            return
        }

        viewModelScope.launch {
            try {
                val location = locationManager.getCurrentLocation()
                if (location != null) {
                    _state.value = _state.value.copy(userLocation = location)
                    updateDistances(location)
                } else {
                    _state.value = _state.value.copy(
                        errorMessage = "Could not get location"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = e.message ?: "Location error"
                )
            }
        }
    }

    /**
     * Calculate distances from user location to all places.
     */
    private fun updateDistances(userLocation: Location) {
        val placesWithDistance = _state.value.places.map { place ->
            val distance = if (place.latitude != null && place.longitude != null) {
                DistanceCalculator.distanceTo(userLocation, place.latitude, place.longitude)
            } else {
                null
            }
            PlaceWithDistance(place, distance)
        }
        _state.value = _state.value.copy(placesWithDistance = placesWithDistance)
    }

    /**
     * Open route to destination using maps app.
     */
    fun openRoute(latitude: Double, longitude: Double, label: String) {
        RoutingManager.openRoute(context, latitude, longitude, label)
    }

    /**
     * Retry location permissions check (after user grants permission).
     */
    fun retryLocationPermissions() {
        checkLocationPermissions()
        if (_state.value.hasLocationPermission) {
            updateUserLocation()
        }
    }
}
