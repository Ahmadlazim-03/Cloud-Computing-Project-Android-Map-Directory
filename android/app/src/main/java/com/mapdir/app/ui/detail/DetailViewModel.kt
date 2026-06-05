package com.mapdir.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapdir.app.data.model.Place
import com.mapdir.app.data.repository.PlaceRepository
import com.mapdir.app.ui.components.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Place Detail screen.
 *
 * Receives `placeId` from navigation and loads the full place detail.
 */
class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: PlaceRepository = PlaceRepository()
) : ViewModel() {

    private val placeId: Int = savedStateHandle.get<Int>("placeId") ?: -1

    private val _detailState = MutableStateFlow<UiState<Place>>(UiState.Loading)
    val detailState: StateFlow<UiState<Place>> = _detailState.asStateFlow()

    init {
        loadDetail()
    }

    fun retry() {
        loadDetail()
    }

    private fun loadDetail() {
        if (placeId < 0) {
            _detailState.value = UiState.Error("ID tempat tidak valid")
            return
        }

        viewModelScope.launch {
            _detailState.value = UiState.Loading

            repository.getPlaceDetail(placeId)
                .onSuccess { place ->
                    _detailState.value = UiState.Success(place)
                }
                .onFailure { error ->
                    _detailState.value = UiState.Error(
                        error.message ?: "Gagal memuat detail tempat"
                    )
                }
        }
    }
}
