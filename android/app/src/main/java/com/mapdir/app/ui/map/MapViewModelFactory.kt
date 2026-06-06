package com.mapdir.app.ui.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mapdir.app.data.repository.PlaceRepository
import com.mapdir.app.util.LocationManager

/**
 * Factory for creating MapViewModel with required dependencies.
 */
class MapViewModelFactory(
    private val placeRepository: PlaceRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            val locationManager = LocationManager(context)
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(placeRepository, locationManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
