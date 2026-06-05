package com.mapdir.app.ui.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapdir.app.data.model.Place
import com.mapdir.app.data.repository.PlaceRepository
import com.mapdir.app.ui.components.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Place List screen.
 *
 * Receives an optional `categorySlug` via [SavedStateHandle] from navigation
 * and loads places filtered by that category. Supports additional search via query param.
 */
class PlaceListViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: PlaceRepository = PlaceRepository()
) : ViewModel() {

    /** Category slug passed from navigation (nullable = show all). */
    val categorySlug: String? = savedStateHandle.get<String>("categorySlug")

    /** Category display name for the TopAppBar title. */
    val categoryName: String = savedStateHandle.get<String>("categoryName") ?: "Semua Tempat"

    // ── State ──────────────────────────────────────────────────────────────────
    private val _placesState = MutableStateFlow<UiState<List<Place>>>(UiState.Loading)
    val placesState: StateFlow<UiState<List<Place>>> = _placesState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadPlaces()
    }

    // ── Public actions ─────────────────────────────────────────────────────────

    fun onSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            loadPlaces()
        }
    }

    fun retry() {
        loadPlaces()
    }

    // ── Private ────────────────────────────────────────────────────────────────

    private fun loadPlaces() {
        viewModelScope.launch {
            _placesState.value = UiState.Loading

            val query = _searchQuery.value.takeIf { it.isNotBlank() }

            repository.getPlaces(category = categorySlug, query = query)
                .onSuccess { (places, _) ->
                    _placesState.value = if (places.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(places)
                    }
                }
                .onFailure { error ->
                    _placesState.value = UiState.Error(
                        error.message ?: "Gagal memuat tempat"
                    )
                }
        }
    }
}
