package com.mapdir.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapdir.app.data.model.Category
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
 * ViewModel for the Home screen.
 *
 * Manages:
 * - Category list loading
 * - Place list loading (with search & category filter)
 * - Search query debouncing (300ms)
 * - Selected category state
 */
class HomeViewModel(
    private val repository: PlaceRepository = PlaceRepository()
) : ViewModel() {

    // ── Categories ─────────────────────────────────────────────────────────────
    private val _categoriesState = MutableStateFlow<UiState<List<Category>>>(UiState.Loading)
    val categoriesState: StateFlow<UiState<List<Category>>> = _categoriesState.asStateFlow()

    // ── Places ─────────────────────────────────────────────────────────────────
    private val _placesState = MutableStateFlow<UiState<List<Place>>>(UiState.Loading)
    val placesState: StateFlow<UiState<List<Place>>> = _placesState.asStateFlow()

    // ── Search & Filter ────────────────────────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadCategories()
        loadPlaces()
    }

    // ── Public actions ─────────────────────────────────────────────────────────

    fun onSearchQuery(query: String) {
        _searchQuery.value = query
        // Debounce search: wait 300ms after last keystroke
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            loadPlaces()
        }
    }

    fun onCategorySelected(slug: String?) {
        _selectedCategory.value = if (_selectedCategory.value == slug) null else slug
        loadPlaces()
    }

    fun retry() {
        loadCategories()
        loadPlaces()
    }

    // ── Private loaders ────────────────────────────────────────────────────────

    private fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = UiState.Loading
            repository.getCategories()
                .onSuccess { categories ->
                    _categoriesState.value = if (categories.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(categories)
                    }
                }
                .onFailure { error ->
                    _categoriesState.value = UiState.Error(
                        error.message ?: "Gagal memuat kategori"
                    )
                }
        }
    }

    private fun loadPlaces() {
        viewModelScope.launch {
            _placesState.value = UiState.Loading

            val query = _searchQuery.value.takeIf { it.isNotBlank() }
            val category = _selectedCategory.value

            repository.getPlaces(category = category, query = query)
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
